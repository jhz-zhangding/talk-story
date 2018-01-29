package com.efrobot.talkstory.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.PreferencesUtils;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.AlbumItemDecortion;
import com.efrobot.talkstory.adapter.AlbumRecylerAdapter;
import com.efrobot.talkstory.adapter.RecentStoryAdapter;
import com.efrobot.talkstory.albumdetail.AlbumDetailActivity;
import com.efrobot.talkstory.allalbum.AllAlbumActivity;
import com.efrobot.talkstory.base.WithPlayerBaseActivity;
import com.efrobot.talkstory.bean.AlbumBean;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.bean.AudiaBean;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.env.PlayListCache;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.search.SearchPageActivity;
import com.google.gson.Gson;
import com.jingchen.pulltorefresh.PullToRefreshLayout;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends WithPlayerBaseActivity implements View.OnClickListener, PullToRefreshLayout.OnPullListener, AbsListView.OnScrollListener {

    private final String TAG = MainActivity.class.getSimpleName();

    private HttpUtils httpUtils;

    private EditText searchEt;
    private TextView startSearchBtn;

    private PullToRefreshLayout ptrl;

    private RecyclerView albumGridView;
    private GridView recentStoryGridView;

    private AlbumRecylerAdapter albumAdapter;
    private RecentStoryAdapter recentStoryAdapter;

    private List<AlbumItemBean> albumBeanList;
    private List<AudiaItemBean> recentStoryBeanList;

    //请求参数
    private int page = 1;
    private int size = 10;
    private String keyword = "";
    private int tagId = 0;

    private TalkStoryApplication application;

    private int lastid = 0;
    private int lastType = 0;

    private int visibleItemCount;
    private int visibleLastIndex;

    @Override
    protected int getZdContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void updateAdapter() {
        if (recentStoryAdapter != null) {
            recentStoryAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
    }

    View headView;

    @Override
    protected void initView() {
        lastid = PreferencesUtils.getInt(getContext(), "lastId");
        lastType = PreferencesUtils.getInt(getContext(), "lastType");
        httpUtils = new HttpUtils(false);

        application = TalkStoryApplication.from(this);
        application.isStopService = false;

        searchEt = (EditText) findViewById(R.id.main_search_edit);
        startSearchBtn = (TextView) findViewById(R.id.main_search_edit_btn);
        ptrl = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        recentStoryGridView = (GridView) findViewById(R.id.recent_story_grid_view);
        albumGridView = (RecyclerView) findViewById(R.id.competitive_products_grid_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        albumGridView.addItemDecoration(new AlbumItemDecortion(52));
        albumGridView.setLayoutManager(linearLayoutManager);

        albumBeanList = new ArrayList<>();
        recentStoryBeanList = new ArrayList<>();

        setListener();
        setHttpData();

        setFirstIntoHistoryData();
        registerMainBoradcastReceiver();
    }

    private boolean isNeedSetHistory = false;

    private void setFirstIntoHistoryData() {
        //设置历史记录
        List<AudiaItemBean> historyBeanList = PlayListCache.getInstance(this).getList();
        if (historyBeanList != null && historyBeanList.size() > 0) {
            for (int i = 0; i < historyBeanList.size(); i++) {
                if (historyBeanList.get(i).getId() == lastid) {
                    AudiaItemBean audiaItemBean = historyBeanList.get(i);
                    List<VersionBean> versionBeanList = audiaItemBean.getVersions();
                    for (int j = 0; j < versionBeanList.size(); j++) {
                        if (lastType == versionBeanList.get(j).getType()) {
                            application.setCurrentPlayBean(audiaItemBean, versionBeanList.get(i));
                            isNeedSetHistory = false;
                            break;
                        }
                    }
                }
            }
        } else {
            isNeedSetHistory = true;
        }
        updatePlayerView();
    }

    private void setCurrentBean() {

    }

    @Override
    protected void initListener() {

    }

    private void setHttpData() {
        Map<String, Object> albumMap = HttpParamUtils.getAlbumParamMap(0, 10, "", 0);
        httpUtils.Post(HttpParamUtils.ALBUM_LIST_URL, albumMap, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                L.e(TAG, "onSuccess:" + result);
                AlbumBean albumBean = new Gson().fromJson(result, AlbumBean.class);
                if (albumBean != null && albumBean.getData() != null) {
                    albumBeanList = albumBean.getData();
                    albumAdapter = new AlbumRecylerAdapter(MainActivity.this, albumBeanList);
                    albumAdapter.setOnItemClickListener(new AlbumRecylerAdapter.OnItemClickListener() {
                        @Override
                        public void onClick(int position) {
                            if (albumBeanList != null && albumBeanList.size() > 0) {
                                Intent intent = new Intent(MainActivity.this, AlbumDetailActivity.class);
                                intent.putExtra("id", albumBeanList.get(position).getId());
                                startActivityForResult(intent, Constants.MAIN_REQUEST_REQUEST);
                            }
                        }
                    });
                    albumGridView.setAdapter(albumAdapter);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                L.e(TAG, "onError");
                showToast("请求失败，请检查网络");
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

        getAudioList();

    }

    private void getAudioList() {
        Map<String, Object> audioMap = HttpParamUtils.getAudioParamMap(page, size, keyword, tagId);
        httpUtils.Post(HttpParamUtils.AUDIO_LIST_URL, audioMap, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                page++;
                AudiaBean audiaBean = new Gson().fromJson(result, AudiaBean.class);
                if (audiaBean != null && audiaBean.getData() != null) {
                    if (audiaBean.getData().size() > 0) {

                        /**防止第一次进入没有历史记录从而底部播放器没有内容**/
                        if (application.getCurrentPlayBean() == null) {
                            application.setCurrentPlayBean(audiaBean.getData().get(0), -1);
                            updatePlayerView();
                        }

                        recentStoryBeanList.addAll(audiaBean.getData());
                        setAudioAdapterData();
                    } else {
                        Toast.makeText(MainActivity.this, "已经没有数据了", Toast.LENGTH_SHORT).show();
                    }
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

    private void setListener() {
        startSearchBtn.setOnClickListener(this);
        findViewById(R.id.query_all_album_btn).setOnClickListener(this);
//        albumGridView.setOnItemClickListener(new AlbumItemClickListener());
        recentStoryGridView.setOnItemClickListener(new RecentStoryItemClickListener());

        //设置下拉，上拉监听
        ptrl.setOnPullListener(this);
    }

//    private void setAlbumAdapterData() {
//        if (albumAdapter != null) {
//            albumAdapter = new AlbumAdapter(this, albumBeanList);
//            albumGridView.setAdapter(albumAdapter);
//        } else {
//
//        }
//    }

    private void setAudioAdapterData() {
        if (recentStoryAdapter == null) {
            recentStoryAdapter = new RecentStoryAdapter(MainActivity.this, recentStoryBeanList);
            recentStoryGridView.setAdapter(recentStoryAdapter);

            //存储当前列表 用于切换上下条
            if (isNeedSetHistory) {
                PlayListCache.getInstance(getContext()).setList(recentStoryBeanList);
            }
        } else {
            recentStoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        page = 1;
        recentStoryBeanList.clear();
        setHttpData();

        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        getAudioList();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.visibleItemCount = visibleItemCount;
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    private class AlbumItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (albumBeanList != null && albumBeanList.size() > 0) {
                Intent intent = new Intent(MainActivity.this, AlbumDetailActivity.class);
                intent.putExtra("id", albumBeanList.get(i).getId());
                startActivityForResult(intent, Constants.MAIN_REQUEST_REQUEST);
            }
        }
    }

    private class RecentStoryItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            PlayListCache.getInstance(getContext()).setList(recentStoryBeanList);
            PlayMediaActivity.openActivity(MainActivity.this, PlayMediaActivity.class, recentStoryBeanList.get(i), Constants.MAIN_REQUEST_REQUEST);
            //将当前的故事列表存储
//            Intent intent = new Intent(MainActivity.this, PlayMediaActivity.class);
//            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.main_search_edit_btn:
                String keyword = searchEt.getText().toString();
                SearchPageActivity.openSearchActivity(getContext(), SearchPageActivity.class, keyword, Constants.MAIN_REQUEST_REQUEST);
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

    public void registerMainBoradcastReceiver() {
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
                if (recentStoryAdapter != null) {
                    recentStoryAdapter.notifyDataSetChanged();
                }
            }
        }

    };


    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        application.isStopService = true;
        application.stopMediaService();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.UPDATE_PROGRESS_RESULT) {
            if (recentStoryAdapter != null) {
                recentStoryAdapter.notifyDataSetChanged();
            }
        }
    }
}
