package com.czre.mongo.util;


import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.regex.Pattern;

public class MongoDBOperationImpl implements MongoDBOperation {

    /************************************************** 非接口定义的方法 **************************************************/
    private static MongoDatabase db = MongoDBObject.getDB();
    private String CollectionName;
    /************************************************** 文档定位属性 **************************************************/
    private BasicDBObject query;// 查询
    private List<FuzzyQuery> FuzzyQueryList;
    /************************************************** 挑选与排序属性和接口 **************************************************/

    private BasicDBObject select;// 挑选
    private BasicDBObject sort;// 排序
    /************************************************** 更新条件属性 **************************************************/
    private BasicDBObject update;// 修改
    private BasicDBObject set;
    private BasicDBObject inc;
    private BasicDBObject unset;
    private BasicDBObject push;
    private BasicDBObject pull;

    /************************************************** 构造方法 **************************************************/
    public MongoDBOperationImpl(String CollectionName) {
        this.CollectionName = CollectionName;
    }

    /************************************************** 工具方法-util **************************************************/


    public static <T> Object disposeResult(Class<T> sourceClass, Document source) throws Exception {

        return MongoUtil.transformJavaBean(sourceClass, source, source);
    }

    public MongoCollection<Document> collection() {
        return db.getCollection(CollectionName);
    }

    public void destroy() {
        CollectionName = null;

        // 基础操作
        query = null;
        select = null;
        sort = null;
        update = null;

        // 模糊查询
        FuzzyQueryList = null;

        // 更新操作
        set = null;
        inc = null;
        unset = null;
        push = null;
        pull = null;
    }

    public void query(String key, Object val) {
        if (query == null) {
            query = new BasicDBObject();
        }
        query.append(key, val);
    }

    public FuzzyQuery query(String key) {
        if (FuzzyQueryList == null) {
            FuzzyQueryList = new ArrayList<FuzzyQuery>();
        }
        FuzzyQuery FuzzyQuery = new FuzzyQuery(key);
        FuzzyQueryList.add(FuzzyQuery);
        return FuzzyQuery;
    }

    public FuzzyQuery query() {
        if (FuzzyQueryList == null) {
            FuzzyQueryList = new ArrayList<FuzzyQuery>();
        }
        FuzzyQuery FuzzyQuery = new FuzzyQuery();
        FuzzyQueryList.add(FuzzyQuery);
        return FuzzyQuery;
    }

    public void jointFuzzy() {
        if (query == null) {
            query = new BasicDBObject();
        }
        if (FuzzyQueryList != null) {
            for (FuzzyQuery fuzzyQuery : FuzzyQueryList) {
                if (fuzzyQuery.getQuery() != null) {
                    String key = fuzzyQuery.getQuery();
                    BasicDBObject conditions = fuzzyQuery.getCondition();
                    // 目前是循环，之后可改为数组
                    query.append(key, conditions);
                } else if (fuzzyQuery.getOr() != null) {
                    List<BasicDBObject> or = fuzzyQuery.getOr();
                    for (BasicDBObject value : or) {
                        if (value.get("_id") != null) {
                            value.put("_id", new ObjectId(value.get("_id").toString()));
                        }
                    }
                    query.append("$or", or);
                }
            }

        }
    }

    public void select(String key, int val) {
        if (select == null) {
            select = new BasicDBObject();
        }
        select.append(key, val);
    }

    public void sort(String key, int val) {
        if (sort == null) {
            sort = new BasicDBObject();
        }
        sort.append(key, val);
    }

    public void updateInit() {
        if (update == null) {
            update = new BasicDBObject();
        }
    }

    public void set(String key, Object val) {
        updateInit();
        if (set == null) {
            set = new BasicDBObject();
            update.append("$set", set);
        }
        set.append(key, val);
    }

    public void push(String key, Object val) {

        updateInit();

        if (push == null) {
            push = new BasicDBObject();
            update.append("$push", push);
        }
        push.append(key, object2Document(val));
    }

