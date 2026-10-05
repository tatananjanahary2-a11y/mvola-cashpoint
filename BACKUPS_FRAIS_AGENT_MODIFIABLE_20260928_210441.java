package com.hermogenio.cashpoint;

import android.Manifest;
import android.app.Activity;
import android.view.ViewGroup;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.graphics.Color;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    // ========================================================
    // CASHPOINT PROFESSIONAL UI
    // ========================================================
    private static final int UI_BG = Color.rgb(245, 247, 250);
    private static final int UI_CARD = Color.WHITE;
    private static final int UI_PRIMARY = Color.rgb(0, 150, 136);
    private static final int UI_PRIMARY_DARK = Color.rgb(0, 105, 92);
    private static final int UI_TEXT = Color.rgb(25, 32, 38);
    private static final int UI_MUTED = Color.rgb(105, 115, 125);
    private static final int UI_BORDER = Color.rgb(225, 230, 235);
    private static final int UI_SUCCESS = Color.rgb(25, 135, 84);

    private GradientDrawable uiBackground(
            int color,
            float radius) {

        GradientDrawable d =
                new GradientDrawable();

        d.setColor(color);
        d.setCornerRadius(dp(radius));

        return d;
    }

    private float dp(float value) {

        return value *
                getResources()
                        .getDisplayMetrics()
                        .density;
    }

    private TextView uiText(
            String value,
            float size,
            int color,
            boolean bold) {

        TextView t =
                new TextView(this);

        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);

        if (bold) {
            t.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD);
        }

        return t;
    }

    private TextView uiTitle(String value) {

        TextView t =
                uiText(
                        value,
                        22,
                        UI_TEXT,
                        true);

        t.setPadding(
                dpInt(4),
                dpInt(10),
                dpInt(4),
                dpInt(8));

        return t;
    }

    private TextView uiSubtitle(String value) {

        TextView t =
                uiText(
                        value,
                        14,
                        UI_MUTED,
                        false);

        t.setPadding(
                dpInt(4),
                0,
                dpInt(4),
                dpInt(12));

        return t;
    }

    private int dpInt(int value) {

        return (int)
                (value *
                getResources()
                        .getDisplayMetrics()
                        .density +
                0.5f);
    }

    private void styleButton(Button b) {

        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD);

        b.setAllCaps(false);

        b.setPadding(
                dpInt(16),
                dpInt(12),
                dpInt(16),
                dpInt(12));

        b.setBackground(
                uiBackground(
                        UI_PRIMARY,
                        14));
    }

    private void styleInput(EditText e) {

        e.setTextSize(16);
        e.setTextColor(UI_TEXT);
        e.setHintTextColor(UI_MUTED);
        e.setSingleLine(true);

        e.setPadding(
                dpInt(14),
                dpInt(12),
                dpInt(14),
                dpInt(12));

        e.setBackground(
                uiBackground(
                        UI_CARD,
                        12));

        e.setBackgroundTintList(null);
    }


    private static final int REQUEST_CONTACTS = 1001;
    private static final int REQUEST_CALL = 1002;

    private LinearLayout content;
    private TextView header;

    private final ArrayList<String> historique =
            new ArrayList<>();

    /*
     * FRAIS DE SERVICE AGENT / CASH POINT
     * Ces montants sont distincts des frais opérateur.
     */
    private final int[][] fraisAgent = {
            {2000, 500, 150, 650},
            {5000, 600, 275, 875},
            {10001, 1000, 650, 1650},
            {25000, 1500, 650, 2150},
            {25001, 2000, 1300, 3300},
            {50000, 2500, 1300, 3800},
            {50001, 3500, 1900, 5400},
            {100000, 3300, 1900, 5200},
            {100001, 5000, 3400, 8400},
            {250000, 6000, 3400, 9400},
            {250001, 8000, 4700, 12700},
            {500000, 9000, 4700, 13700},
            {500001, 10000, 8800, 18800},
            {1000000, 12000, 8800, 20800},
            {1000001, 25000, 14700, 39700},
            {2000000, 28000, 14700, 42700},
            {2000001, 32000, 19600, 51600},
            {3000000, 32000, 19600, 51600},
            {3000001, 35000, 24500, 59500},
            {4000001, 39000, 29400, 68400},
            {5000001, 45000, 34300, 79300},
            {6000001, 50000, 39200, 89200},
            {7000001, 55000, 44100, 99100},
            {8000001, 60000, 49000, 109000},
            {9000001, 65000, 53900, 118900},
            {10000001, 75000, 59000, 134000},
            {11000000, 85000, 64000, 149000},
            {12000000, 95000, 69000, 164000},
            {13000000, 105000, 74000, 179000},
            {14000000, 115000, 79000, 194000},
            {15000000, 125000, 84000, 209000},
            {16000000, 135000, 89000, 224000},
            {17000000, 145000, 94000, 239000},
            {18000000, 165000, 98000, 263000},
            {19000000, 180000, 100000, 280000}
    };

    /*
     * FRAIS OPERATEUR
     * [minimum, maximum, depot, retrait]
     */
    private final int[][] fraisOperateur = {
            {100, 1000, 70, 100},
            {1001, 5000, 70, 150},
            {5001, 10000, 150, 275},
            {10001, 20000, 250, 550},
            {20001, 25000, 250, 650},
            {25001, 50000, 500, 1300},
            {50001, 100000, 1000, 1900},
            {100001, 250000, 1900, 3400},
            {250001, 500000, 1900, 4700},
            {500001, 1000000, 3200, 8800},
            {1000001, 2000000, 3800, 14700},
            {2000001, 3000000, 5000, 19600},
            {3000001, 4000000, 6300, 24500},
            {4000001, 5000000, 7500, 29400},
            {5000001, 6000000, 9400, 34300},
            {6000001, 7000000, 10700, 39200},
            {7000001, 8000000, 12500, 44100},
            {8000001, 9000000, 14400, 49000},
            {9000001, 10000000, 15700, 53900},
            {10000001, 11000000, 17500, 59000},
            {11000001, 12000000, 18800, 64000},
            {12000001, 13000000, 20000, 69000},
            {13000001, 14000000, 21300, 74000},
            {14000001, 15000000, 23200, 79000},
            {15000001, 16000000, 25000, 84000},
            {16000001, 17000000, 26300, 89000},
            {17000001, 18000000, 28200, 94000},
            {18000001, 19000000, 30000, 98000},
            {19000001, 20000000, 31300, 100000}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ====================================================
        // SMS PERMISSIONS - ADD ONLY
        // Permet à CASHPOINT de recevoir/lire les SMS M'VOLA
        // ====================================================
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            java.util.ArrayList<String> permissions = new java.util.ArrayList<>();

            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECEIVE_SMS);
            }

            if (checkSelfPermission(Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_SMS);
            }

            if (checkSelfPermission(Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_CONTACTS);
            }

            if (permissions.size() > 0) {
                requestPermissions(
                    permissions.toArray(new String[0]),
                    5001
                );
            }
        }

        super.onCreate(savedInstanceState);
        construireInterface();
    }

    private void construireInterface() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(12), dp(14), dp(90));

        header = new TextView(this);
        header.setText(
                "M'VOLA\n" +
                "CASH POINT • USSD\n\n" +
                "28 SEPT. 2026");
        header.setTextSize(24);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(15), 0, dp(20));

        root.addView(header);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        root.addView(content);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);

        Button accueil = button("⌂\nAccueil");
        Button benefice = button("📊\nBénéfice");
        Button contacts = button("👥\nContacts");
        Button historiqueBtn = button("🕘\nHistorique");

        nav.addView(accueil, poids());
        nav.addView(benefice, poids());
        nav.addView(contacts, poids());
        nav.addView(historiqueBtn, poids());

        root.addView(nav);

        accueil.setOnClickListener(v -> showHome());
        benefice.setOnClickListener(v -> showBenefice());
        contacts.setOnClickListener(v -> showContacts());
        historiqueBtn.setOnClickListener(v -> showHistorique());

        scroll.addView(root);
        setContentView(scroll);

        showHome();
    }

    private void showHome() {

        content.removeAllViews();

        // ====================================================
        // HEADER DASHBOARD PRO
        // ====================================================

        LinearLayout welcome =
                new LinearLayout(this);

        welcome.setOrientation(
                LinearLayout.VERTICAL);

        welcome.setPadding(
                dpInt(20),
                dpInt(18),
                dpInt(20),
                dpInt(18));

        welcome.setBackground(
                uiBackground(
                        UI_PRIMARY_DARK,
                        20));

        TextView brand =
                uiText(
                        "M'VOLA",
                        28,
                        Color.WHITE,
                        true);

        TextView cashpoint =
                uiText(
                        "CASH POINT",
                        15,
                        Color.WHITE,
                        true);

        TextView description =
                uiText(
                        "Gestion professionnelle • USSD",
                        13,
                        Color.WHITE,
                        false);

        welcome.addView(brand);
        welcome.addView(cashpoint);
        welcome.addView(description);

        LinearLayout.LayoutParams welcomeParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        welcomeParams.setMargins(
                dpInt(8),
                dpInt(8),
                dpInt(8),
                dpInt(16));

        content.addView(
                welcome,
                welcomeParams);

        // ====================================================
        // TITRE
        // ====================================================

        content.addView(
                uiTitle("Gestion Cash Point"));

        content.addView(
                uiSubtitle(
                        "Opérations rapides • Tarifs • Bénéfice net"));

        // ====================================================
        // RESUME
        // ====================================================

        LinearLayout resume =
                new LinearLayout(this);

        resume.setOrientation(
                LinearLayout.VERTICAL);

        resume.setPadding(
                dpInt(18),
                dpInt(16),
                dpInt(18),
                dpInt(16));

        resume.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        TextView resumeTitle =
                uiText(
                        "APERÇU",
                        12,
                        UI_MUTED,
                        true);

        TextView resumeValue =
                uiText(
                        "M'VOLA • CASH POINT",
                        19,
                        UI_TEXT,
                        true);

        TextView resumeInfo =
                uiText(
                        "Opérations, USSD, frais et bénéfice net",
                        13,
                        UI_MUTED,
                        false);

        resume.addView(resumeTitle);
        resume.addView(resumeValue);
        resume.addView(resumeInfo);

        LinearLayout.LayoutParams resumeParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        resumeParams.setMargins(
                dpInt(8),
                0,
                dpInt(8),
                dpInt(18));

        content.addView(
                resume,
                resumeParams);

        // ====================================================
        // OPERATIONS RAPIDES
        // ====================================================

        TextView operationsTitle =
                uiText(
                        "Opérations rapides",
                        18,
                        UI_TEXT,
                        true);

        operationsTitle.setPadding(
                dpInt(8),
                0,
                dpInt(8),
                dpInt(10));

        content.addView(
                operationsTitle);

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL);

        Button transfert =
                proHomeButton(
                        "💸",
                        "Transfert",
                        "Mandefa vola");

        Button depot =
                proHomeButton(
                        "💰",
                        "Dépôt",
                        "Dépôt client");

        addHomeButton(
                row1,
                transfert);

        addHomeButton(
                row1,
                depot);

        content.addView(row1);

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL);

        Button credit =
                proHomeButton(
                        "📶",
                        "Crédit",
                        "Mivarotra Crédit");

        Button offres =
                proHomeButton(
                        "🛍️",
                        "Offres",
                        "Mivarotra Offre");

        addHomeButton(
                row2,
                credit);

        addHomeButton(
                row2,
                offres);

        content.addView(row2);

        LinearLayout row3 =
                new LinearLayout(this);

        row3.setOrientation(
                LinearLayout.HORIZONTAL);

        Button solde =
                proHomeButton(
                        "💳",
                        "Solde",
                        "Mijery Solde");

        Button cp =
                proHomeButton(
                        "🏪",
                        "Cash Point",
                        "Transfert vers CP");

        addHomeButton(
                row3,
                solde);

        addHomeButton(
                row3,
                cp);

        content.addView(row3);

        LinearLayout row4 =
                new LinearLayout(this);

        row4.setOrientation(
                LinearLayout.HORIZONTAL);

        Button frais =
                proHomeButton(
                        "💵",
                        "Frais Agent",
                        "Service Cash Point");

        Button benef =
                proHomeButton(
                        "📊",
                        "Bénéfice",
                        "Net par transaction");

        addHomeButton(
                row4,
                frais);

        addHomeButton(
                row4,
                benef);

        content.addView(row4);

        // ====================================================
        // NAVIGATION EXISTANTE / ACTIONS
        // ====================================================

        transfert.setOnClickListener(
                v -> showTransfert());

        depot.setOnClickListener(
                v -> showDepot());

        credit.setOnClickListener(
                v -> showCredit());

        offres.setOnClickListener(
                v -> showOffres());

        frais.setOnClickListener(
                v -> showFrais());

        benef.setOnClickListener(
                v -> showBenefice());

        solde.setOnClickListener(
                v -> appelerUSSD(
                        "#111*1*7*1#"));

        cp.setOnClickListener(
                v -> appelerUSSD(
                        "#111*1*9#"));
    }

    // ========================================================
    // BOUTON ACCUEIL PRO
    // ========================================================

    private Button proHomeButton(
            String icon,
            String title,
            String subtitle) {

        Button b =
                new Button(this);

        b.setText(
                icon +
                "\n" +
                title +
                "\n" +
                subtitle);

        b.setTextSize(13);
        b.setTextColor(UI_TEXT);
        b.setGravity(
                Gravity.CENTER);

        b.setAllCaps(false);

        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD);

        b.setPadding(
                dpInt(6),
                dpInt(10),
                dpInt(6),
                dpInt(10));

        b.setMinHeight(
                dpInt(105));

        b.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        return b;
    }

    private void addHomeButton(
            LinearLayout row,
            Button button) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(112),
                        1f);

        params.setMargins(
                dpInt(5),
                dpInt(5),
                dpInt(5),
                dpInt(5));

        row.addView(
                button,
                params);
    }

    private void showTransfert() {

        content.removeAllViews();

        content.addView(
                uiTitle("💸 Transfert"));

        content.addView(
                uiSubtitle(
                        "Mandefa vola • Créer un code USSD"));

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL);

        card.setPadding(
                dpInt(18),
                dpInt(18),
                dpInt(18),
                dpInt(18));

        card.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        EditText numero =
                input("Numéro du client");

        styleInput(numero);

        EditText montant =
                input("Montant (Ar)");

        styleInput(montant);

        TextView resultat =
                result();

        Button generate =
                button("Générer le code USSD");

        styleButton(generate);

        Button call =
                button("📞 Appeler USSD");

        styleButton(call);

        card.addView(numero);
        card.addView(montant);
        card.addView(generate);
        card.addView(resultat);
        card.addView(call);

        content.addView(card);

        generate.setOnClickListener(v -> {

            String n =
                    cleanNumber(
                            numero.getText().toString());

            String m =
                    digits(
                            montant.getText().toString());

            if (n.isEmpty() || m.isEmpty()) {

                toast(
                        "Numéro et montant obligatoires");

                return;
            }

            String code =
                    "#111*1*2*" +
                    n +
                    "*1*" +
                    m +
                    "#";

            resultat.setText(
                    "CODE USSD\n\n" + code);

            ajouterHistorique(
                    "Transfert : " +
                    m +
                    " Ar");
        });

        call.setOnClickListener(v -> {

            String n =
                    cleanNumber(
                            numero.getText().toString());

            String m =
                    digits(
                            montant.getText().toString());

            if (n.isEmpty() || m.isEmpty()) {

                toast(
                        "Numéro et montant obligatoires");

                return;
            }

            appelerUSSD(
                    "#111*1*2*" +
                    n +
                    "*1*" +
                    m +
                    "#");
        });
    }


    private void showDepot() {

        content.removeAllViews();

        content.addView(
                uiTitle("💰 Dépôt"));

        content.addView(
                uiSubtitle(
                        "Dépôt à distance • Dépôt client"));

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL);

        card.setPadding(
                dpInt(18),
                dpInt(18),
                dpInt(18),
                dpInt(18));

        card.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        EditText numero =
                input("Numéro du client");

        styleInput(numero);

        EditText montant =
                input("Montant (Ar)");

        styleInput(montant);

        TextView resultat =
                result();

        Button generate =
                button("Générer le code USSD");

        styleButton(generate);

        Button call =
                button("📞 Appeler USSD");

        styleButton(call);

        card.addView(numero);
        card.addView(montant);
        card.addView(generate);
        card.addView(resultat);
        card.addView(call);

        content.addView(card);

        generate.setOnClickListener(v -> {

            String n =
                    cleanNumber(
                            numero.getText().toString());

            String m =
                    digits(
                            montant.getText().toString());

            if (n.isEmpty() || m.isEmpty()) {

                toast(
                        "Numéro et montant obligatoires");

                return;
            }

            String code =
                    "#111*1*2*" +
                    n +
                    "*1*" +
                    m +
                    "#";

            resultat.setText(
                    "CODE USSD\n\n" + code);

            ajouterHistorique(
                    "Dépôt : " +
                    m +
                    " Ar");
        });

        call.setOnClickListener(v -> {

            String n =
                    cleanNumber(
                            numero.getText().toString());

            String m =
                    digits(
                            montant.getText().toString());

            if (n.isEmpty() || m.isEmpty()) {

                toast(
                        "Numéro et montant obligatoires");

                return;
            }

            appelerUSSD(
                    "#111*1*2*" +
                    n +
                    "*1*" +
                    m +
                    "#");
        });
    }


    private void showCredit() {

        content.removeAllViews();

        content.addView(
                uiTitle("📶 Mivarotra Crédit"));

        content.addView(
                uiSubtitle(
                        "Vente de crédit • Code USSD"));

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL);

        card.setPadding(
                dpInt(18),
                dpInt(18),
                dpInt(18),
                dpInt(18));

        card.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        EditText numero =
                input("Numéro du client");

        styleInput(numero);

        EditText montant =
                input("Montant (Ar)");

        styleInput(montant);

        TextView resultat =
                result();

        Button generate =
                button("Générer le code USSD");

        styleButton(generate);

        Button call =
                button("📞 Appeler USSD");

        styleButton(call);

        card.addView(numero);
        card.addView(montant);
        card.addView(generate);
        card.addView(resultat);
        card.addView(call);

        content.addView(card);

        generate.setOnClickListener(v -> {

            String n =
                    cleanNumber(
                            numero.getText().toString());

            String m =
                    digits(
                            montant.getText().toString());

            if (n.isEmpty() || m.isEmpty()) {

                toast(
                        "Numéro et montant obligatoires");

                return;
            }

            String code =
                    "#111*1*4*2*1*" +
                    n +
                    "*" +
                    m +
                    "#";

            resultat.setText(
                    "CODE USSD\n\n" + code);

            ajouterHistorique(
                    "Crédit : " +
                    m +
                    " Ar");
        });

        call.setOnClickListener(v -> {

            String n =
                    cleanNumber(
                            numero.getText().toString());

            String m =
                    digits(
                            montant.getText().toString());

            if (n.isEmpty() || m.isEmpty()) {

                toast(
                        "Numéro et montant obligatoires");

                return;
            }

            appelerUSSD(
                    "#111*1*4*2*1*" +
                    n +
                    "*" +
                    m +
                    "#");
        });
    }


    private void showOffres() {
        content.removeAllViews();

        TextView title = uiTitle("🛍️ Mivarotra Offre");
        content.addView(title);

        TextView info = uiSubtitle(
                "Sélection rapide des offres");

        content.addView(info);

        final EditText numero = new EditText(this);
        numero.setHint("Numéro du client");
        numero.setInputType(2);
        styleInput(numero);

        content.addView(
            numero,
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        );

        final TextView selection = new TextView(this);
        selection.setText(
            "OFFRE SÉLECTIONNÉE\nAucune offre sélectionnée"
        );
        selection.setTextSize(16);
        selection.setTextColor(UI_TEXT);
        selection.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        selection.setPadding(
                dpInt(18),
                dpInt(18),
                dpInt(18),
                dpInt(18));
        selection.setBackground(
                uiBackground(UI_CARD, 16));

        LinearLayout.LayoutParams selectionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        selectionParams.setMargins(
                0, dpInt(12), 0, dpInt(12));

        content.addView(selection, selectionParams);

        final TextView code = new TextView(this);
        code.setText("Code USSD\nAucun code généré");
        code.setTextSize(15);
        code.setTextColor(UI_TEXT);
        code.setPadding(
                dpInt(18),
                dpInt(18),
                dpInt(18),
                dpInt(18));
        code.setBackground(
                uiBackground(
                        Color.rgb(239, 243, 246),
                        16));

        LinearLayout.LayoutParams codeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        codeParams.setMargins(
                0, dpInt(4), 0, dpInt(12));

        content.addView(code, codeParams);

        final String[] categorie = {""};
        final String[] offreNom = {""};
        final String[] offreNumero = {""};

        LinearLayout categories =
                new LinearLayout(this);

        categories.setOrientation(
                LinearLayout.HORIZONTAL);

        categories.setGravity(Gravity.CENTER);

        Button mora = new Button(this);
        mora.setText("MORA");
        styleButton(mora);

        Button first = new Button(this);
        first.setText("FIRST");
        styleButton(first);

        Button yelow = new Button(this);
        yelow.setText("YELOW");
        styleButton(yelow);

        categories.addView(
                mora,
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(52),
                        1));

        categories.addView(
                first,
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(52),
                        1));

        categories.addView(
                yelow,
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(52),
                        1));

        content.addView(categories);

        final LinearLayout liste = new LinearLayout(this);
        liste.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams listeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        listeParams.setMargins(
                0, dpInt(12), 0, dpInt(8));

        content.addView(liste, listeParams);

        final String[][] moraOffers = {
            {"Mora 500", "1"},
            {"MoraOne 1000", "2"},
            {"Mora+ 2000", "3"},
            {"Mora+ 5000", "4"},
            {"Mora international", "5"},
            {"Morantsika", "6"}
        };

        final String[][] firstOffers = {
            {"First premium", "1"},
            {"First premium+", "2"},
            {"Promo first prestige 15Go", "3"},
            {"First royal", "4"}
        };

        final String[][] yelowOffers = {
            {"YELOW100", "1"},
            {"YELOW SMS", "2"},
            {"YELOW 500", "3"},
            {"YELOW 1000", "4"},
            {"YELOW ONE", "5"},
            {"Yelow 200", "6"},
            {"Yelow 2000", "7"},
            {"Yelow 2500", "8"},
            {"YELOW UP", "9"}
        };

        final java.util.function.BiConsumer<String[][], String> afficher =
            (offers, cat) -> {
                categorie[0] = cat;
                liste.removeAllViews();

                for (String[] offer : offers) {
                    Button b = new Button(this);
                    b.setText("🛍️ " + offer[0]);
                    b.setTextSize(15);
                    styleButton(b);

                    b.setOnClickListener(v -> {
                        offreNom[0] = offer[0];
                        offreNumero[0] = offer[1];

                        selection.setText(
                            "OFFRE SÉLECTIONNÉE\n" +
                            "✅ " + offreNom[0]
                        );

                        code.setText(
                            "Code USSD\nAucun code généré"
                        );
                    });

                    liste.addView(
                        b,
                        new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    );
                }
            };

        mora.setOnClickListener(v ->
            afficher.accept(moraOffers, "1")
        );

        first.setOnClickListener(v ->
            afficher.accept(firstOffers, "2")
        );

        yelow.setOnClickListener(v ->
            afficher.accept(yelowOffers, "3")
        );

        Button generate = new Button(this);
        generate.setText("Générer le code USSD");
        content.addView(generate);

        generate.setOnClickListener(v -> {
            String num = numero.getText().toString().trim();

            if (num.isEmpty()) {
                numero.setError("Ampidiro ny numéro");
                return;
            }

            if (offreNom[0].isEmpty()) {
                Toast.makeText(
                    this,
                    "Sélectionnez une offre",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String ussd =
                "#111*1*4*5*" +
                num +
                "*" +
                categorie[0] +
                "*" +
                offreNumero[0] +
                "#";

            code.setText(
                "Code USSD\n" + ussd
            );
        });

        Button call = new Button(this);
        call.setText("📞 Appeler USSD");
        content.addView(call);

        call.setOnClickListener(v -> {
            String num = numero.getText().toString().trim();

            if (num.isEmpty()) {
                numero.setError("Ampidiro ny numéro");
                return;
            }

            if (offreNom[0].isEmpty()) {
                Toast.makeText(
                    this,
                    "Sélectionnez une offre",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String ussd =
                "#111*1*4*5*" +
                num +
                "*" +
                categorie[0] +
                "*" +
                offreNumero[0] +
                "#";

            appelerUSSD(ussd);
        });

        afficher.accept(moraOffers, "1");
    }


    private void afficherOffres(
            EditText numero,
            TextView resultat,
            String groupe) {

        String n = cleanNumber(
                numero.getText().toString());

        if (n.isEmpty()) {
            toast("Numéro du client obligatoire");
            return;
        }

        StringBuilder s = new StringBuilder();

        s.append("OFFRE SÉLECTIONNÉE : ")
                .append(groupe)
                .append("\n\n");

        if (groupe.equals("MORA")) {

            offre(s, "Mora 500",
                    "#111*1*4*5*" + n + "*1*1#");

            offre(s, "Mora One",
                    "#111*1*4*5*" + n + "*1*2#");

            offre(s, "Mora+ 2000",
                    "#111*1*4*" + n + "*1*3#");

            offre(s, "Mora+ 5000",
                    "#111*1*4*" + n + "*1*4#");

            offre(s, "Mora international",
                    "#111*1*4*" + n + "*1*5#");

            offre(s, "Morantsika",
                    "#111*1*4*" + n + "*1*6#");

        } else if (groupe.equals("FIRST")) {

            offre(s, "First premium",
                    "#111*1*4*5*" + n + "*2*1#");

            offre(s, "First premium+",
                    "#111*1*4*5*" + n + "*2*2#");

            offre(s, "Promo first prestige 15Go",
                    "#111*1*4*5*" + n + "*2*3#");

            offre(s, "First royal",
                    "#111*1*4*5*" + n + "*2*4#");

        } else {

            offre(s, "YELOW100",
                    "#111*1*4*5*" + n + "*3*1#");

            offre(s, "YELOW SMS",
                    "#111*1*4*5*" + n + "*3*2#");

            offre(s, "YELOW 500",
                    "#111*1*4*5*" + n + "*3*3#");

            offre(s, "YELOW 1000",
                    "#111*1*4*5*" + n + "*3*4#");

            offre(s, "YELOW ONE",
                    "#111*1*4*5*" + n + "*3*5#");

            offre(s, "Yelow 200",
                    "#111*1*4*5*" + n + "*3*6#");

            offre(s, "Yelow 2000",
                    "#111*1*4*5*" + n + "*3*7#");

            offre(s, "Yelow 2500",
                    "#111*1*4*5*" + n + "*3*8#");

            offre(s, "Yelow UP",
                    "#111*1*4*5*" + n + "*3*9#");
        }

        resultat.setText(s.toString());

        ajouterHistorique(
                "Offre " + groupe);
    }

    private void offre(
            StringBuilder s,
            String nom,
            String code) {

        s.append(nom)
                .append("\n")
                .append(code)
                .append("\n\n");
    }

    private void showFrais() {

        content.removeAllViews();

        content.addView(
                uiTitle("💵 Frais Agent / Cash Point"));

        content.addView(
                uiSubtitle(
                        "Frais de service du Cash Point"));

        TextView notice = uiText(
                "Frais de service Agent / Cash Point\n\n" +
                "Ces montants sont séparés des frais opérateur.",
                15,
                UI_MUTED,
                false);

        notice.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        notice.setBackground(
                uiBackground(
                        Color.rgb(239, 243, 246),
                        16));

        content.addView(notice);

        EditText recherche =
                new EditText(this);

        recherche.setHint(
                "Rechercher un montant");

        recherche.setInputType(2);

        styleInput(recherche);

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        searchParams.setMargins(
                0, dpInt(14), 0, dpInt(10));

        content.addView(
                recherche,
                searchParams);

        LinearLayout table =
                new LinearLayout(this);

        table.setOrientation(
                LinearLayout.VERTICAL);

        table.setPadding(
                dpInt(12),
                dpInt(12),
                dpInt(12),
                dpInt(12));

        table.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        TextView header =
                uiText(
                        "Montant          Envoi          Retrait          Total",
                        13,
                        UI_TEXT,
                        true);

        header.setPadding(
                dpInt(8),
                dpInt(8),
                dpInt(8),
                dpInt(12));

        table.addView(header);

        LinearLayout rows =
                new LinearLayout(this);

        rows.setOrientation(
                LinearLayout.VERTICAL);

        table.addView(rows);

        content.addView(table);

        Runnable afficher =
                () -> {

                    rows.removeAllViews();

                    String rechercheTexte =
                            recherche.getText()
                                    .toString()
                                    .trim();

                    for (int[] r : fraisAgent) {

                        String montantTexte =
                                format(r[0]);

                        if (!rechercheTexte.isEmpty()
                                && !montantTexte.contains(
                                        rechercheTexte)) {
                            continue;
                        }

                        TextView row =
                                uiText(
                                        montantTexte +
                                        " Ar    " +
                                        format(r[1]) +
                                        " Ar    " +
                                        format(r[2]) +
                                        " Ar    " +
                                        format(r[3]) +
                                        " Ar",
                                        13,
                                        UI_TEXT,
                                        false);

                        row.setPadding(
                                dpInt(8),
                                dpInt(10),
                                dpInt(8),
                                dpInt(10));

                        rows.addView(row);
                    }
                };

        recherche.addTextChangedListener(
                new android.text.TextWatcher() {

                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {}

                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {
                        afficher.run();
                    }

                    public void afterTextChanged(
                            android.text.Editable e) {}
                });

        afficher.run();
    }

    private void showBenefice() {

        content.removeAllViews();

        content.addView(
                uiTitle("📊 Calcul bénéfice net"));

        content.addView(
                uiSubtitle(
                        "Gain réel sur chaque transaction"));

        TextView formule =
                uiText(
                        "Formule :\n" +
                        "Frais service Agent / Cash Point" +
                        " − Frais opérateur\n\n" +
                        "Le montant envoyé n'est pas ajouté " +
                        "au bénéfice.",
                        15,
                        UI_MUTED,
                        false);

        formule.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        formule.setBackground(
                uiBackground(
                        Color.rgb(239, 243, 246),
                        16));

        content.addView(formule);

        Spinner type =
                new Spinner(this);

        String[] types = {
                "💰 Dépôt à distance",
                "💵 Retrait d'argent"
        };

        type.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        types));

        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        typeParams.setMargins(
                0, dpInt(14), 0, dpInt(8));

        content.addView(type, typeParams);

        EditText montant =
                new EditText(this);

        montant.setHint(
                "Montant de la transaction (Ar)");

        montant.setInputType(2);

        styleInput(montant);

        content.addView(montant);

        Button calculer =
                button(
                        "Calculer le bénéfice net");

        styleButton(calculer);

        content.addView(calculer);

        TextView resultat =
                uiText(
                        "Bénéfice net\nAucun calcul effectué",
                        16,
                        UI_TEXT,
                        true);

        resultat.setPadding(
                dpInt(18),
                dpInt(18),
                dpInt(18),
                dpInt(18));

        resultat.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        LinearLayout.LayoutParams resultParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        resultParams.setMargins(
                0, dpInt(14), 0, dpInt(10));

        content.addView(
                resultat,
                resultParams);

        calculer.setOnClickListener(v -> {

            String valeur =
                    montant.getText()
                            .toString()
                            .trim();

            if (valeur.isEmpty()) {

                montant.setError(
                        "Ampidiro ny montant");

                return;
            }

            int amount;

            try {

                amount = Integer.parseInt(
                        valeur.replaceAll(
                                "[^0-9]",
                                ""));

            } catch (Exception e) {

                montant.setError(
                        "Montant invalide");

                return;
            }

            if (amount <= 0) {

                montant.setError(
                        "Montant invalide");

                return;
            }

            int agentIndex =
                    trouverAgentIndex(amount);

            int operatorIndex =
                    trouverOperateurIndex(
                            amount);

            if (agentIndex < 0
                    || operatorIndex < 0) {

                toast(
                        "Montant hors barème disponible");

                return;
            }

            int[] agent =
                    fraisAgent[agentIndex];

            int[] operator =
                    fraisOperateur[operatorIndex];

            int fraisAgentChoisi =
                    type.getSelectedItemPosition() == 0
                            ? agent[1]
                            : agent[2];

            int fraisOperateurChoisi =
                    type.getSelectedItemPosition() == 0
                            ? operator[1]
                            : operator[2];

            int beneficeNet =
                    fraisAgentChoisi -
                    fraisOperateurChoisi;

            String operation =
                    type.getSelectedItemPosition() == 0
                            ? "💰 Dépôt à distance"
                            : "💵 Retrait d'argent";

            resultat.setText(
                    "📊 BÉNÉFICE NET\n\n" +
                    "Type : " +
                    operation +
                    "\n\n" +
                    "Montant : " +
                    format(amount) +
                    " Ar\n\n" +
                    "Frais Agent / Cash Point : " +
                    format(fraisAgentChoisi) +
                    " Ar\n\n" +
                    "Frais opérateur : " +
                    format(fraisOperateurChoisi) +
                    " Ar\n\n" +
                    "Bénéfice net : " +
                    format(beneficeNet) +
                    " Ar\n\n" +
                    format(fraisAgentChoisi) +
                    " − " +
                    format(fraisOperateurChoisi) +
                    " = " +
                    format(beneficeNet) +
                    " Ar");

            ajouterHistorique(
                    "Bénéfice " +
                    operation +
                    " : " +
                    format(beneficeNet) +
                    " Ar");
        });
    }

    private int trouverAgentIndex(int montant) {

        /*
         * Les valeurs du tableau Agent sont conservées.
         * On utilise la borne suivante pour former les intervalles.
         */
        for (int i = 0;
             i < fraisAgent.length;
             i++) {

            int debut =
                    fraisAgent[i][0];

            int fin =
                    i + 1 < fraisAgent.length
                    ? fraisAgent[i + 1][0] - 1
                    : Integer.MAX_VALUE;

            if (montant >= debut &&
                    montant <= fin) {

                return i;
            }
        }

        return -1;
    }

    private int trouverOperateurIndex(
            int montant) {

        for (int i = 0;
             i < fraisOperateur.length;
             i++) {

            int[] r =
                    fraisOperateur[i];

            if (montant >= r[0] &&
                    montant <= r[1]) {

                return i;
            }
        }

        return -1;
    }

    private void showContacts() {

        content.removeAllViews();

        content.addView(
                uiTitle("👥 Contacts"));

        content.addView(
                uiSubtitle(
                        "Répertoire Android"));

        TextView info =
                uiText(
                        "Utilisation de la connexion Android " +
                        "aux contacts existante.",
                        15,
                        UI_MUTED,
                        false);

        info.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        info.setBackground(
                uiBackground(
                        Color.rgb(239, 243, 246),
                        16));

        content.addView(info);

        Button charger =
                button("📱 Charger les contacts");

        styleButton(charger);

        LinearLayout.LayoutParams chargerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        chargerParams.setMargins(
                0,
                dpInt(14),
                0,
                dpInt(10));

        content.addView(
                charger,
                chargerParams);

        TextView resultat =
                uiText(
                        "Appuyez sur « Charger les contacts » " +
                        "pour afficher le Répertoire Android.",
                        15,
                        UI_TEXT,
                        false);

        resultat.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        resultat.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        content.addView(resultat);

        charger.setOnClickListener(
                v -> chargerContacts(resultat));
    }

    private void chargerContacts(
            TextView resultat) {

        Cursor cursor =
                getContentResolver().query(
                        ContactsContract
                                .CommonDataKinds
                                .Phone.CONTENT_URI,
                        new String[]{
                                ContactsContract
                                        .CommonDataKinds
                                        .Phone
                                        .DISPLAY_NAME,
                                ContactsContract
                                        .CommonDataKinds
                                        .Phone
                                        .NUMBER
                        },
                        null,
                        null,
                        ContactsContract
                                .CommonDataKinds
                                .Phone
                                .DISPLAY_NAME +
                                " ASC");

        if (cursor == null) {

            resultat.setText(
                    "Impossible de charger les contacts.");

            return;
        }

        StringBuilder s =
                new StringBuilder();

        int count = 0;

        try {

            while (cursor.moveToNext()) {

                int nameIndex =
                        cursor.getColumnIndex(
                                ContactsContract
                                        .CommonDataKinds
                                        .Phone
                                        .DISPLAY_NAME);

                int numberIndex =
                        cursor.getColumnIndex(
                                ContactsContract
                                        .CommonDataKinds
                                        .Phone
                                        .NUMBER);

                String name =
                        nameIndex >= 0
                        ? cursor.getString(nameIndex)
                        : "";

                String phone =
                        numberIndex >= 0
                        ? cursor.getString(numberIndex)
                        : "";

                s.append(
                        name == null ? "" : name);

                s.append("\n");

                s.append(
                        phone == null ? "" : phone);

                s.append("\n\n");

                count++;

                if (count >= 300) {
                    break;
                }
            }

        } finally {
            cursor.close();
        }

        if (s.length() == 0) {
            s.append("Aucun contact trouvé.");
        }

        resultat.setText(s.toString());
    }

    private void showHistorique() {

        content.removeAllViews();

        content.addView(
                uiTitle("🕘 Historique"));

        content.addView(
                uiSubtitle(
                        "Dernières opérations"));

        TextView info =
                uiText(
                        "Retrouvez ici les opérations enregistrées " +
                        "par CASH POINT.",
                        15,
                        UI_MUTED,
                        false);

        info.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        info.setBackground(
                uiBackground(
                        Color.rgb(239, 243, 246),
                        16));

        content.addView(info);

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        infoParams.setMargins(
                0,
                dpInt(10),
                0,
                dpInt(10));

        info.setLayoutParams(infoParams);

        TextView resultat =
                uiText(
                        "",
                        15,
                        UI_TEXT,
                        false);

        resultat.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        resultat.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        content.addView(resultat);

        afficherHistorique(resultat);

        // =====================================================
        // ADD-ONLY: SMS M'VOLA / YAS HISTORY + FEE SEARCH
        // =====================================================

        TextView smsTitle =
                uiTitle("📩 SMS M’VOLA / YAS");

        content.addView(smsTitle);

        TextView smsInfo =
                uiSubtitle(
                        "Transactions reçues automatiquement par SMS");

        content.addView(smsInfo);

        final EditText rechercheSms =
                new EditText(this);

        rechercheSms.setHint(
                "🔎 Rechercher numéro, nom, référence...");

        rechercheSms.setSingleLine(true);
        styleInput(rechercheSms);

        content.addView(
                rechercheSms,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout filtresSms =
                new LinearLayout(this);

        filtresSms.setOrientation(
                LinearLayout.HORIZONTAL);

        filtresSms.setGravity(
                Gravity.CENTER);

        Button tousSms =
                new Button(this);

        tousSms.setText("📋 Tous");
        styleButton(tousSms);

        Button avecFraisSms =
                new Button(this);

        avecFraisSms.setText("💰 Avec frais");
        styleButton(avecFraisSms);

        Button sansFraisSms =
                new Button(this);

        sansFraisSms.setText("🆓 Sans frais");
        styleButton(sansFraisSms);

        filtresSms.addView(
                tousSms,
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(52),
                        1));

        filtresSms.addView(
                avecFraisSms,
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(52),
                        1));

        filtresSms.addView(
                sansFraisSms,
                new LinearLayout.LayoutParams(
                        0,
                        dpInt(52),
                        1));

        content.addView(filtresSms);

        final TextView resultatSms =
                uiText(
                        "",
                        15,
                        UI_TEXT,
                        false);

        resultatSms.setPadding(
                dpInt(16),
                dpInt(16),
                dpInt(16),
                dpInt(16));

        resultatSms.setBackground(
                uiBackground(
                        UI_CARD,
                        18));

        LinearLayout.LayoutParams smsResultParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        smsResultParams.setMargins(
                0,
                dpInt(12),
                0,
                dpInt(14));

        content.addView(
                resultatSms,
                smsResultParams);

        final String[] filtreSms =
                {"TOUS"};

        Runnable refreshSms =
                () -> afficherHistoriqueSms(
                        resultatSms,
                        rechercheSms.getText().toString(),
                        filtreSms[0]);

        tousSms.setOnClickListener(v -> {
            filtreSms[0] = "TOUS";
            refreshSms.run();
        });

        avecFraisSms.setOnClickListener(v -> {
            filtreSms[0] = "AVEC_FRAIS";
            refreshSms.run();
        });

        sansFraisSms.setOnClickListener(v -> {
            filtreSms[0] = "SANS_FRAIS";
            refreshSms.run();
        });

        rechercheSms.setOnEditorActionListener(
                (v, actionId, event) -> {
                    refreshSms.run();
                    return false;
                });

        rechercheSms.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {
                        refreshSms.run();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                });

        refreshSms.run();

        Button effacer =
                button("🗑️ Effacer l'historique");

        styleButton(effacer);

        LinearLayout.LayoutParams effacerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        effacerParams.setMargins(
                0,
                dpInt(14),
                0,
                dpInt(10));

        content.addView(
                effacer,
                effacerParams);

        effacer.setOnClickListener(
                v -> {

                    historique.clear();

                    afficherHistorique(resultat);

                    toast("Historique effacé.");
                });
    }

    private void afficherHistorique(
            TextView resultat) {

        if (historique.isEmpty()) {

            resultat.setText(
                    "Aucune opération enregistrée.");

            return;
        }

        StringBuilder s =
                new StringBuilder();

        for (int i = historique.size() - 1;
             i >= 0;
             i--) {

            s.append("• ")
                    .append(historique.get(i))
                    .append("\n\n");
        }

        resultat.setText(s.toString());
    }


    // =========================================================
    // ADD-ONLY SMS HISTORY
    // =========================================================
    private void afficherHistoriqueSms(
            TextView resultat,
            String recherche,
            String filtre) {

        try {

            android.content.SharedPreferences prefs =
                    getSharedPreferences(
                            "MVOLA_SMS_TRANSACTIONS",
                            MODE_PRIVATE);

            String json =
                    prefs.getString(
                            "transactions",
                            "[]");

            JSONArray array =
                    new JSONArray(json);

            String query =
                    recherche == null
                            ? ""
                            : recherche
                                .trim()
                                .toLowerCase(
                                    java.util.Locale.ROOT);

            StringBuilder s =
                    new StringBuilder();

            int count = 0;

            for (int i = 0;
                 i < array.length();
                 i++) {

                JSONObject o =
                        array.optJSONObject(i);

                if (o == null) {
                    continue;
                }

                long frais =
                        o.optLong("frais", 0);

                boolean avecFrais =
                        frais > 0;

                if ("AVEC_FRAIS".equals(filtre)
                        && !avecFrais) {
                    continue;
                }

                if ("SANS_FRAIS".equals(filtre)
                        && avecFrais) {
                    continue;
                }

                String type =
                        o.optString("type", "");

                String nom =
                        o.optString("nom", "");

                String numero =
                        o.optString("numero", "");

                String reference =
                        o.optString(
                                "reference",
                                "");

                String montant =
                        formatAr(
                                o.optLong(
                                        "montant",
                                        0));

                String fraisText =
                        formatAr(frais);

                String bonus =
                        formatAr(
                                o.optLong(
                                        "bonus",
                                        0));

                String solde =
                        formatAr(
                                o.optLong(
                                        "solde",
                                        0));

                String date =
                        o.optString(
                                "date",
                                "");

                String heure =
                        o.optString(
                                "heure",
                                "");

                String raison =
                        o.optString(
                                "raison",
                                "");

                String searchable =
                        (type + " " +
                         nom + " " +
                         numero + " " +
                         reference + " " +
                         raison + " " +
                         montant + " " +
                         fraisText)
                        .toLowerCase(
                                java.util.Locale.ROOT);

                if (!query.isEmpty()
                        && !searchable.contains(query)) {
                    continue;
                }

                s.append("━━━━━━━━━━━━━━━━━━\n");
                s.append("📩 ")
                        .append(type)
                        .append("\n");

                if (!nom.isEmpty()) {
                    s.append("👤 Nom : ")
                            .append(nom)
                            .append("\n");
                }

                if (!numero.isEmpty()) {
                    s.append("📱 Numéro : ")
                            .append(numero)
                            .append("\n");
                }

                s.append("💵 Montant : ")
                        .append(montant)
                        .append(" Ar\n");

                if (avecFrais) {
                    s.append("💰 Frais : ")
                            .append(fraisText)
                            .append(" Ar")
                            .append(" — AVEC FRAIS\n");
                } else {
                    s.append("🆓 Frais : 0 Ar")
                            .append(" — SANS FRAIS\n");
                }

                s.append("🎁 Bonus : ")
                        .append(bonus)
                        .append(" Ar\n");

                s.append("💳 Solde : ")
                        .append(solde)
                        .append(" Ar\n");

                if (!reference.isEmpty()) {
                    s.append("🔖 Référence : ")
                            .append(reference)
                            .append("\n");
                }

                if (!raison.isEmpty()) {
                    s.append("📝 Raison : ")
                            .append(raison)
                            .append("\n");
                }

                if (!date.isEmpty()
                        || !heure.isEmpty()) {

                    s.append("🕘 ")
                            .append(date);

                    if (!heure.isEmpty()) {
                        s.append(" ")
                                .append(heure);
                    }

                    s.append("\n");
                }

                count++;

                if (count >= 100) {
                    break;
                }

                s.append("\n");
            }

            if (count == 0) {

                if (query.isEmpty()) {
                    s.append(
                            "Aucune transaction SMS " +
                            "correspondante.");
                } else {
                    s.append(
                            "Aucun SMS trouvé pour : ")
                            .append(recherche);
                }
            } else {

                s.insert(
                        0,
                        "📊 " +
                        count +
                        " transaction(s) SMS\n\n");
            }

            resultat.setText(
                    s.toString());

        } catch (Exception e) {

            resultat.setText(
                    "Impossible de lire " +
                    "l'historique SMS.");

            e.printStackTrace();
        }
    }

    private String formatAr(long montant) {

        return String.format(
                java.util.Locale.getDefault(),
                "%,d",
                montant)
                .replace(',', ' ');
    }

    private void ajouterHistorique(
            String texte) {

        historique.add(texte);

        while (historique.size() > 100) {
            historique.remove(0);
        }
    }

    private void appelerUSSD(
            String code) {

        try {

            String encoded =
                    Uri.encode(code);

            Intent intent =
                    new Intent(
                            Intent.ACTION_CALL,
                            Uri.parse("tel:" + encoded));

            if (android.os.Build.VERSION.SDK_INT >= 23 &&
                    checkSelfPermission(
                            Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.CALL_PHONE
                        },
                        REQUEST_CALL);

                return;
            }

            startActivity(intent);

        } catch (Exception e) {

            try {

                Intent dial =
                        new Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse(
                                        "tel:" +
                                        Uri.encode(code)));

                startActivity(dial);

            } catch (Exception ignored) {

                toast("Impossible d'ouvrir le téléphone.");
            }
        }
    }

    private EditText input(
            String hint) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(16);
        e.setSingleLine(true);

        e.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(56));

        p.setMargins(
                0,
                dp(5),
                0,
                dp(8));

        e.setLayoutParams(p);

        return e;
    }

    private TextView result() {

        TextView t =
                new TextView(this);

        t.setTextSize(16);
        t.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        p.setMargins(
                0,
                dp(6),
                0,
                dp(10));

        t.setLayoutParams(p);

        return t;
    }

    private Button button(
            String text) {

        Button b =
                new Button(this);

        b.setText(text);
        b.setTextSize(14);
        b.setAllCaps(false);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        p.setMargins(
                0,
                dp(4),
                0,
                dp(4));

        b.setLayoutParams(p);

        return b;
    }

    private void section(
            String text) {

        TextView t =
                new TextView(this);

        t.setText(text);
        t.setTextSize(22);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(
                0,
                dp(12),
                0,
                dp(8));

        content.addView(t);
    }

    private void info(
            String text) {

        TextView t =
                new TextView(this);

        t.setText(text);
        t.setTextSize(15);
        t.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        -2);

        p.setMargins(
                0,
                0,
                0,
                dp(10));

        t.setLayoutParams(p);

        content.addView(t);
    }

    private LinearLayout.LayoutParams poids() {

        return new LinearLayout.LayoutParams(
                0,
                -2,
                1f);
    }

    private String digits(
            String value) {

        if (value == null) {
            return "";
        }

        return value.replaceAll(
                "[^0-9]",
                "");
    }

    private String cleanNumber(
            String value) {

        if (value == null) {
            return "";
        }

        String n =
                value.replaceAll(
                        "[^0-9+]",
                        "");

        return n.replace(
                "+",
                "");
    }

    private int number(
            EditText e) {

        String s =
                digits(e.getText().toString());

        if (s.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {
            return 0;
        }
    }

    private String format(
            int n) {

        return String.format(
                Locale.FRANCE,
                "%,d",
                n).replace(
                ',',
                ' ');
    }

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return (int)
                (value * density + 0.5f);
    }

    private void toast(
            String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults);

        if (requestCode == REQUEST_CONTACTS) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                showContacts();
            } else {

                toast(
                        "Permission Contacts refusée.");
            }
        }

        if (requestCode == REQUEST_CALL) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {

                toast(
                        "Permission téléphone accordée. Relance l'opération.");
            } else {

                toast(
                        "Permission téléphone refusée.");
            }
        }
    }

    @Override
    public void onBackPressed() {

        showHome();
    }
}
