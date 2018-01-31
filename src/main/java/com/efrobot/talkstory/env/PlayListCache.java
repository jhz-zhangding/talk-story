package com.efrobot.talkstory.env;

import android.content.Context;
import android.text.TextUtils;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.utils.PreferencesUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 缓存当前播放列表 用于切换上一条和下一条
 * Created by zd on 2018/1/15.
 */
public class PlayListCache {

    public static PlayListCache instance;

    public static List<AudiaItemBean> list = new ArrayList<>();

    private static Context mContext;

    private int currentPlayMode = 1;

    public static PlayListCache getInstance(Context context) {
        if (instance == null) {
            instance = new PlayListCache();
        }
        mContext = context;
        return instance;
    }

    /**
     * 设置当前播放列表
     *
     * @param list
     */
    public void setList(List<AudiaItemBean> list) {
        if (PlayListCache.list != null) {
            PlayListCache.list.clear();
        }
        PlayListCache.list.addAll(list);

        //当前播放列表保存到手机内存当中
        setList(new Gson().toJson(list));
    }

    /**
     * 将播放列表保存到手机内存
     *
     * @param dataJsonStr
     */
    public void setList(String dataJsonStr) {
        PreferencesUtils.putString(mContext, Constants.MUSIC_LIST_DATA_CACHE, dataJsonStr);
    }

    public List<AudiaItemBean> getList() {
        List<AudiaItemBean> list = new ArrayList<>();
        if (PlayListCache.list != null && PlayListCache.list.size() > 0) {
            list.addAll(PlayListCache.list);
        } else {

            String data = PreferencesUtils.getString(mContext, Constants.MUSIC_LIST_DATA_CACHE);
            if (!TextUtils.isEmpty(data)) {
                List<AudiaItemBean> datalist = new Gson().fromJson(data, new TypeToken<List<AudiaItemBean>>() {
                }.getType());
                if (datalist != null && datalist.size() > 0) {
                    list = datalist;
                }
            }
        }
        return list;
    }

    private HistoryBean gerateHistoryData(AudiaItemBean audiaItemBean) {
        HistoryBean historyBean = new HistoryBean();

        if (audiaItemBean == null || audiaItemBean.getVersions() == null || audiaItemBean.getVersions().size() <= 0) {
            return null;
        }

        VersionBean versionBean = audiaItemBean.getVersions().get(0);
        if (audiaItemBean != null && versionBean != null) {
            historyBean.setId(audiaItemBean.getId());
            historyBean.setName(audiaItemBean.getName());
            historyBean.setTeacherName(audiaItemBean.getTeacherName());
            historyBean.setSmallImg(audiaItemBean.getSmallImg());
            historyBean.setAudioPath(versionBean.getAudioPath());
            historyBean.setAudioUrl(versionBean.getAudioUrl());
            historyBean.setPlayTime(versionBean.getPlayTime());
            historyBean.setType(versionBean.getType());
        }
        return historyBean;
    }

    /**
     * 获取当前列表的下一条音频
     *
     * @param id
     * @return
     */
    public AudiaItemBean getNextAudio(int id) {
        currentPlayMode = PreferencesUtils.getInt(mContext, "playMode", Constants.ORDER_PLAY_MODE);
        L.e("PlayListCache", "getNextAudio");
        boolean isContains = false;
        AudiaItemBean audiaItemBean = null;
        int nextAudioIndex = 0;
        List<AudiaItemBean> mList = getList();
        if (mList != null && mList.size() > 0) {
            for (int i = 0; i < mList.size(); i++) {
                if (id == mList.get(i).getId()) {
                    nextAudioIndex = i;
                    isContains = true;
                    break;
                }
            }

            switch (currentPlayMode) {
                case Constants.ORDER_PLAY_MODE:
                    nextAudioIndex = nextAudioIndex + 1;
                    if (nextAudioIndex >= mList.size()) {
                        nextAudioIndex = 0;
                    }
                    break;
                case Constants.RANDOM_PLAY_MODE:
                    nextAudioIndex = new Random().nextInt(mList.size());
                    break;
                case Constants.CIRCEL_PLAY_MODE:
                    nextAudioIndex = nextAudioIndex + 1;
                    break;
            }

            if (!isContains) {
                nextAudioIndex = 0;
            }

            if (nextAudioIndex >= mList.size()) {
                nextAudioIndex = 0;
            }
            L.e("PlayListCache", "nextAudioIndex = " + nextAudioIndex);
            audiaItemBean = mList.get(nextAudioIndex);
        }
        return audiaItemBean;
    }

    /**
     * 获取当前列表的上一条数据
     *
     * @param id
     * @return
     */
    public AudiaItemBean getLastAudio(int id) {
        L.e("PlayListCache", "");
        boolean isContains = false;
        AudiaItemBean audiaItemBean = null;
        int lastAudioIndex = 0;
        List<AudiaItemBean> mList = getList();
        if (mList != null && mList.size() > 0) {
            for (int i = 0; i < mList.size(); i++) {
                if (id == mList.get(i).getId()) {
                    lastAudioIndex = i;
                    isContains = true;
                    break;
                }
            }

            switch (currentPlayMode) {
                case Constants.ORDER_PLAY_MODE:
                    lastAudioIndex = lastAudioIndex - 1;
                    if (lastAudioIndex < 0) {
                        lastAudioIndex = mList.size() - 1;
                    }
                    break;
                case Constants.RANDOM_PLAY_MODE:
                    lastAudioIndex = new Random().nextInt(mList.size());
                    break;
                case Constants.CIRCEL_PLAY_MODE:

                    break;
            }

            if (!isContains) {
                lastAudioIndex = 0;
            }

            if (lastAudioIndex < 0) {
                lastAudioIndex = 0;
            }

            audiaItemBean = mList.get(lastAudioIndex);

        }
        return audiaItemBean;
    }


}
