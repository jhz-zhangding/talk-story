package com.efrobot.talkstory.allalbum;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by zd on 2018/1/10.
 */
public class AllAlbumAdapter extends BaseAdapter {

    private Context context;

    private List<AlbumItemBean> albumBeanList;

    private LayoutInflater inflater;

    private ImageLoader imageLoader;

    public AllAlbumAdapter(Context context, List<AlbumItemBean> albumBeanList) {
        this.context = context;
        this.albumBeanList = albumBeanList;
        this.inflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return albumBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return albumBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.item_all_album, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.all_album_item_image);
            viewHolder.title = (TextView) view.findViewById(R.id.all_album_item_name);
            viewHolder.teacherImage = (ImageView) view.findViewById(R.id.all_album_item_teacher_image);
            viewHolder.teacherName = (TextView) view.findViewById(R.id.all_album_item_teacher_name);
            viewHolder.count = (TextView) view.findViewById(R.id.all_album_item_count);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AlbumItemBean data = albumBeanList.get(i);
        if (data != null) {
            if (!TextUtils.isEmpty(data.getImage()))
                imageLoader.displayImage(data.getImage(), viewHolder.imageView);

            viewHolder.title.setText(data.getName());

            if (!TextUtils.isEmpty(data.getTeacherImg()))
                imageLoader.displayImage(data.getTeacherImg(), viewHolder.teacherImage);

            viewHolder.teacherName.setText(data.getTeacherName());

            viewHolder.count.setText(data.getAudioCount() + "个故事");
        }

        return view;
    }

    class ViewHolder {
        ImageView imageView;
        TextView title;
        ImageView teacherImage;
        TextView teacherName;
        TextView count;
    }
}
