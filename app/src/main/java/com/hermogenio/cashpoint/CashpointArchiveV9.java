package com.hermogenio.cashpoint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

public class CashpointArchiveV9 {

    private static final int REQUEST_CODE = 2099;

    public static void schedule(Context context) {

        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        if (c.getTimeInMillis() <= System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent i = new Intent(
                context,
                CashpointArchiveReceiverV9.class
        );

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (am != null) {
            am.set(
                    AlarmManager.RTC_WAKEUP,
                    c.getTimeInMillis(),
                    pi
            );
        }
    }

    public static void archive(Context context) {

        JSONArray source =
                CashpointDataV9.loadTransactions(context);

        if (source.length() == 0) {
            schedule(context);
            return;
        }

        JSONObject archives =
                CashpointDataV9.archives(context);

        String date = CashpointDataV9.today();

        JSONArray day =
                archives.optJSONArray(date);

        if (day == null) {
            day = new JSONArray();
        }

        for (int i = 0; i < source.length(); i++) {

            JSONObject tx =
                    source.optJSONObject(i);

            if (tx == null) {
                continue;
            }

            String ref =
                    tx.optString("reference", "");

            boolean exists = false;

            for (int j = 0; j < day.length(); j++) {

                JSONObject old =
                        day.optJSONObject(j);

                if (old != null &&
                        ref.length() > 0 &&
                        ref.equals(
                                old.optString(
                                        "reference",
                                        ""
                                )
                        )) {

                    exists = true;
                    break;
                }
            }

            if (!exists) {
                day.put(tx);
            }
        }

        try {
            archives.put(date, day);
        } catch (org.json.JSONException e) {
            android.util.Log.e(
                    "CASHPOINT_ARCHIVE_V9",
                    "Erreur sauvegarde archive pour " + date,
                    e
            );
            return;
        }

        CashpointDataV9.saveArchives(
                context,
                archives
        );

        context.getSharedPreferences(
                "MVOLA_SMS_TRANSACTIONS",
                Context.MODE_PRIVATE
        )
                .edit()
                .putString(
                        "transactions",
                        "[]"
                )
                .apply();

        schedule(context);
    }

    public static class Receiver
            extends BroadcastReceiver {

        @Override
        public void onReceive(
                Context context,
                Intent intent
        ) {
            archive(context);
        }
    }
}
