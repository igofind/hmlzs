package com.core.util;

import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by sunpeng
 */
public class QiniuAuthUtil {

    private Auth auth;

    private String bucket;

    private String accessDomain;

    @Autowired
    private UploadManager uploadManager;

    public QiniuAuthUtil(String accessKey, String secretKey, String bucket, String accessDomain) {
        this.auth = Auth.create(accessKey, secretKey);
        this.bucket = bucket;
        this.accessDomain = accessDomain;
    }

    public Auth getAuth() {
        return auth;
    }

    public String getAccessDomain() {
        return accessDomain;
    }

    public UploadManager getUploadManager() {
        return uploadManager;
    }

    public void setUploadManager(UploadManager uploadManager) {
        this.uploadManager = uploadManager;
    }

    // 简单上传，使用默认策略
    public String getUpToken0(String bucket) {
        return auth.uploadToken(bucket);
    }

    public String getUpToken0() {
        return auth.uploadToken(bucket);
    }

    // 覆盖上传
    public String getUpToken1(String key) {
        return auth.uploadToken(bucket, key);
    }

    // 设置指定上传策略
    public String getUpToken2() {
        return getUpToken2(bucket);
    }

    public String getUpToken2(String bucket) {
        return auth.uploadToken(bucket, null, 3600, new StringMap()
                .putNotEmpty("returnBody", "{\"key\": $(key),\"size\": $(fsize),\"type\": $(mimeType), \"hash\": $(etag), \"width\": $(imageInfo.width), \"height\": $(imageInfo.height)}"));
    }

    // 设置预处理、去除非限定的策略字段
    private String getUpToken3() {
        return auth.uploadToken(bucket, null, 3600, new StringMap()
                .putNotEmpty("persistentOps", "").putNotEmpty("persistentNotifyUrl", "")
                .putNotEmpty("persistentPipeline", ""), true);
    }

    /**
     * 生成上传token
     *
     * @param bucket  空间名
     * @param key     key，可为 null
     * @param expires 有效时长，单位秒。默认3600s
     * @param policy  上传策略的其它参数，如 new StringMap().put("endUser", "uid").putNotEmpty("returnBody", "")。
     *                scope通过 bucket、key间接设置，deadline 通过 expires 间接设置
     * @param strict  是否去除非限定的策略字段，默认true
     * @return 生成的上传token
     */
    public String uploadToken(String bucket, String key, long expires, StringMap policy, boolean strict) {
        return "";
    }

    public class MyRet {
        public String key;
        public long size;
        public String type;
        public String hash;
        public int width;
        public int height;
    }

    private String getUpToken() {
        return auth.uploadToken("bucket", null, 3600, new StringMap()
                .putNotEmpty("returnBody", "{\"key\": $(key), \"hash\": $(etag), \"width\": $(imageInfo.width), \"height\": $(imageInfo.height)}"));
    }
}
