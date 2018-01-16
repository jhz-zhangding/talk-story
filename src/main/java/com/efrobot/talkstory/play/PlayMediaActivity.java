package com.efrobot.talkstory.play;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.PopupWindowAdapter;
import com.efrobot.talkstory.base.BaseActivity;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.db.HistoryManager;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.env.PlayListCache;
import com.efrobot.talkstory.utils.PopupOrderPriceDetail;
import com.efrobot.talkstory.utils.TimeUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.List;

public class PlayMediaActivity extends BaseActivity implements View.OnClickListener {

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

        application.setCurrentPlayBean(gerateHistoryData());
//        addHistoryToDatabase();
    }

    protected void initView() {

        application = TalkStoryApplication.from(this);
        audiaItemBean = (AudiaItemBean) getIntent().getSerializableExtra("data");

        currentPlayType = getIntent().getIntExtra("type", -1);

        inflater = LayoutInflater.from(this);
        imageLoader = ImageLoader.getInstance();

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

        initData();
    }

    private MyTagAdapter myTagAdapter;

    private void initData() {
        if (audiaItemBean != null) {
            albumNameTv.setText(audiaItemBean.getAlbumName());
            nameTv.setText(audiaItemBean.getName());

            if (!TextUtils.isEmpty(audiaItemBean.getRotateImg()))
                imageLoader.displayImage(audiaItemBean.getRotateImg(), rotateImage);

            if (audiaItemBean.getVersions() == null || audiaItemBean.getVersions().size() <= 0) {
                return;
            }

            //获取播放实体
            getVersionBean();

            myTagAdapter = new MyTagAdapter(audiaItemBean.getVersions());
            tagFlowLayout.setAdapter(myTagAdapter);
            tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
                @Override
                public boolean onTagClick(View view, int position, FlowLayout parent) {

                    currentPlayType = audiaItemBean.getVersions().get(position).getType();

                    if (myTagAdapter != null) {
                        myTagAdapter.notifyDataChanged();
                    }

                    String url = audiaItemBean.getVersions().get(position).getAudioUrl();
                    if (!versionBean.getAudioUrl().equals(url)) {
                        versionBean = audiaItemBean.getVersions().get(position);
                        startPlay();
                        application.setCurrentPlayBean(gerateHistoryData());
                    }

                    return false;
                }
            });

            if (versionBean != null) {
                int currentTotalTime = versionBean.getPlayTime();
                totalTimeTv.setText(TimeUtils.ShowMusicTime(currentTotalTime));
                mSeekBar.setMax(currentTotalTime);
            }

            //判断页面的播放内容是否和后台播放的一致
            if (application.getCurrentPlayBean() != null) {
                int id = application.getCurrentPlayBean().getId();
                if (id == audiaItemBean.getId()) {
                    String serviceMediaUrl = application.getCurrentPlayBean().getAudioUrl();
                    if (serviceMediaUrl.equals(versionBean.getAudioUrl())) {
                        if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                            if (!application.mediaPlayService.getMediaPlayer().isPlaying()) {
                                application.mediaPlayService.continueOrStart();
                            }
                        } else {
                            startPlay();
                        }
                    } else {
                        startPlay();
                    }
                } else {
                    startPlay();
                }
            } else {
                startPlay();
            }

            startUpdateSeekBar();
        }

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
                textView.setBackgroundResource(R.drawable.bg_language_selected);
            } else {
                textView.setBackgroundResource(R.drawable.bg_language_unselected);
            }
            return textView;
        }

    }

    //获取第一条播放源

    private void getVersionBean() {
        if (audiaItemBean.getVersions() != null && audiaItemBean.getVersions().size() > 0) {
            if (currentPlayType != -1) {
                for (int i = 0; i < audiaItemBean.getVersions().size(); i++) {
                    if (currentPlayType == audiaItemBean.getVersions().get(i).getType()) {
                        versionBean = audiaItemBean.getVersions().get(i);
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

                break;
            case R.id.play_last_btn:
                //上一条
                if (PlayListCache.list != null) {
                    audiaItemBean = PlayListCache.getInstance(getContext()).getLastAudio(audiaItemBean.getId());
                    if (audiaItemBean != null && audiaItemBean.getVersions() != null) {
                        if (audiaItemBean.getVersions().size() > 0) {
                            /** 默认播放第一条 **/
                            versionBean = audiaItemBean.getVersions().get(0);
                            initData();
                            application.setCurrentPlayBean(gerateHistoryData());
                        }
                    }
                }
                updateSeekBak();
                break;
            case R.id.play_next_btn:
                //下一条
                if (PlayListCache.list != null) {
                    audiaItemBean = PlayListCache.getInstance(getContext()).getNextAudio(audiaItemBean.getId());
                    if (audiaItemBean != null && audiaItemBean.getVersions() != null) {
                        if (audiaItemBean.getVersions().size() > 0) {
                            /** 默认播放第一条 **/
                            versionBean = audiaItemBean.getVersions().get(0);
                            initData();
                            application.setCurrentPlayBean(gerateHistoryData());
                        }
                    }
                }
                updateSeekBak();
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
                showPopupWindowView(view);
                playerRecentList.requestFocus();
                break;
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


    private PopupOrderPriceDetail popupWindow;
    private PopupWindowAdapter popupWindowAdapter;
    private View contentView;
    private int id;

    private void showPopupWindowView(View view) {
        id = audiaItemBean.getId();
        List<HistoryBean> historyBeanList = HistoryManager.getInstance(getContext()).queryAllContent();
        contentView = LayoutInflater.from(this).inflate(R.layout.popup_history_lauout, null);
        popupWindow = new PopupOrderPriceDetail(this, contentView);
        ListView listView = (ListView) contentView.findViewById(R.id.pop_list_view);
        popupWindowAdapter = new PopupWindowAdapter(this, historyBeanList, id);
        listView.setAdapter(popupWindowAdapter);
        popupWindow.showUp(view);
    }


    private void startPlay() {
        if (application != null) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                if (versionBean != null) {
                    application.mediaPlayService.startOtherVideo(versionBean.getAudioUrl());
                }
            } else {
                application.startMediaService(this, versionBean.getAudioUrl());
            }
            application.isPlayingStory = true;
        }
    }

    private void clickPlayOrPause() {
        if (application != null) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                if (application.mediaPlayService.getMediaPlayer().isPlaying()) {
                    application.mediaPlayService.pause();
                } else {
                    application.mediaPlayService.continueOrStart();
                    startUpdateSeekBar();
                }
            } else {
                if (versionBean != null) {
                    application.isPlayingStory = true;
                    application.startMediaService(this, versionBean.getAudioUrl());
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
                        playerStartOrPauseBtn.setBackgroundResource(R.mipmap.player_pause);
                    } else {
                        //暂停
                        stopRotateImageAnim();
                        playerStartOrPauseBtn.setBackgroundResource(R.mipmap.player_start);
                    }

                    break;
            }
        }
    };

    private void updateSeekBak() {
        if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
            if (application.mediaPlayService.getMediaPlayer().isPlaying()) {
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

//    private void addHistoryToDatabase() {
//        try {
//            HistoryBean historyBean = gerateHistoryData();
//            if (HistoryManager.getInstance(this).queryIdExits(audiaItemBean.getId())) {
//                //如果存在就删掉 继续更新
//                HistoryManager.getInstance(this).deleteContentById(audiaItemBean.getId());
//            }
//            HistoryManager.getInstance(this).insertContent(historyBean);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private HistoryBean gerateHistoryData() {
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
