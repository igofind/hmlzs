package com.core.service;


import com.core.domain.Article;
import com.core.domain.SearchResult;
import com.core.util.ArticleDocumentUtil;
import com.core.util.LuceneConfig;
import com.core.util.LuceneUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.util.automaton.TooComplexToDeterminizeException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gxl
 */
@Service
public class ArticleIndexService {

    @Autowired
    LuceneConfig luceneConfig;

    /**
     * 创建文章索引
     *
     * @param article
     */
    public void addArticle(Article article) {
        IndexWriter writer = null;
        try {
            writer = LuceneUtil.getIndexWriter();
            writer.addDocument(ArticleDocumentUtil.article2Document(article));
            writer.commit();
            LuceneUtil.updateIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 一次创建多个文章索引
     *
     * @param articles
     */
    public void addArticle(ArrayList<Article> articles) {
        IndexWriter writer = null;
        try {
            writer = LuceneUtil.getIndexWriter();
            for (Article article : articles) {
                writer.addDocument(ArticleDocumentUtil.article2Document(article));
            }
            writer.commit();
            LuceneUtil.updateIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新文章索引
     *
     * @param article
     */
    public void updateArticle(Article article) {
        IndexWriter writer = null;
        try {
            writer = LuceneUtil.getIndexWriter();
            writer.updateDocument(new Term("id", article.getId() + ""), ArticleDocumentUtil.article2Document(article));
            writer.commit();
            LuceneUtil.updateIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 一次更新多个文章
     *
     * @param articles
     */
    public void updateArticle(ArrayList<Article> articles) {
        IndexWriter writer = null;
        try {
            writer = LuceneUtil.getIndexWriter();
            for (Article article : articles) {
                writer.updateDocument(new Term("id", article.getId() + ""), ArticleDocumentUtil.article2Document(article));
            }
            writer.commit();
            LuceneUtil.updateIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除索引
     *
     * @param id
     */
    public void deleteArticle(Integer id) {
        IndexWriter writer = null;
        try {
            writer = LuceneUtil.getIndexWriter();
            writer.deleteDocuments(new Term("id", id.toString()));
            writer.commit();
            LuceneUtil.updateIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteArticle(ArrayList<Long> ids) {
        IndexWriter writer = null;
        try {
            writer = LuceneUtil.getIndexWriter();
            for (Long id : ids) {
                writer.deleteDocuments(new Term("id", id.toString()));
            }
            writer.commit();
            LuceneUtil.updateIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 搜索
     *
     * @param kw
     * @param maxNum
     * @return
     */
    public SearchResult<Article> searchArticle(String kw, int maxNum) {
        List<Article> list = new ArrayList<Article>();
        try {
            //1.创建query
            QueryParser queryParser = new MultiFieldQueryParser(new String[]{"title", "content"}, LuceneConfig.getAnalyzer());
            Query query = queryParser.parse(kw);
            //2、创建indexSearch
            IndexSearcher indexSearcher = LuceneUtil.getIndexSearcher();

            TopDocs topDocs = indexSearcher.search(query, maxNum);
            int count = topDocs.totalHits;
            ScoreDoc[] docs = topDocs.scoreDocs;
            //3.创建高亮器
            Highlighter highlighter = LuceneUtil.createHighlighter(query);
            for (int i = 0; i < docs.length; i++) {
                list.add(ArticleDocumentUtil.document2Article(indexSearcher.doc(docs[i].doc), highlighter));
            }
            return new SearchResult<Article>(count, list);
        } catch (TooComplexToDeterminizeException e) {
            return new SearchResult<Article>(0, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 分页搜索
     *
     * @param kw       关键字
     * @param startNum 开始索引
     * @param maxNum   搜索个数
     * @return
     */
    public SearchResult<Article> searchArticle(String kw, int startNum, int maxNum) {
        List<Article> list = new ArrayList<Article>();
        IndexSearcher indexSearcher = null;
        try {
            //1.创建query
            QueryParser queryParser = new MultiFieldQueryParser(new String[]{"title", "content"}, LuceneConfig.getAnalyzer());
            Query query = queryParser.parse(kw);
            //2、创建indexSearch
            indexSearcher = LuceneUtil.getIndexSearcher();

            TopDocs topDocs = indexSearcher.search(query, startNum + maxNum);
            int count = topDocs.totalHits;

            ScoreDoc[] docs = topDocs.scoreDocs;
            int minNum = Math.min(docs.length, startNum + maxNum);
            //3.创建高亮器
            Highlighter highlighter = LuceneUtil.createHighlighter(query);

            for (int i = startNum; i < minNum; i++) {
                Document document = indexSearcher.doc(docs[i].doc);
                list.add(ArticleDocumentUtil.document2Article(document, highlighter));
            }

            return new SearchResult<Article>(count, list);
        } catch (TooComplexToDeterminizeException e) {
            return new SearchResult<Article>(0, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public SearchResult<Article> searchArticle(Query query, int startNum, int maxNum, Sort sort) {
        List<Article> list = new ArrayList<Article>();
        IndexSearcher indexSearcher = null;
        indexSearcher = LuceneUtil.getIndexSearcher();
        TopDocs topDocs = null;
        try {
            if (sort != null) {
                topDocs = indexSearcher.search(query, startNum + maxNum, sort);
            } else {
                topDocs = indexSearcher.search(query, startNum + maxNum);
            }
            int count = topDocs.totalHits;

            ScoreDoc[] docs = topDocs.scoreDocs;
            int minNum = Math.min(docs.length, startNum + maxNum);

            Highlighter highlighter = LuceneUtil.createHighlighter(query);

            for (int i = startNum; i < minNum; i++) {
                Document document = indexSearcher.doc(docs[i].doc);
                list.add(ArticleDocumentUtil.document2Article(document, highlighter));
            }
            return new SearchResult<Article>(count, list);
        } catch (TooComplexToDeterminizeException e) {
            return new SearchResult<Article>(0, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //
    public void deleteArticle(String data) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();
            ArrayList<Long> ids = new ArrayList<Long>();
            while (iterator.hasNext()) {
                ids.add(iterator.next().getLongValue());
            }
            if (ids.size() > 0) {
                this.deleteArticle(ids);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void updateStaticize(String data, ArticleService articleService) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();
            ArrayList<Article> addArticles = new ArrayList<Article>();
            ArrayList<Article> updateArticles = new ArrayList<Article>();
            while (iterator.hasNext()) {
                Article article = articleService.find(Article.class, iterator.next().getLongValue());
                article.setContent(articleService.getArticleConetnt(article));
                ;
                if (article.getUpdateDate() != null) {
                    updateArticles.add(article);
                } else {
                    addArticles.add(article);
                }
            }
            // 批量建索引
            if (addArticles.size() > 0) {
                this.addArticle(addArticles);
            }
            if (updateArticles.size() > 0) {
                this.updateArticle(updateArticles);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
