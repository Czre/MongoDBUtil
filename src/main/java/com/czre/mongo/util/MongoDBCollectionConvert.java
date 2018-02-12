package com.czre.mongo.util;

import com.czre.mongo.pojo.User;

/**
 * Created by czre on 2018/2/11
 */
public class MongoDBCollectionConvert {
    public static Class<?> convert(String CollectionName) {
        Class<?> classType = null;
        switch (CollectionName) {
            case "user":
                classType = User.class;
                break;
        }
        return classType;
    }
}
