package com.core.service;

import com.core.domain.Article;
import com.core.domain.Category;
import com.core.domain.Media;
import com.core.repository.sqlBuilder.Page;
import com.core.security.domain.Admin;
import com.core.security.domain.User;
import com.core.util.Constant;
import com.core.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sunpeng
 */
@Service
public class SearchService extends BaseService {

    @Autowired
    private TreeService treeService;

    @Autowired
    private MediaService mediaService;

    public String getInitData() {

        ObjectNode objectNode = objectMapper.createObjectNode();

        List<Category> categories = list(Category.class, " WHERE status = " + Constant.COMMON_STATUS_ACCEPT);

        ArrayNode categoriesNode = objectMapper.createArrayNode();
        for (int i = 0; i < categories.size(); i++) {

            Category category = categories.get(i);
            ObjectNode item = objectMapper.createObjectNode();
            item.put("name", category.getDepict());
            item.put("id", category.getId());
            categoriesNode.add(item);
        }
        objectNode.put("category", categoriesNode);

        List<User> users = list(User.class, " WHERE status = " + Constant.COMMON_STATUS_ACCEPT);
        List<Admin> admins = list(Admin.class, " WHERE status = " + Constant.COMMON_STATUS_ACCEPT);
        ArrayNode usersNode = objectMapper.createArrayNode();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            ObjectNode item = objectMapper.createObjectNode();
            item.put("name", user.getName());
            item.put("account", user.getAccount());

            usersNode.add(item);
        }

        for (int i = 0; i < admins.size(); i++) {
            Admin admin = admins.get(i);
            ObjectNode item = objectMapper.createObjectNode();
            item.put("name", admin.getName());
            item.put("account", admin.getAccount());
            usersNode.add(item);
        }

        objectNode.put("users", usersNode);

        ArrayNode mediaCategory = objectMapper.createArrayNode();
        ObjectNode imgFile = objectMapper.createObjectNode();
        imgFile.put("key", Constant.MEDIA_TYPE_IMAGE[0]);
        imgFile.put("name", Constant.MEDIA_TYPE_IMAGE[1]);
        mediaCategory.add(imgFile);

        ObjectNode audioFile = objectMapper.createObjectNode();
        audioFile.put("key", Constant.MEDIA_TYPE_AUDIO[0]);
        audioFile.put("name", Constant.MEDIA_TYPE_AUDIO[1]);
        mediaCategory.add(audioFile);

        ObjectNode otherFile = objectMapper.createObjectNode();
        otherFile.put("key", Constant.MEDIA_TYPE_FILE[0]);
        otherFile.put("name", Constant.MEDIA_TYPE_FILE[1]);
        mediaCategory.add(otherFile);
        objectNode.put("mediaCategory", mediaCategory);

        return objectNode.toString();
    }

    public String searchArticle(int pageSize, int page, String title, String categoryId, String focus, String headLine, String status, String creator, String createDate, String updateDate, String publishDate) {

        try {
            String sql = " WHERE 1 = 1 AND status != " + Constant.ARTICLE_STATUS_HIDDEN;
            String orderBySql = " ORDER BY createDate DESC";

            Map<String, Object> param = new HashMap<String, Object>();

            if (StringUtils.isNotBlank(title)) {
                sql += " AND title like :title ";
                param.put("title", "%" + title + "%");
            }
            if (NumberUtils.isNumber(categoryId)) {
                sql += " AND categoryid = :categoryId ";
                param.put("categoryId", categoryId);
            }

            if (NumberUtils.isNumber(focus)) {
                sql += " AND focus = :focus ";
                param.put("focus", focus);
            }

            if (NumberUtils.isNumber(headLine)) {
                sql += " AND headLine = :headLine ";
                param.put("headLine", headLine);
            }

            if (NumberUtils.isNumber(status)) {
                sql += " AND status = :status ";
                param.put("status", status);
            }

            if (StringUtils.isNotBlank(creator)) {
                sql += " AND creator like :creator ";
                param.put("creator", creator);
            }

            if (StringUtils.isNotBlank(createDate)) {
                sql += " AND createDate >= :createDate ";
                param.put("createDate", DateUtil.extDateFix(createDate));
            }
            if (StringUtils.isNotBlank(updateDate)) {
                sql += " AND updateDate >= :updateDate ";
                param.put("updateDate", DateUtil.extDateFix(updateDate));
                orderBySql += ", updateDate ";
            }
            if (StringUtils.isNotBlank(publishDate)) {
                sql += " AND publishDate >= :publishDate ";
                param.put("publishDate", DateUtil.extDateFix(publishDate));
                orderBySql += ", publishDate ";
            }

            sql += orderBySql;
            Page<Article> articlePage = getPage(Article.class, sql, param, pageSize, page);
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (int i = 0; i < articlePage.getResultList().size(); i++) {
                Article article = articlePage.getResultList().get(i);
                ObjectNode articleNode = objectMapper.valueToTree(article);

                articleNode.remove("content");

                List<Media> medias = mediaService.findMedias(article.getId());
                StringBuilder sb = new StringBuilder();
                int mediaSize = medias != null ? medias.size() : 0;
                for (int j = 0; j < mediaSize; j++) {
                    Media media = medias.get(j);
                    String mediaInfo = "[";
                    String endStr = (j + 1) == mediaSize ? "]" : "],";
                    switch (media.getCategory()) {
                        case 1:
                            mediaInfo = mediaInfo + Constant.MEDIA_TYPE_IMAGE[1] + ":" + StringUtils.defaultIfBlank(media.getTitle(), "(无)") + endStr;
                            break;
                        case 2:
                            mediaInfo = mediaInfo + Constant.MEDIA_TYPE_AUDIO[1] + ":" + StringUtils.defaultIfBlank(media.getTitle(), "(无)") + endStr;
                            break;
                        default:
                            mediaInfo = mediaInfo + Constant.MEDIA_TYPE_FILE[1] + ":" + StringUtils.defaultIfBlank(media.getTitle(), "(无)") + endStr;
                            break;
                    }
                    sb.append(mediaInfo);
                }

                articleNode.put("media", sb.toString());

                if (article.getCategoryId() > 0) {
                    Category category = find(Category.class, article.getCategoryId());
                    articleNode.put("categoryName", category.getDepict());
                }
                if (article.getTreeId() != 0) {
                    articleNode.put("treePath", treeService.getTreeRoad(article.getTreeId()));
                }
                arrayNode.add(articleNode);
            }

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("data", arrayNode);
            objectNode.put("totalData", articlePage.getTotalData());

            return objectNode.toString();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }

    }

}
