package com.efrobot.talkstory.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.TalkStoryApplication;

import java.io.File;
import java.io.IOException;

/**
 * Created by zd on 2017/12/19.
 */
public class MediaPlayService extends Service implements CacheListener {

    private TalkStoryApplication application;

    public MediaPlayer mediaPlayer;

    private String VIDEO_URL = "";

    private String TAG = MediaPlayService.class.getSimpleName();

    private int currentPosition = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        application = TalkStoryApplication.from(this);
        application.setMediaPlayService(this);
        VIDEO_URL = intent.getStringExtra("media_url");
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
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        return mediaPlayer;
    }

    private void startVideo() {
        HttpProxyCacheServer proxy = TalkStoryApplication.getProxy(this);
        proxy.registerCacheListener(this, VIDEO_URL);
        String proxyUrl = proxy.getProxyUrl(VIDEO_URL);
        Log.d(TAG, "Use proxy url " + proxyUrl + " instead of original url " + VIDEO_URL);
        try {
            mediaPlayer.setDataSource(proxyUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    try {
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void playNext(String path) {
        if (!TextUtils.isEmpty(path)) {

        }
    }

    public void playLast(String path) {
        if (!TextUtils.isEmpty(path)) {

        }
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
//        progressBar.setSecondaryProgress(percentsAvailable);
        setCachedState(percentsAvailable == 100);
        Log.d(TAG, String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
    }

    private void checkCachedState() {
        HttpProxyCacheServer proxy = TalkStoryApplication.getProxy(this);
        boolean fullyCached = proxy.isCached(VIDEO_URL);
        setCachedState(fullyCached);
        if (fullyCached) {
//            progressBar.setSecondaryProgress(100);
        }
    }

    private void setCachedState(boolean cached) {
//        int statusIconId = cached ? R.drawable.ic_cloud_done : R.drawable.ic_cloud_download;
//        cacheStatusImageView.setImageResource(statusIconId);
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
