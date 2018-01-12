package com.efrobot.talkstory.allalbum;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.adapter.AlbumAdapter;
import com.efrobot.talkstory.base.BaseActivity;
import com.efrobot.talkstory.base.WithPlayerBaseActivity;
import com.efrobot.talkstory.bean.AlbumBean;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.google.gson.Gson;
import com.jingchen.pulltorefresh.PullToRefreshLayout;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllAlbumActivity extends WithPlayerBaseActivity implements PullToRefreshLayout.OnPullListener {

    private PullToRefreshLayout refreshView;

    private GridView gridView;

    private AlbumAdapter allAlbumAdapter;

    private List<AlbumItemBean> list = new ArrayList<>();

    private HttpUtils httpUtils;

    private int page = 1;
    private int size = 15;
    private String keyword = "";
    private int tagId = 0;

    @Override
    protected int getZdContentView() {
        return R.layout.activity_all_album;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpUtils = new HttpUtils(false);
        initView();
        initListener();
        getHttpData();
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        //下拉刷新
        page = 0;
        getHttpData();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        //上拉加载
        getHttpData();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    private void getHttpData() {
        Map<String, Object> map = HttpParamUtils.getAlbumParamMap(page, size, keyword, tagId);
        httpUtils.Post(HttpParamUtils.ALBUM_LIST_URL, map, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                page++;
                L.e("AllAlbumActivity", "onSuccess:" + result);
                AlbumBean albumBean = new Gson().fromJson(result, AlbumBean.class);
                List<AlbumItemBean> albumItemBeanList = albumBean.getData();
                if (albumItemBeanList != null && albumItemBeanList.size() > 0) {
                    list.addAll(albumBean.getData());
                    setAdapterData();
                } else {
                    Toast.makeText(AllAlbumActivity.this, "已经没有数据了", Toast.LENGTH_SHORT).show();
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

    protected void initView() {
        refreshView = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        gridView = (GridView) findViewById(R.id.all_album_grid_view);

        refreshView.setOnPullListener(this);
    }

    private void setAdapterData() {
        if (allAlbumAdapter == null) {
            allAlbumAdapter = new AlbumAdapter(this, list);
            gridView.setAdapter(allAlbumAdapter);
        } else {
            allAlbumAdapter.notifyDataSetChanged();
        }
    }

    protected void initListener() {

    }

}
