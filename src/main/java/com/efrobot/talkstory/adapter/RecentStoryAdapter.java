package com.efrobot.talkstory.adapter;

import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.List;

/**
 * Created by zd on 2017/12/22.
 */
public class RecentStoryAdapter extends BaseAdapter {

    private Context context;
    private List<AudiaItemBean> list;
    private ImageLoader imageLoader;
    private MyTagAdapter myTagAdapter;
    private LayoutInflater inflater;
    private TalkStoryApplication application;
    private int id = -1;
    private int dataId;
    //    private ImageOptions options;

    public RecentStoryAdapter(Context context, List<AudiaItemBean> list) {
        this.context = context;
        this.list = list;
        imageLoader = ImageLoader.getInstance();
        inflater = LayoutInflater.from(context);
        application = TalkStoryApplication.from(context);
//        options = new ImageOptions.Builder()
//                //设置加载过程中的图片
//                .setLoadingDrawableId(R.mipmap.ic_launcher)
//                //设置加载失败后的图片
//                .setFailureDrawableId(R.mipmap.ic_launcher)
//                //设置使用缓存
//                .setUseMemCache(false)
//                //设置显示圆形图片
//                .setCircular(false)
//                //设置支持gif
//                .setIgnoreGif(false)
//                .build();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_recent_story, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view.findViewById(R.id.recent_story_image);
            viewHolder.authorImage = (ImageView) view.findViewById(R.id.recent_story_author_image);
            viewHolder.storyFrom = (TextView) view.findViewById(R.id.recent_story_from);
            viewHolder.storyTitle = (TextView) view.findViewById(R.id.recent_story_title);

            viewHolder.tagFlowLayout = (TagFlowLayout) view.findViewById(R.id.language_flow_layout);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final AudiaItemBean data = list.get(i);

        if (!TextUtils.isEmpty(data.getBigImg()))
//            x.image().bind(viewHolder.imageView, data.getBigImg(), options);
            imageLoader.displayImage(data.getBigImg(), viewHolder.imageView);

        if (!TextUtils.isEmpty(data.getTeacherImg()))
//            x.image().bind(viewHolder.authorImage, data.getTeacherImg(), options);
            imageLoader.displayImage(data.getTeacherImg(), viewHolder.authorImage);

        viewHolder.storyFrom.setText(data.getTeacherName() + "  " + data.getAlbumName());
        viewHolder.storyTitle.setText(data.getName());

        dataId = data.getId();
        myTagAdapter = new MyTagAdapter(data.getVersions());
        viewHolder.tagFlowLayout.setAdapter(myTagAdapter);
        viewHolder.tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                int type = data.getVersions().get(position).getType();
                if (myTagAdapter != null) {
                    myTagAdapter.notifyDataChanged();
                }
                PlayMediaActivity.openActivity(context, PlayMediaActivity.class, data, type, Constants.MAIN_REQUEST_REQUEST);
                return false;
            }
        });


        return view;
    }

    class ViewHolder {
        ImageView imageView;
        ImageView authorImage;
        TextView storyFrom;
        TextView storyTitle;

        TagFlowLayout tagFlowLayout;
    }

    class MyTagAdapter extends TagAdapter<VersionBean> {

        public MyTagAdapter(List<VersionBean> datas) {
            super(datas);
        }

        @Override
        public View getView(FlowLayout parent, int position, VersionBean o) {
            TextView textView = (TextView) inflater.inflate(R.layout.item_version, null);
            String txt = getLanguageStrFromType(o.getType()) + "" + TimeUtils.ShowTime(o.getPlayTime() * 1000);
            textView.setText(txt);

            if (application.getCurrentPlayBean() != null) {
                if (!TextUtils.isEmpty(application.getCurrentPlayBean().getAudioUrl())) {
                    if (o.getAudioUrl().equals(application.getCurrentPlayBean().getAudioUrl())) {
                        textView.setBackgroundResource(R.drawable.bg_language_selected);
                    } else {
                        textView.setBackgroundResource(R.drawable.bg_language_unselected);
                    }
                }
            } else {
                textView.setBackgroundResource(R.drawable.bg_language_unselected);
            }
            return textView;
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
}
