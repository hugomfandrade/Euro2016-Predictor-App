package org.hugoandrade.euro2016.predictor.cloudsim.parser;

import android.content.ContentValues;
import android.database.Cursor;

import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.ISO8601;

/**
 * Parses the objects to Json data.
 */
public class CloudPOJOFormatter {

    public User parseAccount(Cursor c) {
        return new User(
                getColumnValue(c, "_" + User.Entry.Cols.ID, null),
                getColumnValue(c, User.Entry.Cols.EMAIL, null),
                getColumnValue(c, User.Entry.Cols.PASSWORD, null),
                getColumnValue(c, User.Entry.Cols.SCORE, 0)
        );
    }

    /*public SystemData parseSystemData(Cursor c) {
        return new SystemData(
                getColumnValue(c, "_" + SystemData.Entry.Cols.ID, null),
                getColumnValue(c, SystemData.Entry.Cols.RULES, null),
                getColumnValue(c, SystemData.Entry.Cols.APP_STATE, 0) != 0,
                ISO8601.toCalendar(getColumnValue(c, SystemData.Entry.Cols.SYSTEM_DATE, null)),
                ISO8601.toCalendar(getColumnValue(c, SystemData.Entry.Cols.DATE_OF_CHANGE, null))
        );
    }

    public SystemData parseSystemData(ContentValues values) {
        return new SystemData(
                getColumnValue(values, "_" + SystemData.Entry.Cols.ID, null),
                getColumnValue(values, SystemData.Entry.Cols.RULES, null),
                getColumnValue(values, SystemData.Entry.Cols.APP_STATE, 0) != 0,
                ISO8601.toCalendar(getColumnValue(values, SystemData.Entry.Cols.SYSTEM_DATE, null)),
                ISO8601.toCalendar(getColumnValue(values, SystemData.Entry.Cols.DATE_OF_CHANGE, null))
        );
    }/**/

    public Country parseCountry(Cursor c) {

        return new Country(
                getColumnValue(c, "_" + Country.Entry.Cols.ID, null),
                getColumnValue(c, Country.Entry.Cols.NAME, null),
                getColumnValue(c, Country.Entry.Cols.MATCHES_PLAYED, 0),
                getColumnValue(c, Country.Entry.Cols.VICTORIES, 0),
                getColumnValue(c, Country.Entry.Cols.DRAWS, 0),
                getColumnValue(c, Country.Entry.Cols.DEFEATS, 0),
                getColumnValue(c, Country.Entry.Cols.GOALS_FOR, 0),
                getColumnValue(c, Country.Entry.Cols.GOALS_AGAINST, 0),
                getColumnValue(c, Country.Entry.Cols.GOALS_DIFFERENCE, 0),
                getColumnValue(c, Country.Entry.Cols.GROUP, null),
                getColumnValue(c, Country.Entry.Cols.POINTS, 0),
                getColumnValue(c, Country.Entry.Cols.POSITION, 0),
                getColumnValue(c, Country.Entry.Cols.COEFFICIENT, 0f),
                getColumnValue(c, Country.Entry.Cols.FAIR_PLAY_POINTS, 0));
    }

    public Match parseMatch(ContentValues values) {

        return new Match(
                getColumnValue(values, "_" + Match.Entry.Cols.ID, null),
                getColumnValue(values, Match.Entry.Cols.MATCH_NUMBER, 0),
                getColumnValue(values, Match.Entry.Cols.HOME_TEAM_ID, null),
                getColumnValue(values, Match.Entry.Cols.HOME_TEAM_ID, null),
                getColumnValue(values, Match.Entry.Cols.HOME_TEAM_GOALS, -1),
                getColumnValue(values, Match.Entry.Cols.AWAY_TEAM_GOALS, -1),
                getColumnValue(values, Match.Entry.Cols.HOME_TEAM_NOTES, null),
                getColumnValue(values, Match.Entry.Cols.AWAY_TEAM_NOTES, null),
                getColumnValue(values, Match.Entry.Cols.GROUP, null),
                getColumnValue(values, Match.Entry.Cols.STAGE, null),
                getColumnValue(values, Match.Entry.Cols.STADIUM, null),
                ISO8601.toDate(getColumnValue(values, Match.Entry.Cols.DATE_AND_TIME, null))
        );
    }

