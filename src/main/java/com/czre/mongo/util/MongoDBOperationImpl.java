package com.czre.mongo.util;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by czre on 2018/2/7
 */
public class MongoDBOperationImpl implements MongoDBOperation {

    private static MongoDatabase mongoDatabase = MongoDBObject.getMongoDB();
    private String collectionName;

    private BasicDBObject query;
    private List<FuzzyQuery> fuzzyQueryList;

    private BasicDBObject select;
    private BasicDBObject sort;

    private BasicDBObject update;
    private BasicDBObject set;

    private BasicDBObject inc;
    private BasicDBObject unset;
    private BasicDBObject push;
    private BasicDBObject pull;

    public MongoDBOperationImpl(String collectionName) {
        this.collectionName = collectionName;
    }

    public MongoCollection<Document> collection() {
        return mongoDatabase.getCollection(collectionName);
    }

    public void destroy() {

        collectionName = null;

        query = null;
        select = null;
        update = null;

        fuzzyQueryList = null;

        set = null;
        inc = null;
        unset = null;
        push = null;
        pull = null;
    }

    public void query(String key, Object value) {
        if (query == null) {
            query = new BasicDBObject();
        }
        query.append(key, value);
    }

    public FuzzyQuery query(String key) {
        if (fuzzyQueryList == null) {
            fuzzyQueryList = new ArrayList<FuzzyQuery>();
        }
        FuzzyQuery fuzzyQuery = new FuzzyQuery(key);
        fuzzyQueryList.add(fuzzyQuery);
        return fuzzyQuery;
    }

    public FuzzyQuery query() {
        if (fuzzyQueryList == null) {
            fuzzyQueryList = new ArrayList<FuzzyQuery>();
        }
        FuzzyQuery fuzzyQuery = new FuzzyQuery();
        fuzzyQueryList.add(fuzzyQuery);
        return fuzzyQuery;
    }

    public void jointFuzzy() {
        if (query == null) {
            query = new BasicDBObject();
        }
        if (fuzzyQueryList != null) {
            for (FuzzyQuery fuzzyQuery : fuzzyQueryList) {
                if (fuzzyQuery.getQuery() != null) {
                    String key = fuzzyQuery.getQuery();
                    BasicDBObject conditions = fuzzyQuery.getCondition();
                    // 目前是循环,之后可改为数组
                    query.append(key, conditions);
                } else if (fuzzyQuery.getOr() != null) {
                    List<BasicDBObject> or = fuzzyQuery.getOr();
                    for (BasicDBObject value : or) {
                        if (value.get("_id") != null) {
                            value.put("_id", new ObjectId(value.get("_id").toString()));
                        }
                    }
                    query.append("or", or);
                }
            }
        }
        if (query.get("_id") != null) {
            query.put("_id", new ObjectId(query.get("_id").toString()));
        }
        System.out.println(query);
    }

    public void select(String key, int value) {
        if (select == null) {
            select = new BasicDBObject();
        }
        select.append(key, value);
    }

    public void sort(String key, int value) {
        if (sort == null) {
            sort = new BasicDBObject();
        }
        sort.append(key, value);
    }

    public void updateInit() {
        if (update == null) {
            update = new BasicDBObject();
        }
    }

    public void set(String key, int value) {
        updateInit();
        if (set == null) {
            set = new BasicDBObject();
            update.append("$set", set);
        }
        set.append(key, value);
    }

    public void push(String key, int value) {
        updateInit();
        if (push == null) {
            push = new BasicDBObject();
            update.append("$push", push);
        }
        push.append(key, value);
    }

    public void pull(String key, int value) {
        updateInit();
        if (pull == null) {
            pull = new BasicDBObject();
            update.append("$pull", pull);
        }
        pull.append(key, value);
    }

    public void unset(String key) {
        updateInit();
        if (unset == null) {
            unset = new BasicDBObject();
            update.append("$unset", unset);
        }
        unset.append(key, 1);
    }

    public void inc(String key, int value) {
        updateInit();
        if (inc == null) {
            inc = new BasicDBObject();
            update.append("$inc", inc);
        }
        inc.append(key, value);
    }

