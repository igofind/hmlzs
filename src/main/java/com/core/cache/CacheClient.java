package com.core.cache;

import java.util.Date;
import java.util.Map;

/**
 * Created by sunpeng
 */

public abstract class CacheClient {

    /**
     * Adds data to the server; only the key and the value are specified.
     *
     * @param key
     * @param value
     * @return true/false
     */

    public abstract boolean add(String key, Object value);

    /**
     * Adds data to the server; the key, value, and an expiration time are specified.
     *
     * @param key
     * @param value
     * @param expiry
     * @return true, if the data was successfully stored
     */
    public abstract boolean add(String key, Object value, Date expiry);

    /**
     * Stores data on the server; only the key and the value are specified.
     *
     * @param key
     * @param value
     * @return true/false
     */

    public abstract boolean set(String key, Object value);

    /**
     * Stores data on the server; the key, value, and an expiration time are specified.
     *
     * @param key
     * @param value
     * @param expiry
     * @return true, if the data was successfully stored
     */
    public abstract boolean set(String key, Object value, Date expiry);

    /**
     * Retrieve a key from the server, using a specific hash.
     * If the data was compressed or serialized when compressed,
     * it will automatically be decompressed or serialized, as appropriate
     *
     * @param key
     * @return the object that was previously stored, or null if it was not previously stored
     */

    public abstract Object get(String key);

    /**
     * Retrieve multiple objects from the memcache.
     * This is recommended over repeated calls to get(), since it is more efficient
     *
     * @param keys
     * @return Object array ordered in same order as key array containing results
     */
    public abstract Object[] getMultiArray(String[] keys);

    /**
     * Retrieve multiple objects from the memcache.
     * This is recommended over repeated calls to get(), since it is more efficient.
     *
     * @param keys
     * @return a hashmap with entries for each key is found by the server,
     * keys that are not found are not entered into the hashmap,
     * but attempting to retrieve them from the hashmap gives you null.
     */
    public abstract Map<String, Object> getMulti(String[] keys);

    /**
     * Deletes an object from cache given cache key
     *
     * @param key
     * @return true, if the data was deleted successfully
     */

    public abstract boolean delete(String key);

    /**
     * Checks to see if key exists in cache
     *
     * @param key
     * @return true if key found in cache, false if not (or if cache is down)
     */

    public abstract boolean keyExists(String key);

    /**
     * Invalidates the entire cache.
     * Will return true only if succeeds in clearing all servers
     *
     * @return success true/false
     */

    public abstract boolean flushAll();

    /**
     * Invalidates the entire cache.
     * Will return true only if succeeds in clearing all servers.
     * If pass in null, then will try to flush all servers.
     *
     * @param servers optional array of host(s) to flush (host:port)
     * @return success true/false
     */
    public abstract boolean flushAll(String[] servers);

}
