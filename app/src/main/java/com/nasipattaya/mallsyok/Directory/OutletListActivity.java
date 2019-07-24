package com.nasipattaya.mallsyok.Directory;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nasipattaya.mallsyok.BottomNavigationBar;
import com.nasipattaya.mallsyok.OfflineUtils;
import com.nasipattaya.mallsyok.R;
import com.nasipattaya.mallsyok.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by davidcheah on 16/2/18.
 */

public class OutletListActivity extends BottomNavigationBar {

    private String TAG = "info";

    private FirebaseFirestore db;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems;
    private ArrayList<String> outletArrayList;
    private ArrayList<String> outletFullList;

    private ListView listView;

    private EditText editText;
    private Spinner spinner;
    private TextView mallLabel;
    private ImageView offlineIcon;
    private ProgressBar progressBar;
    private Toast toast, askUserWaitToast;
    private CountDownTimer timer, askUserWaitTimer;

    private String mallKey = null;
    private String outletKey = null;
    private String outletName = null;
    private String categoryKey = null;
    private String[] items;

    private long lastClickTime = 0;
    private long clickBufferTime = 1000;

    private long maxTimeLoad = 8000;

    private int previousLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        outletArrayList = new ArrayList<String>();
        outletFullList = new ArrayList<String>();

        items = new String[]{};

        setupUI();

