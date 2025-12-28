package com.taskboard.controller;
import javafx.scene.layout.HBox;

import com.taskboard.dao.ProjectMorphiaDAO;
import com.taskboard.model.Project;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import java.util.List;

public class ProjectController {
    @FXML private TextField tfProjectName;
    @FXML private Label lblProjectError;
    @FXML private TableView<Project> tvProjects;
    @FXML private TableColumn<Project, String> colProjectName;
    @FXML private TableColumn<Project, Void> colProjectActions;
    @FXML private TableView<com.taskboard.model.Task> tvProjectTasks;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskTitle;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskStatus;

    private final ProjectMorphiaDAO projectDAO = new ProjectMorphiaDAO();
    private Project selectedProject;

    @FXML
    public void initialize() {
        // Center the main layout if it's a VBox
        if (tvProjects != null && tvProjects.getParent() instanceof javafx.scene.layout.VBox vbox) {
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
        }
        // Table1: Projects table with edit/delete actions
        List<Project> projects = projectDAO.getByOwner(com.taskboard.session.CurrentUser.get());
        if (tvProjects != null && colProjectName != null && colProjectActions != null) {
            tvProjects.setItems(FXCollections.observableArrayList(projects));
            colProjectName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
            colProjectActions.setCellFactory(tc -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                {
                    editBtn.setOnAction(e -> {
                        Project project = getTableView().getItems().get(getIndex());
                        TextInputDialog dialog = new TextInputDialog(project.getNom());
                        dialog.setTitle("Edit Project");
                        dialog.setHeaderText("Rename project");
                        dialog.setContentText("New name:");
                        dialog.showAndWait().ifPresent(name -> {
                            if (!name.isBlank()) {
                                project.setNom(name);
                                projectDAO.update(project);
                                getTableView().refresh();
                            }
                        });
                    });
                    deleteBtn.setOnAction(e -> {
                        Project project = getTableView().getItems().get(getIndex());
                        com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
                        com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
                        List<com.taskboard.model.Column> cols = columnDAO.getByProject(project);
                        for (com.taskboard.model.Column col : cols) {
                            List<com.taskboard.model.Task> tasks = taskDAO.getByColumn(col);
                            for (com.taskboard.model.Task t : tasks) {
                                taskDAO.delete(t.getId());
                            }
                            columnDAO.delete(col.getId());
                        }
                        projectDAO.delete(project.getId());
                        getTableView().getItems().remove(project);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox box = new HBox(5, editBtn, deleteBtn);
                        setGraphic(box);
                    }
                }
            });
            tvProjects.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedProject = newVal;
                    loadProjectTasks();
                }
            });
        }
        // Table2: Project tasks (no actions)
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
        tvProjects.getItems().add(project);
        tvProjects.getSelectionModel().select(project);
        lblProjectError.setText("");
    }

    @FXML
    private void onContinue() {
        System.out.println("[DEBUG] onContinue: selectedProject=" + selectedProject);

        if (selectedProject == null) {
            lblProjectError.setText("Select or create a project");
            return;
        }
        try {
            com.taskboard.session.CurrentProject.set(selectedProject);
            Stage stage = (Stage) tvProjects.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/column.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (java.io.IOException e) {
            lblProjectError.setText("Failed to load columns view");
            e.printStackTrace();
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
            Stage stage = (Stage) tvProjects.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblProjectError.setText("Failed to logout");
        }
    }
}
