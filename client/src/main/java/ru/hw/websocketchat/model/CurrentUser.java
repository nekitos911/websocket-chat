package ru.hw.websocketchat.model;

import javafx.scene.paint.Color;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    @Getter
    private final String username;
    @Getter
    private final String password;

    @Getter
    @Setter(AccessLevel.NONE)
    private static String messagePassword;

    @Setter
    @Getter
    private Color color;

    public CurrentUser(@Value("${app.username}") String username, @Value("${app.password}") String password, @Value("${app.message.password}") String messagePassword) {
        this.username = username;
        this.password = password;
        CurrentUser.messagePassword = messagePassword;
    }
}
