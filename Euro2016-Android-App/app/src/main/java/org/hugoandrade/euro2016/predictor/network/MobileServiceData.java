package org.hugoandrade.euro2016.predictor.network;

import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;

import java.util.List;

public class MobileServiceData implements Parcelable {

    public static final int LOGIN = 1;
    public static final int SIGN_UP = 2;
    public static final int UPDATE_SCORES_OF_PREDICTIONS = 3;
    public static final int GET_SYSTEM_DATA = 4;
    public static final int GET_MATCHES = 5;
    public static final int GET_COUNTRIES = 6;
    public static final int GET_PREDICTIONS = 7;
    public static final int GET_USERS = 8;
    public static final int UPDATE_SYSTEM_DATA = 9;
    public static final int UPDATE_COUNTRY = 10;
    public static final int UPDATE_MATCH = 11;
    public static final int DELETE_COUNTRY = 12;
    public static final int DELETE_MATCH = 13;
    public static final int INSERT_COUNTRY = 14;
    public static final int INSERT_MATCH = 15;
    public static final int INSERT_PREDICTION = 16;
    public static final int CREATE_LEAGUE = 17;
    public static final int JOIN_LEAGUE = 18;
    public static final int GET_LEAGUES = 19;
    public static final int GET_LEAGUE_TOP = 20;
    public static final int DELETE_LEAGUE = 21;
    public static final int LEAVE_LEAGUE = 22;
    public static final int FETCH_MORE_USERS = 23;
    public static final int FETCH_RANK_OF_USER = 24;
    public static final int LOGOUT = 25;
    public static final int FETCH_USERS_BY_STAGE = 26;
    public static final int FETCH_MORE_USERS_BY_STAGE = 27;

    public static final int REQUEST_RESULT_FAILURE = 0;
    public static final int REQUEST_RESULT_SUCCESS = 1;

    private int mOperationType;
    private int mOperationResult;
    private SystemData mSystemData;
    private Country mCountry;
    private List<Country> mCountryList;
    private Match mMatch;
    private List<Match> mMatchList;
    private User mUser;
    private List<User> mUserList;
    private List<LeagueUser> mLeagueUserList;
    private Prediction mPrediction;
    private List<Prediction> mPredictionList;
    private LoginData mLoginData;
    private League mLeague;
    private LeagueWrapper mLeagueWrapper;
    private List<League> mLeagueList;
    private List<LeagueWrapper> mLeagueWrapperList;
    private String mString;
    private String mMessage;

    public MobileServiceData(int operationType, int operationResult) {
        mOperationType = operationType;
        mOperationResult = operationResult;
    }

    public int getOperationType() {
        return mOperationType;
    }

    public int getOperationResult() {
        return mOperationResult;
    }

    public SystemData getSystemData() {
        return mSystemData;
    }

    private void setSystemData(SystemData systemData) {
        mSystemData = systemData;
    }

    public Country getCountry() {
        return mCountry;
    }

    private void setCountry(Country country) {
        mCountry = country;
    }

    public List<Country> getCountryList() {
        return mCountryList;
    }

    private void setCountryList(List<Country> countryList) {
        mCountryList = countryList;
    }

    public Match getMatch() {
        return mMatch;
    }

    private void setMatch(Match match) {
        mMatch = match;
    }

    public List<Match> getMatchList() {
        return mMatchList;
    }

    private void setMatchList(List<Match> matchList) {
        mMatchList = matchList;
    }

    public User getUser() {
        return mUser;
    }

    private void setUser(User user) {
        mUser = user;
    }

    public List<LeagueUser> getLeagueUserList() {
        return mLeagueUserList;
    }

    private void setLeagueUserList(List<LeagueUser> leagueUserList) {
        mLeagueUserList = leagueUserList;
    }

    public List<User> getUserList() {
        return mUserList;
    }

    private void setUserList(List<User> userList) {
        mUserList = userList;
    }

    public String getString() {
        return mString;
    }

    private void setString(String aString) {
        mString = aString;
    }

    public Prediction getPrediction() {
        return mPrediction;
    }

    private void setPrediction(Prediction prediction) {
        mPrediction = prediction;
    }

    public List<Prediction> getPredictionList() {
        return mPredictionList;
    }

    private void setPredictionList(List<Prediction> predictionList) {
        mPredictionList = predictionList;
    }

    public LoginData getLoginData() {
        return mLoginData;
    }

    private void setLoginData(LoginData loginData) {
        mLoginData = loginData;
    }

    public League getLeague() {
        return mLeague;
    }

    private void setLeague(League league) {
        mLeague = league;
    }

    public List<League> getLeagueList() {
        return mLeagueList;
    }

    private void setLeagueList(List<League> leagueList) {
        mLeagueList = leagueList;
    }

    public List<LeagueWrapper> getLeagueWrapperList() {
        return mLeagueWrapperList;
    }

    private void setLeagueWrapperList(List<LeagueWrapper> leagueWrapperList) {
        mLeagueWrapperList = leagueWrapperList;
    }

