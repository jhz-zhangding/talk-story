package com.efrobot.talkstory.bean;

import java.util.List;

public class AudiaBean {

    private int total;

    private List<AudiaItemBean> data;

    private int code;

    private String msg;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<AudiaItemBean> getData() {
        return data;
    }

    public void setData(List<AudiaItemBean> data) {
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
