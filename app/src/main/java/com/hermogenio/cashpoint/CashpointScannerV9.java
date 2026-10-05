package com.hermogenio.cashpoint;

import android.app.Activity;
import android.content.Intent;

public class CashpointScannerV9 {

    public static final int REQUEST =
            9091;

    public static void open(Activity activity) {

        Intent i = new Intent(
                activity,
                CashpointScannerActivity.class
        );

        activity.startActivityForResult(
                i,
                REQUEST
        );
    }
}
