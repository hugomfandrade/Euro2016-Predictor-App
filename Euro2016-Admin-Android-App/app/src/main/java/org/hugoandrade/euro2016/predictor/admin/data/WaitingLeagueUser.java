package org.hugoandrade.euro2016.predictor.admin.data;

import android.os.Parcel;
import android.os.Parcelable;

public class WaitingLeagueUser implements Parcelable {

    private String mUserID;
    private String mLeagueCode;

    public static class Entry {

        public static class Cols {
            public static final String LEAGUE_CODE = "LeagueCode";
            public static final String USER_ID = "UserID";
        }
    }

    public WaitingLeagueUser(String userID, String leagueCode) {
        mUserID = userID;
        mLeagueCode = leagueCode;
    }

    public String getUserID() {
        return mUserID;
    }

    public String getLeagueCode() {
        return mLeagueCode;
    }

    protected WaitingLeagueUser(Parcel in) {
        mUserID = in.readString();
        mLeagueCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserID);
        dest.writeString(mLeagueCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WaitingLeagueUser> CREATOR = new Creator<WaitingLeagueUser>() {
        @Override
        public WaitingLeagueUser createFromParcel(Parcel in) {
            return new WaitingLeagueUser(in);
        }

        @Override
        public WaitingLeagueUser[] newArray(int size) {
            return new WaitingLeagueUser[size];
        }
    };
}
