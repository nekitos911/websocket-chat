package ru.hw.websocketchat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ChatMessage {
    private MessageType type;
    private byte[] content;
    private String sender;
    private Date dateTime;
    private Set<String> userList = new HashSet<>();

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

}
