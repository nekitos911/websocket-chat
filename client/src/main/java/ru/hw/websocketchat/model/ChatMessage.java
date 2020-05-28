package ru.hw.websocketchat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.hw.websocketchat.blowfish.Blowfish;
import ru.hw.websocketchat.blowfish.enums.EncipherMode;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
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

    public static class ChatMessageBuilder {
        public ChatMessageBuilder content(byte[] content) {
            var bf = new Blowfish(CurrentUser.getMessagePassword());
            this.content = bf.encipher(content,EncipherMode.ECB);
            return this;
        }
    }
}

