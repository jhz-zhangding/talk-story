package com.efrobot.talkstory;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import com.danikula.videocache.HttpProxyCacheServer;
import com.efrobot.library.mvp.utils.PreferencesUtils;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.CurrentPlayBean;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.db.DbHelper;
import com.efrobot.talkstory.db.HistoryManager;
import com.efrobot.talkstory.service.MediaPlayService;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.xutils.x;

import java.util.List;

/**
 * Created by zd on 2017/12/18.
 */
public class TalkStoryApplication extends Application {

    private final String TAG = TalkStoryApplication.class.getSimpleName();

    //是否开启了音频服务
    public static boolean isStartMediaService = false;

    private DbHelper mDbHelper;

    //音频服务
    public MediaPlayService mediaPlayService;

    //http代理
    private HttpProxyCacheServer proxy;

    //当前播放实体类
    public CurrentPlayBean currentPlayBean;

    //是否正在播放
    public boolean isPlayingStory = false;

    //存储播放历史条数最大阀值
    private final int MAX_HISTORY_NUM = 100;

    public boolean isStopService = false;

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false); // 是否输出debug日志，开启debug会影响性能

        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);
        //Initialize ImageLoader with configuration.
//        ImageLoader.getInstance().init(configuration);
        initImageLoader(getApplicationContext());
    }

    public static TalkStoryApplication from(Context context) {
        return (TalkStoryApplication) context.getApplicationContext();
    }

    public synchronized DbHelper getDataBase() {
        if (mDbHelper == null)
            mDbHelper = new DbHelper(getApplicationContext());
        return mDbHelper;
    }


    public static HttpProxyCacheServer getProxy(Context context) {
        TalkStoryApplication app = (TalkStoryApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .build();
    }

//    private HttpProxyCacheServer newProxy() {
//        return new HttpProxyCacheServer(this);
//    }

    public void startMediaService(Context context, String url, int id) {
        Intent intent = new Intent(context, MediaPlayService.class);
        intent.putExtra("media_url", url);
        intent.putExtra("media_id", id);
        context.startService(intent);
    }

    public void stopMediaService() {
        if (mediaPlayService != null) {
            mediaPlayService.stopSelf();
            mediaPlayService = null;
            isStartMediaService = false;
            isPlayingStory = false;
        }
    }

    public void setMediaPlayService(MediaPlayService mediaPlayService) {
        this.mediaPlayService = mediaPlayService;
        isStartMediaService = true;
    }

    public CurrentPlayBean getCurrentPlayBean() {
        return currentPlayBean;
    }

    public void setCurrentPlayBean(CurrentPlayBean currentPlayBean) {
        PreferencesUtils.putInt(this, "lastId", currentPlayBean.getAudiaItemBean().getId());
        this.currentPlayBean = currentPlayBean;
    }

    public void setCurrentPlayBean(AudiaItemBean audiaItemBean, int type) {
        CurrentPlayBean currentPlayBean = new CurrentPlayBean();

        List<VersionBean> versionBeanList = audiaItemBean.getVersions();
        if (type == -1 && versionBeanList != null && versionBeanList.size() > 0) {
            currentPlayBean.setAudiaItemBean(audiaItemBean);
            currentPlayBean.setVersionBean(versionBeanList.get(0));
        } else {
            for (int j = 0; j < versionBeanList.size(); j++) {
                if (type == versionBeanList.get(j).getType()) {
                    currentPlayBean.setAudiaItemBean(audiaItemBean);
                    currentPlayBean.setVersionBean(versionBeanList.get(j));
                    break;
                }
            }
        }
        PreferencesUtils.putInt(this, "lastId", currentPlayBean.getAudiaItemBean().getId());
        this.currentPlayBean = currentPlayBean;
    }

    public void setCurrentPlayBean(AudiaItemBean audiaItemBean, VersionBean versionBean) {
        CurrentPlayBean mCurrentPlayBean = new CurrentPlayBean();
        mCurrentPlayBean.setAudiaItemBean(audiaItemBean);
        mCurrentPlayBean.setVersionBean(versionBean);
        this.currentPlayBean = mCurrentPlayBean;
    }

//    public VersionBean getVersionBean(int type) {
//        VersionBean versionBean = null;
//        if (getCurrentPlayBean() != null) {
//            List<VersionBean> versionBeanList = getCurrentPlayBean().getVersions();
//            for (int i = 0; i < versionBeanList.size(); i++) {
//                int verType = versionBeanList.get(i).getType();
//                if (type == verType) {
//                    versionBean = versionBeanList.get(i);
//                    break;
//                }
//            }
//        }
//        return versionBean;
//    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(480, 800)          // default = device screen dimensions
                .threadPriority(Thread.NORM_PRIORITY - 1)   // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13)              // default
                .imageDownloader(new BaseImageDownloader(context)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);

    }


}
