package com.core.cache;

import java.util.Date;
import java.util.Map;

/**
 * Created by sunpeng
 * TODO This client could be used in the future.
 */
public class RedisClient extends CacheClient{

    @Override
    public boolean add(String key, Object value) {
        return false;
    }

    @Override
    public boolean add(String key, Object value, Date expiry) {
        return false;
    }

    @Override
    public boolean set(String key, Object value) {
        return false;
    }

    @Override
    public boolean set(String key, Object value, Date expiry) {
        return false;
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public Object[] getMultiArray(String[] keys) {
        return new Object[0];
    }

    @Override
    public Map<String, Object> getMulti(String[] keys) {
        return null;
    }

    @Override
    public boolean delete(String key) {
        return false;
    }

    @Override
    public boolean keyExists(String key) {
        return false;
    }

    @Override
    public boolean flushAll() {
        return false;
    }

    @Override
    public boolean flushAll(String[] servers) {
        return false;
    }
}
