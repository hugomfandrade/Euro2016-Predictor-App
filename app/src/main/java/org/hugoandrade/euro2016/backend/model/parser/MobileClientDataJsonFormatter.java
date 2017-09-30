package org.hugoandrade.euro2016.backend.model.parser;


import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.utils.ISO8601;

/**
 * Parses the objects to Json data.
 */
public class MobileClientDataJsonFormatter {


    public JsonObject getAsJsonObject(Country country) {

        return JsonObjectBuilder.instance()
                .addProperty(Country.Entry.COLUMN__ID, country.getID())
                .addProperty(Country.Entry.COLUMN_NAME, country.getName())
                .addProperty(Country.Entry.COLUMN_MATCHES_PLAYED, country.getMatchesPlayed())
                .addProperty(Country.Entry.COLUMN_VICTORIES, country.getVictories())
                .addProperty(Country.Entry.COLUMN_DRAWS, country.getDraws())
                .addProperty(Country.Entry.COLUMN_DEFEATS, country.getDefeats())
                .addProperty(Country.Entry.COLUMN_GOALS_FOR, country.getGoalsFor())
                .addProperty(Country.Entry.COLUMN_GOALS_AGAINST, country.getGoalsAgainst())
                .addProperty(Country.Entry.COLUMN_GOALS_DIFFERENCE, country.getGoalsDifference())
                .addProperty(Country.Entry.COLUMN_GROUP, country.getGroup())
                .addProperty(Country.Entry.COLUMN_POSITION, country.getPosition())
                .addProperty(Country.Entry.COLUMN_POINTS, country.getPoints())
                .create();
    }

    public JsonObject getAsJsonObject(SystemData systemData) {

        return JsonObjectBuilder.instance()
                .addProperty(SystemData.Entry.COLUMN__ID, systemData.getID())
                .addProperty(SystemData.Entry.COLUMN_APP_STATE, systemData.getAppState())
                .addProperty(SystemData.Entry.COLUMN_RULES, systemData.getRules())
                .addProperty(SystemData.Entry.COLUMN_SYSTEM_DATE, ISO8601.fromCalendar(systemData.getSystemDate()))
                .addProperty(SystemData.Entry.COLUMN_DATE_OF_CHANGE, ISO8601.fromCalendar(systemData.getDateOfChange()))
                .create();
    }

    public JsonObject getAsJsonObject(Match match) {
        return JsonObjectBuilder.instance().addProperty(Match.Entry.COLUMN__ID , match.getID())
                .addProperty(Match.Entry.COLUMN_MATCH_NO, match.getMatchNumber())
                .addProperty(Match.Entry.COLUMN_HOME_TEAM, match.getHomeTeam())
                .addProperty(Match.Entry.COLUMN_AWAY_TEAM, match.getAwayTeam())
                .addProperty(Match.Entry.COLUMN_HOME_TEAM_GOALS, match.getHomeTeamGoals())
                .addProperty(Match.Entry.COLUMN_AWAY_TEAM_GOALS, match.getAwayTeamGoals())
                .addProperty(Match.Entry.COLUMN_HOME_TEAM_NOTES, match.getHomeTeamNotes())
                .addProperty(Match.Entry.COLUMN_AWAY_TEAM_NOTES, match.getAwayTeamNotes())
                .addProperty(Match.Entry.COLUMN_GROUP, match.getGroup())
                .addProperty(Match.Entry.COLUMN_STAGE, match.getStage())
                .addProperty(Match.Entry.COLUMN_STADIUM, match.getStadium())
                .addProperty(Match.Entry.COLUMN_DATE_AND_TIME, ISO8601.fromDate(match.getDateAndTime()))
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
