package org.hugoandrade.euro2016.predictor.data.raw;

import android.os.Parcel;
import android.os.Parcelable;

public class LeagueUser implements Parcelable {

    private User mUser;
    private int mRank;

    public static class Entry {

        public static final String TABLE_NAME = "LeagueUser";

        public static class Cols {
            public static final String ID = "id";
            public static final String LEAGUE_ID = "LeagueID";
            public static final String USER_ID = "UserID";
            public static final String RANK = "Rank";
        }
    }

    public LeagueUser(User user, int rank) {
        mUser = user;
        mRank = rank;
    }

    public LeagueUser(User user) {
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }

    public int getRank() {
        return mRank;
    }

    protected LeagueUser(Parcel in) {
        mUser = in.readParcelable(User.class.getClassLoader());
        mRank = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mUser, flags);
        dest.writeInt(mRank);
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
