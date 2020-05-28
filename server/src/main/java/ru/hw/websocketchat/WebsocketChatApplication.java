package ru.hw.websocketchat;

import org.apache.catalina.filters.RemoteIpFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.hw.md5.MD5;
import ru.hw.websocketchat.model.User;
import ru.hw.websocketchat.service.UserService;

import java.math.BigInteger;

@SpringBootApplication
public class WebsocketChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketChatApplication.class, args);
    }

    @Bean
    public RemoteIpFilter remoteIpFilter() {
        return new RemoteIpFilter();
    }

    @Bean
    public CommandLineRunner start(UserService userService){
        return args -> {
            if (userService.usersCount() == 0) {
                userService.saveUser(new User(null, "test", "test"));
                userService.saveUser(new User(null, "admin", "admin"));

            }
        };
    }
}
