package com.efrobot.talkstory.albumdetail;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.TalkStoryApplication;
import com.efrobot.talkstory.adapter.LanguageAdapter;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.efrobot.talkstory.env.Constants;
import com.efrobot.talkstory.play.PlayMediaActivity;
import com.efrobot.talkstory.utils.OptionsUtils;
import com.efrobot.talkstory.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.List;

/**
 * Created by zd on 2018/1/10.
 */
public class DetailListAdapter extends BaseAdapter {

    private Context context;

    private List<AudiaItemBean> lists;

    private ImageLoader imageLoader;

    private LanguageAdapter languageAdapter;

    private LayoutInflater mInflater;

    private MyTagAdapter myTagAdapter;

    private TalkStoryApplication application;


    public DetailListAdapter(Context context, List<AudiaItemBean> lists) {
        this.context = context;
        this.lists = lists;
        imageLoader = ImageLoader.getInstance();
        mInflater = LayoutInflater.from(context);
        application = TalkStoryApplication.from(context);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int i) {
        return lists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_detail_list, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.item_image);
            viewHolder.title = (TextView) view.findViewById(R.id.item_title);
            viewHolder.teacherImage = (ImageView) view.findViewById(R.id.item_teach_image);
            viewHolder.teachName = (TextView) view.findViewById(R.id.item_teach_name);
            viewHolder.des = (TextView) view.findViewById(R.id.item_des);
            viewHolder.tagFlowLayout = (TagFlowLayout) view.findViewById(R.id.item_grid_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final AudiaItemBean audiaItemBean = lists.get(i);
        viewHolder.imageView.setTag(audiaItemBean.getSmallImg());
        if (null != audiaItemBean) {
            if (!TextUtils.isEmpty(audiaItemBean.getSmallImg())) {
                ImageAware imageAware = new ImageViewAware(viewHolder.imageView, false);
                imageLoader.displayImage(audiaItemBean.getSmallImg(), imageAware, OptionsUtils.getInstance().getCircelOption());
            }

            viewHolder.title.setText(audiaItemBean.getName());

            if (!TextUtils.isEmpty(audiaItemBean.getTeacherImg())) {
                ImageAware imageAware = new ImageViewAware(viewHolder.teacherImage, false);
                imageLoader.displayImage(audiaItemBean.getTeacherImg(), imageAware, OptionsUtils.getInstance().getCircelOption());
            }

            viewHolder.teachName.setText(audiaItemBean.getTeacherName());

            viewHolder.des.setText(audiaItemBean.getAlbumName());

            final List<VersionBean> versionBeen = lists.get(i).getVersions();
            myTagAdapter = new MyTagAdapter(versionBeen);
            viewHolder.tagFlowLayout.setAdapter(myTagAdapter);
            viewHolder.tagFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
                @Override
                public boolean onTagClick(View view, int position, FlowLayout parent) {
                    int type = versionBeen.get(position).getType();
                    if (myTagAdapter != null) {
                        myTagAdapter.notifyDataChanged();
                    }
                    PlayMediaActivity.openActivity(context, PlayMediaActivity.class, audiaItemBean, type, Constants.MAIN_REQUEST_REQUEST);
                    return false;
                }
            });

        }

        return view;
    }

    class MyTagAdapter extends TagAdapter<VersionBean> {

        public MyTagAdapter(List<VersionBean> datas) {
            super(datas);
        }

        @Override
        public View getView(FlowLayout parent, int position, VersionBean o) {
            TextView textView = (TextView) mInflater.inflate(R.layout.item_version, null);
            String txt = getLanguageStrFromType(o.getType()) + "" + TimeUtils.ShowTime(o.getPlayTime() * 1000);
            textView.setText(txt);

            if (application.getCurrentPlayBean() != null) {
                if (!TextUtils.isEmpty(application.getCurrentPlayBean().getAudioUrl())) {
                    if (o.getAudioUrl().equals(application.getCurrentPlayBean().getAudioUrl())) {
                        setBackgroundColor(o.getType(), textView, true);
                    } else {
                        setBackgroundColor(o.getType(), textView, false);
                    }
                }
            } else {
                textView.setBackgroundResource(R.drawable.bg_language_unselected);
            }
            return textView;
        }

    }

    class ViewHolder {
        ImageView imageView;
        TextView title;
        ImageView teacherImage;
        TextView teachName;
        TextView des;
        TagFlowLayout tagFlowLayout;
    }

    //双语
    private final int DOUBLE_LANGUAGE = 1;
    //纯英
    private final int ENGLISH_LANGUAGE = 2;
    //中文
    private final int CHINESE_LANGUAGE = 3;
    //原版
    private final int SOURCE_LANGUAGE = 4;

    private void setBackgroundColor(int type, TextView textView, boolean isSelected) {
        switch (type) {
            case DOUBLE_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.double_language_selected);
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setBackgroundResource(R.drawable.double_language_unselected);
                    textView.setTextColor(Color.parseColor("#FF60C1D6"));
                }
                break;
            case ENGLISH_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.english_language_selected);
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setBackgroundResource(R.drawable.english_language_unselected);
                    textView.setTextColor(Color.parseColor("#FFE98C8C"));
                }
                break;
            case CHINESE_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.chinese_language_selected);
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setBackgroundResource(R.drawable.chinese_language_unselected);
                    textView.setTextColor(Color.parseColor("#FFF5A623"));
                }
                break;
            case SOURCE_LANGUAGE:
                if (isSelected) {
                    textView.setBackgroundResource(R.drawable.source_language_selected);
                    textView.setTextColor(Color.WHITE);
                } else {
                    textView.setBackgroundResource(R.drawable.source_language_unselected);
                    textView.setTextColor(Color.parseColor("#cccccc"));
                }
                break;
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
