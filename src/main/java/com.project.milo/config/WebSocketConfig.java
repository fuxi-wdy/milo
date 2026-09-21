package com.project.milo.config;

import com.project.milo.websocket.ProgressWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置：注册 /ws/progress 进度推送端点
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ProgressWebSocketHandler progressWebSocketHandler;

    public WebSocketConfig(ProgressWebSocketHandler progressWebSocketHandler) {
        this.progressWebSocketHandler = progressWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(progressWebSocketHandler, "/ws/progress").setAllowedOrigins("*");
    }
}
