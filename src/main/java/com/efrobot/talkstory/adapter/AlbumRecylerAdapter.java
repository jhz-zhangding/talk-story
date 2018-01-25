package com.efrobot.talkstory.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.efrobot.talkstory.R;
import com.efrobot.talkstory.bean.AlbumItemBean;
import com.efrobot.talkstory.utils.OptionsUtils;

import java.util.List;

/**
 * Created by zd on 2018/1/25.
 */
public class AlbumRecylerAdapter extends RecyclerView.Adapter<AlbumRecylerAdapter.ViewHolder> {

    private Context context;
    private List<AlbumItemBean> list;
    private LayoutInflater inflater;
    private OnItemClickListener onItemClickListener;

    public AlbumRecylerAdapter(Context context, List<AlbumItemBean> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_album, null);
        view.setOnClickListener(new MyOnClickListener());
        return new ViewHolder(view);
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onClick((Integer) v.getTag());
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.itemView.setTag(position);
        AlbumItemBean data = list.get(position);
        if (!TextUtils.isEmpty(data.getImage())) {
            Glide.with(context).load(data.getImage()).apply(OptionsUtils.getInstance().getRoundGlideOption(12)).into(viewHolder.albumImage);
        }

        viewHolder.albumTitle.setText(data.getName());

        if (!TextUtils.isEmpty(data.getTeacherImg())) {
            Glide.with(context).load(data.getTeacherImg()).apply(RequestOptions.circleCropTransform()).into(viewHolder.authorImage);
        }

        viewHolder.authorName.setText(data.getTeacherName());
        viewHolder.storyNum.setText(data.getAudioCount() + "个故事");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView albumImage;
        private TextView albumTitle;
        private ImageView authorImage;
        private TextView authorName;
        private TextView storyNum;

        public ViewHolder(View view) {
            super(view);
            albumImage = (ImageView) view.findViewById(R.id.album_image);
            albumTitle = (TextView) view.findViewById(R.id.album_title);
            authorImage = (ImageView) view.findViewById(R.id.album_author_image);
            authorName = (TextView) view.findViewById(R.id.album_author_name);
            storyNum = (TextView) view.findViewById(R.id.album_story_num);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

}
