package ru.hw.websocketchat;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.SocketUtils;
import org.springframework.util.StringUtils;
import ru.hw.websocketchat.ui.FXMain;

@SpringBootApplication
@Slf4j
public class ClientApp {
    public static void main(String[] args) throws InterruptedException {
        setRandomPort(7070, 7999);
        Application.launch(FXMain.class, args);
    }

    private static void setRandomPort(int minPort, int maxPort) {
        try {
            String userDefinedPort = System.getProperty("server.port", System.getenv("SERVER_PORT"));
            if (StringUtils.isEmpty(userDefinedPort)) {
                int port = SocketUtils.findAvailableTcpPort(minPort, maxPort);
                System.setProperty("server.port", String.valueOf(port));
                log.info("Server port set to {}.", port);
            }
        } catch (IllegalStateException var4) {
            log.warn("No port available in range 5000-5100. Default embedded server configuration will be used.");
        }
    }
}
