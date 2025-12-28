package com.taskboard.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

@Entity("columns")
public class Column {
    @Id
    private ObjectId id;
    @Reference(lazy = false)
    private Project projet;
    private String nom;
    private int ordre;

    public Column() {}
    public Column(Project projet, String nom, int ordre) {
        this.projet = projet;
        this.nom = nom;
        this.ordre = ordre;
    }
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public Project getProjet() { return projet; }
    public void setProjet(Project projet) { this.projet = projet; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
}
