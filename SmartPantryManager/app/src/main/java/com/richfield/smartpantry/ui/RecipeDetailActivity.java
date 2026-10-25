package com.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.db.DatabaseHelper;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.Format;
import com.richfield.smartpantry.util.RecipeMatcher;

import java.util.List;
import java.util.Map;

/** Shows the full ingredient list (with a tick/cross against the pantry) and the method. */
public class RecipeDetailActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));
        enableBackArrow();

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Recipe recipe = db.getRecipe(getIntent().getLongExtra(EXTRA_RECIPE_ID, -1));
        if (recipe == null) {
            finish();
            return;
        }
        getSupportActionBar().setTitle("Recipe");

        List<PantryItem> pantry = db.getAllPantryItems();
        Map<String, Double> totals = RecipeMatcher.buildPantryTotals(pantry,
                new AppPreferences(this).isIgnoreExpired());

        ((TextView) findViewById(R.id.tv_name)).setText(recipe.getName());
        ((TextView) findViewById(R.id.tv_meta)).setText(recipe.getPrepTimeMinutes() + " min  \u2022  "
                + recipe.getServings() + (recipe.getServings() == 1 ? " serving" : " servings"));
        ((TextView) findViewById(R.id.tv_description)).setText(recipe.getDescription());

        LinearLayout llIngredients = findViewById(R.id.ll_ingredients);
        int missing = 0;
        for (RecipeIngredient ing : recipe.getIngredients()) {
            boolean have = RecipeMatcher.hasIngredient(totals, ing);
            if (!have) missing++;
            TextView row = makeRow((have ? "\u2713  " : "\u2717  ")
                    + Format.quantity(ing.getQuantity()) + " " + ing.getUnit() + "  "
                    + Format.capitalize(ing.getName()));
            row.setTextColor(ContextCompat.getColor(this, have ? R.color.green_primary : R.color.red));
            llIngredients.addView(row);
        }

        TextView tvStatus = findViewById(R.id.tv_status);
        if (missing == 0) {
            tvStatus.setText("\u2713 Your pantry has everything for this recipe");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
        } else {
            tvStatus.setText(missing + (missing == 1 ? " ingredient is" : " ingredients are")
                    + " missing from your pantry");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }

        LinearLayout llSteps = findViewById(R.id.ll_steps);
        int n = 1;
        for (String step : recipe.getStepList()) {
            TextView row = makeRow(n++ + ".  " + step);
            row.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            llSteps.addView(row);
        }
    }

    private TextView makeRow(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setGravity(Gravity.START);
        tv.setLineSpacing(0f, 1.15f);
        int pad = (int) (6 * getResources().getDisplayMetrics().density);
        tv.setPadding(0, pad, 0, pad);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return tv;
    }
}
