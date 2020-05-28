package ru.hw.websocketchat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import ru.hw.websocketchat.blowfish.Blowfish;
import ru.hw.websocketchat.model.ChatMessage;
import ru.hw.websocketchat.service.ActiveUsersCountService;

import java.util.Arrays;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ActiveUsersCountService activeUsersCountService;


    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        log.info("Server received message: {}", chatMessage);
        log.info("Message string content: {}", Blowfish.bytesToStringUTFNIO(chatMessage.getContent()));
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        activeUsersCountService.addUser(chatMessage.getSender());

        log.info("Active users: {}", Arrays.toString(activeUsersCountService.getActiveUserNames().toArray(String[]::new)));
        chatMessage.setUserList(activeUsersCountService.getActiveUserNames());
        return chatMessage;
    }

}