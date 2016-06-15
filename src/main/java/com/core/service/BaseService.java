package com.core.service;

import com.core.config.Config;
import com.core.repository.BaseRepository;
import com.core.repository.sqlBuilder.Page;
import com.core.security.SupportFactory;
import com.core.security.UserInfo;
import com.core.util.IpUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sunpeng on 2015/9/23.
 */
@Service
public class BaseService {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    protected BaseRepository baseRepository;

    @Autowired
    protected SupportFactory supportFactory;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected Config config;

    private int PAGE_SIZE_DEFAULT = 20;

    private int PAGE_NUM_DEFAULT = 1;

    public <T> List<T> list(Class<T> type) {
        return baseRepository.list(type, null, null);
    }

    public <T> List<T> list(Class<T> type, String sql) {
        return baseRepository.list(type, sql, null);
    }

    public <T> List<T> list(Class<T> type, String sql, Map<String, Object> param) {
        return baseRepository.list(type, sql, param);
    }

    public <T> T search(Class<T> type) {
        return search(type, null, null);
    }

    public <T> T search(Class<T> type, String sql) {
        return search(type, sql, null);
    }

    public <T> T search(Class<T> type, String sql, Map<String, Object> params) {
        return null;
    }

    public <T> T find(Class<T> type, long id) {

        T t = null;

        try {

            t = baseRepository.find(type, id);

            // } catch (EmptyResultDataAccessException e) {
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }

        return t;
    }

    public <T> int count(Class<T> type, String sql, Map<String, Object> params) {
        return baseRepository.count(type, sql, params);
    }

    public <T> int count(Class<T> type, String sql) {
        return baseRepository.count(type, sql, null);
    }

    public <T> int count(Class<T> type) {
        return baseRepository.count(type, null, null);
    }

    public <T> Page<T> getPage(Class<T> type, String sql, Map<String, Object> param, int pageSize, int pageNum) {

        Page<T> page = new Page<T>();

        if (pageNum > 0) {
            page.setPageNum(pageNum);
        } else {
            page.setPageNum(PAGE_NUM_DEFAULT);
        }

        if (pageSize > 0) {
            page.setPageSize(pageSize);
        } else {
            page.setPageSize(PAGE_SIZE_DEFAULT);
        }

        page.setTotalData(baseRepository.count(type, sql, param));

        pageNum = pageNum > page.getTotalPage() && page.getTotalPage() != 0 ? page.getTotalPage() : pageNum;

        page.setPageNum(pageNum);

        page.setResultList(baseRepository.pageList(type, sql, param, pageSize, pageNum));

        return page;
    }

    public void log(String action, String content) throws Exception {

        UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");
        if (userInfo == null || userInfo.getId() == 0) {
            userInfo = supportFactory.getSecuritySupport().getUserInfo();
        }

        com.core.security.domain.Log log = new com.core.security.domain.Log();
        log.setUserId(userInfo.getId());
        log.setName(userInfo.getAccount());
        log.setAction(action);
        log.setContent(content);
        log.setIp(IpUtil.getIp(request));
        log.setCreateDate(new Date());
        baseRepository.create(log);
    }

}
