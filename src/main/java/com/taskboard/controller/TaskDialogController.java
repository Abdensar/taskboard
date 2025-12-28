package com.taskboard.controller;

import com.taskboard.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import com.taskboard.model.Column;
import java.util.List;
import java.util.Optional;

public class TaskDialogController {

    @FXML private TextField tfTitle;
    @FXML private TextArea  taDesc;
    @FXML private ComboBox<String> cbPriority;
    @FXML private DatePicker dpDue;
    @FXML private ComboBox<String> cbLabels;
    @FXML private Label lblCurrentColumn;

    private Task task; // null = create

    @FXML
    public void initialize() {
        // TODO: Replace with actual project context
        cbPriority.getItems().setAll("1 (Low)", "2", "3 (Medium)", "4", "5 (High)");
        cbPriority.getSelectionModel().select("3 (Medium)");
        dpDue.setValue(LocalDate.now());
        com.taskboard.model.Column col = com.taskboard.session.CurrentColumn.get();
        if (lblCurrentColumn != null && col != null) {
            lblCurrentColumn.setText("Column: " + col.getNom());
        }
        // Populate label ComboBox with project labels
        cbLabels.getItems().clear();
        if (col != null && col.getProjet() != null) {
            com.taskboard.dao.LabelMorphiaDAO labelDAO = new com.taskboard.dao.LabelMorphiaDAO();
            java.util.List<com.taskboard.model.Label> labels = labelDAO.getAllByProject(col.getProjet());
            for (com.taskboard.model.Label l : labels) {
                cbLabels.getItems().add(l.getNom());
            }
        }
        cbLabels.getItems().add("+ Add new label...");
        cbLabels.setOnAction(e -> {
            String selected = cbLabels.getValue();
            if ("+ Add new label...".equals(selected)) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add Label");
                dialog.setHeaderText("Create a new label for this project");
                dialog.setContentText("Label name:");
                dialog.showAndWait().ifPresent(name -> {
                    if (!name.isBlank()) {
                        com.taskboard.model.Label newLabel = new com.taskboard.model.Label(name);
                        newLabel.setProject(col.getProjet());
                        new com.taskboard.dao.LabelMorphiaDAO().create(newLabel);
                        cbLabels.getItems().add(cbLabels.getItems().size() - 1, name);
                        cbLabels.setValue(name);
                    }
                });
            }
        });
    }

    public void setTask(Task t) {
        this.task = t;
        tfTitle.setText(t.getTitre());
        taDesc.setText(t.getDescription());
        cbPriority.getSelectionModel().select(
            t.getPriorite() == 1 ? "1 (Low)" :
            t.getPriorite() == 3 ? "3 (Medium)" :
            t.getPriorite() == 5 ? "5 (High)" : String.valueOf(t.getPriorite())
        );
        if (t.getEcheance() != null) {
            dpDue.setValue(new java.sql.Date(t.getEcheance().getTime()).toLocalDate());
        }
        // For column, department, and labels: adapt as needed to your UI logic or remove if not used
    }

    public Optional<Task> getTask() {
        System.out.println("[DEBUG] getTask called");
        String titre  = tfTitle.getText().trim();
        String desc   = taDesc.getText().trim();
        int priorite  = cbPriority.getSelectionModel().getSelectedIndex() + 1;
        java.util.Date echeance = null;
        if (dpDue.getValue() != null) {
            echeance = java.sql.Date.valueOf(dpDue.getValue());
        }
        com.taskboard.model.Column selectedCol = com.taskboard.session.CurrentColumn.get();
        com.taskboard.model.Project project = selectedCol != null ? selectedCol.getProjet() : null;
        List<com.taskboard.model.Label> etiquettes = new java.util.ArrayList<>();
        if (cbLabels.getValue() != null && project != null && !cbLabels.getValue().isBlank() && !cbLabels.getValue().equals("+ Add new label...")) {
            com.taskboard.dao.LabelMorphiaDAO labelDAO = new com.taskboard.dao.LabelMorphiaDAO();
            String trimmed = cbLabels.getValue().trim();
            com.taskboard.model.Label existing = labelDAO.getByNameAndProject(trimmed, project);
            if (existing != null) {
                etiquettes.add(existing);
            } else {
                com.taskboard.model.Label newLabel = new com.taskboard.model.Label(trimmed);
                newLabel.setProject(project);
                labelDAO.create(newLabel);
                etiquettes.add(newLabel);
            }
        }
        if (titre.isEmpty() || selectedCol == null) return Optional.empty();
        if (task == null) {
            Task newTask = new Task(selectedCol, titre, desc, priorite, echeance, etiquettes);
            System.out.println("[DEBUG] getTask returning new: " + newTask);
            return Optional.of(newTask);
        } else {
            Task updatedTask = new Task(selectedCol, titre, desc, priorite, echeance, etiquettes);
            updatedTask.setId(task.getId());
            System.out.println("[DEBUG] getTask returning updated: " + updatedTask);
            return Optional.of(updatedTask);
        }
    }
}