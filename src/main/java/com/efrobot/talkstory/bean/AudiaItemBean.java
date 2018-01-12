package com.efrobot.talkstory.bean;

import java.io.Serializable;
import java.util.List;

public class AudiaItemBean implements Serializable{

    private String albumDes;

    private int albumId;

    private String albumName;

    private String backgroundImg;

    private String bigImg;

    private String des;

    private int id;

    private String name;

    private String rotateImg;

    private String smallImg;

    private int teacherId;

    private String teacherImg;

    private String teacherName;

    private List<VersionBean> versions;

    public String getAlbumDes() {
        return albumDes;
    }

    public void setAlbumDes(String albumDes) {
        this.albumDes = albumDes;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getBackgroundImg() {
        return backgroundImg;
    }

    public void setBackgroundImg(String backgroundImg) {
        this.backgroundImg = backgroundImg;
    }

    public String getBigImg() {
        return bigImg;
    }

    public void setBigImg(String bigImg) {
        this.bigImg = bigImg;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
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

    public String getRotateImg() {
        return rotateImg;
    }

    public void setRotateImg(String rotateImg) {
        this.rotateImg = rotateImg;
    }

    public String getSmallImg() {
        return smallImg;
    }

    public void setSmallImg(String smallImg) {
        this.smallImg = smallImg;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherImg() {
        return teacherImg;
    }

    public void setTeacherImg(String teacherImg) {
        this.teacherImg = teacherImg;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public List<VersionBean> getVersions() {
        return versions;
    }

    public void setVersions(List<VersionBean> versions) {
        this.versions = versions;
    }
}
