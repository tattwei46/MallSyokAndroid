package com.nasipattaya.mallsyok.Others;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.Menu.OthersActivity;
import com.nasipattaya.mallsyok.R;

public class AboutActivity extends BottomNavigationBar {
    private TextView activityText;
    private ImageButton navigateBackBtn;
    private TextView websiteText;
    private TextView policyText;
    private ProgressBar progressBar;
    private String TAG = this.getClass().getSimpleName();
    String websiteUrl = "http://www.mallsyok.com";
    String policyUrl = "https://mallsyok.wordpress.com/2018/09/30/privacy-policy/";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init UI Handles
        activityText = (TextView) findViewById(R.id.activity_label);
        navigateBackBtn = (ImageButton) findViewById(R.id.icon_navigate_back);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        websiteText = (TextView) findViewById(R.id.website_label);
        policyText = (TextView) findViewById(R.id.policy_label);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        setupUI();

        // Set UI Handles
        activityText.setText(getResources().getString(R.string.MENU_OTHERS_ABOUT_US));
        navigateBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutActivity.this, OthersActivity.class));
            }
        });

        // Hide action bar
        getSupportActionBar().hide();

    }

    private void setupUI() {
        progressBar.setVisibility(View.GONE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        setWebsiteOnClicked();
        setPolicyOnClicked();
    }

    private void setWebsiteOnClicked(){
        websiteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBrowser(websiteUrl);
            }
        });
    }

    private void setPolicyOnClicked(){
        policyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBrowser(policyUrl);
            }
        });
    }

    private void launchBrowser(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public int getContentViewId() {
        return R.layout.about_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.others_menu;
    }
}
