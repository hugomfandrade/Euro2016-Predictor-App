package org.hugoandrade.euro2016.predictor.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import org.hugoandrade.euro2016.predictor.R;
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

    public static int getFirstNotPlayedMatch(List<Match> matchList, Date serverTime) {
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

    public static int getMatchNumberOfFirstNotPlayedMatch(List<Match> matchList, Date serverTime) {
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

    public static int getPositionOfFirstNotPlayedMatch(List<Match> matchList, Date serverTime) {
        return getPositionOfFirstNotPlayedMatch(matchList, serverTime, 0);
    }

    public static int getPositionOfFirstNotPlayedMatch(List<Match> matchList, Date serverTime, int offset) {
        if (matchList != null) {
            for (int i = 0; i < matchList.size(); i++) {
                if (matchList.get(i).getDateAndTime().after(serverTime)) {
                    return (i < offset)? 0 : (i - offset);
                }
            }
            return (matchList.size() < offset)? 0 : (matchList.size() - offset);
        }
        return 0;
    }

    public static Match getLastPlayedMatch(List<Match> matchList, Date serverTime) {
        if (matchList != null && matchList.size() != 0) {
            Match lastMatch = null;
            for (Match match : matchList) {
                if (match.getDateAndTime().after(serverTime)) {
                    return lastMatch;
                }
                else {
                    lastMatch = match;
                }
            }
            return matchList.get(matchList.size() - 1);
        }
        return null;
    }

    public static Match getFirstMatchOfTomorrow(List<Match> matchList, Date time) {
        Calendar tomorrow = toTomorrow(toCalendar(time));
        if (matchList != null && matchList.size() != 0) {
            for (Match match : matchList) {

                if (match.getDateAndTime().after(tomorrow.getTime())) {
                    return match;
                }
            }
            return matchList.get(matchList.size() - 1);
        }
        return null;
    }

    private static Calendar toCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    private static Calendar toTomorrow(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar;
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

    public static List<Match> getMatchList(List<Match> matchList, int minMatchNumber, int maxMatchNumber) {

        List<Match> mList = new ArrayList<>();
        for (Match m : matchList) {
            if (m.getMatchNumber() >= minMatchNumber && m.getMatchNumber() <= maxMatchNumber) {
                mList.add(m);
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

    public static boolean isMatchupSetUp(Match match) {
        return match != null && isCountry(match.getHomeTeamName()) && isCountry(match.getAwayTeamName());

    }

    public static boolean hasAtLeastOneOfTheMatchupSetUp(Match match) {
        return match != null && (isCountry(match.getHomeTeamName()) || isCountry(match.getAwayTeamName()));

    }

    public static boolean isCountry(String name) {
        if (name == null) return false;
        switch (name) {
            case "France":
            case "Romania":
            case "Albania":
            case "Switzerland":
            case "England":
            case "Russia":
            case "Wales":
            case "Slovakia":
            case "Germany":
            case "Ukraine":
            case "Poland":
            case "Northern Ireland":
            case "Spain":
            case "Czech Republic":
            case "Turkey":
            case "Croatia":
            case "Belgium":
            case "Italy":
            case "Ireland":
            case "Sweden":
            case "Portugal":
            case "Iceland":
            case "Austria":
            case "Hungary":
                return true;
        }
        return false;
    }

    public static boolean isValidToGetPreviousMatches(SparseArray<Match> matchSet, Match match) {

        switch (match.getStage()) {
            case "Group Stage":
            case "Round of 16":
                return false;
            case "Quarter Final":
            case "Semi Final":
            case "Final":

                if (!MatchUtils.havePreviousMatchesBeenSetUpStrict(matchSet, match)) {
                    return false;
                }
                /*if (!MatchUtils.havePreviousMatchesBeenSetUp(matchSet, match)) {
                    return false;
                }/**/
                break;
            default:
                return false;

        }

        return true;
    }

    public static String tryGetTemporaryAwayTeam(Context context, SparseArray<Match> matchSet, Match match) {

        if (!isValidToGetPreviousMatches(matchSet, match)) {
            return TranslationUtils.translateCountryName(context, match.getAwayTeamName());
        }

        return getTemporaryAwayTeam(context, matchSet, match);
    }

    public static String tryGetTemporaryHomeTeam(Context context, SparseArray<Match> matchSet, Match match) {

        if (!isValidToGetPreviousMatches(matchSet, match)) {
            return TranslationUtils.translateCountryName(context, match.getHomeTeamName());
        }

        return getTemporaryHomeTeam(context, matchSet, match);
    }

    public static String getTemporaryHomeTeam(Context context, SparseArray<Match> matchSet, Match match) {

        String firstName;
        String secondName;
        switch (match.getMatchNumber()) {
            case 51:
                firstName = matchSet.get(50).getHomeTeamName();
                secondName = matchSet.get(50).getAwayTeamName();
                break;
            case 50:
                firstName = matchSet.get(47).getHomeTeamName();
                secondName = matchSet.get(47).getAwayTeamName();
                break;
            case 49:
                firstName = matchSet.get(45).getHomeTeamName();
                secondName = matchSet.get(45).getAwayTeamName();
                break;
            case 48:
                firstName = matchSet.get(40).getHomeTeamName();
                secondName = matchSet.get(40).getAwayTeamName();
                break;
            case 47:
                firstName = matchSet.get(41).getHomeTeamName();
                secondName = matchSet.get(41).getAwayTeamName();
                break;
            case 46:
                firstName = matchSet.get(38).getHomeTeamName();
                secondName = matchSet.get(38).getAwayTeamName();
                break;
            case 45:
                firstName = matchSet.get(37).getHomeTeamName();
                secondName = matchSet.get(37).getAwayTeamName();
                break;
                default:
                    return TranslationUtils.translateCountryName(context, match.getHomeTeamName());

        }

        return TextUtils.concat(
                TranslationUtils.translateCountryName(context, firstName),
                "\n",
                context.getString(R.string.or),
                "\n",
                TranslationUtils.translateCountryName(context, secondName)
        ).toString();
    }

    public static String getTemporaryAwayTeam(Context context, SparseArray<Match> matchSet, Match match) {

        String firstName;
        String secondName;
        switch (match.getMatchNumber()) {
            case 51:
                firstName = matchSet.get(49).getHomeTeamName();
                secondName = matchSet.get(49).getAwayTeamName();
                break;
            case 50:
                firstName = matchSet.get(48).getHomeTeamName();
                secondName = matchSet.get(48).getAwayTeamName();
                break;
            case 49:
                firstName = matchSet.get(46).getHomeTeamName();
                secondName = matchSet.get(46).getAwayTeamName();
                break;
            case 48:
                firstName = matchSet.get(44).getHomeTeamName();
                secondName = matchSet.get(44).getAwayTeamName();
                break;
            case 47:
                firstName = matchSet.get(43).getHomeTeamName();
                secondName = matchSet.get(43).getAwayTeamName();
                break;
            case 46:
                firstName = matchSet.get(42).getHomeTeamName();
                secondName = matchSet.get(42).getAwayTeamName();
                break;
            case 45:
                firstName = matchSet.get(39).getHomeTeamName();
                secondName = matchSet.get(39).getAwayTeamName();
                break;
                default:
                    return TranslationUtils.translateCountryName(context, match.getAwayTeamName());

        }

        return TextUtils.concat(
                TranslationUtils.translateCountryName(context, firstName),
                "\n",
                context.getString(R.string.or),
                "\n",
                TranslationUtils.translateCountryName(context, secondName)
        ).toString();
    }

    public static boolean havePreviousMatchesBeenSetUp(SparseArray<Match> matchSet, Match match) {

        switch (match.getMatchNumber()) {
            case 51:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(50)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(49));
            case 50:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(47)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(48));
            case 49:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(45)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(46));
            case 48:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(40)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(44));
            case 47:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(41)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(43));
            case 46:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(38)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(42));
            case 45:
                return hasAtLeastOneOfTheMatchupSetUp(matchSet.get(37)) || hasAtLeastOneOfTheMatchupSetUp(matchSet.get(39));

        }
        return false;
    }

    public static boolean havePreviousMatchesBeenSetUpStrict(SparseArray<Match> matchSet, Match match) {

        switch (match.getMatchNumber()) {
            case 51:
                return isMatchupSetUp(matchSet.get(50)) && isMatchupSetUp(matchSet.get(49));
            case 50:
                return isMatchupSetUp(matchSet.get(47)) && isMatchupSetUp(matchSet.get(48));
            case 49:
                return isMatchupSetUp(matchSet.get(45)) && isMatchupSetUp(matchSet.get(46));
            case 48:
                return isMatchupSetUp(matchSet.get(40)) && isMatchupSetUp(matchSet.get(44));
            case 47:
                return isMatchupSetUp(matchSet.get(41)) && isMatchupSetUp(matchSet.get(43));
            case 46:
                return isMatchupSetUp(matchSet.get(38)) && isMatchupSetUp(matchSet.get(42));
            case 45:
                return isMatchupSetUp(matchSet.get(37)) && isMatchupSetUp(matchSet.get(39));

        }
        return false;
    }

    public static String getScoreOfHomeTeam(Match match) {
        if (match == null || match.getHomeTeamGoals() == -1) return "";

        return (match.getHomeTeamNotes() == null ? "" : match.getHomeTeamNotes())
                    + String.valueOf(match.getHomeTeamGoals());
    }

    public static String getScoreOfAwayTeam(Match match) {
        if (match == null || match.getAwayTeamGoals() == -1) return "";

        return String.valueOf(match.getAwayTeamGoals()) +
                (match.getAwayTeamNotes() == null ? "" : match.getAwayTeamNotes());
    }

    public static String getShortMatchUp(Context context, Match match) {
        if (match == null) return "";

        return TextUtils.concat(
                TranslationUtils.translateCountryName(context, match.getHomeTeamName()),
                " - ",
                TranslationUtils.translateCountryName(context, match.getAwayTeamName())).toString();
    }
}

