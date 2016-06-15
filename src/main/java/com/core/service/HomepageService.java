package com.core.service;

import com.core.domain.*;
import com.core.util.Constant;
import com.core.util.DateUtil;
import com.core.util.VelocityUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by sunpeng
 */
@Service
public class HomepageService extends BaseService {

    @Autowired
    private ToolManager toolManager;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private MediaService mediaService;

    public String getHomePageData() {

        String[] navBannerData = searchNavBannerData();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        try {
            for (int i = 0; i < navBannerData.length; i++) {
                arrayNode.add(new TextNode(navBannerData[i]));
            }

            String[] futianData = searchFutianData();
            ArrayNode futianNode = objectMapper.createArrayNode();
            for (int i = 0; i < futianData.length; i++) {

                futianNode.add(futianData[i]);
            }
            arrayNode.add(futianNode);
        } catch (Exception e) {
            logger.error(e);
        }
        return arrayNode.toString();
    }

    public String[] searchNavBannerData() {

        String[] results = new String[3];

        Article article = find(Article.class, Constant.ARTICLE_NAV_ID);

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("articleId", Constant.ARTICLE_NAV_ID);

        List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleid  = :articleId ORDER BY seq ASC ", param);
        StringBuilder sb = new StringBuilder();
        sb.append(article.getContent());

        for (int i = 0; i < subArticles.size(); i++) {
            SubArticle subArticle = subArticles.get(i);
            sb.append(subArticle.getContent());
        }

        String navBannerStr = sb.toString();
        String[] split = StringUtils.split(navBannerStr, Constant.NAV_SPLIT_TOKEN);

        results[0] = split.length >= 1 ? split[Constant.HOMEPAGE_NAV_MAIN] : "";
        results[1] = split.length >= 2 ? split[Constant.HOMEPAGE_NAV_SECOND] : "";
        results[2] = split.length >= 3 ? split[Constant.HOMEPAGE_BANNER] : "";
        return results;
    }

    public Map<Long, String> getNavMap() {
        String[] navBannerData = searchNavBannerData();

        String navArray = navBannerData[0] + navBannerData[1];
        Map<Long, String> result = new HashMap<Long, String>();
        String[] liItem = navArray.split(";");

        for (int i = 0; i < liItem.length; i++) {
            if (liItem[i].indexOf("?cid=") != -1) {
                String[] itemArr = liItem[i].split("\\?cid=");
                if (itemArr.length == 2) {
                    String[] item = liItem[i].split(",");
                    result.put(NumberUtils.toLong(itemArr[1]), item[1]);
                }
            }
        }

        return result;
    }

    public ObjectNode updateNav(String navStr, String navSecond) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            String[] original = searchNavBannerData();

            StringBuilder sb = new StringBuilder();
            sb.append(StringUtils.trimToEmpty(navStr)).append(Constant.NAV_SPLIT_TOKEN);
            sb.append(StringUtils.trimToEmpty(navSecond)).append(Constant.NAV_SPLIT_TOKEN);

            sb.append(original[2]);

            Article article = find(Article.class, Constant.ARTICLE_NAV_ID);

            String[] strings = articleService.buildContentArray(sb.toString());

            if (strings != null && strings.length > 0) {

                Map<String, Object> param = new HashMap<String, Object>();
                param.put("articleId", article.getId());

                List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleid = :articleId ORDER BY seq ASC ", param);
                for (int i = 0; i < subArticles.size(); i++) {
                    SubArticle subArticle = subArticles.get(i);
                    baseRepository.delete(SubArticle.class, subArticle.getId());
                }

                article.setContent(strings[0]);

                for (int i = 1; i < strings.length; i++) {
                    SubArticle subArticle = new SubArticle();
                    subArticle.setSeq(i);
                    subArticle.setArticleId(article.getId());
                    subArticle.setContent(strings[i]);
                    subArticle.setCreateDate(new Date());

                    baseRepository.create(subArticle);
                }

                baseRepository.update(article);
            }

