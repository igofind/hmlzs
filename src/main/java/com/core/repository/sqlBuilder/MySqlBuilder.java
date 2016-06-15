package com.core.repository.sqlBuilder;

import com.core.util.DBUtil;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sunpeng
 */
public class MySqlBuilder extends SQLBuilder {

    private ConcurrentHashMap<Class, List<String>> metaMap = new ConcurrentHashMap<Class, List<String>>();

    private <T> String buildSQLCreate(Class<T> type, long id) {

        StringBuffer sqlBuffer = new StringBuffer();

        StringBuffer paramBuffer = new StringBuffer();

        sqlBuffer.append("INSERT INTO ")
                .append(DBUtil.getAppName(type))
                .append("_")
                .append(type.getSimpleName().toLowerCase())
                .append("(");

        paramBuffer.append(" VALUES(");

        List<String> fieldList = getFieldList(type);
        for (String field : fieldList) {
            if ("APP_NAME".equals(field.toUpperCase())) {
                continue;
            }
            if ("id".equals(field.toLowerCase())) {
                paramBuffer.append(id);
                paramBuffer.append(",");
            } else {
                paramBuffer.append(" :")
                        .append(field)
                        .append(",");
            }
            sqlBuffer.append(field).append(",");

        }
        sqlBuffer.setCharAt(sqlBuffer.length() - 1, ')');
        paramBuffer.setCharAt(paramBuffer.length() - 1, ')');

        return sqlBuffer.append(paramBuffer).toString();
    }

    private <T> String buildSQLQueryById(Class<T> type) {

        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        List<String> fieldList = getFieldList(type);
        for (String field : fieldList) {

            if ("APP_NAME".equals(field.toUpperCase())) {
                continue;
            }
            sqlBuffer.append(field).append(", ");
        }
        sqlBuffer.setCharAt(sqlBuffer.lastIndexOf(","), ' ');
        sqlBuffer.append("FROM ")
                .append(DBUtil.getAppName(type))
                .append("_")
                .append(type.getSimpleName().toLowerCase())
                .append(" WHERE id = :id ");
        return sqlBuffer.toString();
    }

    private <T> String buildSQLDelete(Class<T> type) {

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append("DELETE FROM ")
                .append(DBUtil.getAppName(type))
                .append("_")
                .append(type.getSimpleName().toLowerCase())
                .append(" WHERE id = :id ");

        return stringBuffer.toString();
    }

    private <T> String buildSQLUpdate(Class<T> type) {

        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("UPDATE ")
                .append(DBUtil.getAppName(type))
                .append("_")
                .append(type.getSimpleName().toLowerCase())
                .append(" SET ");
        String whereTerm = " WHERE id = :id ";
        List<String> fieldList = getFieldList(type);
        for (String field : fieldList) {
            if ("APP_NAME".equals(field.toUpperCase())) {
                continue;
            }
            if ("id".equals(field)) {
                continue;
            }
            sqlBuffer.append(field)
                    .append("= :")
                    .append(field)
                    .append(", ");

        }
        sqlBuffer.setCharAt(sqlBuffer.lastIndexOf(","), ' ');
        sqlBuffer.append(whereTerm);

        return sqlBuffer.toString();
    }

    private <T> String buildSQLList(Class<T> type, String sql) {

        StringBuffer sqlBuffer = new StringBuffer();

        sqlBuffer.append("SELECT ");
        List<String> fieldList = getFieldList(type);
        for (String field : fieldList) {

            if ("APP_NAME".equals(field)) {
                continue;
            }
            sqlBuffer.append(field)
                    .append(", ");
        }
        sqlBuffer.setCharAt(sqlBuffer.lastIndexOf(","), ' ');
        sqlBuffer.append("FROM ")
                .append(DBUtil.getAppName(type))
                .append("_")
                .append(type.getSimpleName().toLowerCase());

        if (StringUtils.isNotBlank(sql)) {
            sqlBuffer.append(" ")
                    .append(sql);
        }
        return sqlBuffer.toString();
    }

    public <T> String buildPageSQL(String sql, int pageSize, int pageNum) {
        return new StringBuffer()
                .append(sql)
                .append(" LIMIT ")
                .append((pageNum - 1) * pageSize)
                .append(",")
                .append(pageSize)
                .toString();
    }

    public <T> String buildCountSQL(Class<T> type, String sql) {

        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT COUNT(1) FROM ")
                .append(DBUtil.getAppName(type))
                .append("_")
                .append(type.getSimpleName().toLowerCase())
                .append(sql);

        return sqlBuffer.toString();
    }

    @Override
    public <T> String getSQLCreate(Class<T> type, long id) {
        return buildSQLCreate(type, id);
    }

    @Override
    public <T> String getSQLQuery(Class<T> type) {
        return buildSQLQueryById(type);
    }

    @Override
    public <T> String getSQLDelete(Class<T> type) {
        return buildSQLDelete(type);
    }

    @Override
    public <T> String getSQLUpdate(Class<T> type) {
        return buildSQLUpdate(type);
    }

    @Override
    public <T> String getSQLList(Class<T> type, String sql) {

        return buildSQLList(type, sql);
    }

    public List<String> getFieldList(Class type) {
        List<String> result = metaMap.get(type);
        if (result == null) {
            result = new ArrayList<String>();
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isTransient(field.getModifiers())) {
                    result.add(field.getName());
                }
            }
            metaMap.put(type, result);
        }
        return result;
    }

    public long getIdFromObject(Object object) throws Exception {

        Method method = object.getClass().getMethod("getId");

        return (Long) method.invoke(object);
    }

    @Override
    public String getPageSQL(String sql, int pageSize, int pageNum) {
        return buildPageSQL(sql, pageSize, pageNum);
    }

    @Override
    public <T> String getCountSQL(Class<T> type, String sql) {
        return buildCountSQL(type, sql);
    }

}
