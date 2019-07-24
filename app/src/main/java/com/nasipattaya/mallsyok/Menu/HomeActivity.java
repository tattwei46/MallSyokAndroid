package com.nasipattaya.mallsyok.Menu;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kobakei.ratethisapp.RateThisApp;
import com.nasipattaya.mallsyok.DebugUtils;
import com.nasipattaya.mallsyok.Directory.OutletListActivity;
import com.nasipattaya.mallsyok.OfflineUtils;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;
import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends LocationActivity {

    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems;
    private String[] items;
    private ListView listView;
    private EditText editText;
    private ProgressBar progressBar;
    private ImageView splashScreen, bgScreen, offlineIcon;
    private TextView selectMallTV, versionLabel;
    private Button locateBtn;
    private FirebaseFirestore db;

    private long lastClickList = 0;
    private long lastClickLocation = 0;
    private long clickBufferTime = 1000;

    private static ArrayList<String> mallNameArrayList;

    private static ArrayList<String> mallCoordinatesArrayList;

    private static ArrayList<Integer> nearestMallNameArrayList;

    private double latitude, longitude;

    private CountDownTimer timer;

    private int previousLength;

    public class MallLocations {
        double latitude;
        double longitude;
    }

    private TextView locationMethodLbl, locationMethodText, longitudeLbl, longitudeText, latitudeLbl, latitudeText;

    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init ArrayList
        mallNameArrayList = new ArrayList<String>();
        mallCoordinatesArrayList = new ArrayList<String>();
        nearestMallNameArrayList = new ArrayList<Integer>();

        setupLocation();

        setupUI();

        run();

    }

    private void finishGettingLocation(Location location){
        if (location!=null){
            showDebugLocation(location);
            findNearestMall(location);
        } else DebugUtils.loggerError(this, "Location is null");
    }

    private void showDebugLocation(Location location){
        if (location != null){
            //Todo Remove when location is stable
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            latitudeText.setText(Double.toString(latitude));
            longitudeText.setText(Double.toString(longitude));
        } else DebugUtils.loggerError(this, "Location is null");
    }

    private void run(){
        if (OfflineUtils.isNetworkAvailable(HomeActivity.this)){
            runOnline();
        } else {
            runOffline();
        }
    }

    private void launchAppRater(){
        // Monitor launch times and interval from installation
        RateThisApp.onCreate(this);

        RateThisApp.Config config = new RateThisApp.Config(5, 8);

        config.setTitle(R.string.APP_RATER_TITLE);
        config.setMessage(R.string.APP_RATER_MESSAGE);
        //config.setYesButtonText(R.string.my_own_rate);
        //config.setNoButtonText(R.string.my_own_thanks);
        //config.setCancelButtonText(R.string.my_own_cancel);

        //TODO: Change back review url to playstore
        //config.setUrl("https://play.google.com/store/apps/details?id=com.nasipattaya.mallsyok");
        config.setUrl("https://mallsyok.wordpress.com/getintouch/");

        // Initiate configuration
        RateThisApp.init(config);
        // If the condition is satisfied, "Rate this app" dialog will be shown
        RateThisApp.showRateDialogIfNeeded(HomeActivity.this);
    }

    private void getMallListFromFirebase() {

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Query a list of all malls from Firestore
        db.collection("Malls").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        mallNameArrayList.add(document.get("mallName").toString());
                        mallCoordinatesArrayList.add(document.get("mallCoordinates").toString());
                    }
                } else {
                    DebugUtils.loggerDebug(HomeActivity.this, "Error getting documents");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                setupMallListViewFromFirebase();
            }
        });
    }

    private void setupMallListViewFromFirebase(){
        // Copy list of all malls to local array string
        copyLocalToGlobalArrayList();
        setupMallListView();
    }

    private void copyLocalToGlobalArrayList(){
        setGlobalMallNameAL(mallNameArrayList);
        setGlobalMallCoordinatesAL(mallCoordinatesArrayList);
    }

    private void copyGlobalToLocalArrayList(){
        mallNameArrayList = getGlobalMallNameAL();
        mallCoordinatesArrayList = getGlobalMallCoordinatesAL();
    }

    private void copyLocalToWorkingArrayList(){
        if (mallNameArrayList.size() != 0) {

            items = new String[mallNameArrayList.size()];

            items = mallNameArrayList.toArray(items);
        }
    }

    private void searchMallName(String textToSearch) {
        for (String item:items){
            if (!item.toLowerCase().contains(textToSearch.toLowerCase())){
                listItems.remove(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private int getIndexFromLocalArrayList(String name){
        for (int i = 0; i < mallNameArrayList.size(); i++){
            if (mallNameArrayList.get(i).contains(name)){
                return i + 1;
            }
        }
        return 0;
    }

    private void resetMallListView(){

        listItems = new ArrayList<>(Arrays.asList(items));
        adapter = new ArrayAdapter<String>(this, R.layout.single_mall_row, R.id.textItem, listItems);
        listView.setAdapter(adapter);
    }

    private void setupUI(){

        listView = (ListView) findViewById(R.id.mall_search_lv);
        editText = (EditText) findViewById(R.id.mall_search_et);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        splashScreen = (ImageView) findViewById(R.id.splash_screen);
        bgScreen = (ImageView) findViewById(R.id.background);
        selectMallTV = (TextView) findViewById(R.id.select_mall_label);
        versionLabel = (TextView) findViewById(R.id.version_label);
        offlineIcon = (ImageView) findViewById(R.id.offline_icon);
        locateBtn = (Button) findViewById(R.id.locate_me_btn);

        locationMethodLbl = (TextView) findViewById(R.id.location_option_lbl);
        locationMethodText = (TextView) findViewById(R.id.location_option_text);
        longitudeLbl = (TextView) findViewById(R.id.longitude_lbl);
        longitudeText = (TextView) findViewById(R.id.longitude_text);
        latitudeLbl = (TextView) findViewById(R.id.latitude_label);
        latitudeText = (TextView) findViewById(R.id.latitude_text);

        locationMethodLbl.setVisibility(View.GONE);
        locationMethodText.setVisibility(View.GONE);
        longitudeLbl.setVisibility(View.GONE);
        longitudeText.setVisibility(View.GONE);
        latitudeLbl.setVisibility(View.GONE);
        latitudeText.setVisibility(View.GONE);

        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide action bar
        getSupportActionBar().hide();

        // Hide Select Mall Label
        selectMallTV.setVisibility(View.GONE);

        // Hide Offline screen
        offlineIcon.setVisibility(View.GONE);

        // Hide edit text
        editText.setFocusable(false);
        editText.setVisibility(View.GONE);

        // Hide list view
        listView.setVisibility(View.GONE);

        // Hide Progress bar
        progressBar.setVisibility(View.GONE);

        if (TextUtils.isEmpty(getGlobalMallKey())) {
            // Show splash screen
            splashScreen.setVisibility(View.VISIBLE);
        } else {
            splashScreen.setVisibility(View.GONE);
        }

        // Hide background screen
        bgScreen.setVisibility(View.GONE);

        // Show version label
        versionLabel.setVisibility(View.VISIBLE);

        // Hide locate button
        locateBtn.setVisibility(View.GONE);

        hideNavigationBar();

        setVersionLabel();
    }

    private void updateUI(){

        //Set color of window background to similar background to prevent white flashing
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.icon_blue));

        // Show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide action bar
        getSupportActionBar().hide();

        // Show list view
        listView.setVisibility(View.VISIBLE);
        listView.setFastScrollEnabled(true);

        // Show Select Mall Label
        selectMallTV.setVisibility(View.VISIBLE);

        // Show edit text
        editText.setFocusableInTouchMode(true);
        editText.setVisibility(View.VISIBLE);

        // Hide Progress bar
        progressBar.setVisibility(View.GONE);

        // Hide splashscreen
        splashScreen.setVisibility(View.GONE);

        // Show background screen
        bgScreen.setVisibility(View.VISIBLE);

        // Hide version label
        versionLabel.setVisibility(View.GONE);

        //Todo Show locate button for release v1.40
        // Show locate button
        locateBtn.setVisibility(View.VISIBLE);

        showNavigationBar();
    }


    private void runOffline(){

        setUIOffline();

        // Set offline icon onclick
        setOfflineIconOnClick();

        ToastUtils.toastOffline(this);

    }

    private void setUIOffline(){
        //Set color of window background to similar background to prevent white flashing
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.icon_blue));

        // Show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide action bar
        getSupportActionBar().hide();

        // Hide Progress bar
        progressBar.setVisibility(View.GONE);

        // Hide splashscreen
        splashScreen.setVisibility(View.GONE);

        // Show background screen
        bgScreen.setVisibility(View.VISIBLE);

        // Hide version label
        versionLabel.setVisibility(View.GONE);

        // Show Offline screen
        offlineIcon.setVisibility(View.VISIBLE);

        hideNavigationBar();
    }

    private void setOfflineIconOnClick(){
        offlineIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OfflineUtils.rotateOfflineIcon(HomeActivity.this, offlineIcon);
                OfflineUtils.startOfflineTimer(HomeActivity.this);
            }
        });
    }

    private void startOutletListActivity(){

        Intent outletListActivity = new Intent(HomeActivity.this, OutletListActivity.class);

        startActivity(outletListActivity);
    }

    private void setVersionLabel(){
        versionLabel.setText(R.string.VERSION_LABEL);
    }

    private void hideNavBarBehindKeyboard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void runOnline(){

        launchAppRater();

        items = new String[]{};

        hideNavBarBehindKeyboard();

        //Query list of malls at start
        if (getGlobalMallNameAL().isEmpty()) {
            getMallListFromFirebase();
        } else {
            getMallListFromLocal();
        }

        setupUICallback();

    }

    private void getMallListFromLocal(){
        copyGlobalToLocalArrayList();
        setupMallListView();
    }

    private void setupMallListView(){
        copyLocalToWorkingArrayList();
        resetMallListView();
        updateUI();
    }

    private void setupUICallback(){
        setLocateBtnOnClick();
        setEditTextAddTextChange();
        setListViewSetOnItemClick();
    }

    private void setListViewSetOnItemClick(){
        // Click from mall list to start OutletListActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Prevent multiple tap to trigger multiple intents
                if (SystemClock.elapsedRealtime() - lastClickList < clickBufferTime){
                    return;
                }

                lastClickList = SystemClock.elapsedRealtime();

                // Get the item that user taps
                setGlobalMallName(listView.getItemAtPosition(i).toString());

                int mallIndex = getIndexFromLocalArrayList(getGlobalMallName());

                if (mallIndex != 0){
                    // Build mallKey from listview position
                    setGlobalMallKey("M" + String.format("%03d", mallIndex));
                    startOutletListActivity();
                }

            }
        });
    }

    private void setEditTextAddTextChange(){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousLength = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                listView.setVisibility(View.VISIBLE);

                if (charSequence.toString().equals("")) {
                    resetMallListView();
                } else {
                    searchMallName(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (previousLength > editable.length()){
                    // Backspace key is detected
                    resetMallListView();
                    searchMallName(editable.toString());
                }
            }
        });
    }

    private void startLocationTimer(){
        timer = new CountDownTimer(1000,1000){
            public void onTick(long millisUntilFinished) {
                // You can monitor the progress here as well by changing the onTick() time
            }
            public void onFinish() {
                mCurrentLocation = getCurrentLocation();
                if (mCurrentLocation != null) {
                    stopLocationRequest();
                    finishGettingLocation(mCurrentLocation);
                } else {
                    progressBar.setVisibility(View.GONE);
                    ToastUtils.toastLong(HomeActivity.this, "Unable to get location");
                }
            }
        };

        timer.start();
    }

    private void setLocateBtnOnClick(){
        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Prevent multiple tap to trigger multiple intents
                if (SystemClock.elapsedRealtime() - lastClickLocation < clickBufferTime){
                    return;
                }

                lastClickLocation = SystemClock.elapsedRealtime();

                if(checkLocationPermission()){
                    hideUIWhileGettingLocation();
                    startLocationRequest();
                    startLocationTimer();
                }
            }
        });
    }

    private void hideUIWhileGettingLocation(){

        // Hide Select Mall Label
        selectMallTV.setVisibility(View.GONE);

        // Hide Offline screen
        offlineIcon.setVisibility(View.GONE);

        // Hide edit text
        editText.setFocusable(false);
        editText.setVisibility(View.GONE);

        // Hide list view
        listView.setVisibility(View.GONE);

        // Show Progress bar
        progressBar.setVisibility(View.VISIBLE);

        splashScreen.setVisibility(View.GONE);

        // Hide background screen
        bgScreen.setVisibility(View.VISIBLE);

        // Show version label
        versionLabel.setVisibility(View.GONE);

        // Hide locate button
        locateBtn.setVisibility(View.GONE);
    }

    private void showUIAfterGettingLocation(){

        //Todo Remove when location is stable
//        locationMethodLbl.setVisibility(View.VISIBLE);
//        locationMethodText.setVisibility(View.VISIBLE);
//        longitudeLbl.setVisibility(View.VISIBLE);
//        longitudeText.setVisibility(View.VISIBLE);
//        latitudeLbl.setVisibility(View.VISIBLE);
//        latitudeText.setVisibility(View.VISIBLE);

        // Show Select Mall Label
        selectMallTV.setVisibility(View.VISIBLE);

        // Hide Offline screen
        offlineIcon.setVisibility(View.GONE);

        // Show edit text
        editText.setFocusable(true);
        editText.setVisibility(View.VISIBLE);

        // Hide list view
        listView.setVisibility(View.VISIBLE);

        // Show Progress bar
        progressBar.setVisibility(View.GONE);

        splashScreen.setVisibility(View.GONE);

        // Hide background screen
        bgScreen.setVisibility(View.VISIBLE);

        // Show version label
        versionLabel.setVisibility(View.GONE);

        // Hide locate button
        locateBtn.setVisibility(View.VISIBLE);
    }

    private void startOutletListActivityFromLocation(int i){
        // Get the item that user taps
        setGlobalMallName(listView.getItemAtPosition(i).toString());

        // Build mallKey from listview position
        setGlobalMallKey("M" + String.format("%03d", (i + 1)));

        startOutletListActivity();
    }

    private float calculateDistance(double src_latitude, double src_longitude, double dest_latitude, double dest_longitude){
        float[] distance = new float[10];
        Location.distanceBetween(src_latitude, src_longitude, dest_latitude, dest_longitude, distance);
        return distance[0];
    }

    private boolean checkWithinMallRadius(double src_latitude, double src_longtitude, double dest_latitude, double dest_longitude){
        float radiusScope = 250;
        if (calculateDistance(src_latitude, src_longtitude, dest_latitude, dest_longitude) < radiusScope){
            return true;
        } else return false;
    }

    private MallLocations splitCoordinateIntoLatLong(String coordinate){
        MallLocations mallLocations = new MallLocations();
        if (coordinate.contains(",")){

            //Removes space at the start and end of string
            coordinate = coordinate.trim();

            //Remove space in middle of string
            coordinate = coordinate.replace(" ", "");

            // Split string with one comma, into 2 strings
            String[] splitCoordinate = coordinate.split(",");
            mallLocations.latitude = Double.parseDouble(splitCoordinate[0]);
            mallLocations.longitude = Double.parseDouble(splitCoordinate[1]);
        }
        return mallLocations;
    }

    private void findNearestMall(Location location){
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        MallLocations mallLocations;

        nearestMallNameArrayList.clear();

        for (int i = 0; i < mallCoordinatesArrayList.size(); i++){
            mallLocations = splitCoordinateIntoLatLong(mallCoordinatesArrayList.get(i));
            if (checkWithinMallRadius(latitude, longitude, mallLocations.latitude, mallLocations.longitude)){
                nearestMallNameArrayList.add(i);
            }
        }

        if (nearestMallNameArrayList.size() > 1){
            getNearestMallList();
        } else if (nearestMallNameArrayList.size() == 1){
            //Assuming index 0 is the only nearest mall
            startOutletListActivityFromLocation(nearestMallNameArrayList.get(0));
        } else if (nearestMallNameArrayList.isEmpty()){
            ToastUtils.toastLong(this, "No nearest mall in vicinity. Please search manually");
        }

        showUIAfterGettingLocation();
    }

    private void getNearestMallList(){
        ArrayList<String> newListItem = new ArrayList<String>();
        for (int i = 0; i< nearestMallNameArrayList.size(); i++){
            newListItem.add(listItems.get(nearestMallNameArrayList.get(i)));
        }

        listItems.clear();
        listItems.addAll(newListItem);
        adapter = new ArrayAdapter<String>(this, R.layout.single_mall_row, R.id.textItem, listItems);
        listView.setAdapter(adapter);
        ToastUtils.toastLong(this, "You have " + nearestMallNameArrayList.size() + " malls near you");
    }

    @Override
    public int getContentViewId() {
        return R.layout.home_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.home_menu;
    }


}
