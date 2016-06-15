package com.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sunpeng
 */
public class DBUtil {

    public static <T> String getAppName(Class<T> type){

        String appName = "";

        try {

            T t = type.newInstance();

            Method getAppName = type.getMethod("getAppName");

            appName = (String) getAppName.invoke(t);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return appName;
    }

}
