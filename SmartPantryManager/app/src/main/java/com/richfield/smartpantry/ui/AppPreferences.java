package com.richfield.smartpantry.ui;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the Settings screen. */
public class AppPreferences {
    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public boolean isExpiryAlertsEnabled() { return prefs.getBoolean(KEY_EXPIRY_ALERTS, true); }
    public void setExpiryAlertsEnabled(boolean v) { prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, v).apply(); }

    public boolean isIgnoreExpired() { return prefs.getBoolean(KEY_IGNORE_EXPIRED, true); }
    public void setIgnoreExpired(boolean v) { prefs.edit().putBoolean(KEY_IGNORE_EXPIRED, v).apply(); }

    public String getDefaultUnit() { return prefs.getString(KEY_DEFAULT_UNIT, "g"); }
    public void setDefaultUnit(String u) { prefs.edit().putString(KEY_DEFAULT_UNIT, u).apply(); }
}
