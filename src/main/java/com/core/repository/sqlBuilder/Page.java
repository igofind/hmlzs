package com.core.repository.sqlBuilder;

import java.util.List;

/**
 * Created by sunpeng
 */
public class Page<T> {

    private int pageSize = 20;

    private int pageNum = 1;

    private int totalData;

    private List<T> resultList;

    public int getTotalPage() {

        return (totalData / pageSize) + (totalData % pageSize == 0 ? 0 : 1);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getTotalData() {
        return totalData;
    }

    public void setTotalData(int totalData) {
        this.totalData = totalData;
    }

    public List<T> getResultList() {
        return resultList;
    }

    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }
}
