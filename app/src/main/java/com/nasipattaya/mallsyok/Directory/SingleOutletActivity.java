package com.nasipattaya.mallsyok.Directory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.Menu.NavigationActivity;
import com.nasipattaya.mallsyok.Model.Outlet;
import com.nasipattaya.mallsyok.PhoneNumUtils;
import com.nasipattaya.mallsyok.OfflineUtils;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;
import com.squareup.picasso.Picasso;

/**
 * Created by davidcheah on 2/2/18.
 */

public class SingleOutletActivity extends BottomNavigationBar {

    private FirebaseFirestore db;
    private StorageReference storageReference;

    private String TAG = "info";
    private String phoneNumber = null;

    private TextView outletNameTV;
    private TextView outletCategoryTV;
    private TextView outletFloorNumTV;
    private TextView outletUnitNumTV;
    private TextView outletContactNumTV;

    private TextView outletCategoryLabel;
    private TextView outletFloorNumLabel;
    private TextView outletUnitNumLabel;
    private TextView outletContactNumLabel;

    private ImageView offlineIcon;
    private ImageView outletImage;

    private ProgressBar progressBar;

    private float left;
    private float top;
    private String floorNum;

    private boolean getOutletInfoSuccess = false;

    private boolean getOutletMapSuccess = false;

    private boolean isFeatureReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        offlineIcon = (ImageView) findViewById(R.id.offline_icon);

        outletImage = (ImageView) findViewById(R.id.outlet_image);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        outletNameTV = (TextView) findViewById(R.id.single_outlet_name);
        outletCategoryTV = (TextView) findViewById(R.id.single_category);
        outletFloorNumTV = (TextView) findViewById(R.id.single_floor_number);
        outletUnitNumTV = (TextView) findViewById(R.id.single_unit_number);
        outletContactNumTV = (TextView) findViewById(R.id.single_contact_number);

        outletCategoryLabel = (TextView) findViewById(R.id.single_category_label);
        outletFloorNumLabel = (TextView) findViewById(R.id.single_floor_number_label);
        outletUnitNumLabel = (TextView) findViewById(R.id.single_unit_number_label);
        outletContactNumLabel = (TextView) findViewById(R.id.single_contact_number_label);


        storageReference = FirebaseStorage.getInstance().getReference();

        // Hide action bar
        getSupportActionBar().hide();

        setupUI();

