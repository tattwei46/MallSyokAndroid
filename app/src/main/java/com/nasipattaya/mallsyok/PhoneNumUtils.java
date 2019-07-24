package com.nasipattaya.mallsyok;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PhoneNumUtils extends Activity{

    public static void callPhoneNumber(String phoneNumber, Context context){
        String dial = "tel:" + phoneNumber;
        // Use ACTION_DIAL to show phone manager with number. User then need to press call
        // Use ACTION_CALL to directly start making call. User no need to press call
        // For ACTION_CALL to work, need to add permission android.permission.CALL_PHONE in the AndroidManifest.xml
        context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
    }

    public static boolean checkPhoneNumberValid(String phoneNumber){
        if ((phoneNumber.contains("+60")||phoneNumber.contains("1800")||phoneNumber.contains("1300"))
                && (phoneNumber.length() > 6)){
            return true;

        } else return false;
    }

}
