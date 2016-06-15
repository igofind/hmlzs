package com.core.service;

import com.core.repository.sqlBuilder.Page;
import com.core.security.SecuritySupport;
import com.core.security.UserInfo;
import com.core.security.domain.Admin;
import com.core.security.domain.Role;
import com.core.security.domain.User;
import com.core.security.domain.UserRole;
import com.core.util.Constant;
import com.core.util.EncryptUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by sunpeng
 */
@Service
public class AccountService extends BaseService {


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ObjectNode createAccount(User user) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.put("success", true);

            User account = findAccount(user.getAccount());
            if (account != null && account.getId() > 0) {
                objectNode.put("result", "exit");
                return objectNode;
            }

            user.setPwd(EncryptUtil.md5(config.getInitialPwd()));
            user.setStatus(Constant.COMMON_STATUS_REJECT);
            // user is not manager
            user.setManager(Constant.USER_MANAGER_DEGRADE);
            user.setCreateDate(new Date());

            user.setId(baseRepository.create(user));

            // Log in to the database
            log("账户创建", user.toString());

            objectNode.put("result", "success");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public User findAccount(String account) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("account", account);

        List<User> list = list(User.class, " WHERE account = :account ", param);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);

    }

    public ObjectNode listAll(int pageSize, int pageNum) {

        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("success", true);
        Page<User> userPage = this.getPage(User.class, " ORDER BY createDate DESC ", null, pageSize, pageNum);

        List<User> resultList = userPage.getResultList();

        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (int i = 0; i < resultList.size(); i++) {

            User user = resultList.get(i);

            ObjectNode objectNode = objectMapper.valueToTree(user);
            // remove the pwd node
            objectNode.remove("pwd");

            List<Role> roles = listUserRoles(String.valueOf(user.getId()));
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < roles.size(); j++) {
                sb.append(roles.get(j).getDepict());
                if ((j + 1) < roles.size()) {
                    sb.append(",");
                }
            }

            objectNode.put("roles", sb.toString());
            arrayNode.add(objectNode);
        }

        resultNode.put("result", "success");
        resultNode.put("data", arrayNode);
        resultNode.put("totalData", userPage.getTotalData());

        return resultNode;
    }

    public ObjectNode updateAcceptAccount(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();
            resultNode.put("success", true);

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("ids: [ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                User user = baseRepository.find(User.class, id);

                user.setUpdateDate(new Date());
                user.setStatus(Constant.COMMON_STATUS_ACCEPT);
                baseRepository.update(user);

                stringBuilder.append(user.getId()).append(" ");
            }

            // Log in to the database
            log("账户解锁", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateRejectAccount(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("ids: [ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                User user = baseRepository.find(User.class, id);
                user.setUpdateDate(new Date());

                user.setStatus(Constant.COMMON_STATUS_REJECT);
                baseRepository.update(user);

                stringBuilder.append(user.getId()).append(" ");
            }

            // Log in to the database
            log("账户锁定", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateInfo(User user) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");

            if (userInfo.isAdmin()) {

                Admin admin = find(Admin.class, userInfo.getId());

                admin.setName(user.getName());
                admin.setPhone(user.getPhone());
                admin.setMail(user.getMail());
                admin.setDepict(user.getDepict());

                admin.setUpdateDate(new Date());
                baseRepository.update(admin);

                log("账户信息更新", admin.toString());

            } else {
                User _user = find(User.class, userInfo.getId());

                _user.setName(user.getName());
                _user.setPhone(user.getPhone());
                _user.setMail(user.getMail());
                _user.setDepict(user.getDepict());

                _user.setUpdateDate(new Date());
                baseRepository.update(_user);

                log("账户信息更新", _user.toString());
            }

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updatePwd(String oldPwd, String pwd, String cfrmPwd) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            if (!StringUtils.equals(pwd, cfrmPwd)) {

                objectNode.put("result", "different");
                return objectNode;
            }

            if (!Pattern.matches("(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,20}", pwd)) {
                objectNode.put("result", "simple");
                return objectNode;
            }

            SecuritySupport securitySupport = supportFactory.getSecuritySupport();

            String oldPwdMd5 = EncryptUtil.md5(oldPwd);

            UserInfo userInfo = securitySupport.getUserInfo();

            // validate the old password
            userInfo = securitySupport.getUserInfo(userInfo.getId(), userInfo.getAccount(), oldPwdMd5);

            if (userInfo != null && userInfo.getId() != 0) {
                if (userInfo.isAdmin()) {
                    Admin admin = find(Admin.class, userInfo.getId());
                    admin.setPwd(EncryptUtil.md5(pwd));
                    admin.setUpdateDate(new Date());
                    baseRepository.update(admin);

                    log("账户密码修改", admin.toString());
                } else {
                    User user = find(User.class, userInfo.getId());

                    user.setPwd(EncryptUtil.md5(pwd));
                    user.setUpdateDate(new Date());
                    baseRepository.update(user);

                    log("账户密码修改", user.toString());
                }
                objectNode.put("result", "success");
            } else {
                objectNode.put("result", "wrongpwd");
            }
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public void deleteUserRoles(long userId) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);

        List<UserRole> list = list(UserRole.class, " WHERE userId = :userId ", param);

        for (int i = 0; i < list.size(); i++) {
            baseRepository.delete(UserRole.class, list.get(i).getId());
        }

    }

    public ObjectNode listUsersRoles4Filter2(String[] accountId, String str) {

        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("success", true);

        List<Role> roleList = listRoles4Filter(str);
        ArrayNode arrayNode = objectMapper.createArrayNode();
        HashMap<Long, Role> userRolesMap = new HashMap<Long, Role>();

        if (accountId != null && accountId.length == 1) {
            List<Role> userHasRole = listUserRoles(accountId[0]);

            for (int i = 0; i < userHasRole.size(); i++) {

                userRolesMap.put(userHasRole.get(i).getId(), userHasRole.get(i));
            }
        }

        for (int i = 0; i < roleList.size(); i++) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            Role role = roleList.get(i);
            objectNode.put("id", role.getId());
            objectNode.put("depict", role.getDepict());

            if (userRolesMap.containsKey(role.getId())) {
                objectNode.put("userHas", true);
                userRolesMap.remove(role.getId());
            } else {
                objectNode.put("userHas", false);
            }

            arrayNode.add(objectNode);
        }

        for (Long aLong : userRolesMap.keySet()) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            Role role = userRolesMap.get(aLong);
            objectNode.put("id", role.getId());
            objectNode.put("depict", role.getDepict());
            objectNode.put("userHas", true);
            arrayNode.add(objectNode);
        }

        resultNode.put("data", arrayNode);
        return resultNode;
    }

    public List<Role> listRoles4Filter(String filter) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("status", Constant.COMMON_STATUS_ACCEPT);
        String sql = " WHERE status = :status";
        if (StringUtils.isNotBlank(filter)) {

            param.put("depict", "%" + filter + "%");
            sql = sql + " AND depict LIKE :depict ";
        }

        return list(Role.class, sql, param);
    }

    public ObjectNode createUserRoles(String[] accountIds, String[] roleIds) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            if (accountIds == null || roleIds == null) {
                return resultNode;
            }

            if (accountIds.length == 1) {
                long id = NumberUtils.toLong(accountIds[0], 0);

                User user = find(User.class, id);
                deleteUserRoles(user.getId());

                if (user != null && user.getId() > 0) {
                    for (int i = 0; i < roleIds.length; i++) {
                        long roleId = NumberUtils.toLong(roleIds[i]);
                        Role role = find(Role.class, roleId);

                        if (role == null || role.getId() == 0) {
                            continue;
                        }

                        UserRole exitUR = this.findUR(id, roleId);
                        if (exitUR == null || exitUR.getId() == 0) {

                            UserRole ur = new UserRole();
                            ur.setUserId(id);
                            ur.setRoleId(roleId);
                            ur.setCreateDate(new Date());
                            baseRepository.create(ur);
                        }

                    }

                } else {
                    return resultNode;
                }

            } else {
                for (int i = 0; i < accountIds.length; i++) {

                    long id = NumberUtils.toLong(accountIds[i], 0);

                    if (id == 0) {
                        continue;
                    }

                    User user = find(User.class, id);
                    if (user == null || user.getId() == 0) {
                        continue;
                    }

                    for (int j = 0; j < roleIds.length; j++) {
                        long roleId = NumberUtils.toLong(roleIds[j]);
                        Role role = find(Role.class, roleId);

                        if (role == null || role.getId() == 0) {
                            continue;
                        }
                        UserRole exitUR = this.findUR(id, roleId);
                        if (exitUR == null || exitUR.getId() == 0) {

                            UserRole ur = new UserRole();
                            ur.setUserId(id);
                            ur.setRoleId(roleId);
                            ur.setCreateDate(new Date());
                            baseRepository.create(ur);
                        }
                    }
                }
            }

            // Log in to the database
            String content = "accountIds:" + ArrayUtils.toString(accountIds) + "; roleIds:" + ArrayUtils.toString(roleIds);
            log("账户:角色添加", content);

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public List<Role> listUserRoles(String userId) {

        long id = NumberUtils.toLong(userId, 0);

        if (id <= 0) {
            return null;
        }

        Field[] declaredFields = Role.class.getDeclaredFields();

        StringBuffer sqlBuf = new StringBuffer("SELECT ");

        String roleTableAlias = " r";
        String userRoleTableAlias = " ur";
        String roleDot = " " + roleTableAlias + ".";
        String userRoleDot = " " + userRoleTableAlias + ".";

        int count = 0;

        for (int i = 0; i < declaredFields.length; i++) {

            Field field = declaredFields[i];
            String fieldName = roleDot + field.getName().toLowerCase() + " AS " + field.getName();

            sqlBuf.append(fieldName);

            if (++count != declaredFields.length) {
                sqlBuf.append(",");
            }
        }

        sqlBuf.append(" FROM ")
                .append(Role.getAppName() + "_" + Role.class.getSimpleName().toLowerCase())
                .append(roleTableAlias)
                .append(",")
                .append(Role.getAppName() + "_" + UserRole.class.getSimpleName().toLowerCase())
                .append(userRoleTableAlias)
                .append(" WHERE ")
                .append(userRoleDot + "roleid =")
                .append(roleDot + "id" + " AND ")
                .append(userRoleDot + "userid = :userId");

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);

        return namedParameterJdbcTemplate.query(sqlBuf.toString(), param, new BeanPropertyRowMapper<Role>(Role.class));
    }

    public UserRole findUR(long userId, long roleId) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);
        param.put("roleId", roleId);

        List<UserRole> userRoles = this.list(UserRole.class, " WHERE userId = :userId AND roleId = :roleId ", param);

        if (userRoles != null && userRoles.size() != 0) {
            return userRoles.get(0);
        } else {
            return null;
        }
    }

    public ObjectNode updateUpgrade(String data) {

        try {

            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("accountIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                User user = baseRepository.find(User.class, id);

                user.setManager(Constant.USER_MANAGER_UPGRADE);

                user.setUpdateDate(new Date());
                baseRepository.update(user);

                stringBuilder.append(user.getId()).append(" ");
            }

            log("账户升级", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {

            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public ObjectNode updateDegrade(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();
            StringBuilder stringBuilder = new StringBuilder("accountIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                User user = baseRepository.find(User.class, id);

                user.setManager(Constant.USER_MANAGER_DEGRADE);

                user.setUpdateDate(new Date());
                baseRepository.update(user);

                stringBuilder.append(user.getId()).append(" ");
            }

            log("账户降级", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateResetPwd(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();
            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("accountIds:[ ");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();
                User user = baseRepository.find(User.class, id);

                user.setPwd(EncryptUtil.md5(config.getInitialPwd()));

                user.setUpdateDate(new Date());
                baseRepository.update(user);

                stringBuilder.append(user.getId()).append(" ");
            }

            resultNode.put("result", "success");
            resultNode.put("success", true);

            log("账户密码重置", stringBuilder.append("]").toString());

            return resultNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }
}
