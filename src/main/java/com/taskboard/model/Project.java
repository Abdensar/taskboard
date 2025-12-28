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
    @Override
    public String toString() { return nom; }
}

