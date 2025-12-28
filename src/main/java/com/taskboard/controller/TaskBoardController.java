package com.taskboard.controller;

import com.taskboard.dao.TaskMorphiaDAO;
import com.taskboard.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.Optional;

public class TaskBoardController {
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String>  colTitle;
    @FXML private TableColumn<Task, String>  colDesc;
    @FXML private TableColumn<Task, Number>  colPriority;
    @FXML private TableColumn<Task, String>  colDue;
    @FXML private TableColumn<Task, String>  colLabels;
    @FXML private TableColumn<Task, Void> colActions;
    @FXML private TableColumn<Task, String>  colDepartment;
    @FXML private ComboBox<String> filterColumnCombo;
    @FXML private ComboBox<String> filterValueCombo;
    @FXML private DatePicker dueDateFilterPicker;

    @FXML private Button exportBtn;
    @FXML private Label lblColumnName;
    @FXML private ComboBox<com.taskboard.model.Project> projectSelector;
    @FXML private ComboBox<com.taskboard.model.Column> columnSelector;

    // Drag-and-drop support
    private Task draggedTask = null;

    private final TaskMorphiaDAO dao = new TaskMorphiaDAO();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private void loadColumnsForProject() {
        com.taskboard.model.Project selected = projectSelector.getSelectionModel().getSelectedItem();
        if (selected == null) {
            columnSelector.setItems(FXCollections.observableArrayList());
            return;
        }
        com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
        java.util.List<com.taskboard.model.Column> columns = columnDAO.getByProject(selected);
        columnSelector.getItems().clear();
        columnSelector.getItems().add(null); // null means 'All'
        columnSelector.getItems().addAll(columns);
        columnSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(com.taskboard.model.Column c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty ? null : (c == null ? "All" : c.getNom()));
            }
        });
        columnSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(com.taskboard.model.Column c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty ? null : (c == null ? "All" : c.getNom()));
            }
        });
        columnSelector.getSelectionModel().selectFirst();
    }

    private void loadTasksForSelection() {
        com.taskboard.model.Project selectedProject = projectSelector.getSelectionModel().getSelectedItem();
        com.taskboard.model.Column selectedColumn = columnSelector.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            taskList.clear();
            taskTable.setItems(taskList);
            return;
        }
        com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
        com.taskboard.dao.TaskMorphiaDAO taskDAO = new com.taskboard.dao.TaskMorphiaDAO();
        java.util.List<com.taskboard.model.Task> filtered;
        if (selectedColumn == null) {
            // All columns for project
            java.util.List<com.taskboard.model.Column> columns = columnDAO.getByProject(selectedProject);
            filtered = new java.util.ArrayList<>();
            for (com.taskboard.model.Column col : columns) {
                filtered.addAll(taskDAO.getByColumn(col));
            }
        } else {
            filtered = taskDAO.getByColumn(selectedColumn);
        }
        taskList.setAll(filtered);
        taskTable.setItems(taskList);
    }

    private void printAllTasks(String context) {
        List<Task> all = dao.getAll();
        System.out.println("[DEBUG] Task list after " + context + ":");
        for (Task t : all) {
            System.out.println("[DEBUG] " + t);
        }
    }

    @FXML
    public void initialize() {
        // Populate project selector
        com.taskboard.dao.ProjectMorphiaDAO projectDAO = new com.taskboard.dao.ProjectMorphiaDAO();
        com.taskboard.model.User currentUser = com.taskboard.session.CurrentUser.get();
        java.util.List<com.taskboard.model.Project> projects = projectDAO.getByOwner(currentUser);
        projectSelector.setItems(FXCollections.observableArrayList(projects));
        projectSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(com.taskboard.model.Project p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getNom());
            }
        });

        // Default select the current project from session
        com.taskboard.model.Project currentProject = com.taskboard.session.CurrentProject.get();
        if (currentProject != null) {
            for (com.taskboard.model.Project p : projects) {
                if (p.getId().equals(currentProject.getId())) {
                    projectSelector.getSelectionModel().select(p);
                    break;
                }
            }
        } else if (!projects.isEmpty()) {
            projectSelector.getSelectionModel().selectFirst();
        }
        loadColumnsForProject();

        // When project changes, reload columns and set default column
        projectSelector.setOnAction(e -> {
            loadColumnsForProject();
            // Default to first column of new project (not 'All')
            ObservableList<com.taskboard.model.Column> cols = columnSelector.getItems();
            if (cols != null && cols.size() > 1) {
                columnSelector.getSelectionModel().select(1); // skip 'All' (null)
            } else if (cols != null && cols.size() == 1) {
                columnSelector.getSelectionModel().select(0);
            }
            loadTasksForSelection();
        });

        // Default select the current column from session
        com.taskboard.model.Column currentColumn = com.taskboard.session.CurrentColumn.get();
        ObservableList<com.taskboard.model.Column> columns = columnSelector.getItems();
        if (currentColumn != null && columns != null) {
            for (com.taskboard.model.Column c : columns) {
                if (c != null && c.getId().equals(currentColumn.getId())) {
                    columnSelector.getSelectionModel().select(c);
                    break;
                }
            }
        } else if (columns != null && columns.size() > 1) {
            columnSelector.getSelectionModel().select(1); // skip 'All'
        }

        // When column changes, reload tasks
        columnSelector.setOnAction(e -> loadTasksForSelection());

        // Place selectors above the table in a horizontal layout (HBox)
        javafx.scene.Parent parent = taskTable.getParent();
        if (parent instanceof javafx.scene.layout.VBox vbox) {
            Label projectLabel = new Label("Project:");
            projectLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 5 0 0;");
            Label columnLabel = new Label("Column:");
            columnLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 5 0 15;");
            javafx.scene.layout.HBox selectorBar = new javafx.scene.layout.HBox(10, projectLabel, projectSelector, columnLabel, columnSelector);
            selectorBar.setStyle("-fx-padding: 10; -fx-alignment: center-left;");
            if (!vbox.getChildren().contains(selectorBar)) {
                vbox.getChildren().add(0, selectorBar);
            }
        }

        // Immediately load tasks for the default selection
        loadTasksForSelection();
        // Setup columns
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        colDesc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        colPriority.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPriorite()));
        colDue.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getEcheance() != null ? c.getValue().getEcheance().toString() : ""
        ));
        colLabels.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getEtiquettes() != null && !c.getValue().getEtiquettes().isEmpty()
                ? c.getValue().getEtiquettes().stream().map(label -> label.getNom()).reduce((a, b) -> a + ", " + b).orElse("")
                : ""
        ));
        // Setup Actions column with Edit and Delete buttons
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(8, editBtn, deleteBtn);
            {
                editBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 11px;");
                editBtn.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    onEdit(task);
                });
                deleteBtn.setOnAction(e -> {
                    Task task = getTableView().getItems().get(getIndex());
                    onDelete(task);
                });
                pane.setStyle("-fx-alignment: center;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        // Immediately load tasks for the default selection
        loadTasksForSelection();
        updateFilterValueCombo();
        applyFilter();
        // Hide the column menu button
        taskTable.setTableMenuButtonVisible(false);
        // Set column resize policy to constrained
        taskTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Drag-and-drop setup
        taskTable.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    draggedTask = row.getItem();
                    javafx.scene.input.Dragboard db = row.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(draggedTask.getId().toString());
                    db.setContent(content);
                    event.consume();
                }
            });
            row.setOnDragOver(event -> {
                if (draggedTask != null && row.getItem() != null && row.getItem() != draggedTask) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                    event.consume();
                }
            });
            row.setOnDragDropped(event -> {
                if (draggedTask != null && row.getItem() != null && row.getItem() != draggedTask) {
                    int draggedIdx = taskList.indexOf(draggedTask);
                    int targetIdx = row.getIndex();
                    // Move task in the list
                    taskList.remove(draggedTask);
                    taskList.add(targetIdx, draggedTask);
                    // Optionally update column if moving between columns (not shown here)
                    taskTable.setItems(taskList);
                    // Persist order if needed (implement in DAO if you want to save order)
                    dao.update(draggedTask);
                    draggedTask = null;
                    event.setDropCompleted(true);
                    event.consume();
                }
            });
            row.setOnDragDone(event -> draggedTask = null);
            return row;
        });

        // Filtering section setup
        if (filterColumnCombo != null) {
            filterColumnCombo.setItems(FXCollections.observableArrayList("All", "Priorité", "Étiquette", "Date d’échéance"));
            filterColumnCombo.getSelectionModel().selectFirst();
            filterColumnCombo.setOnAction(e -> updateFilterValueCombo());
        }
        if (filterValueCombo != null) {
            filterValueCombo.setOnAction(e -> applyFilter());
        }
        if (dueDateFilterPicker != null) {
            dueDateFilterPicker.setValue(java.time.LocalDate.now());
            dueDateFilterPicker.setOnAction(e -> applyFilter());
        }
    }

    private void updateFilterValueCombo() {
        if (filterColumnCombo == null || filterValueCombo == null || dueDateFilterPicker == null) return;
        String column = filterColumnCombo.getValue();
        if (column == null) {
            filterValueCombo.setVisible(false);
            dueDateFilterPicker.setVisible(false);
            filterValueCombo.getItems().clear();
            return;
        }
        if (column.equals("Échéance")) {
            filterValueCombo.setVisible(false);
            dueDateFilterPicker.setVisible(true);
            dueDateFilterPicker.setValue(null);
            return;
        } else {
            filterValueCombo.setVisible(true);
            dueDateFilterPicker.setVisible(false);
        }
        filterValueCombo.getItems().clear();
        switch (column) {
            case "Étiquette": {
                List<String> etiquettes = taskList.stream()
                    .flatMap(t -> t.getEtiquettes() != null ? t.getEtiquettes().stream().map(label -> label.getNom()) : java.util.stream.Stream.empty())
                    .filter(l -> l != null && !l.isBlank())
                    .distinct().sorted().collect(java.util.stream.Collectors.toList());
                filterValueCombo.getItems().add("Toutes");
                filterValueCombo.getItems().addAll(etiquettes);
                break;
            }
            case "Priorité":
                filterValueCombo.getItems().add("Toutes");
                filterValueCombo.getItems().addAll("1 (Faible)", "2", "3 (Moyenne)", "4", "5 (Haute)");
                break;
        }
        filterValueCombo.setValue("Toutes");
        applyFilter();
    }

    private void applyFilter() {
        if (filterColumnCombo == null || filterValueCombo == null || dueDateFilterPicker == null) {
            taskTable.setItems(taskList);
            return;
        }
        String column = filterColumnCombo.getValue();
        ObservableList<Task> filtered = FXCollections.observableArrayList(taskList);
        if (column == null) {
            taskTable.setItems(filtered);
            return;
        }
        if (column.equals("Échéance")) {
            if (dueDateFilterPicker.getValue() == null) {
                taskTable.setItems(filtered);
                return;
            }
            filtered = FXCollections.observableArrayList(
                filtered.stream().filter(t -> t.getEcheance() != null &&
                    new java.sql.Date(t.getEcheance().getTime()).toLocalDate().equals(dueDateFilterPicker.getValue())
                ).toList()
            );
            taskTable.setItems(filtered);
            return;
        }
        String value = filterValueCombo.getValue();
        if (value == null || value.equals("Toutes")) {
            taskTable.setItems(filtered);
            return;
        }
        switch (column) {
            case "Étiquette":
                filtered = FXCollections.observableArrayList(
                    filtered.stream().filter(t -> t.getEtiquettes() != null &&
                        t.getEtiquettes().stream().anyMatch(l -> l.getNom().equals(value))
                    ).toList()
                );
                break;
            case "Priorité":
                int priorite = switch (value) {
                    case "1 (Faible)" -> 1;
                    case "3 (Moyenne)" -> 3;
                    case "5 (Haute)" -> 5;
                    default -> Integer.parseInt(value.replaceAll("[^0-9]", ""));
                };
                filtered = FXCollections.observableArrayList(
                    filtered.stream().filter(t -> t.getPriorite() == priorite).toList()
                );
                break;
        }
        taskTable.setItems(filtered);
        taskTable.refresh();
    }

    @FXML
    private void onRefresh() {
        loadTasksForSelection();
    }

    @FXML
    private void onExport() {
        try {
            String filePath = "tasks_export.csv";
            com.taskboard.util.TaskExportUtil.exportTasksToCSV(taskTable.getItems(), filePath);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Success");
            alert.setHeaderText(null);
            alert.setContentText("Tasks exported to " + filePath);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Failed to export tasks");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onBackToColumns() {
        try {
            Stage stage = (Stage) taskTable.getScene().getWindow();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/column.fxml"));
            stage.getScene().setRoot(loader.load());
        } catch (java.io.IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load columns view");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onAddTask() {
        System.out.println("[DEBUG] onAddTask called");
        com.taskboard.model.Column selectedCol = columnSelector.getSelectionModel().getSelectedItem();
        if (selectedCol == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Column Selected");
            alert.setHeaderText(null);
            alert.setContentText("No column is selected. Please select a column.");
            alert.showAndWait();
            return;
        }
        Optional<Task> opt = showTaskDialogWithColumn(selectedCol);
        opt.ifPresent(task -> {
            // Set the selected column for the new task
            task.setColonne(selectedCol);
            System.out.println("[DEBUG] onAddTask received task: " + task);
            dao.create(task);
            printAllTasks("add");
            loadTasksForSelection();
        });
    }

    @FXML
    private void onEdit(Task task) {
        System.out.println("[DEBUG] onEdit called for: " + task);
        Optional<Task> opt = showTaskDialog(task);
        opt.ifPresent(t -> {
            System.out.println("[DEBUG] onEdit received task: " + t);
            dao.update(t);
            printAllTasks("edit");
        });
    }

    @FXML
    private void onDelete(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this task?\n" + task.getTitre());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dao.delete(task.getId());
            printAllTasks("delete");
            loadTasksForSelection();
        }
    }

    // New method: pass the selected column to dialog controller
    private Optional<Task> showTaskDialogWithColumn(com.taskboard.model.Column selectedCol) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taskDialog.fxml"));
            DialogPane pane = loader.load();
            TaskDialogController ctrl = loader.getController();
            if (selectedCol != null) ctrl.setColumn(selectedCol);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("New Task");
            ButtonType saveButton = pane.getButtonTypes().stream()
                .filter(bt -> "Save".equals(bt.getText()))
                .findFirst().orElse(ButtonType.OK);
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButton) {
                return ctrl.getTask();
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Legacy method kept for edit functionality
    private Optional<Task> showTaskDialog(Task existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/taskDialog.fxml"));
            DialogPane pane = loader.load();
            TaskDialogController ctrl = loader.getController();
            if (existing != null) ctrl.setTask(existing);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle(existing == null ? "New Task" : "Edit Task");

            // Get the Save button instance from the FXML
            ButtonType saveButton = pane.getButtonTypes().stream()
                .filter(bt -> "Save".equals(bt.getText()))
                .findFirst().orElse(ButtonType.OK);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButton) {
                return ctrl.getTask();
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
    