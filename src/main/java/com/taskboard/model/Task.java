package com.taskboard.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.List;

@Entity("tasks")
public class Task {
    @Id
    private ObjectId id;

    @Reference
    private Column colonne;

    private String titre;
    private String description;
    private int priorite; // 1-5
    private Date echeance;

    @Reference
    private List<Label> etiquettes;

    public Task() {}

    public Task(Column colonne, String titre, String description, int priorite, Date echeance, List<Label> etiquettes) {
        this.colonne = colonne;
        this.titre = titre;
        this.description = description;
        this.priorite = priorite;
        this.echeance = echeance;
        this.etiquettes = etiquettes;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public Column getColonne() { return colonne; }
    public void setColonne(Column colonne) { this.colonne = colonne; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPriorite() { return priorite; }
    public void setPriorite(int priorite) {
        if (priorite < 1 || priorite > 5) {
            throw new IllegalArgumentException("Priorité doit être entre 1 et 5.");
        }
        this.priorite = priorite;
    }
    public Date getEcheance() { return echeance; }
    public void setEcheance(Date echeance) {
        Date now = new Date();
        if (echeance != null && echeance.before(now)) {
            throw new IllegalArgumentException("L'échéance doit être après la date de création.");
        }
        this.echeance = echeance;
    }
    public List<Label> getEtiquettes() { return etiquettes; }
    public void setEtiquettes(List<Label> etiquettes) { this.etiquettes = etiquettes; }

    @Override
    public String toString() {
        return titre + " (" + (colonne != null ? colonne.getNom() : "") + ")";
    }
}