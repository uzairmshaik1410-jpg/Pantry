package com.richfield.smartpantry.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its ingredients. Maps to the recipes table (+ recipe_ingredients). */
public class Recipe {
    private long id;
    private String name;
    private String description;
    private int prepTimeMinutes;
    private int servings;
    /** Steps are stored in one column, separated by new-line characters. */
    private String steps;
    private final List<RecipeIngredient> ingredients = new ArrayList<>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }
    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }
    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }

    public List<String> getStepList() {
        List<String> list = new ArrayList<>();
        if (steps != null) {
            for (String s : steps.split("\n")) {
                if (!s.trim().isEmpty()) list.add(s.trim());
            }
        }
        return list;
    }
}
