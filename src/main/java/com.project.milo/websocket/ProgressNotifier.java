package com.project.milo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进度消息广播器：把后台任务的进度实时推送给所有已连接的 WebSocket 会话
 */
@Component
public class ProgressNotifier {

    private static final Logger log = LoggerFactory.getLogger(ProgressNotifier.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }

    public int sessionCount() {
        return sessions.size();
    }

    public void send(com.project.milo.websocket.ProgressMessage message) {
        if (sessions.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(json));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("推送进度消息失败: {}", e.getMessage());
        }
    }
}
