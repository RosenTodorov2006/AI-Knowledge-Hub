package org.example.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    public static final String WS_ENDPOINT = "/ws";
    public static final String BROKER_DESTINATION_PREFIX = "/topic";
    public static final String APP_DESTINATION_PREFIX = "/app";

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(BROKER_DESTINATION_PREFIX);
        registry.setApplicationDestinationPrefixes(APP_DESTINATION_PREFIX);
    }
}