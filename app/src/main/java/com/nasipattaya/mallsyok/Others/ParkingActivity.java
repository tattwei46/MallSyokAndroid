package com.nasipattaya.mallsyok.Others;

import android.content.Intent;
import android.nfc.Tag;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.Menu.OthersActivity;
import com.nasipattaya.mallsyok.Model.Mall;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;

public class ParkingActivity extends BottomNavigationBar {

    private TextView activityText;
    private ImageButton navigateBackBtn;
    private TextView weekdayText;
    private TextView weekdayLabel;
    private TextView weekendText;
    private TextView weekendLabel;
    private TextView lostText;
    private TextView lostLabel;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init UI Handles
        activityText = (TextView) findViewById(R.id.activity_label);
        navigateBackBtn = (ImageButton) findViewById(R.id.icon_navigate_back);
        weekdayText = (TextView) findViewById(R.id.parking_weekday_text);
        weekdayLabel = (TextView) findViewById(R.id.parking_weekday_label);
        weekendText = (TextView) findViewById(R.id.parking_weekend_text);
        weekendLabel = (TextView) findViewById(R.id.parking_weekend_label);
        lostText = (TextView) findViewById(R.id.parking_lost_text);
        lostLabel = (TextView) findViewById(R.id.parking_lost_label);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        // Set UI Handles
        activityText.setText(getResources().getString(R.string.MENU_OTHERS_PARKING));
        navigateBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ParkingActivity.this, OthersActivity.class));
            }
        });

        // Hide action bar
        getSupportActionBar().hide();

        setupUI();

        getParkinginfo();

    }

    private void setupUI(){
        progressBar.setVisibility(View.VISIBLE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        weekdayText.setVisibility(View.GONE);
        weekdayLabel.setVisibility(View.GONE);
        weekendText.setVisibility(View.GONE);
        weekendLabel.setVisibility(View.GONE);
        lostText.setVisibility(View.GONE);
        lostLabel.setVisibility(View.GONE);
    }

    private void updateUI(){
        progressBar.setVisibility(View.GONE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        if (!checkEmpty(weekdayText.getText().toString())){
            weekdayText.setVisibility(View.VISIBLE);
            weekdayLabel.setVisibility(View.VISIBLE);
        }
        if (!checkEmpty(weekendText.getText().toString())) {
            weekendText.setVisibility(View.VISIBLE);
            weekendLabel.setVisibility(View.VISIBLE);
        }
        if (!checkEmpty(lostText.getText().toString())) {
            lostText.setVisibility(View.VISIBLE);
            lostLabel.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkEmpty(String text){
        if (TextUtils.isEmpty(text)){
            return true;
        }
        return false;
    }

    private void getParkinginfo(){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Mall mall = documentSnapshot.toObject(Mall.class);

                if ((mall.getParkingWeekday() != null) || (mall.getParkingWeekend() != null) || (mall.getParkingLost() != null)) {
                    weekdayText.setText(mall.getParkingWeekday().replace("\\n", "\n"));
                    weekendText.setText(mall.getParkingWeekend().replace("\\n", "\n"));
                    lostText.setText(mall.getParkingLost());

                    updateUI();
                } else {
                    Log.i(TAG, "No information found:");
                    ToastUtils.toastLong(ParkingActivity.this, "No information found");
                    updateUI();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "No information found:");
                ToastUtils.toastLong(ParkingActivity.this, "No information found");
                updateUI();
            }
        });
    }

    @Override
    public int getContentViewId() {
        return R.layout.parking_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.others_menu;
    }
}

