package com.core.util;

/**
 * Created by sunpeng
 */
public class Constant {
    public static final String ENCODE_UTF8 = "UTF-8";

    public static final String VELOCITY_TEMPLATE_OUT_PATH = "article";
    public static final String STATIC_RESOURCE_URL_PREFIX = "resURLPrefix";
    public static final String LIST_PAGE_URL_PREFIX = "listURLPrefix";
    public static final String WINDOW_TITLE_SUFFIX = "_黄梅老祖寺";

    /* common*/
    public static final int COMMON_STATUS_REJECT = -1;
    public static final int COMMON_STATUS_NORMAL = 0;
    public static final int COMMON_STATUS_ACCEPT = 1;

    /* class Tree*/
    public static final int TREE_NO_CHILD = 0;
    public static final int TREE_HAS_CHILD = 1;

    public static final int TREE_ROAD = 0;
    public static final int STORE_PATH = 1;

    /* class Category*/
    public static final int CATEGORY_NO_CHILD = 0;
    public static final int CATEGORY_HAS_CHILD = 1;

    /* article content length */
    public static final int ARTICLE_CONTENT_LENGTH = 255;

    /* manager */
    public static final int USER_MANAGER_DEGRADE = 0;
    public static final int USER_MANAGER_UPGRADE = 1;

    public static final int HOMEPAGE_NAV_MAIN = 0;
    public static final int HOMEPAGE_NAV_SECOND = 1;
    public static final int HOMEPAGE_BANNER = 2;
    public static final String NAV_SPLIT_TOKEN = "@";
    public static final String FUTIAN_SPLIT_TOKEN = "@";
    public static final int FUTIAN_SPLIT_SIZE = 5;

    public static final int NAV_SPLIT_SIZE = 3;

    // reference CATEGORY_ID_NAVBANNER
    public static final int ARTICLE_NAV_ID = 1;
    public static final int ARTICLE_FUTIAN_ID = 2;

    public static final int ARTICLE_HEADLINE = 1;
    public static final int ARTICLE_CANCEL_HEADLINE = 0;
    public static final int ARTICLE_STATUS_HIDDEN = -2;

    // category
    public static final int CATEGORY_ID_NAVBANNER = 1;
    public static final int CATEGORY_ID_NEWS = 2;
    public static final int CATEGORY_ID_LIFE = 3;
    public static final int CATEGORY_ID_ZIYUNFOGUO = 4;
    public static final int CATEGORY_ID_CORPUS = 5;
    public static final int CATEGORY_ID_KNOWLEDGE = 6;
    public static final int CATEGORY_ID_AUDIO = 7;
    public static final int CATEGORY_ID_ZHUYINGPOSUO = 8;
    public static final int CATEGORY_ID_LAW = 9;
    public static final int CATEGORY_ID_GUANGZHONGFUTIAN = 10;
    public static final int CATEGORY_ID_NOTICE = 11;
    public static final int CATEGORY_ID_CONTACT= 12;

    public static final String ARTICLE_LIST_PAGE_NAME = "articleList";
    public static final String PHOTO_LIST_PAGE_NAME = "photoList";

    // media cateory
    public static final String[] MEDIA_TYPE_FILE = {"0", "其他"};
    public static final String[] MEDIA_TYPE_IMAGE = {"1", "图片"};
    public static final String[] MEDIA_TYPE_AUDIO = {"2", "音频"};

    // img regex
    public static final String NO_SUPPORT_TYPE = "不支持的上传类型!";
    public static final String IMG_FILTER = "^.+\\.(jpg|jpeg|gif|png|bmp)$";
    public static final String IMG_FILTER_MSG = "上传失败: 不支持的图片类型! 请上传jpg|jpeg|gif|png|bmp格式的图片!";
    public static final String AUDIO_FILTER = "^.+\\.(mp3)$";
    public static final String AUDIO_FILTER_MSG = "上传失败: 不支持的音频类型! 请上传mp3格式的音频文件!";

    // media param
    public static final String[] IMAGE_FORMAT_TYPES = {"jpg", "gif", "png", "bmp"};
    /*public static final String media_param_modal = "modal";// 缩略模式
    public static final String media_param_width = "width";// 宽
    public static final String media_param_height = "height"; // 高
    public static final String media_param_format = "format"; // 格式转换：png->jpg
    public static final String media_param_interlace = "interlace"; // 渐进显示*/

    public static final int TEMPLATE_FILE_BUFF_SIZE = 1024 * 2;

    public static final int TEMPLATE_BUILD_IN_INDEX = 1;
    public static final int TEMPLATE_BUILD_IN_HEAD = 2;
    public static final int TEMPLATE_BUILD_IN_NAV = 3;
    public static final int TEMPLATE_BUILD_IN_NOTICE = 4;
    public static final int TEMPLATE_BUILD_IN_BANNER = 5;
    public static final int TEMPLATE_BUILD_IN_SCRIPT = 6;
    public static final int TEMPLATE_BUILD_IN_FAWU = 7;
    public static final int TEMPLATE_BUILD_IN_FUTIAN = 8;
    public static final int TEMPLATE_BUILD_IN_FOOT = 9;
    public static final int TEMPLATE_BUILD_IN_ARTICLELIST = 10;
    public static final int TEMPLATE_BUILD_IN_PHOTOLIST = 11;
    public static final int TEMPLATE_BUILD_IN_ARTICLE1 = 12;
    public static final int TEMPLATE_BUILD_IN_ARTICLE2 = 13;
    public static final int TEMPLATE_BUILD_IN_SEARCHLIST = 14;



    //lucene
    public static final int LUCENE_CONTENT_FRAGMENT_LEN=100;
    public static final int LUCENE_TITLE_FRAGMENT_LEN=30;
    public static final int LUCENE_SEARCH_PAGESIZE=10;
}
