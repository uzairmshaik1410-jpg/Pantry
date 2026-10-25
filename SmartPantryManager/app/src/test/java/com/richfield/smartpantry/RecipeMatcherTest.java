package com.richfield.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.richfield.smartpantry.model.MatchResult;
import com.richfield.smartpantry.model.PantryItem;
import com.richfield.smartpantry.model.Recipe;
import com.richfield.smartpantry.model.RecipeIngredient;
import com.richfield.smartpantry.util.IngredientNormalizer;
import com.richfield.smartpantry.util.RecipeMatcher;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Tests for the strict-matching rule (assignment section 2.3). Run with: ./gradlew test */
public class RecipeMatcherTest {

    private Recipe recipe(String name, RecipeIngredient... ings) {
        Recipe r = new Recipe();
        r.setName(name);
        r.setPrepTimeMinutes(10);
        r.setServings(1);
        r.getIngredients().addAll(Arrays.asList(ings));
        return r;
    }

    private PantryItem item(String name, double qty, String unit) {
        return new PantryItem(0, name, qty, unit, null);
    }

    private final Recipe omelette = recipe("Omelette",
            new RecipeIngredient("egg", 3, "pcs"),
            new RecipeIngredient("cheese", 30, "g"),
            new RecipeIngredient("butter", 10, "g"),
            new RecipeIngredient("tomato", 1, "pcs"),
            new RecipeIngredient("salt", 1, "tsp"));

    @Test
    public void recipeWithAllIngredients_isSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                item("Eggs", 6, "pcs"), item("Cheese", 100, "g"), item("Butter", 250, "g"),
                item("Tomatoes", 2, "pcs"), item("Salt", 1, "tbsp"));
        List<MatchResult> r = RecipeMatcher.findStrictMatches(Arrays.asList(omelette), pantry, true);
        assertEquals(1, r.size());
    }

    @Test
    public void fiveIngredientsWithFourInPantry_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                item("Egg", 6, "pcs"), item("Cheese", 100, "g"), item("Butter", 250, "g"),
                item("Tomato", 2, "pcs"));               // salt missing
        assertTrue(RecipeMatcher.findStrictMatches(Arrays.asList(omelette), pantry, true).isEmpty());
    }

    @Test
    public void insufficientQuantity_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                item("Egg", 2, "pcs"), item("Cheese", 100, "g"), item("Butter", 250, "g"),
                item("Tomato", 2, "pcs"), item("Salt", 1, "tsp"));   // only 2 of 3 eggs
        assertTrue(RecipeMatcher.findStrictMatches(Arrays.asList(omelette), pantry, true).isEmpty());
    }

    @Test
    public void singularAndPluralNamesMatch() {
        assertEquals(IngredientNormalizer.normalize("tomato"), IngredientNormalizer.normalize("Tomatoes"));
        assertEquals(IngredientNormalizer.normalize("potato"), IngredientNormalizer.normalize(" POTATOES "));
        assertEquals(IngredientNormalizer.normalize("berry"), IngredientNormalizer.normalize("berries"));
        assertEquals(IngredientNormalizer.normalize("lentil"), IngredientNormalizer.normalize("Lentils"));
    }

    @Test
    public void unitDifferencesAreConverted() {
        Recipe pancakes = recipe("Pancakes", new RecipeIngredient("flour", 200, "g"),
                new RecipeIngredient("milk", 300, "ml"));
        List<PantryItem> pantry = Arrays.asList(item("Flour", 1, "kg"), item("Milk", 1, "l"));
        assertEquals(1, RecipeMatcher.findStrictMatches(Arrays.asList(pancakes), pantry, true).size());
    }

    @Test
    public void duplicatePantryRowsAreAddedTogether() {
        Recipe r = recipe("Mash", new RecipeIngredient("potato", 500, "g"));
        List<PantryItem> pantry = Arrays.asList(item("Potato", 300, "g"), item("Potatoes", 300, "g"));
        assertEquals(1, RecipeMatcher.findStrictMatches(Arrays.asList(r), pantry, true).size());
    }

    @Test
    public void expiredItemsAreIgnoredWhenSettingIsOn() {
        Recipe r = recipe("Toast", new RecipeIngredient("bread", 2, "pcs"));
        List<PantryItem> pantry = new ArrayList<>();
        pantry.add(new PantryItem(1, "Bread", 4, "pcs", "2000-01-01"));
        assertTrue(RecipeMatcher.findStrictMatches(Arrays.asList(r), pantry, true).isEmpty());
        assertEquals(1, RecipeMatcher.findStrictMatches(Arrays.asList(r), pantry, false).size());
    }

    @Test
    public void almostThereListHoldsOnlyRecipesMissingExactlyOne() {
        List<PantryItem> pantry = Arrays.asList(
                item("Egg", 6, "pcs"), item("Cheese", 100, "g"), item("Butter", 250, "g"),
                item("Tomato", 2, "pcs"));               // salt missing
        List<MatchResult> almost = RecipeMatcher.findAlmostThere(Arrays.asList(omelette), pantry, true);
        assertEquals(1, almost.size());
        assertEquals("salt", almost.get(0).getMissing().get(0).getName());
        assertTrue(RecipeMatcher.findStrictMatches(Arrays.asList(omelette), pantry, true).isEmpty());
    }
}
