package org.hugoandrade.euro2016.predictor.admin.network;

import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.User;

import java.util.List;

public class MobileServiceData implements Parcelable {

    public static final int LOGIN = 1;
    public static final int SIGN_UP = 1;
    public static final int UPDATE_SCORES_OF_PREDICTIONS = 3;
    public static final int GET_SYSTEM_DATA = 4;
    public static final int GET_MATCHES = 5;
    public static final int GET_COUNTRIES = 6;
    public static final int GET_USERS = 7;
    public static final int UPDATE_SYSTEM_DATA = 8;
    public static final int UPDATE_COUNTRY = 9;
    public static final int UPDATE_MATCH = 10;
    public static final int DELETE_COUNTRY = 11;
    public static final int DELETE_MATCH = 12;
    public static final int INSERT_COUNTRY = 13;
    public static final int INSERT_MATCH = 14;

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
    private LoginData mLoginData;
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

    public List<User> getUserList() {
        return mUserList;
    }

    private void setUserList(List<User> userList) {
        mUserList = userList;
    }

    public LoginData getLoginData() {
        return mLoginData;
    }

    private void setLoginData(LoginData loginData) {
        mLoginData = loginData;
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

        public Builder setLoginData(LoginData loginData) {
            m.setLoginData(loginData);
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
        mLoginData = in.readParcelable(LoginData.class.getClassLoader());
        mMessage = in.readString();
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
        dest.writeParcelable(mLoginData, flags);
        dest.writeString(mMessage);
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
