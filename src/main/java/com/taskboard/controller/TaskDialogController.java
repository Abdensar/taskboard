package com.taskboard.controller;

import com.taskboard.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TaskDialogController {

    @FXML private TextField tfTitle;
    @FXML private TextArea  taDesc;
    @FXML private ComboBox<String> cbPriority;
    @FXML private DatePicker dpDue;
    @FXML private ComboBox<String> cbColumn;
    @FXML private ComboBox<String> cbDepartment;
    @FXML private TextField tfLabels;

    private Task task; // null = create

    @FXML
    public void initialize() {
        cbColumn.getItems().setAll("To Do", "In Progress", "Done");
        cbColumn.getSelectionModel().select("To Do");
        cbPriority.getItems().setAll("1 (Low)", "2", "3 (Medium)", "4", "5 (High)");
        cbPriority.getSelectionModel().select("3 (Medium)");
        cbDepartment.getItems().setAll("Development", "Marketing", "Sales", "HR", "Other");
        cbDepartment.getSelectionModel().select("Development");
        dpDue.setValue(LocalDate.now());
    }

    public void setTask(Task t) {
        this.task = t;
        tfTitle.setText(t.getTitle());
        taDesc.setText(t.getDescription());
        cbPriority.getSelectionModel().select(
            t.getPriority() == 1 ? "1 (Low)" :
            t.getPriority() == 3 ? "3 (Medium)" :
            t.getPriority() == 5 ? "5 (High)" : String.valueOf(t.getPriority())
        );
        dpDue.setValue(t.getDueDate());
        cbColumn.getSelectionModel().select(t.getColumn());
        cbDepartment.getSelectionModel().select(t.getDepartment() == null ? "Development" : t.getDepartment());
        tfLabels.setText(String.join(",", t.getLabels()));
    }

    public Optional<Task> getTask() {
        System.out.println("[DEBUG] getTask called");
        String title  = tfTitle.getText().trim();
        String desc   = taDesc.getText().trim();
        int priority  = cbPriority.getSelectionModel().getSelectedIndex() + 1;
        LocalDate due = dpDue.getValue();
        String column = cbColumn.getValue();
        String department = cbDepartment.getValue();
        List<String> labels = List.of(tfLabels.getText().trim().split("\\s*,\\s*"));
        System.out.println("[DEBUG] getTask values: title=" + title + ", desc=" + desc + ", priority=" + priority + ", due=" + due + ", column=" + column + ", department=" + department + ", labels=" + labels);
        if (title.isEmpty()) return Optional.empty();
        if (task == null) {
            Task newTask = new Task(title, desc, priority, due, column, labels, department);
            System.out.println("[DEBUG] getTask returning new: " + newTask);
            return Optional.of(newTask);
        } else {
            Task updatedTask = new Task(title, desc, priority, due, column, labels, department);
            updatedTask.setId(task.getId());
            System.out.println("[DEBUG] getTask returning updated: " + updatedTask);
            return Optional.of(updatedTask);
        }
    }
}