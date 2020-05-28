package ru.hw.websocketchat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import ru.hw.websocketchat.ClientConnectionSessionHandler;

import java.util.concurrent.ExecutionException;

@Configuration
@Slf4j
public class ClientConfiguration {
    @Value("${app.client.host}")
    private String host;

    @Bean
    public WebSocketStompClient stompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient());
        stompClient.setMessageConverter(messageConverter());
        stompClient.setTaskScheduler(taskScheduler());

        return stompClient;
    }

//    @Lazy
//    @Bean
//    public StompSession stompSession() {
//        StompHeaders connectHeaders = new StompHeaders();
//        connectHeaders.add("login", "admin");
//        connectHeaders.add("passcode", "admin");
//
//        try {
//            return stompClient().connect(host, new WebSocketHttpHeaders(), connectHeaders, new ClientConnectionSessionHandler()).get();
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }

    @Bean
    public WebSocketClient webSocketClient() {
        return new StandardWebSocketClient();
    }

    @Bean
    public MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
}
