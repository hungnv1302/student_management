package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // Load file FXML
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/app/auth/loginPage.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("Quản lý sinh viên - Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
