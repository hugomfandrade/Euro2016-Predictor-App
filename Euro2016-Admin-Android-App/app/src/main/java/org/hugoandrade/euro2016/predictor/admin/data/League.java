package org.hugoandrade.euro2016.predictor.admin.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

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
