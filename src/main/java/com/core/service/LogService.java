package com.core.service;

import com.core.repository.sqlBuilder.Page;
import com.core.security.domain.Log;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by sunpeng
 */
@Service
public class LogService extends BaseService {

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectNode list(int pageSize, int pageNum, String sql) {

        ObjectNode objectNode = objectMapper.createObjectNode();
        Page<Log> page = this.getPage(Log.class, sql, null, pageSize, pageNum);

        List<Log> resultList = page.getResultList();

        objectNode.put("data", objectMapper.valueToTree(resultList));
        objectNode.put("totalData", page.getTotalData());
        objectNode.put("success", true);

        return objectNode;

    }

    public ObjectNode listAll(int pageSize, int pageNum) {
        return list(pageSize, pageNum, " ORDER BY id DESC");
    }

}
