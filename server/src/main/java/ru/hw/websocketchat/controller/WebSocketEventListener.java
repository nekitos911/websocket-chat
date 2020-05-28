package ru.hw.websocketchat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.hw.websocketchat.model.ChatMessage;
import ru.hw.websocketchat.service.ActiveUsersCountService;

import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ActiveUsersCountService activeUsersCountService;

    @EventListener(SessionConnectEvent.class)
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        log.info("Received a new web socket connection");
//        var user = ((UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
//        activeUsersCountService.addUser(user.getUsername());
    }

    @EventListener(SessionDisconnectEvent.class)
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null) {
            activeUsersCountService.removeUser(username);

            log.info("User Disconnected : " + username);
            log.info("Active users: {}", Arrays.toString(activeUsersCountService.getActiveUserNames().toArray(String[]::new)));

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setUserList(activeUsersCountService.getActiveUserNames());

            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
