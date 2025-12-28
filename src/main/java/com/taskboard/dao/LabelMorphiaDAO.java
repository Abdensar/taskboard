package com.taskboard.dao;

import com.taskboard.model.Label;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import java.util.List;

public class LabelMorphiaDAO {
    private final Datastore datastore;

    public LabelMorphiaDAO(Datastore datastore) {
        this.datastore = datastore;
    }
    public LabelMorphiaDAO() {
        this.datastore = com.taskboard.config.MorphiaConfig.getDatastore();
    }

    public void create(Label label) {
        datastore.save(label);
    }

    public Label getById(ObjectId id) {
        return datastore.find(Label.class).filter("_id", id).first();
    }

    public List<Label> getAll() {
        return datastore.find(Label.class).iterator().toList();
    }
    public List<Label> getAllByProject(com.taskboard.model.Project project) {
        return datastore.find(Label.class).filter("project", project).iterator().toList();
    }
    public Label getByNameAndProject(String nom, com.taskboard.model.Project project) {
        return datastore.find(Label.class)
            .filter("nom", nom)
            .filter("project", project)
            .first();
    }

    public void update(Label label) {
        datastore.save(label);
    }

    public void delete(ObjectId id) {
        Query<Label> query = datastore.find(Label.class).filter("_id", id);
        query.delete();
    }
}
