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
        Task t = new Task("Learn Morphia",
                          "Understand how to save & query",
                          3,
                          LocalDate.now().plusDays(2),
                          "To Do",
                          List.of("study", "mongo"));
        dao.save(t);
        System.out.println("Saved: " + t);

        // 2) List all tasks
        List<Task> all = dao.findAll();
        System.out.println("\nAll tasks (" + all.size() + "):");
        all.forEach(System.out::println);

        // 3) Find by column
        List<Task> todo = dao.findByColumn("To Do");
        System.out.println("\nIn 'To Do' column (" + todo.size() + "):");
        todo.forEach(System.out::println);

        // 4) Find by ID
        Optional<Task> found = dao.findById(t.getId());
        found.ifPresentOrElse(
                ft -> System.out.println("\nFound by ID: " + ft),
                () -> System.out.println("\nTask not found!")
        );

        System.out.println("\n=== End of test ===");
    }
}