            log("导航更新", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateBanner(String bannerStr) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            String[] original = searchNavBannerData();

            StringBuilder sb = new StringBuilder();
            sb.append(original[0]).append(Constant.NAV_SPLIT_TOKEN);
            sb.append(original[1]).append(Constant.NAV_SPLIT_TOKEN);
            sb.append(StringUtils.trimToEmpty(bannerStr));

            List<Article> articles = list(Article.class, " WHERE id = " + Constant.ARTICLE_NAV_ID);
            Article article = articles.get(0);

            String[] strings = articleService.buildContentArray(sb.toString());

            if (strings != null && strings.length > 0) {

                Map<String, Object> param = new HashMap<String, Object>();
                param.put("articleId", article.getId());

                List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleid = :articleId ORDER BY seq ASC ", param);
                for (int i = 0; i < subArticles.size(); i++) {
                    SubArticle subArticle = subArticles.get(i);
                    baseRepository.delete(SubArticle.class, subArticle.getId());
                }

                article.setContent(strings[0]);

                for (int i = 1; i < strings.length; i++) {
                    SubArticle subArticle = new SubArticle();
                    subArticle.setSeq(i);
                    subArticle.setArticleId(article.getId());
                    subArticle.setContent(strings[i]);
                    subArticle.setCreateDate(new Date());

                    baseRepository.create(subArticle);
                }

                baseRepository.update(article);
            }

            log("banner更新", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String[] searchFutianData() {

        Article article = find(Article.class, Constant.ARTICLE_FUTIAN_ID);

        StringBuilder sb = new StringBuilder();
        sb.append(article.getContent());

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("articleId", article.getId());

        List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleid = :articleId  ORDER BY seq ASC ", param);

        for (int i = 0; i < subArticles.size(); i++) {

            SubArticle subArticle = subArticles.get(i);
            sb.append(subArticle.getContent());
        }

        String[] _result = StringUtils.split(sb.toString(), Constant.FUTIAN_SPLIT_TOKEN);
        String[] result = new String[Constant.FUTIAN_SPLIT_SIZE];

        for (int i = 0; i < result.length; i++) {
            if (i < _result.length) {
                result[i] = HtmlUtils.htmlUnescape(_result[i]);
            } else {
                result[i] = "";
            }
        }

        return result;
    }

    public ObjectNode updateFutian(String content, String usage, String bank, String account, String number) {

        try {
            Article article = find(Article.class, Constant.ARTICLE_FUTIAN_ID);

            StringBuilder sb = new StringBuilder();
            sb.append(HtmlUtils.htmlEscape(content)).append(Constant.FUTIAN_SPLIT_TOKEN)
                    .append(HtmlUtils.htmlEscape(usage)).append(Constant.FUTIAN_SPLIT_TOKEN)
                    .append(HtmlUtils.htmlEscape(bank)).append(Constant.FUTIAN_SPLIT_TOKEN)
                    .append(HtmlUtils.htmlEscape(account)).append(Constant.FUTIAN_SPLIT_TOKEN)
                    .append(HtmlUtils.htmlEscape(number));

            String[] contentArray = articleService.buildContentArray(sb.toString());

            if (contentArray != null && contentArray.length > 0) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("articleId", article.getId());

                List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleid = :articleId ORDER BY seq ASC ", param);
                for (int i = 0; i < subArticles.size(); i++) {
                    SubArticle subArticle = subArticles.get(i);
                    baseRepository.delete(SubArticle.class, subArticle.getId());
                }

                article.setContent(contentArray[0]);

                for (int i = 1; i < contentArray.length; i++) {
                    SubArticle subArticle = new SubArticle();
                    subArticle.setSeq(i);
                    subArticle.setArticleId(article.getId());

                    subArticle.setContent(contentArray[i]);
                    subArticle.setCreateDate(new Date());

                    baseRepository.create(subArticle);
                }

                baseRepository.update(article);
            }

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            log("广种福田更新", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode staticizeAll() {

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");
        PrintWriter pw = null;

        try {
            ToolContext toolManagerContext = toolManager.createContext();
            // window title
            toolManagerContext.put("winTitle","首页" + Constant.WINDOW_TITLE_SUFFIX);
            toolManagerContext.put("depict","");

            String[] navDataArr = searchNavBannerData();
            Map<Integer, String> navMap = new HashMap<Integer, String>();

            if (StringUtils.isNotBlank(navDataArr[0])) {
                String[] navArr = navDataArr[0].split(";");

                for (int i = 0; i < navArr.length; i++) {
                    if (StringUtils.isNotBlank(navArr[i])) {
                        String[] item = navArr[i].split("\\?cid=");
                        if (item.length == 2) {
                            navMap.put(NumberUtils.toInt(item[1]), navArr[i].split(",")[1]);
                        }
                    }
                }
            }

            Map<String, Object> param = new HashMap<String, Object>();
            List<Article> focusList = list(Article.class, " WHERE status = 1 AND focus = 1 ORDER BY createDate DESC LIMIT 0,5 ");
            List<Object[]> focusArrList = new ArrayList<Object[]>();
            for (int i = 0; i < focusList.size(); i++) {
                Article article = focusList.get(i);
                Object[] item = new Object[5];
                MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                Media media = find(Media.class, mediaArticle.getMediaId());
                item[0] = article.getTitle();
                item[1] = article.getUrl();
                item[2] = mediaService.getImageUrl(media, config.getHomePageFocusBigImg());
                item[3] = mediaService.getImageUrl(media, config.getHomePageFocusSmallImg());
                if (i == 0) {
                    item[4] = true;
                } else {
                    item[4] = false;
                }
                focusArrList.add(item);
            }
            toolManagerContext.put("focus", focusArrList);

            // <!-- 新闻法讯 -->
            // category data
            param.put("categoryId", Constant.CATEGORY_ID_NEWS);
            param.put("headline", Constant.ARTICLE_HEADLINE);
            Category newsCategory = find(Category.class, Constant.CATEGORY_ID_NEWS);
            List<Article> newsList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,9 ", param);
            Map<String, Object> newsMap = new HashMap<String, Object>();
            for (int i = 0; i < newsList.size(); i++) {
                Article article = newsList.get(i);
                if (i == 0) {
                    List<String> first = new ArrayList<String>();
                    first.add(article.getUrl());
                    first.add(article.getTitle());
                    first.add(article.getDepict());
                    newsMap.put("first", first);
                } else {
                    String[] otherItem = new String[2];
                    otherItem[0] = article.getUrl();
                    otherItem[1] = article.getTitle();

                    List<String[]> other = (List<String[]>) newsMap.get("other");
                    if (other == null) {
                        other = new ArrayList<String[]>();
                    }
                    other.add(otherItem);
                    newsMap.put("other", other);
                }
            }
            if (navMap.containsKey(Constant.CATEGORY_ID_NEWS)) {
                newsMap.put("more", navMap.get(Constant.CATEGORY_ID_NEWS));
            }
            newsMap.put("name", newsCategory.getDepict());
            toolManagerContext.put("news", newsMap);

            // <!-- 生活禅 -->
            param.put("categoryId", Constant.CATEGORY_ID_LIFE);
            Category lifeCategory = find(Category.class, Constant.CATEGORY_ID_LIFE);
            List<Article> lifeList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,4 ", param);
            Map<String, Object> lifeMap = new HashMap<String, Object>();

            for (int i = 0; i < lifeList.size(); i++) {
                Article article = lifeList.get(i);
                if (i == 0) {
                    List<String> first = new ArrayList<String>();
                    first.add(article.getUrl());
                    first.add(article.getTitle());
                    first.add(article.getDepict());
                    first.add(DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD));
                    MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                    if (mediaArticle != null) {
                        Media media = find(Media.class, mediaArticle.getMediaId());
                        first.add(mediaService.getImageUrl(media,config.getHomePageLifeImg()));
                    } else {
                        first.add("");
                    }
                    lifeMap.put("first", first);
                } else {
                    String[] otherItem = new String[3];
                    otherItem[0] = article.getUrl();
                    otherItem[1] = article.getTitle();
                    otherItem[2] = DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD);

                    List<String[]> other = (List<String[]>) lifeMap.get("other");
                    if (other == null) {
                        other = new ArrayList<String[]>();
                    }
                    other.add(otherItem);
                    lifeMap.put("other", other);
                }
            }
            if (navMap.containsKey(Constant.CATEGORY_ID_LIFE)) {
                lifeMap.put("more", navMap.get(Constant.CATEGORY_ID_LIFE));
            }
            lifeMap.put("name", lifeCategory.getDepict());
            toolManagerContext.put("life", lifeMap);

            //<!-- 紫云佛国 -->
            param.put("categoryId", Constant.CATEGORY_ID_ZIYUNFOGUO);
            List<Article> ziyunfoguoList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,4 ", param);
            Category ziyunfoguoCategory = find(Category.class, Constant.CATEGORY_ID_ZIYUNFOGUO);
            Map<String, Object> ziyunfoguoMap = new HashMap<String, Object>();
            for (int i = 0; i < ziyunfoguoList.size(); i++) {
                Article article = ziyunfoguoList.get(i);
                if (i == 0) {
                    List<String> first = new ArrayList<String>();
                    first.add(article.getUrl());
                    first.add(article.getTitle());
                    first.add(article.getDepict());
                    first.add(DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD));
                    MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                    if (mediaArticle != null) {
                        Media media = find(Media.class, mediaArticle.getMediaId());
                        first.add(mediaService.getImageUrl(media,config.getHomePageLifeImg()));// use life-category's img config.
                    } else {
                        first.add("");
                    }
                    ziyunfoguoMap.put("first", first);
                } else {
                    String[] otherItem = new String[3];
                    otherItem[0] = article.getUrl();
                    otherItem[1] = article.getTitle();
                    otherItem[2] = DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD);

                    List<String[]> other = (List<String[]>) ziyunfoguoMap.get("other");
                    if (other == null) {
                        other = new ArrayList<String[]>();
                    }
                    other.add(otherItem);
                    ziyunfoguoMap.put("other", other);
                }
            }
            if (navMap.containsKey(Constant.CATEGORY_ID_ZIYUNFOGUO)) {
                ziyunfoguoMap.put("more", navMap.get(Constant.CATEGORY_ID_ZIYUNFOGUO));
            }
            ziyunfoguoMap.put("name", ziyunfoguoCategory.getDepict());
            toolManagerContext.put("ziyun", ziyunfoguoMap);

            // <!-- 慧公文集 -->
            param.put("categoryId", Constant.CATEGORY_ID_CORPUS);
            List<Article> corpusList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,4 ", param);
            Category corpusCategory = find(Category.class, Constant.CATEGORY_ID_CORPUS);
            Map<String, Object> corpusMap = new HashMap<String, Object>();
            for (int i = 0; i < corpusList.size(); i++) {
                Article article = corpusList.get(i);
                if (i == 0) {
                    List<String> first = new ArrayList<String>();
                    first.add(article.getUrl());
                    first.add(article.getTitle());
                    first.add(article.getDepict());
                    first.add(DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD));
                    MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                    if (mediaArticle != null) {
                        Media media = find(Media.class, mediaArticle.getMediaId());
                        first.add(mediaService.getImageUrl(media, config.getHomePageCorpusImg()));
                    } else {
                        first.add("");
                    }
                    corpusMap.put("first", first);
                } else {

                    String[] otherItem = new String[4];
                    otherItem[0] = article.getUrl();
                    otherItem[1] = article.getTitle();
                    otherItem[2] = DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD);
                    if (i == 1) {
                        MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                        if (mediaArticle != null) {
                            Media media = find(Media.class, mediaArticle.getMediaId());
                            List<String> first = (List<String>) corpusMap.get("first");
                            first.add(mediaService.getImageUrl(media, config.getHomePageCorpusImg()));
                            corpusMap.put("first", first);
                        }
                    }

                    List<String[]> other = (List<String[]>) corpusMap.get("other");
                    if (other == null) {
                        other = new ArrayList<String[]>();
                    }
                    other.add(otherItem);
                    corpusMap.put("other", other);
                }

            }
            if (navMap.containsKey(Constant.CATEGORY_ID_CORPUS)) {
                corpusMap.put("more", navMap.get(Constant.CATEGORY_ID_CORPUS));
            }
            corpusMap.put("name", corpusCategory.getDepict());
            toolManagerContext.put("corpus", corpusMap);

