package com.core.service;

import com.core.domain.Category;
import com.core.repository.sqlBuilder.Page;
import com.core.util.Constant;
import com.core.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.util.*;

/**
 * Created by sunpeng
 */
@Service
public class CategoryService extends BaseService {

    public ObjectNode createCategory(Category category) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            if (category.getParentId() > 0) {

                Category parentCategory = find(Category.class, category.getParentId());

                if (parentCategory.getHasChild() == Constant.CATEGORY_NO_CHILD) {

                    parentCategory.setHasChild(Constant.CATEGORY_HAS_CHILD);
                    baseRepository.update(parentCategory);
                }
            }

            category.setCreateDate(new Date());

            category.setId(baseRepository.create(category));

            log("分类创建", "categoryId: " + category.getId());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateCategory(String data) {

        try {

            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("categoryIds:[ ");

            while (iterator.hasNext()) {

                ObjectNode nextNode = (ObjectNode) iterator.next();

                nextNode.remove("fullPath");

                /* replace the html entity*/
                nextNode.put("name", nextNode.findValue("name").getTextValue());

                nextNode.put("createDate", DateUtil.extDateFix(nextNode.findValue("createDate").getTextValue()));
                nextNode.put("updateDate", DateUtil.extDateFix(nextNode.findValue("updateDate").getTextValue()));

                Category category = objectMapper.readValue(nextNode, Category.class);

                category.setUpdateDate(new Date());
                baseRepository.update(category);

                stringBuilder.append(category.getId()).append(" ");
            }

            log("分类更新", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);

            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode deleteCategory(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("categoryIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                if (id > 0) {
                    baseRepository.delete(Category.class, id);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("分类删除", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);

            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateAcceptCategory(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("categoryIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                if (id > 0) {
                    Category category = find(Category.class, id);
                    category.setStatus(Constant.COMMON_STATUS_ACCEPT);
                    category.setUpdateDate(new Date());
                    baseRepository.update(category);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("分类审核(启用)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateRejectCategory(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("categoryIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                if (id > 0) {
                    Category category = find(Category.class, id);
                    category.setStatus(Constant.COMMON_STATUS_REJECT);
                    category.setUpdateDate(new Date());
                    baseRepository.update(category);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("分类审核(废弃)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode list(int pageSize, int pageNum, String parentId, boolean accept) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            String sql = "";

            Map<String, Object> params = null;
            if (StringUtils.isNotBlank(parentId)) {
                sql = " WHERE parentId = :parentId ";
                params = new HashMap<String, Object>();
                params.put("parentId", NumberUtils.toLong(parentId, 0));
            }

            if (accept) {
                if (sql.contains("WHERE")) {
                    sql = sql + " and status = 1 ";
                } else {
                    sql = " WHERE status = 1 ";
                }
            }

            Page<Category> page = this.getPage(Category.class, sql, params, pageSize, pageNum);

            List<Category> categoryList = page.getResultList();

            ArrayNode arrayNode = objectMapper.createArrayNode();

            for (int i = 0; i < categoryList.size(); i++) {

                Category category = categoryList.get(i);

                category.setName(HtmlUtils.htmlEscape(category.getName()));

                ObjectNode jsonNode = objectMapper.valueToTree(category);

                if (category.getParentId() != 0) {
                    jsonNode.put("fullPath", this.getWholePath(category.getParentId()) + category.getDepict());
                } else {
                    jsonNode.put("fullPath", category.getDepict());
                }

                arrayNode.add(jsonNode);
            }

            objectNode.put("result", "success");
            objectNode.put("totalData", page.getTotalData());
            objectNode.put("data", arrayNode);
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode listAll(int pageSize, int pageNum, String parentId) {
        return list(pageSize, pageNum, parentId, false);
    }

    public ObjectNode listAccept(int pageSize, int pageNum, String parentId) {
        return list(pageSize, pageNum, parentId, true);
    }

    public List<Category> listByStatus(int status) {
        String sql = " WHERE 1=1 ";
        Map <String,Object> param=new HashMap<String, Object>();
        if(status!=-100){
            sql+=" AND status = :status ";
            param.put("status", status);
        }
        return this.list(Category.class,sql,param);
    }

    public String getWholePath(long categoryId) {

        Category category = find(Category.class, categoryId);

        String result = HtmlUtils.htmlUnescape(category.getDepict());

        if (category.getParentId() != 0) {

            return getWholePath(category.getParentId()) + result + File.separator;
        } else {

            return result + File.separator;
        }
    }

}
