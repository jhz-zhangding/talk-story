package com.efrobot.talkstory.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zd on 2018/1/25.
 */
public class AlbumItemDecortion extends RecyclerView.ItemDecoration {

    private int space = 0;

    public AlbumItemDecortion(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != 0)
            outRect.left = space;
    }
}
