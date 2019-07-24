package com.nasipattaya.mallsyok;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.nasipattaya.mallsyok.Directory.OutletListActivity;
import com.nasipattaya.mallsyok.Menu.HighlightActivity;
import com.nasipattaya.mallsyok.Menu.HomeActivity;
import com.nasipattaya.mallsyok.Menu.MapActivity;
import com.nasipattaya.mallsyok.Menu.NavigationActivity;
import com.nasipattaya.mallsyok.Menu.OthersActivity;

/**
 * BottomNavigationBar class contains public methods that can be accessed across all class that extends from it
 */

public abstract class BottomNavigationBar extends PublicID implements BottomNavigationView.OnNavigationItemSelectedListener {
    
    private BottomNavigationView bottomNavigationView;
    private int navBarHeight;
    private int navBarWidth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav_bar);
        BottomNavigationBarHelper.disableShiftMode((bottomNavigationView));
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        navBarHeight = bottomNavigationView.getMeasuredHeight();
        navBarWidth = bottomNavigationView.getMeasuredWidth();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateNavigationBarState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    public int getNavBarHeight(){
        return navBarHeight;
    }

    public int getNavBarWidth(){
        return navBarWidth;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    public void hideNavigationBar(){
        bottomNavigationView.setVisibility(View.GONE);
    }

    public void showNavigationBar(){
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

    public abstract int getContentViewId();

    public abstract int getNavigationMenuItemId();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_menu:
                startActivity(new Intent(this, HomeActivity.class));
                break;
//            case R.id.highlight_menu:
//                //Todo to remove toast when feature ready
//                ToastUtils.toastShort(this, "This feature is not supported yet!");
//                //startActivity(new Intent(this, HighlightActivity.class));
//                break;
            case R.id.directory_menu:
                if (getGlobalMallKey() != null) {
                    startActivity(new Intent(this, OutletListActivity.class));
                } else ToastUtils.toastShort(this, "Please select a mall");
                break;
            case R.id.navigation_menu:

                if (getGlobalMallKey() != null) {
                    startActivity(new Intent(this, MapActivity.class));
                } else ToastUtils.toastShort(this, "Please select a mall");
                break;
            case R.id.others_menu:

                if (getGlobalMallKey() != null){
                    startActivity(new Intent(this, OthersActivity.class));
                } else ToastUtils.toastShort(this, "Please select a mall");
                break;
        }
        return false;
    }

}
