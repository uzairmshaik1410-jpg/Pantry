package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.db.DatabaseHelper;
import com.richfield.smartpantry.util.UnitConverter;

/** Settings: expiry alerts, ignore-expired matching, default unit, and clearing the pantry. */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));

        final AppPreferences prefs = new AppPreferences(this);

        MaterialSwitch swAlerts = findViewById(R.id.sw_alerts);
        swAlerts.setChecked(prefs.isExpiryAlertsEnabled());
        swAlerts.setOnCheckedChangeListener((b, checked) -> prefs.setExpiryAlertsEnabled(checked));

        MaterialSwitch swIgnore = findViewById(R.id.sw_ignore_expired);
        swIgnore.setChecked(prefs.isIgnoreExpired());
        swIgnore.setOnCheckedChangeListener((b, checked) -> prefs.setIgnoreExpired(checked));

        MaterialAutoCompleteTextView etUnit = findViewById(R.id.et_default_unit);
        etUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, UnitConverter.UNITS));
        etUnit.setText(prefs.getDefaultUnit(), false);
        etUnit.setOnItemClickListener((parent, view, position, id) ->
                prefs.setDefaultUnit(UnitConverter.UNITS[position]));

        Button btnClear = findViewById(R.id.btn_clear_pantry);
        btnClear.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Clear pantry?")
                .setMessage("All your pantry ingredients will be deleted. Recipes are not affected.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Clear", (d, w) -> {
                    DatabaseHelper.getInstance(this).deleteAllPantryItems();
                    Toast.makeText(this, "Pantry cleared", Toast.LENGTH_SHORT).show();
                })
                .show());

        setupBottomNav(R.id.nav_settings);
    }
}
