package com.taskboard.app;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class PingAtlas {
    public static void main(String[] args) {
        String uri = System.getenv("MONGO_URI");
        try (MongoClient client = MongoClients.create(uri)) {
            MongoDatabase db  = client.getDatabase("admin");
            db.runCommand(new org.bson.Document("ping", 1));
            System.out.println("✅ Atlas reachable !");
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
}