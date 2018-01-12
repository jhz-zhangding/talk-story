package com.efrobot.talkstory.bean;

import android.database.Cursor;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by zd on 2018/1/12.
 */
public class HistoryBean implements Serializable {

    @DatabaseField(columnName = "id")
    private int id;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "teacherName")
    private String teacherName;

    @DatabaseField(columnName = "audioPath")
    private String audioPath;

    @DatabaseField(columnName = "audioUrl")
    private String audioUrl;

    @DatabaseField(columnName = "playTime")
    private int playTime;


    @DatabaseField(columnName = "type")
    private int type;

    @DatabaseField(columnName = "smallImg")
    private String smallImg;

    public HistoryBean() {

    }

    public HistoryBean(Cursor c) {
        id = c.getInt(c.getColumnIndexOrThrow("id"));
        name = c.getString(c.getColumnIndexOrThrow("name"));
        teacherName = c.getString(c.getColumnIndexOrThrow("teacherName"));
        audioPath = c.getString(c.getColumnIndexOrThrow("audioPath"));
        audioUrl = c.getString(c.getColumnIndexOrThrow("audioUrl"));
        playTime = c.getInt(c.getColumnIndexOrThrow("playTime"));
        type = c.getInt(c.getColumnIndexOrThrow("type"));
        smallImg = c.getString(c.getColumnIndexOrThrow("smallImg"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSmallImg() {
        return smallImg;
    }

    public void setSmallImg(String smallImg) {
        this.smallImg = smallImg;
    }
}
