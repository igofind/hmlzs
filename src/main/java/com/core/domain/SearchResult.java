package com.core.domain;

import java.util.List;

/**
 * Created by gxl
 */
public class SearchResult <T>{
    private List<T> list;
    private int count;

    public SearchResult(int count, List<T> list) {
        this.count = count;
        this.list = list;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
