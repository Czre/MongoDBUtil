package com.czre.mongo.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by czre on 2018/2/5
 */
public class PropertyConstants {
    private static Properties properties;

    private static void setProperty(){
        if (properties==null) {
            properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                properties.load(loader.getResourceAsStream("mongoDB.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getPropertiesKey(String key){
        if (properties==null) {
            setProperty();
        }
        return properties.getProperty(key, "default");
    }
}
