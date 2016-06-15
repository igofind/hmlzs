package com.core.service;

import com.core.repository.sqlBuilder.Page;
import com.core.security.domain.Right;
import com.core.util.Constant;
import com.core.util.DateUtil;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Iterator;

/**
 * Created by sunpeng
 */
@Service
public class RightService extends BaseService {

    /*@Autowired
    private SecurityRepository securityRepository;*/

    public ObjectNode listAll(int pageSize, int page) {

        ObjectNode objectNode = objectMapper.createObjectNode();

        Page<Right> rightPage = this.getPage(Right.class, "", null, pageSize, page);

        ArrayNode arrayNode = objectMapper.valueToTree(rightPage.getResultList());

        objectNode.put("data", arrayNode);
        objectNode.put("totalData", rightPage.getTotalData());

        objectNode.put("result", "success");
        objectNode.put("success", true);

        return objectNode;
    }

    public ObjectNode update(String data) {

        try {

            ObjectNode objectNode = objectMapper.createObjectNode();
            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("rightIds:[ ");

            while (iterator.hasNext()) {
                ObjectNode nextNode = (ObjectNode) iterator.next();
                nextNode.put("createDate", DateUtil.extDateFix(nextNode.findValue("createDate").getTextValue()));
                Right right = objectMapper.treeToValue(nextNode, Right.class);
                baseRepository.update(right);

                stringBuilder.append(right.getId()).append(" ");
            }

            log("权限更新", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public ObjectNode updateAccept(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("rightIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                Right right = find(Right.class, id);
                right.setStatus(Constant.COMMON_STATUS_ACCEPT);
                baseRepository.update(right);

                stringBuilder.append(right.getId()).append(" ");
            }

            log("权限启用", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateReject(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("rightIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                Right right = find(Right.class, id);
                right.setStatus(Constant.COMMON_STATUS_REJECT);
                baseRepository.update(right);

                stringBuilder.append(right.getId()).append(" ");
            }

            log("权限废弃", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }
}
