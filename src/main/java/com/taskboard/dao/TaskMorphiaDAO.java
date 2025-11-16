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
        datastore.save(task);          // upsert
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

    /** Delete one task */
    public void delete(Task task) {
        datastore.delete(task);
    }
}