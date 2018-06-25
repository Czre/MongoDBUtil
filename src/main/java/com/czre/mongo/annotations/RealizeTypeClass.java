package com.czre.mongo.annotations;

import java.util.*;

public class RealizeTypeClass {

    private String listPath;
    private String setPath;
    private String mapPath;

    public static RealizeTypeClass newRealizeTypeClass() {
        RealizeTypeClass realizeTypeClass = new RealizeTypeClass();

        realizeTypeClass.listPath = RealizeType.List.ArrayList.getPath();
        realizeTypeClass.setPath = RealizeType.Set.HashSet.getPath();
        realizeTypeClass.mapPath = RealizeType.Map.HashMap.getPath();

        return realizeTypeClass;
    }


    public static RealizeTypeClass newRealizeTypeClass(MongoRealize annotation) {
        RealizeTypeClass realizeTypeClass = new RealizeTypeClass();
        realizeTypeClass.listPath = annotation.List().getPath();
        realizeTypeClass.setPath = annotation.Set().getPath();
        realizeTypeClass.mapPath = annotation.Map().getPath();
        return realizeTypeClass;
    }

    public Set newSet() {

        Set set = null;
        try {
            set = (Set) Class.forName(setPath).newInstance();
        } catch (Exception e) {
            set = new HashSet();
            e.printStackTrace();
        }

        return set;
    }

    public Map newMap() {

        Map map = null;
        try {
            map = (Map) Class.forName(mapPath).newInstance();
        } catch (Exception e) {
            map = new HashMap();
            e.printStackTrace();
        }

        return map;
    }


    public List newList() {

        List list = null;
        try {
            list = (List) Class.forName(listPath).newInstance();
        } catch (Exception e) {
            list = new ArrayList();
            e.printStackTrace();
        }

        return list;
    }
}
