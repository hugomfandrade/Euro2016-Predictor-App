package org.hugoandrade.euro2016.backend.cloudsim.parser;


import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.backend.object.Account;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.Prediction;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.utils.ISO8601;

/**
 * Parses the objects to Json data.
 */
public class CloudContentValuesParser {

    public Account parseAccount(Cursor c) {
        return new Account(
                getColumnValue(c, Account.Entry.COLUMN__ID, null),
                getColumnValue(c, Account.Entry.COLUMN_USERNAME, null),
                getColumnValue(c, Account.Entry.COLUMN_PASSWORD, null),
                getColumnValue(c, Account.Entry.COLUMN_SCORE, -1)
        );
    }

    public Account parseAccount(ContentValues values) {

        return new Account(
                getColumnValue(values, Account.Entry.COLUMN__ID, null),
                getColumnValue(values, Account.Entry.COLUMN_USERNAME, null),
                getColumnValue(values, Account.Entry.COLUMN_PASSWORD, null),
                getColumnValue(values, Account.Entry.COLUMN_SCORE, -1)
        );
    }

    public SystemData parseSystemData(Cursor c) {
        return new SystemData(
                getColumnValue(c, SystemData.Entry.COLUMN__ID, -1),
                getColumnValue(c, SystemData.Entry.COLUMN_RULES, null),
                getColumnValue(c, SystemData.Entry.COLUMN_APP_STATE, 0) != 0,
                ISO8601.toCalendar(getColumnValue(c, SystemData.Entry.COLUMN_SYSTEM_DATE, null)),
                ISO8601.toCalendar(getColumnValue(c, SystemData.Entry.COLUMN_DATE_OF_CHANGE, null))
        );
    }

    public SystemData parseSystemData(ContentValues values) {
        return new SystemData(
                getColumnValue(values, SystemData.Entry.COLUMN__ID, -1),
                getColumnValue(values, SystemData.Entry.COLUMN_RULES, null),
                getColumnValue(values, SystemData.Entry.COLUMN_APP_STATE, 0) != 0,
                ISO8601.toCalendar(getColumnValue(values, SystemData.Entry.COLUMN_SYSTEM_DATE, null)),
                ISO8601.toCalendar(getColumnValue(values, SystemData.Entry.COLUMN_DATE_OF_CHANGE, null))
        );
    }

    public Match parseMatch(ContentValues values) {

        return new Match(
                getColumnValue(values, Match.Entry.COLUMN__ID, -1),
                getColumnValue(values, Match.Entry.COLUMN_MATCH_NO, -1),
                getColumnValue(values, Match.Entry.COLUMN_HOME_TEAM, null),
                getColumnValue(values, Match.Entry.COLUMN_AWAY_TEAM, null),
                getColumnValue(values, Match.Entry.COLUMN_HOME_TEAM_GOALS, -1),
                getColumnValue(values, Match.Entry.COLUMN_AWAY_TEAM_GOALS, -1),
                getColumnValue(values, Match.Entry.COLUMN_HOME_TEAM_NOTES, null),
                getColumnValue(values, Match.Entry.COLUMN_AWAY_TEAM_NOTES, null),
                getColumnValue(values, Match.Entry.COLUMN_GROUP, null),
                getColumnValue(values, Match.Entry.COLUMN_STAGE, null),
                getColumnValue(values, Match.Entry.COLUMN_STADIUM, null),
                ISO8601.toDate(getColumnValue(values, Match.Entry.COLUMN_DATE_AND_TIME, null))
        );
    }

    public Match parseMatch(Cursor c) {

        return new Match(
                c.getInt(c.getColumnIndex(Match.Entry.COLUMN__ID)),
                c.getInt(c.getColumnIndex(Match.Entry.COLUMN_MATCH_NO)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_HOME_TEAM)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_AWAY_TEAM)),
                c.getInt(c.getColumnIndex(Match.Entry.COLUMN_HOME_TEAM_GOALS)),
                c.getInt(c.getColumnIndex(Match.Entry.COLUMN_AWAY_TEAM_GOALS)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_HOME_TEAM_NOTES)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_AWAY_TEAM_NOTES)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_GROUP)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_STAGE)),
                c.getString(c.getColumnIndex(Match.Entry.COLUMN_STADIUM)),
                ISO8601.toDate(c.getString(c.getColumnIndex(Match.Entry.COLUMN_DATE_AND_TIME)))
        );
    }

    public Prediction parsePrediction(Cursor c) {

        return new Prediction(
                getColumnValue(c, Prediction.Entry.COLUMN__ID, -1),
                getColumnValue(c, Prediction.Entry.COLUMN_USER_ID, null),
                getColumnValue(c, Prediction.Entry.COLUMN_MATCH_NO, -1),
                getColumnValue(c, Prediction.Entry.COLUMN_HOME_TEAM_GOALS, -1),
                getColumnValue(c, Prediction.Entry.COLUMN_AWAY_TEAM_GOALS, -1),
                getColumnValue(c, Prediction.Entry.COLUMN_SCORE, -1));
    }

    public Prediction parsePrediction(ContentValues values) {

        return new Prediction(
                getColumnValue(values, Prediction.Entry.COLUMN__ID, -1),
                getColumnValue(values, Prediction.Entry.COLUMN_USER_ID, null),
                getColumnValue(values, Prediction.Entry.COLUMN_MATCH_NO, -1),
                getColumnValue(values, Prediction.Entry.COLUMN_HOME_TEAM_GOALS, -1),
                getColumnValue(values, Prediction.Entry.COLUMN_AWAY_TEAM_GOALS, -1),
                getColumnValue(values, Prediction.Entry.COLUMN_SCORE, -1));
    }

    private int getColumnValue(Cursor cursor, String columnName, int defaultValue) {
        try {
            return cursor.getInt(cursor.getColumnIndex(columnName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getColumnValue(Cursor cursor, String columnName, String defaultValue) {
        try {
            return cursor.getString(cursor.getColumnIndex(columnName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getColumnValue(ContentValues values, String columnName, int defaultValue) {
        Integer i = values.getAsInteger(columnName);

        return i == null? defaultValue : i;
    }

    private String getColumnValue(ContentValues values, String columnName, String defaultValue) {
        String s = values.getAsString(columnName);

        return s == null? defaultValue : s;
    }
}
