package com.efrobot.talkstory.play;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.PreferencesUtils;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.PopupWindowAdapter;
import com.efrobot.talkstory.base.BaseActivity;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.AudioDetail;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.env.PlayListCache;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.efrobot.talkstory.utils.TimeUtils;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.xutils.common.Callback;

import java.util.List;
import java.util.Map;

public class PlayMediaActivity extends BaseActivity implements View.OnClickListener {

    private DrawerLayout drawerLayout;

    private LayoutInflater inflater;
    private AudiaItemBean audiaItemBean;

    private DisplayImageOptions options3;
    private ImageLoader imageLoader;

    private TextView albumNameTv;
    private TextView nameTv;
    private ImageView rotateImage;
    private TagFlowLayout tagFlowLayout;

    //播放器按钮
    private ImageView playerModeBtn;
    private ImageView playerLastBtn;
    private ImageView playerStartOrPauseBtn;
    private ImageView playerNextBtn;
    private ImageView playerRecentList;
    private TalkStoryApplication application;

    private TextView currentTimeTv;
    private TextView totalTimeTv;
    private SeekBar mSeekBar;

    private VersionBean versionBean = null;
    private int currentPlayType = -1;

    private boolean isAlreadyAdd = false;

    public static void openActivity(Context context, Class cls, AudiaItemBean audiaItemBean, int requestCode) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("data", audiaItemBean);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    public static void openActivity(Context context, Class cls, AudiaItemBean audiaItemBean, int type, int requestCode) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("data", audiaItemBean);
        intent.putExtra("type", type);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        initView();
        initListener();

