package com.efrobot.talkstory.http;

import java.util.HashMap;
import java.util.Map;

public class HttpParamUtils {

    /**
     * POST URL
     */
    //获取听故事标签
    public static String TAG_LIST_URL = "https://story-robot.dayutang.cn/story/tags";

    //获取音频列表
    public static String AUDIO_LIST_URL = "https://story-robot.dayutang.cn/story/audio/list";

    //获取专辑列表
    public static String ALBUM_LIST_URL = "https://story-robot.dayutang.cn/story/album/list";

    //获取专辑下音频列表
    public static String AUDIO_UNDER_ALBUM_LIST_URL = "https://story-robot.dayutang.cn/story/audio/getListByAlbumId";

    //通过ID获取音频
    public static String AUDIO_BY_ID_URL = "https://story-robot.dayutang.cn/story/audio/%s";

    //通过ID获取专辑
    public static String ALBUM_INFO_BY_ID_URL = "https://story-robot.dayutang.cn/story/album/%s";


    private static HttpParamUtils httpParamUtils;

    public static HttpParamUtils getInstance() {
        if (httpParamUtils == null) {
            httpParamUtils = new HttpParamUtils();
        }
        return httpParamUtils;
    }

    /**
     * 获取音频列表
     *
     * @param page
     * @param size
     * @param keyword
     * @param tagId
     * @return
     */
    public static Map<String, Object> getAudioParamMap(int page, int size, String keyword, int tagId) {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("size", size);
        map.put("keyword", keyword);
        map.put("tagId", tagId);
        return map;
    }


    /**
     * 获取专辑列表参数
     *
     * @param page
     * @param size
     * @param keyword
     * @param tagId
     * @return
     */
    public static Map<String, Object> getAlbumParamMap(int page, int size, String keyword, int tagId) {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("size", size);
        map.put("keyword", keyword);
        map.put("tagId", tagId);
        return map;
    }

    /**
     * 获取专辑下音频列表
     *
     * @param page
     * @param size
     * @param albumId
     * @return
     */
    public static Map<String, Object> getAudioUnderAlbumParamMap(int page, int size, int albumId) {
        Map<String, Object> map = new HashMap<>();
        map.put("page", page);
        map.put("size", size);
        map.put("albumId", albumId);
        return map;
    }

    /**
     * 通过ID获取音频
     *
     * @return
     */
    public static Map<String, Object> getAudioByIdParamMap() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

    /**
     * 通过ID获取专辑
     *
     * @return
     */
    public static Map<String, Object> getAlbumByIdParamMap() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

}