        run();
    }

    private void run(){
        if (OfflineUtils.isNetworkAvailable(OutletListActivity.this)){
            runOnline();
        } else {
            runOffline();
        }
    }

    private void runOnline(){
        if (TextUtils.isEmpty(getGlobalMallName()) || TextUtils.isEmpty(getGlobalMallKey())){
            runOffline();
        } else {
            showNavigationBar();
            setMallName();
            initializeSpinner();
            getOutletList();
            setUICallback();
        }
    }

    private void setUICallback(){
        setSpinnerOnClicked();
        setEditTextOnTextChanged();
        setListViewOnItemClicked();
    }

    private void runOffline(){
        setOfflineUI();
        setOfflineIconOnClick();
        ToastUtils.toastOffline(this);
    }

    private void setOfflineUI(){
        offlineIcon.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        hideNavigationBar();
    }

    private void setOfflineIconOnClick(){
        offlineIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OfflineUtils.rotateOfflineIcon(OutletListActivity.this, offlineIcon);
                OfflineUtils.startOfflineTimer(OutletListActivity.this);
            }
        });
    }

    private void setListViewOnItemClicked(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Prevent multiple tap to trigger multiple intents
                if (SystemClock.elapsedRealtime() - lastClickTime < clickBufferTime){
                    return;
                }

                lastClickTime = SystemClock.elapsedRealtime();

                // Get the item that user taps
                outletName = listView.getItemAtPosition(i).toString();

                setGlobalOutletName(outletName);

                // Build outletKey based on listview position and mallKey
                outletKey = getGlobalMallKey().toUpperCase() + "S" + String.format("%03d", (getOutletKeyFromOutletName(outletName) + 1));

                setGlobalOutletKey(outletKey);
                startSingleOutletActivity();
            }
        });
    }

    private void hideNavBarBehindKeyboard(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void setEditTextOnTextChanged(){

        hideNavBarBehindKeyboard();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                previousLength = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (charSequence.toString().equals("")) {
                    resetOutletListView();
                    spinner.setEnabled(true);
                    //spinner.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                } else {

                    listView.setVisibility(View.VISIBLE);
                    spinner.setEnabled(false);
                    //spinner.setBackgroundColor(getResources().getColor(R.color.spinnerDisable));

                    // Perform searchOutletName on user key
                    searchOutletName(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (previousLength > editable.length()){
                    // Backspace key is detected
                    resetOutletListView();
                    searchOutletName(editable.toString());
                }
            }
        });
    }

    // Sort list in ascending order
    private void sortOutletList(){
        Collections.sort(outletArrayList);
    }

    private void setSpinnerOnClicked(){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // Cancel toast if is shown
                if(toast != null) {
                    toast.cancel();
                }

                // Make sure we do not reload the list at the start of the activity
                if (categoryKey == null){
                    if (i > 0) {
                        categoryKey = adapterView.getItemAtPosition(i).toString();
                        reloadList();
                    }
                }

                // Make sure we only reload the list if the selected is different from previous category
                if ((categoryKey != null) && (!(categoryKey.equals(adapterView.getItemAtPosition(i).toString())))){
                    categoryKey = adapterView.getItemAtPosition(i).toString();
                    reloadList();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void setMallName(){

        if (TextUtils.isEmpty(getGlobalMallName())){
            ToastUtils.toastNoMall(this);
        } else {

            // Grab mall name and set as title
            mallLabel.setText(getGlobalMallName());
        }
    }

    private void initializeSpinner(){
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this, R.array.outlet_categories,
                R.layout.custom_spinner);

        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinAdapter);

        // Set the spinner at selected category on next screen
        if (categoryKey != null) {
            int spinnerPosition = spinAdapter.getPosition(categoryKey);
            spinner.setSelection(spinnerPosition);
        }
    }

    private void startSingleOutletActivity(){

        Intent singleOutletActivity = new Intent(OutletListActivity.this, SingleOutletActivity.class);

        startActivity(singleOutletActivity);
    }

    private void hideKeyboard(){

        // Suppress soft keyboard until user explicitly touches editText view
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void startTimer(){
        askUserWait();
        // Time out after 5 seconds
        timer = new CountDownTimer(maxTimeLoad,1000){
            public void onTick(long millisUntilFinished) {
                //Log.i(TAG, "seconds remaining: " + millisUntilFinished / 1000);
                // You can monitor the progress here as well by changing the onTick() time
            }
            public void onFinish() {
                // stop async task if not in progress
                if (progressBar.isShown()) {
                    progressBar.setVisibility(View.GONE);

                    ToastUtils.toastNoOutlet(OutletListActivity.this);
                }
            }
        };

        timer.start();
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
                askUserWaitToast = Toast.makeText(OutletListActivity.this, "Sorry, this could take a while.", Toast.LENGTH_SHORT);
                askUserWaitToast.show();
            }
        };

        askUserWaitTimer.start();
    }

    private void cancelAskUserWait(){
        if (askUserWaitTimer != null) {
            askUserWaitTimer.cancel();
        }
        if (askUserWaitToast != null) {
            askUserWaitToast.cancel();
        }
    }

    private void stopTimer(){
        timer.cancel();
    }

    private void getOutletList() {
        db = FirebaseFirestore.getInstance();
        clearArray();
        hideKeyboard();

        editText.setFocusable(false);
        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        startTimer();

        if (categoryKey != null){
            if (categoryKey.equals("All categories")){
                getAllOutletList();
                sortOutletList();
            } else {
                getCategorizedOutletList();
            }
        } else {
            getAllOutletList();
        }
    }

    private int getOutletKeyFromOutletName(String outletName){
        return outletFullList.indexOf(outletName);
    }

    private void getAllOutletList() {

        // Query a list of all malls from Firestore
        db.collection("Malls").document(getGlobalMallKey()).collection("Outlets").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        outletArrayList.add(document.get("outletName").toString());
                        outletFullList.add(document.get("outletName").toString());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {

                cancelAskUserWait();
                setupOutletListView();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting documents: ", e);
            }
        });
    }

    private void getCategorizedOutletList(){

        // Query a list of all malls from Firestore
        db.collection("Malls").document(getGlobalMallKey()).collection("Outlets").whereEqualTo("category", categoryKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        outletArrayList.add(document.get("outletName").toString());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                //cancelAskUserWait();
                setupOutletListView();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error getting documents: ", e);
            }
        });
    }

    private void setupOutletListView(){

        if (outletArrayList.size() != 0) {
            // Copy list of all malls to local array string
            sortOutletList();
            stopTimer();
            copyListToLocal();
            resetOutletListView();
            updateUI();
        }
    }
    private void reloadList(){
        getOutletList();
    }

    private void clearArray(){
        outletArrayList.clear();
    }

    private void copyListToLocal(){
        if (outletArrayList.size() != 0) {

            items = new String[outletArrayList.size()];

            items = outletArrayList.toArray(items);
        }
    }

    private void searchOutletName(String textToSearch) {
        for (String item:items){
            if (!item.toLowerCase().contains(textToSearch.toLowerCase())){
                listItems.remove(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void resetOutletListView(){

        listItems = new ArrayList<>(Arrays.asList(items));
        adapter = new ArrayAdapter<String>(this, R.layout.single_outlet_row, R.id.textItem, listItems);
        listView.setAdapter(adapter);
    }

    private void setupUI(){

        listView = (ListView) findViewById(R.id.outlet_search_lv);
        editText = (EditText) findViewById(R.id.outlet_search_et);
        spinner = (Spinner) findViewById(R.id.category_spinner);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mallLabel = (TextView) findViewById(R.id.mall_name_label);
        offlineIcon = (ImageView) findViewById(R.id.offline_icon);

        // Hide action bar
        getSupportActionBar().hide();

        // Hide mall name label
        mallLabel.setVisibility(View.GONE);

        // Hide offline icon
        offlineIcon.setVisibility(View.GONE);

        // Hide Search for outlet
        editText.setFocusable(false);
        editText.setVisibility(View.GONE);

        // Hide Select Category
        spinner.setVisibility(View.GONE);

        // Show Progress bar
        progressBar.setVisibility(View.VISIBLE);

    }

    private void updateUI(){

        // Hide action bar
        getSupportActionBar().hide();

        // Show mall name label
        mallLabel.setVisibility(View.VISIBLE);

        // Hide offline icon
        offlineIcon.setVisibility(View.GONE);

        // Show Search for outlet
        editText.setFocusableInTouchMode(true);
        editText.setVisibility(View.VISIBLE);

        // Show List View
        listView.setVisibility(View.VISIBLE);

        // Enable fast scroll of list view
        listView.setFastScrollEnabled(true);

        // Show Select Category
        spinner.setVisibility(View.VISIBLE);

        // Hide Progress bar
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public int getContentViewId() {
        return R.layout.outlet_list_activity;
    }

    @Override
    public int getNavigationMenuItemId() {
        return R.id.directory_menu;
    }
}
