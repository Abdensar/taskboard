package com.taskboard.dao;

import com.taskboard.model.Project;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import java.util.List;

public class ProjectMorphiaDAO {
    private final Datastore datastore;

    public ProjectMorphiaDAO(Datastore datastore) {
        this.datastore = datastore;
    }
    public ProjectMorphiaDAO() {
        this.datastore = com.taskboard.config.MorphiaConfig.getDatastore();
    }

    public void create(Project project) {
        datastore.save(project);
    }

    public Project getById(ObjectId id) {
        return datastore.find(Project.class).filter("_id", id).first();
    }

    public List<Project> getByOwner(com.taskboard.model.User owner) {
        return datastore.find(Project.class).filter("owner", owner).iterator().toList();
    }
    public List<Project> getBySharedUser(com.taskboard.model.User user) {
        return datastore.find(Project.class).filter("sharedUsers", user).iterator().toList();
    }

    public void update(Project project) {
        datastore.save(project);
    }

    public void delete(ObjectId id) {
        Query<Project> query = datastore.find(Project.class).filter("_id", id);
        query.delete();
    }
}
