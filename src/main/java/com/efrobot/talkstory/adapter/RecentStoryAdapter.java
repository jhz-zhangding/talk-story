package com.efrobot.talkstory.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.nostra13.universalimageloader.core.ImageLoader;

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
//    private ImageOptions options;

    public RecentStoryAdapter(Context context, List<AudiaItemBean> list) {
        this.context = context;
        this.list = list;
        imageLoader = ImageLoader.getInstance();
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
            view = LayoutInflater.from(context).inflate(R.layout.item_recent_story, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view.findViewById(R.id.recent_story_image);
            viewHolder.authorImage = (ImageView) view.findViewById(R.id.recent_story_author_image);
            viewHolder.storyFrom = (TextView) view.findViewById(R.id.recent_story_from);
            viewHolder.storyTitle = (TextView) view.findViewById(R.id.recent_story_title);

            viewHolder.bilingual = (TextView) view.findViewById(R.id.recent_story_double_language);
            viewHolder.pureEnglish = (TextView) view.findViewById(R.id.recent_story_single_language);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AudiaItemBean data = list.get(i);

        if (!TextUtils.isEmpty(data.getBigImg()))
//            x.image().bind(viewHolder.imageView, data.getBigImg(), options);
            imageLoader.displayImage(data.getBigImg(), viewHolder.imageView);

        if (!TextUtils.isEmpty(data.getTeacherImg()))
//            x.image().bind(viewHolder.authorImage, data.getTeacherImg(), options);
            imageLoader.displayImage(data.getTeacherImg(), viewHolder.authorImage);

        viewHolder.storyFrom.setText(data.getAlbumName());
        viewHolder.storyTitle.setText(data.getAlbumDes());


        return view;
    }

    class ViewHolder {
        ImageView imageView;
        ImageView authorImage;
        TextView storyFrom;
        TextView storyTitle;

        TextView bilingual; //双语
        TextView pureEnglish; //英汉
    }
}
