package org.hugoandrade.euro2016.predictor.admin.object;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

public class LoginData implements Parcelable {

    public static class Entry {

        public static final String API_NAME_LOGIN = "Login";
        public static final String API_NAME_REGISTER = "Register";

        public static class Cols {
            public final static String USER_ID = "UserID";
            public final static String EMAIL = "Email";
            public final static String PASSWORD = "Password";
            public final static String TOKEN = "Token";
        }

        // SQLite table mName
        // PATH & TOKEN for entire table
        public static final String PATH_LOGIN = API_NAME_LOGIN;
        public static final int PATH_LOGIN_TOKEN = 210;

        // PATH & TOKEN for single row of table
        public static final String PATH_REGISTER = API_NAME_REGISTER;
        public static final int PATH_REGISTER_TOKEN = 220;

        // URI for this content.
        public static final Uri CONTENT_URI = CloudDatabaseSimProvider.BASE_URI.buildUpon()
                .appendPath(PATH_LOGIN).build();

        // CONTENT/MIME TYPE for this content
        private final static String MIME_TYPE_END = PATH_LOGIN;
        public static final String CONTENT_TYPE_DIR = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.dir/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
    }

    private String mUserID;
    private String mEmail;
    private String mPassword;
    private String mToken;

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

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
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
