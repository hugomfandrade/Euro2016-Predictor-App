package org.hugoandrade.euro2016.predictor.model.parser;

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

import java.util.Calendar;
import java.util.List;

public class MobileClientData implements Parcelable {

    private final int mOperationType;
    private final int mOperationResult;

    private SystemData mSystemData;
    private Calendar mServerTime;
    private LoginData mLoginData;
    private Country mCountry;
    private List<Country> mCountryList;
    private Match mMatch;
    private List<Match> mMatchList;
    private User mUser;
    private List<User> mUserList;
    private List<LeagueUser> mLeagueUserList;
    private Prediction mPrediction;
    private List<Prediction> mPredictionList;
    private int mPredictionMatchNo;
    private String mPredictionUserID;
    private League mLeague;
    private LeagueWrapper mLeagueWrapper;
    private List<League> mLeagueList;
    private List<LeagueWrapper> mLeagueWrapperList;
    private String mErrorMessage;
    private boolean mNetworkState;
    private int mInteger;
    private String mString;

    // Data Extras Key
    public static final int REQUEST_RESULT_FAILURE = 1;
    public static final int REQUEST_RESULT_SUCCESS = 2;


    public enum OperationType {
        @SuppressWarnings("unused") OPERATION_UNKNOWN,

        GET_SYSTEM_DATA,
        LOGIN,
        LOGOUT,
        REGISTER,
        GET_INFO,
        GET_PREDICTIONS,
        GET_LATEST_PERFORMANCE,
        GET_PREDICTIONS_OF_USERS,
        PUT_PREDICTION,
        CREATE_LEAGUE,
        JOIN_LEAGUE,
        DELETE_LEAGUE,
        LEAVE_LEAGUE,
        FETCH_MORE_USERS,
        FETCH_USERS_BY_STAGE,
        FETCH_MORE_USERS_BY_STAGE,
    }

    /**
     * Private constructor. Initializes Message
     */
    private MobileClientData(int operationType, int operationResult) {
        mOperationResult = operationResult;
        mOperationType = operationType;
    }


    protected MobileClientData(Parcel in) {
        mOperationType = in.readInt();
        mOperationResult = in.readInt();

        mSystemData = in.readParcelable(SystemData.class.getClassLoader());
        mLoginData = in.readParcelable(LoginData.class.getClassLoader());

        mCountry = in.readParcelable(Country.class.getClassLoader());
        mCountryList = in.createTypedArrayList(Country.CREATOR);
        mMatch = in.readParcelable(Match.class.getClassLoader());
        mMatchList = in.createTypedArrayList(Match.CREATOR);
        mUser = in.readParcelable(User.class.getClassLoader());
        mUserList = in.createTypedArrayList(User.CREATOR);
        mLeagueUserList = in.createTypedArrayList(LeagueUser.CREATOR);

        mPrediction = in.readParcelable(Prediction.class.getClassLoader());
        mPredictionList = in.createTypedArrayList(Prediction.CREATOR);
        mPredictionMatchNo = in.readInt();
        mPredictionUserID = in.readString();

        mLeague = in.readParcelable(League.class.getClassLoader());
        mLeagueList = in.createTypedArrayList(League.CREATOR);
        mLeagueWrapper = in.readParcelable(LeagueWrapper.class.getClassLoader());
        mLeagueWrapperList = in.createTypedArrayList(LeagueWrapper.CREATOR);

        mErrorMessage = in.readString();
        mNetworkState = in.readByte() != 0;
        mServerTime = (Calendar) in.readSerializable();
        mInteger = in.readInt();
        mString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mOperationType);
        dest.writeInt(mOperationResult);

        dest.writeParcelable(mSystemData, flags);
        dest.writeParcelable(mLoginData, flags);

        dest.writeParcelable(mCountry, flags);
        dest.writeTypedList(mCountryList);
        dest.writeParcelable(mMatch, flags);
        dest.writeTypedList(mMatchList);
        dest.writeParcelable(mUser, flags);
        dest.writeTypedList(mUserList);
        dest.writeTypedList(mLeagueUserList);

        dest.writeParcelable(mPrediction, flags);
        dest.writeTypedList(mPredictionList);
        dest.writeInt(mPredictionMatchNo);
        dest.writeString(mPredictionUserID);

