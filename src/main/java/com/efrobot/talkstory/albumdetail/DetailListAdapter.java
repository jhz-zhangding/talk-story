package com.efrobot.talkstory.albumdetail;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.adapter.LanguageAdapter;
import com.efrobot.talkstory.bean.AudiaItemBean;
import com.efrobot.talkstory.bean.VersionBean;
import com.nostra13.universalimageloader.core.ImageLoader;
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


    public DetailListAdapter(Context context, List<AudiaItemBean> lists) {
        this.context = context;
        this.lists = lists;
        imageLoader = ImageLoader.getInstance();
        mInflater = LayoutInflater.from(context);
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
        final ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = mInflater.inflate(R.layout.item_detail_list, null);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.item_image);
            viewHolder.title = (TextView) view.findViewById(R.id.item_title);
            viewHolder.teacherImage = (ImageView) view.findViewById(R.id.item_teach_image);
            viewHolder.teachName = (TextView) view.findViewById(R.id.item_teach_name);
            viewHolder.des = (TextView) view.findViewById(R.id.item_des);
            viewHolder.gridView = (TagFlowLayout) view.findViewById(R.id.item_grid_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        AudiaItemBean audiaItemBean = lists.get(i);
        if (audiaItemBean != null) {
            if (!TextUtils.isEmpty(audiaItemBean.getSmallImg()))
                imageLoader.displayImage(audiaItemBean.getSmallImg(), viewHolder.imageView);

            viewHolder.title.setText(audiaItemBean.getName());

            if (!TextUtils.isEmpty(audiaItemBean.getTeacherImg()))
                imageLoader.displayImage(audiaItemBean.getTeacherImg(), viewHolder.teacherImage);

            viewHolder.teachName.setText(audiaItemBean.getTeacherName());

            viewHolder.des.setText(audiaItemBean.getAlbumName());

            List<VersionBean> versionBeen = lists.get(i).getVersions();
//            languageAdapter = new LanguageAdapter(context, versionBeen);
//            if (viewHolder.gridView != null) {
//                viewHolder.gridView.removeAllViews();
//                ViewGroup parent = (ViewGroup) viewHolder.gridView.getParent();
//                if (parent != null) {
//                    parent.removeAllViews();
//                }
//            }

            viewHolder.gridView.setAdapter(new TagAdapter<VersionBean>(versionBeen) {

                @Override
                public View getView(FlowLayout parent, int position, VersionBean versionBean) {
                    TextView tv = (TextView) mInflater.inflate(R.layout.item_version, viewHolder.gridView, false);
                    tv.setText(getLanguageStrFromType(versionBean.getType()));
                    return tv;
                }
            });

        }

        return view;
    }

    class ViewHolder {
        ImageView imageView;
        TextView title;
        ImageView teacherImage;
        TextView teachName;
        TextView des;
        TagFlowLayout gridView;
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
