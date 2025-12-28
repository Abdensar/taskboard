package com.taskboard.controller;
import javafx.scene.layout.HBox;

import com.taskboard.dao.ColumnMorphiaDAO;
import com.taskboard.model.Column;
import com.taskboard.model.Project;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class ColumnController {
    @FXML private TextField tfColumnName;
    @FXML private Spinner<Integer> spOrder;
    @FXML private Label lblColumnError;
    @FXML private Label lblProjectName;
    @FXML private TableView<com.taskboard.model.Task> tvColumnTasks;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskTitle;
    @FXML private TableColumn<com.taskboard.model.Task, String> colTaskLabels;

    private final ColumnMorphiaDAO columnDAO = new ColumnMorphiaDAO();
    private final List<Column> columns = new ArrayList<>();
    @FXML private TableView<Column> tvColumns;
    @FXML private TableColumn<Column, String> colColumnName;
    @FXML private TableColumn<Column, Void> colColumnActions;
    private Project currentProject;

    @FXML
    public void initialize() {
        currentProject = com.taskboard.session.CurrentProject.get();
        if (currentProject != null) {
            lblProjectName.setText("Project: " + currentProject.getNom());
            List<Column> projectColumns = columnDAO.getByProject(currentProject);
            columns.clear();
            columns.addAll(projectColumns);
            tvColumns.setItems(FXCollections.observableArrayList(columns));
            tvColumns.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tvColumns.setOnMouseClicked(e -> loadColumnTasks());
        }
        colTaskTitle.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitre()));
        colTaskLabels.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getEtiquettes() != null ? data.getValue().getEtiquettes().stream().map(l -> l.getNom()).reduce((a, b) -> a + ", " + b).orElse("") : ""
        ));
        loadColumnTasks();
        spOrder.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Setup columns table
        if (tvColumns != null && colColumnName != null && colColumnActions != null) {
            tvColumns.setItems(FXCollections.observableArrayList(columns));
            colColumnName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
            colColumnActions.setCellFactory(tc -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");
                {
                    editBtn.setOnAction(e -> {
                        Column col = getTableView().getItems().get(getIndex());
                        TextInputDialog dialog = new TextInputDialog(col.getNom());
                        dialog.setTitle("Edit Column");
                        dialog.setHeaderText("Rename column");
                        dialog.setContentText("New name:");
                        dialog.showAndWait().ifPresent(name -> {
                            if (!name.isBlank()) {
                                col.setNom(name);
                                columnDAO.update(col);
                                getTableView().refresh();
                            }
                        });
                    });
                    deleteBtn.setOnAction(e -> {
                        Column col = getTableView().getItems().get(getIndex());
                        com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
                        List<com.taskboard.model.Task> tasks = taskDAO.getByColumn(col);
                        for (com.taskboard.model.Task t : tasks) {
                            taskDAO.delete(t.getId());
                        }
                        columnDAO.delete(col.getId());
                        getTableView().getItems().remove(col);
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
        }
    }

    @FXML
    private void onAddColumn() {
        String name = tfColumnName.getText().trim();
        int order = spOrder.getValue();
        Project project = com.taskboard.session.CurrentProject.get();
        if (name.isEmpty()) {
            lblColumnError.setText("Column name required");
            return;
        }
        if (project == null) {
            lblColumnError.setText("No project selected. Please go back and select a project.");
            return;
        }
        Column col = new Column(project, name, order);
        columnDAO.create(col);
        columns.add(col);
        tvColumns.setItems(FXCollections.observableArrayList(columns));
        tfColumnName.clear();
        spOrder.getValueFactory().setValue(1);
        lblColumnError.setText("");
    }

    @FXML
    private void onContinue() {
        System.out.println("[DEBUG] onContinue: selectedColumn=" + tvColumns.getSelectionModel().getSelectedItem());
        Column selectedColumn = tvColumns.getSelectionModel().getSelectedItem();
        if (selectedColumn == null) {
            lblColumnError.setText("Select a column to continue");
            return;
        }
        com.taskboard.session.CurrentColumn.set(selectedColumn);
        try {
            Stage stage = (Stage) tvColumns.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/taskboard.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (java.io.IOException e) {
            lblColumnError.setText("Failed to load task board");
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackToProjects() {
        try {
            Stage stage = (Stage) tvColumns.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/project.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblColumnError.setText("Failed to go back to projects");
        }
    }

    private void loadColumnTasks() {
        if (tvColumns == null || tvColumnTasks == null) return;
        Column selected = tvColumns.getSelectionModel().getSelectedItem();
        if (selected == null) {
            tvColumnTasks.setItems(FXCollections.observableArrayList());
            return;
        }
        com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
        List<com.taskboard.model.Task> tasks = taskDAO.getByColumn(selected);
        tvColumnTasks.setItems(FXCollections.observableArrayList(tasks));
    }

    @FXML
    private void onLogout() {
        com.taskboard.session.CurrentUser.clear();
        com.taskboard.session.CurrentProject.clear();
        try {
            Stage stage = (Stage) tvColumns.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) {
            lblColumnError.setText("Failed to logout");
        }
    }
}
