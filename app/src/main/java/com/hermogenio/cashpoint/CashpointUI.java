package com.hermogenio.cashpoint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class CashpointUI {

    private CashpointUI() {}

    // =========================================================
    // CASHPOINT V7 — FINTECH DESIGN SYSTEM
    // =========================================================

    public static final int AUBERGINE =
            Color.rgb(55, 18, 72);

    public static final int AUBERGINE_DARK =
            Color.rgb(38, 12, 50);

    public static final int AUBERGINE_LIGHT =
            Color.rgb(91, 45, 108);

    public static final int ORANGE =
            Color.rgb(255, 111, 32);

    public static final int ORANGE_DARK =
            Color.rgb(225, 82, 18);

    public static final int WHITE =
            Color.WHITE;

    public static final int BACKGROUND =
            Color.rgb(247, 245, 249);

    public static final int CARD =
            Color.WHITE;

    public static final int TEXT =
            Color.rgb(35, 29, 39);

    public static final int MUTED =
            Color.rgb(115, 107, 120);

    public static final int BORDER =
            Color.rgb(229, 224, 232);

    public static GradientDrawable bg(
            int color,
            float radius
    ) {
        GradientDrawable d =
                new GradientDrawable();

        d.setColor(color);
        d.setCornerRadius(radius);

        return d;
    }

    public static GradientDrawable stroke(
            int color,
            int border,
            int width,
            float radius
    ) {
        GradientDrawable d =
                new GradientDrawable();

        d.setColor(color);
        d.setStroke(width, border);
        d.setCornerRadius(radius);

        return d;
    }

    public static void page(View view) {
        if (view != null) {
            view.setBackgroundColor(BACKGROUND);
        }
    }

    public static void card(View view) {
        if (view == null) return;

        view.setBackground(
                stroke(
                        CARD,
                        BORDER,
                        1,
                        28
                )
        );

        view.setElevation(3f);
    }

    public static void primaryButton(
            TextView button
    ) {
        if (button == null) return;

        button.setTextColor(WHITE);
        button.setGravity(Gravity.CENTER);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setPadding(
                18,
                14,
                18,
                14
        );

        button.setBackground(
                bg(
                        ORANGE,
                        24
                )
        );

        button.setElevation(3f);
    }

    public static void secondaryButton(
            TextView button
    ) {
        if (button == null) return;

        button.setTextColor(
                AUBERGINE
        );

        button.setGravity(Gravity.CENTER);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setPadding(
                18,
                14,
                18,
                14
        );

        button.setBackground(
                stroke(
                        WHITE,
                        BORDER,
                        1,
                        24
                )
        );
    }

    public static TextView text(
            Context context,
            String value,
            float size
    ) {
        TextView t =
                new TextView(context);

        t.setText(value);
        t.setTextColor(TEXT);
        t.setTextSize(size);

        return t;
    }

    public static TextView title(
            Context context,
            String value
    ) {
        TextView t =
                text(
                        context,
                        value,
                        22
                );

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        return t;
    }

    public static TextView subtitle(
            Context context,
            String value
    ) {
        TextView t =
                text(
                        context,
                        value,
                        13
                );

        t.setTextColor(MUTED);

        return t;
    }

    public static TextView amount(
            Context context,
            String value
    ) {
        TextView t =
                text(
                        context,
                        value,
                        28
                );

        t.setTextColor(WHITE);

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        return t;
    }

    public static LinearLayout card(
            Context context
    ) {
        LinearLayout l =
                new LinearLayout(context);

        l.setOrientation(
                LinearLayout.VERTICAL
        );

        l.setPadding(
                18,
                18,
                18,
                18
        );

        l.setBackground(
                stroke(
                        CARD,
                        BORDER,
                        1,
                        28
                )
        );

        l.setElevation(3f);

        return l;
    }

    public static LinearLayout header(
            Context context,
            String titleText,
            String subtitleText
    ) {
        LinearLayout h =
                new LinearLayout(context);

        h.setOrientation(
                LinearLayout.VERTICAL
        );

        h.setPadding(
                22,
                20,
                22,
                20
        );

        h.setBackground(
                bg(
                        AUBERGINE,
                        0
                )
        );

        TextView title =
                title(
                        context,
                        titleText
                );

        title.setTextColor(WHITE);

        TextView subtitle =
                subtitle(
                        context,
                        subtitleText
                );

        subtitle.setTextColor(
                Color.rgb(
                        225,
                        213,
                        231
                )
        );

        h.addView(title);

        h.addView(
                subtitle,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        return h;
    }

    public static LinearLayout quickAction(
            Context context
    ) {
        LinearLayout c =
                card(context);

        c.setClickable(true);
        c.setFocusable(true);

        return c;
    }

    public static TextView icon(
            Context context,
            String value
    ) {
        TextView t =
                new TextView(context);

        t.setText(value);
        t.setTextSize(26);
        t.setGravity(Gravity.CENTER);

        return t;
    }

    public static TextView actionTitle(
            Context context,
            String value
    ) {
        TextView t =
                text(
                        context,
                        value,
                        15
                );

        t.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        return t;
    }

    public static TextView actionInfo(
            Context context,
            String value
    ) {
        TextView t =
                subtitle(
                        context,
                        value
                );

        t.setPadding(
                0,
                4,
                0,
                0
        );

        return t;
    }

    // =========================================================
    // V7 — STYLE AUTOMATIQUE DES ECRANS EXISTANTS
    // UI ONLY — aucune logique métier
    // =========================================================

    
public static void styleExistingScreen(
        android.view.View root
) {
    if (root == null) {
        return;
    }

    try {
        android.content.Context context =
                root.getContext();

        boolean dark =
                false;

        styleExistingScreen(
                context,
                root,
                dark
        );

    } catch (Throwable ignored) {
    }
}

public static void styleExistingScreen(
        android.content.Context context,
        android.view.View root,
        boolean dark
) {
    if (root == null) {
        return;
    }

    int pageBg =
            dark
                    ? Color.rgb(25, 18, 29)
                    : BACKGROUND;

    int cardBg =
            dark
                    ? Color.rgb(48, 36, 53)
                    : WHITE;

    int fg =
            dark
                    ? Color.WHITE
                    : TEXT;

    int muted =
            dark
                    ? Color.rgb(205, 195, 210)
                    : MUTED;

    int border =
            dark
                    ? Color.rgb(90, 72, 96)
                    : BORDER;

    root.setBackgroundColor(pageBg);

    applyGlobalTheme(
            root,
            dark,
            pageBg,
            cardBg,
            fg,
            muted,
            border
    );
}


private static android.graphics.drawable.Drawable createThemeBackground(
        int color,
        int radius
) {
    android.graphics.drawable.GradientDrawable drawable =
            new android.graphics.drawable.GradientDrawable();

    drawable.setColor(color);
    drawable.setCornerRadius(radius);

    return drawable;
}

private static void applyGlobalTheme(
        android.view.View view,
        boolean dark,
        int pageBg,
        int cardBg,
        int fg,
        int muted,
        int border
) {
    if (view == null) {
        return;
    }

    try {

        Object tag = view.getTag();

        boolean header =
                "CASHPOINT_HEADER".equals(tag);

        boolean card =
                "CASHPOINT_CARD".equals(tag);

        boolean button =
                view instanceof android.widget.Button;

        if (header) {

            view.setBackground(
                    createThemeBackground(
                            dark
                                    ? AUBERGINE_DARK
                                    : AUBERGINE,
                            20
                    )
            );

        } else if (card) {

            view.setBackground(
                    createThemeBackground(
                            cardBg,
                            18
                    )
            );

        } else if (
                view instanceof android.widget.ScrollView
        ) {

            view.setBackgroundColor(pageBg);
        }

        if (
                view instanceof android.widget.TextView
        ) {

            android.widget.TextView tv =
                    (android.widget.TextView) view;

            if (header) {

                tv.setTextColor(Color.WHITE);

            } else if (button) {

                tv.setTextColor(
                        dark
                                ? Color.WHITE
                                : TEXT
                );

            } else {

                tv.setTextColor(fg);
            }
        }

        if (
                view instanceof android.widget.EditText
        ) {

            android.widget.EditText edit =
                    (android.widget.EditText) view;

            edit.setTextColor(fg);
            edit.setHintTextColor(muted);

            edit.setBackground(
                    stroke(
                            cardBg,
                            border,
                            1,
                            18
                    )
            );
        }

        if (
                view instanceof android.widget.CompoundButton
        ) {

            android.widget.CompoundButton cb =
                    (android.widget.CompoundButton) view;

            cb.setTextColor(fg);
        }

        if (
                view instanceof android.view.ViewGroup
        ) {

            android.view.ViewGroup group =
                    (android.view.ViewGroup) view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                applyGlobalTheme(
                        group.getChildAt(i),
                        dark,
                        pageBg,
                        cardBg,
                        fg,
                        muted,
                        border
                );
            }
        }

    } catch (Throwable ignored) {
    }
}


    private static void styleTree(
            android.view.View view
    ) {
        if (view == null) {
            return;
        }

        // -----------------------------------------------------
        // TEXT
        // -----------------------------------------------------

        if (view instanceof android.widget.TextView) {

            android.widget.TextView tv =
                    (android.widget.TextView) view;

            String value =
                    tv.getText() == null
                            ? ""
                            : tv.getText().toString()
                              .trim();

            String lower =
                    value.toLowerCase(
                            java.util.Locale.ROOT
                    );

            // Boutons
            if (view instanceof android.widget.Button) {

                if (
                        lower.contains("annul") ||
                        lower.contains("retour") ||
                        lower.contains("fermer")
                ) {
                    secondaryButton(tv);
                } else {
                    primaryButton(tv);
                }

                tv.setMinHeight(52);
            }

            // Champs / titres de navigation
            else if (
                    lower.equals("transfert") ||
                    lower.equals("dépôt") ||
                    lower.equals("depot") ||
                    lower.equals("crédit") ||
                    lower.equals("credit") ||
                    lower.equals("offres") ||
                    lower.equals("contacts") ||
                    lower.equals("historique") ||
                    lower.contains("cash point")
            ) {
                tv.setTextColor(AUBERGINE);

                tv.setTypeface(
                        Typeface.DEFAULT,
                        Typeface.BOLD
                );
            }

            // Textes généraux
            else {
                tv.setTextColor(TEXT);
            }
        }

        // -----------------------------------------------------
        // INPUTS
        // -----------------------------------------------------

        if (
                view instanceof
                android.widget.EditText
        ) {
            android.widget.EditText edit =
                    (android.widget.EditText) view;

            edit.setTextColor(TEXT);
            edit.setHintTextColor(MUTED);

            edit.setPadding(
                    16,
                    14,
                    16,
                    14
            );

            edit.setBackground(
                    stroke(
                            WHITE,
                            BORDER,
                            1,
                            18
                    )
            );
        }

        // -----------------------------------------------------
        // CONTAINER
        // -----------------------------------------------------

        if (
                view instanceof
                android.widget.LinearLayout
        ) {
            android.widget.LinearLayout layout =
                    (android.widget.LinearLayout) view;

            layout.setClipToPadding(false);
        }

        // -----------------------------------------------------
        // RECURSION
        // -----------------------------------------------------

        if (
                view instanceof
                android.view.ViewGroup
        ) {
            android.view.ViewGroup group =
                    (android.view.ViewGroup) view;

            for (int i = 0;
                 i < group.getChildCount();
                 i++) {

                styleTree(
                        group.getChildAt(i)
                );
            }
        }
    }

}
