package com.hermogenio.cashpoint;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CashpointDailyArchive {

    private static final String SOURCE_PREF =
            "MVOLA_SMS_TRANSACTIONS";

    private static final String SOURCE_KEY =
            "transactions";

    private static final String ARCHIVE_PREF =
            "CASHPOINT_HISTORIQUE_PAR_DATE";

    public static void verifierEtArchiver(
            Context context
    ) {

        try {

            SharedPreferences source =
                    context.getSharedPreferences(
                            SOURCE_PREF,
                            Context.MODE_PRIVATE
                    );

            String raw =
                    source.getString(
                            SOURCE_KEY,
                            "[]"
                    );

            JSONArray transactions =
                    new JSONArray(raw);

            if (transactions.length() == 0) {
                return;
            }

            CalendarHelper heure =
                    new CalendarHelper();

            if (heure.hour() < 20) {
                return;
            }

            String date =
                    new SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                    ).format(
                            new Date()
                    );

            SharedPreferences archive =
                    context.getSharedPreferences(
                            ARCHIVE_PREF,
                            Context.MODE_PRIVATE
                    );

            JSONArray ancien =
                    new JSONArray(
                            archive.getString(
                                    date,
                                    "[]"
                            )
                    );

            for (int i = 0;
                    i < transactions.length();
                    i++) {

                JSONObject tx =
                        transactions.getJSONObject(i);

                boolean existe = false;

                String ref =
                        tx.optString(
                                "reference",
                                ""
                        );

                for (int j = 0;
                        j < ancien.length();
                        j++) {

                    JSONObject old =
                            ancien.getJSONObject(j);

                    if (!ref.isEmpty()
                            && ref.equals(
                            old.optString(
                                    "reference",
                                    ""
                            ))) {

                        existe = true;
                        break;
                    }
                }

                if (!existe) {
                    ancien.put(tx);
                }
            }

            archive.edit()
                    .putString(
                            date,
                            ancien.toString()
                    )
                    .apply();

            /*
             * IMPORTANT:
             * Tsy mamafa ny historique raha mbola
             * tsy azo antoka fa voatahiry tsara.
             */
            source.edit()
                    .putString(
                            SOURCE_KEY,
                            "[]"
                    )
                    .apply();

        } catch (Exception e) {

            android.util.Log.e(
                    "CASHPOINT_ARCHIVE",
                    "Erreur archive",
                    e
            );
        }
    }

    private static class CalendarHelper {

        int hour() {

            return Integer.parseInt(
                    new SimpleDateFormat(
                            "HH",
                            Locale.getDefault()
                    ).format(
                            new Date()
                    )
            );
        }
    }
}
