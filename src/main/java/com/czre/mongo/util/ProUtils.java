package com.czre.mongo.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by czre on 2018/1/31
 */
public class ProUtils {
    private static Properties properties;



    private synchronized static void loadProps(String fileName) {
        properties = new Properties();
        InputStream in = null;
        try {
            in = ProUtils.class.getClassLoader().getResourceAsStream(fileName);
            properties.load(in);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage() + "\n" + fileName + "未找到");
        } catch (IOException e) {
            System.err.println(e.getMessage() + "\nIO异常");
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage() + "\n关闭流出现异常");
                }
            }
        }
        System.out.println(fileName + "加载完成");
    }
}

