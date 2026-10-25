package com.richfield.smartpantry.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities to a common base so that "1 kg" in the pantry satisfies "200 g" in a recipe.
 * Two dimensions are supported:
 *  - AMOUNT: weight and volume, both converted to a base of grams / millilitres (1 g ~ 1 ml, a
 *            deliberate simplification that is fine for kitchen quantities).
 *  - COUNT : whole items ("pcs"), which are never converted into weight or volume.
 */
public final class UnitConverter {

    public static final String[] UNITS = {"pcs", "g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb"};
    public static final String DIM_AMOUNT = "AMOUNT";
    public static final String DIM_COUNT = "COUNT";

    private static final Map<String, Double> TO_BASE = new HashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();
    static {
        TO_BASE.put("pcs", 1.0);
        TO_BASE.put("g", 1.0);
        TO_BASE.put("kg", 1000.0);
        TO_BASE.put("ml", 1.0);
        TO_BASE.put("l", 1000.0);
        TO_BASE.put("tsp", 5.0);
        TO_BASE.put("tbsp", 15.0);
        TO_BASE.put("cup", 240.0);
        TO_BASE.put("oz", 28.35);
        TO_BASE.put("lb", 453.6);

        String[][] a = {
                {"pc", "pcs"}, {"piece", "pcs"}, {"pieces", "pcs"}, {"unit", "pcs"}, {"units", "pcs"},
                {"gram", "g"}, {"grams", "g"}, {"gr", "g"},
                {"kilogram", "kg"}, {"kilograms", "kg"}, {"kgs", "kg"},
                {"millilitre", "ml"}, {"milliliter", "ml"}, {"millilitres", "ml"}, {"milliliters", "ml"},
                {"litre", "l"}, {"liter", "l"}, {"litres", "l"}, {"liters", "l"},
                {"teaspoon", "tsp"}, {"teaspoons", "tsp"}, {"tablespoon", "tbsp"}, {"tablespoons", "tbsp"},
                {"cups", "cup"}, {"ounce", "oz"}, {"ounces", "oz"}, {"pound", "lb"}, {"pounds", "lb"}, {"lbs", "lb"}
        };
        for (String[] pair : a) ALIASES.put(pair[0], pair[1]);
    }

    private UnitConverter() { }

    public static String canonicalUnit(String unit) {
        if (unit == null) return "pcs";
        String u = unit.trim().toLowerCase(Locale.ROOT);
        if (ALIASES.containsKey(u)) return ALIASES.get(u);
        return TO_BASE.containsKey(u) ? u : "pcs";
    }

    public static String dimension(String unit) {
        return "pcs".equals(canonicalUnit(unit)) ? DIM_COUNT : DIM_AMOUNT;
    }

    public static double toBase(double quantity, String unit) {
        return quantity * TO_BASE.get(canonicalUnit(unit));
    }

    public static boolean isValidUnit(String unit) {
        if (unit == null) return false;
        for (String u : UNITS) if (u.equals(unit)) return true;
        return false;
    }
}
