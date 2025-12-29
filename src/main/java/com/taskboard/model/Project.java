package com.taskboard.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import java.util.List;
import org.bson.types.ObjectId;

@Entity("projects")
public class Project {
    @Id
    private ObjectId id;
    @Reference
    private User owner;
    private String nom;
    @Reference
    private java.util.List<User> sharedUsers = new java.util.ArrayList<>();

    public Project() {}
    public Project(User owner, String nom) {
        this.owner = owner;
        this.nom = nom;
    }
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public java.util.List<User> getSharedUsers() { return sharedUsers; }
    public void setSharedUsers(java.util.List<User> sharedUsers) { this.sharedUsers = sharedUsers; }
    public void addSharedUser(User user) { if (!sharedUsers.contains(user)) sharedUsers.add(user); }
    public void removeSharedUser(User user) { sharedUsers.remove(user); }
    public boolean isSharedWith(User user) { return sharedUsers.contains(user); }
    @Override
    public String toString() { return nom; }
}

