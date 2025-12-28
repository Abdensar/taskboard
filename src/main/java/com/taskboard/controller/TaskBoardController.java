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
    @FXML private TableColumn<Task, String>  colStatus;
    @FXML private TableColumn<Task, Number>  colPriority;
    @FXML private TableColumn<Task, String>  colDue;
    @FXML private TableColumn<Task, String>  colLabels;
    @FXML private TableColumn<Task, String>  colDepartment;
    @FXML private TableColumn<Task, Void>    colActions;
    @FXML private ComboBox<String> filterColumnCombo;
    @FXML private ComboBox<String> filterValueCombo;
    @FXML private DatePicker dueDateFilterPicker;

    @FXML private Button exportBtn;
    @FXML private Label lblColumnName;

    // Drag-and-drop support
    private Task draggedTask = null;

    private final TaskMorphiaDAO dao = new TaskMorphiaDAO();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private void loadTasks() {
        com.taskboard.model.Column col = com.taskboard.session.CurrentColumn.get();
        List<Task> filtered = col != null ? dao.getByColumn(col) : List.of();
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
        // Show current column name
        com.taskboard.model.Column currentColumn = com.taskboard.session.CurrentColumn.get();
        if (lblColumnName != null && currentColumn != null) {
            lblColumnName.setText("Column: " + currentColumn.getNom());
        }
        // Setup columns
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        colDesc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getColonne() != null ? c.getValue().getColonne().getNom() : ""
        ));
        colPriority.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPriorite()));
        colDue.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getEcheance() != null ? c.getValue().getEcheance().toString() : ""
        ));
        colLabels.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getEtiquettes() != null && !c.getValue().getEtiquettes().isEmpty()
                ? c.getValue().getEtiquettes().stream().map(label -> label.getNom()).reduce((a, b) -> a + ", " + b).orElse("")
                : ""
        ));
        colActions.setCellFactory(tc -> new TableCell<Task, Void>() {
            private final Button editBtn = new Button();
            private final Button delBtn  = new Button();
            private final HBox pane = new HBox(8, editBtn, delBtn);
            {
                editBtn.setText("✎");
                editBtn.setTooltip(new Tooltip("Edit Task"));
                editBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-background-radius: 50%; -fx-font-weight: bold; -fx-padding: 6; -fx-font-size: 14px; width: 32px; height: 32px;");
                delBtn.setText("🗑");
                delBtn.setTooltip(new Tooltip("Delete Task"));
                delBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-background-radius: 50%; -fx-font-weight: bold; -fx-padding: 6; -fx-font-size: 14px; width: 32px; height: 32px;");
                pane.setStyle("-fx-alignment: center;");
                editBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    onEdit(t);
                });
                delBtn.setOnAction(e -> {
                    Task t = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Task");
                    alert.setHeaderText("Are you sure you want to delete this task?");
                    alert.setContentText(t.getTitre());
                    alert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                dao.delete(t.getId());
                                loadTasks();
                            } catch (Exception ex) {
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Error");
                                errorAlert.setHeaderText("Failed to delete task");
                                errorAlert.setContentText(ex.getMessage());
                                errorAlert.showAndWait();
                                ex.printStackTrace();
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        loadTasks();
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
            filterColumnCombo.getItems().setAll("Priorité", "Étiquette", "Échéance");
            filterColumnCombo.setValue("Priorité");
            filterColumnCombo.setOnAction(e -> updateFilterValueCombo());
        }
        if (filterValueCombo != null) {
            filterValueCombo.setOnAction(e -> applyFilter());
        }
        if (dueDateFilterPicker != null) {
            dueDateFilterPicker.setOnAction(e -> applyFilter());
        }
        loadTasks();
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
                    .flatMap(t -> t.getEtiquettes() != null ? t.getEtiquettes().stream().map(l -> l.getNom()) : java.util.stream.Stream.empty())
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
        loadTasks();
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
        if (com.taskboard.session.CurrentColumn.get() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No Column Selected");
            alert.setHeaderText(null);
            alert.setContentText("No column is selected. Please go back and select a column.");
            alert.showAndWait();
            return;
        }
        Optional<Task> opt = showTaskDialog(null);
        opt.ifPresent(task -> {
            // Always reload the column from DB to ensure it has a valid ID
            com.taskboard.model.Column col = com.taskboard.session.CurrentColumn.get();
            if (col != null && col.getId() != null) {
                com.taskboard.dao.ColumnMorphiaDAO columnDAO = new com.taskboard.dao.ColumnMorphiaDAO();
                com.taskboard.model.Column dbCol = columnDAO.getById(col.getId());
                if (dbCol != null) {
                    task.setColonne(dbCol);
                }
            }
            System.out.println("[DEBUG] onAddTask received task: " + task);
            dao.create(task);
            loadTasks();
            printAllTasks("add");
        });
    }

    @FXML
    private void onEdit(Task task) {
        System.out.println("[DEBUG] onEdit called for: " + task);
        Optional<Task> opt = showTaskDialog(task);
        opt.ifPresent(t -> {
            System.out.println("[DEBUG] onEdit received task: " + t);
            dao.update(t);
            loadTasks();
            printAllTasks("edit");
        });
    }

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