package com.project.milo.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 进度 WebSocket 端点处理器（端点地址：/ws/progress）
 * 连接建立后即被注册到 ProgressNotifier，后台任务进度会实时推送到该连接
 */
@Component
public class ProgressWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ProgressWebSocketHandler.class);

    private final ProgressNotifier notifier;

    public ProgressWebSocketHandler(ProgressNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        notifier.register(session);
        log.info("WebSocket 连接建立: sessionId={}", session.getId());
        try {
            session.sendMessage(new TextMessage(
                    "{\"type\":\"CONNECTED\",\"message\":\"进度通道已连接\",\"total\":0,\"processed\":0,\"percent\":0}"));
        } catch (Exception e) {
            log.warn("发送连接确认消息失败: {}", e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 前端消息暂不处理，可扩展为暂停 / 取消等控制指令
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        notifier.unregister(session);
        log.info("WebSocket 连接关闭: sessionId={}", session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        notifier.unregister(session);
        log.warn("WebSocket 传输错误: {}", exception.getMessage());
    }
}
