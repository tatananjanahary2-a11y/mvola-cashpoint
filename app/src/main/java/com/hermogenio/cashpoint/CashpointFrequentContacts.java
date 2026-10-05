package com.hermogenio.cashpoint;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CashpointFrequentContacts {

    private static final String PREF =
            "CASHPOINT_FREQUENT_CONTACTS";

    private static final String KEY =
            "contacts";

    private final SharedPreferences prefs;

    public CashpointFrequentContacts(Context context) {
        prefs = context.getSharedPreferences(
                PREF,
                Context.MODE_PRIVATE
        );
    }

    private JSONArray load() {

        try {

            return new JSONArray(
                    prefs.getString(
                            KEY,
                            "[]"
                    )
            );

        } catch (Exception e) {

            return new JSONArray();
        }
    }

    private void save(JSONArray array) {

        prefs.edit()
                .putString(
                        KEY,
                        array.toString()
                )
                .apply();
    }

    public void enregistrer(
            String numero,
            String nom
    ) {

        numero = normaliserNumero(numero);

        if (numero.length() < 10) {
            return;
        }

        try {

            JSONArray array = load();

            for (int i = 0;
                    i < array.length();
                    i++) {

                JSONObject o =
                        array.getJSONObject(i);

                if (numero.equals(
                        o.optString("numero")
                )) {

                    o.put(
                            "frequence",
                            o.optInt(
                                    "frequence",
                                    0
                            ) + 1
                    );

                    if (nom != null
                            && !nom.trim().isEmpty()) {

                        o.put(
                                "nom",
                                nom.trim()
                        );
                    }

                    save(array);
                    return;
                }
            }

            JSONObject nouveau =
                    new JSONObject();

            nouveau.put(
                    "numero",
                    numero
            );

            nouveau.put(
                    "nom",
                    nom == null
                            ? ""
                            : nom.trim()
            );

            nouveau.put(
                    "frequence",
                    1
            );

            array.put(nouveau);

            save(array);

        } catch (Exception ignored) {
        }
    }

    public List<String> rechercher(
            String saisie
    ) {

        String q =
                normaliserNumero(saisie);

        List<JSONObject> result =
                new ArrayList<>();

        try {

            JSONArray array = load();

            for (int i = 0;
                    i < array.length();
                    i++) {

                JSONObject o =
                        array.getJSONObject(i);

                String numero =
                        o.optString(
                                "numero",
                                ""
                        );

                if (q.length() == 0
                        || numero.startsWith(q)) {

                    result.add(o);
                }
            }

        } catch (Exception ignored) {
        }

        Collections.sort(
                result,
                new Comparator<JSONObject>() {

                    @Override
                    public int compare(
                            JSONObject a,
                            JSONObject b
                    ) {

                        return Integer.compare(
                                b.optInt(
                                        "frequence",
                                        0
                                ),
                                a.optInt(
                                        "frequence",
                                        0
                                )
                        );
                    }
                }
        );

        List<String> sortie =
                new ArrayList<>();

        for (JSONObject o : result) {

            String numero =
                    o.optString(
                            "numero",
                            ""
                    );

            String nom =
                    o.optString(
                            "nom",
                            ""
                    );

            if (nom.isEmpty()) {

                sortie.add(numero);

            } else {

                sortie.add(
                        numero +
                        " — " +
                        nom
                );
            }
        }

        return sortie;
    }

    public void activerAutocomplete(
            AutoCompleteTextView input
    ) {

        input.setThreshold(1);

        input.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        List<String> suggestions =
                                rechercher(
                                        s.toString()
                                );

                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(
                                        input.getContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        suggestions
                                );

                        input.setAdapter(adapter);

                        if (!suggestions.isEmpty()) {
                            input.showDropDown();
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        input.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String valeur =
                            parent.getItemAtPosition(
                                    position
                            ).toString();

                    int separator =
                            valeur.indexOf(" — ");

                    if (separator > 0) {

                        valeur =
                                valeur.substring(
                                        0,
                                        separator
                                );
                    }

                    input.setText(valeur);

                    input.setSelection(
                            input.length()
                    );
                }
        );
    }

    public static String normaliserNumero(
            String numero
    ) {

        if (numero == null) {
            return "";
        }

        return numero
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .trim();
    }
}
