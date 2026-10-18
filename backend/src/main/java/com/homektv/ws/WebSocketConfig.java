package com.homektv.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 注册（详设§4.1）。端点 /ws，局域网内允许任意来源。
 *
 * WebSocket registration (detailed design §4.1). Endpoint /ws, allows any origin within the local network.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final KtvWebSocketHandler handler;

    public WebSocketConfig(KtvWebSocketHandler handler) {
        this.handler = handler;
    }

    /**
     * 注册 WebSocket 处理器到 /ws 端点，添加客户端类型拦截器，允许任意来源。
     *
     * Registers the WebSocket handler at the /ws endpoint, adds a client-type interceptor,
     * and allows any origin pattern.
     *
     * @param registry WebSocket 处理器注册表 / WebSocket handler registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws")
                .addInterceptors(new ClientTypeInterceptor())
                .setAllowedOriginPatterns("*");   // 局域网自用，不限制来源
    }
}
