package com.core.service;

import com.core.domain.Article;
import com.core.domain.Media;
import com.core.domain.MediaArticle;
import com.core.repository.sqlBuilder.Page;
import com.core.security.UserInfo;
import com.core.security.domain.Admin;
import com.core.security.domain.User;
import com.core.util.Constant;
import com.core.util.DateUtil;
import com.core.util.QiniuAuthUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.text.ParseException;
import java.util.*;

/**
 * Created by sunpeng
 */
@Service
public class MediaService extends BaseService {

    @Autowired
    private QiniuAuthUtil qiniuAuthUtil;

    public String createMedia(MultipartHttpServletRequest msr) {

        Iterator<String> fileNames = msr.getFileNames();
        ObjectNode objectNode = objectMapper.createObjectNode();

        // kindEditor : error=0 => upload success
        objectNode.put("error", 0);
        UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");

        // default image
        int mediaType = NumberUtils.toInt(msr.getParameter("type"), NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0]));

        try {

            StringBuilder stringBuilder = new StringBuilder("mediaIds:[ ");

            // TODO more than one file
            while (fileNames.hasNext()) {

                MultipartFile msrFile = msr.getFile(fileNames.next());

                String fileName = msrFile.getOriginalFilename().toLowerCase();

                String title = msr.getParameter("title");
                int modal = NumberUtils.toInt(msr.getParameter("modal"), -1);

                int width = NumberUtils.toInt(msr.getParameter("width"), 0);
                int height = NumberUtils.toInt(msr.getParameter("height"), 0);
                int format = NumberUtils.toInt(msr.getParameter("format"), -1);

                int interlace = NumberUtils.toInt(msr.getParameter("interlace"), 0) == 1 ? 1 : 0;
                int keepFileName = NumberUtils.toInt(msr.getParameter("keepFileName"), 0) == 1 ? 1 : 0;

                boolean isImage = false;

                switch (mediaType) {
                    case 1:
                        if (fileName.matches(Constant.IMG_FILTER)) {
                            isImage = true;
                        } else {
                            objectNode.put("error", 1);
                            objectNode.put("message", Constant.IMG_FILTER_MSG);
                            return objectNode.toString();
                        }
                        break;
                    case 2:

                        if (!fileName.matches(Constant.AUDIO_FILTER)) {
                            objectNode.put("error", 1);
                            objectNode.put("message", Constant.AUDIO_FILTER_MSG);
                            return objectNode.toString();
                        }
                        break;
                    default:
                        objectNode.put("error", 1);
                        objectNode.put("message", Constant.NO_SUPPORT_TYPE);
                        return objectNode.toString();
                }

                // keep the file's name
                QiniuAuthUtil.MyRet ret;
                if (keepFileName == 1) {
                    ret = upload(msrFile.getBytes(), fileName);
                } else {
                    ret = upload(msrFile.getBytes());
                }
                boolean updateMedia = true;

                Media media = findMedia(ret.key);

                if (media == null) {
                    media = new Media();
                    updateMedia = false;
                }

                media.setCategory(mediaType);
                media.setTitle(StringUtils.isNotEmpty(title) ? title : msrFile.getOriginalFilename());

                media.setSize(ret.size);
                media.setUkey(ret.key);
                media.setFtype(ret.type);
                media.setHash(ret.hash);

                media.setWidth(isImage && width > 0 ? width : ret.width);
                media.setHeight(isImage && height > 0 ? height : ret.height);

                if (isImage) {
                    media.setHandle(buildImageViewParam(modal, width, height, format, interlace));
                }

                media.setStatus(1);
                media.setCreator(userInfo.getAccount());
                media.setCreateDate(new Date());
                if (updateMedia) {
                    baseRepository.update(media);
                } else {
                    media.setId(baseRepository.create(media));
                }

                stringBuilder.append(media.getId()).append(" ");

                log("媒体创建(上传)", stringBuilder.toString());

                objectNode.put("url", qiniuAuthUtil.getAccessDomain() + ret.key);
            }
            objectNode.put("success", true);
            objectNode.put("result", "success");

        } catch (QiniuException e) {
            objectNode.put("error", 1);

            Response r = e.response;
            // 请求失败时简单状态信息
            logger.error(r.toString());
            try {
                // 响应的文本信息
                logger.error(r.bodyString());
            } catch (QiniuException e1) {
                //ignore
            }
        } catch (Exception e) {
            objectNode.put("error", 1);
            logger.error(e.toString());
        }

        return objectNode.toString();
    }

    private QiniuAuthUtil.MyRet upload(byte[] bytes) throws QiniuException {
        return upload(bytes, null);
    }

    private QiniuAuthUtil.MyRet upload(byte[] bytes, String key) throws QiniuException {
        Response res = qiniuAuthUtil.getUploadManager().put(bytes, key, qiniuAuthUtil.getUpToken2());
        QiniuAuthUtil.MyRet ret = res.jsonToObject(QiniuAuthUtil.MyRet.class);
        logger.info(res.toString());
        logger.info(res.bodyString());

        return ret;
    }

    private Media findMedia(String key) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("ukey", key);
        List<Media> medias = list(Media.class, " WHERE ukey = :ukey ", param);
        if (medias != null && medias.size() > 0) {
            return medias.get(0);
        }
        return null;
    }

    public String searchMedia(int pageSize, int page, String id, String title, String category, String status, String creator, String createDate) {

        try {
            String sql = " WHERE 1 = 1 ";
            String orderBySql = " ORDER BY createDate DESC";

            Map<String, Object> param = new HashMap<String, Object>();
            long _id = 0;
            if (NumberUtils.isNumber(id) && (_id = NumberUtils.toLong(id, 0)) > 0) {

                sql += " AND id = :id ";
                param.put("id", _id);
            } else {

                if (StringUtils.isNotBlank(title)) {
                    sql += " AND title like :title ";
                    param.put("title", "%" + title + "%");
                }
                if (NumberUtils.isNumber(category)) {
                    sql += " AND category = :category ";
                    param.put("category", category);
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
            }

            sql += orderBySql;
            Page<Media> mediaPage = getPage(Media.class, sql, param, pageSize, page);

            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (int i = 0; i < mediaPage.getResultList().size(); i++) {

                Media media = mediaPage.getResultList().get(i);
                ObjectNode mediaNode = objectMapper.valueToTree(media);

                switch (mediaNode.findValue("category").getIntValue()) {
                    case 1:
                        mediaNode.put("categoryName", Constant.MEDIA_TYPE_IMAGE[1]);
                        break;
                    case 2:
                        mediaNode.put("categoryName", Constant.MEDIA_TYPE_AUDIO[1]);
                        break;
                    default:
                        mediaNode.put("categoryName", Constant.MEDIA_TYPE_FILE[1]);
                        break;
                }

                mediaNode.put("url", getImageUrl(media));
                arrayNode.add(mediaNode);
            }

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("data", arrayNode);
            objectNode.put("totalData", mediaPage.getTotalData());

            return objectNode.toString();
        } catch (ParseException e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String getInitData() {

        ObjectNode objectNode = objectMapper.createObjectNode();

        ArrayNode categoriesNode = objectMapper.createArrayNode();

        ObjectNode imgFile = objectMapper.createObjectNode();
        imgFile.put("key", Constant.MEDIA_TYPE_IMAGE[0]);
        imgFile.put("name", Constant.MEDIA_TYPE_IMAGE[1]);
        categoriesNode.add(imgFile);

        ObjectNode audioFile = objectMapper.createObjectNode();
        audioFile.put("key", Constant.MEDIA_TYPE_AUDIO[0]);
        audioFile.put("name", Constant.MEDIA_TYPE_AUDIO[1]);
        categoriesNode.add(audioFile);

        ObjectNode otherFile = objectMapper.createObjectNode();
        otherFile.put("key", Constant.MEDIA_TYPE_FILE[0]);
        otherFile.put("name", Constant.MEDIA_TYPE_FILE[1]);
        categoriesNode.add(otherFile);

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

        return objectNode.toString();
    }

    public String updateAcceptMedia(String data) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("mediaIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Media media = find(Media.class, id);

                    if (media.getStatus() != Constant.COMMON_STATUS_ACCEPT) {
                        media.setStatus(Constant.COMMON_STATUS_ACCEPT);
                        baseRepository.update(media);

                        stringBuilder.append(id).append(" ");
                    } else {
                        continue;
                    }
                }
            }

            log("多媒体审核(启用)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode.toString();

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String updateRejectMedia(String data) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("mediaIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Media media = find(Media.class, id);

                    if (media.getStatus() != Constant.COMMON_STATUS_REJECT) {
                        media.setStatus(Constant.COMMON_STATUS_REJECT);
                        baseRepository.update(media);

                        stringBuilder.append(id).append(" ");
                    } else {
                        continue;
                    }
                }
            }

            log("多媒体审核(废弃)", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode.toString();

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }
    
    public String updateMediaTitle(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            while (iterator.hasNext()) {
                ObjectNode next = (ObjectNode) iterator.next();
                long id = next.findValue("id").getLongValue();
                Media media = find(Media.class, id);
                media.setTitle(next.findValue("title").getTextValue());
                baseRepository.update(media);

                stringBuilder.append(id).append(" ");
            }

            // Log in to database.
            log("多媒体更新(标题)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode.toString();

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String updateMediaHandle(String id, String title, String modal, String width, String height, String interlace) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            resultNode.put("result", "success");
            resultNode.put("success", true);
            long _id = NumberUtils.toLong(id, 0);
            if (_id > 0) {

                StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");
                stringBuilder.append(id).append("]");

                Media media = find(Media.class, _id);
                media.setTitle(title);

                int _modal = NumberUtils.toInt(modal, -1);
                int _width = NumberUtils.toInt(width, 0);
                int _height = NumberUtils.toInt(height, 0);
                int _interlace = NumberUtils.toInt(interlace, 0);

                if (_modal >= 0 && _modal <= 5) {
                    media.setHandle(buildImageViewParam(_modal, _width, _height, -1, _interlace));
                } else {
                    media.setHandle("");
                }
                baseRepository.update(media);

                // Log in to database.
                log("多媒体更新(图片操作)", stringBuilder.toString());
            }
            return resultNode.toString();

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String buildImageViewParam(int modal, int width, int height, int format, int interlace) {

        StringBuilder sb = new StringBuilder();

        // modal:[0,5] reference to qiniu's doc
        if (modal >= 0 && modal <= 5 && (width > 0 && width < 9999 || height > 0 && height < 9999)) {
            sb.append(modal).append(";");
            if (width > 0 && width < 9999) {
                sb.append("w").append(":").append(width).append(";");
            }
            if (height > 0 && height < 9999) {
                sb.append("h").append(":").append(height).append(";");
            }
        }

        if (format >= 0 && Constant.IMAGE_FORMAT_TYPES.length > format) {
            sb.append("format").append(":").append(format).append(";");
        }
        if (interlace == 1) {
            sb.append("interlace").append(":").append(interlace).append(";");
        }
        return sb.toString();
    }

    public String getImageUrl(Media media) {

        String _url = qiniuAuthUtil.getAccessDomain() + media.getUkey();

        if (StringUtils.isBlank(media.getHandle())) {
            return _url;
        }
        return _url + "?imageView2/" + media.getHandle().replaceAll(":|;", "/");
    }

    public String getImageUrl(Media media, String handleStr) {

        String _url = qiniuAuthUtil.getAccessDomain() + media.getUkey();

        if (StringUtils.isBlank(handleStr)) {
            return getImageUrl(media);
        }
        return _url + "?imageView2/" + handleStr + "/interlace/1";
    }

    public String getImageUrl(Media media, String handleStr, boolean interlace) {

        String _url = qiniuAuthUtil.getAccessDomain() + media.getUkey();

        if (StringUtils.isBlank(handleStr)) {
            return getImageUrl(media);
        }
        return _url + "?imageView2/" + handleStr + (interlace ? "/interlace/1" : "");
    }

    public String getMediaUrl(Media media) {
        return qiniuAuthUtil.getAccessDomain() + media.getUkey();
    }

    public String updateAddHeadLineMedia(String articleId, String mediaId, String focus) {
        try {
            long _articleId = NumberUtils.toLong(articleId, 0);
            long _mediaId = NumberUtils.toLong(mediaId, 0);

            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("success", true);
            objectNode.put("result", "success");

            if (_articleId <= 0 || _mediaId <= 0) {
                return objectNode.toString();
            }

            Article article = find(Article.class, _articleId);
            Media media = find(Media.class, _mediaId);
            if (article == null || article.getId() == 0 || media == null || media.getId() == 0) {
                return objectNode.toString();
            }

            MediaArticle mart = findMediaArticle(_articleId, media.getCategory());
            if (mart == null) {
                mart = new MediaArticle();
                mart.setArticleId(_articleId);
                mart.setMediaId(_mediaId);
                mart.setMediaCategory(media.getCategory());
                mart.setCreateDate(new Date());
                mart.setId(baseRepository.create(mart));
            } else {
                mart.setMediaId(_mediaId);
                mart.setCreateDate(new Date());
                baseRepository.update(mart);
            }

            boolean _focus = NumberUtils.isNumber(focus) && NumberUtils.toInt(focus, 0) == 1;
            if (_focus && media.getCategory() == NumberUtils.toInt(Constant.MEDIA_TYPE_IMAGE[0])) {
                article.setFocus(1);
                baseRepository.update(article);
            } else {
                article.setFocus(0);
                baseRepository.update(article);
            }

            log("多媒体配置头条文章", "MediaArticle id:" + mart.getId() + "; 焦点图:" + _focus);

            return objectNode.toString();
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public MediaArticle findMediaArticle(long articleId, int category) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("articleId", articleId);
        param.put("category", category);

        List<MediaArticle> mediaArticles = list(MediaArticle.class, " WHERE articleId = :articleId and mediaCategory = :category", param);
        if (mediaArticles != null && mediaArticles.size() != 0) {
            return mediaArticles.get(0);
        }

        return null;
    }

    public List<Media> findMedias(long articleId) {

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("articleId", articleId);
        List<Media> medias = null;

        List<MediaArticle> mediaArticles = list(MediaArticle.class, " WHERE articleId = :articleId ", param);
        if (mediaArticles != null && mediaArticles.size() != 0) {
            medias = new ArrayList<Media>();

            for (int i = 0; i < mediaArticles.size(); i++) {
                MediaArticle mediaArticle = mediaArticles.get(0);
                Media media = find(Media.class, mediaArticle.getMediaId());
                medias.add(media);
            }
        }
        return medias;
    }

    public String updateCancelHeadLineMedia(String data) {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("articleIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Article article = find(Article.class, id);
                    if (article.getFocus() == 1) {
                        article.setFocus(0);
                        baseRepository.update(article);
                    }
                }
            }

            log("多媒体取消焦点图", stringBuilder.append("]").toString());

            objectNode.put("result", "success");
            objectNode.put("success", true);
            return objectNode.toString();

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }
}
