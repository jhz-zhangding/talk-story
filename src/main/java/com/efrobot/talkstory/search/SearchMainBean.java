package com.efrobot.talkstory.search;

import java.util.List;

/**
 * Created by zd on 2018/1/13.
 */
public class SearchMainBean {

    private List<SearchDataBean> data;

    private int code;

    private String msg;

    public List<SearchDataBean> getData() {
        return data;
    }

    public void setData(List<SearchDataBean> data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
