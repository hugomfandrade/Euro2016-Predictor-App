package org.hugoandrade.euro2016.predictor.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.User;

import java.util.ArrayList;
import java.util.List;

public class LeagueWrapper implements Parcelable {

    public static final String OVERALL_ID = "Overall_ID";
    public static final String OVERALL_NAME = "Predictor App members";

    private final League mLeague;
    private final List<User> mUserList;

    public LeagueWrapper(League league, List<User> userList) {
        mLeague = league;
        mUserList = userList;
    }

    public LeagueWrapper(League league) {
        mLeague = league;
        mUserList = new ArrayList<>();
    }

    public void setUserList(List<User> userList) {
        mUserList.addAll(userList);
    }

    public List<User> getUserList() {
        return mUserList;
    }

    public League getLeague() {
        return mLeague;
    }

    protected LeagueWrapper(Parcel in) {
        mLeague = in.readParcelable(League.class.getClassLoader());
        mUserList = in.createTypedArrayList(User.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLeague, flags);
        dest.writeTypedList(mUserList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LeagueWrapper> CREATOR = new Creator<LeagueWrapper>() {
        @Override
        public LeagueWrapper createFromParcel(Parcel in) {
            return new LeagueWrapper(in);
        }

        @Override
        public LeagueWrapper[] newArray(int size) {
            return new LeagueWrapper[size];
        }
    };

    @Override
    public String toString() {
        String s = "LeagueWrapper{" +
                "mLeague=" + mLeague.getName() +
                ", mUserList=";
        for (User user : mUserList)
            s += "username=" + user.getEmail() + ",";
        s += '}';
        return s;
    }

    public static LeagueWrapper createOverall(List<User> userList) {
        return new LeagueWrapper(
                new League(OVERALL_ID, OVERALL_NAME, null, null, 0),
                userList
        );
    }
}
