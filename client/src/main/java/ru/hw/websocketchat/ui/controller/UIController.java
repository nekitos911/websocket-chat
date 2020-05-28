package ru.hw.websocketchat.ui.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import ru.hw.websocketchat.blowfish.Blowfish;
import ru.hw.websocketchat.blowfish.enums.EncipherMode;
import ru.hw.websocketchat.model.ChatMessage;
import ru.hw.websocketchat.model.CurrentUser;
import ru.hw.websocketchat.model.PGClass;
import ru.hw.websocketchat.model.SaltKey;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Component("UIController")
@RequiredArgsConstructor
@Slf4j
public class UIController extends StompSessionHandlerAdapter {
    private final Map<String, Color> userColor = new ConcurrentHashMap<>();

    private final WebSocketStompClient stompClient;
    public VBox clientListBox;
    public TextArea txtMsg;
    public VBox chatBox;
    public Button btnFile;

    private StompSession stompSession;

    @Value("${app.client.host}")
    private String host;
    private final CurrentUser currentUser;

    @SneakyThrows
    @FXML
    public void initialize() throws ExecutionException, InterruptedException {
        var privateKey = BigInteger.valueOf(SecureRandom.getInstanceStrong().nextInt());
//        var salt = new RestTemplate().getForEntity("http://" + host + "/salt?username={username}", String.class, Map.of("username", currentUser.getUsername()));
//        var salt = new RestTemplate().getForEntity("http://" + host + "/salt", String.class).getBody();
        var pg = new RestTemplate().getForEntity("http://" + host + "/pg", PGClass.class).getBody();

        var clientOpenKey = pg.getG().modPow(privateKey, pg.getP());

        var serverOpenKey = new RestTemplate().getForEntity("http://" + host + "/openKey?clientKey={clientKey}", SaltKey.class, Map.of("clientKey", clientOpenKey.toString())).getBody();

        var key = new BigInteger(serverOpenKey.getKey()).modPow(privateKey, pg.getP()).toByteArray();
        var salt = serverOpenKey.getSalt();


        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("login", currentUser.getUsername());
        connectHeaders.add("salt", Blowfish.bytesToStringArray(Blowfish.stringToBytesUTFNIO(salt)));
        connectHeaders.add("passcode", Blowfish.bytesToStringArray(new Blowfish(key).encipher(Blowfish.stringToBytesUTFNIO((currentUser.getPassword() + salt)), EncipherMode.CBC)));
        stompSession = stompClient.connect("ws://" + host + "/chat", new WebSocketHttpHeaders() , connectHeaders, this).get();
    }

    public void sendMessage() {
       stompSession.send("/app/chat.sendMessage",
               ChatMessage.builder().
                       type(ChatMessage.MessageType.CHAT).
                       sender(currentUser.getUsername()).
                       content(txtMsg.getText().getBytes()).
                       dateTime(new Date()).
                       build()
       );

       txtMsg.clear();
       txtMsg.requestFocus();
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        session.subscribe("/topic/public", this);
        session.send("/app/chat.addUser", ChatMessage.builder().sender(currentUser.getUsername()).type(ChatMessage.MessageType.JOIN).build());

        log.info("New session: {}", session.getSessionId());
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error(Arrays.toString(exception.getStackTrace()));
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return ChatMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        var msg = ((ChatMessage)payload);
        log.info("Received: {}", msg);

        Optional.ofNullable(msg.getUserList())
                .ifPresent(uList -> uList.forEach(u -> {
                    if (!userColor.containsKey(u)) {
                        userColor.put(u, Color.color(Math.random(), Math.random(), Math.random()));
                    }
                } ));

        switch (msg.getType()) {
            case CHAT:
                showMessage(msg);
                break;
            case JOIN:
            case LEAVE:
                sendNotice(msg.getSender(), msg.getType());
                updateUserList(msg.getUserList());
                break;
            default:break;
        }
    }

    private void sendNotice(String username, ChatMessage.MessageType messageType) {
        Platform.runLater(() -> {
            HBox hbox = new HBox(10);
            hbox.setAlignment(Pos.CENTER);
            chatBox.setAlignment(Pos.TOP_CENTER);
            var msg = "";
            if (messageType.equals(ChatMessage.MessageType.LEAVE)) {
                msg = username + " has left chat";
            } else if(messageType.equals(ChatMessage.MessageType.JOIN)) {
                msg = username + " connected to chat";
            }
            var text = new Text(msg);
            text.setFill(Color.WHITE);
            hbox.getChildren().addAll(text);
            chatBox.getChildren().addAll(hbox);
        });

    }