    public void push(String key, String val) {

        updateInit();

        if (push == null) {
            push = new BasicDBObject();
            update.append("$push", push);
        }
        push.append(key, val);
    }

    public void pull(String key, Object val) {
        updateInit();

        if (pull == null) {
            pull = new BasicDBObject();
            update.append("$pull", pull);
        }
        pull.append(key, val);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.doubleseven.element.mongodb.MongoDbOperation#unset(java.lang.String)
     */
    public void unset(String key) {
        updateInit();

        if (unset == null) {
            unset = new BasicDBObject();
            update.append("$unset", unset);
        }

        unset.append(key, 1);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.doubleseven.element.mongodb.MongoDbOperation#inc(java.lang.String,
     * java.lang.Object)
     */
    public void inc(String key, Object val) {
        updateInit();

        if (inc == null) {
            inc = new BasicDBObject();
            update.append("$inc", inc);
        }
        inc.append(key, val);
    }

    /************************************************** 持久化接口 **************************************************/

    /*
     * (non-Javadoc)
     *
     * @see
     * com.doubleseven.element.mongodb.MongoDbOperation#insertOne(java.lang.
     * Object)
     */
    public boolean insertOne(Object obj) {
        try {
            Document doc = MongoDBUtil.BeanToDoc(obj);

            collection().insertOne(doc);
            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.doubleseven.element.mongodb.MongoDbOperation#deleteOne()
     */

    public boolean insertOne(Document doc) {
        try {
            collection().insertOne(doc);
            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertMany(List docs) {
        try {
            collection().insertMany(docs);
            // 调用后销毁
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

            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.doubleseven.element.mongodb.MongoDbOperation#deleteMany()
     */
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


    /*
     * (non-Javadoc)
     *
     * @see com.doubleseven.element.mongodb.MongoDbOperation#updateOne()
     */
    public boolean updateOne() {
        try {
            // 拼接模糊查询


            jointFuzzy();

            collection().updateOne(query, update);
            // 调用后销毁
            destroy();
            return true;
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.doubleseven.element.mongodb.MongoDbOperation#updateMany()
     */
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

    public Object selectOneBackBean(Class T) {
        // 拼接模糊查询
        jointFuzzy();
        Object result = null;
        try {
            Document doc = collection().find(query).projection(select).first();


            if (doc != null) {
                result = disposeResult(T, doc);
            }
            // 调用后销毁
            destroy();

        } catch (Exception e) {

        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.doubleseven.element.mongodb.MongoDbOperation#selectManyBackBean()
     */
    @SuppressWarnings("rawtypes")
    public List selectManyBackBean(Class T) {
        List<Object> result = new ArrayList<Object>();

        try {
            // 拼接模糊查询
            jointFuzzy();
            MongoCursor<Document> cursor = collection().find(query).projection(select).sort(sort).iterator();

            while (cursor.hasNext()) {


                Document doc = cursor.next();
                result.add(disposeResult(T, doc));
            }

            // 调用后销毁
            destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List selectPageBackBean(Class T, MongoDBPage page) throws Exception {
        // 拼接模糊查询
        jointFuzzy();

        page.setRowCount((int) collection().count(query));


        MongoCursor<Document> cursor = collection().find(query).projection(select).skip((page.getCurrentPage() - 1) * page.getPageSize()).limit(page.getPageSize()).sort(sort).iterator();
        ArrayList<Object> result = new ArrayList<Object>();

        while (cursor.hasNext()) {
            Document doc = cursor.next();

            result.add(disposeResult(T, doc));
        }

        // 调用后销毁
        destroy();

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.doubleseven.element.mongodb.MongoDbOperation#selectMostBackBean()
     */
    public Object selectMostBackBean(Class T) throws Exception {
        // 拼接模糊查询
        jointFuzzy();

        MongoCursor<Document> cursor = collection().find(query).projection(select).sort(sort).limit(1).iterator();

        Object result = null;

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            if (doc != null) {
                result = disposeResult(T, doc);
            }
        }

        // 调用后销毁
        destroy();
        return result;
    }

    public Document selectOneBackDoc() throws Exception {
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

    /*
     * (non-Javadoc)
     *
     * @see com.doubleseven.element.mongodb.MongoDbOperation#selectMany()
     */
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

    /************************************************** 查询接口-other（文档定位/挑选/排序） **************************************************/

    /*
     * (non-Javadoc)
     *
     * @see
     * com.doubleseven.element.mongodb.MongoDbOperation#selectPage(com.doubleseven
     * .element.mongodb.MongoDbPagePojo)
     */
    public Map<String, Object> selectPageBackDoc(MongoDBPage page) throws Exception {
        // 拼接模糊查询
        jointFuzzy();

        page.setRowCount((int) collection().count(query));
        MongoCursor<Document> cursor = collection().find(query).projection(select).skip((page.getCurrentPage() - 1) * page.getPageSize()).limit(page.getPageSize()).sort(sort).iterator();
        List<Document> result = new ArrayList<Document>();

        while (cursor.hasNext()) {
            Document value = cursor.next();
            if (value.get("_id") != null) {
                value.append("_id", value.get("_id").toString());
            }
            result.add(value);
        }

        Map<String, Object> PageEntity = new HashMap<String, Object>();
        PageEntity.put("pageSize", page.getPageSize());
        PageEntity.put("currentPage", page.getCurrentPage());
        PageEntity.put("rowCount", page.getRowCount());
        PageEntity.put("pageCount", page.getPageCount());
        PageEntity.put("result", result);

        // 调用后销毁
        destroy();

        return PageEntity;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.doubleseven.element.mongodb.MongoDbOperation#selectMostBackDoc()
     */
    public Document selectMostBackDoc() throws Exception {
        // 拼接模糊查询
        jointFuzzy();

        MongoCursor<Document> cursor = collection().find(query).projection(select).sort(sort).limit(1).iterator();

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

    @Override
    public List<Document> aggregate(List list) {
        List<Document> data = new ArrayList<Document>();

        AggregateIterable<Document> result = collection().aggregate(list);
        for (Document value : result) {
            data.add(value);
        }
        return data;
    }


//
////转换Collection类型
//publicstatic<T>voidtransformMap(MapoldMap,MapnewMap,Class<?>KeyType,Class<?>ValueType)throwsException{
//Set<Object>keys=oldMap.keySet();
//for(Objectkey:keys){
//
////根据key、得到value
//Objectvalue=oldMap.get(key);
//
//
//}
//
//}

//转换Collection类型

    public Document object2Document(Object obj) {
        return new Document(MongoDBUtil.objectToMap(obj));
    }

    public class FuzzyQuery {
        private String query;
        private BasicDBObject condition;// 条件
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

        public FuzzyQuery gt(Long value) {
            condition.append("$gt", value);
            return this;
        }

        public FuzzyQuery gte(Long value) {
            condition.append("$gte", value);
            return this;
        }

        public FuzzyQuery lt(Long value) {
            condition.append("$lt", value);
            return this;
        }

        public FuzzyQuery lte(Long value) {
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


        public FuzzyQuery in(Collection value) {
            condition.append("$in", value);
            return this;
        }

        public FuzzyQuery in(String[] value) {
            condition.append("$in", value);
            return this;
        }
        public FuzzyQuery in(int[] value) {
            condition.append("$in", value);
            return this;
        }


        public FuzzyQuery nin(Collection value) {
            condition.append("$nin", value);
            return this;
        }

        public FuzzyQuery nin(String[] value) {
            condition.append("$nin", value);
            return this;
        }
        public FuzzyQuery nin(int[] value) {
            condition.append("$nin", value);
            return this;
        }

        public Pattern pattern(String query) {
            Pattern pattern = Pattern.compile("^.*" + query + ".*$", Pattern.CASE_INSENSITIVE);
            return pattern;
        }
    }
}



