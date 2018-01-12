package com.efrobot.talkstory.adapter;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.bean.AlbumBean;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.utils.xUtilsImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.List;

/**
 * Created by zd on 2017/12/22.
 */
public class AlbumAdapter extends BaseAdapter {

    private Context context;
    private List<AlbumItemBean> list;
    private ImageLoader imageLoader;
//    private ImageOptions options;

    public AlbumAdapter(Context context, List<AlbumItemBean> list) {
        this.context = context;
        this.list = list;
        imageLoader = ImageLoader.getInstance();
//        options = new ImageOptions.Builder()
//                //设置加载过程中的图片
//                .setLoadingDrawableId(R.drawable.ic_launcher)
//                //设置加载失败后的图片
//                .setFailureDrawableId(R.drawable.ic_launcher)
//                //设置使用缓存
//                .setUseMemCache(false)
//                //设置显示圆形图片
//                .setCircular(true)
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
            view = LayoutInflater.from(context).inflate(R.layout.item_album, null);
            viewHolder = new ViewHolder();
            viewHolder.albumImage = (ImageView) view.findViewById(R.id.album_image);
            viewHolder.albumTitle = (TextView) view.findViewById(R.id.album_title);
            viewHolder.authorImage = (ImageView) view.findViewById(R.id.album_author_image);
            viewHolder.authorName = (TextView) view.findViewById(R.id.album_author_name);
            viewHolder.storyNum = (TextView) view.findViewById(R.id.album_story_num);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AlbumItemBean data = list.get(i);
        if (!TextUtils.isEmpty(data.getImage()))
//            xUtilsImageUtils.display(viewHolder.albumImage, data.getImage());
            imageLoader.displayImage(data.getImage(), viewHolder.albumImage);

        viewHolder.albumTitle.setText(data.getName());

        if (!TextUtils.isEmpty(data.getTeacherImg()))
//            xUtilsImageUtils.display(viewHolder.authorImage, data.getTeacherImg());
            imageLoader.displayImage(data.getTeacherImg(), viewHolder.authorImage);

        viewHolder.authorName.setText(data.getTeacherName());
        viewHolder.storyNum.setText(data.getAudioCount() + "个故事");
        return view;
    }

    class ViewHolder {
        private ImageView albumImage;
        private TextView albumTitle;
        private ImageView authorImage;
        private TextView authorName;
        private TextView storyNum;
    }

}