    private void updateUserList(Set<String> users) {
        if (users == null) return;

        Platform.runLater(() -> {
            clientListBox.getChildren().clear();
            for (String username : users) {
                HBox container = new HBox();
                container.setAlignment(Pos.CENTER_LEFT);
                container.setSpacing(10);
                container.setPrefWidth(clientListBox.getPrefWidth());
                container.setPadding(new Insets(5));
                container.getStyleClass().add("online-user-container");
                Circle img = new Circle(30, 30, 15);
                img.setFill(userColor.get(username));

                container.getChildren().add(img);

                VBox userDetailContainer = new VBox();
                userDetailContainer.setPrefWidth(clientListBox.getPrefWidth() / 1.7);
                Label lblUsername = new Label(username);
                lblUsername.setPadding(new Insets(5, 0, 0, 0));
                lblUsername.getStyleClass().add("online-label");
                userDetailContainer.getChildren().add(lblUsername);
//            User user=null;
//            try {
//                user=controller.get(client);
//                Label lblName = new Label(username);
//                lblName.getStyleClass().add("online-label-details");
//                userDetailContainer.getChildren().add(lblName);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }catch (NullPointerException ex){
//                System.out.println("user is null");
//            }
                container.getChildren().add(userDetailContainer);

                Label settings = new Label("...");
                settings.getStyleClass().add("online-settings");
                settings.setTextAlignment(TextAlignment.CENTER);
                container.getChildren().add(settings);
                clientListBox.getChildren().add(container);
            }
        });
    }

    private void showMessage(ChatMessage msg) {
        if (msg == null || StringUtils.isEmpty(msg.getContent())) return;

        msg.setContent(new Blowfish(CurrentUser.getMessagePassword()).decipher(msg.getContent()));

        Text text = new Text(msg.getDateTime().toString() + "\n\n" + new String(msg.getContent()));

        text.setFill(Color.WHITE);
        text.getStyleClass().add("message");
        TextFlow tempFlow = new TextFlow();

        tempFlow.getChildren().add(text);
        tempFlow.setMaxWidth(260);

        TextFlow flow = new TextFlow(tempFlow);

        HBox hbox = new HBox(20);


        var avatar = new Circle(32,32,16);
        Text usernameText = new Text(msg.getSender().substring(0, 1).toUpperCase());
        usernameText.setBoundsType(TextBoundsType.VISUAL);
        usernameText.setFill(Color.WHITE);
        StackPane stack = new StackPane();
        avatar.setFill(userColor.get(msg.getSender()));
        stack.getChildren().addAll(avatar, usernameText);
//        avatar.setFill(Color.);


        usernameText.getStyleClass().add("imageView");

        if (!currentUser.getUsername().equals(msg.getSender())) {
            tempFlow.getStyleClass().add("tempFlowFlipped");
            flow.getStyleClass().add("textFlowFlipped");
            chatBox.setAlignment(Pos.TOP_LEFT);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.getChildren().add(stack);
            hbox.getChildren().add(flow);
        } else {
            text.setFill(Color.WHITE);
            tempFlow.getStyleClass().add("tempFlow");
            flow.getStyleClass().add("textFlow");
            hbox.setAlignment(Pos.BOTTOM_RIGHT);
            hbox.getChildren().add(flow);
            hbox.getChildren().add(stack);
        }

        hbox.getStyleClass().add("hbox");
        Platform.runLater(() -> chatBox.getChildren().addAll(hbox));

        txtMsg.requestFocus();



//        var textFlow = new TextFlow();
//        textFlow.getChildren().add(new Text(msg.getContent()));
//        var hbox = new HBox(12);
//        var avatar = new Circle(32,32,16);
//        Text text = new Text(msg.getSender().substring(0, 1).toUpperCase());
//        text.setBoundsType(TextBoundsType.VISUAL);
//        StackPane stack = new StackPane();
//        stack.getChildren().addAll(avatar, text);
//        avatar.setFill(Color.TRANSPARENT);
//        avatar.setStroke(Color.color(Math.random(), Math.random(), Math.random()));
//
//        hbox.getChildren().addAll(stack, textFlow);
//
//        Platform.runLater(() -> chatBox.getChildren().addAll(hbox));


//        showMessageArea.appendText(
//                "Date: " + LocalDateTime.now() + "\n" +
//                        "From: " + msg.getSender() + "\n" +
//                        "Message: " + msg.getContent() + "\n\n\r"
//        );
    }

    public void sendMessageByKey(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case ENTER:
                if (keyEvent.isShiftDown()) {
                    txtMsg.appendText("\n");
                    break;
                }
                sendMessage();
                break;
            default:
                break;
        }
    }

    public void fileAction(ActionEvent actionEvent) {
    }
}
