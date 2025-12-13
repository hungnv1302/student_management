package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.config.SceneNavigator;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo Stage cho SceneNavigator
        SceneNavigator.init(primaryStage);

        // Mở màn hình login
        SceneNavigator.goTo(
                "/app/auth/login.fxml",
                "Đăng nhập hệ thống"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
