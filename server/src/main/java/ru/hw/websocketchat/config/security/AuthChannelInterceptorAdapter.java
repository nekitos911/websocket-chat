package ru.hw.websocketchat.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.hw.websocketchat.blowfish.Blowfish;
import ru.hw.websocketchat.service.UserService;
import ru.hw.websocketchat.service.WebSocketAuthenticatorService;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {
    private static final String USERNAME_HEADER = "login";
    private static final String PASSWORD_HEADER = "passcode";
    private static final String CREDENTIALS = "stompCredentials";
    private static final String SALT = "salt";
    private final UserService userService;
    private final WebSocketAuthenticatorService webSocketAuthenticatorService;

    @Override
    public Message<?> preSend(final Message<?> message, final MessageChannel channel) {
        final StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT == accessor.getCommand()) {

            final String username = accessor.getFirstNativeHeader(USERNAME_HEADER);
            final String salt = Blowfish.bytesToStringUTFNIO(Blowfish.StringArrayToBytes(accessor.getFirstNativeHeader("salt")));

//            if (!userService.containsSalt(salt)) {
//                throw new RuntimeException("salt is wrong");
//            }

            var key = userService.getKey(salt);

            var arr = Blowfish.StringArrayToBytes(accessor.getPasscode());
            final String password = Blowfish.bytesToStringUTFNIO(new Blowfish(key).decipher(arr))
                    .replace(salt, "");

            try {
                final UsernamePasswordAuthenticationToken user = webSocketAuthenticatorService.getAuthenticatedOrFail(username, password);
                accessor.setUser(user);
            } catch (Exception ex) {
                userService.saveSalt(salt, key);
                throw ex;
            }
        }
        return message;
    }
}
