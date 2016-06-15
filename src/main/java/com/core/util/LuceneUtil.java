package com.core.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gxl
 */
public class LuceneUtil {

    private static IndexWriter indexWriter;
    private static IndexSearcher indexSearcher;
    private static IndexReader indexReader;
    private static Directory directory;

    static {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(LuceneConfig.getAnalyzer());
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(getDirectory(), indexWriterConfig);

//            LogMergePolicy mergePolicy = new LogDocMergePolicy();
//            mergePolicy.setMergeFactor(10);
//            mergePolicy.setMaxMergeDocs(10);
//            indexWriterConfig.setMaxBufferedDocs(10);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        indexWriter.close();
                        indexReader.close();
                    } catch (Exception e) {
                        new RuntimeException(e);
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Directory getDirectory() {
        String dir = LuceneConfig.getIndexDir();
        try {

            File file = new File(System.getProperty("webapp.root") + dir);
            if (!file.isDirectory()) {
                file.mkdir();
            }
            directory = FSDirectory.open(file.toPath());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return directory;
    }

    public static IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public static IndexSearcher getIndexSearcher() {
        if (indexReader == null) {
            try {
                indexReader = DirectoryReader.open(getDirectory());
                indexSearcher = new IndexSearcher(indexReader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return indexSearcher;
    }

    /*
     * 更新indexSearcher
     */
    public static void updateIndex() {
        if (indexReader != null) {
            try {
                indexReader.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        indexReader = null;
    }

    /**
     * 创建返回高亮器
     *
     * @param query
     * @return
     * @throws IOException
     * @throws InvalidTokenOffsetsException
     */
    public static Highlighter createHighlighter(Query query) throws IOException, InvalidTokenOffsetsException {
        Formatter formatter = new SimpleHTMLFormatter(LuceneConfig.getPreTag(), LuceneConfig.getPostTag());
        Scorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        return highlighter;
    }

    /**
     * 创建Query对象
     *
     * @param queryStr 查询语句
     * @param fields   索引字段
     * @return
     * @throws IOException
     */
    public static Query createQuery(String queryStr, String[] fields) throws IOException {
        BooleanQuery query = new BooleanQuery();
        Analyzer analyzer = LuceneConfig.getAnalyzer();

        TokenStream content = analyzer.tokenStream("", queryStr);
        content.reset();
        while (content.incrementToken()) {
            CharTermAttribute attribute = content.getAttribute(CharTermAttribute.class);

            for (String field : fields) {
                PhraseQuery phraseQuery = new PhraseQuery();
                phraseQuery.add(new Term(field, attribute.toString()));
                query.add(phraseQuery, BooleanClause.Occur.SHOULD);
            }
        }
        content.close();
        return query;
    }


    public static String Html2Text(String htmlStr) {
        String textStr = "";
        Pattern p_script, p_style, p_html;
        Matcher m_script, m_style, m_html;
        try {
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
            String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); //过滤script标签

            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); //过滤style标签

            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); //过滤html标签

            textStr = htmlStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return textStr;//返回文本字符串
    }

    public String dateFormat(String s) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(Long.parseLong(s)));
    }

}
