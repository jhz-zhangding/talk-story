package com.efrobot.talkstory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.bean.VersionBean;

import java.util.List;

/**
 * Created by zd on 2018/1/10.
 */
public class LanguageAdapter extends BaseAdapter {

    private Context context;

    private List<VersionBean> versionBeen;

    public LanguageAdapter(Context context, List<VersionBean> versionBeen) {
        this.context = context;
        this.versionBeen = versionBeen;
    }

    @Override
    public int getCount() {
        return versionBeen.size();
    }

    @Override
    public Object getItem(int i) {
        return versionBeen.get(i);
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
            view = LayoutInflater.from(context).inflate(R.layout.item_version, null);
            viewHolder.textView = (TextView) view.findViewById(R.id.item_language_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        VersionBean versionBean = versionBeen.get(i);
        if (versionBean != null) {
            viewHolder.textView.setText(getLanguageStrFromType(versionBean.getType()));
        }

        return view;
    }

    class ViewHolder {
        TextView textView;
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
