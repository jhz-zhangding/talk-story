package com.efrobot.talkstory.search;

import java.util.List;

/**
 * Created by zd on 2018/1/13.
 */
public class SearchDataBean {

    private String name;

    private List<SearchItemBean> list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SearchItemBean> getList() {
        return list;
    }

    public void setList(List<SearchItemBean> list) {
        this.list = list;
    }
}
