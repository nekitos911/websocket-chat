package ru.hw.websocketchat.ui.service;

import ru.hw.websocketchat.model.ChatMessage;

public interface MessageService {
    void sendMessage(ChatMessage message);
}
