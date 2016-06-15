package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "文章检索管理者")
@Controller
@RequestMapping("/admin")
public class SearchController extends BaseController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private HttpServletRequest request;

    @AsRight(id = 1, depict = "文章检索页面访问")
    @RequestMapping(value = "/articleSearch", method = RequestMethod.GET)
    public String showPageNR() {

        request.setAttribute("initData", searchService.getInitData());
        return getView("articleSearch");
    }

    @AsRight(id = 2, depict = "文章检索页面数据查询")
    @ResponseBody
    @RequestMapping(value = "/search/search", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String searchNR(int limit, int page, String title, String category, String focus,String headLine, String status, String creator, String createDate, String updateDate, String publishDate) {

        return searchService.searchArticle(limit, page, title, category, focus,headLine, status, creator, createDate, updateDate, publishDate);
    }

}
