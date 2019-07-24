package com.nasipattaya.mallsyok.Others;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.Menu.MapActivity;
import com.nasipattaya.mallsyok.Menu.OthersActivity;
import com.nasipattaya.mallsyok.Model.Mall;
import com.nasipattaya.mallsyok.PhoneNumUtils;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;

public class ContactActivity extends BottomNavigationBar {

    private TextView activityText;
    private ImageButton navigateBackBtn;
    private TextView addressText;
    private TextView addressLabel;
    private TextView mobileText;
    private TextView mobileLabel;
    private TextView websiteText;
    private TextView websiteLabel;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String TAG = this.getClass().getSimpleName();

    private String websiteURL;
    private String phoneNumber;
    private String address;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init UI Handles
        activityText = (TextView) findViewById(R.id.activity_label);
        navigateBackBtn = (ImageButton) findViewById(R.id.icon_navigate_back);
        addressLabel = (TextView) findViewById(R.id.contact_address_label);
        addressText = (TextView) findViewById(R.id.contact_address_text);
        mobileLabel = (TextView) findViewById(R.id.contact_mobile_label);
        mobileText = (TextView) findViewById(R.id.contact_mobile_text);
        websiteText = (TextView) findViewById(R.id.contact_website_text);
        websiteLabel = (TextView) findViewById(R.id.contact_website_label);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        setupUI();

        // Set UI Handles
        activityText.setText(getResources().getString(R.string.MENU_OTHERS_CONTACT));
        navigateBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ContactActivity.this, OthersActivity.class));
            }
        });

        // Hide action bar
        getSupportActionBar().hide();

        getContactInfo();

    }

    private void setupUI(){
        progressBar.setVisibility(View.VISIBLE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        addressLabel.setVisibility(View.GONE);
        mobileLabel.setVisibility(View.GONE);
        websiteLabel.setVisibility(View.GONE);
        addressText.setVisibility(View.GONE);
        mobileText.setVisibility(View.GONE);
        mobileText.setVisibility(View.GONE);
    }

    private void updateUI(){
        progressBar.setVisibility(View.GONE);
        activityText.setVisibility(View.VISIBLE);
        navigateBackBtn.setVisibility(View.VISIBLE);
        if (!checkEmpty(addressText.getText().toString())){
            addressLabel.setVisibility(View.VISIBLE);
            addressText.setVisibility(View.VISIBLE);
        }
        if (!checkEmpty(mobileText.getText().toString())){
            mobileLabel.setVisibility(View.VISIBLE);
            mobileText.setVisibility(View.VISIBLE);
        }
        if (!checkEmpty(websiteText.getText().toString())) {
            websiteLabel.setVisibility(View.VISIBLE);
            websiteText.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkEmpty(String text){
        if (TextUtils.isEmpty(text)){
            return true;
        }
        return false;
    }

    private void setPhoneNumberOnClicked(){
        mobileText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = mobileText.getText().toString();
                if (PhoneNumUtils.checkPhoneNumberValid(phoneNumber)){
                    PhoneNumUtils.callPhoneNumber(phoneNumber, ContactActivity.this);
                }
            }
        });
    }

    private void setWebsiteOnClicked(){
        websiteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                websiteURL = websiteText.getText().toString();
                if (checkValidUrl(websiteURL)) {
                    launchBrowser(websiteURL);
                }
            }
        });
    }

    private void setAddressOnClicked(){
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address = addressText.getText().toString();
                launchMap(address);
            }
        });
    }

    private void launchBrowser(String url){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void launchMap(String address){
        Uri mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void setupUICallback(){
        setPhoneNumberOnClicked();
        setWebsiteOnClicked();
        setAddressOnClicked();
    }

    private void showContactTips(){
        String title = "Tips";
        String message = "1. Tap address to view in map\n2. Tap number to call\n3. Tap website to open browser";
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ContactActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.checkbox, null);
        CheckBox mCheckBox = mView.findViewById(R.id.skip);
        mBuilder.setTitle(title);
        mBuilder.setMessage(message);
        mBuilder.setView(mView);
        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    storeDialogStatus(true);
                }else{
                    storeDialogStatus(false);
                }
            }
        });

        if(getDialogStatus()){
            mDialog.hide();
        }else{
            mDialog.show();
        }
    }

    private boolean checkValidUrl(String url) {
        String httpHeader = "http://";

        // If found string http:// at index 0 of whole url
        if(url.indexOf(httpHeader) == 0){
            return true;
        } else return false;
    }

    private void storeDialogStatus(boolean isChecked){
        SharedPreferences mSharedPreferences = getSharedPreferences("CheckItem", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("item", isChecked);
        mEditor.apply();
    }

    private boolean getDialogStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("CheckItem", MODE_PRIVATE);
        return mSharedPreferences.getBoolean("item", false);
    }

    private void getContactInfo(){
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Mall mall = documentSnapshot.toObject(Mall.class);

                if ((mall.getMallAddress() != null) || (mall.getMallPhone() != null) || (mall.getMallWebsite() != null)){
                    addressText.setText(mall.getMallAddress().replace("\\n", "\n"));
                    mobileText.setText(mall.getMallPhone());
                    websiteText.setText(mall.getMallWebsite());

                    updateUI();
                    showContactTips();
                    setupUICallback();

                } else {
                    Log.i(TAG, "No information found:");
                    ToastUtils.toastLong(ContactActivity.this, "No information found");
                    updateUI();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "No information found:");
                ToastUtils.toastLong(ContactActivity.this, "No information found");
                updateUI();
            }
        });
    }

    @Override
    public int getContentViewId() {
        return R.layout.contact_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.others_menu;
    }
}

