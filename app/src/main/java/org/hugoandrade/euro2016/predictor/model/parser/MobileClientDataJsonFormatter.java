package org.hugoandrade.euro2016.predictor.model.parser;

import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.data.Prediction;

/**
 * Parses the objects to Json data.
 */
public class MobileClientDataJsonFormatter {

    public JsonObject getAsJsonObject(Prediction prediction) {

        return JsonObjectBuilder.instance()
                .addProperty(Prediction.Entry.Cols.ID, prediction.getID())
                .addProperty(Prediction.Entry.Cols.USER_ID, prediction.getUserID())
                .addProperty(Prediction.Entry.Cols.MATCH_NO, prediction.getMatchNumber() == -1? null: prediction.getMatchNumber())
                .addProperty(Prediction.Entry.Cols.HOME_TEAM_GOALS, prediction.getHomeTeamGoals() == -1? null: prediction.getHomeTeamGoals())
                .addProperty(Prediction.Entry.Cols.AWAY_TEAM_GOALS, prediction.getAwayTeamGoals() == -1? null: prediction.getAwayTeamGoals())
                .addProperty(Prediction.Entry.Cols.SCORE, prediction.getScore() == -1? null : prediction.getScore())
                .create();
    }

    public JsonObject getAsJsonObject(LoginData loginData) {

        return JsonObjectBuilder.instance()
                .addProperty(LoginData.Entry.Cols.EMAIL, loginData.getEmail())
                .addProperty(LoginData.Entry.Cols.PASSWORD, loginData.getPassword())
                .create();
    }

    private static class JsonObjectBuilder {

        private final JsonObject mJsonObject;

        private static JsonObjectBuilder instance() {
            return new JsonObjectBuilder();
        }

        private JsonObjectBuilder() {
            mJsonObject = new JsonObject();
        }

        JsonObjectBuilder addProperty(String property, String value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        JsonObjectBuilder addProperty(String property, Number value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        JsonObjectBuilder addProperty(String property, Boolean value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        JsonObject create() {
            return mJsonObject;
        }
    }
}
