package com.core.service;

import com.core.repository.SecurityRepository;
import com.core.security.domain.*;
import com.core.util.Constant;
import com.core.util.DateUtil;
import com.core.util.EncryptUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by sunpeng
 */
@Service
public class RoleService extends BaseService {

    @Autowired
    private SecurityRepository securityRepository;

    public ObjectNode listAll() {

        ObjectNode resultNode = objectMapper.createObjectNode();

        ArrayNode arrayNode = objectMapper.createArrayNode();

        List<Role> resultList = list(Role.class);

        for (int i = 0; i < resultList.size(); i++) {

            Role role = resultList.get(i);

            ObjectNode roleNode = objectMapper.valueToTree(role);

            roleNode.put("key", roleNode.findValue("id"));
            roleNode.put("checked", false);

            roleNode.remove("md5");
            roleNode.remove("id");

            roleNode.put("children", listRoleRights(role.getId()));

            arrayNode.add(roleNode);
        }

        resultNode.put("children", arrayNode);

        resultNode.put("result", "success");
        resultNode.put("success", true);

        return resultNode;
    }

    public ArrayNode listRoleRights(long roleId) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("roleId", roleId);

        List<RoleRight> roleRights = list(RoleRight.class, " WHERE roleId = :roleId ", param);

        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (int i = 0; i < roleRights.size(); i++) {
            Right right = find(Right.class, roleRights.get(i).getRightId());
            ObjectNode objectNode = objectMapper.valueToTree(right);

            objectNode.put("key", objectNode.findValue("id"));
            objectNode.put("leaf", true);
            objectNode.put("checked", false);

            objectNode.remove("md5");
            objectNode.remove("id");

            arrayNode.add(objectNode);
        }

