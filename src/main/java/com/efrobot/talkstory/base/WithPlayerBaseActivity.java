package com.efrobot.talkstory.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.PopupWindowAdapter;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.db.HistoryManager;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public abstract class WithPlayerBaseActivity extends Activity {

    public TalkStoryApplication application;

    private PopupWindow popupWindow;
    private PopupWindowAdapter popupWindowAdapter;
    public List<HistoryBean> historyBeanList;

    private ImageView playImage;
    private ImageView playLastImg, playStartOrPauseImg, playNextImg;
    private TextView playNameTv;
    private ProgressBar progressBar;
    private TextView currentPlayTimeTv, totalPlayTimeTv;
    private ImageView playModeImg, playHistoryImg, playVolumImg;

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract int getZdContentView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        application = TalkStoryApplication.from(this);

        historyBeanList = HistoryManager.getInstance(this).queryAllContent();

        FrameLayout view = (FrameLayout) findViewById(R.id.frame_layout);
        View childView = LayoutInflater.from(this).inflate(getZdContentView(), view, false);
        view.addView(childView);

        initPlayView();
        initPopupWindowView();

        initView();
        initListener();
    }

    private void initPlayView() {
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
    }

    View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.play_last_btn:
                    //上一条
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
                        if (historyBeanList != null && historyBeanList.size() > 0) {
                            String mMediaUrl = historyBeanList.get(historyBeanList.size() - 1).getAudioUrl();
                            application.isPlayingStory = true;
                            application.startMediaService(getContext(), mMediaUrl);
                        }
                    }
                    updatePlayerView();
                    break;
                case R.id.play_next_btn:
                    //吓一跳

                    break;
                case R.id.play_mode:
                    //播放模式

                    break;
                case R.id.play_history:
                    //历史
                    showOnTopPopupWindow(view);
                    break;
                case R.id.play_volume:
                    //音量

                    break;
            }
        }
    };

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

    private void initPopupWindowView() {
        contentView = LayoutInflater.from(this).inflate(R.layout.popup_history_lauout, null);

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        measuredWidth = contentView.getMeasuredWidth();
        measuredHeight = contentView.getMeasuredHeight();

        popupWindow = new PopupWindow(contentView, getResources().getDisplayMetrics().widthPixels
                , getResources().getDisplayMetrics().heightPixels);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        ListView listView = (ListView) contentView.findViewById(R.id.pop_list_view);
        popupWindowAdapter = new PopupWindowAdapter(this, historyBeanList, 1);
        listView.setAdapter(popupWindowAdapter);
    }

    private void showOnTopPopupWindow(View view) {
        historyBeanList = HistoryManager.getInstance(this).queryAllContent();
        if (popupWindowAdapter != null) {
            popupWindowAdapter.notifyDataSetChanged();
        }

        popupWindow.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupWindow.getContentView().getMeasuredWidth();    //  获取测量后的宽度
        int popupHeight = popupWindow.getContentView().getMeasuredHeight();  //获取测量后的高度
        int[] location = new int[2];

        view.getLocationOnScreen(location);
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] + view.getWidth() / 2, location[1] - measuredHeight);

        hideBottomUIMenu();
    }


    private final int UPDATE_SEEKBAR_VIEW = 1;

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
            }
        }
    };

    private void updateSeekBak() {
        if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
            if (application.mediaPlayService.getMediaPlayer().isPlaying()) {
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
        if (application.getCurrentPlayBean() != null) {
            playNameTv.setText(application.getCurrentPlayBean().getName());
            totalPlayTimeTv.setText(" / " + TimeUtils.ShowMusicTime(application.currentPlayBean.getPlayTime()));
            progressBar.setMax(application.currentPlayBean.getPlayTime());
            if (!TextUtils.isEmpty(application.getCurrentPlayBean().getSmallImg()))
                ImageLoader.getInstance().displayImage(application.getCurrentPlayBean().getSmallImg(), playImage);
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

    class UpdatePlayerBroadcastReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.UPDATE_PLAYER_RECEIVER_ACTION)) {
                updatePlayerView();
            }
        }
    }

}
