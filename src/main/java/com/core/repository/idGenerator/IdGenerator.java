package com.core.repository.idGenerator;

import java.util.List;

/**
 * Created by sunpeng
 */
public interface IdGenerator {

    long getNextId(String table, String appName);

    List<Long> initKeyGen(String table, String appName);

    List<Long> updateKeyGen(String table);
}
