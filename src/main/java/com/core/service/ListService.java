package com.core.service;

import com.core.domain.Article;
import com.core.domain.Category;
import com.core.domain.Media;
import com.core.domain.MediaArticle;
import com.core.repository.sqlBuilder.Page;
import com.core.util.Constant;
import com.core.util.DateUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunpeng
 */
@Service
public class ListService extends BaseService {

    @Autowired
    private MediaService mediaService;

    public Object[] articleList(int pageNum, String _categoryId) {

        if (pageNum <= 0) {
            pageNum = 1;
        }

        int categoryId = NumberUtils.toInt(_categoryId, Constant.CATEGORY_ID_NEWS);

        Object[] result = new Object[3];

        if (categoryId == Constant.CATEGORY_ID_ZHUYINGPOSUO) {
            result[0] = Constant.PHOTO_LIST_PAGE_NAME;
            result[1] = photoList(12, pageNum, categoryId);
        } else {
            result[0] = Constant.ARTICLE_LIST_PAGE_NAME;
            result[1] = normalList(10, pageNum, categoryId);
        }

        // window title
        Category category = find(Category.class, categoryId);

        result[2] = category.getDepict() + Constant.WINDOW_TITLE_SUFFIX;

        return result;

    }

    public Map<String, Object> normalList(int pageSize, int pageNum, int categoryId) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("categoryId", categoryId);
        param.put("status", Constant.COMMON_STATUS_ACCEPT);

        Page<Article> page = getPage(Article.class, " WHERE categoryid = :categoryId AND status = :status ORDER BY publishDate DESC", param, pageSize, pageNum);

        List<Article> articles = page.getResultList();
        List<Map> articleList = new ArrayList<Map>();

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            Map<String, Object> item = new HashMap<String, Object>();

            item.put("url", article.getUrl());
            item.put("title", article.getTitle());
            item.put("depict", article.getDepict());
            item.put("publishDate", DateUtil.formatDate(article.getPublishDate(), DateUtil.FORMAT_PATTERN_YYMMDD));
            articleList.add(item);
        }
        param.clear();

        Category category = find(Category.class, categoryId);
        param.put("categoryId", category.getId());
        param.put("category", category.getDepict());
        param.put("data", articleList);
        param.put("totalPage", page.getTotalPage());
        param.put("pageNum", page.getPageNum());
        param.put("listEmpty", articles.size() == 0);

        return param;
    }

    public Map<String, Object> photoList(int pageSize, int pageNum, int categoryId) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("categoryId", categoryId);
        param.put("status", Constant.COMMON_STATUS_ACCEPT);

        Page<Article> page = getPage(Article.class, " WHERE categoryid = :categoryId AND status = :status ORDER BY publishDate DESC", param, pageSize, pageNum);

        List<Article> articles = page.getResultList();
        List<Map> articleList = new ArrayList<Map>();

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            Map<String, Object> item = new HashMap<String, Object>();
            MediaArticle mediaArticle = mediaService.findMediaArticle(article.getId(), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));
            String image = "";
            if (mediaArticle != null) {
                Media media = find(Media.class, mediaArticle.getMediaId());
                image = mediaService.getImageUrl(media, config.getHomePageZhuYingImg()); // list page's img size = home page'img size
            }

            item.put("image", image);
            item.put("url", article.getUrl());
            item.put("title", article.getTitle());
            articleList.add(item);
        }

        param.clear();

        Category category = find(Category.class, categoryId);
        param.put("categoryId", category.getId());
        param.put("category", category.getDepict());
        param.put("data", articleList);
        param.put("totalPage", page.getTotalPage());
        param.put("pageNum", page.getPageNum());
        param.put("listEmpty", articles.size() == 0);

        return param;
    }

}
