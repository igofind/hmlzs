package com.core.controller;

import com.core.config.Config;
import com.core.service.ListService;
import com.core.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created by sunpeng
 */
@Controller
@RequestMapping("/")
public class ListController extends BaseController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ListService listService;

    /**
     * 文章列表页面
     *
     * @param page
     * @param cid
     * @return
     */
    @RequestMapping(value = "/c-{cid}-{page}.html", method = RequestMethod.GET)
    public String articleList(@ModelAttribute("page") int page,
                              @ModelAttribute("cid") String cid) {

        Object[] result = listService.articleList(page, cid);

        request.setAttribute("list", result[1]);
        request.setAttribute("listUrl", "\"" + config.getListDomain() + String.format(config.getListParam() + "\"", cid));
        request.setAttribute("winTitle", result[2]);


        request.setAttribute(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
        request.setAttribute(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

        return getView((String) result[0]);
    }

}