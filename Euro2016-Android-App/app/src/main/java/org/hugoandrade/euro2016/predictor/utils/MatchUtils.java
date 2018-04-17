package org.hugoandrade.euro2016.predictor.utils;

import org.hugoandrade.euro2016.predictor.data.raw.Match;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        return didHomeTeamWinRegularTime(match) || didHomeTeamWinByPenaltyShootout(match);
    }

    public static boolean didAwayTeamWin(Match match) {
        return didAwayTeamWinRegularTime(match) || didAwayTeamWinByPenaltyShootout(match);
    }

    public static boolean didHomeTeamWinRegularTime(Match match) {
        return match.getHomeTeamGoals() > match.getAwayTeamGoals();
    }

    public static boolean didAwayTeamWinRegularTime(Match match) {
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

    public static String getShortDescription(Match match) {
        if (!MatchUtils.isMatchPlayed(match))
            return "";
        return getAsString(match.getHomeTeamNotes()) +
                Integer.toString(match.getHomeTeamGoals()) +
                " - " +
                Integer.toString(match.getAwayTeamGoals()) +
                getAsString(match.getAwayTeamNotes());
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

    public static int getMatchNumberOfFirstNotPlayedMatched(List<Match> matchList, Date serverTime) {
        if (matchList != null) {
            for (int i = 0; i < matchList.size(); i++) {
                if (matchList.get(i).getDateAndTime().after(serverTime)) {
                    return matchList.get(i).getMatchNumber();
                }
            }
            return matchList.size() + 1;
        }
        return 0;
    }

    public static List<Match> getMatchList(List<Match> matchList, StaticVariableUtils.SStage stage, int matchday) {

        List<Match> mList = new ArrayList<>();
        for (Match m : matchList) {
            if (stage.name.equals(m.getStage())) {
                if (matchday == 1 && m.getMatchNumber() >= 1 && m.getMatchNumber() <= 12) {
                    mList.add(m);
                } else if (matchday == 2 && m.getMatchNumber() >= 13 && m.getMatchNumber() <= 24) {
                    mList.add(m);
                } else if (matchday == 3 && m.getMatchNumber() >= 25 && m.getMatchNumber() <= 36) {
                    mList.add(m);
                }
            }
        }
        return mList;
    }

    public static List<Match> getMatchList(List<Match> matchList, StaticVariableUtils.SStage stage) {
        List<Match> mList = new ArrayList<>();
        for (Match m : matchList) {
            if (stage.name.equals(m.getStage())) {
                mList.add(m);
            }
        }
        return mList;
    }
}

