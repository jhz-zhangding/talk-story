package com.efrobot.talkstory.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.PreferencesUtils;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.PopupWindowAdapter;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.AudioDetail;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.db.HistoryManager;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.env.PlayListCache;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.utils.PopupOrderPriceDetail;
import com.efrobot.talkstory.utils.TimeUtils;
import com.efrobot.talkstory.utils.VerticalSeekBar;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.xutils.common.Callback;

import java.util.List;
import java.util.Map;

public abstract class WithPlayerBaseActivity extends Activity {

    private final String TAG = WithPlayerBaseActivity.class.getSimpleName();

    public TalkStoryApplication application;

    private DrawerLayout drawerLayout;

    private PopupOrderPriceDetail volumePopupWindow;
    private PopupWindowAdapter popupWindowAdapter;
    public List<AudiaItemBean> audiaItemBeanList;

    private ImageView playImage;
    private ImageView playLastImg, playStartOrPauseImg, playNextImg;
    private TextView playNameTv;
    private ProgressBar progressBar;
    private TextView currentPlayTimeTv, totalPlayTimeTv;
    private ImageView playModeImg, playHistoryImg, playVolumImg;

    public AudioManager audiomanage;
    private int maxVolume, currentVolume;

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract int getZdContentView();

    protected abstract void updateAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        application = TalkStoryApplication.from(this);

        audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        FrameLayout view = (FrameLayout) findViewById(R.id.frame_layout);
        View childView = LayoutInflater.from(this).inflate(getZdContentView(), view, false);
        view.addView(childView);

        initPlayView();

