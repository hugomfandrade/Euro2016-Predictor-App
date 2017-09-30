package org.hugoandrade.euro2016.backend.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Helper class for handling a most common subset of ISO 8601 strings
 * (in the following format: "2008-03-01T13:00:00+01:00"). It supports
 * parsing the "Z" timezone, but many other less-used features are
 * missing.
 */
public final class ISO8601 {
    /** Transform Calendar to ISO 8601 string. */
    public static String fromCalendar(final Calendar calendar) {
        return fromDate(calendar.getTime());
    }

    public static String fromDate(Date date) {
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.UK)
            .format(date);
        return formatted.substring(0, 26) + ":" + formatted.substring(26);
    }

    public static String fromCalendarButClean(Calendar calendar) {
        return new SimpleDateFormat("dd-MM-yyyy - HH:mm:ss", Locale.UK)
                .format(calendar.getTime());
    }

    /** Get current date and time formatted as ISO 8601 string. */
    @SuppressWarnings("unused")
    public static String now() {
        return fromCalendar(GregorianCalendar.getInstance());
    }

    /** Transform ISO 8601 string to Date. */
    public static Date toDate(final String iso8601string) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.UK);
        try {
            return sdf.parse(iso8601string);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Calendar toCalendar(String iso8601string) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(toDate(iso8601string));
        } catch (NullPointerException e ) {
            return null;
        }
        return c;
    }

    public static Calendar getDate(int day, int month, int year, int hour, int min) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, 0);
        return c;
    }
}
