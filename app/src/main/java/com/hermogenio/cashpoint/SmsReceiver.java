package com.hermogenio.cashpoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private static final String PREFS = "MVOLA_SMS_TRANSACTIONS";
    private static final String KEY = "transactions";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            return;
        }

        SmsMessage[] messages =
                Telephony.Sms.Intents.getMessagesFromIntent(intent);

        if (messages == null || messages.length == 0) {
            return;
        }

        StringBuilder body = new StringBuilder();
        String sender = "";

        for (SmsMessage sms : messages) {
            if (sms == null) continue;

            if (sender.isEmpty()) {
                sender = sms.getOriginatingAddress();
            }

            String part = sms.getMessageBody();
            if (part != null) {
                body.append(part);
            }
        }

        String smsText = body.toString().trim();

        if (smsText.isEmpty()) {
            return;
        }

        /*
         * M'VOLA / YAS transaction SMS detection.
         * We intentionally require transaction-related words.
         */
        String lower = smsText.toLowerCase(Locale.ROOT);

        boolean isMvola =
                lower.contains("mvola") ||
                lower.contains("solde") ||
                lower.contains("ref:") ||
                lower.contains("depot reussi") ||
                lower.contains("envoye a") ||
                lower.contains("recu de") ||
                lower.contains("achat de credit yas");

        if (!isMvola) {
            return;
        }

        try {
            JSONObject transaction = parseTransaction(
                    context,
                    smsText,
                    sender
            );

            saveTransaction(context, transaction);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject parseTransaction(
            Context context,
            String sms,
            String sender
    ) throws Exception {

        JSONObject o = new JSONObject();

        String clean = sms
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String lower = clean.toLowerCase(Locale.ROOT);

        // ----------------------------------------------------
        // TYPE OPERATION
        // ----------------------------------------------------
        String type = "Autre opération";

        if (lower.contains("recu de")) {
            type = "Argent reçu";
        } else if (lower.contains("depot reussi")) {
            type = "Dépôt";
        } else if (lower.contains("envoye a")) {
            type = "Transfert / Envoi";
        } else if (lower.contains("achat de credit yas")) {
            type = "Achat crédit YAS";
        }

        // ----------------------------------------------------
        // NOM + NUMERO
        // ----------------------------------------------------
        String nom = "";
        String numero = "";

        Matcher m;

        m = Pattern.compile(
                "(?i)recu\\s+de\\s+(.+?)\\s+(0\\d{9})\\s+le\\s+"
        ).matcher(clean);

        if (m.find()) {
            nom = m.group(1).trim();
            numero = m.group(2).trim();
        }

        if (numero.isEmpty()) {
            m = Pattern.compile(
                    "(?i)envoye\\s+a\\s+(.+?)\\s+(0\\d{9})\\s+le\\s+"
            ).matcher(clean);

            if (m.find()) {
                nom = m.group(1).trim();
                numero = m.group(2).trim();
            }
        }

        if (numero.isEmpty()) {
            m = Pattern.compile(
                    "(?i)depot\\s+reussi:\\s*"
                    + "\\d+\\s*Ar\\s+a[uù]pres\\s+de\\s+"
                    + "(.+?)\\s*\\((0\\d{9})\\)"
            ).matcher(clean);

            if (m.find()) {
                nom = m.group(1).trim();
                numero = m.group(2).trim();
            }
        }

        if (numero.isEmpty()) {
            m = Pattern.compile(
                    "(?i)pour\\s+(0\\d{9})"
            ).matcher(clean);

            if (m.find()) {
                numero = m.group(1).trim();
            }
        }

        // ----------------------------------------------------
        // MONTANT
        // ----------------------------------------------------
        long montant = extractAmountAfterKeywords(
                clean,
                "recu",
                "envoye",
                "depot reussi",
                "achat de credit yas"
        );

        // ----------------------------------------------------
        // FRAIS
        // ----------------------------------------------------
        long frais = extractAmount(
                clean,
                "(?i)Frais\\s*:\\s*([0-9 .]+)\\s*Ar"
        );

        // ----------------------------------------------------
        // BONUS
        // ----------------------------------------------------
        long bonus = extractAmount(
                clean,
                "(?i)Bonus\\s*:\\s*([0-9 .]+)\\s*Ar"
        );

        // ----------------------------------------------------
        // SOLDE
        // ----------------------------------------------------
        long solde = extractAmount(
                clean,
                "(?i)Solde(?:\\s+MVola)?\\s*:\\s*([0-9 .]+)\\s*Ar"
        );

        // ----------------------------------------------------
        // REFERENCE
        // ----------------------------------------------------
        String reference = extractText(
                clean,
                "(?i)Ref\\s*:\\s*([0-9]+)"
        );

        // ----------------------------------------------------
        // RAISON
        // ----------------------------------------------------
        String raison = extractText(
                clean,
                "(?i)Raison\\s*:\\s*([^.]*)"
        );

        // ----------------------------------------------------
        // DATE
        // ----------------------------------------------------
        String date = extractText(
                clean,
                "(?i)le\\s+(\\d{2}/\\d{2}/\\d{2})"
        );

        // ----------------------------------------------------
        // HEURE
        // ----------------------------------------------------
        String heure = extractText(
                clean,
                "(?i)le\\s+\\d{2}/\\d{2}/\\d{2}\\s+a\\s+(\\d{2}:\\d{2})"
        );

        // ----------------------------------------------------
        // RECEIVE TIME
        // ----------------------------------------------------
        String receptionDate = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        // ----------------------------------------------------
        // SAVE ALL FIELDS
        // ----------------------------------------------------
        o.put("type", type);
        o.put("nom", nom);
        o.put("numero", numero);
        o.put("montant", montant);
        o.put("frais", frais);
        o.put("bonus", bonus);
        o.put("solde", solde);
        o.put("reference", reference);
        o.put("raison", raison);
        o.put("date", date);
        o.put("heure", heure);
        o.put("dateReception", receptionDate);
        o.put("smsSender", sender == null ? "" : sender);
        o.put("smsOriginal", sms);

        return o;
    }

    private long extractAmountAfterKeywords(
            String text,
            String... keywords
    ) {

        for (String keyword : keywords) {

            Pattern p = Pattern.compile(
                    "(?i)" +
                    Pattern.quote(keyword) +
                    ".*?([0-9][0-9 .]*)\\s*Ar"
            );

            Matcher m = p.matcher(text);

            if (m.find()) {
                return parseNumber(m.group(1));
            }
        }

        return 0;
    }

    private long extractAmount(
            String text,
            String regex
    ) {

        Matcher m = Pattern.compile(regex).matcher(text);

        if (m.find()) {
            return parseNumber(m.group(1));
        }

        return 0;
    }

    private String extractText(
            String text,
            String regex
    ) {

        Matcher m = Pattern.compile(regex).matcher(text);

        if (m.find()) {
            return m.group(1).trim();
        }

        return "";
    }

    private long parseNumber(String value) {

        if (value == null) {
            return 0;
        }

        String digits = value.replaceAll("[^0-9]", "");

        if (digits.isEmpty()) {
            return 0;
        }

        try {
            return Long.parseLong(digits);
        } catch (Exception e) {
            return 0;
        }
    }

    private void saveTransaction(
            Context context,
            JSONObject transaction
    ) {

        try {

            SharedPreferences prefs =
                    context.getSharedPreferences(
                            PREFS,
                            Context.MODE_PRIVATE
                    );

            String old = prefs.getString(KEY, "[]");

            JSONArray array = new JSONArray(old);

            // Avoid duplicate SMS/reference.
            String newRef =
                    transaction.optString("reference", "");

            if (!newRef.isEmpty()) {

                for (int i = 0; i < array.length(); i++) {

                    JSONObject oldObj =
                            array.optJSONObject(i);

                    if (oldObj == null) continue;

                    if (newRef.equals(
                            oldObj.optString("reference", "")
                    )) {
                        return;
                    }
                }
            }

            JSONArray updated = new JSONArray();

            updated.put(transaction);

            // Keep latest 100 transactions.
            for (int i = 0;
                 i < array.length() && updated.length() < 100;
                 i++) {

                JSONObject item = array.optJSONObject(i);

                if (item != null) {
                    updated.put(item);
                }
            }

            prefs.edit()
                    .putString(KEY, updated.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