        return arrayNode;
    }

    public ObjectNode createRole(String name, String depict, String[] rightIds) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            Role role = new Role();

            role.setStatus(Constant.COMMON_STATUS_NORMAL);
            role.setName(name);
            role.setDepict(depict);
            role.setMd5(EncryptUtil.md5(name));
            role.setCreateDate(new Date());
            long roleId = baseRepository.create(role);

            for (int i = 0; i < rightIds.length; i++) {

                long rightId = NumberUtils.toLong(rightIds[i], 0);
                if (rightId == 0) {
                    continue;
                }
                Right right = find(Right.class, rightId);
                if (right == null || right.getId() == 0) {
                    continue;
                }

                RoleRight rr = new RoleRight();
                rr.setRoleId(roleId);
                rr.setRightId(rightId);
                rr.setCreateDate(new Date());

                baseRepository.create(rr);
            }

            log("角色创建", "roleId: " + roleId);

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateAddRight(String[] roleIds, String[] rightIds) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            for (int i = 0; i < roleIds.length; i++) {

                long id = NumberUtils.toLong(roleIds[i], 0);

                if (id == 0) {
                    continue;
                }

                Role role = find(Role.class, id);
                if (role == null || role.getId() == 0) {
                    continue;
                }

                for (int j = 0; j < rightIds.length; j++) {
                    long rightId = NumberUtils.toLong(rightIds[j]);
                    if (rightId == 0) {
                        continue;
                    }
                    Right right = find(Right.class, rightId);
                    if (right == null || right.getId() == 0) {
                        continue;
                    }
                    RoleRight exitRR = securityRepository.findRR(id, rightId);
                    if (exitRR == null || exitRR.getId() == 0) {

                        RoleRight rr = new RoleRight();
                        rr.setRightId(rightId);
                        rr.setRoleId(id);
                        rr.setCreateDate(new Date());
                        baseRepository.create(rr);
                    }
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("roleIds:").append(ArrayUtils.toString(roleIds)).
                    append("; rightIds:").append(ArrayUtils.toString(rightIds));
            log("角色权限添加", stringBuilder.toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode update(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("roleIds:[ ");

            while (iterator.hasNext()) {
                ObjectNode nextNode = (ObjectNode) iterator.next();
                nextNode.put("createDate", DateUtil.extDateFix(nextNode.findValue("createDate").getTextValue()));
                Role role = objectMapper.treeToValue(nextNode, Role.class);
                baseRepository.update(role);

                stringBuilder.append(role.getId()).append(" ");
            }

            log("角色更新", stringBuilder.append("]").toString());

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

            StringBuilder stringBuilder = new StringBuilder("roleIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                Role role = find(Role.class, id);
                role.setStatus(Constant.COMMON_STATUS_ACCEPT);
                baseRepository.update(role);

                stringBuilder.append(role.getId()).append(" ");
            }

            log("角色审核(启用)", stringBuilder.append("]").toString());

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

            StringBuilder stringBuilder = new StringBuilder("roleIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                Role role = find(Role.class, id);
                role.setStatus(Constant.COMMON_STATUS_REJECT);
                baseRepository.update(role);

                stringBuilder.append(role.getId()).append(" ");
            }

            log("角色审核(废弃)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode deleteRole(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();
            ArrayNode usingRoles = objectMapper.createArrayNode();

            StringBuilder stringBuilder = new StringBuilder("roleIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("roleId", id);

                List<UserRole> userRoles = list(UserRole.class, " WHERE roleId = :roleId ", param);
                if (userRoles == null || userRoles.size() == 0) {
                    continue;
                }
                ObjectNode roleNode = objectMapper.createObjectNode();
                Role role = find(Role.class, id);
                roleNode.put("depict", role.getDepict());

                StringBuffer sb = new StringBuffer();

                for (int i = 0; i < userRoles.size(); i++) {
                    UserRole ur = userRoles.get(i);
                    User user = find(User.class, ur.getUserId());
                    sb.append(user.getAccount());

                    if ((i + 1) < userRoles.size()) {
                        sb.append(" | ");
                    }
                }

                roleNode.put("users", sb.toString());
                usingRoles.add(roleNode);

            }

            if (usingRoles.size() == 0) {
                Iterator<JsonNode> iterator2 = jsonNode.iterator();
                while (iterator2.hasNext()) {
                    JsonNode next = iterator2.next();
                    long id = next.getLongValue();
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("roleId", id);

                    List<RoleRight> roleRights = list(RoleRight.class, " WHERE roleId = :roleId ", param);

                    for (int i = 0; i < roleRights.size(); i++) {
                        baseRepository.delete(RoleRight.class, roleRights.get(i).getId());
                    }
                    baseRepository.delete(Role.class, id);

                    stringBuilder.append(id).append(" ");
                }

                log("角色删除", stringBuilder.append("]").toString());

                resultNode.put("result", "success");
            } else {

                resultNode.put("feedback", usingRoles);
                resultNode.put("result", "using");
            }
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public ObjectNode deleteRoleRights(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Map map = objectMapper.readValue(jsonNode, Map.class);

            StringBuilder stringBuilder = new StringBuilder("rightIds:[ ");

            for (Object object : map.keySet()) {
                long roleId = NumberUtils.toLong((String) object, 0);
                if (roleId == 0) {
                    continue;
                }
                List rightIds = (List) map.get(object);
                for (int i = 0; i < rightIds.size(); i++) {
                    RoleRight rr = securityRepository.findRR(roleId, NumberUtils.toLong(rightIds.get(i).toString()));
                    if (rr != null) {
                        baseRepository.delete(RoleRight.class, rr.getId());

                        stringBuilder.append(rr.getId()).append(" ");
                    }
                }
            }

            log("角色权限删除", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode listByFilter(String filter) {
        ObjectNode objectNode = objectMapper.createObjectNode();

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("status", Constant.COMMON_STATUS_ACCEPT);
        String sql = " WHERE status = :status";
        if (StringUtils.isNotBlank(filter)) {

            param.put("depict", "%" + filter + "%");
            sql = sql + " AND depict LIKE :depict ";
        }

        List<Right> list = list(Right.class, sql, param);

        ArrayNode arrayNode = objectMapper.valueToTree(list);
        objectNode.put("data", arrayNode);
        objectNode.put("success", true);
        objectNode.put("result", "success");

        return objectNode;
    }

    public ObjectNode listRoleNoRights(String roleId, String str) {

        ObjectNode objectNode = objectMapper.createObjectNode();
        long id = NumberUtils.toLong(roleId, 0);
        if (id > 0) {
            List<Right> rights = securityRepository.listRoleNoRights(id, str);
            ArrayNode arrayNode = objectMapper.valueToTree(rights);
            objectNode.put("data", arrayNode);
        }

        objectNode.put("result", "success");
        objectNode.put("success", true);

        return objectNode;

    }

    public ObjectNode roleRightSearch(String[] roleIds, String str) {

        if (roleIds != null && roleIds.length == 1) {
            return this.listRoleNoRights(roleIds[0], str);
        } else {
            return this.listByFilter(str);
        }
    }

    public String updateCloneRole(String data) {

        try {

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            JsonNode jsonNode = objectMapper.readTree(data);
            if (jsonNode.size() == 0) {
                return objectNode.toString();
            }

            Role cloneRole = new Role();
            cloneRole.setName("cloneRole" + System.currentTimeMillis());
            cloneRole.setMd5(EncryptUtil.md5(cloneRole.getName()));
            cloneRole.setDepict("克隆角色");
            cloneRole.setStatus(Constant.COMMON_STATUS_ACCEPT);
            cloneRole.setCreateDate(new Date());
            cloneRole.setId(baseRepository.create(cloneRole));

            long roleId = 0;
            Iterator<JsonNode> iterator = jsonNode.iterator();
            while (iterator.hasNext()) {
                roleId = iterator.next().getLongValue();
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("roleId", roleId);
                List<RoleRight> roleRights = list(RoleRight.class, " WHERE roleId = :roleId ", param);
                for (int i = 0; i < roleRights.size(); i++) {

                    RoleRight roleRight = roleRights.get(i);
                    RoleRight cloneRoleRight = new RoleRight();
                    cloneRoleRight.setRoleId(cloneRole.getId());
                    cloneRoleRight.setRightId(roleRight.getRightId());
                    cloneRoleRight.setCreateDate(new Date());
                    baseRepository.create(cloneRoleRight);
                }
            }

            if (jsonNode.size() == 1) {
                Role role = find(Role.class, roleId);
                cloneRole.setDepict(role.getDepict() + "_" + "克隆" + cloneRole.getId());
                baseRepository.update(cloneRole);
            }

            log("角色克隆", data);

            return objectNode.toString();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }
}
