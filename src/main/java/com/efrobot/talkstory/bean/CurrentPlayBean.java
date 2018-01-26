package com.efrobot.talkstory.bean;

import java.util.List;

/**
 * 当前播放项目数据
 * Created by zd on 2018/1/26.
 */
public class CurrentPlayBean {

    private AudiaItemBean audiaItemBean;

    private VersionBean versionBean;

    public AudiaItemBean getAudiaItemBean() {
        return audiaItemBean;
    }

    public void setAudiaItemBean(AudiaItemBean audiaItemBean) {
        this.audiaItemBean = audiaItemBean;
    }

    public VersionBean getVersionBean() {
        return versionBean;
    }

    public void setVersionBean(VersionBean versionBean) {
        this.versionBean = versionBean;
    }
}