    public boolean insertOne(Object obj) {
        try {
            collection().insertOne(MongoDBUtil.beanToDoc(obj));
            destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOne() {
        try {
            // 拼接模糊查询
            jointFuzzy();
            collection().deleteOne(query);

            destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMany() {
        try {
            // 拼接模糊查询
            jointFuzzy();
            collection().deleteMany(query);

            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOne() {
        try {
            // 拼接模糊查询
            jointFuzzy();
            collection().updateOne(query, update);
            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateMany() {
        try {
            // 拼接模糊查询
            jointFuzzy();
            collection().updateMany(query, update);

            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Object selectOneBackBean() throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException {
        // 拼接模糊查询
        jointFuzzy();

        Document doc = collection().find(query).projection(select).first();

        Object result = null;
        if (doc != null) {
            result = MongoDBUtil.docToBean(doc, collectionName);
        }
        // 调用后销毁
        destroy();
        return result;
    }

    public List selectManyBackBean() throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException {

        // 拼接模糊查询
        jointFuzzy();

        MongoCursor<Document> cursor = collection().find(query).projection(select).sort(sort).iterator();
        List<Object> result = new ArrayList<Object>();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            result.add(MongoDBUtil.docToBean(doc, collectionName));
        }

        // 调用后销毁
        destroy();
        return result;
    }

    public MongoDBPage selectPageReturnBean(MongoDBPage page) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException {
        // 拼接模糊查询
        jointFuzzy();

        page.setCount((int) collection().count(query));
        MongoCursor<Document> cursor = collection().find(query).projection(select).skip((page.getNumber() - 1) * page.getSize()).limit(page.getSize()).sort(sort).iterator();
        ArrayList<Object> result = new ArrayList<Object>();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            result.add(MongoDBUtil.docToBean(doc, collectionName));
        }

        page.setResult(result);
        // 调用后销毁
        destroy();

        return page;
    }

    public Object selectMostBackBean() throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException {
// 拼接模糊查询
        jointFuzzy();

        MongoCursor<Document> cursor = collection().find(query).projection(select).projection(sort).limit(1).iterator();

        Object result = null;

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            if (result != null) {
                result = MongoDBUtil.docToBean(doc, collectionName);
            }
        }

        // 调用后销毁
        destroy();
        return result;
    }

    public Document selectOneBackDoc() {
        // 拼接模糊查询
        jointFuzzy();

        Document result = collection().find(query).projection(select).first();

        if (result != null) {
            if (result.get("_id") != null) {
                result.append("_id", result.get("_id").toString());
            }
        }

        // 调用后销毁
        destroy();
        return result;
    }

    public List<Document> selectManyBackDoc() {

        // 拼接模糊查询
        jointFuzzy();

        MongoCursor<Document> cursor = collection().find(query).projection(select).sort(sort).iterator();
        List<Document> result = new ArrayList<Document>();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            if (doc.get("_id") != null) {
                doc.append("_id", doc.get("_id").toString());
            }
            result.add(doc);
        }

        // 调用后销毁
        destroy();
        return result;
    }

    public Map<String, Object> selectPageBackDoc(MongoDBPage page) {
        // 拼接模糊查询
        jointFuzzy();

        page.setCount((int) collection().count(query));
        MongoCursor<Document> cursor = collection().find(query).projection(select).skip((page.getNumber() - 1) * page.getSize()).limit(page.getSize()).sort(sort).iterator();
        List<Document> result = new ArrayList<Document>();

        while (cursor.hasNext()) {
            Document value = cursor.next();
            if (value.get("_id") != null) {
                value.append("_id", value.get("_id").toString());
            }
            result.add(value);
        }

        Map<String, Object> PageEntity = new HashMap<String, Object>();
        PageEntity.put("size", page.getSize());
        PageEntity.put("number", page.getNumber());
        PageEntity.put("total", page.getTotal());
        PageEntity.put("count", page.getCount());
        PageEntity.put("result", result);

        // 调用后销毁
        destroy();

        return PageEntity;
    }

    public Document selectMostBackDoc() {
        // 拼接模糊查询
        jointFuzzy();

        MongoCursor<Document> cursor = collection().find(query).projection(select).projection(sort).limit(1).iterator();

        Document result = null;

        while (cursor.hasNext()) {
            result = cursor.next();

            if (result.get("_id") != null) {
                result.put("_id", result.get("_id").toString());
            }
        }

        // 调用后销毁
        destroy();
        return result;
    }

    public class FuzzyQuery {
        private String query;
        private BasicDBObject condition; // 条件
        private List<BasicDBObject> or;

        public FuzzyQuery(String query) {
            condition = new BasicDBObject();
            this.query = query;
        }

        public FuzzyQuery() {
            or = new ArrayList<BasicDBObject>();
        }

        public String getQuery() {
            return query;
        }

        public BasicDBObject getCondition() {
            return condition;
        }

        public List<BasicDBObject> getOr() {
            return or;
        }

        public FuzzyQuery or(String key, Object value) {
            or.add(new BasicDBObject(key, value));
            return this;
        }

        public FuzzyQuery fuzzy(String key, String value) {
            or.add(new BasicDBObject(key, pattern(value)));
            return this;
        }

        public FuzzyQuery gt(long value) {
            condition.append("$gt", value);
            return this;
        }

        public FuzzyQuery gte(long value) {
            condition.append("$gte", value);
            return this;
        }

        public FuzzyQuery lt(long value) {
            condition.append("$lt", value);
            return this;
        }

        public FuzzyQuery lte(long value) {
            condition.append("$lte", value);
            return this;
        }

        public FuzzyQuery gt(int value) {
            condition.append("$gt", value);
            return this;
        }

        public FuzzyQuery gte(int value) {
            condition.append("$gte", value);
            return this;
        }

        public FuzzyQuery lt(int value) {
            condition.append("$lt", value);
            return this;
        }

        public FuzzyQuery lte(int value) {
            condition.append("$lte", value);
            return this;
        }

        // 使用正则进行匹配
        public Pattern pattern(String value) {
            return Pattern.compile("^.*" + query + ".*$", Pattern.CASE_INSENSITIVE);
        }
    }
}
