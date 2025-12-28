package com.taskboard.app;

import com.taskboard.dao.TaskMorphiaDAO;
import com.taskboard.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MainCLI {

    private static final TaskMorphiaDAO dao = new TaskMorphiaDAO();

    public static void main(String[] args) {
        System.out.println("=== TaskBoard CLI Test ===");

        // 1) Insert a sample task
        com.taskboard.model.Column col = new com.taskboard.model.Column();
        col.setNom("To Do");
        com.taskboard.model.Label label = new com.taskboard.model.Label("study");
        Task t = new Task(col,
                          "Learn Morphia",
                          "Understand how to save & query",
                          3,
                          java.sql.Date.valueOf(LocalDate.now().plusDays(2)),
                          List.of(label));
        dao.create(t);
        System.out.println("Saved: " + t);

        // 2) List all tasks
        List<Task> all = dao.getAll();
        System.out.println("\nAll tasks (" + all.size() + "):");
        all.forEach(System.out::println);

        // 3) Find by column
        List<Task> todo = dao.getByColumn(col);
        System.out.println("\nIn 'To Do' column (" + todo.size() + "):");
        todo.forEach(System.out::println);

        // 4) Find by ID
        Task found = dao.getById(t.getId());
        if (found != null) {
            System.out.println("\nFound by ID: " + found);
        } else {
            System.out.println("\nTask not found!");
        }


        System.out.println("\n=== End of test ===");
    }
}