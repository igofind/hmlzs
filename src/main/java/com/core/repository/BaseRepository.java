package com.core.repository;

import com.core.cache.CacheClient;
import com.core.config.Config;
import com.core.repository.idGenerator.IdGenerator;
import com.core.repository.sqlBuilder.SQLBuilder;
import com.core.util.DBUtil;
import com.core.util.JsonUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunpeng
 */
@Repository
public class BaseRepository {

    protected Log logger = LogFactory.getLog(BaseRepository.class);

    @Autowired
    protected CacheClient cacheClient;

    @Autowired
    protected Config config;

    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    protected IdGenerator idGenerator;

    @Autowired
    protected SQLBuilder sqlBuilder;

    public static RowMapper<Long> idRowMapper = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getLong(1);
        }
    };

    public long create(Object object) throws Exception {

        long id = 0;

        String appName = DBUtil.getAppName(object.getClass());
        id = sqlBuilder.getIdFromObject(object);

        if (id == 0) {

            id = idGenerator.getNextId(object.getClass().getSimpleName().toLowerCase(), appName);
        }

        jdbcTemplate.update(sqlBuilder.getSQLCreate(object.getClass(), id), new BeanPropertySqlParameterSource(object));

        return id;
    }

    public <T> T find(Class<T> type, Long id) {

        T obj = null;
        boolean needCache = false;
        needCache = needCache(type);
        if (needCache) {
            obj = findFromCache(id, type);
        }
        if (obj == null) {
            obj = findFromDb(id, type);
            if (needCache) {
                putToCache(obj, type, id);
            }
        }

        return obj;
    }

    public <T> T findFromCache(long id, Class<T> type) {

        String key = DBUtil.getAppName(type) + "-" + type.getSimpleName() + "-" + id;
        String v = (String) cacheClient.get(key);
        if (v != null) {
            return JsonUtil.fromJson(v, type);
        }
        return null;
    }

    public <T> T findFromDb(long id, Class<T> type) {

        String sql = sqlBuilder.getSQLQuery(type);

        Map<String, String> params = new HashMap<String, String>();
        params.put("id", String.valueOf(id));

        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<T>(type));
    }

    public <T> void putToCache(T obj, Class<T> type, long id) {

        String key = DBUtil.getAppName(type) + "-" + type.getSimpleName() + "-" + id;
        cacheClient.set(key, JsonUtil.toJson(obj));
    }

    public int update(Object object) throws Exception {

        String sql = sqlBuilder.getSQLUpdate(object.getClass());
        if (needCache(object.getClass())) {
            deleteFromCache(sqlBuilder.getIdFromObject(object), object.getClass());
        }

        return jdbcTemplate.update(sql, new BeanPropertySqlParameterSource(object));
    }

    public <T> boolean deleteFromCache(long id, Class<T> type) {
        return cacheClient.delete(DBUtil.getAppName(type) + "-" + type.getSimpleName() + "-" + id);
    }

    public <T> int delete(Class<T> type, long id) {

        String appName = DBUtil.getAppName(type);

        Map<String, String> params = new HashMap<String, String>();

        params.put("id", String.valueOf(id));

        if (needCache(type)) {
            cacheClient.delete(appName + "-" + type.getSimpleName() + "-" + id);
        }

        return jdbcTemplate.update(sqlBuilder.getSQLDelete(type), params);
    }

    public <T> List<T> list(Class<T> type, String sql, Map<String, Object> param) {

        List<T> result = new ArrayList<T>();
        String sqlString = "";
        sqlString = sqlBuilder.getSQLList(type, sql);

        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        if (MapUtils.isNotEmpty(param)) {
            mapSqlParameterSource.addValues(param);
        }

        if (needCache(type)) {

            List<Long> idList = jdbcTemplate.query(sqlString, mapSqlParameterSource, idRowMapper);
            for (Long id : idList) {
                result.add(find(type, id));
            }
        } else {

            result = jdbcTemplate.query(sqlString, mapSqlParameterSource, new BeanPropertyRowMapper<T>(type));
        }

        return result;
    }

    public <T> boolean needCache(Class<T> type) {

        try {
            Method method = type.getMethod("needCache");

            return (Boolean) method.invoke(type);

        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public <T> int count(Class<T> type, String sql, Map<String, Object> param) {

        return jdbcTemplate.queryForObject(sqlBuilder.getCountSQL(type, sql), param, Integer.class);
    }

    public <T> List<T> pageList(Class<T> type, String sql, Map<String, Object> param, int pageSize, int pageNum) {

        return list(type, sqlBuilder.getPageSQL(sql, pageSize, pageNum), param);
    }

}
