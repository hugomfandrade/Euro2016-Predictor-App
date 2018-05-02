package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudPOJOFormatter;
import org.hugoandrade.euro2016.predictor.admin.data.League;
import org.hugoandrade.euro2016.predictor.admin.data.LeagueUser;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.Prediction;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.User;
import org.hugoandrade.euro2016.predictor.admin.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.PredictionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class CloudDatabaseSimHelper {

    private CloudPOJOFormatter cvParser = new CloudPOJOFormatter();
    private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();

    CloudDatabaseSimHelper() {
    }

    String[] getLeagues(SQLiteDatabase sQLiteDatabase, String selection) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(LeagueUser.Entry.TABLE_NAME);

        Cursor c = qb.query(sQLiteDatabase,
                null,
                selection,
                null,
                null,
                null,
                "_" + LeagueUser.Entry.Cols.ID);

        String[] r = new String[c.getCount()];
        int i = 0;
        if (c.moveToFirst()) {
            do {
                r[i] = cvParser.parseLeagueUser(c).getLeagueID();
                i++;

            } while (c.moveToNext());
        }
        c.close();
        return r;
    }

    SystemData getSystemData(DatabaseHelper dbHelper) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SystemData.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                null,
                null,
                null,
                null,
                "_" + SystemData.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            SystemData systemData = cvParser.parseSystemData(c);
            c.close();
            return systemData;
        }
        return null;
    }

    Calendar getMatchDate(DatabaseHelper dbHelper, int matchNumber) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Match.Entry.TABLE_NAME);
        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                Match.Entry.Cols.MATCH_NUMBER +  " = \"" + matchNumber + "\"",
                null, null, null,
                "_" + Match.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(cvParser.parseMatch(c).getDateAndTime());
            c.close();
            return calendar;
        }

        c.close();
        return null;
    }

    Prediction getPrediction(DatabaseHelper dbHelper, String userID, int matchNumber) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Prediction.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                Prediction.Entry.Cols.USER_ID + " = \"" + userID + "\" AND " +
                        Prediction.Entry.Cols.MATCH_NO + " = \"" + matchNumber + "\"",
                null, null, null,
                "_" + Prediction.Entry.Cols.ID);


        if (c.getCount() > 0 && c.moveToFirst()) {
            Prediction prediction = cvParser.parsePrediction(c);
            c.close();
            return prediction;
        }

        c.close();
        return null;
    }

    Calendar getSystemTime(DatabaseHelper dbHelper) {

        SystemData systemData = getSystemData(dbHelper);

        if (systemData == null)
            return null;

        return systemData.getSystemDate();
    }

    public List<League> getLeaguesOfUser(DatabaseHelper dbHelper, String userID) {
        List<League> leagueList = new ArrayList<>();

        String tableName = "League" +
                " INNER JOIN (SELECT LeagueID, COUNT(*) AS NumberOfMembers FROM LeagueUser GROUP BY LeagueID) AS t" +
                " ON League._id = t.LeagueID" +
                " INNER JOIN LeagueUser" +
                " ON League._id = LeagueUser.LeagueID";

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(tableName);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                LeagueUser.Entry.Cols.USER_ID + " = \"" + userID + "\"",
                null,
                null,
                null,
                "_" + LeagueUser.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            while (c.moveToNext()) {
                leagueList.add(cvParser.parseLeague(c));
            }
            c.close();
            return leagueList;
        }

        c.close();
        return leagueList;
    }

    League getLeague(DatabaseHelper dbHelper, String name) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(League.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                League.Entry.Cols.NAME + " = \"" + name + "\"",
                null,
                null,
                null,
                "_" + League.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            League league = cvParser.parseLeague(c);
            c.close();
            return league;
        }

        c.close();
        return null;
    }

    LeagueUser getLeagueUser(DatabaseHelper dbHelper, String leagueID, String userID) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(LeagueUser.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                LeagueUser.Entry.Cols.LEAGUE_ID + " = \"" + leagueID + "\""
                        + " AND "
                        + LeagueUser.Entry.Cols.USER_ID + " = \"" + userID + "\"",
                null,
                null,
                null,
                "_" + League.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            LeagueUser leagueUser = cvParser.parseLeagueUser(c);
            c.close();
            return leagueUser;
        }

        c.close();
        return null;
    }

    League getLeagueByID(DatabaseHelper dbHelper, String id) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(League.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                "_" + League.Entry.Cols.ID + " = \"" + id + "\"",
                null,
                null,
                null,
                "_" + League.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            League league = cvParser.parseLeague(c);
            c.close();
            return league;
        }

        c.close();
        return null;
    }

    League getLeagueByCode(DatabaseHelper dbHelper, String code) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(League.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                League.Entry.Cols.CODE + " = \"" + code + "\"",
                null,
                null,
                null,
                "_" + League.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            League league = cvParser.parseLeague(c);
            c.close();
            return league;
        }

        c.close();
        return null;
    }

    private static int CODE_LENGTH = 8;

    String generateUniqueLeagueCode(DatabaseHelper dbHelper) {
        String code = generateCode(CODE_LENGTH);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(League.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                League.Entry.Cols.CODE + " = \"" + code + "\"",
                null,
                null,
                null,
                "_" + League.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            // Generate another one
            c.close();
            return generateCode(CODE_LENGTH);
        }
        c.close();

        return code;
    }

    User getAccount(DatabaseHelper dbHelper, String email) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(User.Entry.TABLE_NAME);

        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                User.Entry.Cols.EMAIL + " = \"" + email + "\"",
                null,
                null,
                null,
                "_" + User.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            User user = cvParser.parseAccount(c);
            c.close();
            return user;
        }

        c.close();
        return null;
    }

    void updateScoresOfPredictionsOfAllMatches(DatabaseHelper dbHelper) {
        // Get System Data
        SystemData systemData = getSystemData(dbHelper);

        // Query by all Matches and update predictions scores of matches
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Match.Entry.TABLE_NAME);
        Cursor c = qb.query(dbHelper.getReadableDatabase(),	null, null, null, null, null, "_" + Match.Entry.Cols.ID);

        if (c.moveToFirst()) {
            do {
                Match match = cvParser.parseMatch(c);
                updateScoresOfPredictionsOfMatch(dbHelper, match, systemData);
            } while (c.moveToNext());
        }
        c.close();
    }

    void updateScoresOfPredictionsOfMatch(DatabaseHelper dbHelper, Match match, SystemData systemData) {
        if (systemData == null) return;

        // Get all Predictions with the MATCH_NUMBER
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Prediction.Entry.TABLE_NAME);
        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                Prediction.Entry.Cols.MATCH_NO + " = " + Integer.toString(match.getMatchNumber()),
                null, null, null,
                Prediction.Entry.Cols.MATCH_NO);

        if (c.moveToFirst()) {
            do {
                // Get prediction
                Prediction prediction = cvParser.parsePrediction(c);

                // Store previous score
                int oldScore = prediction.getScore() == -1? 0 : prediction.getScore();

                // Get new prediction score
                prediction = computePredictionScore(match, prediction, systemData);

                // Update entry
                ContentValues predictionValues = cvFormatter.getAsContentValues(prediction);
                predictionValues.remove("_" + Prediction.Entry.Cols.ID);

                int count = dbHelper.getWritableDatabase().update(
                        Prediction.Entry.TABLE_NAME,
                        predictionValues,
                        "_" + Prediction.Entry.Cols.ID + " = " + prediction.getID(),
                        null);

                // If there was a row updated, update account score
                if (count > 0) {

                    String userID = prediction.getUserID();
                    int diffScore = prediction.getScore() - oldScore;

                    updateAccountScore(dbHelper, userID, diffScore);
                }
            } while (c.moveToNext());
        }
        c.close();
    }

    private void updateAccountScore(DatabaseHelper dbHelper, String userID, int diffScore) {
        // Query account table
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(User.Entry.TABLE_NAME);
        Cursor c = qb.query(dbHelper.getReadableDatabase(),
                null,
                "_" + User.Entry.Cols.ID + " = " + userID,
                null, null, null, null);

        if (c.moveToFirst()) {
            do {
                // Update Account score
                User user = cvParser.parseAccount(c);
                user.setScore(user.getScore() + diffScore);

                ContentValues cvUser = cvFormatter.getAsContentValues(user);
                cvUser.remove("_" + User.Entry.Cols.ID);

                dbHelper.getWritableDatabase().update(User.Entry.TABLE_NAME,
                        cvFormatter.getAsContentValues(user),
                        "_" + User.Entry.Cols.ID + " = " + user.getID(),
                        null);
            } while (c.moveToNext());
        }
        c.close();
    }

    private Prediction computePredictionScore(Match match, Prediction prediction, SystemData systemData) {

        int incorrectPrediction = systemData.getRules().getRuleIncorrectPrediction();
        int correctOutcome = systemData.getRules().getRuleCorrectOutcome();
        int correctMarginOfVictory = systemData.getRules().getRuleCorrectMarginOfVictory();
        int correctPrediction = systemData.getRules().getRuleCorrectPrediction();

        if (!MatchUtils.isMatchPlayed(match)) {
            prediction.setScore(-1);
            return prediction;
        }
        if (!PredictionUtils.isPredictionSet(prediction)) {
            prediction.setScore(incorrectPrediction);
            return prediction;
        }

        // Both (match and prediction) home teams win
        if ((MatchUtils.didHomeTeamWin(match) && PredictionUtils.didPredictHomeTeamWin(prediction)) ||
                (MatchUtils.didAwayTeamWin(match) && PredictionUtils.didPredictAwayTeamWin(prediction))) {
            if (PredictionUtils.isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else if (PredictionUtils.isMarginOfVictoryCorrect(match, prediction))
                prediction.setScore(correctMarginOfVictory);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (MatchUtils.didTeamsTied(match) && PredictionUtils.didPredictTie(prediction) && !MatchUtils.wasThereAPenaltyShootout(match)) {
            if (PredictionUtils.isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (MatchUtils.didTeamsTied(match) && MatchUtils.wasThereAPenaltyShootout(match)) {
            if (MatchUtils.didHomeTeamWinByPenaltyShootout(match) && PredictionUtils.didPredictHomeTeamWin(prediction)) {
                prediction.setScore(correctOutcome);
                return prediction;
            }
            if (MatchUtils.didAwayTeamWinByPenaltyShootout(match) && PredictionUtils.didPredictAwayTeamWin(prediction)) {
                prediction.setScore(correctOutcome);
                return prediction;
            }
        }
        prediction.setScore(incorrectPrediction);
        return prediction;
    }

    public boolean haveRulesChanged(DatabaseHelper dbHelper, SystemData newSystemData) {

        SystemData preSystemData = getSystemData(dbHelper);

        return preSystemData == null || !preSystemData.getRawRules().equals(newSystemData.getRawRules());
    }

    private static String generateCode(int length) {
        String _sym = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < length; i++) {
            str.append(_sym.charAt((int) (Math.random() * (_sym.length()))));
        }

        return str.toString();
    }
}
