package com.czre.mongo.util;

import com.czre.mongo.annotations.MongoMapper;
import com.czre.mongo.annotations.MongoRealize;
import com.czre.mongo.annotations.RealizeTypeClass;
import org.apache.commons.beanutils.BeanUtils;
import org.bson.Document;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MongoUtil {

    /************************************************** 存入 **************************************************/


    /************************************************** 取出 **************************************************/

    // public static void main(String args[]) throws Exception {
    //     MongoDbOperation dbo = new MongoDbOperationImpl("span");
    //
    //     Document document = dbo.selectOneBackDoc();
    //     Object o = transformJavaBean(SpanPo.class, document);
    //
    //     System.out.println(o);
    // }
    public static <T> Object transformJavaBean(Class<T> sourceClass, Document source, Document origin) throws Exception {

        Object target = sourceClass.newInstance();

        Field[] fields = sourceClass.getDeclaredFields();// 获取所有属性,包含private
        for (Field field : fields) {
            //根据对应关系获得待转换的对象
            Object sourceValue = getSourceValue(field, source);
            if (sourceValue == null) {
                //如果根据对应关系没有匹配的结果，跳过
                continue;
            }

            RealizeTypeClass realizeTypeClass;//转换类的类型记录
            if (field.isAnnotationPresent(MongoRealize.class)) {//有指定字段的映射
                MongoRealize annotation = field.getAnnotation(MongoRealize.class);
                boolean _id = annotation._id();
                if (_id == true) {
                    //如果是_id,那么就转换为string赋值
                    BeanUtils.setProperty(target, field.getName(), sourceValue.toString());
                    continue;
                }

                realizeTypeClass = RealizeTypeClass.newRealizeTypeClass(annotation);
            } else {
                realizeTypeClass = RealizeTypeClass.newRealizeTypeClass();
            }

            //获取到判断实现类类型的对象
            Type genericType = field.getGenericType();

            if (genericType instanceof ParameterizedType) {
                //集合类
                ParameterizedType pt = (ParameterizedType) genericType;//转换为集合的声明

                BeanUtils.setProperty(target, field.getName(), transformAssemble(sourceValue, pt, realizeTypeClass, origin));
            } else {
                //非集合类
                Class propertyClass = (Class) genericType;//转换为参数的类

                //根据类判断是直接添加还是递归转换
                if (isRecursion(propertyClass)) {
                    //需要递归
                    BeanUtils.setProperty(target, field.getName(), transformJavaBean(propertyClass, (Document) sourceValue, origin));
                } else {
                    //不需要递归，直接添加

                    BeanUtils.setProperty(target, field.getName(), sourceValue);

                }
            }
        }

        return target;
    }

    public static Map transformMap(Map sourceMap, ParameterizedType pt, RealizeTypeClass realizeTypeClass, Document origin) throws Exception {
        Map targetMap = realizeTypeClass.newMap();
        Type keyType = pt.getActualTypeArguments()[0];
        Type valueType = pt.getActualTypeArguments()[1];

        if (!(keyType instanceof ParameterizedType) && !(valueType instanceof ParameterizedType)) {
            //key和value都不是集合类
            Class keyClass = (Class) keyType;
            Class valueClass = (Class) valueType;

            //根据类判断是直接添加还是JavaBean递归转换
            if (!isRecursion(keyClass)) {//key不要递归
                if (isRecursion(valueClass)) {//value需要递归
                    for (Object key : sourceMap.keySet()) {
                        Object value = sourceMap.get(key);
                        targetMap.put(key, transformJavaBean(valueClass, (Document) value, origin));
                    }
                } else {//不需要递归，直接添加
                    for (Object key : sourceMap.keySet()) {
                        Object value = sourceMap.get(key);
                        targetMap.put(key,  value);
                    }
                }
            } else {//key是需要递归的，类型声明有问题，不做处理

            }
        } else if (!(keyType instanceof ParameterizedType) && valueType instanceof ParameterizedType) {
            //key不是集合类，value是集合类
            Class keyClass = (Class) keyType;
            ParameterizedType valuePt = (ParameterizedType) valueType;

            //根据类判断是直接添加还是JavaBean递归转换
            if (!isRecursion(keyClass)) {//key不要递归

                for (Object key : sourceMap.keySet()) {
                    Object value = sourceMap.get(key);
                    targetMap.put(key, transformAssemble(value, valuePt, realizeTypeClass, origin));
                }

            } else {//key是需要递归的，类型声明有问题，不做处理

            }

        } else {
            //key是集合类，value是集合类，声明有问题，不做处理
        }
        return targetMap;
    }

    public static void transformCollection(Collection<Object> target, Collection<Object> source, ParameterizedType pt, RealizeTypeClass realizeTypeClass, Document origin) throws Exception {
        Type sonType = pt.getActualTypeArguments()[0];

        if (sonType instanceof ParameterizedType) {//子类型是集合类，要transformAssemble递归处理
            ParameterizedType sonPt = (ParameterizedType) sonType;
            for (Object son : source) {
                target.add(transformAssemble((Collection<Object>) son, sonPt, realizeTypeClass, origin));
            }
        } else if (sonType instanceof Class) {//不是集合类
            Class sonClass = (Class) sonType;

            //根据类判断是直接添加还是JavaBean递归转换
            if (isRecursion(sonClass)) {//需要递归
                for (Object son : source) {
                    target.add(transformJavaBean(sonClass, (Document) son, origin));
                }
            } else {//不要递归
                for (Object son : source) {
                    target.add(son);
                }
            }
        }
    }

    public static List transformList(Collection<Object> sourceCollection, ParameterizedType pt, RealizeTypeClass realizeTypeClass, Document origin) throws Exception {
        List targetList = realizeTypeClass.newList();

        transformCollection(targetList, sourceCollection, pt, realizeTypeClass, origin);

        return targetList;
    }

    public static Set transformSet(Collection<Object> sourceCollection, ParameterizedType pt, RealizeTypeClass realizeTypeClass, Document origin) throws Exception {
        Set targetSet = realizeTypeClass.newSet();
        transformCollection(targetSet, sourceCollection, pt, realizeTypeClass, origin);
        return targetSet;
    }

    //转换集合体，包括list、set、map
    public static Object transformAssemble(Object source, ParameterizedType pt, RealizeTypeClass realizeTypeClass, Document origin) throws Exception {
        Class clazz = (Class) pt.getRawType();
        if (Map.class.isAssignableFrom(clazz)) {

            return transformMap((Map) source, pt, realizeTypeClass, origin);

        } else if (List.class.isAssignableFrom(clazz)) {

            return transformList((Collection<Object>) source, pt, realizeTypeClass, origin);
        } else if (Set.class.isAssignableFrom(clazz)) {

            return transformSet((Collection<Object>) source, pt, realizeTypeClass, origin);
        } else {
            //声明有问题，不做处理
            return null;
        }
    }

    /************************************************** 工具方法 **************************************************/
    //根据对应关系获得待转换的对象
    public static Object getSourceValue(Field field, Object object) {

        if (field.isAnnotationPresent(MongoMapper.class)) {
            MongoMapper annotation = field.getAnnotation(MongoMapper.class);
            String value = annotation.value();
            String[] mapper = value.split("\\.");
            for (String key : mapper) {
                try {
                    object = ((Document) object).get(key);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return object;
        } else {
            return ((Document) object).get(field.getName());
        }
    }

    public static boolean isRecursion(Class<?> classType) {
        if (classType.isPrimitive() || classType == byte[].class || classType == String.class || classType == Boolean.class || classType == Byte.class || classType == Short.class || classType == Integer.class || classType == Long.class || classType == Float.class || classType == Double.class || classType == Enum.class) {
            //基础数据类型
            return false;
        } else if(classType.getName().contains("Object")){
            return false;
        }  else{
            //需要递归
            return true;
        }
    }

}

