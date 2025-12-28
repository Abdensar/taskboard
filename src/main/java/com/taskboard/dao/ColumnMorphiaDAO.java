package com.taskboard.dao;

import com.taskboard.model.Column;
import com.taskboard.model.Project;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import java.util.List;

public class ColumnMorphiaDAO {
    private final Datastore datastore;

    public ColumnMorphiaDAO(Datastore datastore) {
        this.datastore = datastore;
    }
    public ColumnMorphiaDAO() {
        this.datastore = com.taskboard.config.MorphiaConfig.getDatastore();
    }

    public void create(Column column) {
        datastore.save(column);
    }

    public Column getById(ObjectId id) {
        return datastore.find(Column.class).filter("_id", id).first();
    }

    public List<Column> getAll() {
        return datastore.find(Column.class).iterator().toList();
    }

    public List<Column> getByProject(Project project) {
        return datastore.find(Column.class).filter("projet", project).iterator().toList();
    }

    public void update(Column column) {
        datastore.save(column);
    }

    public void delete(ObjectId id) {
        Query<Column> query = datastore.find(Column.class).filter("_id", id);
        query.delete();
    }
}
