package com.nasipattaya.mallsyok;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class OfflineUtils extends Activity {

    public static String TAG = OfflineUtils.class.getSimpleName();

    private static CountDownTimer timer;

    public static boolean isNetworkAvailable(Context context){

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Log.v(TAG, "Internet Connection Not Present");
            return false;
        }
    }

    public static void rotateOfflineIcon(Context context, ImageView imageView){
        imageView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate));
    }

    public static void startOfflineTimer(final Context context){
        timer = new CountDownTimer(1300,1000){
            public void onTick(long millisUntilFinished) {
                // You can monitor the progress here as well by changing the onTick() time
            }
            public void onFinish() {
                // stop async task if not in progress
                if (isNetworkAvailable(context)){
                    Intent intent = ((Activity)context).getIntent();
                    ((Activity) context).finish();
                    context.startActivity(intent);
                }
            }
        };

        timer.start();
    }
}
