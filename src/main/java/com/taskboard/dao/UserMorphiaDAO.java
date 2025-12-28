package com.taskboard.dao;

import com.taskboard.model.User;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import java.util.List;

public class UserMorphiaDAO {
    private final Datastore datastore;

    public UserMorphiaDAO(Datastore datastore) {
        this.datastore = datastore;
    }
    public UserMorphiaDAO() {
        this.datastore = com.taskboard.config.MorphiaConfig.getDatastore();
    }

    public void create(User user) {
        if (getByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        datastore.save(user);
    }

    public User getById(ObjectId id) {
        return datastore.find(User.class).filter("_id", id).first();
    }

    public List<User> getAll() {
        return datastore.find(User.class).iterator().toList();
    }

    public User getByEmail(String email) {
        return datastore.find(User.class).filter("email", email).first();
    }

    public void update(User user) {
        datastore.save(user);
    }

    public void delete(ObjectId id) {
        Query<User> query = datastore.find(User.class).filter("_id", id);
        query.delete();
    }
}
