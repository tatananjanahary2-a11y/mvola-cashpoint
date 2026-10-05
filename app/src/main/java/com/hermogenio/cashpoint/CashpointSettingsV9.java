package com.hermogenio.cashpoint;

import android.content.Context;
import android.content.SharedPreferences;

public class CashpointSettingsV9 {

    private static final String PREFS = "CASHPOINT_SETTINGS_V8";

    private final SharedPreferences p;

    public CashpointSettingsV9(Context context) {
        p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getAgentName() {
        return p.getString("agent_name", "");
    }

    public void setAgentName(String value) {
        p.edit().putString("agent_name", value == null ? "" : value).apply();
    }

    public boolean isDarkMode() {
        return p.getBoolean("dark_mode", false);
    }

    public void setDarkMode(boolean value) {
        p.edit().putBoolean("dark_mode", value).apply();
    }

    public boolean isPinEnabled() {
        return p.getBoolean("pin_enabled", false);
    }

    public void setPinEnabled(boolean value) {
        p.edit().putBoolean("pin_enabled", value).apply();
    }

    public void setPin(String pin) {
        p.edit().putString("pin_code", pin == null ? "" : pin).apply();
    }

    public boolean verifyPin(String pin) {
        String saved = p.getString("pin_code", "");
        return saved.length() > 0 && saved.equals(pin);
    }
}
