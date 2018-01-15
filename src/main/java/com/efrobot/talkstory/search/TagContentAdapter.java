package com.efrobot.talkstory.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.efrobot.talkstory.R;

import java.util.List;

/**
 * Created by zd on 2018/1/13.
 */
public class TagContentAdapter extends BaseAdapter {

    private Context context;

    private List<SearchItemBean> list;

    private LayoutInflater inflater;

    public TagContentAdapter(Context context, List<SearchItemBean> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
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
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.tag_content_layout, null);
            viewHolder.textView = (TextView) view.findViewById(R.id.tag_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String text = list.get(i).getName();
        viewHolder.textView.setText(text);

        return view;
    }

    class ViewHolder {

        TextView textView;

    }

}
