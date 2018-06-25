package com.czre.mongo.util;


import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoDBObject {
    private static MongoClient mongoClient;
    private static MongoDatabase db;

    static {
        init();
    }

    private static void init() {
        Map<String, String> getenv = System.getenv();
        String ipAddress = getenv.get("MONGO_HOST");
        int portNumber = Integer.parseInt(getenv.get("MONGO_PORT"));
        String userName = getenv.get("MONGO_USERNAME");
        String passWord = getenv.get("MONGO_PASSWORD");
        String logindataBase = getenv.get("MONGO_LOGIN_DATABASE");
        String usedataBase = getenv.get("MONGO_USE_DATABASE");
        if (passWord.trim().equals("")) {
            mongoClient = new MongoClient(ipAddress, portNumber);
            db = mongoClient.getDatabase(usedataBase);
            return;
        }
        try {

            ServerAddress serverAddress = new ServerAddress(ipAddress, portNumber);
            List<ServerAddress> addrs = new ArrayList<ServerAddress>();
            addrs.add(serverAddress);

            MongoCredential credential = MongoCredential.createScramSha1Credential(userName, logindataBase, passWord.toCharArray());

            List<MongoCredential> credentials = new ArrayList<MongoCredential>();
            credentials.add(credential);
            mongoClient = new MongoClient(addrs, credentials);
            db = mongoClient.getDatabase(usedataBase);
        } catch (Exception e) {
        }
    }

    public static MongoDatabase getDB() {

        System.out.println("db=" + db);
        return db;
    }
}