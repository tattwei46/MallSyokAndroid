package com.nasipattaya.mallsyok.Menu;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.R;

import java.util.ArrayList;
import java.util.List;

public class OthersActivity extends BottomNavigationBar {

    private List<Others> others;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI handle
        RecyclerView recycleView = (RecyclerView)findViewById(R.id.recycle_view);

        recycleView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OthersActivity.this);
        recycleView.setLayoutManager(linearLayoutManager);

        initializeOthersMenu();

        // Hide action bar
        getSupportActionBar().hide();

        OthersRecycleViewAdapter adapter = new OthersRecycleViewAdapter(others, OthersActivity.this);
        recycleView.setAdapter(adapter);

    }

    @Override
    public int getContentViewId() {
        return R.layout.others_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.others_menu;
    }

    public class Others {
        String name;
        int image;

        Others(String name, int image){
            this.name = name;
            this.image = image;
        }
    }

    private void initializeOthersMenu(){
        others = new ArrayList<>();
        others.add(new Others(getResources().getString(R.string.MENU_OTHERS_CONTACT), R.drawable.others_contact));
        others.add(new Others(getResources().getString(R.string.MENU_OTHERS_OPENING), R.drawable.others_opening));
        others.add(new Others(getResources().getString(R.string.MENU_OTHERS_PARKING), R.drawable.others_parking));
        others.add(new Others(getResources().getString(R.string.MENU_OTHERS_DIRECTION), R.drawable.others_direction));
        others.add(new Others(getResources().getString(R.string.MENU_OTHERS_ABOUT_US), R.drawable.others_about));
    }
}


