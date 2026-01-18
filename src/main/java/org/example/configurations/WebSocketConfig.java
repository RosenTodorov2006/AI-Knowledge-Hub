package org.example.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Дефинираме "брокера". Всичко, което започва с /topic, 
        // сървърът ще може да изпраща към браузъра.
        config.enableSimpleBroker("/topic");

        // Префикс за съобщения, които идват ОТ браузъра КЪМ сървъра.
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Това е началният URL, към който JS ще се свърже.
        // .withSockJS() е за поддръжка на по-стари браузъри.
        registry.addEndpoint("/ws").withSockJS();
    }
}