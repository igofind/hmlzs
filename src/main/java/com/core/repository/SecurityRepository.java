package com.core.repository;

import com.core.security.RightBuilder;
import com.core.security.domain.Right;
import com.core.security.domain.Role;
import com.core.security.domain.RoleRight;
import com.core.util.Constant;
import com.core.util.EncryptUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunpeng
 */
@Repository
public class SecurityRepository extends BaseRepository {

    protected Log logger = LogFactory.getLog(this.getClass());

    public Role findRole(Class<?> clazz){

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("md5", EncryptUtil.md5(clazz.getName()));

        List<Role> list = this.list(Role.class, " WHERE md5 = :md5", param);
        if (list != null && list.size() != 0) {
            return list.get(0);
        }

        return null;
    }

    public Right findRight(Method declaredMethod){

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("md5", RightBuilder.buildRightKey(declaredMethod));

        List<Right> list = this.list(Right.class, " WHERE md5 = :md5 ", param);
        if (list != null && list.size() != 0) {
            return list.get(0);
        }

        return null;
    }

    public boolean hasRight(long userId, Method method){

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);

        Right right = findRight(method);

        // The right has been cancelled.
        if (right.getStatus() == Constant.COMMON_STATUS_REJECT) {
            return true;
        }

        param.put("rightId", right.getId());

        // SELECT COUNT(1) FROM cms_user u, cms_userrole ur, cms_role ro,cms_roleright rr, cms_right ri
        // WHERE u.id = :userId AND u.id = ur.userid AND ur.roleid = ro.id AND ro.status = 1 AND ro.id = rr.roleid AND rr.rightid = ri.id AND ri.id = :rigthId;
        String sql = "SELECT COUNT(1) FROM " +
                config.getPLATFORM() + "_user u, " +
                config.getPLATFORM() + "_userrole ur, " +
                config.getPLATFORM() + "_role ro, " +
                config.getPLATFORM() + "_roleright rr, " +
                config.getPLATFORM() + "_right ri " +
                "WHERE u.id = :userId AND " +
                "u.id = ur.userid AND " +
                "ur.roleid = ro.id AND " +
                "ro.status = 1 AND " +
                "ro.id = rr.roleid AND " +
                "rr.rightid = ri.id AND " +
                "ri.id = :rightId ";

        Integer count = jdbcTemplate.queryForObject(sql, param, Integer.class);

        return (count > 0);
    }

    public RoleRight findRR(long roleId, long rightId){

        RoleRight roleRight = null;

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("roleId", roleId);
        param.put("rightId", rightId);

        List<RoleRight> roleRights = this.list(RoleRight.class, " WHERE roleId = :roleId AND rightId = :rightId ", param);

        if (roleRights != null && roleRights.size() != 0) {
            roleRight = roleRights.get(0);
        }

        return roleRight;
    }

    public List<Right> listRoleRights(long roleId) {
        Field[] declaredFields = Right.class.getDeclaredFields();

        StringBuffer sqlBuf = new StringBuffer("SELECT ");

        String rightTableAlias = " r";
        String roleRightTableAlias = " rr";
        String rightDot = " " + rightTableAlias + ".";
        String roleRightDot = " " + roleRightTableAlias + ".";

        int count = 0;

        for (int i = 0; i < declaredFields.length; i++) {

            Field field = declaredFields[i];
            String fieldName = rightDot + field.getName().toLowerCase() + " AS " + field.getName();

            sqlBuf.append(fieldName);

            if (++count != declaredFields.length) {
                sqlBuf.append(",");
            }
        }

        sqlBuf.append(" FROM ")
                .append(Right.getAppName() + "_" + Right.class.getSimpleName().toLowerCase())
                .append(rightTableAlias)
                .append(",")
                .append(Right.getAppName() + "_" + RoleRight.class.getSimpleName().toLowerCase())
                .append(roleRightTableAlias)
                .append(" WHERE ")
                .append(roleRightDot + "rightid =")
                .append(rightDot + "id" + " AND ")
                .append(roleRightDot + "roleid = :roleId");

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("roleId", roleId);

        return jdbcTemplate.query(sqlBuf.toString(), param, new BeanPropertyRowMapper<Right>(Right.class));
    }

    public List<Right> listRoleNoRights(long roleId, String str){

        List<Right> exitRights = listRoleRights(roleId);
        List<String> exitRightIds = new ArrayList<String>();

        String sql = "";
        Map<String, Object> param = null;

        if (StringUtils.isNotBlank(str)) {
            param = new HashMap<String, Object>();
            param.put("depict", "%" + str + "%");

            sql = " WHERE depict LIKE :depict ";
        }

        List<Right> allRight = list(Right.class, sql, param);

        for (int i = 0; i < exitRights.size(); i++) {
            exitRightIds.add(exitRights.get(i).getMd5());
        }

        List<Right> result = new ArrayList<Right>();

        for (int i = 0; i < allRight.size(); i++) {
            if (!exitRightIds.contains(allRight.get(i).getMd5())) {
                result.add(allRight.get(i));
            }
        }

        return result;
    }

}
