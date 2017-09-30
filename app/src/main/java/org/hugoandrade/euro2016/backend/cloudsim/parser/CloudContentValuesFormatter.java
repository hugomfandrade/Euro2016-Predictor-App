package org.hugoandrade.euro2016.backend.cloudsim.parser;


import android.content.ContentValues;


import org.hugoandrade.euro2016.backend.object.Account;
import org.hugoandrade.euro2016.backend.object.Prediction;

/**
 * Parses the objects to ContentValues data.
 */
public class CloudContentValuesFormatter {

    public ContentValues getAsContentValues(Prediction prediction) {
        return ContentValuesBuilder.instance()
                .put(Prediction.Entry.COLUMN__ID, prediction.getID() == -1 ?
                        null : prediction.getID())
                .put(Prediction.Entry.COLUMN_MATCH_NO, prediction.getMatchNumber())
                .put(Prediction.Entry.COLUMN_HOME_TEAM_GOALS, prediction.getHomeTeamGoals() == -1 ?
                        null : prediction.getHomeTeamGoals())
                .put(Prediction.Entry.COLUMN_AWAY_TEAM_GOALS, prediction.getAwayTeamGoals() == -1 ?
                        null : prediction.getAwayTeamGoals())
                .put(Prediction.Entry.COLUMN_USER_ID, prediction.getUserID())
                .put(Prediction.Entry.COLUMN_SCORE, prediction.getScore() == -1 ?
                        null : prediction.getScore())
                .create();
    }

    public ContentValues getAsContentValues(Account account) {
        return ContentValuesBuilder.instance()
                .put(Account.Entry.COLUMN__ID, account.getID())
                .put(Account.Entry.COLUMN_USERNAME, account.getUsername())
                .put(Account.Entry.COLUMN_PASSWORD, account.getPassword())
                .put(Account.Entry.COLUMN_SCORE, account.getScore() == -1 ?
                        null : account.getScore())
                .create();
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
