package com.nasipattaya.mallsyok;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ToastUtils extends Activity{

    public static void toastShort(Context context, String text){
        Toast.makeText(context, text,
                Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context context, String text){
        Toast.makeText(context, text,
                Toast.LENGTH_LONG).show();
    }

    public static void toastNoMall(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.ERR_NO_MALL),
                Toast.LENGTH_LONG).show();
    }

    public static void toastNoOutlet(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.ERR_NO_OUTLET),
                Toast.LENGTH_LONG).show();
    }

    public static void toastOffline(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.ERR_OFFLINE),
                Toast.LENGTH_LONG).show();
    }

    public static void toastNoInfo(Context context) {
        Toast.makeText(context, context.getResources().getString(R.string.ERR_NO_INFO),
                Toast.LENGTH_LONG).show();
    }
}
