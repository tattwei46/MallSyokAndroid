package com.nasipattaya.mallsyok.Menu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
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
import com.nasipattaya.mallsyok.DebugUtils;
import com.nasipattaya.mallsyok.Model.Outlet;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;
import com.nasipattaya.mallsyok.TouchImageView;
import com.squareup.picasso.Picasso;

public class NavigationActivity extends BottomNavigationBar {

    private TextView textView;
    private TouchImageView touchImageView;
    private ProgressBar progressBar;

    private Bitmap map, marker;

    private FirebaseFirestore db;
    private StorageReference storageReference;

    private boolean getOutletInfoSuccess = false;

    private float left;
    private float top;
    private String floorNum;

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

        getOutletInfo();
    }

    private boolean hasExtras(){
        if (getIntent().hasExtra("floorNum") &&
                getIntent().hasExtra("left") &&
                getIntent().hasExtra("top")) {
            Bundle bundle = getIntent().getExtras();
            left = bundle.getFloat("left");
            top = bundle.getFloat("top");
            floorNum = bundle.getString("floorNum");
            return true;
        } else return false;
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

    private void getImageURI(){
        String mallKey = getGlobalMallKey().toString();
        String outletKey = getGlobalOutletKey().toString();

        String imagePath = "FloorMap/"+mallKey+"/"+floorNum+".jpg";
        storageReference.child(imagePath).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(touchImageView, new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess()
                    {
                        getMap();
                        getMarker();
                        drawMarkerOnMapRatio(map, marker, left, top);
                        updateUI();
                    }

                    @Override
                    public void onError(Exception e) {
                        DebugUtils.loggerError(NavigationActivity.this, "Floor map cannot be loaded");
                        updateUINotFound();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                DebugUtils.loggerError(NavigationActivity.this, "Floor map not found");
                updateUINotFound();
            }
        });

    }

    private void getMap(){
        map = ((BitmapDrawable)touchImageView.getDrawable()).getBitmap();
    }

    private void getMarker(){
        marker = BitmapFactory.decodeResource(this
                .getResources(), R.drawable.marker);

    }

    private void getOutletInfo(){

        if (hasExtras()) {
            getOutletInfoSuccess = false;
            db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("Malls").document(getGlobalMallKey()).collection("Outlets").document(getGlobalOutletKey());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Outlet outlet = documentSnapshot.toObject(Outlet.class);

                    if (TextUtils.isEmpty(outlet.getRoundLeft()) ||
                            TextUtils.isEmpty(outlet.getRoundTop()) ||
                            TextUtils.isEmpty(outlet.getFloorNumber())) {
                        noInfoAvailable();
                    } else {

                        left = Float.valueOf(outlet.getRoundLeft());
                        top = Float.valueOf(outlet.getRoundTop());
                        floorNum = outlet.getFloorNumber();

                        getImageURI();

                        getOutletInfoSuccess = true;
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    noInfoAvailable();
                }
            });
        } else {
            noInfoAvailable();
        }
    }

    private void noInfoAvailable(){
        ToastUtils.toastNoInfo(NavigationActivity.this);
        updateUINoInfo();
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // Create a matrix for manipulation
        Matrix matrix = new Matrix();
        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    protected void drawMarkerOnMapRatio(Bitmap map, Bitmap marker, double left, double top) {
        float mapWidth = map.getWidth();
        float mapHeight = map.getHeight();

        float markerWidth = marker.getWidth();
        float markerHeight = marker.getHeight();
        float newLeft = (float)(mapWidth * left - 0.5 * markerWidth);
        float newTop = (float)(mapHeight * top - markerHeight);

        drawMarkerOnMap(map, marker, newLeft, newTop);
    }


    protected void drawMarkerOnMap(Bitmap map, Bitmap marker, float left, float top) {

        Bitmap bmOverlay = Bitmap.createBitmap(map.getWidth(),
                map.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawARGB(0x00, 0, 0, 0);
        canvas.drawBitmap(map, 0, 0, null);
        canvas.drawBitmap(marker, left, top, null);

        BitmapDrawable dr = new BitmapDrawable(bmOverlay);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());

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
