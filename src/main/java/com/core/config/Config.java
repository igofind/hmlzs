package com.core.config;

/**
 * Created by sunpeng
 */
public class Config {

    public Config(String platform, String database) {
        PLATFORM = platform;
        DATABASE = database;
    }

    private final String PLATFORM;
    private final String DATABASE;

    private String domain;
    private String listDomain;
    private String listParam;
    private String staticResourceURLPrefix;

    private String homePageFocusBigImg;
    private String homePageFocusSmallImg;

    private String homePageLifeImg;
    private String homePageCorpusImg;
    private String homePageZhuYingImg;

    private String initialPwd;

    private long dbIdBuffSize;

    private long articleIdAddend;

    private String templateDir;
    private String previewDir;

    public long getDbIdBuffSize() {
        return dbIdBuffSize;
    }

    public void setDbIdBuffSize(long dbIdBuffSize) {
        this.dbIdBuffSize = dbIdBuffSize;
    }

    public long getArticleIdAddend() {
        return articleIdAddend;
    }

    public void setArticleIdAddend(long articleIdAddend) {
        this.articleIdAddend = articleIdAddend;
    }

    public String getPLATFORM() {
        return PLATFORM;
    }

    public String getDATABASE() {
        return DATABASE;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public String getPreviewDir() {
        return previewDir;
    }

    public void setPreviewDir(String previewDir) {
        this.previewDir = previewDir;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getListDomain() {
        return listDomain;
    }

    public void setListDomain(String listDomain) {
        this.listDomain = listDomain;
    }

    public String getListParam() {
        return listParam;
    }

    public void setListParam(String listParam) {
        this.listParam = listParam;
    }

    public String getInitialPwd() {
        return initialPwd;
    }

    public void setInitialPwd(String initialPwd) {
        this.initialPwd = initialPwd;
    }

    public String getStaticResourceURLPrefix() {
        return staticResourceURLPrefix;
    }

    public void setStaticResourceURLPrefix(String staticResourceURLPrefix) {
        this.staticResourceURLPrefix = staticResourceURLPrefix;
    }

    public String getHomePageFocusBigImg() {
        return homePageFocusBigImg;
    }

    public void setHomePageFocusBigImg(String homePageFocusBigImg) {
        this.homePageFocusBigImg = homePageFocusBigImg;
    }

    public String getHomePageFocusSmallImg() {
        return homePageFocusSmallImg;
    }

    public void setHomePageFocusSmallImg(String homePageFocusSmallImg) {
        this.homePageFocusSmallImg = homePageFocusSmallImg;
    }

    public String getHomePageLifeImg() {
        return homePageLifeImg;
    }

    public void setHomePageLifeImg(String homePageLifeImg) {
        this.homePageLifeImg = homePageLifeImg;
    }

    public String getHomePageCorpusImg() {
        return homePageCorpusImg;
    }

    public void setHomePageCorpusImg(String homePageCorpusImg) {
        this.homePageCorpusImg = homePageCorpusImg;
    }

    public String getHomePageZhuYingImg() {
        return homePageZhuYingImg;
    }

    public void setHomePageZhuYingImg(String homePageZhuYingImg) {
        this.homePageZhuYingImg = homePageZhuYingImg;
    }
}
