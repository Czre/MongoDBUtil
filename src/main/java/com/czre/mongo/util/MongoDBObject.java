package com.czre.mongo.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by czre on 2018/2/6
 */
public class MongoDBObject {

    public static final String HOST = PropertyConstants.getPropertiesKey("host");
    public static final int PORT = Integer.parseInt(PropertyConstants.getPropertiesKey("port"));
    public static final String USERNAME = PropertyConstants.getPropertiesKey("username");
    public static final String PASSWORD = PropertyConstants.getPropertiesKey("password");
    public static final String LOGINDATABASE = PropertyConstants.getPropertiesKey("loginDataBase");
    public static final String USEDATABASE = PropertyConstants.getPropertiesKey("useDatabase");

    private static MongoClient mongoClient;
    private static MongoDatabase mongoDB;

    static {
        init();
    }

    private static void init() {
        System.out.println(HOST);
        ServerAddress serverAddress = new ServerAddress(HOST, PORT);
        List<ServerAddress> addresses = new ArrayList<ServerAddress>();
        addresses.add(serverAddress);
        MongoCredential credential = MongoCredential.createScramSha1Credential(USERNAME, LOGINDATABASE, PASSWORD.toCharArray());
        List<MongoCredential> credentials = new ArrayList<MongoCredential>();
        credentials.add(credential);
        mongoClient = new MongoClient(addresses, credentials);
        mongoDB = mongoClient.getDatabase(USEDATABASE);
    }

    public static MongoDatabase getMongoDB() {
        return mongoDB;
    }
}
