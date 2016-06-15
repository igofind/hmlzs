package com.core.domain;

/**
 * Created by gxl
 */
public class SearchResultPage <T>{
    private SearchResult<T> searchResult;
    private int currentPage;
    private int pageSize;

    private int pageCount;

    private int beginPageIndex;
    private int endPageIndex;

    public SearchResultPage( SearchResult<T> searchResult,int currentPage,int pageSize) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.searchResult = searchResult;
        int count = searchResult.getCount();
        this.pageCount = count/pageSize+(count%pageSize==0?0:1);

        if(pageCount<10){//总页数小于10
            beginPageIndex=1;
            endPageIndex=pageCount;
        }else{
            if(currentPage-5<=0){//当前页为前5页
                beginPageIndex=1;
                endPageIndex=10;
            }else if(currentPage+5>pageCount){//当前页+5 超出总页
                beginPageIndex=currentPage-4;
                endPageIndex=pageCount;
            }else{
                beginPageIndex=currentPage-4;
                endPageIndex=currentPage+5;
            }
        }

    }


    public int getBeginPageIndex() {
        return beginPageIndex;
    }

    public void setBeginPageIndex(int beginPageIndex) {
        this.beginPageIndex = beginPageIndex;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getEndPageIndex() {
        return endPageIndex;
    }

    public void setEndPageIndex(int endPageIndex) {
        this.endPageIndex = endPageIndex;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public SearchResult<T> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult<T> searchResult) {
        this.searchResult = searchResult;
    }
}
