package com.taskboard.dao;

import com.taskboard.model.Task;
import com.taskboard.model.Column;
import com.taskboard.model.Label;
import com.taskboard.model.Project;
import dev.morphia.Datastore;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;
import com.taskboard.model.User;

public class TaskMorphiaDAOTest {
    public static void main(String[] args) {
        TaskMorphiaDAO dao = new TaskMorphiaDAO();
        System.out.println("=== Morphia DAO Test ===");

        // Create a dummy column and label for the task
        Column col = new Column();
        col.setNom("To Do");
        User user = new User("Test User", "test@example.com", "password");
        Project project = new Project(user, "Test Project");
        col.setProjet(project);
        col.setOrdre(1);

        Label label = new Label("Urgent");

        // Create a task
        Task t = new Task(col, "Test Task", "Test description", 3, new Date(System.currentTimeMillis()+86400000), List.of(label));
        dao.create(t);
        System.out.println("Created: " + t);

        // List all tasks
        List<Task> all = dao.getAll();
        System.out.println("All tasks: " + all);

        // Find by column
        List<Task> inCol = dao.getByColumn(col);
        System.out.println("Tasks in column 'To Do': " + inCol);

        // Find by label
        List<Task> withLabel = dao.getByLabel(label);
        System.out.println("Tasks with label 'Urgent': " + withLabel);

        // Update
        t.setDescription("Updated description");
        dao.update(t);
        System.out.println("Updated: " + dao.getById(t.getId()));

        // Delete
        dao.delete(t.getId());
        System.out.println("Deleted. All tasks now: " + dao.getAll());
    }
}
