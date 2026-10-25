package com.richfield.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/** The outcome of checking one recipe against the pantry. */
public class MatchResult {
    private final Recipe recipe;
    private final List<RecipeIngredient> missing = new ArrayList<>();

    public MatchResult(Recipe recipe) { this.recipe = recipe; }

    public Recipe getRecipe() { return recipe; }
    public List<RecipeIngredient> getMissing() { return missing; }
    /** True only when every required ingredient is available in the required quantity. */
    public boolean isStrictMatch() { return missing.isEmpty(); }
}
