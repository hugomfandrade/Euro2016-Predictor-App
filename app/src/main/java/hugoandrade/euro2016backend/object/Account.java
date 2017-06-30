package hugoandrade.euro2016backend.object;

import java.util.HashMap;

public class Account {

    public static final String TABLE_NAME = "Account";
    public final static String COLUMN__ID = "_id";
    public final static String COLUMN_USERNAME = "Username";
    public final static String COLUMN_PASSWORD = "Password";
    public final static String COLUMN_SCORE = "Score";

    public static HashMap<String, String> PROJECTION_MAP;

}
