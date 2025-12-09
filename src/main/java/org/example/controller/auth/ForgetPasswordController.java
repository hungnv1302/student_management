package org.example.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ForgetPasswordController {

    // Đây là hàm mà FXML đang gọi: onAction="#handleCheckButton"
    @FXML
    private void handleCheckButton(ActionEvent event) {
        System.out.println(">>> handleCheckButton called");
        // TODO: sau này bạn xử lý kiểm tra username/email ở đây
    }

    // Nếu trong forgetPassword.fxml bạn có nút Back về Login,
    // thì onAction trong FXML phải là "#handleBackToLogin"
    @FXML
    private void handleBackToLogin(ActionEvent event) {
        System.out.println(">>> Back to login");

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/app/auth/loginPage.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quản lý sinh viên - Đăng nhập");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
