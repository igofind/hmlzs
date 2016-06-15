package com.core.controller;

import com.core.config.Config;
import com.core.repository.BaseRepository;
import com.core.security.SupportFactory;
import com.core.util.Constant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sunpeng
 */
public class BaseController {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected BaseRepository baseRepository;

    @Autowired
    protected SupportFactory supportFactory;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected Config config;

    protected Log logger = LogFactory.getLog(this.getClass());

    /**
     * get the view name(path)
     *
     * @param viewName
     * @return
     */
    public String getView(String viewName) {

        request.setAttribute(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
        request.setAttribute(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

        return "admin/" + viewName;
    }

    /**
     * get the redirect view name(path)
     *
     * @param viewName
     * @return
     */
    public String getViewRedirect(String viewName) {
        return "redirect:" + viewName;
    }

}
