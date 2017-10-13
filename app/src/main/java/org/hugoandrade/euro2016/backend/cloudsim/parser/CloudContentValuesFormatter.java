package org.hugoandrade.euro2016.backend.cloudsim.parser;


import android.content.ContentValues;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.backend.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.backend.object.Account;
import org.hugoandrade.euro2016.backend.object.Prediction;
import org.hugoandrade.euro2016.backend.object.SystemData;

import java.util.Map;

/**
 * Parses the objects to ContentValues data.
 */
public class CloudContentValuesFormatter {

    public ContentValues getAsContentValues(Prediction prediction) {
        return ContentValuesBuilder.instance()
                .put(Prediction.Entry.Cols._ID, prediction.getID() == -1 ?
                        null : prediction.getID())
                .put(Prediction.Entry.Cols.MATCH_NO, prediction.getMatchNumber())
                .put(Prediction.Entry.Cols.HOME_TEAM_GOALS, prediction.getHomeTeamGoals() == -1 ?
                        null : prediction.getHomeTeamGoals())
                .put(Prediction.Entry.Cols.AWAY_TEAM_GOALS, prediction.getAwayTeamGoals() == -1 ?
                        null : prediction.getAwayTeamGoals())
                .put(Prediction.Entry.Cols.USER_ID, prediction.getUserID())
                .put(Prediction.Entry.Cols.SCORE, prediction.getScore() == -1 ?
                        null : prediction.getScore())
                .create();
    }

    public ContentValues getAsContentValues(Account account) {
        return ContentValuesBuilder.instance()
                .put(Account.Entry.Cols._ID, account.getID())
                .put(Account.Entry.Cols.USERNAME, account.getUsername())
                .put(Account.Entry.Cols.PASSWORD, account.getPassword())
                .put(Account.Entry.Cols.SCORE, account.getScore() == -1 ?
                        null : account.getScore())
                .create();
    }

    public ContentValues getAsContentValues(JsonObject jsonObject) {
        ContentValuesBuilder builder = ContentValuesBuilder.instance();
        for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet())
            // Because it is a boolean
            if (entry.getKey().equals(SystemData.Entry.Cols.APP_STATE)) {
                builder.put(entry.getKey(),
                            MobileClientDataJsonParser.getJsonPrimitive(jsonObject,
                                                                        entry.getKey(),
                                                                        false)? 1 : 0);
            }
            else
                builder.put(entry.getKey(),
                            MobileClientDataJsonParser.getJsonPrimitive(jsonObject,
                                                                        entry.getKey(),
                                                                        null));
        return builder.create();

    }

    private static class ContentValuesBuilder {

        private final ContentValues mContentValues;

        private static ContentValuesBuilder instance() {
            return new ContentValuesBuilder();
        }

        private ContentValuesBuilder() {
            mContentValues = new ContentValues();
        }

        ContentValuesBuilder put(String key, String value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValuesBuilder put(String key, Integer value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValuesBuilder put(String key, Boolean value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValues create() {
            return mContentValues;
        }
    }
}
