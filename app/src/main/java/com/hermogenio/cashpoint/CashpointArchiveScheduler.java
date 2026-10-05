package com.hermogenio.cashpoint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class CashpointArchiveScheduler {

    private static final int REQUEST_CODE =
            2048;

    public static void programmer(
            Context context
    ) {

        try {

            AlarmManager alarm =
                    (AlarmManager)
                            context.getSystemService(
                                    Context.ALARM_SERVICE
                            );

            if (alarm == null) {
                return;
            }

            Intent intent =
                    new Intent(
                            context,
                            CashpointArchiveReceiver.class
                    );

            PendingIntent pending =
                    PendingIntent.getBroadcast(
                            context,
                            REQUEST_CODE,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

            Calendar c =
                    Calendar.getInstance();

            c.set(
                    Calendar.HOUR_OF_DAY,
                    20
            );

            c.set(
                    Calendar.MINUTE,
                    0
            );

            c.set(
                    Calendar.SECOND,
                    0
            );

            c.set(
                    Calendar.MILLISECOND,
                    0
            );

            if (c.getTimeInMillis()
                    <= System.currentTimeMillis()) {

                c.add(
                        Calendar.DAY_OF_YEAR,
                        1
                );
            }

            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    c.getTimeInMillis(),
                    pending
            );

        } catch (Exception e) {

            android.util.Log.e(
                    "CASHPOINT_SCHEDULER",
                    "Erreur scheduler",
                    e
            );
        }
    }
}
