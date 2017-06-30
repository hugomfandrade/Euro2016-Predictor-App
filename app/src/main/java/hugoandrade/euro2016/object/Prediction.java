package hugoandrade.euro2016.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;

import hugoandrade.euro2016.utils.NetworkUtils;

public class Prediction implements Parcelable {

    public static final String TABLE_NAME = "Prediction";
    private static final String COL_NAME__ID = "_id";
    public static final String COL_NAME_MATCH_NO = "MatchNo";
    private static final String COL_NAME_HOME_TEAM_GOALS = "HomeTeamGoals";
    private static final String COL_NAME_AWAY_TEAM_GOALS = "AwayTeamGoals";
    private static final String COL_NAME_SCORE = "Score";
    public static final String COL_NAME_USER_ID = "UserID";
    public static String PastMatchDate = "Past match date";

    private int _id;
    public String userID;
    public int matchNo;
    public int homeTeamGoals;
    public int awayTeamGoals;
    public int score;

    public Prediction(int awayTeamGoals, int homeTeamGoals, int matchNo, int score) {
        this.awayTeamGoals = awayTeamGoals;
        this.homeTeamGoals = homeTeamGoals;
        this.matchNo = matchNo;
        this.score = score;
    }

    public Prediction(int awayTeamGoals, int homeTeamGoals, int matchNo, String userID) {
        this.awayTeamGoals = awayTeamGoals;
        this.homeTeamGoals = homeTeamGoals;
        this.matchNo = matchNo;
        this.userID = userID;
    }

    protected Prediction(Parcel in) {
        _id = in.readInt();
        userID = in.readString();
        matchNo = in.readInt();
        homeTeamGoals = in.readInt();
        awayTeamGoals = in.readInt();
        score = in.readInt();
    }

    //NEW
    private Prediction(int _id, String userID, int matchNo, int homeTeamGoals, int awayTeamGoals, int score) {
        this._id = _id;
        this.userID = userID;
        this.matchNo = matchNo;
        this.homeTeamGoals = homeTeamGoals;
        this.awayTeamGoals = awayTeamGoals;
        this.score = score;
    }

    //NEW
    public Prediction(String userID, int matchNo, int homeTeamGoals, int awayTeamGoals) {
        this.userID = userID;
        this.matchNo = matchNo;
        this.homeTeamGoals = homeTeamGoals;
        this.awayTeamGoals = awayTeamGoals;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(userID);
        dest.writeInt(matchNo);
        dest.writeInt(homeTeamGoals);
        dest.writeInt(awayTeamGoals);
        dest.writeInt(score);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Prediction> CREATOR = new Creator<Prediction>() {
        @Override
        public Prediction createFromParcel(Parcel in) {
            return new Prediction(in);
        }

        @Override
        public Prediction[] newArray(int size) {
            return new Prediction[size];
        }
    };

    public static Prediction getInstanceFromJsonObject(JsonObject jsonObject) {
        return new Prediction(
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME__ID, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_USER_ID, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_MATCH_NO, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_HOME_TEAM_GOALS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_AWAY_TEAM_GOALS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_SCORE, -1));
    }

    public JsonObject getAsJsonObject() {
        JsonObject newPrediction = new JsonObject();
        newPrediction.addProperty(COL_NAME__ID, _id == -1? null: _id);
        newPrediction.addProperty(COL_NAME_USER_ID, userID);
        newPrediction.addProperty(COL_NAME_MATCH_NO, matchNo == -1? null: matchNo);
        newPrediction.addProperty(COL_NAME_HOME_TEAM_GOALS, homeTeamGoals == -1? null: homeTeamGoals);
        newPrediction.addProperty(COL_NAME_AWAY_TEAM_GOALS, awayTeamGoals == -1? null: awayTeamGoals);
        newPrediction.addProperty(COL_NAME_SCORE, score == -1? null: score);

        return newPrediction;
    }

    public Prediction cloneInstance() {
        return new Prediction(_id, userID, matchNo, homeTeamGoals, awayTeamGoals, score);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Prediction))
            return false;
        if (((Prediction) o)._id != this._id)
            return false;
        if (((Prediction) o).matchNo != this.matchNo)
            return false;
        if (((Prediction) o).homeTeamGoals != this.homeTeamGoals)
            return false;
        if (((Prediction) o).awayTeamGoals != this.awayTeamGoals)
            return false;
        if (((Prediction) o).score != this.score)
            return false;
        if (!areEqual(((Prediction) o).userID, this.userID))
            return false;
        return true;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean areEqual(String obj1, String obj2) {
        if (obj1 == null && obj2 == null)
            return true;
        if (obj1 != null && obj2 != null)
            return obj1.equals(obj2);

        return false;
    }
}
