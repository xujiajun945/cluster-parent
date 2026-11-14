# Spring Cloud Gateway 拦截器分层详解

> Spring Cloud Gateway 的过滤/拦截体系，按请求进入顺序逐层拆解，含可运行代码与逐行注释。

---

## 目录

1. [概要](#概要)
2. [L1 Netty 层：连接级拦截](#l1-netty-层连接级拦截)
3. [L2 WebFilter 层：所有请求的第一道](#l2-webfilter-层所有请求的第一道)
4. [L3 路由谓词层：判断是否放行](#l3-路由谓词层判断是否放行)
5. [L4 GlobalFilter / GatewayFilter 层：真正的"拦截器"](#l4-globalfilter--gatewayfilter-层真正的拦截器)
   - [4.1 GlobalFilter 实现鉴权](#41-globalfilter-实现鉴权最常用)
   - [4.2 自定义 GatewayFilterFactory](#42-自定义-gatewayfilterfactory路由级拦截)
   - [4.3 改写请求体](#43-改写请求体鉴权后透传用户信息报文加解密)
   - [4.4 改写响应体](#44-改写响应体)
6. [L5 转发层：链尾的内置 filter](#l5-转发层链尾的内置-filter)
7. [L6 异常处理层：统一错误返回](#l6-异常处理层统一错误返回)
8. [按需求选型速查](#按需求选型速查)
9. [顺序与坑清单](#顺序与坑清单)

---

## 概要

Gateway 是 **WebFlux 栈**，请求从入站到出站会经过 **6 层**横向拦截点；`HandlerInterceptor` 属于第 0 层（Servlet/MVC 层），在 Gateway 中根本不存在。真正可控的分层如下：

| 层 | 组件 | 运行位置 | 典型用途 |
|---|---|---|---|
| L1 | Netty `ChannelHandler` / `NettyServerCustomizer` | Reactor Netty pipeline | 协议级、连接级、TCP 层埋点 |
| L2 | `WebFilter`（含 `SecurityWebFilterChain`） | WebFlux `WebHandler` 链最外层 | 全局日志、CORS、安全头、Spring Security 认证 |
| L3 | `RoutePredicateHandlerMapping` + `RoutePredicate` | 路由匹配 | 路由命中判断（Path/Host/Header/Method…） |
| L4 | `GlobalFilter` + `GatewayFilter` | `FilteringWebHandler` 过滤链 | 鉴权、限流、熔断、重试、改写请求/响应 |
| L5 | `NettyRoutingFilter` / `ReactiveLoadBalancerClientFilter` | 过滤链尾部 | 真正的转发、负载均衡、写回响应 |
| L6 | `ErrorWebExceptionHandler` / `ServerWebExchange` decorator | 异常与响应阶段 | 统一错误体、响应体修改 |

**一句话记忆**：**L2 管"所有进入网关的请求"，L4 管"命中路由后的请求"**。在网关语义上的"拦截器"，99% 都应该落在 L4。

---

## L1 Netty 层：连接级拦截

最底层，用于获取 TCP 连接、心跳、协议升级这些信息。`WebFilter` 拿不到 remote 端口与原始 pipeline。

```java
@Component                                                          // 注册为 Spring Bean，Spring Boot 会自动应用
public class NettyPipelineCustomizer implements NettyServerCustomizer {  // Spring Boot 提供的 Netty 定制扩展点
    @Override
    public HttpServer apply(HttpServer server) {                    // server 是 Reactor Netty 的 HttpServer 实例
        return server.doOnConnection(conn ->                       // 每建立一条 TCP 连接时回调
                conn.addHandlerLast("connCounter", new ChannelInboundHandlerAdapter() {  // 在 pipeline 末尾追加 handler
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {  // 连接建立完成
                        log.info("new conn from {}", ctx.channel().remoteAddress());          // 拿到真实对端 IP:PORT
                        super.channelActive(ctx);                                             // 交给后续 handler，不能吞掉事件
                    }
                }));
    }
}
```

**注意**：这里的 handler 在 HTTP 解析之前执行，不能修改 `ServerWebExchange`，只能处理 `ByteBuf`。大多数业务不需要写到这一层。

---

## L2 WebFilter 层：所有请求的第一道

`WebFilter` 是 WebFlux 版的 `javax.servlet.Filter`，作用于整个 `WebHandler` 链，**先于路由匹配**。CORS、traceId、访问日志和 Spring Security 的 `SecurityWebFilterChain` 都在这一层。

```java
@Component                                                          // 交给 Spring 管理
@Order(Ordered.HIGHEST_PRECEDENCE)                                  // 排序值越小越先执行，-2147483648 是最前
public class TraceWebFilter implements WebFilter {                   // 实现 WebFlux 过滤器接口
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {  // exchange 是当前请求上下文
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);  // 生成本次请求链路 ID
        ServerHttpRequest req = exchange.getRequest()                // 取原始请求
                .mutate()                                            // 拿到一个可修改的 Builder
                .header("X-Trace-Id", traceId)                        // 注入链路 ID 到请求头
                .build();                                            // 构建新的不可变 request
        MDC.put("traceId", traceId);                                 // 写入日志上下文（日志框架才能打出来）
        long start = System.currentTimeMillis();                     // 记录起点用于计时
        return chain.filter(exchange.mutate().request(req).build())   // 放行到下一层，请求头已带 traceId
                .doFinally(sig -> {                                  // doFinally 在整条链结束后执行（响应完成/取消/异常都会触发）
                    long cost = System.currentTimeMillis() - start;   // 计算耗时
                    log.info("{} {} -> {} {}ms", traceId,               // 打印方法
                            exchange.getRequest().getMethod(),          // 打印 HTTP 方法
                            exchange.getResponse().getStatusCode(),     // 打印响应状态码
                            cost);                                     // 打印耗时
                    MDC.remove("traceId");                             // 清理 ThreadLocal，防止线程复用污染
                });
    }
}
```

**关键点**：`exchange.mutate().request(...)` 是"请求改造"的统一手法；`chain.filter(...)` 之前是请求阶段，`doFinally` / `.then(...)` 之后是响应阶段（洋葱模型）。

**请求阶段常见误用**：在 `WebFilter` 里 `getResponse().setStatusCode()` 后不调 `chain.filter()`，会**跳过整个 Gateway**（连路由都不走），这有时是想要的短路，有时会静默吞掉请求。

---

## L3 路由谓词层：判断是否放行

严格说这不是"拦截器"，而是"匹配器"。`RoutePredicateHandlerMapping` 遍历所有 `RouteDefinition`，用 `RoutePredicate` 判断命中，命中才进入 L4。

实践中用配置即可：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: gray-v2                                              # 路由唯一标识
          uri: lb://order-service-v2                               # 目标（lb 表示走负载均衡）
          order: 1                                                 # 路由自身优先级，小的先匹配
          predicates:
            - Path=/api/order/**                                   # 路径谓词
            - Header=X-Gray, v2                                    # 请求头谓词：只有灰度流量进来
            - Method=GET,POST                                      # 方法谓词
```

**重要**：谓词决定"走哪条路由"，过滤链决定"对已命中路由做什么"。只有先命中路由，L4 才会被触发。

### 示例：按来源 IP 网段匹配（`Cidr`）

内置断言 `Header` / `Query` / `Host` 都只做字符串匹配，**没有 IP 网段**这种能力，正好拿来演示。

```java
@Component                                                              // 必须注册为 Bean，Gateway 会自动收集所有 RoutePredicateFactory
public class CidrRoutePredicateFactory
        extends AbstractRoutePredicateFactory<CidrRoutePredicateFactory.Config> {

    private static final String XFF = "X-Forwarded-For";                // 真实 IP 藏在代理头里

    public CidrRoutePredicateFactory() {
        super(Config.class);                                            // 传入配置类
    }

    @Override
    public List<String> shortcutFieldOrder() {                          // 支持 `- Cidr=192.168.0.0/16,10.8.0.0/16`
        return List.of("cidrs");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        // ★ 关键：CIDR 在路由构建时解析一次，不是每个请求都解析
        List<IpCidr> ranges = config.getCidrs().stream()
                .map(IpCidr::new)                                       // 这里做一次字符串 → 网段对象的转换
                .toList();

        return exchange -> {                                            // 每个请求都执行，必须极轻
            InetAddress client = clientIp(exchange);                    // 取真实客户端 IP
            if (client == null) {
                return false;                                           // 拿不到就不匹配，直接放弃这条路由
            }
            return ranges.stream().anyMatch(r -> r.contains(client));   // 命中任意一个网段即通过
        };
    }

    /**
     * 优先取 X-Forwarded-For 的第一个地址；没有才用连接地址。
     * 这点很重要：前面挂了 Nginx / SLB 时，getRemoteAddress() 拿到的是代理的内网 IP。
     */
    private InetAddress clientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst(XFF);
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();                    // 取最左边的，即客户端
            try {
                return InetAddress.getByName(first);                    // 字面量 IP，不会触发 DNS
            } catch (Exception ignored) {
                return null;                                            // 非法格式当作拿不到
            }
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote == null ? null : remote.getAddress();
    }

    @Data                                                               // 提供 getter/setter，才能被绑定
    public static class Config {
        private List<String> cidrs = new ArrayList<>();                 // 支持 IPv4 / IPv6 网段与单 IP
    }

    /** 极简 CIDR 实现，够用于路由匹配 */
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
            if (target.length != network.length) {
                return false;                                           // IPv4 与 IPv6 不能互相匹配
            }
            int bits = prefix;
            for (int i = 0; i < target.length && bits > 0; i++) {
                int mask = bits >= 8 ? 0xFF : (0xFF << (8 - bits)) & 0xFF;
                if ((target[i] & mask) != (network[i] & mask)) {
                    return false;                                       // 前缀位不同即不匹配
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
```

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: admin-api
          uri: lb://admin-service
          predicates:
            # 完整写法
            - name: Cidr
              args:
                cidrs:
                  - 192.168.0.0/16        # 办公网
                  - 10.8.0.0/16           # 容器网段
                  - 203.0.113.7           # 单 IP 也行，不带掩码就是 /32
            # 与其它断言是 AND 关系
            - Path=/api/admin/**
          filters:
            - StripPrefix=2

        # 简写形式
        - id: internal-api
          uri: lb://internal-service
          predicates:
            - Cidr=192.168.0.0/16,10.8.0.0/16
            - Path=/api/internal/**
```

### 未命中时的表现

所有断言是 **AND** 关系，`Cidr` 不通过 → 这条路由不命中 → 继续试下一条路由；全部不命中则返回 404。**不会自动返回 403**，这是断言的语义决定的。

如果你想要 “外部 IP 访问管理接口返回 403 而不是 404”，不能靠断言，得在 `GlobalFilter` 里判断后主动返回 403。

---

## L4 GlobalFilter / GatewayFilter 层：真正的"拦截器"

这是唯一适合实现业务拦截的层。

- **`GlobalFilter`**：全局生效（相当于 Servlet 的 `Filter`），实现 `Ordered` 控制顺序。
- **`GatewayFilter`**：绑定在具体路由上（相当于 MVC 的 `HandlerInterceptor` 但**功能强得多**，可改写请求/响应体）。
- 两者最终被 `FilteringWebHandler` 合并成**一条有序链**。

### 4.1 GlobalFilter 实现鉴权（最常用）

```java
@Component                                                          // 注册进 globalFilters 列表
public class AuthGlobalFilter implements GlobalFilter, Ordered {   // GlobalFilter 只要求 filter 方法，Ordered 要求 getOrder
    private static final List<String> WHITE = List.of("/auth/login", "/actuator/health");  // 白名单路径

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {  // 主拦截逻辑
        ServerHttpRequest req = exchange.getRequest();               // 当前请求
        String path = req.getURI().getPath();                        // 剥掉 host 后的路径，用于白名单匹配
        if (WHITE.contains(path)) {                                  // 命中白名单
            return chain.filter(exchange);                           // 直接放行，不鉴权
        }
        String token = req.getHeaders().getFirst("Authorization");   // 取 Authorization 头
        if (token == null || !token.startsWith("Bearer ")) {         // 没带或格式不对
            return reject(exchange, "missing token");                // 直接返回 401，不调用 chain（链在此中断）
        }
        try {
            Claims claims = JwtUtil.parse(token.substring(7));       // 校验签名并解析载荷
            exchange.getAttributes().put("userId", claims.getSubject());  // 存进 attributes，供后续 filter 读取
            return chain.filter(exchange);                           // 放行
        } catch (Exception e) {                                      // 过期/篡改/格式错误
            return reject(exchange, "invalid token");                // 统一 401
        }
    }

    private Mono<Void> reject(ServerWebExchange exchange, String msg) {  // 封装错误响应
        ServerHttpResponse resp = exchange.getResponse();                // 取响应对象
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);                     // 置 401
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);    // 声明 JSON
        byte[] body = ("{\"code\":401,\"msg\":\"" + msg + "\"}").getBytes(StandardCharsets.UTF_8);  // 构造错误体
        DataBuffer buf = resp.bufferFactory().wrap(body);                // 用分配器包装成 DataBuffer
        return resp.writeWith(Mono.just(buf));                           // 写入响应并结束，Mono 完成即请求结束
    }

    @Override
    public int getOrder() {                                          // 决定在合并链中的位置
        return -100;                                                 // 负数保证排在路由 filter 之前（路由 filter 从 1 开始编号）
    }
}
```

**坑**：`exchange.getAttributes()` **只在过滤器链内部可见**，不会随转发带给下游微服务。要传给下游，必须写进 request header（见 4.3）。

### 4.2 自定义 GatewayFilterFactory（路由级拦截）

```java
@Component                                                          // 必须成为 Bean，才能被 RouteDefinitionRouteLocator 通过 name 找到
public class IdempotentGatewayFilterFactory                          // 类的通用形式
        extends AbstractGatewayFilterFactory<IdempotentGatewayFilterFactory.Config> {  // 模板父类，帮你处理配置绑定

    public IdempotentGatewayFilterFactory() {                        // 必须有构造器
        super(Config.class);                                         // 把配置类交给父类做反射绑定
    }

    @Override
    public String name() {                                           // yml 中 `- name: ` 后面的值
        return "Idempotent";                                         // 此例即 yml 写 `- name: Idempotent`
    }

    @Override
    public List<String> shortcutFieldOrder() {                       // 支持 `- Idempotent=30` 这种简写
        return List.of("ttlSeconds");                                // 简写时唯一的参数名
    }

    @Override
    public GatewayFilter apply(Config config) {                      // 每个路由构建时调用一次，返回真正的 filter
        return new OrderedGatewayFilter(                             // 包装以便自定义顺序
                (exchange, chain) -> {                               // 这里是每个请求都会执行的 lambda
                    ServerHttpRequest req = exchange.getRequest();   // 取请求
                    String key = req.getHeaders().getFirst("X-Idem-Key");  // 幂等键由客户端传
                    if (key == null) {                               // 未传幂等键
                        return chain.filter(exchange);               // 视为非幂等请求，直接放行
                    }
                    String cacheKey = "idem:" + key;                 // 构造 Redis key
                    return redisTemplate.opsForValue()               // 操作 Redis String
                            .setIfAbsent(cacheKey, "1",              // SETNX 语义：不存在才写入
                                    Duration.ofSeconds(config.getTtlSeconds()))  // 配置的过期时间
                            .flatMap(ok -> {                         // 拿到 SETNX 的布尔结果
                                if (Boolean.TRUE.equals(ok)) {       // 首次请求
                                    return chain.filter(exchange);    // 放行到下游
                                }
                                exchange.getResponse()            // 重复请求
                                        .setStatusCode(HttpStatus.CONFLICT);  // 返回 409
                                return exchange.getResponse().setComplete();  // 结束响应
                            });
                },
                config.getOrder());                                  // 顺序取自配置
    }

    @Data                                                            // Lombok 生成 getter/setter
    public static class Config {                                     // 配置类，字段名与 yml 参数一一对应
        private long ttlSeconds = 60;                                // 幂等窗口，默认 60 秒
        private int order = 0;                                       // 过滤链顺序
    }
}
```

搭配 yml：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: pay                                                     # 路由标识
          uri: lb://pay-service                                       # 目标服务
          predicates:
            - Path=/api/pay/**                                        # 命中路径
          filters:
            - StripPrefix=2                                           # 内置 filter：去掉前两段路径前缀
            - Idempotent=30                                           # 自定义 filter：简写形式，ttl=30
```

**内置常用 GatewayFilter 一览**（全部由 `filters` 配置，无需写代码）：

| filter | 作用 |
|---|---|
| `StripPrefix=n` | 剥掉前 n 段路径 |
| `PrefixPath=/x` | 加路径前缀 |
| `RewritePath=/a/(?<v>.*), /b/${v}` | 正则重写路径 |
| `AddRequestHeader` / `RemoveRequestHeader` | 改请求头 |
| `AddRequestParameter` | 加 query 参数 |
| `AddResponseHeader` / `DedupeResponseHeader` | 改响应头 / 去重 |
| `SetStatus` | 强制改状态码 |
| `RequestSize` | 限制请求体大小 |
| `Retry` | 重试（见坑清单） |
| `CircuitBreaker` | 熔断降级 |
| `RequestRateLimiter` | 限流 |
| `RedirectTo` / `SecureHeaders` | 重定向 / 安全响应头 |

### 4.3 改写请求体（鉴权后透传用户信息、报文加解密）

`ServerWebExchange` 的 request/response 是只读包装，**改体要用 Decorator**，且必须把 body 的 `Flux<DataBuffer>` 聚合成完整字节。

```java
@Override
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        ServerHttpRequest req = exchange.getRequest();                        // 原请求
        return DataBufferUtils.join(req.getBody())                            // 把 Flux<DataBuffer> 聚合成单个 DataBuffer（会先把整个 body 读进内存）
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))  // body 为空时给个空 buffer，避免 NPE
                .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
                .flatMap(buf -> {
                    byte[] bytes = new byte[buf.readableByteCount()];           // 按可读长度分配
                    buf.read(bytes);                                            // 拷贝出字节
                    DataBufferUtils.release(buf);                               // 必须释放，DataBuffer 是池化的，不释放会内存泄漏
                    String text = new String(bytes, StandardCharsets.UTF_8);    // 还原成字符串
                    byte[] newBody = text.replace("\"debug\":true", "\"debug\":false")  // 示例：篡改报文
                                        .getBytes(StandardCharsets.UTF_8);       // 回写字节
                    ServerHttpRequestDecorator dec = new ServerHttpRequestDecorator(req) {  // 用装饰器包住原请求
                        @Override
                        public Flux<DataBuffer> getBody() {                     // 重写 getBody，下游读到的就是新 body
                            return Flux.just(exchange.getResponse()             // 用响应的 bufferFactory 分配，保证同一 pool
                                    .bufferFactory().wrap(newBody));            // 包装新字节
                        }
                        @Override
                        public HttpHeaders getHeaders() {                       // 长度变了，头必须同步
                            HttpHeaders h = new HttpHeaders();                  // 新建一份
                            h.putAll(super.getHeaders());                       // 复制原头
                            h.remove(HttpHeaders.TRANSFER_ENCODING);            // 去掉 chunked，否则和 Content-Length 冲突
                            h.setContentLength(newBody.length);                 // 设置正确的长度
                            return h;                                           // 返回新头
                        }
                    };
                    return chain.filter(exchange.mutate().request(dec).build());  // 用装饰后的请求继续走链
                });
    };
}
```

**代价**：这一步会把整个请求体读进内存，大文件上传场景会直接撑爆堆，因此**只对 JSON 小报文使用**，或用 `ExchangeFilterFunction` 之外的流式方案。

### 4.4 改写响应体

```java
@Override
public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
        ServerHttpResponse resp = exchange.getResponse();                     // 原响应
        ServerHttpResponseDecorator dec = new ServerHttpResponseDecorator(resp) {  // 装饰响应
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {    // 下游写回响应时被调用
                if (body instanceof Flux) {                                    // 只处理流式响应
                    Flux<? extends DataBuffer> flux = (Flux<? extends DataBuffer>) body;  // 强转
                    return super.writeWith(flux.collectList().map(list -> {    // 聚合全部 buffer
                        // 此处可把 list 拼成完整字节，做脱敏/包一层 code 字段，再 wrap 回去
                        return list;                                            // 示例直接原样返回
                    }));
                }
                return super.writeWith(body);                                  // 非 Flux 原样透传
            }
        };
        return chain.filter(exchange.mutate().response(dec).build());          // 用装饰后的响应继续
    };
}
```

**注意**：`setComplete()` / 异常响应 **不会**走 `writeWith`，别指望在这里处理错误体——错误走 L6。

---

## L5 转发层：链尾的内置 filter

这两类 filter 由框架自动排在链末，**不要自己写**：

| filter | order | 说明 |
|---|---|---|
| `NettyWriteResponseFilter` | `-1` | 把下游 Netty 响应写回客户端 |
| `ReactiveLoadBalancerClientFilter` | `10150` | `lb://` 的服务发现 + 负载均衡 |
| `NettyRoutingFilter` | `Integer.MAX_VALUE` | 真正发起 HTTP 转发（链的终止符） |

顺序含义：你的 `GlobalFilter` 只要 `getOrder() < 10150`，就一定在负载均衡与转发之前。

---

## L6 异常处理层：统一错误返回

`WebFilter` / `GlobalFilter` 抛出的异常不会自动变成友好响应，需要 `ErrorWebExceptionHandler`。

```java
@Component                                                          // 注册为 Bean 即可自动生效
@Order(-1)                                                          // 必须小于 DefaultErrorWebExceptionHandler（是 -1 的同级比较，靠 Bean 顺序），确保优先接管
public class GlobalErrorHandler implements ErrorWebExceptionHandler {  // WebFlux 的异常处理器接口

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {  // 任意环节抛出的异常都汇到这里
        ServerHttpResponse resp = exchange.getResponse();                  // 取响应
        if (resp.isCommitted()) {                                          // 响应已经开始写出（例如流式响应中途炸）
            return Mono.error(ex);                                         // 只能向上抛，无法再改状态码
        }
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;              // 默认 500
        String msg = "internal error";                                     // 默认文案
        if (ex instanceof ResponseStatusException rse) {                   // 框架自身的状态异常（如 404 路由未命中）
            status = (HttpStatus) rse.getStatusCode();                     // 沿用它的状态码
            msg = rse.getReason();                                         // 沿用它的原因
        } else if (ex instanceof TimeoutException) {                       // 下游超时
            status = HttpStatus.GATEWAY_TIMEOUT;                           // 504
            msg = "upstream timeout";                                      // 文案
        }
        resp.setStatusCode(status);                                        // 写入最终状态码
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);      // JSON 返回
        byte[] body = String.format("{\"code\":%d,\"msg\":\"%s\"}", status.value(), msg)  // 拼错误体
                .getBytes(StandardCharsets.UTF_8);
        return resp.writeWith(Mono.just(resp.bufferFactory().wrap(body)));   // 写回并结束
    }
}
```

**要点**：路由不命中时框架抛 `ResponseStatusException(NOT_FOUND)`，如果你也想返回统一 JSON，就要覆盖它，否则是默认的 404 空体。

---

## 按需求选型速查

| 需求 | 层 | 选什么 |
|---|---|---|
| 全局 traceId / 访问日志 / 耗时 | L2 | `WebFilter` + `@Order` |
| CORS 跨域 | L2 | `CorsWebFilter`（Spring 内置） |
| Spring Security 认证（OAuth2/JWT） | L2 | `@EnableWebFluxSecurity` + `SecurityWebFilterChain` |
| 自定义 JWT 鉴权、传递 userId | L4 | `GlobalFilter`（order < 10150） |
| 只对某个服务生效的规则（签名、幂等） | L4 | `GatewayFilterFactory` + 路由 `filters` |
| 限流 | L4 | `RequestRateLimiter` + `KeyResolver` Bean |
| 熔断降级 | L4 | `CircuitBreaker` filter + `ReactiveCircuitBreakerFactory` |
| 失败重试 | L4 | `Retry` filter（注意 `methods: GET` 才安全） |
| 路径改写/加前缀/去前缀 | L4 | `RewritePath` / `StripPrefix` / `PrefixPath` |
| 请求体解密/校验、响应体脱敏 | L4 | `ServerHttpRequestDecorator` / `ServerHttpResponseDecorator` |
| 灰度/AB/按流量比例导流 | L3 + L4 | `predicates: Header=X-Gray` + 两条路由 + `Weight` |
| 统一错误响应 | L6 | `ErrorWebExceptionHandler` |
| 连接级统计、TCP 保活 | L1 | `NettyServerCustomizer` |

---

## 顺序与坑清单

1. **`GlobalFilter` 与路由 filter 混排**：`FilteringWebHandler` 把所有 global + route filter 用 `AnnotationAwareOrderComparator` 统一排序；路由内 filter 的索引**从 1 开始**，所以 `getOrder() <= 0` 的 GlobalFilter 一定在最前。
2. **`@Order` 只管 `WebFilter`，不管 `GlobalFilter`**（后者由 `getOrder()` 方法决定），别混用注解。
3. **不要引入 `spring-boot-starter-web`**，会激活 MVC 与 `DispatcherServlet`，Gateway 直接报"Spring MVC found on classpath, which is incompatible with Spring Cloud Gateway"。
4. **`@WebFilter`、`HandlerInterceptor`、`javax.servlet.Filter` 全部无效**——没有 Servlet 容器。
5. **`exchange.getAttributes()` 不跨进程**：要传给下游必须写 header。
6. **响应修饰只在 `beforeCommit` / `writeWith` 里做**，`setComplete()` 之后什么都没有了。
7. **`DataBufferUtils.join` 后必须 `release`**，`DataBuffer` 是池化的，漏一次就是一次堆外泄漏。
8. **改 body 必改 `Content-Length`**，并删掉 `Transfer-Encoding: chunked`，否则下游解析错乱。
9. **阻塞调用禁止**：`GlobalFilter` 里不能出现 `Thread.sleep`、JDBC、同步 Redis。必须包成 `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`。
10. **`RequestRateLimiter` 依赖 Redis**，无 Redis 用 `GatewayRedisAutoConfiguration` 会启动失败；也可以用内存版自定义实现。
11. **`Retry` 默认不限方法**，POST 重试可能导致重复下单，必须显式写 `methods: GET`。
12. **`CircuitBreaker` 需要 `fallbackUri`**，且 fallback 必须走 `forward:`，否则熔断后没有兜底响应。

---

## 依赖参考

```xml
<!-- WebFlux + Gateway 核心，不要同时引入 spring-boot-starter-web -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- 服务发现 + lb:// 负载均衡 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- 限流（RequestRateLimiter 依赖 Redis） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>

<!-- 熔断降级 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

```yaml
# 限流的 KeyResolver 必须自己声明成 Bean，yml 里用 #{@beanName} 引用
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter              # 限流 filter
              args:
                redis-rate-limiter.replenishRate: 10     # 每秒补充 10 个令牌
                redis-rate-limiter.burstCapacity: 20     # 桶容量 20（允许突发）
                redis-rate-limiter.requestedTokens: 1    # 每个请求消耗 1 个令牌
                key-resolver: "#{@ipKeyResolver}"        # 按 IP 限流，指向自定义 Bean
            - name: CircuitBreaker                  # 熔断
              args:
                name: userCB                        # 熔断器名称（对应 Resilence4J 配置）
                fallbackUri: forward:/fallback/user  # 熔断后转发到本机降级接口
```

```java
@Bean                                                               // 必须暴露成 Bean，供 yml 的 SpEL 引用
public KeyResolver ipKeyResolver() {                                // 按客户端 IP 做限流键
    return exchange -> Mono.just(                                   // KeyResolver 返回 Mono<String>
            exchange.getRequest().getRemoteAddress()                // 拿到对端地址
                    .getAddress().getHostAddress());                // 只取 IP 字符串
}
```
