package com.czre.mongo.util;


import com.mongodb.BasicDBObject;
import org.bson.Document;
import com.czre.mongo.util.MongoDBOperationImpl.FuzzyQuery;
import java.util.List;
import java.util.Map;

public interface MongoDBOperation<MongoDBVo> {

    /********************************************************************************** 条件拼接 **********************************************************************************/

    /************************************************** 文档定位接口 **************************************************/

    public abstract void query(String key, Object val);

    public abstract FuzzyQuery query(String key);

    public abstract FuzzyQuery query();

    /************************************************** 挑选与排序的接口 **************************************************/

    public abstract void select(String key, int val);

    public abstract void sort(String key, int val);

    /************************************************** 更新条件接口 **************************************************/
    public abstract void set(String key, Object val);

    public abstract void push(String key, Object val);

    public abstract void pull(String key, Object val);

    void push(String key, String val);

    public abstract void unset(String key);

    public abstract void inc(String key, Object val);

    /********************************************************************************** 操作数据库 **********************************************************************************/

    /************************************************** 添加接口 **************************************************/

    public abstract boolean insertMany(List<Document> docs);

    public abstract boolean insertOne(Object obj);

    public boolean insertOne(Document doc);

    /************************************************** 删除接口（文档定位） **************************************************/

    public abstract boolean deleteOne();

    public abstract boolean deleteMany();

    /************************************************** 更新接口（文档定位/更新条件） **************************************************/
    public abstract boolean updateOne();

    public abstract boolean updateMany();

    /************************************************** 查询接口-BackBean（文档定位/挑选/排序） **************************************************/

    public abstract Object selectOneBackBean(Class<MongoDBVo> T);

    public abstract List selectManyBackBean(Class<MongoDBVo> T);

    public abstract List selectPageBackBean(Class<MongoDBVo> T, MongoDBPage page) throws Exception;

    public abstract Object selectMostBackBean(Class<MongoDBVo> T) throws Exception;

    /************************************************** 查询接口-BackDoc（文档定位/挑选/排序） **************************************************/

    public abstract Document selectOneBackDoc() throws Exception;

    public abstract List<Document> selectManyBackDoc();

    public abstract Map<String, Object> selectPageBackDoc(MongoDBPage page) throws Exception;

    public abstract Document selectMostBackDoc() throws Exception;

    public List<Document> aggregate(List<BasicDBObject> list);
}