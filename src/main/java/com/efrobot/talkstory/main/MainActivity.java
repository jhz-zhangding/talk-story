package com.efrobot.talkstory.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.AlbumAdapter;
import com.efrobot.talkstory.adapter.PopupWindowAdapter;
import com.efrobot.talkstory.adapter.RecentStoryAdapter;
import com.efrobot.talkstory.albumdetail.AlbumDetailActivity;
import com.efrobot.talkstory.allalbum.AllAlbumActivity;
import com.efrobot.talkstory.base.WithPlayerBaseActivity;
import com.efrobot.talkstory.bean.AlbumBean;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.bean.AudiaBean;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.HistoryBean;
import com.efrobot.talkstory.db.HistoryManager;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.service.MediaPlayService;
import com.efrobot.talkstory.utils.NoScrollGridView;
import com.efrobot.talkstory.utils.TimeUtils;
import com.google.gson.Gson;
import com.jingchen.pulltorefresh.PullToRefreshLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends WithPlayerBaseActivity implements View.OnClickListener, PullToRefreshLayout.OnPullListener {

    private final String TAG = MainActivity.class.getSimpleName();

    private HttpUtils httpUtils;

    private EditText searchEt;
    private TextView startSearchBtn;

    private PullToRefreshLayout ptrl;

    private NoScrollGridView albumGridView;
    private NoScrollGridView recentStoryGridView;

    private AlbumAdapter albumAdapter;
    private RecentStoryAdapter recentStoryAdapter;

    private List<AlbumItemBean> albumBeanList;
    private List<AudiaItemBean> recentStoryBeanList;

    //请求参数
    private int page = 1;
    private int size = 10;
    private String keyword = "";
    private int tagId = 0;

    private TalkStoryApplication application;

    @Override
    protected int getZdContentView() {
        return R.layout.activity_main;
    }


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
    }

    @Override
    protected void initView() {
        httpUtils = new HttpUtils(false);

        application = TalkStoryApplication.from(this);

        searchEt = (EditText) findViewById(R.id.main_search_edit);
        startSearchBtn = (TextView) findViewById(R.id.main_search_edit_btn);
        ptrl = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        albumGridView = (NoScrollGridView) findViewById(R.id.competitive_products_grid_view);
        recentStoryGridView = (NoScrollGridView) findViewById(R.id.recent_story_grid_view);

        albumBeanList = new ArrayList<>();
        recentStoryBeanList = new ArrayList<>();

        setListener();
        setHttpData();
    }

    @Override
    protected void initListener() {

    }

    private void setHttpData() {
        Map<String, Object> albumMap = HttpParamUtils.getAlbumParamMap(0, 5, "", 0);
        httpUtils.Post(HttpParamUtils.ALBUM_LIST_URL, albumMap, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                L.e(TAG, "onSuccess:" + result);
                AlbumBean albumBean = new Gson().fromJson(result, AlbumBean.class);
                if (albumBean != null && albumBean.getData() != null) {
                    albumBeanList = albumBean.getData();
                    albumAdapter = new AlbumAdapter(MainActivity.this, albumBeanList);
                    albumGridView.setAdapter(albumAdapter);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                L.e(TAG, "onError");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                L.e(TAG, "onCancelled");
            }

            @Override
            public void onFinished() {
                L.e(TAG, "onFinished");
            }
        });

        Map<String, Object> audioMap = HttpParamUtils.getAudioParamMap(page, size, keyword, tagId);
        httpUtils.Post(HttpParamUtils.AUDIO_LIST_URL, audioMap, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                AudiaBean audiaBean = new Gson().fromJson(result, AudiaBean.class);
                if (audiaBean != null && audiaBean.getData() != null) {
                    recentStoryBeanList = audiaBean.getData();
                    if (recentStoryBeanList != null && recentStoryBeanList.size() > 0) {
                        setAudioAdapterData();
                    } else {
                        Toast.makeText(MainActivity.this, "已经没有数据了", Toast.LENGTH_SHORT).show();
                    }
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
        });

    }

    private void setListener() {
        startSearchBtn.setOnClickListener(this);
        findViewById(R.id.query_all_album_btn).setOnClickListener(this);
        albumGridView.setOnItemClickListener(new AlbumItemClickListener());
        recentStoryGridView.setOnItemClickListener(new RecentStoryItemClickListener());

        //设置下拉，上拉监听
        ptrl.setOnPullListener(this);
    }

    private void setAlbumAdapterData() {
        if (albumAdapter != null) {
            albumAdapter = new AlbumAdapter(this, albumBeanList);
            albumGridView.setAdapter(albumAdapter);
        } else {

        }
    }

    private void setAudioAdapterData() {
        if (recentStoryAdapter == null) {
            recentStoryAdapter = new RecentStoryAdapter(MainActivity.this, recentStoryBeanList);
            recentStoryGridView.setAdapter(recentStoryAdapter);
        } else {
            recentStoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    private class AlbumItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (albumBeanList != null && albumBeanList.size() > 0) {
                Intent intent = new Intent(MainActivity.this, AlbumDetailActivity.class);
                intent.putExtra("id", albumBeanList.get(i).getId());
                startActivity(intent);
            }
        }
    }

    private class RecentStoryItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            PlayMediaActivity.openActivity(MainActivity.this, PlayMediaActivity.class, recentStoryBeanList.get(i), Constants.MAIN_REQUEST_REQUEST);
//            Intent intent = new Intent(MainActivity.this, PlayMediaActivity.class);
//            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.main_search_edit_btn:

                break;
            case R.id.query_all_album_btn:
                Intent intent = new Intent(this, AllAlbumActivity.class);
                startActivityForResult(intent, RESULT_OK);
                break;
        }
    }

    @Override
    public boolean isShouldHideInput(View v, MotionEvent event) {
        return super.isShouldHideInput(v, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.UPDATE_PROGRESS_RESULT) {
            updatePlayerView();
        }
    }

}
