package com.debin.atjs.ui.main.drawer;

import android.view.View;
import android.widget.TextView;

import com.debin.atjs.ui.widget.BindableViewHolder;

import com.debin.atjs.R;

/**
 * Created by Stardust on 2017/12/10.
 */

public class DrawerMenuGroupViewHolder extends BindableViewHolder<DrawerMenuItem> {

    private TextView mTextView;

    public DrawerMenuGroupViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.title);
    }

    @Override
    public void bind(DrawerMenuItem data, int position) {
        mTextView.setText(data.getTitle());
        int padding = itemView.getResources().getDimensionPixelOffset(R.dimen.divider_drawer_menu_group);
        itemView.setPadding(0, position == 0 ? 0 : padding, 0, 0);
    }
}