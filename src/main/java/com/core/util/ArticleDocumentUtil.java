package com.core.util;

import com.core.domain.Article;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by gxl
 */
public class ArticleDocumentUtil {

    /**
     * Article2Document
     * @param article
     * @return
     */
    public static Document article2Document(Article article) {
        Document doc = new Document();
        //排序字段

        Date publishDate=new Date();
        if(article.getPublishDate()!=null){
            publishDate = article.getPublishDate();
        }
        doc.add(new NumericDocValuesField("publishDate", publishDate.getTime()));

        doc.add(new StringField("id", article.getId()+"", Field.Store.YES));//
        doc.add(new StringField("source",StringUtils.isEmpty(article.getSource())?"":article.getSource(), Field.Store.YES));
        doc.add(new StringField("author",StringUtils.isEmpty(article.getAuthor())?"":article.getAuthor(), Field.Store.YES));

        doc.add(new TextField("title", StringUtils.isEmpty(article.getTitle())?"":article.getTitle(), Field.Store.YES));
        doc.add(new TextField("depict",StringUtils.isEmpty(article.getDepict())?"":article.getDepict(), Field.Store.YES));
        doc.add(new TextField("content",StringUtils.isEmpty(article.getContent())?"": LuceneUtil.Html2Text(article.getContent()), Field.Store.YES));
        doc.add(new StoredField("url", StringUtils.isEmpty(article.getUrl())?"":article.getUrl()));
        doc.add(new StringField("categoryId", article.getCategoryId()+"", Field.Store.NO));

        doc.add(new LongField("publishDate",publishDate.getTime(), Field.Store.YES));
        return doc;
    }

    /**
     * Document2Article
     * @param document
     * @return
     */
    public static Article document2Article(Document document,Highlighter highlighter) throws ParseException, IOException, InvalidTokenOffsetsException {

        String title = document.get("title");
        String htitle = highlighter.getBestFragment(LuceneConfig.getAnalyzer(),"title",title);
        if(htitle==null) {
            int minLen = Math.min(Constant.LUCENE_TITLE_FRAGMENT_LEN, title.length());
            title = title.substring(0,minLen);
        }else {
            title = htitle;
        }

        String content = document.get("content");
        String hcontent = highlighter.getBestFragment(LuceneConfig.getAnalyzer(),"content",content);
        if(hcontent==null) {
            int minLen = Math.min(Constant.LUCENE_CONTENT_FRAGMENT_LEN, content.length());
            content = content.substring(0,minLen);
        }else{
            content = hcontent;
        }

        String createTimeStr = document.get("createDate");
        String publishTimeStr = document.get("publishDate");
        long createTime,publishTime;
        Date createDate=null,publishDate=null;

        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);

        article.setUrl(document.get("url"));
        if (StringUtils.isNotBlank(createTimeStr)){
            createTime = NumberUtils.toLong(createTimeStr, 0);
            if(createTime!=0) {
                createDate = new Date(createTime);
            }
        }
        if(StringUtils.isNotBlank(publishTimeStr)){
            publishTime = NumberUtils.toLong(publishTimeStr,0);
            if(publishTime!=0) {
                publishDate = new Date(publishTime);
            }
        }
        article.setCreateDate(createDate);
        article.setPublishDate(publishDate);

        return article;
    }
}
