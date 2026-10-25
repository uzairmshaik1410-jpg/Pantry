package com.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.db.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.AppDateUtils;
import com.richfield.smartpantry.util.Format;

import java.util.List;

/** Launcher screen: lists the pantry (READ), opens the form for CREATE / UPDATE, and handles DELETE. */
public class PantryActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper db;
    private AppPreferences prefs;
    private PantryAdapter adapter;
    private TextView tvEmpty, tvBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));

        db = DatabaseHelper.getInstance(this);
        prefs = new AppPreferences(this);
        tvEmpty = findViewById(R.id.tv_empty);
        tvBanner = findViewById(R.id.tv_banner);

        RecyclerView rv = findViewById(R.id.rv_pantry);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditIngredientActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    /** Reload every time the screen comes back so changes made elsewhere are visible. */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.submit(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        int expiring = 0;
        for (PantryItem p : items) {
            if (AppDateUtils.isExpiringSoon(p.getExpiryDate())) expiring++;
        }
        if (prefs.isExpiryAlertsEnabled() && expiring > 0) {
            tvBanner.setText("\u26A0 " + expiring + (expiring == 1 ? " item is" : " items are")
                    + " expiring within " + AppDateUtils.EXPIRING_SOON_DAYS + " days");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(final PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + Format.capitalize(item.getName()) + " from your pantry?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .show();
    }
}
