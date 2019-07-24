package com.nasipattaya.mallsyok.Others;

import android.content.Intent;
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

import java.util.Calendar;

public class OpeningActivity extends BottomNavigationBar {

    private TextView activityText;
    private ImageButton navigateBackBtn;
    private TextView openingLabel;
    private TextView openingText;
    private TextView openStatusText;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private int currentHour;
    private String TAG = this.getClass().getSimpleName();

    private boolean isMallOpen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init UI Handles
        activityText = (TextView) findViewById(R.id.activity_label);
        navigateBackBtn = (ImageButton) findViewById(R.id.icon_navigate_back);
        openingLabel = (TextView) findViewById(R.id.opening_hours_label);
        openingText = (TextView) findViewById(R.id.opening_hours_text);
        openStatusText = (TextView) findViewById(R.id.open_status_text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        setupUI();

        // Set UI Handles
        activityText.setText(getResources().getString(R.string.MENU_OTHERS_OPENING));
        navigateBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OpeningActivity.this, OthersActivity.class));
            }
        });

        // Hide action bar
        getSupportActionBar().hide();

        getOpeningHours();

    }

    private int getCurrentHour(){
        Calendar rightNow = Calendar.getInstance();

        // return in format 24hrs(00-23)
        return rightNow.get(Calendar.HOUR_OF_DAY);
    }

    private void setupUI() {
        progressBar.setVisibility(View.VISIBLE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        openingLabel.setVisibility(View.GONE);
        openingText.setVisibility(View.GONE);
        openStatusText.setVisibility(View.GONE);
    }

    private void updateUI(){
        progressBar.setVisibility(View.GONE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        if (!checkEmpty(openingText.getText().toString())){
            openingLabel.setVisibility(View.GONE);
            openingText.setVisibility(View.VISIBLE);
            openStatusText.setVisibility(View.VISIBLE);
            if (isMallOpen) {
                openStatusText.setText(R.string.MENU_OTHERS_OPENING_STATUS_OPEN);
            } else openStatusText.setText(R.string.MENU_OTHERS_OPENING_STATUS_CLOSE);
        }
    }

    private boolean checkEmpty(String text){
        if (TextUtils.isEmpty(text)){
            return true;
        }
        return false;
    }

    private void getOpeningHours(){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Mall mall = documentSnapshot.toObject(Mall.class);

                if (mall.getOpeningHours() != null){
                    openingLabel.setText("Time");
                    openingText.setText(mall.getOpeningHours());
                    currentHour = getCurrentHour();
                    isMallOpen =  getOpenStatus(mall.getOpeningHours(), currentHour);
                    updateUI();

                } else {
                    Log.i(TAG, "No information found:");
                    ToastUtils.toastLong(OpeningActivity.this, "No information found");
                    updateUI();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "No information found:");
                ToastUtils.toastLong(OpeningActivity.this, "No information found");
                updateUI();
            }
        });
    }

    private boolean getOpenStatus(String s, int currentHour){
        int am;
        int pm;
        String[] arrayString = s.split("AM");

        // parse string to int
        am = Integer.parseInt(arrayString[0]);

        String [] arrayString1 = arrayString[1].split(" ");
        String [] arrayString2 = arrayString1[2].split("PM");

        // parse string to int
        pm = Integer.parseInt(arrayString2[0]);

        // convert pm to 24hrs format
        pm = pm + 12;

        if (currentHour >= am && currentHour < pm) {
            return true;
        } else return false;
    }

    @Override
    public int getContentViewId() {
        return R.layout.opening_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.others_menu;
    }
}