    public Match parseMatch(Cursor c) {

        return new Match(
                getColumnValue(c, "_" + Match.Entry.Cols.ID, null),
                getColumnValue(c, Match.Entry.Cols.MATCH_NUMBER, 0),
                getColumnValue(c, Match.Entry.Cols.HOME_TEAM_ID, null),
                getColumnValue(c, Match.Entry.Cols.HOME_TEAM_ID, null),
                getColumnValue(c, Match.Entry.Cols.HOME_TEAM_GOALS, -1),
                getColumnValue(c, Match.Entry.Cols.AWAY_TEAM_GOALS, -1),
                getColumnValue(c, Match.Entry.Cols.HOME_TEAM_NOTES, null),
                getColumnValue(c, Match.Entry.Cols.AWAY_TEAM_NOTES, null),
                getColumnValue(c, Match.Entry.Cols.GROUP, null),
                getColumnValue(c, Match.Entry.Cols.STAGE, null),
                getColumnValue(c, Match.Entry.Cols.STADIUM, null),
                ISO8601.toDate(getColumnValue(c, Match.Entry.Cols.DATE_AND_TIME, null))
        );
    }

    public Prediction parsePrediction(Cursor c) {

        return new Prediction(
                getColumnValue(c, "_" + Prediction.Entry.Cols.ID, null),
                getColumnValue(c, Prediction.Entry.Cols.USER_ID, null),
                getColumnValue(c, Prediction.Entry.Cols.MATCH_NO, -1),
                getColumnValue(c, Prediction.Entry.Cols.HOME_TEAM_GOALS, -1),
                getColumnValue(c, Prediction.Entry.Cols.AWAY_TEAM_GOALS, -1),
                getColumnValue(c, Prediction.Entry.Cols.SCORE, -1));
    }

    public Prediction parsePrediction(ContentValues values) {

        return new Prediction(
                getColumnValue(values, "_" + Prediction.Entry.Cols.ID, null),
                getColumnValue(values, Prediction.Entry.Cols.USER_ID, null),
                getColumnValue(values, Prediction.Entry.Cols.MATCH_NO, -1),
                getColumnValue(values, Prediction.Entry.Cols.HOME_TEAM_GOALS, -1),
                getColumnValue(values, Prediction.Entry.Cols.AWAY_TEAM_GOALS, -1),
                getColumnValue(values, Prediction.Entry.Cols.SCORE, -1));
    }

    public LoginData parseLoginData(ContentValues values) {
        return new LoginData(
                getColumnValue(values, LoginData.Entry.Cols.EMAIL, null),
                getColumnValue(values, LoginData.Entry.Cols.PASSWORD, null));
    }

    private int getColumnValue(Cursor cursor, String columnName, int defaultValue) {
        try {
            return cursor.getInt(cursor.getColumnIndex(columnName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float getColumnValue(Cursor cursor, String columnName,
                                 @SuppressWarnings("SameParameterValue") float defaultValue) {
        try {
            return cursor.getFloat(cursor.getColumnIndex(columnName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getColumnValue(Cursor cursor, String columnName,
                                  @SuppressWarnings("SameParameterValue") String defaultValue) {
        try {
            return cursor.getString(cursor.getColumnIndex(columnName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getColumnValue(ContentValues values, String columnName,
                               @SuppressWarnings("SameParameterValue")  int defaultValue) {
        Integer i = values.getAsInteger(columnName);

        return i == null? defaultValue : i;
    }

    private String getColumnValue(ContentValues values, String columnName,
                                  @SuppressWarnings("SameParameterValue") String defaultValue) {
        String s = values.getAsString(columnName);

        return s == null? defaultValue : s;
    }
}
