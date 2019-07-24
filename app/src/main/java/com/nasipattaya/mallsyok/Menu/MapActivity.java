package com.nasipattaya.mallsyok.Menu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.DebugUtils;
import com.nasipattaya.mallsyok.Model.Mall;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;
import com.nasipattaya.mallsyok.TouchImageView;
import com.squareup.picasso.Picasso;

import static com.nasipattaya.mallsyok.DialogUtils.showDialogTips;

public class MapActivity extends BottomNavigationBar {

    private TextView textView;
    private TouchImageView touchImageView;
    private ProgressBar progressBar;

    private Bitmap map;

    private FirebaseFirestore db;
    private StorageReference storageReference;

    private String[] listMaps;
    private String defaultMapName = "1";
    private int selectedFloorMap = 1;

    private int maxTimeLoad = 8000;
    private CountDownTimer askUserWaitTimer;
    private Toast askUserWaitToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide action bar
        getSupportActionBar().hide();

        // Init UI
        textView = (TextView) findViewById(R.id.activity_label);
        touchImageView = (TouchImageView) findViewById(R.id.img);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        storageReference = FirebaseStorage.getInstance().getReference();

        setupUI();

        getListOfMaps();

    }

    private void setupUI(){
        textView.setText("Floor Map");
        touchImageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void updateUINoInfo(){
        touchImageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void updateUINotFound(){
        touchImageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        ToastUtils.toastLong(this, getResources().getString(R.string.ERR_NO_INFO));
    }

    private void updateUI(){
        touchImageView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void getImageURI(String mapName){
        String mallKey = getGlobalMallKey().toString();

        String imagePath = "FloorMap/"+mallKey+"/"+mapName+".jpg";
        askUserWait();
        storageReference.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(touchImageView, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess()
                    {
                        cancelAskUserWait();
                        getMap();
                        drawMap(map);
                        //addFloorAllButtons(listMaps);
                        updateUI();
                    }

                    @Override
                    public void onError(Exception e) {
                        DebugUtils.loggerError(MapActivity.this, "Floor map cannot be loaded");
                        updateUINotFound();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                DebugUtils.loggerError(MapActivity.this, "Floor map not found");
                updateUINotFound();
            }
        });
    }

    private void showMapTips(){
        String title = "Tips";
        String message = "1. Pinch out to zoom in.\n2. Pinch out to zoom out.\n3. Drag to move map around.";
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
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

    private void getImageURIDefault(String mapName){
        String mallKey = getGlobalMallKey().toString();

        String imagePath = "FloorMap/"+mallKey+"/"+mapName+".jpg";
        askUserWait();
        storageReference.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(touchImageView, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess()
                    {
                        cancelAskUserWait();
                        showMapTips();
                        getMap();
                        drawMap(map);
                        addFloorAllButtons(listMaps);
                        updateUI();
                    }

                    @Override
                    public void onError(Exception e) {
                        DebugUtils.loggerError(MapActivity.this, "Floor map cannot be loaded");
                        updateUINotFound();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                DebugUtils.loggerError(MapActivity.this, "Floor map not found");
                updateUINotFound();
            }
        });
    }

    private void getMap(){
        map = ((BitmapDrawable)touchImageView.getDrawable()).getBitmap();
    }


    private void getListOfMaps(){

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Mall mall = documentSnapshot.toObject(Mall.class);

                if (mall.getListMaps() != null){
                    listMaps = splitMaps(mall.getListMaps());
                    getImageURIDefault(defaultMapName);
                } else {
                    noInfoAvailable();
                    updateUI();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                noInfoAvailable();
                updateUI();
            }
        });
    }

    private void cancelAskUserWait(){
        if (askUserWaitTimer != null) {
            askUserWaitTimer.cancel();
        }
        if (askUserWaitToast != null) {
            askUserWaitToast.cancel();
        }
    }

    private void addFloorAllButtons(String[] listMaps){

        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int length = listMaps.length;
        double size = length + 0.5 * length + 0.5;

        int topMargin = (int)((screenHeight / 10) * 7.5) ;
        int btnWidth = (int)(screenWidth / size);

        for (int i = 0; i < listMaps.length; i++) {
            addFloorButton(i, listMaps[i].toString(), topMargin, btnWidth, (int)((0.5 + 1.5 * i) * btnWidth));
        }
    }

    private void addFloorButton(final int btnId, String btnLabel, int topMargin, int width, int leftMargin){
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_navigation_activity);

        RelativeLayout.LayoutParams relBtn = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        relBtn.topMargin = topMargin;
        relBtn.leftMargin = leftMargin;
        relBtn.width = width;

        Button floorBtn = new Button(this);
        floorBtn.setLayoutParams(relBtn);
        floorBtn.setText(btnLabel);
        floorBtn.setId(btnId);
        floorBtn.setShadowLayer(0,0,0,0);
        if (btnId == selectedFloorMap) {
            floorBtn.setBackgroundColor(getResources().getColor(R.color.white));
            floorBtn.setTextColor(getResources().getColor(R.color.icon_pink));
        } else {
            floorBtn.setBackgroundColor(Color.TRANSPARENT);
            floorBtn.setTextColor(0xFFEEEEEE);
        }

        //add button to the layout
        layout.addView(floorBtn);

        floorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFloorMap(btnId);
            }
        });
    }

    private void changeBtnBackground(int oldID, int newID){
        Button oldBtn = (Button) findViewById(oldID);
        Button newBtn = (Button) findViewById(newID);
        oldBtn.setBackgroundColor(Color.TRANSPARENT);
        oldBtn.setTextColor(0xFFEEEEEE);
        newBtn.setBackgroundColor(getResources().getColor(R.color.white));
        newBtn.setTextColor(getResources().getColor(R.color.icon_pink));
        selectedFloorMap = newID;
    }

    private void loadFloorMap(int id){
        progressBar.setVisibility(View.VISIBLE);
        touchImageView.setVisibility(View.GONE);
        changeBtnBackground(selectedFloorMap, id);
        getImageURI(listMaps[id]);
    }

    private void askUserWait(){
        // Time out after 5 seconds
        askUserWaitTimer = new CountDownTimer(maxTimeLoad / 4,1000){
            public void onTick(long millisUntilFinished) {
                //Log.i(TAG, "seconds remaining: " + millisUntilFinished / 1000);
                // You can monitor the progress here as well by changing the onTick() time
            }
            public void onFinish() {
                // stop async task if not in progress
                askUserWaitToast = Toast.makeText(MapActivity.this, "Sorry, this could take some time.", Toast.LENGTH_SHORT);
                askUserWaitToast.show();
            }
        };

        askUserWaitTimer.start();
    }

    private String[] splitMaps(String s){
        String [] arrayList = s.split(",");
        return arrayList;
    }

    private void noInfoAvailable(){
        ToastUtils.toastNoInfo(MapActivity.this);
        updateUINoInfo();
    }

    protected void drawMap(Bitmap map) {
        BitmapDrawable dr = new BitmapDrawable(map);
        touchImageView.setImageDrawable(dr);
    }

    @Override
    public int getContentViewId() {
        return R.layout.navigation_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.navigation_menu;
    }
}
