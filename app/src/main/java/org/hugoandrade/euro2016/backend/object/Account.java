package org.hugoandrade.euro2016.backend.object;

import android.net.Uri;

import org.hugoandrade.euro2016.backend.cloudsim.CloudDatabaseSimProvider;

public class Account {

    public static class Entry {
        public static final String TABLE_NAME = "Account";

        public final static String COLUMN__ID = "_id";
        public final static String COLUMN_USERNAME = "Username";
        public final static String COLUMN_PASSWORD = "Password";
        public final static String COLUMN_SCORE = "Score";

        public final static String REQUEST_TYPE = "Parameter_RequestType";
        public final static String REQUEST_TYPE_LOG_IN = "Login";
        public final static String REQUEST_TYPE_SIGN_UP = "Signup";

        // SQLite table mName
        // PATH & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 170;

        // PATH & TOKEN for single row of table
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

    private String mID;
    private String mUsername;
    private String mPassword;
    private int mScore;

    public Account(String id, String username, String password, int score) {
        mID = id;
        mUsername = username;
        mPassword = password;
        mScore = score;
    }

    public String getID() {
        return mID;
    }

    public String getUsername() {
        return mUsername;
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
}
