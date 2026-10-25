package com.richfield.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Helpers for the optional expiry date (stored as an ISO string yyyy-MM-dd). */
public final class AppDateUtils {

    /** Items expiring within this many days are flagged as "expiring soon". */
    public static final int EXPIRING_SOON_DAYS = 3;

    private AppDateUtils() { }

    private static Date parse(String iso) {
        if (iso == null || iso.trim().isEmpty()) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            f.setLenient(false);
            return f.parse(iso);
        } catch (ParseException e) {
            return null;
        }
    }

    /** Days from today until the date (negative = already expired); null if no valid date. */
    public static Integer daysUntil(String iso) {
        Date d = parse(iso);
        if (d == null) return null;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long diff = d.getTime() - today.getTimeInMillis();
        return (int) Math.round(diff / 86_400_000.0);
    }

    public static boolean isExpired(String iso) {
        Integer days = daysUntil(iso);
        return days != null && days < 0;
    }

    public static boolean isExpiringSoon(String iso) {
        Integer days = daysUntil(iso);
        return days != null && days >= 0 && days <= EXPIRING_SOON_DAYS;
    }

    /** "2026-10-25" -> "25 Oct 2026". */
    public static String formatDisplay(String iso) {
        Date d = parse(iso);
        if (d == null) return "";
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d);
    }
}
