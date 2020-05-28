package ru.hw.websocketchat.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StageInitializer implements ApplicationListener<FXMain.StageReadyEvent> {

    private final ApplicationContext applicationContext;
    @Value("test")
    private String applicationTitle;

    @Override
    public void onApplicationEvent(FXMain.StageReadyEvent stageReadyEvent) {

        try {
            Stage stage = stageReadyEvent.getStage();
            FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("fxml/ChatRoom.fxml").getURL());
            fxmlLoader.setControllerFactory(this.applicationContext::getBean);

            Parent load = fxmlLoader.load();
            stage.setScene(new Scene(load, 800, 600));
            stage.setTitle(applicationTitle);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
