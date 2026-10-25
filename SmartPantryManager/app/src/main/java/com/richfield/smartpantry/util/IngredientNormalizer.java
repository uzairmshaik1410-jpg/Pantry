package com.richfield.smartpantry.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns free-text ingredient names into a comparable key so that trivial differences
 * ("Tomatoes", " tomato ", "TOMATO") do not break the strict-matching rule.
 */
public final class IngredientNormalizer {

    private static final Set<String> KEEP_AS_IS = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus", "watercress",
            "lemongrass", "hibiscus", "bass", "grits"));

    private static final Map<String, String> IRREGULAR = new HashMap<>();
    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
    }

    private IngredientNormalizer() { }

    public static String normalize(String raw) {
        if (raw == null) return "";
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return cleaned;
        String[] words = cleaned.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = singular(words[i]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(words[i]);
        }
        return sb.toString();
    }

    /** Very small rule-based singulariser - enough for kitchen vocabulary, no NLP needed. */
    static String singular(String w) {
        if (w.length() < 4 || KEEP_AS_IS.contains(w)) return w;
        if (IRREGULAR.containsKey(w)) return IRREGULAR.get(w);
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";        // berries -> berry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);              // tomatoes -> tomato
        if (w.endsWith("ches") || w.endsWith("shes") || w.endsWith("xes")
                || w.endsWith("sses") || w.endsWith("zes")) {
            return w.substring(0, w.length() - 2);                                  // sandwiches -> sandwich
        }
        if (w.endsWith("ss") || w.endsWith("us") || w.endsWith("is")) return w;
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);                // eggs -> egg
        return w;
    }
}
