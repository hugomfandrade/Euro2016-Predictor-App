package org.hugoandrade.euro2016.predictor.admin.utils;


import org.hugoandrade.euro2016.predictor.admin.object.Match;

/**
 * Provides some general utility Match helper methods.
 */
public final class MatchUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = MatchUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private MatchUtils() {
        throw new AssertionError();
    }

    public static boolean isMatchPlayed(Match match) {
        return match.getHomeTeamGoals() != -1 && match.getAwayTeamGoals() != -1;
    }

    public static boolean didHomeTeamWin(Match match) {
        return match.getHomeTeamGoals() > match.getAwayTeamGoals();
    }

    public static boolean didAwayTeamWin(Match match) {
        return match.getAwayTeamGoals() > match.getHomeTeamGoals();
    }

    public static boolean didTeamsTied(Match match) {
        return match.getHomeTeamGoals() == match.getAwayTeamGoals();
    }

    public static boolean didHomeTeamWinByPenaltyShootout(Match match) {
        return match.getHomeTeamNotes() != null && match.getHomeTeamNotes().equals("p");
    }

    public static boolean didAwayTeamWinByPenaltyShootout(Match match) {
        return match.getAwayTeamNotes() != null && match.getAwayTeamNotes().equals("p");
    }

    public static boolean wasThereAPenaltyShootout(Match match) {
        return (match.getHomeTeamNotes() != null && match.getHomeTeamNotes().equals("p") ||
                (match.getAwayTeamNotes() != null && match.getAwayTeamNotes().equals("p")));
    }

    public static String getAsString(String value) {
        return value == null ? "": value;
    }

    public static String getAsString(int value) {
        return value == -1 ? "": Integer.toString(value);
    }

    public static String getString(String value) {
        return value.equals("") ? null: value;
    }

    public static int getInt(String value) {
        return getInt(value, -1);
    }

    public static int getInt(String value, int defaultValue) {
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

