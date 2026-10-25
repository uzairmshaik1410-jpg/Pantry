package com.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.richfield.smartpantry.R;
import com.richfield.smartpantry.db.DatabaseHelper;
import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.util.RecipeMatcher;

import java.util.List;

/**
 * Runs the strict-matching rule against the current pantry. The main list contains ONLY recipes
 * with every ingredient available. The optional "Almost there" tab is a clearly separate list.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private AppPreferences prefs;
    private RecipeAdapter adapter;
    private TextView tvEmpty, tvSummary;
    private MaterialButton btnReady, btnAlmost;
    private boolean showAlmost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));

        db = DatabaseHelper.getInstance(this);
        prefs = new AppPreferences(this);
        tvEmpty = findViewById(R.id.tv_empty);
        tvSummary = findViewById(R.id.tv_summary);
        btnReady = findViewById(R.id.btn_ready);
        btnAlmost = findViewById(R.id.btn_almost);

        RecyclerView rv = findViewById(R.id.rv_recipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        rv.setAdapter(adapter);

        MaterialButtonToggleGroup toggle = findViewById(R.id.toggle_mode);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            showAlmost = (checkedId == R.id.btn_almost);
            loadRecipes();
        });

        setupBottomNav(R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void loadRecipes() {
        List<Recipe> recipes = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();
        boolean ignoreExpired = prefs.isIgnoreExpired();

        List<MatchResult> strict = RecipeMatcher.findStrictMatches(recipes, pantry, ignoreExpired);
        List<MatchResult> almost = RecipeMatcher.findAlmostThere(recipes, pantry, ignoreExpired);

        btnReady.setText("Ready to cook (" + strict.size() + ")");
        btnAlmost.setText("Almost there (" + almost.size() + ")");

        List<MatchResult> shown = showAlmost ? almost : strict;
        adapter.submit(shown);

        if (showAlmost) {
            tvSummary.setText("Missing exactly one ingredient. These are NOT in your ready-to-cook list.");
        } else {
            tvSummary.setText(strict.size() + (strict.size() == 1 ? " recipe" : " recipes")
                    + " you can cook right now");
        }

        if (shown.isEmpty()) {
            if (showAlmost) {
                tvEmpty.setText("No recipes are missing exactly one ingredient right now.");
            } else if (pantry.isEmpty()) {
                tvEmpty.setText("Your pantry is empty.\nAdd some ingredients to see recipes.");
            } else {
                tvEmpty.setText("No recipes match your pantry yet \u2013 add more ingredients.");
            }
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
