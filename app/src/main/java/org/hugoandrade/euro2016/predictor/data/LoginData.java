package org.hugoandrade.euro2016.predictor.data;

import android.os.Parcel;
import android.os.Parcelable;

public class LoginData implements Parcelable {

    private String mUserID;
    private String mEmail;
    private String mPassword;
    private String mToken;

    public static class Entry {

        public static final String API_NAME_LOGIN = "Login";
        public static final String API_NAME_REGISTER = "Register";

        public static class Cols {
            public final static String USER_ID = "UserID";
            public final static String EMAIL = "Email";
            public final static String PASSWORD = "Password";
            public final static String TOKEN = "Token";
        }
    }

    public LoginData(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    public LoginData(String userID, String email, String password, String token) {
        mUserID = userID;
        mEmail = email;
        mPassword = password;
        mToken = token;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPassword() {
        return mPassword;
    }

    public String getUserID() {
        return mUserID;
    }

    public String getToken() {
        return mToken;
    }

    public LoginData(Parcel in) {
        mUserID = in.readString();
        mEmail = in.readString();
        mPassword = in.readString();
        mToken = in.readString();
    }

    public static final Creator<LoginData> CREATOR = new Creator<LoginData>() {
        @Override
        public LoginData createFromParcel(Parcel in) {
            return new LoginData(in);
        }

        @Override
        public LoginData[] newArray(int size) {
            return new LoginData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserID);
        dest.writeString(mEmail);
        dest.writeString(mPassword);
        dest.writeString(mToken);
    }
}
