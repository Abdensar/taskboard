package com.taskboard.dao;

import com.taskboard.config.MorphiaConfig;
import com.taskboard.model.Task;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import java.util.List;
import java.util.Optional;

public class TaskMorphiaDAO {

    private final Datastore datastore = MorphiaConfig.getDatastore();

    /* ---------- CRUD ---------- */

    /** Insert or update (if id != null) */
    public void save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            throw new IllegalArgumentException("Task must have a valid id");
        }
        System.out.println("[DEBUG] DAO.save called with: " + task);
        System.out.println("[DEBUG] Task id: " + task.getId());
        datastore.save(task);          // upsert
        System.out.println("[DEBUG] DAO.save completed.");
    }

    /** Delete one task */
    public void delete(Task task) {
        if (task.getId() != null) {
            datastore.find(Task.class).filter(Filters.eq("_id", task.getId())).delete();
        }
    }

    /** Find by primary key */
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(
                datastore.find(Task.class)
                         .filter(Filters.eq("_id", id))
                         .first()
        );
    }

    /** Get every task */
    public List<Task> findAll() {
        return datastore.find(Task.class)
                        .stream()
                        .toList();
    }

    /** Get tasks in one column */
    public List<Task> findByColumn(String column) {
        return datastore.find(Task.class)
                        .filter(Filters.eq("column", column))
                        .stream()
                        .toList();
    }
}