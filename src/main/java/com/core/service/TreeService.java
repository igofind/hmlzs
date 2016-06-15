package com.core.service;

import com.core.domain.Tree;
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
public class TreeService extends BaseService {

    public ObjectNode createTreeOrNode(Tree tree) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            tree.setName(tree.getName());

            tree.setTreeRoad(tree.getName());

            if (tree.getParentId() > 0) {

                Tree parentTree = find(Tree.class, tree.getParentId());

                if (parentTree.getHasChild() == Constant.TREE_NO_CHILD) {

                    parentTree.setHasChild(Constant.TREE_HAS_CHILD);
                    baseRepository.update(parentTree);
                }
            }

            tree.setCreateDate(new Date());

            tree.setId(baseRepository.create(tree));

            log("树(Tree)创建", tree.toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateTreeOrNode(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("treeIds:[");

            while (iterator.hasNext()) {

                ObjectNode nextNode = (ObjectNode) iterator.next();
                nextNode.remove("fullStorePath");

                /* fix the ext's date format problem*/
                nextNode.put("createDate", DateUtil.extDateFix(nextNode.findValue("createDate").getTextValue()));
                nextNode.put("updateDate", DateUtil.extDateFix(nextNode.findValue("updateDate").getTextValue()));

                /* replace the html entity*/
                nextNode.put("treeRoad", nextNode.findValue("name").getTextValue());

                Tree tree = objectMapper.readValue(nextNode, Tree.class);

                tree.setUpdateDate(new Date());
                baseRepository.update(tree);

                stringBuilder.append(tree.getId()).append(" ");

            }

            log("树(Tree)更新", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public ObjectNode deleteTreeOrNode(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("treeIds:[");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                if (id > 0) {
                    baseRepository.delete(Tree.class, id);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("树(Tree)删除", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateAcceptTree(String data) {
        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("treeIds:[");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                if (id > 0) {
                    Tree tree = find(Tree.class, id);
                    tree.setStatus(Constant.COMMON_STATUS_ACCEPT);
                    tree.setUpdateDate(new Date());
                    baseRepository.update(tree);

                    stringBuilder.append(id).append(" ");
                }
            }
            log("树(Tree)审核(启用)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateRejectTree(String data) {
        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("treeIds:[");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                if (id > 0) {
                    Tree tree = find(Tree.class, id);
                    tree.setStatus(Constant.COMMON_STATUS_REJECT);
                    tree.setUpdateDate(new Date());
                    baseRepository.update(tree);

                    stringBuilder.append(id).append(" ");
                }
            }
            log("树(Tree)审核(废弃)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String getTreeRoad(long treeId) {

        return getWholePath(treeId, Constant.TREE_ROAD);
    }

    public String getStorePath(long treeId) {

        return getWholePath(treeId, Constant.STORE_PATH);
    }

    public String getWholePath(long treeId, int flag) {

        String result = "";
        Tree targetTree = find(Tree.class, treeId);

        if (Constant.TREE_ROAD == flag) {
            result = HtmlUtils.htmlEscape(targetTree.getTreeRoad());
        } else {
            result = HtmlUtils.htmlEscape(targetTree.getStorePath());
        }

        if (targetTree.getParentId() != 0) {

            return getWholePath(targetTree.getParentId(), flag) + File.separator + result;
        } else {

            return result;
        }
    }

    public ObjectNode treeList(String parentId, int pageSize, int pageNum) {

        ObjectNode objectNode = objectMapper.createObjectNode();
        String sql = " ORDER BY createDate DESC";

        ArrayNode arrayNode = objectMapper.createArrayNode();

        Map<String, Object> params = null;
        if (StringUtils.isNotBlank(parentId)) {
            sql = " WHERE parentId = :parentId " + sql;
            params = new HashMap<String, Object>();
            params.put("parentId", NumberUtils.toLong(parentId, 0));
        }

        Page<Tree> page = this.getPage(Tree.class, sql, params, pageSize, pageNum);

        List<Tree> treeList = page.getResultList();

        for (int i = 0; i < treeList.size(); i++) {
            Tree tree = treeList.get(i);
            String fullStorePath = "";

            if (tree.getParentId() != 0) {

                // get the treeNode's treeRoad and storePath(when someone update the db, its may be changed)
                tree.setTreeRoad(this.getTreeRoad(tree.getId()));
                fullStorePath = this.getStorePath(tree.getId());
            } else {
                tree.setTreeRoad(HtmlUtils.htmlEscape(tree.getTreeRoad()));
            }

            tree.setName(HtmlUtils.htmlEscape(tree.getName()));
            tree.setStorePath(HtmlUtils.htmlEscape(tree.getStorePath()));

            ObjectNode treeNode = objectMapper.valueToTree(tree);
            treeNode.put("fullStorePath", StringUtils.isNotEmpty(fullStorePath) ? fullStorePath : tree.getStorePath());

            arrayNode.add(treeNode);
        }

        objectNode.put("totalData", page.getTotalData());
        objectNode.put("data", arrayNode);
        objectNode.put("success", true);

        return objectNode;

    }

    public ObjectNode buildTreeNav() {

        ObjectNode objectNode = objectMapper.createObjectNode();

        List<Tree> trees = this.list(Tree.class, " WHERE status = " + Constant.COMMON_STATUS_ACCEPT);

        // no parent
        List<Tree> ancestors = new ArrayList<Tree>();
        // has parent and child
        Map<Long, List<Tree>> fatherMap = new HashMap<Long, List<Tree>>();
        // no child
        Map<Long, List<Tree>> childMap = new HashMap<Long, List<Tree>>();

        for (int i = 0; i < trees.size(); i++) {
            Tree tree = trees.get(i);
            List<Tree> treeList = null;

            tree.setName(HtmlUtils.htmlEscape(tree.getName()));

            //no parent
            if (tree.getParentId() == 0) {
                ancestors.add(tree);
            } else {

                // no child
                if (tree.getHasChild() == 0) {

                    if (childMap.containsKey(tree.getParentId())) {
                        treeList = childMap.get(tree.getParentId());
                    } else {
                        treeList = new ArrayList<Tree>();
                    }
                    treeList.add(tree);
                    childMap.put(tree.getParentId(), treeList);

                } else {

                    // has parent and child
                    if (fatherMap.containsKey(tree.getParentId())) {
                        treeList = fatherMap.get(tree.getParentId());
                    } else {
                        treeList = new ArrayList<Tree>();
                    }
                    treeList.add(tree);
                    fatherMap.put(tree.getParentId(), treeList);
                }

            }
        }

        ArrayNode rootArrayNode = objectMapper.createArrayNode();
        // ancestors loop
        for (int i = 0; i < ancestors.size(); i++) {

            Tree ancestor = ancestors.get(i);

            List<Tree> children = new ArrayList<Tree>();
            if (fatherMap.containsKey(ancestor.getId())) {
                children.addAll(fatherMap.get(ancestor.getId()));
            }

            if (childMap.containsKey(ancestor.getId())) {
                children.addAll(childMap.get(ancestor.getId()));
            }
            rootArrayNode.add(recursiveTree(ancestor, fatherMap, childMap));
        }

        objectNode.put("children", rootArrayNode);
        objectNode.put("success", true);

        return objectNode;

    }

    public ObjectNode recursiveTree(Tree ancestor, Map<Long, List<Tree>> fatherMap, Map<Long, List<Tree>> childrenMap) {

        List<Tree> children = new ArrayList<Tree>();

        if (fatherMap.containsKey(ancestor.getId())) {
            children.addAll(fatherMap.get(ancestor.getId()));
            fatherMap.remove(ancestor.getId());
        }

        if (childrenMap.containsKey(ancestor.getId())) {
            children.addAll(childrenMap.get(ancestor.getId()));
            childrenMap.remove(ancestor.getId());
        }

        ArrayNode childNode = objectMapper.createArrayNode();

        if (children.size() == 0) {

            return buildTreeNode(ancestor, false, null);
        } else {
            for (int i = 0; i < children.size(); i++) {
                childNode.add(recursiveTree(children.get(i), fatherMap, childrenMap));
            }
            return buildTreeNode(ancestor, true, childNode);
        }
    }

    public ObjectNode buildTreeNode(Tree tree, boolean father, ArrayNode childNode) {

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("text", tree.getName());
        if (!father) {
            objectNode.put("leaf", true);
        } else {
            objectNode.put("expanded", false);
            objectNode.put("children", childNode);
        }
        // ti = treeId
        objectNode.put("target", "articleList?treeId=" + tree.getId());
        return objectNode;
    }

}
