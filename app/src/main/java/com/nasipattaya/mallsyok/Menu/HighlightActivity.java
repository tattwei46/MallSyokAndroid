package com.nasipattaya.mallsyok.Menu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.R;

public class HighlightActivity extends BottomNavigationBar {

    private TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init UI
        textView = (TextView) findViewById(R.id.activity_label);
        textView.setText("Highlights");

        // Hide action bar
        getSupportActionBar().hide();

    }

    @Override
    public int getContentViewId() {
        return R.layout.highlight_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return 1;
        //return R.id.highlight_menu;
    }
}

