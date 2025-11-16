package com.taskboard.model;

import dev.morphia.annotations.*;
import java.time.LocalDate;
import java.util.List;

@Entity("tasks")               // MongoDB collection name
public class Task {

    @Id
    private String id;          // MongoDB _id

    private String title;
    private String description;
    private int priority;       // 1-5
    private LocalDate dueDate;
    private String column;      // "To Do", "In Progress", "Done"
    private List<String> labels;

    /* ---------- Constructors ---------- */
    public Task() {}            // Morphia needs empty constructor

    public Task(String title, String description, int priority,
                LocalDate dueDate, String column, List<String> labels) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.column = column;
        this.labels = labels;
    }

    /* ---------- Getters & Setters ---------- */
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    /* ---------- toString ---------- */
    @Override
    public String toString() {
        return title + " (" + column + ")";
    }
}