package com.czre.mongo.util;

import com.czre.mongo.util.MongoDBOperationImpl.FuzzyQuery;
import org.bson.Document;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Created by czre on 2018/2/7
 */
public interface MongoDBOperation {
    /****************条件拼接***************/
    // 文档定位接口
    void query(String key, Object value);

    FuzzyQuery query(String key);

    FuzzyQuery query();

    // 挑选与排序的接口
    void select(String key, int value);

    void sort(String key, int value);

    // 更新条件接口
    void set(String key, int value);

    void push(String key, int value);

    void pull(String key, int value);

    void unset(String key);

    void inc(String key, int value);

    /****************操作数据库***************/
    // 添加接口
    boolean insertOne(Object obj);

    // 删除接口(文档定位)
    boolean deleteOne();

    boolean deleteMany();

    // 更新接口(文档定位/更新条件)
    boolean updateOne();

    boolean updateMany();

    // 查询接口-BackBean (文档定位/挑选/排序)
    Object selectOneBackBean() throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException;

    @SuppressWarnings("rawtypes")
    List selectManyBackBean() throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException;

    MongoDBPage selectPageReturnBean(MongoDBPage page) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException;

    Object selectMostBackBean() throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException;

    // 查询接口-BackDoc (文档定位/挑选/排序)
    Document selectOneBackDoc();

    List<Document> selectManyBackDoc();

    Map<String, Object> selectPageBackDoc(MongoDBPage page);

    Document selectMostBackDoc();
}
