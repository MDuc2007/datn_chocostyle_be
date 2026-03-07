package org.example.chocostyle_datn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client subscribe các topic bắt đầu bằng /topic
        config.enableSimpleBroker("/topic");
        // Client gửi message tới @MessageMapping sẽ bắt đầu bằng /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Dành cho Web (Vue.js) - Giữ nguyên hoàn toàn code cũ của bạn
        registry.addEndpoint("/ws-chocostyle")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 2. Dành cho Mobile (Flutter) - Thêm dòng này để kết nối WebSocket thuần
        registry.addEndpoint("/ws-chocostyle")
                .setAllowedOriginPatterns("*");
    }
}