        initView();
        initListener();
        updatePlayModeImage();
        registerBoradcastReceiver();
    }

    private void initPlayView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.v4_drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        playImage = (ImageView) findViewById(R.id.play_image);
        playLastImg = (ImageView) findViewById(R.id.play_last_btn);
        playStartOrPauseImg = (ImageView) findViewById(R.id.base_play_pause_and_play);
        playNextImg = (ImageView) findViewById(R.id.play_next_btn);
        playNameTv = (TextView) findViewById(R.id.player_music_name);
        progressBar = (ProgressBar) findViewById(R.id.myProgressBar);
        currentPlayTimeTv = (TextView) findViewById(R.id.play_music_current_time);
        totalPlayTimeTv = (TextView) findViewById(R.id.play_music_total_time);
        playModeImg = (ImageView) findViewById(R.id.play_mode);
        playHistoryImg = (ImageView) findViewById(R.id.play_history);
        playVolumImg = (ImageView) findViewById(R.id.play_volume);

        playLastImg.setOnClickListener(onClick);
        playStartOrPauseImg.setOnClickListener(onClick);
        playNextImg.setOnClickListener(onClick);
        playModeImg.setOnClickListener(onClick);
        playHistoryImg.setOnClickListener(onClick);
        playVolumImg.setOnClickListener(onClick);
        findViewById(R.id.play_framelayout).setOnClickListener(onClick);
    }

    private void startPlay(String url, int id) {
        if (application != null) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                if (!TextUtils.isEmpty(url)) {
                    application.mediaPlayService.startOtherVideo(url);
                }
            } else {
                application.startMediaService(this, url, id);
            }
            application.isPlayingStory = true;
        }
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.play_last_btn:
                    //上一条
                    if (PlayListCache.list != null) {
                        int mId = application.getCurrentPlayBean().getAudiaItemBean().getId();
                        AudiaItemBean audiaItemBean = PlayListCache.getInstance(getContext()).getLastAudio(mId);
                        if (audiaItemBean != null && audiaItemBean.getVersions() != null) {
                            if (audiaItemBean.getVersions().size() > 0) {
                                /** 默认播放第一条 **/
                                VersionBean versionBean = audiaItemBean.getVersions().get(0);
                                startPlay(versionBean.getAudioUrl(), audiaItemBean.getId());
                                application.setCurrentPlayBean(audiaItemBean, versionBean);
                            }
                        }
                    }

                    updatePlayerView();
                    updateAdapter();
                    break;
                case R.id.play_next_btn:
                    //下一条
                    if (PlayListCache.list != null) {
                        int mId = application.getCurrentPlayBean().getAudiaItemBean().getId();
                        AudiaItemBean audiaItemBean = PlayListCache.getInstance(getContext()).getNextAudio(mId);
                        if (audiaItemBean != null && audiaItemBean.getVersions() != null) {
                            if (audiaItemBean.getVersions().size() > 0) {
                                /** 默认播放第一条 **/
                                VersionBean versionBean = audiaItemBean.getVersions().get(0);
                                startPlay(versionBean.getAudioUrl(), audiaItemBean.getId());
                                application.setCurrentPlayBean(audiaItemBean, versionBean);
                            }
                        }
                    }
                    updatePlayerView();
                    updateAdapter();
                    break;
                case R.id.base_play_pause_and_play:
                    //开始暂停
                    if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                        //播放器没有关闭
                        if (application.mediaPlayService.mediaPlayer.isPlaying()) {
                            application.mediaPlayService.pause();
                        } else {
                            application.mediaPlayService.continueOrStart();
                        }
                    } else {
                        if (application.getCurrentPlayBean() != null) {
                            String mMediaUrl = application.getCurrentPlayBean().getVersionBean().getAudioUrl();
                            int mMediaId = application.getCurrentPlayBean().getAudiaItemBean().getId();
                            application.isPlayingStory = true;
                            application.startMediaService(getContext(), mMediaUrl, mMediaId);
                        }
                    }
                    updatePlayerView();
                    break;
                case R.id.play_mode:
                    //播放模式
                    int playMode = PreferencesUtils.getInt(getContext(), "playMode", Constants.ORDER_PLAY_MODE);
                    if (playMode == Constants.ORDER_PLAY_MODE) {
                        PreferencesUtils.putInt(getContext(), "playMode", Constants.RANDOM_PLAY_MODE);
                        RobotToastUtil.getInstance(getContext()).showToast("已切换随机播放");
                    } else if (playMode == Constants.RANDOM_PLAY_MODE) {
                        PreferencesUtils.putInt(getContext(), "playMode", Constants.CIRCEL_PLAY_MODE);
                        RobotToastUtil.getInstance(getContext()).showToast("已切换循环播放");
                    } else if (playMode == Constants.CIRCEL_PLAY_MODE) {
                        PreferencesUtils.putInt(getContext(), "playMode", Constants.ORDER_PLAY_MODE);
                        RobotToastUtil.getInstance(getContext()).showToast("已切换顺序播放");
                    }
                    updatePlayModeImage();
                    break;
                case R.id.play_history:
                    //历史
                    showDrawerLayout();
                    break;
                case R.id.play_volume:
                    //音量
                    showVolumePopupWindow();
                    break;
                case R.id.play_framelayout:
                    if (application != null && application.getCurrentPlayBean() != null) {
                        getData(application.getCurrentPlayBean().getAudiaItemBean().getId(), application.getCurrentPlayBean().getVersionBean().getType());
                    }
                    break;
            }
        }
    };

    private void getData(int id, final int type) {
        HttpUtils httpUtils = new HttpUtils(false);
        Map<String, Object> mapInfo = HttpParamUtils.getAudioByIdParamMap();
        String finalUrl = String.format(HttpParamUtils.AUDIO_BY_ID_URL, id);
        httpUtils.Post(finalUrl, mapInfo, new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        L.e("WithPlayerBaseActivity", "onSuccess:" + result);
                        AudioDetail albumBean = new Gson().fromJson(result, AudioDetail.class);
                        if (albumBean != null && albumBean.getData() != null) {
                            PlayMediaActivity.openActivity(getContext(), PlayMediaActivity.class, albumBean.getData(), type, Constants.MAIN_REQUEST_REQUEST);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {

                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                }

        );

    }


    private int[] playMode = new int[]{Constants.ORDER_PLAY_MODE, Constants.RANDOM_PLAY_MODE, Constants.CIRCEL_PLAY_MODE};

    public void updatePlayModeImage() {
        int playMode = PreferencesUtils.getInt(getContext(), "playMode", Constants.ORDER_PLAY_MODE);
        if (playMode == Constants.ORDER_PLAY_MODE) {
            playModeImg.setBackgroundResource(R.mipmap.player_order);
        } else if (playMode == Constants.RANDOM_PLAY_MODE) {
            playModeImg.setBackgroundResource(R.mipmap.player_random);
        } else if (playMode == Constants.CIRCEL_PLAY_MODE) {
            playModeImg.setBackgroundResource(R.mipmap.player_circle);
        }
    }

    /**
     * 隐藏状态栏
     */
    private void applyCompat() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    View contentView;
    int measuredWidth;
    int measuredHeight;
    int id = -1;


    private void initDrawerListData() {
        if (application.getCurrentPlayBean() != null) {
            id = application.getCurrentPlayBean().getAudiaItemBean().getId();
        }

        audiaItemBeanList = PlayListCache.getInstance(this).getList();

        ListView listView = (ListView) findViewById(R.id.base_pop_list_view);
        popupWindowAdapter = new PopupWindowAdapter(this, audiaItemBeanList, id);
        listView.setAdapter(popupWindowAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long iid) {
                showDrawerLayout();
                AudiaItemBean audiaItemBean = audiaItemBeanList.get(position);
                PlayMediaActivity.openActivity(getContext(), PlayMediaActivity.class, audiaItemBean, -1, Constants.MAIN_REQUEST_REQUEST);
            }
        });
    }

    private void showDrawerLayout() {

        if (!drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            initDrawerListData();
            drawerLayout.openDrawer(Gravity.RIGHT);
        } else {
            drawerLayout.closeDrawer(Gravity.RIGHT);
        }
    }

    private VerticalSeekBar verticalSeekBar;

    View volumeLayout;

    private void showVolumePopupWindow() {
        volumeLayout = LayoutInflater.from(this).inflate(R.layout.vertical_volum_layout, null);
        verticalSeekBar = (VerticalSeekBar) volumeLayout.findViewById(R.id.volume_seekbar);

        maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC);  //获取系统最大音量
        verticalSeekBar.setMax(maxVolume);   //拖动条最高值与系统最大声匹配
        currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
        verticalSeekBar.setProgress(currentVolume);

        verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() //调音监听器
        {
            public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC);  //获取当前值
                verticalSeekBar.setProgress(currentVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub


            }
        });

        volumeLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        measuredWidth = verticalSeekBar.getMeasuredWidth();
        measuredHeight = verticalSeekBar.getMeasuredHeight();

        volumePopupWindow = new PopupOrderPriceDetail(this, volumeLayout);
        volumePopupWindow.showUp(playVolumImg);
    }


    private final int UPDATE_SEEKBAR_VIEW = 1;
    private final int HIDE_NAVIGATION_VIEW = 2;

    private boolean isPlay = true;

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_SEEKBAR_VIEW:
                    if (isPlay) {
                        updateSeekBak();
                        sendEmptyMessageDelayed(UPDATE_SEEKBAR_VIEW, 200);
                    }
                    break;
                case HIDE_NAVIGATION_VIEW:
                    hideBottomUIMenu();
                    break;
            }
        }
    };

    private void updateSeekBak() {
        if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
            if (application.mediaPlayService.mediaPlayer.isPlaying()) {
                int currentPosition = application.mediaPlayService.mediaPlayer.getCurrentPosition();
                progressBar.setProgress(currentPosition / 1000);
                currentPlayTimeTv.setText(TimeUtils.ShowTime(currentPosition));
            }
        }
    }

    /***
     * 更新底部播放器数据
     */
    public void updatePlayerView() {
        updatePlayModeImage();
        updateRotateImageAnim();

        if (application.getCurrentPlayBean() != null) {
            playNameTv.setText(application.getCurrentPlayBean().getAudiaItemBean().getName());
            totalPlayTimeTv.setText(" / " + TimeUtils.ShowMusicTime(application.getCurrentPlayBean().getVersionBean().getPlayTime()));
            progressBar.setMax(application.currentPlayBean.getVersionBean().getPlayTime());
            if (!TextUtils.isEmpty(application.getCurrentPlayBean().getAudiaItemBean().getSmallImg()))
                ImageLoader.getInstance().displayImage(application.getCurrentPlayBean().getAudiaItemBean().getSmallImg(), playImage);
            if (application.isPlayingStory) {
                playStartOrPauseImg.setBackgroundResource(R.mipmap.player_pause);
            } else {
                playStartOrPauseImg.setBackgroundResource(R.mipmap.player_start);
            }
        }

        startUpdateUI();
    }

    private void startUpdateUI() {
        if (mHandle.hasMessages(UPDATE_SEEKBAR_VIEW))
            mHandle.removeMessages(UPDATE_SEEKBAR_VIEW);
        mHandle.sendEmptyMessage(UPDATE_SEEKBAR_VIEW);
    }

    private void updateRotateImageAnim() {

        if (application.isPlayingStory) {
            startRotateImageAnim();
        } else {
            stopRotateImageAnim();

        }

    }

    Animation operatingAnim;

    private void startRotateImageAnim() {
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_image_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            playImage.startAnimation(operatingAnim);
        }
    }

    private void stopRotateImageAnim() {
//        if (operatingAnim != null) {
            playImage.clearAnimation();
//        }
    }


    public void showToast(String showToast) {
        Toast.makeText(this, showToast, Toast.LENGTH_SHORT).show();
    }

    public Context getContext() {
        return this;
    }

    public void setFullScreen() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    /***
     * 解决软键盘弹出时任务栏不隐藏和单击输入框以外区域输入法不隐藏的bug
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                setFullScreen();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            top = 300;
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    private void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.UPDATE_PROGRESS_RESULT) {
            updatePlayerView();
        }
    }

    public HistoryBean gerateHistoryData(AudiaItemBean audiaItemBean, VersionBean versionBean) {
        HistoryBean historyBean = new HistoryBean();
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

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.ACTION_NAME);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            L.e(TAG, "接收到广播：" + action);
            if (action.equals(Constants.ACTION_NAME)) {
                updatePlayerView();
            }
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}
