package com.core.util;

import org.apache.lucene.analysis.Analyzer;

/**
 * Created by gxl
 */
public class LuceneConfig {

    private static Analyzer analyzer;
    private static String indexDir;
    private static String defaultSearchField;
    private static String preTag;
    private static String postTag;

    public static Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        LuceneConfig.analyzer = analyzer;
    }

    public static String[] getDefaultSearchField() {
        return defaultSearchField.split(",");
    }

    public void setDefaultSearchField(String defaultSearchField) {
        LuceneConfig.defaultSearchField = defaultSearchField;
    }

    public static String getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(String indexDir) {
        LuceneConfig.indexDir = indexDir;
    }

    public void setPostTag(String postTag) {
        LuceneConfig.postTag = postTag;
    }

    public void setPreTag(String preTag) {
        LuceneConfig.preTag = preTag;
    }

    public static String getPostTag() {
        return postTag;
    }

    public static String getPreTag() {
        return preTag;
    }
}
