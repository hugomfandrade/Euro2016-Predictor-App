package org.hugoandrade.euro2016.predictor.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Prediction implements Parcelable {

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
        public static String PastMatchDate = "Past match date";
    }

    public Prediction(int awayTeamGoals, int homeTeamGoals, int matchNo, int score) {
        mAwayTeamGoals = awayTeamGoals;
        mHomeTeamGoals = homeTeamGoals;
        mMatchNo = matchNo;
        mScore = score;
    }

    public Prediction(int awayTeamGoals, int homeTeamGoals, int matchNo, String userID) {
        mAwayTeamGoals = awayTeamGoals;
        mHomeTeamGoals = homeTeamGoals;
        mMatchNo = matchNo;
        mUserID = userID;
        mScore = -1;
    }

    //NEW
    public Prediction(String id, String userID, int matchNo, int homeTeamGoals, int awayTeamGoals, int score) {
        mID = id;
        mUserID = userID;
        mMatchNo = matchNo;
        mHomeTeamGoals = homeTeamGoals;
        mAwayTeamGoals = awayTeamGoals;
        mScore = score;
    }

    //NEW
    public Prediction(String userID, int matchNo, int homeTeamGoals, int awayTeamGoals) {
        mUserID = userID;
        mMatchNo = matchNo;
        mHomeTeamGoals = homeTeamGoals;
        mAwayTeamGoals = awayTeamGoals;
        mScore = -1;
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

    public void setHomeTeamGoals(int homeTeamGoals) {
        mHomeTeamGoals = homeTeamGoals;
    }

    public int getAwayTeamGoals() {
        return mAwayTeamGoals;
    }

    public void setAwayTeamGoals(int awayTeamGoals) {
        mAwayTeamGoals = awayTeamGoals;
    }

    public int getScore() {
        return mScore;
    }

    @Override
    public String toString() {
        return "Prediction{" +
                "mID=" + mID +
                ", mUserID='" + mUserID + '\'' +
                ", mMatchNo=" + mMatchNo +
                ", mHomeTeamGoals=" + mHomeTeamGoals +
                ", mAwayTeamGoals=" + mAwayTeamGoals +
                ", mScore=" + mScore +
                '}';
    }

    protected Prediction(Parcel in) {
        mID = in.readString();
        mUserID = in.readString();
        mMatchNo = in.readInt();
        mHomeTeamGoals = in.readInt();
        mAwayTeamGoals = in.readInt();
        mScore = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mUserID);
        dest.writeInt(mMatchNo);
        dest.writeInt(mHomeTeamGoals);
        dest.writeInt(mAwayTeamGoals);
        dest.writeInt(mScore);
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

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Prediction))
            return false;
        if (!areEqual(((Prediction) o).mUserID, this.mUserID))
            return false;
        if (((Prediction) o).mMatchNo != this.mMatchNo)
            return false;
        if (((Prediction) o).mHomeTeamGoals != this.mHomeTeamGoals)
            return false;
        if (((Prediction) o).mAwayTeamGoals != this.mAwayTeamGoals)
            return false;
        if (((Prediction) o).mScore != this.mScore)
            return false;
        if (!areEqual(((Prediction) o).mUserID, this.mUserID))
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
