package com.hermogenio.cashpoint;

import android.content.Context;

import org.json.JSONObject;

public class CashpointInvoiceV9 {

    public static String buildText(
            Context context,
            JSONObject tx
    ) {

        CashpointSettingsV9 settings =
                new CashpointSettingsV9(context);

        String agent =
                settings.getAgentName();

        return
                "CASHPOINT\n" +
                "FACTURE / REÇU\n\n" +
                "N° : CP-" +
                System.currentTimeMillis() +
                "\n" +
                "Agent : " + agent + "\n" +
                "Client : " +
                tx.optString("nom", "") +
                "\n" +
                "Téléphone : " +
                tx.optString("numero", "") +
                "\n" +
                "Montant : " +
                tx.optString("montant", "0") +
                " Ar\n" +
                "Frais : " +
                tx.optString("frais", "0") +
                " Ar\n" +
                "Bénéfice : " +
                tx.optString("bonus", "0") +
                " Ar\n" +
                "Date : " +
                tx.optString("date", "") +
                "\n" +
                "Heure : " +
                tx.optString("heure", "") +
                "\n" +
                "Référence : " +
                tx.optString("reference", "") +
                "\n\n" +
                "Merci.";
    }
}
