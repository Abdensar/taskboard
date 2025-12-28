package com.taskboard.util;

import com.taskboard.model.Task;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TaskExportUtil {
    public static void exportTasksToCSV(List<Task> tasks, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Titre,Description,Priorité,Échéance,Colonne,Étiquettes\n");
            for (Task t : tasks) {
                writer.write(String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"\n",
                        t.getTitre(),
                        t.getDescription(),
                        t.getPriorite(),
                        t.getEcheance() != null ? t.getEcheance().toString() : "",
                        t.getColonne() != null ? t.getColonne().getNom() : "",
                        t.getEtiquettes() != null ? t.getEtiquettes().stream().map(l -> l.getNom()).reduce((a, b) -> a + ", " + b).orElse("") : ""
                ));
            }
        }
    }
}
