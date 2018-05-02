package org.hugoandrade.euro2016.predictor.data.raw;

import android.os.Parcel;
import android.os.Parcelable;

public class LeagueUser implements Parcelable {

    private String mID;
    private String mLeagueID;
    private String mUserID;

    public static class Entry {

        public static final String TABLE_NAME = "LeagueUser";

        public static class Cols {
            public static final String ID = "id";
            public static final String LEAGUE_ID = "LeagueID";
            public static final String USER_ID = "UserID";
        }
    }

    public LeagueUser(String id, String leagueID, String userID) {
        mID = id;
        mLeagueID = leagueID;
        mUserID = userID;
    }

    public String getID() {
        return mID;
    }

    public String getLeagueID() {
        return mLeagueID;
    }

    public String getUserID() {
        return mUserID;
    }

    protected LeagueUser(Parcel in) {
        mID = in.readString();
        mLeagueID = in.readString();
        mUserID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mLeagueID);
        dest.writeString(mUserID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LeagueUser> CREATOR = new Creator<LeagueUser>() {
        @Override
        public LeagueUser createFromParcel(Parcel in) {
            return new LeagueUser(in);
        }

        @Override
        public LeagueUser[] newArray(int size) {
            return new LeagueUser[size];
        }
    };
}
