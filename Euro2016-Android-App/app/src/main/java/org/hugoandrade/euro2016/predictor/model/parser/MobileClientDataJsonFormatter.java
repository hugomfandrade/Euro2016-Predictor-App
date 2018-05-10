package org.hugoandrade.euro2016.predictor.model.parser;

import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
/**
 * Parses the objects to Json data.
 */
public class MobileClientDataJsonFormatter {

    public JsonObject getAsJsonObject(Prediction prediction, String... exceptProperties) {

        return JsonObjectBuilder.instance()
                .addProperty(Prediction.Entry.Cols.ID, prediction.getID())
                .addProperty(Prediction.Entry.Cols.USER_ID, prediction.getUserID())
                .addProperty(Prediction.Entry.Cols.MATCH_NO, prediction.getMatchNumber() == -1? null: prediction.getMatchNumber())
                .addProperty(Prediction.Entry.Cols.HOME_TEAM_GOALS, prediction.getHomeTeamGoals() == -1? null: prediction.getHomeTeamGoals())
                .addProperty(Prediction.Entry.Cols.AWAY_TEAM_GOALS, prediction.getAwayTeamGoals() == -1? null: prediction.getAwayTeamGoals())
                .addProperty(Prediction.Entry.Cols.SCORE, prediction.getScore() == -1? null : prediction.getScore())
                .removeProperties(exceptProperties)
                .create();
    }

    public JsonObject getAsJsonObject(LoginData loginData, String... exceptProperties) {

        return JsonObjectBuilder.instance()
                .addProperty(LoginData.Entry.Cols.EMAIL, loginData.getEmail())
                .addProperty(LoginData.Entry.Cols.PASSWORD, loginData.getPassword())
                .removeProperties(exceptProperties)
                .create();
    }

    public JsonObject getAsJsonObject(League league, String... exceptProperties) {

        return JsonObjectBuilder.instance()
                .addProperty(League.Entry.Cols.NAME, league.getName())
                .addProperty(League.Entry.Cols.ADMIN_ID, league.getAdminID())
                .addProperty(League.Entry.Cols.CODE, league.getCode())
                .removeProperties(exceptProperties)
                .create();
    }

    public JsonObjectBuilder build() {
        return JsonObjectBuilder.instance();
    }

    public static class JsonObjectBuilder {

        private final JsonObject mJsonObject;

        public static JsonObjectBuilder instance() {
            return new JsonObjectBuilder();
        }

        public JsonObjectBuilder() {
            mJsonObject = new JsonObject();
        }

        public JsonObjectBuilder addProperty(String property, String value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        public JsonObjectBuilder addProperty(String property, Number value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        public JsonObjectBuilder addProperty(String property, Boolean value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        public JsonObjectBuilder removeProperties(String... properties) {
            for (String property : properties)
                mJsonObject.remove(property);
            return this;
        }

        public JsonObject create() {
            return mJsonObject;
        }
    }
}
