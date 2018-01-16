package com.efrobot.talkstory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.efrobot.talkstory.R;
import com.efrobot.talkstory.bean.HistoryBean;

import java.util.List;

/**
 * Created by zd on 2018/1/12.
 */
public class PopupWindowAdapter extends BaseAdapter {

    private Context context;

    private List<HistoryBean> list;

    private LayoutInflater layoutInflater;

    private int id = -1;

    public PopupWindowAdapter(Context context, List<HistoryBean> list, int id) {
        this.context = context;
        this.list = list;
        this.id = id;
        layoutInflater = LayoutInflater.from(context);
    }

    public void updateSelected(int id) {
        this.id = id;
        notifyDataSetChanged();
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
            view = layoutInflater.inflate(R.layout.item_pop_text, null);
            viewHolder.textView = (TextView) view.findViewById(R.id.pop_item_text_view);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.pop_item_image_view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        HistoryBean historyBean = list.get(i);
        if (historyBean != null) {
            String content = historyBean.getTeacherName() + " - " + historyBean.getName();
            viewHolder.textView.setText(content);

            if (id == historyBean.getId()) {
                viewHolder.imageView.setVisibility(View.VISIBLE);
            } else
                viewHolder.imageView.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

}
