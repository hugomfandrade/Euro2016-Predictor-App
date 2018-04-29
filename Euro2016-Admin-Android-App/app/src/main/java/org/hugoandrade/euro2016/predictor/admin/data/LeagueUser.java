package org.hugoandrade.euro2016.predictor.admin.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

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

        // SQLite table mName
        // PATH_LOGIN & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 330;

        // PATH_LOGIN & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 340;

        // URI for this content.
        public static final Uri CONTENT_URI = CloudDatabaseSimProvider.BASE_URI.buildUpon()
                .appendPath(PATH).build();
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
