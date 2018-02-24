package org.hugoandrade.euro2016.predictor.admin.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

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

        // SQLite table mName
        // PATH_LOGIN & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 170;

        // PATH_LOGIN & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 180;

        // URI for this content.
        public static final Uri CONTENT_URI = CloudDatabaseSimProvider.BASE_URI.buildUpon()
                .appendPath(PATH).build();

        // CONTENT/MIME TYPE for this content
        private final static String MIME_TYPE_END = PATH;
        public static final String CONTENT_TYPE_DIR = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.dir/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
        public static final String CONTENT_ITEM_TYPE = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.item/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
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
