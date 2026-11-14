package com.example.gateway.handler.L5;

/**
 * <h2>L5 异常处理层：统一错误返回</h2>
 * 这几类 filter 由框架自动排在链末，不要自己写：
 * <p>
 * filter	                          order	               说明
 * NettyWriteResponseFilter	          -1	               把下游 Netty 响应写回客户端
 * ReactiveLoadBalancerClientFilter	  10150	               lb:// 的服务发现 + 负载均衡
 * NettyRoutingFilter	              Integer.MAX_VALUE	   真正发起 HTTP 转发（链的终止符）
 * <p>
 *  顺序含义：自定义的 GlobalFilter 只要 getOrder() < 10150，就一定在负载均衡与转发之前。
 */