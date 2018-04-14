package org.hugoandrade.euro2016.predictor.admin.data;

import android.net.Uri;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

public class Prediction {

    private String mID;
    private String mUserID;
    private int mMatchNo;
    private int mHomeTeamGoals;
    private int mAwayTeamGoals;
    private int mScore;

    public static class Entry {

        public static final String TABLE_NAME = "Prediction";

        public static class Cols {
            public static final String ID = "id";
            public static final String USER_ID = "UserID";
            public static final String MATCH_NO = "MatchNumber";
            public static final String HOME_TEAM_GOALS = "HomeTeamGoals";
            public static final String AWAY_TEAM_GOALS = "AwayTeamGoals";
            public static final String SCORE = "Score";
        }

        // SQLite table mName
        // PATH_LOGIN & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 130;

        // PATH_LOGIN & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 140;

        // URI for this content.
        public static final Uri CONTENT_URI = CloudDatabaseSimProvider.BASE_URI.buildUpon()
                .appendPath(PATH).build();
    }

    public Prediction(String id, String userID, int matchNo, int homeTeamGoals, int awayTeamGoals, int score) {
        mID = id;
        mUserID = userID;
        mMatchNo = matchNo;
        mHomeTeamGoals = homeTeamGoals;
        mAwayTeamGoals = awayTeamGoals;
        mScore = score;
    }

    public String getID() {
        return mID;
    }

    public String getUserID() {
        return mUserID;
    }

    public int getMatchNumber() {
        return mMatchNo;
    }

    public int getHomeTeamGoals() {
        return mHomeTeamGoals;
    }

    public int getAwayTeamGoals() {
        return mAwayTeamGoals;
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }
}
