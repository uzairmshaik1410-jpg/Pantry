package com.richfield.smartpantry.util;

import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE CORE BUSINESS LOGIC (assignment section 2.3 - the strict-matching rule).
 *
 * A recipe is "suggested" only if EVERY ingredient it needs is in the pantry, in at least the
 * required quantity. 4 out of 5 ingredients means the recipe is NOT suggested.
 *
 * Robustness against messy real-world data:
 *  - names are normalised (case, whitespace, singular/plural: "tomatoes" == "tomato")
 *  - units are converted ("1 kg" satisfies "200 g", "2 tbsp" satisfies "30 ml")
 *  - several pantry rows of the same ingredient are added together
 *  - optionally, expired pantry items are ignored
 *
 * This class has no Android dependencies, so it is covered by plain JUnit tests.
 */
public final class RecipeMatcher {

    private static final double EPSILON = 1e-6;

    private RecipeMatcher() { }

    private static String key(String name, String unit) {
        return IngredientNormalizer.normalize(name) + "|" + UnitConverter.dimension(unit);
    }

    /** Sums the pantry per (normalised name, dimension), expressed in base units. */
    public static Map<String, Double> buildPantryTotals(List<PantryItem> items, boolean ignoreExpired) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem p : items) {
            if (ignoreExpired && AppDateUtils.isExpired(p.getExpiryDate())) continue;
            String k = key(p.getName(), p.getUnit());
            double base = UnitConverter.toBase(p.getQuantity(), p.getUnit());
            Double current = totals.get(k);
            totals.put(k, (current == null ? 0.0 : current) + base);
        }
        return totals;
    }

    /** True if the pantry totals contain at least the quantity this ingredient requires. */
    public static boolean hasIngredient(Map<String, Double> pantryTotals, RecipeIngredient needed) {
        Double have = pantryTotals.get(key(needed.getName(), needed.getUnit()));
        if (have == null) return false;
        return have + EPSILON >= UnitConverter.toBase(needed.getQuantity(), needed.getUnit());
    }

    public static MatchResult evaluate(Recipe recipe, Map<String, Double> pantryTotals) {
        MatchResult result = new MatchResult(recipe);
        for (RecipeIngredient ing : recipe.getIngredients()) {
            if (!hasIngredient(pantryTotals, ing)) result.getMissing().add(ing);
        }
        return result;
    }

    /** Recipes the user can cook right now: zero missing ingredients. Quickest recipes first. */
    public static List<MatchResult> findStrictMatches(List<Recipe> recipes, List<PantryItem> pantry,
                                                      boolean ignoreExpired) {
        Map<String, Double> totals = buildPantryTotals(pantry, ignoreExpired);
        List<MatchResult> out = new ArrayList<>();
        for (Recipe r : recipes) {
            if (r.getIngredients().isEmpty()) continue;
            MatchResult m = evaluate(r, totals);
            if (m.isStrictMatch()) out.add(m);
        }
        Collections.sort(out, new Comparator<MatchResult>() {
            @Override public int compare(MatchResult a, MatchResult b) {
                return Integer.compare(a.getRecipe().getPrepTimeMinutes(), b.getRecipe().getPrepTimeMinutes());
            }
        });
        return out;
    }

    /** Bonus list: recipes missing EXACTLY ONE ingredient. Kept separate from the strict list. */
    public static List<MatchResult> findAlmostThere(List<Recipe> recipes, List<PantryItem> pantry,
                                                    boolean ignoreExpired) {
        Map<String, Double> totals = buildPantryTotals(pantry, ignoreExpired);
        List<MatchResult> out = new ArrayList<>();
        for (Recipe r : recipes) {
            MatchResult m = evaluate(r, totals);
            if (m.getMissing().size() == 1) out.add(m);
        }
        return out;
    }
}
