package com.efrobot.talkstory.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.efrobot.talkstory.R;

import java.util.List;

/**
 * Created by zd on 2018/1/13.
 */
public class TagListApdater extends BaseExpandableListAdapter {

    private Context context;

    private List<SearchDataBean> list;

    private LayoutInflater inflater;

    private String str;

    public TagListApdater(Context context, List<SearchDataBean> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    public void updateSelected(String str) {
        this.str = str;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return list.get(groupPosition).getList().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return list.get(groupPosition).getList().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_tag_layout, null);
        TextView textView = (TextView) convertView.findViewById(R.id.tag_type);

        textView.setText(list.get(groupPosition).getName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.tag_content_layout, null);
        TextView textView = (TextView) convertView.findViewById(R.id.tag_name);
        String text = list.get(groupPosition).getList().get(childPosition).getName();
        textView.setText(text);
        if (text.equals(str)) {
            textView.setBackgroundResource(R.color.selected_color);
        } else {
            textView.setBackgroundResource(R.color.unselected_color);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

//    private Context context;
//
//    private List<SearchDataBean> list;
//
//    private LayoutInflater inflater;
//
//    private TagContentAdapter contentAdapter;
//
//    private OnTagClickItemListener onTagClickItemListener;
//
//
//    public TagListApdater(Context context, List<SearchDataBean> list) {
//        this.context = context;
//        this.list = list;
//        inflater = LayoutInflater.from(context);
//    }
//
//    public void setOnTagClickItemListener(OnTagClickItemListener onTagClickItemListener) {
//        this.onTagClickItemListener = onTagClickItemListener;
//    }
//
//    @Override
//    public int getCount() {
//        return list.size();
//    }
//
//    @Override
//    public Object getItem(int i) {
//        return list.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    @Override
//    public View getView(int i, View view, ViewGroup viewGroup) {
//        final ViewHolder viewHolder;
//        if (view == null) {
//            viewHolder = new ViewHolder();
//            view = inflater.inflate(R.layout.item_tag_layout, null);
//            viewHolder.textView = (TextView) view.findViewById(R.id.tag_type);
//            viewHolder.listView = (ListView) view.findViewById(R.id.tag_list);
//            view.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) view.getTag();
//        }
//
//        final SearchDataBean searchDataBean = list.get(i);
//        viewHolder.textView.setText(searchDataBean.getName());
//        contentAdapter = new TagContentAdapter(context, searchDataBean.getList());
//        viewHolder.listView.setAdapter(contentAdapter);
//        viewHolder.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                if (onTagClickItemListener != null) {
//                    onTagClickItemListener.onClick(searchDataBean.getList().get(i).getId());
//                }
//
////                if (contentAdapter != null) {
////                    contentAdapter = new TagContentAdapter(context, searchDataBean.getList(), searchDataBean.getList().get(i).getName());
////                    viewHolder.listView.setAdapter(contentAdapter);
////                }
//            }
//        });
//
//        return view;
//    }
//
//    public void updateSelectedView() {
//        if (contentAdapter != null) {
//            contentAdapter.updateSelectedView("");
//        }
//    }
//
//    class ViewHolder {
//
//        TextView textView;
//
//        ListView listView;
//
//    }
//
//    interface OnTagClickItemListener {
//        void onClick(int targetId);
//    }

}
