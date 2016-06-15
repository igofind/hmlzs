package com.core.domain;

import java.util.Date;

/**
 * Created by sunpeng
 */
public class MediaArticle extends Base{

    private long id;
    private long mediaId;
    private long articleId;

    private int mediaCategory;

    private Date createDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMediaId() {
        return mediaId;
    }

    public void setMediaId(long mediaId) {
        this.mediaId = mediaId;
    }

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(long articleId) {
        this.articleId = articleId;
    }

    public int getMediaCategory() {
        return mediaCategory;
    }

    public void setMediaCategory(int mediaCategory) {
        this.mediaCategory = mediaCategory;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
