package com.czre.mongo.util;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.beans.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by czre on 2018/2/9
 */
public class MongoDBUtil {
    public static Document beanToDoc(Object obj) {
        Map<String, Object> map = objectToMap(obj);
        if (map.get("_id") != null) {
            map.put("_id", new ObjectId(map.get("_id").toString()));
        }
        return new Document(map);
    }

    public static Object docToBean(Document document, String collectionName) throws InstantiationException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        if (document.get("_id") != null) {
            document.put("_id", document.get("_id").toString());
        }
        Object object = mapToBean(document, MongoDBCollectionConvert.convert(collectionName));
        return object;
    }

    @SuppressWarnings("unchecked")
    public static <T> Object mapToBean(Map<String, Object> map, Class<T> T) throws IllegalAccessException, InstantiationException, IntrospectionException, InvocationTargetException {
        // 实例化Bean
        T bean = T.newInstance();
        // 获取这个类的所有属性
        Field[] properties = T.getDeclaredFields();
        // 遍历属性集获取单个属性
        for (Field property : properties) {
            // 获取属性申请的名字
            String propertyName = property.getName();
            // 判断map中是否有这个属性定义的名字
            if (map.keySet().contains(propertyName)) {
                // 获取到属性的值value
                Object value = map.get(propertyName);

                property.setAccessible(true);
                // 获取属性类型
                Class<?> propertyType = property.getType();
                see(propertyType, "propertyType=");

                // 属性类型分辨
                int number = switchPathByClassType(propertyType);

                switch (number) {
                    case 1:
                        // 基础数据类型
                        BeanUtils.copyProperty(bean, propertyName, value);
                        break;
                    case 2:
                        // Collection
                        // 得到泛型里的class类型对象
                        // Collection类型
                        // 获取genericType
                        Type genericType = property.getGenericType();
                        if (genericType == null) {
                            continue;
                        }
                        ParameterizedType parameterizedType = (ParameterizedType) genericType;
                        Class<?> collectionValueClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                        int switchPathByCollectionValueClass = switchPathByClassType(collectionValueClass);
                        // 强转成为Collection,用于遍历
                        Collection<Object> collection = (Collection<Object>) value;

                        // 实例化泛型参数强转为Collection,用于接收值
                        Collection<Object> propertyBean = (Collection<Object>) propertyType.newInstance();

                        // 遍历,递归将value中的所有map转化为bean
                        for (Object collectionValue : collection) {
                            switch (switchPathByCollectionValueClass) {
                                case 1:
                                    // 基础数据类型
                                    propertyBean.add(collectionValue);
                                    break;
                                case 2:
                                    // Collection
                                    break;
                                case 3:
                                    // Map
                                    Object collectionValueBean = mapToBean((Map<String, Object>) collectionValue, collectionValueClass);
                                    propertyBean.add(collectionValueBean);
                                    break;
                            }
                        }
                        BeanUtils.copyProperty(bean, propertyName, propertyBean);
                        break;
                    case 3:
                        BeanUtils.copyProperty(bean, propertyName, mapToBean((Map<String, Object>) value, propertyType));
                        break;
                }
            }
        }
        return bean;
    }

    private static int switchPathByClassType(Class<?> classType) {
        // 基本数据类型String,Boolean,Byte,Short,Integer,Long,Float,Double,Enum
        if (classType.isPrimitive() || classType == String.class || classType == Boolean.class || classType == Byte.class || classType == Short.class || classType == Integer.class || classType == Long.class || classType == Float.class || classType == Double.class || classType == Enum.class) {
            return 1;
        } else if (Collection.class.isAssignableFrom(classType)) {
            return 2;
        } else {
            return 3;
        }
    }

    public static void see(Class<?> T, String info) throws IntrospectionException {
        // 获取属性类型的描述
        BeanInfo propertyBeanInfo = Introspector.getBeanInfo(T);
        BeanDescriptor propertyBeanDescriptor = propertyBeanInfo.getBeanDescriptor();
        String propertyTypeName = propertyBeanDescriptor.getDisplayName();
        // 判断是否为基本类型—String、int、float
        // String,Boolean,Byte,Short,Integer,Long,Float,Double
    }

    public static Map<String, Object> objectToMap(Object obj) {
        try {
            Class clazz = obj.getClass();
            Map result = new HashMap();
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; i++) {
                PropertyDescriptor descriptor = propertyDescriptors[i];
                String propertyName = descriptor.getName();
                if (!propertyName.equals("class")) {
                    Method method = descriptor.getReadMethod();
                    Object data = method.invoke(obj, new Object[0]);
                    if (data == null) {
                        continue;
                    }
                    // 判断是否为基础类型
                    // String,Bollean,Byte,Short,Integer,Long,Float,Double
                    // 判断是否集合类,Collection,Map
                    if (data instanceof String || data instanceof Boolean || data instanceof Byte || data instanceof Short || data instanceof Integer || data instanceof Long || data instanceof Float || data instanceof Double || data instanceof Enum) {
                        if (data != null) {
                            result.put(propertyName, data);
                        } else if (data instanceof Collection) {
                            Collection<?> objects = arrayToMap((Collection<?>) result);
                            result.put(propertyName, objects);
                        } else if (data instanceof Map) {
                            Map<Object, Object> map = mapToMap((Map<Object, Object>) data);
                            result.put(propertyName, map);
                        } else {
                            Map map = objectToMap(data);
                            result.put(propertyName, map);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Object, Object> mapToMap(Map<Object, Object> map) {
        Map<Object, Object> result = new HashMap<Object, Object>();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object resultKey = null;
            Object value = entry.getValue();
            Object resultValue = null;
            result.put(compute(key, resultKey), compute(value, resultValue));
        }
        return result;
    }

    private static Object compute(Object param, Object result) {
        if (param instanceof Collection) {
            result = arrayToMap((Collection) param);
        } else if (param instanceof Map) {
            result = mapToMap((Map) param);
        } else {
            if (param instanceof String || param instanceof Boolean || param instanceof Byte || param instanceof Short || param instanceof Integer || param instanceof Long || param instanceof Float || param instanceof Double || param instanceof Enum) {
                if (param != null) {
                    result = param;
                }
            } else {
                result = objectToMap(param);
            }
        }
        return result;
    }

    private static Collection arrayToMap(Collection<?> result) {
        ArrayList arrayList = new ArrayList();
        for (Object data : result) {
            if (data instanceof Collection) {
                Collection col = arrayToMap((Collection) data);
                arrayList.add(col);
            } else if (data instanceof Map) {
                Map map = mapToMap((Map) data);
                arrayList.add(map);
            } else {
                if (data instanceof String || data instanceof Boolean || data instanceof Byte || data instanceof Short || data instanceof Integer || data instanceof Long || data instanceof Float || data instanceof Double || data instanceof Enum) {
                    if (data != null) {
                        arrayList.add(data);
                    }
                } else {
                    Object map = objectToMap(data);
                    arrayList.add(map);
                }
            }
        }
        return arrayList;
    }
}
