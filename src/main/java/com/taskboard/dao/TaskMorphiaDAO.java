package com.taskboard.dao;

import com.taskboard.model.Task;
import com.taskboard.model.Column;
import com.taskboard.model.Label;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import java.util.List;

public class TaskMorphiaDAO {
    private final Datastore datastore;

    // Allow both DI and default config usage
    public TaskMorphiaDAO(Datastore datastore) {
        this.datastore = datastore;
    }
    public TaskMorphiaDAO() {
        this.datastore = com.taskboard.config.MorphiaConfig.getDatastore();
    }

    public void create(Task task) {
        datastore.save(task);
    }

    public Task getById(ObjectId id) {
        return datastore.find(Task.class).filter("_id", id).first();
    }

    public List<Task> getAll() {
        return datastore.find(Task.class).iterator().toList();
    }

    public List<Task> getByColumn(Column colonne) {
        return datastore.find(Task.class).filter("colonne", colonne).iterator().toList();
    }

    public List<Task> getByLabel(Label etiquette) {
        return datastore.find(Task.class).filter("etiquettes", etiquette).iterator().toList();
    }

    public void update(Task task) {
        datastore.save(task);
    }

    public void delete(ObjectId id) {
        Query<Task> query = datastore.find(Task.class).filter("_id", id);
        query.delete();
    }
}