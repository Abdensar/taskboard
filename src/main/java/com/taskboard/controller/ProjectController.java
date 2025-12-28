package com.taskboard.controller;

import com.taskboard.dao.ProjectMorphiaDAO;
import com.taskboard.model.Project;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import java.util.List;

public class ProjectController {
    @FXML private ComboBox<Project> cbProjects;
    @FXML private TextField tfProjectName;
    @FXML private Label lblProjectError;
    @FXML private TableView<com.taskboard.model.Task> tvProjectTasks;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskTitle;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskStatus;

    private final ProjectMorphiaDAO projectDAO = new ProjectMorphiaDAO();
    private Project selectedProject;

    @FXML
    public void initialize() {
        List<Project> projects = projectDAO.getByOwner(com.taskboard.session.CurrentUser.get());
        cbProjects.setItems(FXCollections.observableArrayList(projects));
        cbProjects.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Project p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNom());
            }
        });
        cbProjects.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Project p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNom());
            }
        });
        cbProjects.setOnAction(e -> {
            selectedProject = cbProjects.getValue();
            loadProjectTasks();
        });
        colTaskTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitre()));
        colTaskStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getColonne() != null ? data.getValue().getColonne().getNom() : ""
        ));
        loadProjectTasks();
    }

    @FXML
    private void onCreateProject() {
        String name = tfProjectName.getText().trim();
        if (name.isEmpty()) {
            lblProjectError.setText("Project name required");
            return;
        }
        Project project = new Project(com.taskboard.session.CurrentUser.get(), name);
        projectDAO.create(project);
        cbProjects.getItems().add(project);
        cbProjects.getSelectionModel().select(project);
        lblProjectError.setText("");
    }

    @FXML
    private void onContinue() {
        if (selectedProject == null) {
            lblProjectError.setText("Select or create a project");
            return;
        }
        try {
            com.taskboard.session.CurrentProject.set(selectedProject);
            Stage stage = (Stage) cbProjects.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/column.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (java.io.IOException e) {
            lblProjectError.setText("Failed to load columns view");
        }
    }

    private void loadProjectTasks() {
        if (selectedProject == null || tvProjectTasks == null) return;
        com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
        com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
        List<com.taskboard.model.Column> columns = columnDAO.getByProject(selectedProject);
        List<com.taskboard.model.Task> allTasks = new java.util.ArrayList<>();
        for (com.taskboard.model.Column col : columns) {
            allTasks.addAll(taskDAO.getByColumn(col));
        }
        tvProjectTasks.setItems(javafx.collections.FXCollections.observableArrayList(allTasks));
    }

    @FXML
    private void onLogout() {
        com.taskboard.session.CurrentUser.clear();
        try {
            Stage stage = (Stage) cbProjects.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblProjectError.setText("Failed to logout");
        }
    }
}
