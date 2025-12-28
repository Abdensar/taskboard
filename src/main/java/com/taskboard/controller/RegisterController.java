package com.taskboard.controller;

import com.taskboard.dao.UserMorphiaDAO;
import com.taskboard.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField tfEmail;
    @FXML private TextField tfName;
    @FXML private PasswordField tfPassword;
    @FXML private PasswordField tfConfirm;
    @FXML private Label lblError;

    private final UserMorphiaDAO userDAO = new UserMorphiaDAO();

    @FXML
    private void onRegister() {
        String email = tfEmail.getText().trim();
        String name = tfName.getText().trim();
        String pass = tfPassword.getText();
        String confirm = tfConfirm.getText();
        if (email.isEmpty() || name.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            lblError.setText("All fields required");
            return;
        }
        if (!pass.equals(confirm)) {
            lblError.setText("Passwords do not match");
            return;
        }
        try {
            if (userDAO.getByEmail(email) != null) {
                lblError.setText("Email already registered");
                return;
            }
            User user = new User(name, email, pass);
            userDAO.create(user);
            onGoToLogin();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onGoToLogin() {
        try {
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblError.setText("Failed to load login");
        }
    }
}
