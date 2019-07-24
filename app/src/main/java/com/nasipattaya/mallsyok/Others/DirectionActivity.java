package com.nasipattaya.mallsyok.Others;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.Menu.OthersActivity;
import com.nasipattaya.mallsyok.Model.Mall;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;

public class DirectionActivity extends BottomNavigationBar {

    private TextView activityText;
    private ImageButton navigateBackBtn;
    private TextView railText;
    private TextView railLabel;
    private TextView serviceText;
    private TextView serviceLabel;
    private TextView busText;
    private TextView busLabel;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String TAG = this.getClass().getSimpleName();

    private String serviceURL;
    private String busURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init UI Handles
        activityText = (TextView) findViewById(R.id.activity_label);
        navigateBackBtn = (ImageButton) findViewById(R.id.icon_navigate_back);
        railText = (TextView) findViewById(R.id.direction_rail_text);
        railLabel = (TextView) findViewById(R.id.direction_rail_label);
        serviceText = (TextView) findViewById(R.id.direction_service_text);
        serviceLabel = (TextView) findViewById(R.id.direction_service_label);
        busText = (TextView) findViewById(R.id.direction_bus_text);
        busLabel = (TextView) findViewById(R.id.direction_bus_label);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        // Set UI Handles
        activityText.setText(getResources().getString(R.string.MENU_OTHERS_DIRECTION));
        navigateBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DirectionActivity.this, OthersActivity.class));
            }
        });

        setupUI();

        // Hide action bar
        getSupportActionBar().hide();

        getDirectioninfo();

    }

    private void setupUI(){
        progressBar.setVisibility(View.VISIBLE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        railText.setVisibility(View.GONE);
        railLabel.setVisibility(View.GONE);
        serviceText.setVisibility(View.GONE);
        serviceLabel.setVisibility(View.GONE);
        busText.setVisibility(View.GONE);
        busLabel.setVisibility(View.GONE);
    }

    private void updateUI(){
        progressBar.setVisibility(View.GONE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        if (!checkEmpty(railText.getText().toString())){
            railText.setVisibility(View.VISIBLE);
            railLabel.setVisibility(View.VISIBLE);
        }
        if (!checkEmpty(serviceText.getText().toString())) {
            serviceText.setVisibility(View.VISIBLE);
            serviceLabel.setVisibility(View.VISIBLE);
        }
        if (!checkEmpty(busText.getText().toString())) {
            busText.setVisibility(View.VISIBLE);
            busLabel.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkEmpty(String text){
        if (TextUtils.isEmpty(text)){
            return true;
        }
        return false;
    }

    private void setServiceOnClicked(){
        serviceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceURL = serviceText.getText().toString();
                launchBrowser(serviceURL);
            }
        });
    }

    private void setBusOnClicked(){
        busText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                busURL = busText.getText().toString();
                if (checkValidUrl(busURL)) {
                    launchBrowser(busURL);
                }
            }
        });
    }

    private void setWebsiteOnClicked(){
        setServiceOnClicked();
        setBusOnClicked();
    }

    private void launchBrowser(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private boolean checkValidUrl(String url) {
        String httpHeader = "http://";

        // If found string http:// at index 0 of whole url
        if(url.indexOf(httpHeader) == 0){
            return true;
        } else return false;
    }

    private void getDirectioninfo(){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Mall mall = documentSnapshot.toObject(Mall.class);

                if ((mall.getDirectionRail() != null) || (mall.getDirectionService() != null) || (mall.getDirectionBus() != null)) {
                    railText.setText(mall.getDirectionRail().replace("\\n", "\n"));
                    serviceText.setText(mall.getDirectionService());
                    busText.setText(mall.getDirectionBus().replace("\\n", "\n"));

                    updateUI();
                    setWebsiteOnClicked();

                } else {
                    Log.i(TAG, "No information found:");
                    ToastUtils.toastLong(DirectionActivity.this, "No information found");
                    updateUI();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "No information found:");
                ToastUtils.toastLong(DirectionActivity.this, "No information found");
                updateUI();
            }
        });
    }
    @Override
    public int getContentViewId() {
        return R.layout.direction_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.others_menu;
    }
}

