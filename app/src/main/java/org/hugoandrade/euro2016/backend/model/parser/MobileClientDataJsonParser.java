package org.hugoandrade.euro2016.backend.model.parser;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.utils.ISO8601;

/**
 * Parses the Json data returned from the Mobile Service Client API
 * and returns the objects that contain this data.
 */
public class MobileClientDataJsonParser {

    /**
     * Used for logging purposes.
     */
    private final String TAG =
            getClass().getSimpleName();

    public SystemData parseSystemData(JsonObject jsonObject) {

        return new SystemData(
                getJsonPrimitive(jsonObject, SystemData.Entry.Cols._ID, -1),
                getJsonPrimitive(jsonObject, SystemData.Entry.Cols.RULES, null),
                getJsonPrimitive(jsonObject, SystemData.Entry.Cols.APP_STATE, false),
                ISO8601.toCalendar(getJsonPrimitive(jsonObject, SystemData.Entry.Cols.SYSTEM_DATE, null)),
                ISO8601.toCalendar(getJsonPrimitive(jsonObject, SystemData.Entry.Cols.DATE_OF_CHANGE, null)));


    }

    public ArrayList<Country> parseCountryList(JsonElement result) {

        ArrayList<Country> allCountryList = new ArrayList<>();

        for (JsonElement item : result.getAsJsonArray()) {
            try {
                allCountryList.add(parseCountry(item.getAsJsonObject()));
            } catch (ClassCastException e) {
                Log.e(TAG, "Exception caught when parsing Country" +
                        " data from azure table: " + e.getMessage());
            }
        }
        return allCountryList;
    }

    public Country parseCountry(JsonObject jsonObject) {
        return new Country(
                getJsonPrimitive(jsonObject, Country.Entry.Cols._ID, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.NAME, null),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.MATCHES_PLAYED, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.VICTORIES, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.DRAWS, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.DEFEATS, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.GOALS_FOR, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.GOALS_AGAINST, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.GOALS_DIFFERENCE, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.GROUP, null),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.POINTS, -1),
                getJsonPrimitive(jsonObject, Country.Entry.Cols.POSITION, -1));
    }

    public ArrayList<Match> parseMatchList(JsonElement result) {

        ArrayList<Match> allMatchList = new ArrayList<>();

        for (JsonElement item : result.getAsJsonArray()) {
            try {
                allMatchList.add(parseMatch(item.getAsJsonObject()));
            } catch (ClassCastException e) {
                Log.e(TAG, "Exception caught when parsing Match" +
                        " data from azure table: " + e.getMessage());
            }
        }
        return allMatchList;
    }

    public Match parseMatch(JsonObject jsonObject) {

        return new Match(
                getJsonPrimitive(jsonObject, Match.Entry.Cols._ID, -1),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.MATCH_NO, -1),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.HOME_TEAM, null),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.AWAY_TEAM, null),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.HOME_TEAM_GOALS, -1),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.AWAY_TEAM_GOALS, -1),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.HOME_TEAM_NOTES, null),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.AWAY_TEAM_NOTES, null),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.GROUP, null),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.STAGE, null),
                getJsonPrimitive(jsonObject, Match.Entry.Cols.STADIUM, null),
                ISO8601.toDate(getJsonPrimitive(jsonObject, Match.Entry.Cols.DATE_AND_TIME, null))
        );
    }

    public String parseString(JsonObject jsonObject, String jsonMemberName) {
        return getJsonPrimitive(jsonObject, jsonMemberName, null);
    }

    private static int getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, int defaultValue) {
        try {
            return (int) jsonObject.getAsJsonPrimitive(jsonMemberName).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, String defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, boolean defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
