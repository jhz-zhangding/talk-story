package com.efrobot.talkstory.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.env.Constants;

import java.util.ArrayList;

/**
 * Created by zd on 2018/1/12.
 */
public class HistoryManager {

    private static HistoryManager instance;

    public static Context mContext;

    private static String CONTENT_TABLE = "historybean";

    private static SQLiteDatabase db = null;

    public static HistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new HistoryManager();
        }
        mContext = context;
        db = TalkStoryApplication.from(mContext).getDataBase().getWritableDatabase();
        return instance;
    }

    /**
     * 查询所插入项的内容
     *
     * @return 返回动作数据集合
     */
    public ArrayList<HistoryBean> queryAllContent() {
        ArrayList<HistoryBean> beans = new ArrayList<>();
        Cursor c = null;
        try {
            c = db.query(CONTENT_TABLE, null, null, null, null, null, null);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    beans.add(new HistoryBean(c));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return beans;
    }

    public void updateItem(HistoryBean bean) {
        if (bean == null)
            return;
        ContentValues values = new ContentValues();
        values.put("id", bean.getId());
        values.put("name", bean.getName());
        values.put("teacherName", bean.getName());
        values.put("audioPath", bean.getAudioPath());
        values.put("audioUrl", bean.getAudioUrl());
        values.put("playTime", bean.getPlayTime());
        values.put("type", bean.getType());
        values.put("smallImg", bean.getSmallImg());
        db.update(CONTENT_TABLE, values, "id=? ", new String[]{bean.getId() + ""});
    }

    public void insertContent(HistoryBean bean) {
        if (bean == null)
            return;
        ContentValues values = new ContentValues();
        values.put("id", bean.getId());
        values.put("name", bean.getName());
        values.put("teacherName", bean.getTeacherName());
        values.put("audioPath", bean.getAudioPath());
        values.put("audioUrl", bean.getAudioUrl());
        values.put("playTime", bean.getPlayTime());
        values.put("type", bean.getType());
        values.put("smallImg", bean.getSmallImg());
        db.insert(CONTENT_TABLE, null, values);
    }

    /**
     * 查询id
     */
    public boolean queryIdExits(int id) {
        Cursor c = null;
        try {
            c = db.query(CONTENT_TABLE, null, "id=? ", new String[]{id + ""}, null, null, null);
            if (c != null && c.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return false;
    }

    /**
     * 根据id删除某个项目下的某个内容
     *
     * @param id
     */
    public void deleteContentById(int id) {
        db.delete(CONTENT_TABLE, "id = ? ", new String[]{id + ""});
    }

    /**
     * 删除项目下的某个内容
     */
    public void delete() {
        db.delete(CONTENT_TABLE, null, null);
    }

}
