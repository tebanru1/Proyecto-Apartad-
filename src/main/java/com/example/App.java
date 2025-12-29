package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        Parent root = loadFXML("login");
        scene = new Scene(root);
        stage.setScene(scene);

        stage.setResizable(false);
        stage.centerOnScreen();
        stage.setTitle("CPMS APARTADÓ");

        stage.getIcons().add(
            new Image(Objects.requireNonNull(App.class.getResourceAsStream("/com/example/Logo_Institucional.png")))
        );

        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                App.class.getResource("/com/example/" + fxml + ".fxml")
        );
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
