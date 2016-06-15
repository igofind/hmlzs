package com.core.service;

import com.core.domain.Article;
import com.core.domain.Category;
import com.core.domain.SubArticle;
import com.core.domain.Template;
import com.core.repository.sqlBuilder.Page;
import com.core.security.UserInfo;
import com.core.util.Constant;
import com.core.util.DateUtil;
import com.core.util.VelocityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by sunpeng
 */
@Service
public class ArticleService extends BaseService {

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private HomepageService homepageService;

    public String getInitData() {

        List<Template> templates = list(Template.class, " WHERE status = " + Constant.COMMON_STATUS_ACCEPT);
        ObjectNode objectNode = objectMapper.createObjectNode();

        ArrayNode templateNodes = objectMapper.createArrayNode();
        for (int i = 0; i < templates.size(); i++) {

            Template template = templates.get(i);

            ArrayNode itemNode = objectMapper.createArrayNode();
            itemNode.add(template.getId());
            itemNode.add(template.getName());
            templateNodes.add(itemNode);
        }

        objectNode.put("templates", templateNodes);

        List<Category> categories = list(Category.class, " WHERE status = " + Constant.COMMON_STATUS_ACCEPT);
        ArrayNode categoryNodes = objectMapper.createArrayNode();
        for (int i = 0; i < categories.size(); i++) {

            Category category = categories.get(i);

            ArrayNode itemNode = objectMapper.createArrayNode();
            itemNode.add(category.getId());
            itemNode.add(category.getDepict());
            categoryNodes.add(itemNode);
        }

        objectNode.put("categories", categoryNodes);

        // TODO category
        return objectNode.toString();
    }

    public ObjectNode pageList(String treeId, int pageSize, int pageNum) {

        ObjectNode resultNode = objectMapper.createObjectNode();

        Map<String, Object> param = new HashMap<String, Object>();

        param.put("treeId", treeId);
        param.put("hidden", Constant.ARTICLE_STATUS_HIDDEN);

        Page<Article> page = getPage(Article.class, " WHERE treeId = :treeId AND status > :hidden ORDER BY id DESC", param, pageSize, pageNum);

        List<Article> resultList = page.getResultList();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (int i = 0; i < resultList.size(); i++) {
            Article article = resultList.get(i);

            article.setTitle(HtmlUtils.htmlEscape(article.getTitle()));
            article.setDepict(HtmlUtils.htmlEscape(article.getDepict()));

            param.clear();
            param.put("articleId", article.getId());
            List<SubArticle> subArticles = this.list(SubArticle.class, " WHERE articleId = :articleId ORDER BY seq ASC ", param);

            StringBuffer sb = new StringBuffer(article.getContent());

            for (int j = 0; j < subArticles.size(); j++) {
                sb.append(subArticles.get(j).getContent());
            }

            article.setContent(sb.toString());

            ObjectNode objectNode = objectMapper.valueToTree(article);

            arrayNode.add(objectNode);
        }

        resultNode.put("data", arrayNode);
        resultNode.put("success", true);

        return resultNode;
    }

    public ObjectNode createArticle(Article article) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            String[] content = buildContentArray(article.getContent());
            article.setContent(content[0]);

            // headLine
            if (article.getHeadLine() == 1) {
                article.setHeadLineDate(new Date());
            }

            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");
            article.setCreator(userInfo.getAccount());

            article.setCreateDate(new Date());
            long articleId = baseRepository.create(article);

            for (int i = 1; i < content.length; i++) {
                SubArticle subArticle = new SubArticle();
                subArticle.setContent(content[i]);
                subArticle.setArticleId(articleId);
                subArticle.setSeq(i);
                subArticle.setCreateDate(new Date());
                baseRepository.create(subArticle);
            }

            // Log in to the database.
            log("文章创建", "articleId: " + articleId);

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public String[] buildContentArray(String content) throws UnsupportedEncodingException {

        String targetStr = content;

        int size = targetStr.length() / Constant.ARTICLE_CONTENT_LENGTH;
        size = targetStr.length() % Constant.ARTICLE_CONTENT_LENGTH == 0 ? size : size + 1;

        String[] result = new String[size];

        for (int i = 0; i < size; i++) {
            result[i] = StringUtils.substring(targetStr, i * Constant.ARTICLE_CONTENT_LENGTH, (i + 1) * Constant.ARTICLE_CONTENT_LENGTH);
        }
        return result;
    }

