package com.efrobot.talkstory.http;

import android.text.TextUtils;

import com.efrobot.library.mvp.utils.L;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.common.Callback.Cancelable;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.http.app.ResponseParser;
import org.xutils.http.request.UriRequest;
import org.xutils.x;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtils {

    //合作伙伴ID
    public static String partner = "story-robot";

    //secretKey
    public static String secretKey = "storyrobot*^$@%&2018";

    public Map<String, Object> tokenMap;

    public HttpUtils(boolean isNeedRefreshToken) {
        if (isNeedRefreshToken) {
            tokenMap = getBaseAccessToken();
        } else {
            if (tokenMap == null) {
                tokenMap = getBaseAccessToken();
            }
        }
    }


    public Map<String, Object> getBaseAccessToken() {
        Map<String, Object> map = new HashMap<>();
        long timestamp = System.currentTimeMillis() / 1000;
        map.put("partner", partner);
        map.put("timestamp", timestamp);
        String md5Token = md5(timestamp + secretKey);
        map.put("token", md5Token);
        return map;
    }


    /**
     * 发送get请求
     *
     * @param <T>
     */
    public <T> Cancelable Get(String url, Map<String, Object> map, CommonCallback<T> callback) {
        RequestParams params = new RequestParams(url);
        if (null != map) {
            map.putAll(tokenMap);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                params.addParameter(entry.getKey(), entry.getValue());
            }
        }
        Cancelable cancelable = x.http().get(params, callback);
        L.e("HttpUtils", "params = " + params.getUri());
        return cancelable;
    }

    /**
     * 发送post请求
     *
     * @param <T>
     */
    public <T> Cancelable Post(String url, Map<String, Object> map, CommonCallback<T> callback) {
        RequestParams params = new RequestParams(url);
        if (null != map) {
            map.putAll(tokenMap);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                params.addParameter(entry.getKey(), entry.getValue());
            }
        }
        Cancelable cancelable = x.http().post(params, callback);
        L.e("HttpUtils", "params = " + params.toString());
        return cancelable;
    }

    /**
     * 发送post请求
     *
     * @param url
     * @param callback
     * @param <T>
     * @return
     */
    public <T> Cancelable Post(String url, CommonCallback<T> callback) {
        RequestParams params = new RequestParams(url);
        for (Map.Entry<String, Object> entry : tokenMap.entrySet()) {
            params.addParameter(entry.getKey(), entry.getValue());
        }
        Cancelable cancelable = x.http().post(params, callback);
        L.e("HttpUtils", "params = " + params.toString());
        return cancelable;
    }


    /**
     * 上传文件
     *
     * @param <T>
     */
    public <T> Cancelable UpLoadFile(String url, Map<String, Object> map, CommonCallback<T> callback) {
        RequestParams params = new RequestParams(url);
        if (null != map) {
            map.putAll(tokenMap);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                params.addParameter(entry.getKey(), entry.getValue());
            }
        }
        params.setMultipart(true);
        Cancelable cancelable = x.http().get(params, callback);
        return cancelable;
    }

    /**
     * 下载文件
     *
     * @param <T>
     */
    public static <T> Cancelable DownLoadFile(String url, String filepath, CommonCallback<T> callback) {
        RequestParams params = new RequestParams(url);
        //设置断点续传
        params.setAutoResume(true);
        params.setSaveFilePath(filepath);
        Cancelable cancelable = x.http().get(params, callback);
        return cancelable;
    }


    public class JsonResponseParser implements ResponseParser {
        //检查服务器返回的响应头信息
        @Override
        public void checkResponse(UriRequest request) throws Throwable {
        }

        /**
         * 转换result为resultType类型的对象
         *
         * @param resultType  返回值类型(可能带有泛型信息)
         * @param resultClass 返回值类型
         * @param result      字符串数据
         * @return
         * @throws Throwable
         */
        @Override
        public Object parse(Type resultType, Class<?> resultClass, String result) throws Throwable {
            return new Gson().fromJson(result, resultClass);
        }
    }


    public class MyCallBack<ResultType> implements Callback.CommonCallback<ResultType> {

        @Override
        public void onSuccess(ResultType result) {
            //可以根据公司的需求进行统一的请求成功的逻辑处理
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            //可以根据公司的需求进行统一的请求网络失败的逻辑处理
        }

        @Override
        public void onCancelled(CancelledException cex) {

        }

        @Override
        public void onFinished() {

        }
    }

    public interface DataCallBack {
        void onFail(String result);

        void onSuccess(String result);
    }


    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
