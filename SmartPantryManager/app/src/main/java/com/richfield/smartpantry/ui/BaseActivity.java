package com.richfield.smartpantry.ui;

import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.richfield.smartpantry.R;

/** Shared bottom-navigation behaviour for the three top-level screens. */
public abstract class BaseActivity extends AppCompatActivity {

    /** Wires the bottom navigation bar; each tab is opened with an explicit Intent. */
    protected void setupBottomNav(final int selectedId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;

            Class<?> target;
            if (id == R.id.nav_recipes) target = SuggestedRecipesActivity.class;
            else if (id == R.id.nav_settings) target = SettingsActivity.class;
            else target = PantryActivity.class;

            Intent intent = new Intent(this, target);
            if (target == PantryActivity.class) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            startActivity(intent);
            overridePendingTransition(0, 0);
            // Pantry is the root screen; other tabs close themselves so Back always returns to it.
            if (selectedId != R.id.nav_pantry) finish();
            return true;
        });
    }

    /** Enables the toolbar back arrow on detail-style screens. */
    protected void enableBackArrow() {
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
