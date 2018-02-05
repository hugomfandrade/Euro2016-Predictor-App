package org.hugoandrade.euro2016.predictor.admin.cloudsim.parser;

import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.LoginData;
import org.hugoandrade.euro2016.predictor.admin.object.Match;
import org.hugoandrade.euro2016.predictor.admin.object.SystemData;
import org.hugoandrade.euro2016.predictor.admin.utils.ISO8601;

/**
 * Parses the objects to Json data.
 */
public class CloudDataJsonFormatter {

    /**
     * Used for logging purposes.
     */
    private final String TAG =
            getClass().getSimpleName();

    public JsonObject getAsJsonObject(Country country) {

        return JsonObjectBuilder.instance()
                .addProperty("_" + Country.Entry.Cols.ID, country.getID())
                .addProperty(Country.Entry.Cols.NAME, country.getName())
                .addProperty(Country.Entry.Cols.MATCHES_PLAYED, country.getMatchesPlayed())
                .addProperty(Country.Entry.Cols.VICTORIES, country.getVictories())
                .addProperty(Country.Entry.Cols.DRAWS, country.getDraws())
                .addProperty(Country.Entry.Cols.DEFEATS, country.getDefeats())
                .addProperty(Country.Entry.Cols.GOALS_FOR, country.getGoalsFor())
                .addProperty(Country.Entry.Cols.GOALS_AGAINST, country.getGoalsAgainst())
                .addProperty(Country.Entry.Cols.GOALS_DIFFERENCE, country.getGoalsDifference())
                .addProperty(Country.Entry.Cols.GROUP, country.getGroup())
                .addProperty(Country.Entry.Cols.POSITION, country.getPosition())
                .addProperty(Country.Entry.Cols.POINTS, country.getPoints())
                .addProperty(Country.Entry.Cols.COEFFICIENT, country.getCoefficient())
                .addProperty(Country.Entry.Cols.FAIR_PLAY_POINTS, country.getFairPlayPoints())
                .create();
    }

    public JsonObject getAsJsonObject(SystemData systemData) {

        return JsonObjectBuilder.instance()
                .addProperty("_" + SystemData.Entry.Cols.ID, systemData.getID())
                .addProperty(SystemData.Entry.Cols.APP_STATE, systemData.getAppState())
                .addProperty(SystemData.Entry.Cols.RULES, systemData.getRawRules())
                .addProperty(SystemData.Entry.Cols.SYSTEM_DATE, ISO8601.fromCalendar(systemData.getSystemDate()))
                .addProperty(SystemData.Entry.Cols.DATE_OF_CHANGE, ISO8601.fromCalendar(systemData.getDateOfChange()))
                .create();
    }

    public JsonObject getAsJsonObject(Match match) {

        return JsonObjectBuilder.instance()
                .addProperty("_" + Match.Entry.Cols.ID, match.getID())
                .addProperty(Match.Entry.Cols.MATCH_NUMBER, match.getMatchNumber())
                .addProperty(Match.Entry.Cols.HOME_TEAM_ID, match.getHomeTeamID())
                .addProperty(Match.Entry.Cols.AWAY_TEAM_ID, match.getAwayTeamID())
                .addProperty(Match.Entry.Cols.HOME_TEAM_GOALS, match.getHomeTeamGoals())
                .addProperty(Match.Entry.Cols.AWAY_TEAM_GOALS, match.getAwayTeamGoals())
                .addProperty(Match.Entry.Cols.HOME_TEAM_NOTES, match.getHomeTeamNotes())
                .addProperty(Match.Entry.Cols.AWAY_TEAM_NOTES, match.getAwayTeamNotes())
                .addProperty(Match.Entry.Cols.GROUP, match.getGroup())
                .addProperty(Match.Entry.Cols.STAGE, match.getStage())
                .addProperty(Match.Entry.Cols.STADIUM, match.getStadium())
                .addProperty(Match.Entry.Cols.DATE_AND_TIME, ISO8601.fromDate(match.getDateAndTime()))
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
