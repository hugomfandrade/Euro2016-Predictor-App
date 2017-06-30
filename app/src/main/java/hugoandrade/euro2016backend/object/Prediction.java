package hugoandrade.euro2016backend.object;

import java.util.HashMap;

public class Prediction {

    public static final String TABLE_NAME = "Prediction";
    public static final String COLUMN__ID = "_id";
    public static final String COLUMN_MATCH_NO = "MatchNo";
    public static final String COLUMN_HOME_TEAM_GOALS = "HomeTeamGoals";
    public static final String COLUMN_AWAY_TEAM_GOALS = "AwayTeamGoals";
    public static final String COLUMN_SCORE = "Score";
    public static final String COLUMN_USER_ID = "UserID";

    public static HashMap<String, String> PROJECTION_MAP;

}
