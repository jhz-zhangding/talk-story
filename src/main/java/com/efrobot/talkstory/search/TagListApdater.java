package com.efrobot.talkstory.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.efrobot.talkstory.R;

import java.util.List;

/**
 * Created by zd on 2018/1/13.
 */
public class TagListApdater extends BaseAdapter {

    private Context context;

    private List<SearchDataBean> list;

    private LayoutInflater inflater;

    private TagContentAdapter contentAdapter;

    private OnTagClickItemListener onTagClickItemListener;


    public TagListApdater(Context context, List<SearchDataBean> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void setOnTagClickItemListener(OnTagClickItemListener onTagClickItemListener) {
        this.onTagClickItemListener = onTagClickItemListener;
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
            view = inflater.inflate(R.layout.item_tag_layout, null);
            viewHolder.textView = (TextView) view.findViewById(R.id.tag_type);
            viewHolder.listView = (ListView) view.findViewById(R.id.tag_list);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final SearchDataBean searchDataBean = list.get(i);
        viewHolder.textView.setText(searchDataBean.getName());
        contentAdapter = new TagContentAdapter(context, searchDataBean.getList());
        viewHolder.listView.setAdapter(contentAdapter);
        viewHolder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (onTagClickItemListener != null) {
                    onTagClickItemListener.onClick(searchDataBean.getList().get(i).getId());
                }
            }
        });

        return view;
    }

    class ViewHolder {

        TextView textView;

        ListView listView;

    }

    interface OnTagClickItemListener {
        void onClick(int targetId);
    }

}
