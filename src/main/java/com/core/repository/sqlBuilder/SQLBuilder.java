package com.core.repository.sqlBuilder;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by sunpeng
 */
public abstract class SQLBuilder {

    public abstract <T> String getSQLCreate(Class<T> type, long id);

    public abstract <T> String getSQLQuery(Class<T> type);

    public abstract <T> String getSQLDelete(Class<T> type);

    public abstract <T> String getSQLUpdate(Class<T> type);

    public abstract <T> String getSQLList(Class<T> type, String sql);

    public abstract long getIdFromObject(Object object) throws Exception;

    public abstract String getPageSQL(String sql, int pageSize, int pageNum);

    public abstract <T> String getCountSQL(Class<T> type, String sql);
}
