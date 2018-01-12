package com.efrobot.talkstory.play;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.base.BaseActivity;
import com.efrobot.talkstory.base.WithPlayerBaseActivity;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.db.HistoryManager;
import com.efrobot.talkstory.env.Constants;
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

    public static void openActivity(Context context, Class cls, AudiaItemBean audiaItemBean) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("data", audiaItemBean);
        context.startActivity(intent);
    }

    public static void openActivity(Context context, Class cls, AudiaItemBean audiaItemBean, int requestCode) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("data", audiaItemBean);
        ((Activity) context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initView();
        initListener();
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

    private void initData() {
        if (audiaItemBean != null) {
            albumNameTv.setText(audiaItemBean.getAlbumName());
            nameTv.setText(audiaItemBean.getName());

            if (!TextUtils.isEmpty(audiaItemBean.getRotateImg()))
                imageLoader.displayImage(audiaItemBean.getRotateImg(), rotateImage);


            tagFlowLayout.setAdapter(new TagAdapter<VersionBean>(audiaItemBean.getVersions()) {
                @Override
                public View getView(FlowLayout parent, int position, VersionBean versionBean) {
                    TextView textView = (TextView) inflater.inflate(R.layout.item_version, tagFlowLayout, false);
                    String txt = getLanguageStrFromType(versionBean.getType()) + "" + TimeUtils.ShowTime(versionBean.getPlayTime() * 1000);
                    textView.setText(txt);
                    return textView;
                }
            });

            getVersionBean();
            if (versionBean != null) {
                int currentTotalTime = versionBean.getPlayTime();
                totalTimeTv.setText(TimeUtils.ShowMusicTime(currentTotalTime));
                mSeekBar.setMax(currentTotalTime);
            }

        }

    }


    class MyTagAdapter<VersionBean> extends TagAdapter<VersionBean> {

        public MyTagAdapter(List<VersionBean> datas) {
            super(datas);
        }

        public void updateSelectView() {

        }

        @Override
        public View getView(FlowLayout parent, int position, VersionBean o) {
            TextView textView = (TextView) inflater.inflate(R.layout.item_version, tagFlowLayout, false);
            String txt = getLanguageStrFromType(versionBean.getType()) + "" + TimeUtils.ShowTime(versionBean.getPlayTime() * 1000);
            textView.setText(txt);
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

                break;
            case R.id.play_and_pause_btn:
                application.setCurrentPlayBean(gerateHistoryData());
                addHistoryToDatabase();

                if (!isAlreadyAdd) {
                    if (audiaItemBean != null && versionBean != null) {
                        isAlreadyAdd = true;
                    }
                }
                playOrPause();
                break;
            case R.id.play_next_btn:

                break;
            case R.id.play_recent_list_btn:

                break;
        }

    }

    private void playOrPause() {
        if (application != null) {
            if (application.mediaPlayService != null && application.mediaPlayService.mediaPlayer != null) {
                if (application.mediaPlayService.getMediaPlayer().isPlaying()) {
                    application.mediaPlayService.pause();
                    mHandle.sendEmptyMessage(UPDATE_PLAY_BUTTON_STATE);
                } else {
                    application.mediaPlayService.continueOrStart();
                    startUpdateUI();
                    mHandle.sendEmptyMessage(UPDATE_PAUSE_BUTTON_STATE);
                }
            } else {
                if (audiaItemBean.getVersions() != null && audiaItemBean.getVersions().size() > 0) {
                    application.isPlayingStory = true;
                    application.startMediaService(this, audiaItemBean.getVersions().get(0).getAudioUrl());
                    startUpdateUI();
                    mHandle.sendEmptyMessage(UPDATE_PAUSE_BUTTON_STATE);
                }
            }
        }
    }

    private void startUpdateUI() {
        if (mHandle.hasMessages(UPDATE_SEEKBAR_VIEW))
            mHandle.removeMessages(UPDATE_SEEKBAR_VIEW);
        mHandle.sendEmptyMessage(UPDATE_SEEKBAR_VIEW);
    }

    private boolean isPlay = true;
    private boolean isRotating = false;
    private final int UPDATE_SEEKBAR_VIEW = 1;
    private final int UPDATE_PLAY_BUTTON_STATE = 2;
    private final int UPDATE_PAUSE_BUTTON_STATE = 3;
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
                case UPDATE_PLAY_BUTTON_STATE:
                    //暂停
                    stopRotateImageAnim();
                    playerStartOrPauseBtn.setBackgroundResource(R.mipmap.player_start);
                    break;
                case UPDATE_PAUSE_BUTTON_STATE:
                    //播放
                    isRotating = false;
                    playerStartOrPauseBtn.setBackgroundResource(R.mipmap.player_pause);
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

    private void addHistoryToDatabase() {
        try {
            HistoryBean historyBean = gerateHistoryData();
            if (HistoryManager.getInstance(this).queryIdExits(audiaItemBean.getId())) {
                //如果存在就删掉 继续更新
                HistoryManager.getInstance(this).deleteContentById(audiaItemBean.getId());
            }
            HistoryManager.getInstance(this).insertContent(historyBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
        application.stopMediaService();
        isPlay = false;
        mHandle.removeMessages(UPDATE_SEEKBAR_VIEW);
        mHandle.removeCallbacksAndMessages(null);

        setResult(Constants.UPDATE_PROGRESS_RESULT, new Intent());
        finish();
    }
}
