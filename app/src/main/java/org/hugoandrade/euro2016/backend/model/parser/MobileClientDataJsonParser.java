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
        Log.e("TAG", jsonObject.toString());

        return new SystemData(
                getJsonPrimitive(jsonObject, SystemData.Entry.COLUMN__ID, -1),
                getJsonPrimitive(jsonObject, SystemData.Entry.COLUMN_RULES, null),
                getJsonPrimitive(jsonObject, SystemData.Entry.COLUMN_APP_STATE, false),
                ISO8601.toCalendar(getJsonPrimitive(jsonObject, SystemData.Entry.COLUMN_SYSTEM_DATE, null)),
                ISO8601.toCalendar(getJsonPrimitive(jsonObject, SystemData.Entry.COLUMN_DATE_OF_CHANGE, null)));


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
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN__ID, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_NAME, null),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_MATCHES_PLAYED, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_VICTORIES, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_DRAWS, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_DEFEATS, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_GOALS_FOR, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_GOALS_AGAINST, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_GOALS_DIFFERENCE, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_GROUP, null),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_POINTS, -1),
                getJsonPrimitive(jsonObject, Country.Entry.COLUMN_POSITION, -1));
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
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN__ID, -1),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_MATCH_NO, -1),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_HOME_TEAM, null),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_AWAY_TEAM, null),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_HOME_TEAM_GOALS, -1),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_AWAY_TEAM_GOALS, -1),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_HOME_TEAM_NOTES, null),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_AWAY_TEAM_NOTES, null),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_GROUP, null),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_STAGE, null),
                getJsonPrimitive(jsonObject, Match.Entry.COLUMN_STADIUM, null),
                ISO8601.toDate(getJsonPrimitive(jsonObject, Match.Entry.COLUMN_DATE_AND_TIME, null))
        );
    }

    private int getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, int defaultValue) {
        try {
            return (int) jsonObject.getAsJsonPrimitive(jsonMemberName).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, String defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, boolean defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
