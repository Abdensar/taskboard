package com.taskboard.model;

import dev.morphia.annotations.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity("tasks")
public class Task {
    @Id
    private String id;
    private String title;
    private String description;
    private int priority;
    private LocalDate dueDate;
    private String column;
    private List<String> labels;
    private String department;

    public Task() {
        // Morphia needs an empty constructor
    }

    public Task(String title, String description, int priority, LocalDate dueDate, String column, List<String> labels, String department) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.column = column;
        this.labels = labels;
        this.department = department;
    }

    // Backward compatibility constructor
    public Task(String title, String description, int priority, LocalDate dueDate, String column, List<String> labels) {
        this(title, description, priority, dueDate, column, labels, "");
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }
    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return title + " (" + column + ")";
    }
}