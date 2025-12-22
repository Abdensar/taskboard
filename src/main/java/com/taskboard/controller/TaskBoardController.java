package com.taskboard.controller;

import com.taskboard.dao.TaskMorphiaDAO;
import com.taskboard.model.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    private final TaskMorphiaDAO dao = new TaskMorphiaDAO();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private void loadTasks() {
        List<Task> all = dao.findAll();
        taskList.setAll(all);
        taskTable.setItems(taskList);
    }

    private void printAllTasks(String context) {
        List<Task> all = dao.findAll();
        System.out.println("[DEBUG] Task list after " + context + ":");
        for (Task t : all) {
            System.out.println("[DEBUG] " + t);
        }
    }

    @FXML
    public void initialize() {
        // Setup columns
        colTitle.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));
        colDesc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDescription()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getColumn()));
        colPriority.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getPriority()));
        colDepartment.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDepartment()));
        colDue.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getDueDate())));
        colDepartment.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getDepartment() != null ? c.getValue().getDepartment() : ""
        ));
        colLabels.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getLabels() != null ? String.join(", ", c.getValue().getLabels()) : ""
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
                    alert.setContentText(t.getTitle());
                    alert.showAndWait().ifPresent(result -> {
                        if (result == ButtonType.OK) {
                            try {
                                dao.delete(t);
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

        // Filtering section setup
        if (filterColumnCombo != null) {
            filterColumnCombo.getItems().setAll("Department", "Label", "Priority", "Status", "Due Date");
            filterColumnCombo.setValue("Department");
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
        if (column == null || column.equals("Select column...")) {
            filterValueCombo.setVisible(false);
            dueDateFilterPicker.setVisible(false);
            filterValueCombo.getItems().clear();
            return;
        }
        if (column.equals("Due Date")) {
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
            case "Department": {
                List<String> departments = taskList.stream().map(Task::getDepartment)
                    .filter(dep -> dep != null && !dep.isBlank())
                    .distinct().sorted().collect(java.util.stream.Collectors.toList());
                filterValueCombo.getItems().add("All");
                filterValueCombo.getItems().addAll(departments);
                break;
            }
            case "Label": {
                List<String> labels = taskList.stream()
                    .flatMap(t -> t.getLabels() != null ? t.getLabels().stream() : java.util.stream.Stream.empty())
                    .filter(l -> l != null && !l.isBlank())
                    .distinct().sorted().collect(java.util.stream.Collectors.toList());
                filterValueCombo.getItems().add("All");
                filterValueCombo.getItems().addAll(labels);
                break;
            }
            case "Priority":
                filterValueCombo.getItems().add("All");
                filterValueCombo.getItems().addAll("1 (Low)", "2", "3 (Medium)", "4", "5 (High)");
                break;
            case "Status":
                filterValueCombo.getItems().add("All");
                filterValueCombo.getItems().addAll("To Do", "In Progress", "Done");
                break;
        }
        filterValueCombo.setValue("All");
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
        if (column.equals("Due Date")) {
            if (dueDateFilterPicker.getValue() == null) {
                taskTable.setItems(filtered);
                return;
            }
            filtered = FXCollections.observableArrayList(
                filtered.stream().filter(t -> dueDateFilterPicker.getValue().equals(t.getDueDate())).toList()
            );
            taskTable.setItems(filtered);
            return;
        }
        String value = filterValueCombo.getValue();
        if (value == null || value.equals("All")) {
            taskTable.setItems(filtered);
            return;
        }
        switch (column) {
            case "Department":
                filtered = FXCollections.observableArrayList(
                    filtered.stream().filter(t -> value.equals(t.getDepartment())).toList()
                );
                break;
            case "Label":
                filtered = FXCollections.observableArrayList(
                    filtered.stream().filter(t -> t.getLabels() != null && t.getLabels().contains(value)).toList()
                );
                break;
            case "Priority":
                int priority = switch (value) {
                    case "1 (Low)" -> 1;
                    case "3 (Medium)" -> 3;
                    case "5 (High)" -> 5;
                    default -> Integer.parseInt(value.replaceAll("[^0-9]", ""));
                };
                filtered = FXCollections.observableArrayList(
                    filtered.stream().filter(t -> t.getPriority() == priority).toList()
                );
                break;
            case "Status":
                filtered = FXCollections.observableArrayList(
                    filtered.stream().filter(t -> value.equals(t.getColumn())).toList()
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
    private void onAddTask() {
        System.out.println("[DEBUG] onAddTask called");
        Optional<Task> opt = showTaskDialog(null);
        opt.ifPresent(task -> {
            System.out.println("[DEBUG] onAddTask received task: " + task);
            dao.save(task);
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
            dao.save(t);
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