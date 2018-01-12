package com.efrobot.talkstory.bean;

/**
 * Created by zd on 2018/1/10.
 */
public class AlbumDetail {

    private AlbumItemBean data;

    private int code;

    private String msg;

    public AlbumItemBean getData() {
        return data;
    }

    public void setData(AlbumItemBean data) {
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
