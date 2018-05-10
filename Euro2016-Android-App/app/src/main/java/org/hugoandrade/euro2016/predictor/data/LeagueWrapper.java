package org.hugoandrade.euro2016.predictor.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.User;

import java.util.ArrayList;
import java.util.List;

public class LeagueWrapper implements Parcelable {

    public static final String OVERALL_ID = "Overall_ID";

    private final League mLeague;
    private LeagueUser mMainUser;
    private List<LeagueUser> mUserList;

    public LeagueWrapper(League league, List<LeagueUser> userList) {
        mLeague = league;
        mUserList = userList;
    }

    public LeagueWrapper(League league) {
        mLeague = league;
        mUserList = new ArrayList<>();
    }

    public void setLeagueUserList(List<LeagueUser> userList) {
        mUserList = userList;
    }

    public List<LeagueUser> getLeagueUserList() {
        return mUserList;
    }

    public League getLeague() {
        return mLeague;
    }

    public LeagueUser getMainUser() {
        return mMainUser;
    }

    public void setMainUser(LeagueUser leagueUser) {
        mMainUser = leagueUser;
    }

    protected LeagueWrapper(Parcel in) {
        mLeague = in.readParcelable(League.class.getClassLoader());
        mMainUser = in.readParcelable(LeagueUser.class.getClassLoader());
        mUserList = in.createTypedArrayList(LeagueUser.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLeague, flags);
        dest.writeParcelable(mMainUser, flags);
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
                "mLeague=" + mLeague.getName() + "-" + mLeague.getNumberOfMembers() +
                ", mUserList=";
        for (LeagueUser user : mUserList)
            s += "username=" + (user.getUser() == null? "null" : user.getUser().getEmail() + "-" + user.getUser().getScore()) + ",";
        s += '}';
        return s;
    }

    /*public static LeagueWrapper createOverall(List<User> userList) {
        return new LeagueWrapper(
                new League(OVERALL_ID, OVERALL_NAME, null, null, 0),
                userList
        );
    }/**/
}
