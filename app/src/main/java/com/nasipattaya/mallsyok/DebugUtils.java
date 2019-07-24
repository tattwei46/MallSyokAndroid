package com.nasipattaya.mallsyok;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class DebugUtils extends AppCompatActivity{
    public static boolean DEBUG = true;

    public static void loggerDebug (Context ctx, String message){
        if (DEBUG) {
            Log.d(ctx.getClass().getSimpleName(), message);
        }
    }

    public static void loggerError(Context ctx, String message){
        if (DEBUG) {
            Log.e(ctx.getClass().getSimpleName(), message);
        }
    }
}
