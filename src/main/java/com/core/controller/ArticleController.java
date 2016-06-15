package com.core.controller;

import com.core.config.Config;
import com.core.domain.Article;
import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.ArticleIndexService;
import com.core.service.ArticleService;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * Created by sunpeng
 * <p/>
 * NR == NeedRight
 */
@RightCheck(depict = "文章管理者")
@Controller
@RequestMapping("/admin")
public class ArticleController extends BaseController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleIndexService articleIndexService;

    @Autowired
    private HttpServletRequest request;

    @AsRight(id = 1, depict = "文章列表页面访问")
    @RequestMapping("/articleList")
    public String showPageNR(String treeId) {

        request.setAttribute("treeId", treeId);
        request.setAttribute("initData", articleService.getInitData());
        return getView("article");
    }

    @AsRight(id = 2, depict = "文章列表数据查询")
    @ResponseBody
    @RequestMapping(value = "/article/list", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listNR(String treeId, int pageSize, int page) {

        return articleService.pageList(treeId, pageSize, page).toString();
    }

    @AsRight(id = 3, depict = "文章创建")
    @ResponseBody
    @RequestMapping(value = "/article/create", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String createArticleNR(Article article) {

        return articleService.createArticle(article).toString();
    }

    @AsRight(id = 4, depict = "文章更新(不包括内容)")
    @ResponseBody
    @RequestMapping(value = "/article/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateArticleNR(String data) {

        return articleService.updateArticle(data).toString();
    }

    @AsRight(id = 5, depict = "文章更新(包括内容)")
    @ResponseBody
    @RequestMapping(value = "/article/modify", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String modifyArticleNR(Article article, boolean autoStatic) {

        return articleService.updateModifyArticle(article).toString();
    }

    @AsRight(id = 6, depict = "文章审核(启用)")
    @ResponseBody
    @RequestMapping(value = "/article/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptArticleNR(String data) {

        return articleService.updateAcceptArticle(data).toString();
    }

    @AsRight(id = 7, depict = "文章审核(作废)")
    @ResponseBody
    @RequestMapping(value = "/article/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectArticleNR(String data) {

        return articleService.updateRejectArticle(data).toString();
    }

    @AsRight(id = 8, depict = "文章删除")
    @ResponseBody
    @RequestMapping(value = "/article/delete", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String deleteArticleNR(String data) {
        articleIndexService.deleteArticle(data);
        return articleService.deleteArticle(data).toString();
    }

    @AsRight(id = 9, depict = "文章静态化")
    @ResponseBody
    @RequestMapping("/article/staticize")
    public String staticizeNR(String data) {
        String result = articleService.updateStaticize(data).toString();
        articleIndexService.updateStaticize(data, articleService);

        return result;
    }

    @AsRight(id = 10, depict = "文章设置头条")
    @ResponseBody
    @RequestMapping(value = "/article/headLine", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateHeadLineNR(String data) {

        return articleService.updateHeadLine(data, true);
    }

    @AsRight(id = 11, depict = "文章取消头条")
    @ResponseBody
    @RequestMapping(value = "/article/cancelHeadLine", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateCancelHeadLineNR(String data) {

        return articleService.updateHeadLine(data, false);
    }

    @AsRight(id = 12, depict = "文章预览")
    @ResponseBody
    @RequestMapping(value = "/article/preview", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String previewNR(String data) {
        return articleService.preview(data);
    }

    @RequestMapping(value = "/article/previewPage", method = RequestMethod.GET)
    public String showPreviewPageNR(String target) {

        File file = new File(articleService.buildPreviewFilePath(NumberUtils.toLong(target, 0)));

        if (file.exists()) {

            request.setAttribute("target", target + ".html");
            return getView("preview");
        } else {

            // 404
            return getViewRedirect("404");
        }
    }

    @AsRight(id = 13, depict = "文章列表模板静态化")
    @ResponseBody
    @RequestMapping(value = "/article/listStatic", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listStaticNR() {
        return articleService.listStatic();
    }

}