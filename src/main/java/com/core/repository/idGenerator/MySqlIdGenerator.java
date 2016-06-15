package com.core.repository.idGenerator;

import com.core.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sunpeng
 */
@Component
public class MySqlIdGenerator implements IdGenerator {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private Config dbConfig;

    private ReentrantReadWriteLock rrw = new ReentrantReadWriteLock();

    private Map<String, List<Long>> idMap = new ConcurrentHashMap<String, List<Long>>();

    @Override
    public long getNextId(String table, String appName){

        long result = 0;

        List<Long> ids = null;

        try {

            rrw.writeLock().lock();

            if (!idMap.containsKey(table)) {
                ids = initKeyGen(table, appName);
            } else {
                ids = idMap.get(table);
                if (ids.size() == 0) {
                    ids = updateKeyGen(table);
                }
            }

            result = ids.get(0);
            ids.remove(0);

            idMap.put(table, ids);

        } finally {
            rrw.writeLock().unlock();
        }

        return result;
    }

    public List<Long> initKeyGen(String table, String appName) {

        Long lastUsedId = namedParameterJdbcTemplate.queryForObject("SELECT MAX(id) FROM " + appName + "_" + table, new HashMap<String, Object>(), Long.class);

        lastUsedId = lastUsedId == null ? 0 : lastUsedId;

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("table_name", table);
        params.put("last_used_id", String.valueOf(dbConfig.getDbIdBuffSize() + lastUsedId));

        if (lastUsedId == null) {
            namedParameterJdbcTemplate.update("INSERT INTO " + dbConfig.getPLATFORM() + "_keygen (table_name, last_used_id) VALUES (:table_name, :last_used_id)", params);
        } else {
            int update = namedParameterJdbcTemplate.update("UPDATE " + dbConfig.getPLATFORM() + "_keygen SET last_used_id = :last_used_id WHERE table_name = :table_name", params);

            if (update == 0) {
                namedParameterJdbcTemplate.update("INSERT INTO " + dbConfig.getPLATFORM() + "_keygen (table_name, last_used_id) VALUES (:table_name, :last_used_id)", params);
            }
        }

        List<Long> ids = new ArrayList<Long>();

        for (int i = 0; i < dbConfig.getDbIdBuffSize(); i++) {
            ids.add(lastUsedId + (i + 1));
        }

        return ids;
    }

    public List<Long> updateKeyGen(String table) {

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("table_name", table);

        long lastUsedId = namedParameterJdbcTemplate.queryForObject("SELECT last_used_id FROM " + dbConfig.getPLATFORM() + "_keygen WHERE table_name = :table_name", params, Long.class);

        List<Long> ids = new ArrayList<Long>();

        for (int i = 0; i < dbConfig.getDbIdBuffSize(); i++) {
            ids.add(lastUsedId + (i + 1));
        }

        params.put("last_used_id", String.valueOf(lastUsedId + dbConfig.getDbIdBuffSize()));
        namedParameterJdbcTemplate.update("UPDATE " + dbConfig.getPLATFORM() + "_keygen SET last_used_id = :last_used_id WHERE table_name = :table_name ", params);

        return ids;
    }

}
