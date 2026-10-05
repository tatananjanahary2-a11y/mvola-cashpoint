package com.hermogenio.cashpoint;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

public class CashpointDashboardV9 {

    private final Context context;

    public CashpointDashboardV9(Context context) {
        this.context = context;
    }

    private JSONArray transactions() {
        return CashpointDataV9.loadTransactions(context);
    }

    public long chiffreAffaires() {
        JSONArray a = transactions();
        long total = 0;

        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null) {
                total += CashpointDataV9.number(o.optString("montant"));
            }
        }

        return total;
    }

    public long frais() {
        JSONArray a = transactions();
        long total = 0;

        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null) {
                total += CashpointDataV9.number(o.optString("frais"));
            }
        }

        return total;
    }

    public long bonus() {
        JSONArray a = transactions();
        long total = 0;

        for (int i = 0; i < a.length(); i++) {
            JSONObject o = a.optJSONObject(i);
            if (o != null) {
                total += CashpointDataV9.number(o.optString("bonus"));
            }
        }

        return total;
    }

    public int nombreTransactions() {
        return transactions().length();
    }
}