            // <!-- 佛教常识 -->
            param.put("categoryId", Constant.CATEGORY_ID_KNOWLEDGE);
            List<Article> knowledgeList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,4 ", param);
            Category knowledgeCategory = find(Category.class, Constant.CATEGORY_ID_KNOWLEDGE);
            Map<String, Object> knowledgeMap = new HashMap<String, Object>();

            for (int i = 0; i < knowledgeList.size(); i++) {
                Article article = knowledgeList.get(i);
                if (i == 0) {
                    List<String> first = new ArrayList<String>();
                    first.add(article.getUrl());
                    first.add(article.getTitle());
                    first.add(article.getDepict());
                    first.add(DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD));

                    MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                    if (mediaArticle != null) {
                        Media media = find(Media.class, mediaArticle.getMediaId());
                        first.add(mediaService.getImageUrl(media, config.getHomePageCorpusImg()));// use knowledge-category's img config.
                    } else {
                        first.add("");
                    }
                    knowledgeMap.put("first", first);
                } else {
                    String[] otherItem = new String[4];
                    otherItem[0] = article.getUrl();
                    otherItem[1] = article.getTitle();
                    otherItem[2] = DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_MMDD);
                    if (i == 1) {
                        MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                        if (mediaArticle != null) {
                            Media media = find(Media.class, mediaArticle.getMediaId());
                            List<String> first = (List<String>) knowledgeMap.get("first");
                            first.add(mediaService.getImageUrl(media, config.getHomePageCorpusImg()));
                            knowledgeMap.put("first", first);
                        }
                    }

