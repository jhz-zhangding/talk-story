package com.efrobot.talkstory.utils;

/**
 * 时间工具类
 * Created by zd on 2018/1/11.
 */
public class TimeUtils {

    //时间显示函数,我们获得音乐信息的是以毫秒为单位的，把把转换成我们熟悉的00:00格式
    public static String ShowTime(int time) {
        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

    //时间显示函数,我们获得音乐信息的是以秒为单位的，把把转换成我们熟悉的00:00格式
    public static String ShowMusicTime(int time) {
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d", minute, second);
    }

}