        dest.writeParcelable(mLeague, flags);
        dest.writeTypedList(mLeagueList);
        dest.writeParcelable(mLeagueWrapper, flags);
        dest.writeTypedList(mLeagueWrapperList);

        dest.writeString(mErrorMessage);
        dest.writeByte((byte) (mNetworkState ? 1 : 0));
        dest.writeSerializable(mServerTime);
        dest.writeInt(mInteger);
        dest.writeString(mString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MobileClientData> CREATOR = new Creator<MobileClientData>() {
        @Override
        public MobileClientData createFromParcel(Parcel in) {
            return new MobileClientData(in);
        }

        @Override
        public MobileClientData[] newArray(int size) {
            return new MobileClientData[size];
        }
    };

    /**
     * Factory Method
     */
    public static MobileClientData makeMessage(int operationType, int operationResult) {
        MobileClientData mobileClientData = new MobileClientData(operationType, operationResult);

        // Return the message to the caller.
        return mobileClientData;
    }

    public int getOperationType() {
        return mOperationType;
    }

    public int getOperationResult() {
        return mOperationResult;
    }

    public void setInteger(int integer) {
        mInteger = integer;
    }

    public int getInteger() {
        return mInteger;
    }

    public void setSystemData(SystemData systemData) {
        mSystemData = systemData;
    }

    public SystemData getSystemData() {
        return mSystemData;
    }

    public void setCountry(Country country) {
        mCountry = country;
    }

    public Country getCountry() {
        return mCountry;
    }

    public League getLeague() {
        return mLeague;
    }

    public void setLeague(League league) {
        mLeague = league;
    }

    public LeagueWrapper getLeagueWrapper() {
        return mLeagueWrapper;
    }

    public void setLeagueWrapper(LeagueWrapper leagueWrapper) {
        mLeagueWrapper = leagueWrapper;
    }

    public List<League> getLeagueList() {
        return mLeagueList;
    }

    public void setLeagueList(List<League> leagueList) {
        mLeagueList = leagueList;
    }

    public List<LeagueWrapper> getLeagueWrapperList() {
        return mLeagueWrapperList;
    }

    public void setLeagueWrapperList(List<LeagueWrapper> leagueWrapperList) {
        mLeagueWrapperList = leagueWrapperList;
    }

    public void setCountryList(List<Country> countryList) {
        mCountryList = countryList;
    }

    public List<Country> getCountryList() {
        return mCountryList;
    }

    public void setMatch(Match match) {
        mMatch = match;
    }

    public Match getMatch() {
        return mMatch;
    }

    public List<Match> getMatchList() {
        return mMatchList;
    }

    public void setMatchList(List<Match> matchList) {
        mMatchList = matchList;
    }

    public List<LeagueUser> getLeagueUserList() {
        return mLeagueUserList;
    }

    public void setLeagueUserList(List<LeagueUser> leagueUserList) {
        mLeagueUserList = leagueUserList;
    }

    public List<User> getUserList() {
        return mUserList;
    }

    public void setUsers(List<User> userList) {
        mUserList = userList;
    }

    public void setErrorMessage(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setPredictionMatchNumber(int matchNo) {
        mPredictionMatchNo = matchNo;
    }

    public int getPredictionMatchNumber() {
        return mPredictionMatchNo;
    }

    public void setPredictionUserID(String userID) {
        mPredictionUserID = userID;
    }

    public String getPredictionUserID() {
        return mPredictionUserID;
    }

    public void setPredictionList(List<Prediction> predictionList) {
        mPredictionList = predictionList;
    }

    public List<Prediction> getPredictionList() {
        return mPredictionList;
    }

    public void setPrediction(Prediction prediction) {
        mPrediction = prediction;
    }

    public Prediction getPrediction() {
        return mPrediction;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }

    public void setServerTime(Calendar serverTime) {
        mServerTime = serverTime;
    }

    public Calendar getServerTime() {
        return mServerTime;
    }

    public void setLoginData(LoginData loginData) {
        mLoginData = loginData;
    }

    public LoginData getLoginData() {
        return mLoginData;
    }

    public void setString(String aString) {
        mString = aString;
    }

    public String getString() {
        return mString;
    }

    public void setNetworkState(boolean state) {
        mNetworkState = state;
    }

    public boolean getNetworkState() {
        return mNetworkState;
    }
}
