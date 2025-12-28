package com.taskboard.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;

@Entity("labels")
public class Label {
    @Reference
    private Project project;
    @Id
    private ObjectId id;
    private String nom;
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Label() {}
    public Label(String nom) {
        this.nom = nom;
    }
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
