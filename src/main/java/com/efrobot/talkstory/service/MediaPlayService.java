package com.efrobot.talkstory.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.env.PlayListCache;
import com.efrobot.talkstory.utils.PreferencesUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by zd on 2017/12/19.
 */
public class MediaPlayService extends Service implements CacheListener {

    private TalkStoryApplication application;

    public MediaPlayer mediaPlayer;

    private String VIDEO_URL = "";
    private int VIDEO_ID = 0;

    private String TAG = MediaPlayService.class.getSimpleName();

    private int currentPosition = 0;
    private HttpProxyCacheServer proxy;

    private int currentPlayMode = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        proxy = TalkStoryApplication.getProxy(this);


        currentPlayMode = PreferencesUtils.getInt(this, "playMode", Constants.ORDER_PLAY_MODE);

        application = TalkStoryApplication.from(this);
        application.setMediaPlayService(this);
        VIDEO_URL = intent.getStringExtra("media_url");
        VIDEO_ID = intent.getIntExtra("media_id", -1);
        Log.e("MediaPlayService", "onStartCommand:" + "media_url = " + VIDEO_URL);
        getMediaPlayer();
        if (!TextUtils.isEmpty(VIDEO_URL)) {
            checkCachedState();
            startVideo();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        return mediaPlayer;
    }

    private void startVideo() {
        if (!TextUtils.isEmpty(VIDEO_URL)) {
            proxy.registerCacheListener(this, VIDEO_URL);
            String proxyUrl = proxy.getProxyUrl(VIDEO_URL);
            Log.d(TAG, "Use proxy url " + proxyUrl + " instead of original url " + VIDEO_URL);
            try {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                    //为了防止第二次播放的bug，重新生成MediaPlayer播放器
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(onPreparedListener);
                    mediaPlayer.setOnCompletionListener(onCompletionListener);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(proxyUrl);
                    mediaPlayer.prepareAsync();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "VIDEO_URL为空 " + VIDEO_URL);
        }
    }

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            L.e(TAG, "播放結束，開始播放下一條");
            startPlayNext();
        }
    };

    private List<VersionBean> versionBeen;
    private AudiaItemBean audiaItemBean;
    private VersionBean versionBean;

    private void startPlayNext() {
        boolean isContains = false;
        List<AudiaItemBean> list = PlayListCache.getInstance(this).getList();
        if (list != null) {
            int nextAudioIndex = 0;
            for (int i = 0; i < list.size(); i++) {
                int id = list.get(i).getId();
                if (id == VIDEO_ID) {
                    audiaItemBean = list.get(i);
                    versionBeen = audiaItemBean.getVersions();
                    nextAudioIndex = i;
                    isContains = true;
                    break;
                }
            }

            if (!isContains) {
                if (list.size() > 0) {
                    versionBeen = list.get(0).getVersions();
                }
            }

            if (versionBeen != null && versionBeen.size() > 0) {
                int nextVersionIndex = 0;
                for (int i = 0; i < versionBeen.size(); i++) {
                    String url = versionBeen.get(i).getAudioUrl();
                    if (url.equals(VIDEO_URL)) {
                        nextVersionIndex = i + 1;
                        break;
                    }
                }
                if (nextVersionIndex < versionBeen.size()) {
                    L.e(TAG, "list.size() = " + list.size() + "播放下一个版本----nextVersionIndex = " + nextVersionIndex);
                    //对于多版本故事，默认先播放第一版本，再播放第二版本
                    versionBean = versionBeen.get(nextVersionIndex);
                    VIDEO_URL = versionBeen.get(nextVersionIndex).getAudioUrl();
                    VIDEO_ID = audiaItemBean.getId();
                } else {
                    //版本都播完，开始播放下一个故事
                    switch (currentPlayMode) {
                        case Constants.ORDER_PLAY_MODE:
                            nextAudioIndex = nextAudioIndex + 1;
                            if (nextAudioIndex >= list.size()) {
                                nextAudioIndex = 0;
                            }
                            break;
                        case Constants.RANDOM_PLAY_MODE:
                            nextAudioIndex = new Random().nextInt(list.size());
                            break;
                        case Constants.CIRCEL_PLAY_MODE:

                            break;
                    }

                    L.e(TAG, "list.size() = " + list.size() + "----nextAudioIndex = " + nextAudioIndex);
                    L.e(TAG, "name = " + list.get(nextAudioIndex).getName());
                    if (nextAudioIndex < list.size()) {
                        audiaItemBean = list.get(nextAudioIndex);
                        versionBeen = audiaItemBean.getVersions();
                        if (versionBeen != null && versionBeen.size() > 0) {
                            versionBean = list.get(nextAudioIndex).getVersions().get(0);
                            VIDEO_URL = versionBean.getAudioUrl();
                            VIDEO_ID = list.get(nextAudioIndex).getId();
                        }
                    }
                }
                if (audiaItemBean != null && versionBean != null) {
                    L.e(TAG, "发送广播:更新播放下一条");
                    application.setCurrentPlayBean(audiaItemBean, versionBean);
                    Intent mIntent = new Intent(Constants.ACTION_NAME);
                    //发送广播
                    sendBroadcast(mIntent);
                }
                startVideo();
            }
        } else {
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
        }
    }

    public void startOtherVideo(String url) {
        this.VIDEO_URL = url;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        startVideo();

    }

    public void pause() {
        mediaPlayer.pause();
        application.isPlayingStory = false;
    }

    public void continueOrStart() {
        application.isPlayingStory = true;
        currentPosition = mediaPlayer.getCurrentPosition();
        if (currentPosition > 0) {
            mediaPlayer.start();
            mediaPlayer.seekTo(currentPosition);
        } else {
            mediaPlayer.start();
        }
    }

    //注意seekBar传入的是毫秒级，不是秒
    public void seekTo(int seekBarPotion) {
        application.isPlayingStory = true;
        currentPosition = seekBarPotion;
        if (currentPosition >= 0) {
            mediaPlayer.start();
            mediaPlayer.seekTo(currentPosition * 1000);
        } else {
            mediaPlayer.start();
        }
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
//        progressBar.setSecondaryProgress(percentsAvailable);
        Log.d(TAG, String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
    }

    private void checkCachedState() {
        HttpProxyCacheServer proxy = TalkStoryApplication.getProxy(this);
        boolean fullyCached = proxy.isCached(VIDEO_URL);
        if (fullyCached) {
//            progressBar.setSecondaryProgress(100);
        }
    }

    private final class VideoProgressUpdater extends Handler {

        public void start() {
            sendEmptyMessage(0);
        }

        public void stop() {
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message msg) {
            updateVideoProgress();
            sendEmptyMessageDelayed(0, 500);
        }
    }

    private void updateVideoProgress() {
        int videoProgress = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
//        progressBar.setProgress(videoProgress);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        application.isPlayingStory = false;
        Log.e("MediaPlayService", "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
