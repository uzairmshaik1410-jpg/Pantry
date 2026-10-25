package com.richfield.smartpantry.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Small text helpers shared by the adapters and screens. */
public final class Format {
    private Format() { }

    /** 2.0 -> "2", 2.5 -> "2.5", 0.333 -> "0.33". */
    public static String quantity(double q) {
        DecimalFormat df = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(q);
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
