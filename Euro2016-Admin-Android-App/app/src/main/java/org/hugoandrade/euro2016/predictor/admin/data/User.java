package org.hugoandrade.euro2016.predictor.admin.data;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String mID;
    private String mEmail;
    private String mPassword;
    private int mScore;

    public static class Entry {

        public static final String TABLE_NAME = "Account";

        public static class Cols {
            public final static String ID = "id";
            public final static String EMAIL = "Email";
            public final static String PASSWORD = "Password";
            public final static String SCORE = "Score";
        }
    }

    public User(String id, String email, String password, int score) {
        mID = id;
        mEmail = email;
        mPassword = password;
        mScore = score;
    }

    public User(String id, String email, int score) {
        mID = id;
        mEmail = email;
        mScore = score;
    }

    public User(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    public String getID() {
        return mID;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }

    protected User(Parcel in) {
        mID = in.readString();
        mEmail = in.readString();
        mPassword = in.readString();
        mScore = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mEmail);
        dest.writeString(mPassword);
        dest.writeInt(mScore);
    }
}