                    List<String[]> other = (List<String[]>) knowledgeMap.get("other");
                    if (other == null) {
                        other = new ArrayList<String[]>();
                    }
                    other.add(otherItem);
                    knowledgeMap.put("other", other);
                }
            }
            if (navMap.containsKey(Constant.CATEGORY_ID_KNOWLEDGE)) {
                knowledgeMap.put("more", navMap.get(Constant.CATEGORY_ID_KNOWLEDGE));
            }
            knowledgeMap.put("name", knowledgeCategory.getDepict());
            toolManagerContext.put("knowledge", knowledgeMap);

            // <!-- 梵音宣流 -->
            param.put("categoryId", Constant.CATEGORY_ID_AUDIO);
            Category audioCategory = find(Category.class, Constant.CATEGORY_ID_AUDIO);
            List<Article> audioList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,4 ", param);
            Map<String, Object> audioMap = new HashMap<String, Object>();
            for (int i = 0; i < audioList.size(); i++) {
                Article article = audioList.get(i);
                MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_AUDIO[0]));
                if (mediaArticle != null) {
                    Media media = find(Media.class, mediaArticle.getMediaId());
                    if (i == 0) {
                        audioMap.put("currentUrl", mediaService.getMediaUrl(media));
                        audioMap.put("currentTitle", article.getTitle());
                        continue;
                    }
                    Object[] item = new Object[2];
                    item[0] = article.getTitle();
                    item[1] = mediaService.getMediaUrl(media);

                    List<Object[]> list = (List<Object[]>) audioMap.get("list");
                    if (list == null) {
                        list = new ArrayList<Object[]>();
                    }
                    list.add(item);
                    audioMap.put("list", list);
                }
            }
            if (navMap.containsKey(Constant.CATEGORY_ID_AUDIO)) {
                audioMap.put("more", navMap.get(Constant.CATEGORY_ID_AUDIO));
            }
            audioMap.put("name", audioCategory.getDepict());
            toolManagerContext.put("audio", audioMap);

            //<!-- 竹影婆娑 -->
            param.put("categoryId", Constant.CATEGORY_ID_ZHUYINGPOSUO);
            Category zhuyingCategory = find(Category.class, Constant.CATEGORY_ID_ZHUYINGPOSUO);
            List<Article> zhuyingposuoList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,4 ", param);
            Map<String, Object> zhuyingMap = new HashMap<String, Object>();
            for (int i = 0; i < zhuyingposuoList.size(); i++) {
                Article article = zhuyingposuoList.get(i);
                MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
                if (mediaArticle != null) {
                    Media media = find(Media.class, mediaArticle.getMediaId());

                    Object[] item = new Object[3];
                    item[0] = article.getTitle();
                    item[1] = article.getUrl();
                    item[2] = mediaService.getImageUrl(media,config.getHomePageZhuYingImg());

                    List<Object[]> list = (List<Object[]>) zhuyingMap.get("list");
                    if (list == null) {
                        list = new ArrayList<Object[]>();
                    }
                    list.add(item);
                    zhuyingMap.put("list", list);
                }
            }
            if (navMap.containsKey(Constant.CATEGORY_ID_ZHUYINGPOSUO)) {
                zhuyingMap.put("more", navMap.get(Constant.CATEGORY_ID_ZHUYINGPOSUO));
            }
            zhuyingMap.put("name", zhuyingCategory.getDepict());
            toolManagerContext.put("zhuying", zhuyingMap);

            toolManagerContext.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            toolManagerContext.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            //
            VelocityEngine velocityEngine = toolManager.getVelocityEngine();

            pw = new PrintWriter(VelocityUtil.getBaseFile("index.html"), Constant.ENCODE_UTF8);

            Template template = find(Template.class, Constant.TEMPLATE_BUILD_IN_INDEX);
            velocityEngine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, toolManagerContext, pw);

            pw.flush();

            log("首页静态化", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public ObjectNode staticizeNav() {

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");
        PrintWriter pw = null;

        try {
            ToolContext toolManagerContext = toolManager.createContext();

            String[] navBannerArr = searchNavBannerData();
            Map<String, Object> navBanner = new HashMap<String, Object>();

            // nav ul
            List<String[]> ul = new ArrayList<String[]>();
            String[] navMainItem = StringUtils.split(StringUtils.trim(navBannerArr[0]), ";");
            for (int i = 0; i < navMainItem.length; i++) {
                String[] itemSplit = StringUtils.split(navMainItem[i], ",");
                if (itemSplit.length == 2 && StringUtils.isNotBlank(itemSplit[0]) && StringUtils.isNotBlank(itemSplit[1])) {
                    String[] _item = new String[2];
                    _item[0] = StringUtils.trimToEmpty(itemSplit[0]);
                    _item[1] = StringUtils.trimToEmpty(itemSplit[1]);
                    ul.add(_item);
                }
            }
            navBanner.put("ul", ul);

            // nav ol
            List<String[]> ol = new ArrayList<String[]>();
            String[] navSecondItem = StringUtils.split(StringUtils.trim(navBannerArr[1]), ";");
            for (int i = 0; i < navSecondItem.length; i++) {
                String[] itemSplit = StringUtils.split(navSecondItem[i], ",");
                if (itemSplit.length == 2 && StringUtils.isNotBlank(itemSplit[0]) && StringUtils.isNotBlank(itemSplit[1])) {
                    String[] _item = new String[2];
                    _item[0] = StringUtils.trimToEmpty(itemSplit[0]);
                    _item[1] = StringUtils.trimToEmpty(itemSplit[1]);
                    ol.add(_item);
                }
            }
            navBanner.put("ol", ol);
            toolManagerContext.put("nav", navBanner);

            toolManagerContext.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            toolManagerContext.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            //
            VelocityEngine velocityEngine = toolManager.getVelocityEngine();
            pw = new PrintWriter(this.buildBaseTemplateOutPath("nav.html"), Constant.ENCODE_UTF8);
            Template template = find(Template.class, Constant.TEMPLATE_BUILD_IN_NAV);
            velocityEngine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, toolManagerContext, pw);

            pw.flush();

            // homepage
            staticizeAll();

            log("导航静态化", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public ObjectNode staticizeBanner() {

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");
        PrintWriter pw = null;

        try {
            ToolContext toolManagerContext = toolManager.createContext();

            // nav
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("categoryId", Constant.CATEGORY_ID_NAVBANNER);

            String[] navBannerArr = searchNavBannerData();

            // banner
            List<String> bannerList = new ArrayList<String>();
            String[] bannerItem = StringUtils.split(StringUtils.trim(navBannerArr[2]), ";");
            for (int i = 0; i < bannerItem.length; i++) {
                bannerList.add(StringUtils.trimToEmpty(bannerItem[i]));
            }
            toolManagerContext.put("banner", bannerList);

            toolManagerContext.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            toolManagerContext.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            VelocityEngine velocityEngine = toolManager.getVelocityEngine();

            // banner
            pw = new PrintWriter(this.buildBaseTemplateOutPath("banner.html"), Constant.ENCODE_UTF8);
            Template template = find(Template.class, Constant.TEMPLATE_BUILD_IN_BANNER);
            velocityEngine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, toolManagerContext, pw);
            pw.flush();

            log("banner静态化", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public ObjectNode staticizeFutian() {

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");
        PrintWriter pw = null;

        try {
            ToolContext toolManagerContext = toolManager.createContext();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("categoryId", Constant.CATEGORY_ID_GUANGZHONGFUTIAN);

            Category futianCategory = find(Category.class, Constant.CATEGORY_ID_GUANGZHONGFUTIAN);
            String[] otherArr = new String[2];
            otherArr[0] = futianCategory.getDepict();
            List<Article> futianArticles = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND url != '' ", param);
            if (futianArticles != null && futianArticles.size() > 0) {
                Article article = futianArticles.get(0);
                otherArr[1] = article.getUrl(); // more
            } else {
                otherArr[1] = ""; // more
            }
            toolManagerContext.put("futian", ArrayUtils.addAll(otherArr, searchFutianData()));

            param.clear();
            param.put("categoryId", Constant.CATEGORY_ID_CONTACT);
            List<Article> contactArticles = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND url != '' ", param);
            if (contactArticles != null && contactArticles.size() > 0) {
                Article article = contactArticles.get(0);
                toolManagerContext.put("detail", article.getUrl());
            }

            toolManagerContext.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            toolManagerContext.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            //
            VelocityEngine velocityEngine = toolManager.getVelocityEngine();

            pw = new PrintWriter(this.buildBaseTemplateOutPath("futian.html"), Constant.ENCODE_UTF8);
            Template template = find(Template.class, Constant.TEMPLATE_BUILD_IN_FUTIAN);
            velocityEngine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, toolManagerContext, pw);

            pw.flush();

            log("广种福田静态化", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public ObjectNode staticizeFawu() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");
        PrintWriter pw = null;

        try {
            ToolContext toolManagerContext = toolManager.createContext();

            String[] navDataArr = searchNavBannerData();
            String lawMoreUrl = "";

            if (StringUtils.isNotBlank(navDataArr[1])) {
                String[] navArr = navDataArr[1].split(";");

                for (int i = 0; i < navArr.length; i++) {
                    if (StringUtils.isNotBlank(navArr[i])) {
                        if (navArr[i].indexOf("?cid=" + Constant.CATEGORY_ID_LAW) != -1) {
                            lawMoreUrl = navArr[i].split(",")[1];
                        }
                    }
                }
            }

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("categoryId", Constant.CATEGORY_ID_LAW);
            param.put("headline", Constant.ARTICLE_HEADLINE);

            List<Article> lawList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC LIMIT 0,3", param);
            Category lawCategory = find(Category.class, Constant.CATEGORY_ID_LAW);
            Map<String, Object> lawMap = new HashMap<String, Object>();

            for (int i = 0; i < lawList.size(); i++) {
                Article article = lawList.get(i);
                String[] item = new String[3];
                item[0] = article.getUrl();
                item[1] = article.getTitle();

                param.clear();
                param.put("articleId", article.getId());
                List<SubArticle> subArticles = list(SubArticle.class, " WHERE articleid = :articleId ORDER BY seq ASC ", param);
                StringBuilder sb = new StringBuilder();
                sb.append(article.getContent());
                for (int j = 0; j < subArticles.size(); j++) {
                    sb.append(subArticles.get(j).getContent());
                }
                item[2] = StringUtils.trimToEmpty(sb.toString());
                item[2] = StringUtils.isEmpty(item[2]) ? "" : item[2].replaceAll("</*[a-zA-Z]+>|\\r|\\n|\\t", "");
                item[2] = StringUtils.substring(item[2], 0, item[2].length() > 290 ? 290 : item[2].length()) + "...";

                if (i == 0) {
                    lawMap.put("first", item);
                } else {

                    List<String[]> other = (List<String[]>) lawMap.get("other");
                    if (other == null) {
                        other = new ArrayList<String[]>();
                    }
                    other.add(item);
                    lawMap.put("other", other);
                }
            }
            if (StringUtils.isNotBlank(lawMoreUrl)) {
                lawMap.put("more", lawMoreUrl);
            }
            lawMap.put("name", lawCategory.getDepict());
            toolManagerContext.put("law", lawMap);

            toolManagerContext.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            toolManagerContext.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            //
            VelocityEngine velocityEngine = toolManager.getVelocityEngine();
            pw = new PrintWriter(this.buildBaseTemplateOutPath("fawu.html"), Constant.ENCODE_UTF8);
            Template template = find(Template.class, Constant.TEMPLATE_BUILD_IN_FAWU);
            velocityEngine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, toolManagerContext, pw);

            pw.flush();

            // homepage
            staticizeAll();

            log("法务静态化", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public ObjectNode staticizeNotice() {

        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");
        PrintWriter pw = null;

        try {
            ToolContext toolManagerContext = toolManager.createContext();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("categoryId", Constant.CATEGORY_ID_NOTICE);
            param.put("headline", Constant.ARTICLE_HEADLINE);

            List<Article> noticeList = list(Article.class, " WHERE categoryid = :categoryId AND status = 1 AND headline = :headline AND headlineorder != 0 ORDER BY headlineorder ASC ", param);

            List<String[]> other = new ArrayList<String[]>();

            for (int i = 0; i < noticeList.size(); i++) {
                Article article = noticeList.get(i);

                String[] otherItem = new String[2];
                otherItem[0] = article.getUrl();
                otherItem[1] = article.getTitle();

                other.add(otherItem);
            }
            toolManagerContext.put("notice", other);

            toolManagerContext.put(Constant.STATIC_RESOURCE_URL_PREFIX, config.getStaticResourceURLPrefix());
            toolManagerContext.put(Constant.LIST_PAGE_URL_PREFIX, config.getListDomain());

            //
            VelocityEngine velocityEngine = toolManager.getVelocityEngine();

            pw = new PrintWriter(this.buildBaseTemplateOutPath("notice.html"), Constant.ENCODE_UTF8);
            Template template = find(Template.class, Constant.TEMPLATE_BUILD_IN_NOTICE);
            velocityEngine.mergeTemplate(template.getPath(), Constant.ENCODE_UTF8, toolManagerContext, pw);

            pw.flush();

            log("通知静态化", "");

            return objectNode;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    public String buildBaseTemplateOutPath(String fileName) {
        String target = System.getProperty("webapp.root") + Constant.VELOCITY_TEMPLATE_OUT_PATH + File.separator + "base";
        File file = new File(target);
        if (!file.exists()) {
            file.mkdirs();
        }
        return target + File.separator + fileName;
    }

}
