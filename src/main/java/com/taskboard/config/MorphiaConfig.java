package com.taskboard.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

import io.github.cdimascio.dotenv.Dotenv;

public final class MorphiaConfig {

    private static final String DB_NAME = "taskboard";
    private static Datastore datastore;   // single instance
    private static Dotenv dotenv = Dotenv.load();

    /** Return the same Datastore to everybody */
    public static Datastore getDatastore() {
        if (datastore == null) {
            // 1) open MongoDB connection
            MongoClient mongoClient = MongoClients.create(
                    dotenv.get("MONGO_URI", "mongodb://localhost:27017")   // fallback for local dev
            );
            // 2) create Morphia wrapper
            datastore = Morphia.createDatastore(mongoClient, DB_NAME);
            // 3) scan model package for @Entity classes
            datastore.getMapper().mapPackage("com.taskboard.model");
            // 4) create indexes once
            datastore.ensureIndexes();
        }
        return datastore;
    }

    private MorphiaConfig() { } // utility class – no instances
}