        application.setCurrentPlayBean(audiaItemBean, versionBean);
//        addHistoryToDatabase();
        updatePlayModeImage();
        registerBoradcastReceiver();
    }

    protected void initView() {

        application = TalkStoryApplication.from(this);
        audiaItemBean = (AudiaItemBean) getIntent().getSerializableExtra("data");

        currentPlayType = getIntent().getIntExtra("type", -1);

        inflater = LayoutInflater.from(this);
        imageLoader = ImageLoader.getInstance();

        drawerLayout = (DrawerLayout) findViewById(R.id.v4_drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        albumNameTv = (TextView) findViewById(R.id.album_name);
        nameTv = (TextView) findViewById(R.id.name);
        rotateImage = (ImageView) findViewById(R.id.rotate_image);
        tagFlowLayout = (TagFlowLayout) findViewById(R.id.language_flow_layout);

        playerModeBtn = (ImageView) findViewById(R.id.play_mode_btn);
        playerLastBtn = (ImageView) findViewById(R.id.play_last_btn);
        playerStartOrPauseBtn = (ImageView) findViewById(R.id.play_and_pause_btn);
        playerNextBtn = (ImageView) findViewById(R.id.play_next_btn);
        playerRecentList = (ImageView) findViewById(R.id.play_recent_list_btn);

        currentTimeTv = (TextView) findViewById(R.id.current_play_time);
        totalTimeTv = (TextView) findViewById(R.id.total_play_time);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());

        initData();
    }

    private void showDrawerLayout() {

        if (!drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            initDrawerListData();
            drawerLayout.openDrawer(Gravity.RIGHT);
        } else {
            drawerLayout.closeDrawer(Gravity.RIGHT);
        }
    }

    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                application.mediaPlayService.seekTo(seekBar.getProgress());
            }
        }
    }

    private MyTagAdapter myTagAdapter;

    private void initData() {
        if (audiaItemBean != null) {
            albumNameTv.setText(audiaItemBean.getAlbumName());
            nameTv.setText(audiaItemBean.getName());

            if (!TextUtils.isEmpty(audiaItemBean.getBackgroundImg())) {
//                ImageSize imageSize = new ImageSize(getScreenWidth(), getScreenHeight());
//                ImageLoader.getInstance().displayImage(audiaItemBean.getBackgroundImg(), parentLayout, imageSize);
            }


            if (!TextUtils.isEmpty(audiaItemBean.getSmallImg()))
                imageLoader.displayImage(audiaItemBean.getSmallImg(), rotateImage);

            if (audiaItemBean.getVersions() == null || audiaItemBean.getVersions().size() <= 0) {
                showToast("获取播放信息失败");
                return;
            }

            //判断页面的播放内容是否和后台播放的一致
            if (application.getCurrentPlayBean() != null) {
                int id = application.getCurrentPlayBean().getAudiaItemBean().getId();
                if (id == audiaItemBean.getId()) { //进入的是当前播放页
                    int playerType = application.getCurrentPlayBean().getVersionBean().getType();
                    if (currentPlayType == -1 || currentPlayType == playerType) { //同类型就不重新播了
                        if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                            if (!application.mediaPlayService.mediaPlayer.isPlaying()) {
                                application.mediaPlayService.continueOrStart();
                            }
                            currentPlayType = playerType;
                            getVersionBean();
                        } else {
                            startPlay();
                        }
                    } else { //不是当前类型
                        startPlay();
                    }
                } else { //不是当前ID，自己找类型
                    startPlay();
                }
            } else {
                startPlay();
            }

            if (versionBean != null) {
                int currentTotalTime = versionBean.getPlayTime();
                totalTimeTv.setText(TimeUtils.ShowMusicTime(currentTotalTime));
                mSeekBar.setMax(currentTotalTime);
            }

            myTagAdapter = new MyTagAdapter(audiaItemBean.getVersions());
            tagFlowLayout.setAdapter(myTagAdapter);
            tagFlowLayout.setOnTagClickListener(new MyTagClickOnListener());

            startUpdateSeekBar();
        }

    }

    private void playCompletionUpdate() {
        if (audiaItemBean != null) {
            albumNameTv.setText(audiaItemBean.getAlbumName());
            nameTv.setText(audiaItemBean.getName());

            if (!TextUtils.isEmpty(audiaItemBean.getSmallImg()))
                imageLoader.displayImage(audiaItemBean.getSmallImg(), rotateImage);

            if (versionBean != null) {
                int currentTotalTime = versionBean.getPlayTime();
                totalTimeTv.setText(TimeUtils.ShowMusicTime(currentTotalTime));
                mSeekBar.setMax(currentTotalTime);
            }

            myTagAdapter = new MyTagAdapter(audiaItemBean.getVersions());
            tagFlowLayout.setAdapter(myTagAdapter);
            tagFlowLayout.setOnTagClickListener(new MyTagClickOnListener());
        }
    }

    class MyTagClickOnListener implements TagFlowLayout.OnTagClickListener {

        @Override
        public boolean onTagClick(View view, int position, FlowLayout parent) {

            currentPlayType = audiaItemBean.getVersions().get(position).getType();

            if (myTagAdapter != null) {
                myTagAdapter.notifyDataChanged();
            }

            String url = audiaItemBean.getVersions().get(position).getAudioUrl();
            if (!versionBean.getAudioUrl().equals(url)) {
                startPlay();
                application.setCurrentPlayBean(audiaItemBean, versionBean);
                updateSeekBak();
            }

            return false;

        }
    }

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
                            audiaItemBean = albumBean.getData();
                            currentPlayType = type;
                            initData();
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

    class MyTagAdapter extends TagAdapter<VersionBean> {

        public MyTagAdapter(List<VersionBean> datas) {
            super(datas);
        }

        @Override
        public View getView(FlowLayout parent, int position, VersionBean o) {
            TextView textView = (TextView) inflater.inflate(R.layout.item_version, tagFlowLayout, false);
            String txt = getLanguageStrFromType(o.getType()) + "" + TimeUtils.ShowTime(o.getPlayTime() * 1000);
            textView.setText(txt);

            if (o.getType() == currentPlayType) {
                setBackgroundColor(o.getType(), textView, true);
            } else {
                setBackgroundColor(o.getType(), textView, false);
            }
            return textView;
        }
    }

    //双语
    private final int DOUBLE_LANGUAGE = 1;
    //纯英
    private final int ENGLISH_LANGUAGE = 2;
    //中文
    private final int CHINESE_LANGUAGE = 3;
    //原版
    private final int SOURCE_LANGUAGE = 4;

    private void setBackgroundColor(int type, TextView textView, boolean isSelected) {
        switch (type) {
            case DOUBLE_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.play_page_double_language_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.play_page_double_language_unselected);
                }
                break;
            case ENGLISH_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.play_page_english_language_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.play_page_english_language_unselected);
                }
                break;
            case CHINESE_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.play_page_chinese_language_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.play_page_chinese_language_unselected);
                }
                break;
            case SOURCE_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.play_page_source_language_selected);
                } else {
                    textView.setBackgroundResource(R.drawable.play_page_source_language_unselected);
                }
                break;
        }
    }

    //获取第一条播放源

    private void getVersionBean() {
        if (audiaItemBean.getVersions() != null && audiaItemBean.getVersions().size() > 0) {
            if (currentPlayType != -1) {
                for (int i = 0; i < audiaItemBean.getVersions().size(); i++) {
                    if (currentPlayType == audiaItemBean.getVersions().get(i).getType()) {
                        versionBean = audiaItemBean.getVersions().get(i);
                        currentPlayType = versionBean.getType();
                        break;
                    }
                }
            } else {
                versionBean = audiaItemBean.getVersions().get(0);
                currentPlayType = versionBean.getType();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    protected void initListener() {
        playerModeBtn.setOnClickListener(this);
        playerLastBtn.setOnClickListener(this);
        playerStartOrPauseBtn.setOnClickListener(this);
        playerNextBtn.setOnClickListener(this);
        playerRecentList.setOnClickListener(this);
        findViewById(R.id.play_back_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.play_back_btn:
                finishThis();
                break;
            case R.id.play_mode_btn:
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
            case R.id.play_last_btn:
                //上一条
                if (PlayListCache.list != null) {
                    audiaItemBean = PlayListCache.getInstance(getContext()).getLastAudio(audiaItemBean.getId());
                    if (audiaItemBean != null && audiaItemBean.getVersions() != null) {
                        if (audiaItemBean.getVersions().size() > 0) {
                            /** 默认播放第一条 **/
                            versionBean = audiaItemBean.getVersions().get(0);
                            currentPlayType = versionBean.getType();
                            initData();
                            application.setCurrentPlayBean(audiaItemBean, versionBean);
                        }
                    }
                }
                updateSeekBak();
                updateTagAdapter();
                break;
            case R.id.play_next_btn:
                //下一条
                if (PlayListCache.list != null) {
                    audiaItemBean = PlayListCache.getInstance(getContext()).getNextAudio(audiaItemBean.getId());
                    if (audiaItemBean != null && audiaItemBean.getVersions() != null) {
                        if (audiaItemBean.getVersions().size() > 0) {
                            /** 默认播放第一条 **/
                            versionBean = audiaItemBean.getVersions().get(0);
                            currentPlayType = versionBean.getType();
                            initData();
                            application.setCurrentPlayBean(audiaItemBean, versionBean);
                        }
                    }
                }
                updateSeekBak();
                updateTagAdapter();
                break;
            case R.id.play_and_pause_btn:

                if (!isAlreadyAdd) {
                    if (audiaItemBean != null && versionBean != null) {
                        isAlreadyAdd = true;
                    }
                }
                clickPlayOrPause();
                break;
            case R.id.play_recent_list_btn:
                showDrawerLayout();
                break;
        }

    }

    private void updateTagAdapter() {
        if (myTagAdapter != null) {
            myTagAdapter.notifyDataChanged();
        }
    }

    public void updatePlayModeImage() {
        int playMode = PreferencesUtils.getInt(getContext(), "playMode", Constants.ORDER_PLAY_MODE);
        if (playMode == Constants.ORDER_PLAY_MODE) {
            playerModeBtn.setBackgroundResource(R.mipmap.play_page_order_image);
        } else if (playMode == Constants.RANDOM_PLAY_MODE) {
            playerModeBtn.setBackgroundResource(R.mipmap.play_page_random_image);
        } else if (playMode == Constants.CIRCEL_PLAY_MODE) {
            playerModeBtn.setBackgroundResource(R.mipmap.play_page_circle_image);
        }
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


    private PopupWindowAdapter popupWindowAdapter;
    private int id;

    private void initDrawerListData() {
        id = audiaItemBean.getId();
        final List<AudiaItemBean> audiaItemBeen = PlayListCache.getInstance(this).getList();
        ListView listView = (ListView) findViewById(R.id.play_pop_list_view);
        popupWindowAdapter = new PopupWindowAdapter(this, audiaItemBeen, id);
        listView.setAdapter(popupWindowAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long iid) {
                audiaItemBean = audiaItemBeen.get(position);
                id = audiaItemBean.getId();
                currentPlayType = -1;
                initData();
                if (popupWindowAdapter != null) {
                    popupWindowAdapter.updateSelected(id);
                }
                application.setCurrentPlayBean(audiaItemBean, versionBean);
            }
        });
    }


    private void startPlay() {
        //获取播放实体
        getVersionBean();

        if (application != null) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                if (versionBean != null) {
                    application.mediaPlayService.startOtherVideo(versionBean.getAudioUrl());
                }
            } else {
                application.startMediaService(this, versionBean.getAudioUrl(), audiaItemBean.getId());
            }
            application.isPlayingStory = true;
        }
    }

    private void clickPlayOrPause() {
        if (application != null) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                if (application.mediaPlayService.mediaPlayer.isPlaying()) {
                    application.mediaPlayService.pause();
                } else {
                    application.mediaPlayService.continueOrStart();
                    startUpdateSeekBar();
                }
            } else {
                if (versionBean != null) {
                    application.isPlayingStory = true;
                    application.startMediaService(this, versionBean.getAudioUrl(), audiaItemBean.getId());
                    startUpdateSeekBar();
                }
            }
            mHandle.sendEmptyMessage(UPDATE_PLAY_OR_PAUSE_VIEW);
        }
    }

    private void startUpdateSeekBar() {
        if (mHandle.hasMessages(UPDATE_SEEKBAR_VIEW))
            mHandle.removeMessages(UPDATE_SEEKBAR_VIEW);
        mHandle.sendEmptyMessage(UPDATE_SEEKBAR_VIEW);

        mHandle.sendEmptyMessage(UPDATE_PLAY_OR_PAUSE_VIEW);
    }

    private boolean isPlay = true;
    private boolean isRotating = false;
    private final int UPDATE_SEEKBAR_VIEW = 1;
    private final int UPDATE_PLAY_OR_PAUSE_VIEW = 2;
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_SEEKBAR_VIEW:
//                    if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
//                        isPlay = application.mediaPlayService.getMediaPlayer().isPlaying();
//                    }
                    if (isPlay) {
                        updateSeekBak();
                        sendEmptyMessageDelayed(UPDATE_SEEKBAR_VIEW, 200);
                    }
                    break;
                case UPDATE_PLAY_OR_PAUSE_VIEW:
                    if (application.isPlayingStory) {
                        //播放
                        isRotating = false;
                        playerStartOrPauseBtn.setBackgroundResource(R.mipmap.play_page_pause_play);
                    } else {
                        //暂停
                        stopRotateImageAnim();
                        playerStartOrPauseBtn.setBackgroundResource(R.mipmap.play_page_start_play);
                    }

                    break;
            }
        }
    };

    private void updateSeekBak() {
        if (versionBean != null) {
            String totalTime = TimeUtils.ShowMusicTime(versionBean.getPlayTime());
            if (!totalTime.equals(totalTimeTv.getText().toString())) {
                totalTimeTv.setText(totalTime);
            }
        }

        if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
            if (application.mediaPlayService.mediaPlayer.isPlaying()) {
                if (!isRotating) {
                    startRotateImageAnim();
                }
                int currentPosition = application.mediaPlayService.mediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(currentPosition / 1000);
                currentTimeTv.setText(TimeUtils.ShowTime(currentPosition));
            }
        }
    }

    Animation operatingAnim;

    private void startRotateImageAnim() {
        isRotating = true;
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_image_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            rotateImage.startAnimation(operatingAnim);
        }
    }

    private void stopRotateImageAnim() {
        if (operatingAnim != null) {
            rotateImage.clearAnimation();
        }
    }


    private String getLanguageStrFromType(int type) {
        String str = "";
        switch (type) {
            case 1:
                str = "双语";
                break;
            case 2:
                str = "纯英";
                break;
            case 3:
                str = "中文";
                break;
            case 4:
                str = "原版";
                break;
        }
        return str;
    }

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.ACTION_NAME);
        //注册广播
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_NAME)) {
                if (application.getCurrentPlayBean() != null) {
                    audiaItemBean = application.getCurrentPlayBean().getAudiaItemBean();
                    versionBean = application.getCurrentPlayBean().getVersionBean();
                    currentPlayType = versionBean.getType();
                    playCompletionUpdate();
                    mHandle.sendEmptyMessage(UPDATE_PLAY_OR_PAUSE_VIEW);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        finishThis();
        super.onBackPressed();
    }

    private void finishThis() {
//        application.stopMediaService();
        isPlay = false;
        mHandle.removeMessages(UPDATE_SEEKBAR_VIEW);
        mHandle.removeCallbacksAndMessages(null);

        setResult(Constants.UPDATE_PROGRESS_RESULT, new Intent());
        finish();
    }
}