    public LeagueWrapper getLeagueWrapper() {
        return mLeagueWrapper;
    }

    private void setLeagueWrapper(LeagueWrapper leagueWrapper) {
        mLeagueWrapper = leagueWrapper;
    }

    public String getMessage() {
        return mMessage;
    }

    private void setMessage(String message) {
        mMessage = message;
    }

    public boolean wasSuccessful() {
        return getOperationResult() == REQUEST_RESULT_SUCCESS;
    }

    public static class Builder {
        MobileServiceData m;

        private Builder(int operationType, int operationResult) {
            m = new MobileServiceData(operationType, operationResult);
        }

        public static Builder instance(int operationType, int operationResult) {
            return new Builder(operationType, operationResult);
        }

        public Builder setSystemData(SystemData systemData) {
            m.setSystemData(systemData);
            return this;
        }

        public Builder setCountry(Country country) {
            m.setCountry(country);
            return this;
        }

        public Builder setCountryList(List<Country> countryList) {
            m.setCountryList(countryList);
            return this;
        }

        public Builder setMatch(Match match) {
            m.setMatch(match);
            return this;
        }

        public Builder setMatchList(List<Match> matchList) {
            m.setMatchList(matchList);
            return this;
        }

        public Builder setUser(User user) {
            m.setUser(user);
            return this;
        }

        public Builder setUserList(List<User> userList) {
            m.setUserList(userList);
            return this;
        }

        public Builder setLeagueUserList(List<LeagueUser> leagueUserList) {
            m.setLeagueUserList(leagueUserList);
            return this;
        }

        public Builder setString(String aString) {
            m.setString(aString);
            return this;
        }

        public Builder setPrediction(Prediction prediction) {
            m.setPrediction(prediction);
            return this;
        }

        public Builder setPredictionList(List<Prediction> predictionList) {
            m.setPredictionList(predictionList);
            return this;
        }

        public Builder setLoginData(LoginData loginData) {
            m.setLoginData(loginData);
            return this;
        }

        public Builder setLeague(League league) {
            m.setLeague(league);
            return this;
        }

        public Builder setLeagueList(List<League> leagueList) {
            m.setLeagueList(leagueList);
            return this;
        }

        public Builder setLeagueWrapper(LeagueWrapper league) {
            m.setLeagueWrapper(league);
            return this;
        }

        public Builder setLeagueWrapperList(List<LeagueWrapper> leagueList) {
            m.setLeagueWrapperList(leagueList);
            return this;
        }

        public Builder setMessage(String message) {
            m.setMessage(message);
            return this;
        }

        public MobileServiceData create() {
            return m;
        }
    }

    protected MobileServiceData(Parcel in) {
        mOperationType = in.readInt();
        mOperationResult = in.readInt();
        mSystemData = in.readParcelable(SystemData.class.getClassLoader());
        mCountry = in.readParcelable(Country.class.getClassLoader());
        mCountryList = in.createTypedArrayList(Country.CREATOR);
        mMatch = in.readParcelable(Match.class.getClassLoader());
        mMatchList = in.createTypedArrayList(Match.CREATOR);
        mUser = in.readParcelable(User.class.getClassLoader());
        mUserList = in.createTypedArrayList(User.CREATOR);
        mLeagueUserList = in.createTypedArrayList(LeagueUser.CREATOR);
        mPrediction = in.readParcelable(Prediction.class.getClassLoader());
        mPredictionList = in.createTypedArrayList(Prediction.CREATOR);
        mLoginData = in.readParcelable(LoginData.class.getClassLoader());
        mLeague = in.readParcelable(League.class.getClassLoader());
        mLeagueWrapper = in.readParcelable(LeagueWrapper.class.getClassLoader());
        mLeagueList = in.createTypedArrayList(League.CREATOR);
        mLeagueWrapperList = in.createTypedArrayList(LeagueWrapper.CREATOR);
        mMessage = in.readString();
        mString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mOperationType);
        dest.writeInt(mOperationResult);
        dest.writeParcelable(mSystemData, flags);
        dest.writeParcelable(mCountry, flags);
        dest.writeTypedList(mCountryList);
        dest.writeParcelable(mMatch, flags);
        dest.writeTypedList(mMatchList);
        dest.writeParcelable(mUser, flags);
        dest.writeTypedList(mUserList);
        dest.writeTypedList(mLeagueUserList);
        dest.writeParcelable(mPrediction, flags);
        dest.writeTypedList(mPredictionList);
        dest.writeParcelable(mLoginData, flags);
        dest.writeParcelable(mLeague, flags);
        dest.writeParcelable(mLeagueWrapper, flags);
        dest.writeTypedList(mLeagueList);
        dest.writeTypedList(mLeagueWrapperList);
        dest.writeString(mMessage);
        dest.writeString(mString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MobileServiceData> CREATOR = new Creator<MobileServiceData>() {
        @Override
        public MobileServiceData createFromParcel(Parcel in) {
            return new MobileServiceData(in);
        }

        @Override
        public MobileServiceData[] newArray(int size) {
            return new MobileServiceData[size];
        }
    };
}
