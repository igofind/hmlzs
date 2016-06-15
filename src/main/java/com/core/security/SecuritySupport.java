package com.core.security;

import com.core.repository.SecurityRepository;
import com.core.security.domain.Admin;
import com.core.security.domain.Session;
import com.core.security.domain.User;
import com.core.util.Constant;
import com.core.util.EncryptUtil;
import com.core.util.IpUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunpeng
 */
public class SecuritySupport {

    private Log logger = LogFactory.getLog(this.getClass());

    private final static String TOOKEN_COOKIE_NAME = "_sn_";

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private HttpServletRequest request;

    public SecuritySupport() {
    }

    public ObjectNode login(HttpServletResponse response) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            String account = request.getParameter("account");
            String pwd = request.getParameter("pwd");

            String result = "failed";
            boolean isCheck = true;

            ObjectNode msg = objectMapper.createObjectNode();

            if (StringUtils.isBlank(account)) {
                msg.put("account", "please input a account!");
                isCheck = false;
            }
            if (StringUtils.isBlank(pwd)) {
                msg.put("pwd", "please input your password!");
                isCheck = false;
            }
            if (isCheck) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("account", account);
                param.put("pwd", EncryptUtil.md5(pwd));
                param.put("status", Constant.COMMON_STATUS_ACCEPT);

                User user = findAccount(User.class, param);
                if (user != null && user.getId() != 0) {
                    createSession(response, user.getAccount(), user.getId());
                    user.setLastLoginDate(new Date());
                    securityRepository.update(user);
                    result = "success";

                    log(user.getId(), user.getAccount(), "登录", "");
                } else {
                    Admin admin = findAccount(Admin.class, param);
                    if (admin != null && admin.getId() != 0) {
                        createSession(response, admin.getAccount(), admin.getId());

                        admin.setLastLoginDate(new Date());
                        securityRepository.update(admin);
                        result = "success";

                        log(admin.getId(), admin.getAccount(), "登录", "");
                    } else {
                        result = "failed";
                        msg.put("failed", "用户名或密码错误!");
                    }
                }
            }

            objectNode.put("result", result);
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {

            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public void logout(HttpServletResponse response) {

        try {
            String cookieValue = getCookie(TOOKEN_COOKIE_NAME);

            Session session = findSession(cookieValue);
            securityRepository.delete(Session.class, session.getId());

            if (StringUtils.isNotBlank(cookieValue)) {
                Cookie cookie = new Cookie(TOOKEN_COOKIE_NAME, cookieValue);
                cookie.setPath("/");
                cookie.setDomain(request.getServerName());
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    public UserInfo getUserInfo() {

        Session session = recognize();
        UserInfo userInfo = null;
        if (session != null) {
            userInfo = getUserInfo(session.getUserId(), session.getAccount(), null);
        }

        return userInfo;
    }

    public UserInfo getUserInfo(long id, String account, String pwd) {
        UserInfo userInfo = new UserInfo();

        Map<String, Object> param = new HashMap<String, Object>();

        if (id > 0) {
            param.put("id", id);
        }
        if (StringUtils.isNotBlank(pwd)) {
            param.put("pwd", pwd);
        }

        param.put("account", account);
        param.put("status", Constant.COMMON_STATUS_ACCEPT);

        User user = findAccount(User.class, param);

        if (user != null && user.getId() != 0) {

            userInfo.setId(user.getId());
            userInfo.setAccount(user.getAccount());
            userInfo.setName(user.getName());
            userInfo.setPhone(user.getPhone());
            userInfo.setMail(user.getMail());
            userInfo.setDepict(user.getDepict());
            userInfo.setCreateDate(user.getCreateDate());
            userInfo.setUpdateDate(user.getUpdateDate());
            userInfo.setLastLoginDate(user.getLastLoginDate());
            userInfo.setIsAdmin(false);
            if (user.getManager() == Constant.USER_MANAGER_UPGRADE) {
                userInfo.setIsManager(true);
            }

        } else {

            Admin admin = findAccount(Admin.class, param);

            if (admin != null && admin.getId() != 0) {
                userInfo.setAccount(admin.getAccount());
                userInfo.setId(admin.getId());
                userInfo.setName(admin.getName());
                userInfo.setPhone(admin.getPhone());
                userInfo.setMail(admin.getMail());
                userInfo.setDepict(admin.getDepict());
                userInfo.setCreateDate(admin.getCreateDate());
                userInfo.setUpdateDate(admin.getUpdateDate());
                userInfo.setLastLoginDate(admin.getLastLoginDate());
                userInfo.setIsAdmin(true);
                userInfo.setIsManager(true);
            }
        }

        return userInfo;
    }

    private <T> T findAccount(Class<T> tClass, Map<String, Object> param) {

        StringBuffer sql = new StringBuffer();
        T t = null;
        try {
            t = tClass.newInstance();

            if (param != null && param.size() != 0) {
                int size = param.size();
                int count = 0;
                sql.append(" WHERE");
                for (String key : param.keySet()) {
                    count++;
                    sql.append(" ");
                    sql.append(key);
                    sql.append("= :");
                    sql.append(key);
                    if (count < size) {
                        sql.append(" AND");
                    }
                }
            }

            List<T> list = securityRepository.list(tClass, sql.toString(), param);
            if (list != null && list.size() != 0) {
                t = list.get(0);
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
        return t;
    }

    public boolean hasRight(long userId, Method method) throws Exception {

        return securityRepository.hasRight(userId, method);
    }

    @Transactional
    public void createSession(HttpServletResponse response, String account, long id) {

        String sessionKey = EncryptUtil.md5(account + ":" + System.currentTimeMillis());
        try {

            Session session = new Session();
            session.setLoginDate(new Date());
            // session.setExpireDate();
            // TODO
            session.setSessionKey(sessionKey);
            session.setAccount(account);

            session.setStatus(Constant.COMMON_STATUS_NORMAL);
            session.setUserId(id);

            securityRepository.create(session);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            throw new RuntimeException("");
        }

        Cookie cookie = new Cookie(TOOKEN_COOKIE_NAME, sessionKey);
        // cookie.setSecure(true);
        cookie.setDomain(request.getServerName());
        cookie.setPath("/");

        response.addCookie(cookie);

    }

    public Session findSession(String key) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("sessionKey", key);
        param.put("status", Constant.COMMON_STATUS_NORMAL);

        List<Session> list = securityRepository.list(Session.class, " WHERE sessionKey = :sessionKey AND status = :status ", param);
        if (list != null && list.size() != 0) {
            return list.get(0);
        }

        return null;
    }

    private Session recognize() {

        String sessionKey = getCookie(TOOKEN_COOKIE_NAME);
        Session session = null;

        if (StringUtils.isNotBlank(sessionKey)) {

            session = findSession(sessionKey);
        }

        return session;
    }

    private String getCookie(String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0, c = cookies.length; i < c; ++i) {
                Cookie cookie = cookies[i];
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void log(long id,String account, String action, String content) throws Exception {

        com.core.security.domain.Log log = new com.core.security.domain.Log();
        log.setUserId(id);
        log.setName(account);
        log.setAction(action);
        log.setContent(content);
        log.setIp(IpUtil.getIp(request));
        log.setCreateDate(new Date());
        securityRepository.create(log);
    }
}