    public ObjectNode updateArticle(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            // set user
            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");

            while (iterator.hasNext()) {
                ObjectNode next = (ObjectNode) iterator.next();

                next.put("createDate", DateUtil.extDateFix(next.findValue("createDate").getTextValue()));
                next.put("updateDate", DateUtil.extDateFix(next.findValue("updateDate").getTextValue()));
                next.put("publishDate", DateUtil.extDateFix(next.findValue("publishDate").getTextValue()));
                next.put("headLineDate", DateUtil.extDateFix(next.findValue("headLineDate").getTextValue()));
                next.put("deleteDate", DateUtil.extDateFix(next.findValue("deleteDate").getTextValue()));

                /* remove the additional field */
                next.remove("updater");
                next.remove("templateName");
                next.remove("categoryName");
                next.remove("treePath");
                next.remove("media");

                Article _article = objectMapper.treeToValue(next, Article.class);

                Article article = this.find(Article.class, _article.getId());

                article.setUpdater(userInfo.getAccount());

                article.setCategoryId(_article.getCategoryId());

                article.setTitle(_article.getTitle());
                article.setDepict(_article.getDepict());

                article.setAuthor(_article.getAuthor());
                article.setSource(_article.getSource());

                if (_article.getHeadLineOrder() > 0) {
                    article.setHeadLineOrder(_article.getHeadLineOrder());
                }

                article.setTemplateId(_article.getTemplateId());

                article.setUpdateDate(new Date());

                article.setId(baseRepository.update(article));

                stringBuilder.append(article.getId()).append(" ");

            }

            // Log in to database.
            log("文章更新(不包括内容)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateModifyArticle(Article _article) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            Article article = baseRepository.find(Article.class, _article.getId());

            article.setTitle(_article.getTitle());
            article.setDepict(_article.getDepict());

            article.setCategoryId(_article.getCategoryId());
            article.setTemplateId(_article.getTemplateId());

            article.setAuthor(_article.getAuthor());
            article.setSource(_article.getSource());

            // set user
            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");
            article.setUpdater(userInfo.getAccount());

            article.setUpdateDate(new Date());

            /* delete the old content */
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("articleId", article.getId());
            List<SubArticle> subArticles = baseRepository.list(SubArticle.class, " WHERE articleId = :articleId ", param);

            // baseRepository.execute("delete from " + SubArticle.getAppName()+ "_subarticle WHERE articleId = " + article.getId() , null);
            for (int i = 0; i < subArticles.size(); i++) {
                baseRepository.delete(SubArticle.class, subArticles.get(i).getId());
            }
            /* logically delete the old content */
            // baseRepository.execute("update " + SubArticle.getAppName()+ "_subarticle set status = -1 WHERE articleId = " + article.getId() , null);

            String[] content = buildContentArray(_article.getContent());

            article.setContent(content[0]);

            baseRepository.update(article);

            for (int i = 1; i < content.length; i++) {

                SubArticle subArticle = new SubArticle();
                subArticle.setContent(content[i]);
                subArticle.setArticleId(article.getId());
                subArticle.setSeq(i);
                subArticle.setCreateDate(new Date());
                baseRepository.create(subArticle);

            }

            log("文章更新(包括内容)", "articleId: " + article.getId());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.equals(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateAcceptArticle(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Article article = find(Article.class, id);
                    if (article.getStatus() != Constant.ARTICLE_STATUS_HIDDEN && article.getStatus() != Constant.COMMON_STATUS_ACCEPT) {
                        article.setStatus(Constant.COMMON_STATUS_ACCEPT);
                        article.setUpdater(userInfo.getAccount());
                        article.setUpdateDate(new Date());
                        article.setDeleteDate(null);
                        baseRepository.update(article);
                    } else {
                        continue;
                    }

                    stringBuilder.append(id).append(" ");
                }
            }

            log("文章审核(启用)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateRejectArticle(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Article article = find(Article.class, id);
                    if (article.getStatus() != Constant.ARTICLE_STATUS_HIDDEN && article.getStatus() != Constant.COMMON_STATUS_REJECT) {

                        // delete html
                        deleteStaticHtml(article.getId());
                        article.setUrl("");
                        article.setStatus(Constant.COMMON_STATUS_REJECT);
                        article.setUpdater(userInfo.getAccount());
                        article.setUpdateDate(new Date());
                        article.setDeleteDate(new Date());
                        baseRepository.update(article);
                    }
                    stringBuilder.append(id).append(" ");
                }
            }

            log("文章审核(作废)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode deleteArticle(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.put("result", "success");
            objectNode.put("success", true);

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {

                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("articleId", id);

                    List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleId = :articleId", param);
                    for (int i = 0; i < subArticles.size(); i++) {
                        baseRepository.delete(SubArticle.class, subArticles.get(i).getId());
                    }
                    deleteStaticHtml(id);
                    baseRepository.delete(Article.class, id);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("文章删除", stringBuilder.append("]").toString());

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public ObjectNode updateStaticize(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");
            Map<Long, String> navMap = homepageService.getNavMap();

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();

                Article article = this.find(Article.class, id);
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("articleId", article.getId());

                List<SubArticle> subArticles = this.list(SubArticle.class, " WHERE articleId = :articleId ORDER BY seq ASC ", param);

                StringBuffer sb = new StringBuffer();
                sb.append(article.getContent());

                for (int j = 0; j < subArticles.size(); j++) {
                    sb.append(subArticles.get(j).getContent());
                }

                article.setPublishDate(new Date());

                param.clear();
                Category category = find(Category.class, article.getCategoryId());

                if (category != null && navMap.containsKey(category.getId())) {
                    param.put("showCategory", true);
                    param.put("categoryUrl", navMap.get(category.getId()));
                    param.put("category", category.getDepict());
                }

                param.put("categoryId", category.getId());
                param.put("title", article.getTitle());
                param.put("content", sb.toString());
                param.put("publishDate", DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_YYMMDD));
                param.put("source", article.getSource());
                param.put("author", article.getAuthor());

                VelocityEngine engine = toolManager.getVelocityEngine();

                ToolContext context = toolManager.createContext();

                context.put("article", param);
                context.put("winTitle", article.getTitle() + Constant.WINDOW_TITLE_SUFFIX);
                context.put("depict", article.getDepict());

                context.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
                context.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

                PrintWriter pw = new PrintWriter(new File(VelocityUtil.getTargetFile(category.getName(), article.getId() + config.getArticleIdAddend())), Constant.ENCODE_UTF8);

                Template template = find(Template.class, article.getTemplateId());

                engine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, context, pw);

                pw.close();

                File file = new File(buildPreviewFilePath(article.getId() + config.getArticleIdAddend()));
                if (file.exists()) {
                    file.delete();
                }

                stringBuilder.append(id).append(" ");

                article.setUrl(VelocityUtil.getTargetUrl(config, category.getName(), article.getId() + config.getArticleIdAddend()));
                baseRepository.update(article);
            }

            log("文章静态化", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String updateHeadLine(String data, boolean flag) {

        try {
            ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(data);
            Iterator<JsonNode> iterator = arrayNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");
            UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.findValue("id").getLongValue();

                if (id > 0) {
                    Article article = find(Article.class, id);
                    if (flag) {
                        article.setHeadLineOrder(next.findValue("order").getIntValue());
                        article.setHeadLine(Constant.ARTICLE_HEADLINE);
                        article.setHeadLineDate(new Date());
                    } else {
                        article.setHeadLineOrder(0);
                        article.setHeadLine(Constant.ARTICLE_CANCEL_HEADLINE);
                        article.setHeadLineDate(null);
                    }
                    stringBuilder.append(id).append(" ");
                    article.setUpdater(userInfo.getAccount());
                    baseRepository.update(article);
                }
            }

            if (flag) {
                log("文章设置头条", stringBuilder.append(" ]").toString());
            } else {
                log("文章取消头条", stringBuilder.append(" ]").toString());
            }

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            return objectNode.toString();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

    public String preview(String data) {

        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            // StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            ArrayNode previewURLNode = objectMapper.createArrayNode();

            while (iterator.hasNext()) {
                JsonNode next = iterator.next();
                long id = next.getLongValue();

                Article article = this.find(Article.class, id);
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("articleId", article.getId());

                List<SubArticle> subArticles = this.list(SubArticle.class, " WHERE articleId = :articleId ORDER BY seq ASC ", param);

                StringBuffer sb = new StringBuffer();
                sb.append(article.getContent());

                for (int j = 0; j < subArticles.size(); j++) {
                    sb.append(subArticles.get(j).getContent());
                }

                article.setPublishDate(new Date());

                param.clear();
                Category category = find(Category.class, article.getCategoryId());

                param.put("categoryId", category.getId());// current category to nav
                param.put("category", category.getDepict());
                param.put("title", article.getTitle());
                param.put("content", sb.toString());
                param.put("publishDate", DateUtil.formatDate(new Date(), DateUtil.FORMAT_PATTERN_YYMMDD));
                param.put("source", article.getSource());
                param.put("author", article.getAuthor());

                VelocityEngine engine = toolManager.getVelocityEngine();

                ToolContext context = toolManager.createContext();

                context.put("article", param);
                context.put("winTitle", article.getTitle() + Constant.WINDOW_TITLE_SUFFIX);
                context.put("depict", article.getDepict());

                context.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
                context.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

                PrintWriter pw = new PrintWriter(this.buildPreviewFilePath(article.getId() + config.getArticleIdAddend()), Constant.ENCODE_UTF8);

                Template template = find(Template.class, article.getTemplateId());

                engine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, context, pw);

                pw.close();

                // stringBuilder.append(id).append(" ");
                previewURLNode.add(article.getId() + config.getArticleIdAddend());
            }

            // log("文章预览", stringBuilder.append("]").toString());

            objectNode.put("result", "preview");
            objectNode.put("targets", previewURLNode);
            objectNode.put("success", true);
            return objectNode.toString();

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String buildPreviewFilePath(long articleSerial) {

        String previewDir = System.getProperty("webapp.root") + File.separator + config.getPreviewDir();
        File file = new File(previewDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return previewDir + File.separator + articleSerial + ".html";
    }

    public String listStatic() {

        try {
            VelocityEngine velocityEngine = toolManager.getVelocityEngine();

            ToolContext context = toolManager.createContext();
            String[] listJsp = buildListPagePath();

            context.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            context.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            PrintWriter pw = new PrintWriter(listJsp[0], Constant.ENCODE_UTF8);

            Template articleListTemplate = find(Template.class, Constant.TEMPLATE_BUILD_IN_ARTICLELIST);
            velocityEngine.mergeTemplate(articleListTemplate.getPath(), Constant.ENCODE_UTF8, context, pw);
            pw.flush();

            pw = new PrintWriter(listJsp[1], Constant.ENCODE_UTF8);
            Template photoListTemplate = find(Template.class, Constant.TEMPLATE_BUILD_IN_PHOTOLIST);
            velocityEngine.mergeTemplate(photoListTemplate.getPath(), Constant.ENCODE_UTF8, context, pw);
            pw.flush();

            pw = new PrintWriter(buildSearchListPagePath(), Constant.ENCODE_UTF8);
            Template searchListTemplate = find(Template.class, Constant.TEMPLATE_BUILD_IN_SEARCHLIST);
            velocityEngine.mergeTemplate(searchListTemplate.getPath(), Constant.ENCODE_UTF8, context, pw);
            pw.close();

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            return objectNode.toString();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String[] buildListPagePath() {

        String basePath = System.getProperty("webapp.root") + File.separator + "WEB-INF" + File.separator + "admin" + File.separator;

        String[] listFiles = new String[2];

        listFiles[0] = basePath + "articleList.jsp";
        listFiles[1] = basePath + "photoList.jsp";

        return listFiles;
    }

    public String buildSearchListPagePath(){
        return System.getProperty("webapp.root") + File.separator + "WEB-INF" + File.separator + "search.jsp";
    }

    public void deleteStaticHtml(long articleId) {

        Article article = find(Article.class, articleId);
        Category category = find(Category.class, article.getCategoryId());
        File file = new File(VelocityUtil.getTargetFile(category.getName(), article.getId() + config.getArticleIdAddend()));

        if (file.exists()) {
            file.delete();
        }
    }

    public String getArticleConetnt(Article article) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("articleId", article.getId());

        List<SubArticle> subArticles = this.list(SubArticle.class, " WHERE articleId = :articleId ORDER BY seq ASC ", param);
        StringBuffer sb = new StringBuffer();
        sb.append(article.getContent());

        for (int j = 0; j < subArticles.size(); j++) {
            sb.append(subArticles.get(j).getContent());
        }
        return sb.toString();
    }
}
