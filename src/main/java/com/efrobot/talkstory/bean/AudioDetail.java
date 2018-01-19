package com.efrobot.talkstory.bean;

/**
 * Created by zd on 2018/1/10.
 */
public class AudioDetail {

    private AudiaItemBean data;

    private int code;

    private String msg;

    public AudiaItemBean getData() {
        return data;
    }

    public void setData(AudiaItemBean data) {
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
