package com.efrobot.talkstory.bean;

import java.util.List;

/**
 * 精品bean
 * Created by zd on 2017/12/20.
 */
public class AlbumBean {

    private int total;

    private List<AlbumItemBean> data;

    private int code;

    private String msg;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<AlbumItemBean> getData() {
        return data;
    }

    public void setData(List<AlbumItemBean> data) {
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
