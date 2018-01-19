package com.efrobot.talkstory.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.albumdetail.AlbumDetailActivity;
import com.efrobot.talkstory.albumdetail.DetailListAdapter;
import com.efrobot.talkstory.base.BaseActivity;
import com.efrobot.talkstory.bean.AlbumBean;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.bean.AudiaBean;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.http.HttpParamUtils;
import com.efrobot.talkstory.http.HttpUtils;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.utils.NoScrollGridView;
import com.google.gson.Gson;
import com.jingchen.pulltorefresh.PullToRefreshLayout;

import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchPageActivity extends BaseActivity implements PullToRefreshLayout.OnPullListener, View.OnClickListener {

    private final String TAG = SearchPageActivity.class.getSimpleName();

    private HttpUtils httpUtils;

    private TextView allSearchBtn;

    private EditText searchEt;
    private TextView startSearchBtn;

    private PullToRefreshLayout prl1;

    private ExpandableListView tagListView;
    private TagListApdater tagListApdater;
    private List<SearchDataBean> tagList = new ArrayList<>();

    private NoScrollGridView albumGridView;
    private AlbumSearchAdapter albumAdapter;
    private List<AlbumItemBean> albumBeanList;

    private ImageView imageLeft, imageRight;

    private PullToRefreshLayout pullToRefreshLayout;

    private ListView audioListView;
    private DetailListAdapter detailListAdapter;
    private List<AudiaItemBean> audioList = new ArrayList<>();


    private int page = 1;
    private int size = 10;
    private String keyword = "";
    private int tagId = 0;

    private int albumPage = 1;

    //token是否失效
    private boolean isInvalidToken = false;

    public static void openSearchActivity(Context context, Class cls, String searchStr) {
        Intent intent = new Intent(context, cls);
        intent.putExtra("keyWord", searchStr);
        ((Activity) context).startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_page);

        keyword = getIntent().getStringExtra("keyWord") == null ? "" : getIntent().getStringExtra("keyWord");

        initView();

        getHttpData();

    }

    private void initView() {
        tagListView = (ExpandableListView) findViewById(R.id.search_tag_list_view);

        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);

        audioListView = (ListView) findViewById(R.id.search_content_list_view);
        View headView = LayoutInflater.from(getContext()).inflate(R.layout.search_head_view, null);
        albumGridView = (NoScrollGridView) headView.findViewById(R.id.competitive_products_grid_view);
        imageLeft = (ImageView) headView.findViewById(R.id.search_left);
        imageRight = (ImageView) headView.findViewById(R.id.search_right);

        audioListView.addHeaderView(headView);

        searchEt = (EditText) findViewById(R.id.main_search_edit);
        searchEt.setText(keyword);
        startSearchBtn = (TextView) findViewById(R.id.main_search_edit_btn);

        startSearchBtn.setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        allSearchBtn = (TextView) findViewById(R.id.tag_all_btn);
        albumGridView.setOnItemClickListener(new AlbumItemClickListener());
        audioListView.setOnItemClickListener(new RecentStoryItemClickListener());

        allSearchBtn.setOnClickListener(this);
        imageLeft.setOnClickListener(this);
        imageRight.setOnClickListener(this);
        pullToRefreshLayout.setOnPullListener(this);

        tagListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        tagListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                tagId = tagList.get(groupPosition).getList().get(childPosition).getId();
                page = 1;
                audioList.clear();
                getHttpAlbumData();
                getHttpAudioData();

                if (tagListApdater != null) {
                    String str = tagList.get(groupPosition).getList().get(childPosition).getName();
                    tagListApdater.updateSelected(str);
                }

                return false;
            }
        });
    }

    private class AlbumItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (albumBeanList != null && albumBeanList.size() > 0) {
                Intent intent = new Intent(getContext(), AlbumDetailActivity.class);
                intent.putExtra("id", albumBeanList.get(i).getId());
                startActivityForResult(intent, Constants.MAIN_REQUEST_REQUEST);
            }
        }
    }

    private class RecentStoryItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i != 0) {
                PlayMediaActivity.openActivity(getContext(), PlayMediaActivity.class, audioList.get(i - 1), Constants.MAIN_REQUEST_REQUEST);
            }
        }
    }

    private void getHttpData() {
        getHttpTagData();
        getHttpAlbumData();
        getHttpAudioData();
    }

    private void getHttpTagData() {
        httpUtils = new HttpUtils(isInvalidToken);
        httpUtils.Post(HttpParamUtils.TAG_LIST_URL, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                L.e("onSuccess", HttpParamUtils.TAG_LIST_URL + "////result:" + result);
                SearchMainBean searchMainBean = new Gson().fromJson(result, SearchMainBean.class);
                if (searchMainBean != null && searchMainBean.getData() != null) {
                    if (searchMainBean.getData().size() > 0) {
                        tagList = searchMainBean.getData();
                        setTagAdapter();
                    } else {
                        Toast.makeText(getContext(), "已经没有数据了", Toast.LENGTH_SHORT).show();
                    }
                } else
                    Toast.makeText(getContext(), "暂无数据", Toast.LENGTH_SHORT).show();
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

    private void getHttpAlbumData() {
        httpUtils = new HttpUtils(isInvalidToken);
        Map<String, Object> albumMap = HttpParamUtils.getAlbumParamMap(albumPage, 5, keyword, tagId);
        httpUtils.Post(HttpParamUtils.ALBUM_LIST_URL, albumMap, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                L.e(TAG, "onSuccess:" + result);
                AlbumBean albumBean = new Gson().fromJson(result, AlbumBean.class);
                if (albumBean != null && albumBean.getData() != null) {
                    albumBeanList = albumBean.getData();
                    albumAdapter = new AlbumSearchAdapter(getContext(), albumBeanList);
                    albumGridView.setAdapter(albumAdapter);
                } else {
                    showToast("已经没有数据啦");
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
    }

    private void getHttpAudioData() {
        httpUtils = new HttpUtils(isInvalidToken);
        Map<String, Object> audioMap = HttpParamUtils.getAudioParamMap(page, size, keyword, tagId);
        httpUtils.Post(HttpParamUtils.AUDIO_LIST_URL, audioMap, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                page++;
                AudiaBean audiaBean = new Gson().fromJson(result, AudiaBean.class);
                if (audiaBean != null && audiaBean.getData() != null) {
                    if (audiaBean.getData().size() > 0) {
                        audioList.addAll(audiaBean.getData());
                        setAudioAdapter();
                    } else {
                        Toast.makeText(getContext(), "没有找到数据了哦", Toast.LENGTH_SHORT).show();
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

    private void setTagAdapter() {
        if (tagListApdater == null) {
            tagListApdater = new TagListApdater(this, tagList);
            tagListView.setAdapter(tagListApdater);
        } else {
            tagListApdater.notifyDataSetChanged();
        }

        if (tagList != null) {
            for (int i = 0; i < tagList.size(); i++) {
                tagListView.expandGroup(i);
            }
        }
    }

    private void setAudioAdapter() {
        if (detailListAdapter == null) {
            detailListAdapter = new DetailListAdapter(this, audioList);
            audioListView.setAdapter(detailListAdapter);
        } else {
            detailListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
        page = 1;
        albumPage = 1;
        audioList.clear();
        getHttpAlbumData();
        getHttpAudioData();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {
        getHttpAudioData();
        pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finishAfter();
                break;
            case R.id.main_search_edit_btn:
                /**
                 * 重新搜索内容
                 * */
                againSearch();
                break;
            case R.id.tag_all_btn:
                againSearch();
                break;
            case R.id.search_left:
                if (albumPage == 1) {
                    showToast("已经是第一页啦");
                } else {
                    albumPage--;
                    if (albumPage <= 0) {
                        albumPage = 1;
                    }
                    getHttpAlbumData();
                }
                break;
            case R.id.search_right:
                albumPage++;
                getHttpAlbumData();
                break;
        }
    }

    private void finishAfter() {
        setResult(Constants.UPDATE_PROGRESS_RESULT, new Intent());
        finish();
    }

    @Override
    public void onBackPressed() {
        finishAfter();
        super.onBackPressed();
    }

    private void againSearch() {
        keyword = searchEt.getText().toString();
        tagId = 0;
        page = 1;
        audioList.clear();

        albumPage = 1;

        getHttpData();
        if (tagListApdater != null) {
            tagListApdater.updateSelected("");
        }
    }
}
