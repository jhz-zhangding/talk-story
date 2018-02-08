package com.efrobot.talkstory.albumdetail;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.base.WithPlayerBaseActivity;
import com.efrobot.talkstory.bean.AlbumDetail;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.AudioDetailBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.env.PlayListCache;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.utils.OptionsUtils;
import com.google.gson.Gson;
import com.jingchen.pulltorefresh.PullToRefreshLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlbumDetailActivity extends WithPlayerBaseActivity implements View.OnClickListener, PullToRefreshLayout.OnPullListener {

    private final String TAG = AlbumDetailActivity.class.getSimpleName();

    private ImageView detailImage;

    private TextView title;

    private TextView authorName;

    private ImageView teacherImage;

    private TextView total;

    private PullToRefreshLayout ptrl;

    private ListView audioListView;
    private DetailListAdapter detailListAdapter;
    private List<AudiaItemBean> list = new ArrayList<>();

    private int page = 1;
    private int size = 10;

    private int id = -1;
    private HttpUtils httpUtils;

    @Override
    protected int getZdContentView() {
        return R.layout.activity_album_detail;
    }

    @Override
    protected void updateAdapter() {
        if (detailListAdapter != null) {
            detailListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void initView() {
        id = getIntent().getIntExtra("id", -1);

        ptrl = (PullToRefreshLayout) findViewById(R.id.refresh_view);

        audioListView = (ListView) findViewById(R.id.audio_detail_list_view);

        View headView = LayoutInflater.from(this).inflate(R.layout.layout_detail_head, audioListView, false);
        detailImage = (ImageView) headView.findViewById(R.id.detail_image);
        title = (TextView) headView.findViewById(R.id.audio_detail_title);
        authorName = (TextView) headView.findViewById(R.id.audio_detail_author_name);
        teacherImage = (ImageView) headView.findViewById(R.id.item_teach_image);
        total = (TextView) headView.findViewById(R.id.audio_detail_list_num);

        audioListView.addHeaderView(headView);

        findViewById(R.id.album_back_btn).setOnClickListener(this);
        if (id != -1) {
            getHttpData();
        } else {

        }

        /***
         * 更新底部播放器数据
         */
        updatePlayerView();
    }

    @Override
    protected void initListener() {
        ptrl.setOnPullListener(this);
        audioListView.setOnItemClickListener(onItemClickListener);
    }

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i != 0) {
                //专辑页面点击播放后重新生成播放列表
                PlayListCache.getInstance(getContext()).setList(list);

                AudiaItemBean audiaItemBean = list.get(i - 1);
                PlayMediaActivity.openActivity(getContext(), PlayMediaActivity.class, audiaItemBean, Constants.MAIN_REQUEST_REQUEST);
            }
        }
    };


    private void setAlbumInfoData(AlbumDetail albumDetail) {
        AlbumItemBean album = albumDetail.getData();
        if (album != null) {
            if (!TextUtils.isEmpty(album.getImage())) {
                Glide.with(this).load(album.getImage()).apply(OptionsUtils.getInstance().getRoundGlideOption(12)).into(detailImage);
            }
            title.setText(album.getName());

            if (!TextUtils.isEmpty(album.getTeacherImg()))
                ImageLoader.getInstance().displayImage(album.getTeacherImg(), teacherImage, OptionsUtils.getInstance().getCircelOption());
            authorName.setText(album.getTeacherName());
            total.setText(album.getAudioCount() + "个故事");

        }
    }

    private void setAdapterData() {
        if (detailListAdapter == null) {
            detailListAdapter = new DetailListAdapter(this, list);
            audioListView.setAdapter(detailListAdapter);
        } else {
            detailListAdapter.notifyDataSetChanged();
        }
    }

    private void getHttpData() {
        httpUtils = new HttpUtils(false);
        //专辑信息
        Map<String, Object> mapInfo = HttpParamUtils.getAlbumByIdParamMap();
        String finalUrl = String.format(HttpParamUtils.ALBUM_INFO_BY_ID_URL, id);
        httpUtils.Post(finalUrl, mapInfo, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                L.e(TAG, "onSuccess:" + result);
                AlbumDetail albumDetail = new Gson().fromJson(result, AlbumDetail.class);
                if (albumDetail != null) {
                    setAlbumInfoData(albumDetail);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showToast("请求失败，请检查网络");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

        getListHttp();

    }

    private void getListHttp() {
        //专辑列表
        httpUtils = new HttpUtils(false);
        Map<String, Object> map = HttpParamUtils.getAudioUnderAlbumParamMap(page, size, id);
        httpUtils.Post(HttpParamUtils.AUDIO_UNDER_ALBUM_LIST_URL, map, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                page++;
                L.e(TAG, "onSuccess:" + result);
                AudioDetailBean albumBean = new Gson().fromJson(result, AudioDetailBean.class);
                if (albumBean != null && albumBean.getData() != null) {
                    list.addAll(albumBean.getData());
                    setAdapterData();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showToast("请求失败，请检查网络");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.album_back_btn:
                finishAfter();
                break;
        }
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        //下拉刷新
        page = 1;
        list.clear();
        if (detailListAdapter != null) {
            detailListAdapter.notifyDataSetChanged();
        }
        getHttpData();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        getListHttp();

        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onBackPressed() {
        finishAfter();
        super.onBackPressed();
    }

    private void finishAfter() {
        setResult(Constants.UPDATE_PROGRESS_RESULT, new Intent());
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.UPDATE_PROGRESS_RESULT) {
            if (detailListAdapter != null) {
                detailListAdapter.notifyDataSetChanged();
            }
        }
    }

}
