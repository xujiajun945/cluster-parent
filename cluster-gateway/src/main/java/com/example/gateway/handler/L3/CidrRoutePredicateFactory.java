package com.example.gateway.handler.L3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * <h2>L3 路由谓词层：判断是否放行</h2>
 * <p>
 *     必须注册为 Bean，Gateway 会自动收集所有 RoutePredicateFactory
 *     全局`RoutePredicateHandlerMapping` 遍历所有 `RouteDefinition`，用 `RoutePredicate` 判断命中，命中才进入 L4。
 * </p>
 *
 * @author xujiajun
 * @since 2026/11/14
 */
@Component
public class CidrRoutePredicateFactory extends AbstractRoutePredicateFactory<CidrRoutePredicateFactory.Config> {

    // 真实 IP 藏在代理头里
    private static final String XFF = "X-Forwarded-For";

    /**
     * 构造函数必须有，用于创建配置类实例
     */
    public CidrRoutePredicateFactory() {
        super(Config.class);
    }

    /**
     * 短参数配置，支持 `- Cidr=192.168.0.0/16,10.8.0.0/16`
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("cidrs");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        // ★ 关键：CIDR 在路由构建时解析一次，不是每个请求都解析
        List<IpCidr> ranges = config.getCidrs().stream()
                // 这里做一次字符串 → 网段对象的转换
                .map(IpCidr::new)
                .toList();

        // 每个请求都执行，必须极轻
        return exchange -> {
            // 取真实客户端 IP
            InetAddress client = clientIp(exchange);
            // 拿不到就不匹配，直接放弃这条路由
            if (client == null) {
                return false;
            }
            // 命中任意一个网段即通过
            return ranges.stream().anyMatch(r -> r.contains(client));
        };
    }

    /**
     * 优先取 X-Forwarded-For 的第一个地址；没有才用连接地址。
     * 这点很重要：前面挂了 Nginx / SLB 时，getRemoteAddress() 拿到的是代理的内网 IP。
     */
    private InetAddress clientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst(XFF);
        if (xff != null && !xff.isBlank()) {
            // 取最左边的，即客户端
            String first = xff.split(",")[0].trim();
            try {
                // 字面量 IP，不会触发 DNS
                return InetAddress.getByName(first);
            } catch (Exception ignored) {
                return null;
            }
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote == null ? null : remote.getAddress();
    }

    @Setter
    @Getter
    public static class Config {

        // 支持 IPv4 / IPv6 网段与单 IP
        private List<String> cidrs = new ArrayList<>();
    }

    /**
     * 极简 CIDR 实现，够用于路由匹配
     */
    private static class IpCidr {

        private final byte[] network;

        private final int prefix;

        IpCidr(String cidr) {
            String[] parts = cidr.split("/");
            this.network = toBytes(parts[0]);
            this.prefix = parts.length > 1 ? Integer.parseInt(parts[1]) : network.length * 8;
        }

        boolean contains(InetAddress addr) {
            byte[] target = addr.getAddress();
            // IPv4 与 IPv6 不能互相匹配
            if (target.length != network.length) {
                return false;
            }
            int bits = prefix;
            for (int i = 0; i < target.length && bits > 0; i++) {
                int mask = bits >= 8 ? 0xFF : (0xFF << (8 - bits)) & 0xFF;
                // 前缀位不同即不匹配
                if ((target[i] & mask) != (network[i] & mask)) {
                    return false;
                }
                bits -= 8;
            }
            return true;
        }

        private static byte[] toBytes(String ip) {
            try {
                return InetAddress.getByName(ip).getAddress();
            } catch (Exception e) {
                throw new IllegalArgumentException("非法 IP: " + ip, e);
            }
        }
    }
}