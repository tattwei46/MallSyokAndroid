package com.nasipattaya.mallsyok;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class PublicID extends AppCompatActivity {

    private static String globalMallKey;
    private static String globalMallName;

    private static String globalOutletKey;
    private static String globalOutletName;

    // Getters and Setters for globalMall
    public static String getGlobalMallKey() {
        return globalMallKey;
    }

    public static void setGlobalMallKey(String key) {
        globalMallKey = key;
    }

    public static String getGlobalMallName() {
        return globalMallName;
    }

    public static void setGlobalMallName(String name) {
        globalMallName = name;
    }

    // Getters and Setters for globalOutlet
    public static String getGlobalOutletName() {
        return globalOutletName;
    }

    public static void setGlobalOutletName(String name) {
        globalOutletName = name;
    }

    public static String getGlobalOutletKey() {
        return globalOutletKey;
    }

    public static void setGlobalOutletKey(String key) {
        globalOutletKey = key;
    }


    //Getters and Setters for globalMallArrayList
    private static ArrayList<String> globalMallNameAL = new ArrayList<>();

    private static ArrayList<String> globalMallCoordinatesAL = new ArrayList<>();

    public static ArrayList<String> getGlobalMallCoordinatesAL() {
        return globalMallCoordinatesAL;
    }

    public static void setGlobalMallCoordinatesAL(ArrayList<String> arraylist) {
        globalMallCoordinatesAL = arraylist;
    }

    public static ArrayList<String> getGlobalMallNameAL() {
        return globalMallNameAL;
    }

    public static void setGlobalMallNameAL(ArrayList<String> arraylist) {
        globalMallNameAL = arraylist;
    }
}