        run();

    }

    private void run(){
        if (OfflineUtils.isNetworkAvailable(SingleOutletActivity.this)){
            runOnline();
        } else {
            runOffline();
        }
    }

    private void runOnline(){
        if (TextUtils.isEmpty(getGlobalMallKey()) || TextUtils.isEmpty(getGlobalMallName())
                || TextUtils.isEmpty(getGlobalOutletKey()) || TextUtils.isEmpty(getGlobalMallName())){
            runOffline();
        } else {
            getData();
            setupUICallback();
        }
    }

    private void getData(){
        loadImageFromStore();
        getOutletInfo();
    }

    private void setupUICallback(){
        // Set phone number on clicked
        setPhoneNumberOnClicked();
    }

    private void loadImageFromStore(){
        String imagePath = "OutletImage/"+getGlobalMallKey()+"/"+getGlobalOutletKey()+".jpg";
        storageReference.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(outletImage, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {
                        updateUI();
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                updateUI();
                outletImage.setVisibility(View.GONE);
            }
        });

    }

    private void updateUI(){
        setOutletName();

        //Label
        outletCategoryLabel.setVisibility(View.VISIBLE);
        outletFloorNumLabel.setVisibility(View.VISIBLE);
        outletUnitNumLabel.setVisibility(View.VISIBLE);
        outletContactNumLabel.setVisibility(View.VISIBLE);

        //TextView
        outletNameTV.setVisibility(View.VISIBLE);
        outletCategoryTV.setVisibility(View.VISIBLE);
        outletFloorNumTV.setVisibility(View.VISIBLE);
        outletUnitNumTV.setVisibility(View.VISIBLE);
        outletContactNumTV.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.GONE);
        offlineIcon.setVisibility(View.GONE);

        outletImage.setVisibility(View.VISIBLE);

        showNavigationBar();
    }

    private void runOffline(){

        setupOfflineUI();

        setOfflineIconOnClick();

        ToastUtils.toastOffline(this);
    }

    private void setupOfflineUI() {
        //Label
        outletCategoryLabel.setVisibility(View.GONE);
        outletFloorNumLabel.setVisibility(View.GONE);
        outletUnitNumLabel.setVisibility(View.GONE);
        outletContactNumLabel.setVisibility(View.GONE);

        //TextView
        outletNameTV.setVisibility(View.GONE);
        outletCategoryTV.setVisibility(View.GONE);
        outletFloorNumTV.setVisibility(View.GONE);
        outletUnitNumTV.setVisibility(View.GONE);
        outletContactNumTV.setVisibility(View.GONE);

        offlineIcon.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        hideNavigationBar();
    }

    private void setupUI(){
        //Label
        outletCategoryLabel.setVisibility(View.GONE);
        outletFloorNumLabel.setVisibility(View.GONE);
        outletUnitNumLabel.setVisibility(View.GONE);
        outletContactNumLabel.setVisibility(View.GONE);

        //TextView
        outletNameTV.setVisibility(View.GONE);
        outletCategoryTV.setVisibility(View.GONE);
        outletFloorNumTV.setVisibility(View.GONE);
        outletUnitNumTV.setVisibility(View.GONE);
        outletContactNumTV.setVisibility(View.GONE);

        offlineIcon.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        showNavigationBar();
    }

    private void setPhoneNumberOnClicked(){
        outletContactNumTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                phoneNumber = outletContactNumTV.getText().toString();
                if (PhoneNumUtils.checkPhoneNumberValid(phoneNumber)){
                    PhoneNumUtils.callPhoneNumber(phoneNumber, SingleOutletActivity.this);
                }
            }
        });
    }

    private void setOutletName(){
        if (!(TextUtils.isEmpty(getGlobalMallKey()))) {
            setTitle(getGlobalOutletName());
        }
    }

    private void setOfflineIconOnClick(){
        offlineIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OfflineUtils.rotateOfflineIcon(SingleOutletActivity.this, offlineIcon);
                OfflineUtils.startOfflineTimer(SingleOutletActivity.this);
            }
        });
    }

    private void getOutletInfo(){
        getOutletInfoSuccess = false;
        getOutletMapSuccess = false;
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey()).collection("Outlets").document(getGlobalOutletKey());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //progressBar.setVisibility(View.GONE);
                Outlet outlet = documentSnapshot.toObject(Outlet.class);

                outletNameTV.setText(outlet.getOutletName());
                outletCategoryTV.setText(outlet.getCategory());
                outletFloorNumTV.setText((outlet.getFloorNumber()));
                outletUnitNumTV.setText(outlet.getUnitNumber());
                outletContactNumTV.setText(outlet.getContactNumber());

                if (isFeatureReady) {

                    if (TextUtils.isEmpty(outlet.getRoundLeft()) ||
                            TextUtils.isEmpty(outlet.getRoundTop()) ||
                            TextUtils.isEmpty(outlet.getFloorNumber())) {
                        getOutletMapSuccess = false;
                    } else {
                        getOutletMapSuccess = true;

                        left = Float.valueOf(outlet.getRoundLeft());
                        top = Float.valueOf(outlet.getRoundTop());
                        floorNum = outlet.getFloorNumber();

                        outletUnitNumLabel.append(" (" + getResources().getString(R.string.MSG_TAP_OUTLET_LOCATION) + " )");

                        setUnitNumberOnClick();

                    }

                    getOutletInfoSuccess = true;

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                runOffline();
            }
        });
    }

    private void setUnitNumberOnClick(){
        outletUnitNumTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleOutletActivity.this, NavigationActivity.class);
                intent.putExtra("floorNum", floorNum);
                intent.putExtra("left", left);
                intent.putExtra("top", top);
                startActivity(intent);
            }
        });
    }

    @Override
    public int getContentViewId() {
        return R.layout.single_outlet_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.directory_menu;
    }
}
