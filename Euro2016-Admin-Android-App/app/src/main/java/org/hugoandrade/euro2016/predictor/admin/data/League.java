package org.hugoandrade.euro2016.predictor.admin.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

public class League implements Parcelable {

    private String mID;
    private String mName;
    private String mAdminID;
    private String mCode;

    public static class Entry {

        public static final String TABLE_NAME = "League";
        public static final String API_NAME_CREATE_LEAGUE = "CreateLeague";
        public static final String API_NAME_JOIN_LEAGUE = "JoinLeague";
        public static final String API_NAME_LEAVE_LEAGUE = "LeaveLeague";
        public static final String API_NAME_DELETE_LEAGUE = "DeleteLeague";

        public static class Cols {
            public static final String ID = "id";
            public static final String NAME = "Name";
            public static final String ADMIN_ID = "AdminID";
            public static final String CODE = "Code";
            public static final String NUMBER_OF_MEMBERS = "NumberOfMembers";

            public static final String USER_ID = "UserID";
        }

        // SQLite table mName
        // PATH_LOGIN & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 230;

        // PATH_LOGIN & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 240;

        // URI for this content.
        public static final Uri CONTENT_URI = CloudDatabaseSimProvider.BASE_URI.buildUpon()
                .appendPath(PATH).build();

        public static final String PATH_CREATE_LEAGUE = API_NAME_CREATE_LEAGUE;
        public static final int PATH_CREATE_LEAGUE_TOKEN = 250;

        // PATH & TOKEN for single row of table
        public static final String PATH_JOIN_LEAGUE = API_NAME_JOIN_LEAGUE;
        public static final int PATH_JOIN_LEAGUE_TOKEN = 260;

        public static final String PATH_LEAVE_LEAGUE = API_NAME_LEAVE_LEAGUE;
        public static final int PATH_LEAVE_LEAGUE_TOKEN = 270;

        // PATH & TOKEN for single row of table
        public static final String PATH_DELETE_LEAGUE = API_NAME_DELETE_LEAGUE;
        public static final int PATH_DELETE_LEAGUE_TOKEN = 280;
    }

    public League(String id, String name, String adminID, String code) {
        mID = id;
        mName = name;
        mAdminID = adminID;
        mCode = code;
    }

    public League(String name, String adminID) {
        mName = name;
        mAdminID = adminID;
    }

    public String getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getAdminID() {
        return mAdminID;
    }

    public String getCode() {
        return mCode;
    }

    protected League(Parcel in) {
        mID = in.readString();
        mName = in.readString();
        mAdminID = in.readString();
        mCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mName);
        dest.writeString(mAdminID);
        dest.writeString(mCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<League> CREATOR = new Creator<League>() {
        @Override
        public League createFromParcel(Parcel in) {
            return new League(in);
        }

        @Override
        public League[] newArray(int size) {
            return new League[size];
        }
    };
}
