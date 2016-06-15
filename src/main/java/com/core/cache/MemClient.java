package com.core.cache;

import com.whalin.MemCached.MemCachedClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;

/**
 * Created by sunpeng
 */
public class MemClient extends CacheClient {

    @Autowired
    private MemCachedClient memCachedClient;

    /**
     * Adds data to the server; only the key and the value are specified.
     *
     * @param key
     * @param value
     * @return true/false
     */
    @Override
    public boolean add(String key, Object value) {
        return memCachedClient.add(key, value);
    }

    /**
     * Adds data to the server; the key, value, and an expiration time are specified.
     *
     * @param key
     * @param value
     * @param expiry
     * @return true, if the data was successfully stored
     */
    public boolean add(String key, Object value, Date expiry) {
        return memCachedClient.add(key, value, expiry);
    }

    /**
     * Stores data on the server; only the key and the value are specified.
     *
     * @param key
     * @param value
     * @return true/false
     */
    @Override
    public boolean set(String key, Object value) {
        return memCachedClient.set(key, value);
    }

    /**
     * Stores data on the server; the key, value, and an expiration time are specified.
     *
     * @param key
     * @param value
     * @param expiry
     * @return true, if the data was successfully stored
     */
    public boolean set(String key, Object value, Date expiry) {
        return memCachedClient.set(key, value, expiry);
    }

    /**
     * Retrieve a key from the server, using a specific hash.
     * If the data was compressed or serialized when compressed,
     * it will automatically be decompressed or serialized, as appropriate
     *
     * @param key
     * @return the object that was previously stored, or null if it was not previously stored
     */
    @Override
    public Object get(String key) {
        return memCachedClient.get(key);
    }

    /**
     * Retrieve multiple objects from the memcache.
     * This is recommended over repeated calls to get(), since it is more efficient
     *
     * @param keys
     * @return Object array ordered in same order as key array containing results
     */
    public Object[] getMultiArray(String[] keys) {
        return memCachedClient.getMultiArray(keys);
    }

    /**
     * Retrieve multiple objects from the memcache.
     * This is recommended over repeated calls to get(), since it is more efficient.
     *
     * @param keys
     * @return
     *      a hashmap with entries for each key is found by the server,
     *      keys that are not found are not entered into the hashmap,
     *      but attempting to retrieve them from the hashmap gives you null.
     */
    public Map<String, Object> getMulti(String[] keys) {
        return memCachedClient.getMulti(keys);
    }

    /**
     * Deletes an object from cache given cache key
     *
     * @param key
     * @return true, if the data was deleted successfully
     */
    @Override
    public boolean delete(String key) {
        return memCachedClient.delete(key);
    }

    /**
     * Checks to see if key exists in cache
     *
     * @param key
     * @return true if key found in cache, false if not (or if cache is down)
     */
    @Override
    public boolean keyExists(String key) {
        return memCachedClient.keyExists(key);
    }

    /**
     * Invalidates the entire cache.
     * Will return true only if succeeds in clearing all servers
     *
     * @return success true/false
     */
    @Override
    public boolean flushAll() {
        return memCachedClient.flushAll();
    }

    /**
     * Invalidates the entire cache.
     * Will return true only if succeeds in clearing all servers.
     * If pass in null, then will try to flush all servers.
     *
     * @param servers optional array of host(s) to flush (host:port)
     * @return
     *      success true/false
     */
    public boolean flushAll(String[] servers) {
        return memCachedClient.flushAll(servers);
    }
}
