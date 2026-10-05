package com.hermogenio.cashpoint;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CashpointDataV9 {

    private static final String SOURCE_PREFS = "MVOLA_SMS_TRANSACTIONS";
    private static final String SOURCE_KEY = "transactions";

    private static final String ARCHIVE_PREFS =
            "CASHPOINT_HISTORIQUE_PAR_DATE_V9";

    private static final String ARCHIVE_KEY =
            "archives";

    public static JSONArray loadTransactions(Context context) {
        try {
            SharedPreferences p = context.getSharedPreferences(
                    SOURCE_PREFS,
                    Context.MODE_PRIVATE
            );

            String raw = p.getString(SOURCE_KEY, "[]");

            return new JSONArray(raw);

        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static JSONObject archives(Context context) {
        try {
            SharedPreferences p = context.getSharedPreferences(
                    ARCHIVE_PREFS,
                    Context.MODE_PRIVATE
            );

            return new JSONObject(
                    p.getString(ARCHIVE_KEY, "{}")
            );

        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static void saveArchives(
            Context context,
            JSONObject archives
    ) {
        context.getSharedPreferences(
                ARCHIVE_PREFS,
                Context.MODE_PRIVATE
        ).edit()
                .putString(
                        ARCHIVE_KEY,
                        archives.toString()
                )
                .apply();
    }

    public static String today() {
        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
        ).format(new Date());
    }

    public static long number(String value) {

        if (value == null) {
            return 0;
        }

        String s = value
                .replace("Ar", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "")
                .trim();

        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
