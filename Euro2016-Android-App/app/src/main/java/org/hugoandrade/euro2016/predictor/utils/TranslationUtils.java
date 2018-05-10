package org.hugoandrade.euro2016.predictor.utils;


import android.content.Context;

import org.hugoandrade.euro2016.predictor.R;

import java.util.Locale;

public final class TranslationUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = TranslationUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private TranslationUtils() {
        throw new AssertionError();
    }

    public static String translateStage(Context context, String stage) {

        if (context == null) return stage;

        switch (stage) {
            case "Group Stage": return context.getString(R.string.group_stage);
            case "Round of 16": return context.getString(R.string.round_of_16);
            case "Quarter Final": return context.getString(R.string.quarter_finals);
            case "Semi Final": return context.getString(R.string.semi_finals);
            case "Final": return context.getString(R.string.finals);
        }
        return stage;
    }

    public static String translateCountryName(Context context, String countryName) {

        if (context == null) return countryName;

        switch (countryName) {
            case "France": return context.getString(R.string.country_france);
            case "Romania": return context.getString(R.string.country_romania);
            case "Albania": return context.getString(R.string.country_albania);
            case "Switzerland": return context.getString(R.string.country_switzerland);

            case "England": return context.getString(R.string.country_england);
            case "Russia": return context.getString(R.string.country_russia);
            case "Wales": return context.getString(R.string.country_wales);
            case "Slovakia": return context.getString(R.string.country_slovakia);

            case "Germany": return context.getString(R.string.country_germany);
            case "Ukraine": return context.getString(R.string.country_ukraine);
            case "Poland": return context.getString(R.string.country_poland);
            case "Northern Ireland": return context.getString(R.string.country_northern_ireland);

            case "Spain": return context.getString(R.string.country_spain);
            case "Czech Republic": return context.getString(R.string.country_czech_republic);
            case "Turkey": return context.getString(R.string.country_turkey);
            case "Croatia": return context.getString(R.string.country_croatia);

            case "Belgium": return context.getString(R.string.country_belgium);
            case "Italy": return context.getString(R.string.country_italy);
            case "Ireland": return context.getString(R.string.country_ireland);
            case "Sweden": return context.getString(R.string.country_sweden);

            case "Portugal": return context.getString(R.string.country_portugal);
            case "Iceland": return context.getString(R.string.country_iceland);
            case "Austria": return context.getString(R.string.country_austria);
            case "Hungary": return context.getString(R.string.country_hungary);
        }

        if (countryName.contains("Winner")) {
            int i = countryName.indexOf("Winner");
            countryName = countryName.substring(0, i)
                    + context.getString(R.string.winner)
                    + countryName.substring(i + "Winner".length(), countryName.length());
        }
        if (countryName.contains("Runner-up")) {
            int i = countryName.indexOf("Runner-up");
            countryName = countryName.substring(0, i)
                    + context.getString(R.string.runner_up)
                    + countryName.substring(i + "Runner-up".length(), countryName.length());
        }
        if (countryName.contains("Group")) {
            int i = countryName.indexOf("Group");
            countryName = countryName.substring(0, i)
                    + context.getString(R.string.group)
                    + countryName.substring(i + "Group".length(), countryName.length());
        }
        if (countryName.contains("3rd Place")) {
            int i = countryName.indexOf("3rd Place");
            countryName = countryName.substring(0, i)
                    + context.getString(R.string.third_place)
                    + countryName.substring(i + "3rd Place".length(), countryName.length());
        }
        if (countryName.contains(" or ")) {
            int i = countryName.indexOf(" or ");
            countryName = countryName.substring(0, i)
                    + " "
                    + context.getString(R.string.or)
                    + " "
                    + countryName.substring(i + " or ".length(), countryName.length());
        }
        if (countryName.contains("Match")) {
            int i = countryName.indexOf("Match");
            countryName = countryName.substring(0, i)
                    + context.getString(R.string.match)
                    + countryName.substring(i + "Match".length(), countryName.length());
        }
        return countryName;
    }

    public static String getSuffix(int placeFinish) {
        if (Locale.getDefault().getLanguage().equals("pt")) {
            return "º";
        }
        else {/**/
            if (placeFinish == 1)
                return "st";
            else if (placeFinish == 2)
                return "nd";
            else if (placeFinish == 3)
                return "rd";
            else
                return "th";
        }
    }
}
