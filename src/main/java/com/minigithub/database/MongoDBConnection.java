package com.minigithub.database;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017"; // your connection string
    private static MongoClient mongoClient = null;
    //to store and retrieve data in the MongoDB Java driver using plain old Java objects POJOs
    //We will 1st need to Configure the driver to serialize and deserialize POJOs
    CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

    public MongoDatabase getDatabase(String databaseName) {
        try {
            if (mongoClient == null) {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                System.out.println("Connected to MongoDB!");
            }
            return mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect to MongoDB");
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }
    public void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}
