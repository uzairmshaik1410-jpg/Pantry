package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.db.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.util.AppDateUtils;
import com.richfield.smartpantry.util.UnitConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Form used for both creating a new pantry item and editing an existing one, with validation. */
public class AddEditIngredientActivity extends BaseActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private DatabaseHelper db;
    private TextInputLayout tilName, tilQty, tilUnit;
    private MaterialAutoCompleteTextView etName, etUnit;
    private TextInputEditText etQty, etExpiry;
    private long itemId = -1;
    private String expiryIso = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        enableBackArrow();

        db = DatabaseHelper.getInstance(this);
        tilName = findViewById(R.id.til_name);
        tilQty = findViewById(R.id.til_qty);
        tilUnit = findViewById(R.id.til_unit);
        etName = findViewById(R.id.et_name);
        etQty = findViewById(R.id.et_qty);
        etUnit = findViewById(R.id.et_unit);
        etExpiry = findViewById(R.id.et_expiry);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnDelete = findViewById(R.id.btn_delete);
        Button btnClearDate = findViewById(R.id.btn_clear_date);

        // Ingredient names from the recipe collection are offered as suggestions, which keeps
        // the pantry vocabulary close to what the recipes use.
        etName.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                db.getKnownIngredientNames()));
        etUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, UnitConverter.UNITS));

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            PantryItem item = db.getPantryItem(itemId);
            if (item == null) {
                finish();
                return;
            }
            toolbar.setTitle("Edit ingredient");
            etName.setText(item.getName());
            etQty.setText(com.richfield.smartpantry.util.Format.quantity(item.getQuantity()));
            etUnit.setText(item.getUnit(), false);
            if (item.getExpiryDate() != null) {
                expiryIso = item.getExpiryDate();
                etExpiry.setText(AppDateUtils.formatDisplay(expiryIso));
            }
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            toolbar.setTitle("Add ingredient");
            etUnit.setText(new AppPreferences(this).getDefaultUnit(), false);
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> {
            expiryIso = null;
            etExpiry.setText("");
        });
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void showDatePicker() {
        SimpleDateFormat utc = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
        long initial = MaterialDatePicker.todayInUtcMilliseconds();
        if (expiryIso != null) {
            try {
                Date d = utc.parse(expiryIso);
                if (d != null) initial = d.getTime();
            } catch (ParseException ignored) { }
        }
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Expiry date")
                .setSelection(initial)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            expiryIso = utc.format(new Date(selection));
            etExpiry.setText(AppDateUtils.formatDisplay(expiryIso));
        });
        picker.show(getSupportFragmentManager(), "expiry_picker");
    }

    /** Validates every field; shows an inline error under each invalid field. */
    private boolean validate() {
        boolean ok = true;

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        if (name.isEmpty()) {
            tilName.setError("Please enter an ingredient name");
            ok = false;
        } else if (name.length() < 2 || name.length() > 40) {
            tilName.setError("Name must be 2 to 40 characters");
            ok = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            tilName.setError("Name must contain letters");
            ok = false;
        } else {
            tilName.setError(null);
        }

        String qtyText = etQty.getText() == null ? "" : etQty.getText().toString().trim();
        if (qtyText.isEmpty()) {
            tilQty.setError("Enter a quantity");
            ok = false;
        } else {
            try {
                double q = Double.parseDouble(qtyText);
                if (q <= 0) {
                    tilQty.setError("Must be greater than 0");
                    ok = false;
                } else if (q > 100000) {
                    tilQty.setError("Too large");
                    ok = false;
                } else {
                    tilQty.setError(null);
                }
            } catch (NumberFormatException e) {
                tilQty.setError("Enter a valid number");
                ok = false;
            }
        }

        String unit = etUnit.getText() == null ? "" : etUnit.getText().toString().trim();
        if (!UnitConverter.isValidUnit(unit)) {
            tilUnit.setError("Choose a unit");
            ok = false;
        } else {
            tilUnit.setError(null);
        }
        return ok;
    }

    private void save() {
        if (!validate()) return;

        PantryItem item = new PantryItem();
        item.setName(etName.getText().toString().trim());
        item.setQuantity(Double.parseDouble(etQty.getText().toString().trim()));
        item.setUnit(etUnit.getText().toString().trim());
        item.setExpiryDate(expiryIso);

        if (itemId == -1) {
            db.addPantryItem(item);                       // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            item.setId(itemId);
            db.updatePantryItem(item);                    // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete ingredient?")
                .setMessage("This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    db.deletePantryItem(itemId);          // DELETE
                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }
}
