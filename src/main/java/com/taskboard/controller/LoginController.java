package com.taskboard.controller;

import com.taskboard.dao.UserMorphiaDAO;
import com.taskboard.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField tfEmail;
    @FXML private PasswordField tfPassword;
    @FXML private Label lblError;

    private final UserMorphiaDAO userDAO = new UserMorphiaDAO();

    @FXML
    private void onLogin() {
        String email = tfEmail.getText().trim();
        String pass = tfPassword.getText();
        if (email.isEmpty() || pass.isEmpty()) {
            lblError.setText("Email and password required");
            return;
        }
        try {
            User user = userDAO.getByEmail(email);
            if (user == null || !user.getPassword().equals(pass)) {
                lblError.setText("Invalid email or password");
                return;
            }
            com.taskboard.session.CurrentUser.set(user);
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/project.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onGoToRegister() {
        try {
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblError.setText("Failed to load register");
        }
    }

}
