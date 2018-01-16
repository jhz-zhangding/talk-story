package com.efrobot.talkstory.utils;

import android.graphics.Bitmap;

import com.efrobot.talkstory.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

/**
 * Created by zd on 2018/1/13.
 */
public class OptionsUtils {

    private static OptionsUtils instance;

    public static OptionsUtils getInstance() {
        if (instance == null) {
            instance = new OptionsUtils();
        }
        return instance;
    }

    public DisplayImageOptions getOption() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true) //加载本地图片不需要再做SD卡缓存，只做内存缓存即可
                .considerExifParams(true)
                //设置显示器为SimpleBitmapDisplayer
                .displayer(new SimpleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        return options;
    }

    public DisplayImageOptions getCircelOption() {
        //圆形图片
        DisplayImageOptions options3 = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.mipmap.default_image)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)   //设置图片的解码类型
                .displayer(new Displayer(0))
                .build();
        return options3;
    }

}
