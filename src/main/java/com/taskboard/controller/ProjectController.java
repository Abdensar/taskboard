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

import com.taskboard.model.User;
import com.taskboard.dao.UserMorphiaDAO;

public class ProjectController {
    @FXML private MenuButton userMenuBtn;
    @FXML private MenuItem userNameMenuItem;
    @FXML private MenuItem userEmailMenuItem;
    @FXML private TextField tfProjectName;
    @FXML private Label lblProjectError;
    @FXML private TableView<Project> tvProjects;
    @FXML private TableColumn<Project, String> colProjectName;
    @FXML private TableColumn<Project, String> colOwnership;
    @FXML private TextField tfShareEmail;
    @FXML private Button btnShare;
    @FXML private Label lblShareError;
    @FXML private TableColumn<Project, Void> colProjectActions;
    @FXML private TableView<com.taskboard.model.Task> tvProjectTasks;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskTitle;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskStatus;
    private final ProjectMorphiaDAO projectDAO = new ProjectMorphiaDAO();

    private Project selectedProject;

    @FXML
    public void initialize() {
        // Set user dropdown info
        User currentUser = com.taskboard.session.CurrentUser.get();
        if (userNameMenuItem != null && currentUser != null) {
            userNameMenuItem.setText("Username: " + currentUser.getNom());
        }
        if (userEmailMenuItem != null && currentUser != null) {
            userEmailMenuItem.setText("Email: " + currentUser.getEmail());
        }
        if (btnShare != null) {
            btnShare.setOnAction(e -> onShare());
        }
    
        // Center the main layout if it's a VBox
        if (tvProjects != null && tvProjects.getParent() instanceof javafx.scene.layout.VBox vbox) {
            vbox.setAlignment(javafx.geometry.Pos.CENTER);
        }        // Table1: Projects table with edit/delete actions
        List<Project> ownedProjects = projectDAO.getByOwner(currentUser);
        List<Project> sharedProjects = projectDAO.getBySharedUser(currentUser);
        java.util.Set<Project> allProjects = new java.util.LinkedHashSet<>();
        allProjects.addAll(ownedProjects);
        allProjects.addAll(sharedProjects);
        java.util.List<Project> sortedProjects = new java.util.ArrayList<>(allProjects);
        sortedProjects.sort((a, b) -> {
            boolean aOwned = a.getOwner().equals(currentUser);
            boolean bOwned = b.getOwner().equals(currentUser);
            return Boolean.compare(!aOwned, !bOwned); // owned first
        });
        tvProjects.setItems(FXCollections.observableArrayList(sortedProjects));
        colProjectName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
        colOwnership.setCellValueFactory(data -> {
            Project p = data.getValue();
            String type = "";
            if (p.getOwner() != null && p.getOwner().getId().equals(currentUser.getId())) {
                type = "Owner";
            } else if (p.getSharedUsers() != null) {
                for (User u : p.getSharedUsers()) {
                    if (u.getId().equals(currentUser.getId())) {
                        type = "Shared";
                        break;
                    }
                }
            }
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        colProjectActions.setCellFactory(tc -> new TableCell<Project, Void>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                {
                    editBtn.setOnAction(e -> {
                        Project project = getTableView().getItems().get(getIndex());
                        User currentUser = com.taskboard.session.CurrentUser.get();
                        if (!project.getOwner().equals(currentUser) && (project.getSharedUsers() == null || !project.getSharedUsers().stream().anyMatch(u -> u.getId().equals(currentUser.getId())))) return;
                        TextInputDialog dialog = new TextInputDialog(project.getNom());
                        dialog.setTitle("Edit Project");
                        dialog.setHeaderText("Rename project");
                        dialog.setContentText("New name:");
                        dialog.showAndWait().ifPresent(name -> {
                            if (!name.isBlank()) {
                                project.setNom(name);
                                projectDAO.update(project);
                                getTableView().refresh();
                            }                        });
                    });
                    deleteBtn.setOnAction(e -> {
                        Project project = getTableView().getItems().get(getIndex());
                        User currentUser = com.taskboard.session.CurrentUser.get();
                        if (project.getOwner().equals(currentUser)) {
                            // Owner: delete project and all related data
                            com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
                            com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
                            List<com.taskboard.model.Column> cols = columnDAO.getByProject(project);
                            for (com.taskboard.model.Column col : cols) {
                                List<com.taskboard.model.Task> tasks = taskDAO.getByColumn(col);
                                for (com.taskboard.model.Task t : tasks) {
                                    taskDAO.delete(t.getId());
                                }                                columnDAO.delete(col.getId());
                            }                            projectDAO.delete(project.getId());
                            getTableView().getItems().remove(project);
                        } else if (project.getSharedUsers() != null && project.getSharedUsers().stream().anyMatch(u -> u.getId().equals(currentUser.getId()))) {
                            // Shared user: remove their id from sharedUsers
                            project.getSharedUsers().removeIf(u -> u.getId().equals(currentUser.getId()));
                            projectDAO.update(project);
                            getTableView().getItems().remove(project);
                        }
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Project project = getTableView().getItems().get(getIndex());
                        boolean isOwner = project.getOwner().equals(com.taskboard.session.CurrentUser.get());
                        boolean isSelected = project.equals(selectedProject);
                        editBtn.setDisable(false);
                        deleteBtn.setDisable(false);
                        editBtn.setOnAction(e -> {
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
                            // Delete project and all related data
                            com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
                            com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
                            java.util.List<com.taskboard.model.Column> cols = columnDAO.getByProject(project);
                            for (com.taskboard.model.Column col : cols) {
                                java.util.List<com.taskboard.model.Task> tasks = taskDAO.getByColumn(col);
                                for (com.taskboard.model.Task t : tasks) {
                                    taskDAO.delete(t.getId());
                                }
                                columnDAO.delete(col.getId());
                            }
                            projectDAO.delete(project.getId());
                            getTableView().getItems().remove(project);
                        });
                        HBox box = new HBox(5, editBtn, deleteBtn);
                        setGraphic(box);
                    }                }            });
            tvProjects.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedProject = newVal;
                    loadProjectTasks();
                }
            });
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
        selectedProject = project;
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
    private void onShare() {
        lblShareError.setText("");
        if (selectedProject == null) {
            lblShareError.setText("No project selected.");
            return;
        }
        String email = tfShareEmail.getText().trim();
        if (email.isEmpty()) {
            lblShareError.setText("Please enter an email.");
            return;
        }
        UserMorphiaDAO userDAO = new UserMorphiaDAO();
        User user = userDAO.getByEmail(email);
        if (user == null) {
            lblShareError.setText("User with this email does not exist.");
            return;
        }
        // Strictly enforce: owner cannot be in sharedUsers, shared user cannot share to owner
        if (selectedProject.getOwner().getId().equals(user.getId())) {
            if (!com.taskboard.session.CurrentUser.get().getId().equals(user.getId())) {
                lblShareError.setText("A shared user cannot share the project to the owner.");
            } else {
                lblShareError.setText("You cannot share with yourself.");
            }
            return;
        }
        // Ensure owner is never in sharedUsers
        if (selectedProject.getSharedUsers() != null) {
            selectedProject.getSharedUsers().removeIf(u -> u.getId().equals(selectedProject.getOwner().getId()));
        }
        if (selectedProject.getSharedUsers() != null && selectedProject.getSharedUsers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
            lblShareError.setText("Already shared with this user.");
            return;
        }
        selectedProject.addSharedUser(user);
        new ProjectMorphiaDAO().update(selectedProject);
        lblShareError.setText("Project shared successfully.");
        tfShareEmail.clear();
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
