package hugoandrade.euro2016backend.cloudsim;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;
import java.util.Map;

import hugoandrade.euro2016backend.object.Account;
import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.object.Match;
import hugoandrade.euro2016backend.object.Prediction;
import hugoandrade.euro2016backend.object.SystemData;
import hugoandrade.euro2016backend.utils.ISO8601;

public class CloudDatabaseSimProvider extends ContentProvider {

    private final static String TAG = CloudDatabaseSimProvider.class.getSimpleName();

    static final String PROVIDER_NAME = "hugoandrade.euro2016app.CloudDatabaseSimProvider";
    static final String URL = "content://" + PROVIDER_NAME ;

    @SuppressWarnings("unused") static final Uri CONTENT_URI = Uri.parse(URL);

    static final String DATABASE_NAME = "Euro2016App";
    static final int DATABASE_VERSION = 1;

    static final String COLUMN__ID = "_id";

    static final String CREATE_DB_TABLE_MATCH =
            " CREATE TABLE " + Match.TABLE_NAME + " (" +
                    " " + Match.COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Match.COLUMN_MATCH_NO + " INTEGER UNIQUE NOT NULL, " +
                    " " + Match.COLUMN_HOME_TEAM + " TEXT NOT NULL, " +
                    " " + Match.COLUMN_AWAY_TEAM + " TEXT NOT NULL, " +
                    " " + Match.COLUMN_HOME_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Match.COLUMN_AWAY_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Match.COLUMN_HOME_TEAM_NOTES + " TEXT NULL, " +
                    " " + Match.COLUMN_AWAY_TEAM_NOTES + " TEXT NULL, " +
                    " " + Match.COLUMN_DATE_AND_TIME + " TEXT NOT NULL, " +
                    " " + Match.COLUMN_STADIUM + " TEXT NOT NULL, " +
                    " " + Match.COLUMN_GROUP + " TEXT NULL, " +
                    " " + Match.COLUMN_STAGE + " TEXT NOT NULL " +
                    " );";

    static final String CREATE_DB_TABLE_SYSTEM_DATA =
            " CREATE TABLE " + SystemData.TABLE_NAME + " (" +
                    " " + SystemData.COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + SystemData.COLUMN_SYSTEM_DATE + " TEXT NOT NULL, " +
                    " " + SystemData.COLUMN_DATE_OF_CHANGE + " TEXT NOT NULL, " +
                    " " + SystemData.COLUMN_APP_STATE + " BOOLEAN NOT NULL, " +
                    " " + SystemData.COLUMN_RULES + " TEXT NOT NULL " +
                    " );";

    static final String CREATE_DB_TABLE_PREDICTION =
            " CREATE TABLE " + Prediction.TABLE_NAME + " (" +
                    " " + Prediction.COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Prediction.COLUMN_USER_ID + " TEXT NOT NULL, " +
                    " " + Prediction.COLUMN_MATCH_NO + " INTEGER NOT NULL, " +
                    " " + Prediction.COLUMN_HOME_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Prediction.COLUMN_AWAY_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Prediction.COLUMN_SCORE + " INTEGER NULL" +
                    " );";

    static final String CREATE_DB_TABLE_COUNTRY =
            " CREATE TABLE " + Country.TABLE_NAME + " (" +
                    " " + Country.COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Country.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                    " " + Country.COLUMN_MATCHES_PLAYED + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_GOALS_FOR + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_GOALS_AGAINST + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_GOALS_DIFFERENCE + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_VICTORIES + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_DRAWS + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_DEFEATS + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_GROUP + " TEXT NOT NULL, " +
                    " " + Country.COLUMN_POSITION + " INTEGER NOT NULL, " +
                    " " + Country.COLUMN_POINTS + " INTEGER NOT NULL" +
                    " );";

    static final String CREATE_DB_TABLE_ACCOUNT =
            " CREATE TABLE " + Account.TABLE_NAME + " (" +
                    " " + Account.COLUMN__ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Account.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    " " + Account.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    " " + Account.COLUMN_SCORE + " INTEGER NOT NULL" +
                    " );";

    static final int MATCHES = 1;
    static final int MATCH_ID = 2;
    static final int PREDICTIONS = 3;
    static final int PREDICTION_ID = 4;
    static final int COUNTRIES = 5;
    static final int COUNTRY_ID = 6;
    static final int ACCOUNTS = 7;
    static final int ACCOUNT_ID = 8;
    static final int SYSTEMDATAS = 9;
    static final int SYSTEMDATA_ID = 10;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, Match.TABLE_NAME, MATCHES);
        uriMatcher.addURI(PROVIDER_NAME, Match.TABLE_NAME + "/#", MATCH_ID);
        uriMatcher.addURI(PROVIDER_NAME, Prediction.TABLE_NAME, PREDICTIONS);
        uriMatcher.addURI(PROVIDER_NAME, Prediction.TABLE_NAME + "/#", PREDICTION_ID);
        uriMatcher.addURI(PROVIDER_NAME, Country.TABLE_NAME, COUNTRIES);
        uriMatcher.addURI(PROVIDER_NAME, Country.TABLE_NAME + "/*", COUNTRY_ID);
        uriMatcher.addURI(PROVIDER_NAME, Account.TABLE_NAME, ACCOUNTS);
        uriMatcher.addURI(PROVIDER_NAME, Account.TABLE_NAME + "/#", ACCOUNT_ID);
        uriMatcher.addURI(PROVIDER_NAME, SystemData.TABLE_NAME, SYSTEMDATAS);
        uriMatcher.addURI(PROVIDER_NAME, SystemData.TABLE_NAME + "/#", SYSTEMDATA_ID);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        db = dbHelper.getWritableDatabase();

        return db != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case MATCHES:
                qb.setTables(Match.TABLE_NAME);
                qb.setProjectionMap(Match.PROJECTION_MAP);
                break;

            case MATCH_ID:
                qb.setTables(Match.TABLE_NAME);
                qb.setProjectionMap(Match.PROJECTION_MAP);
                qb.appendWhere(Match.COLUMN__ID + "=" + uri.getPathSegments().get(1));
                break;

            case COUNTRIES:
                qb.setTables(Country.TABLE_NAME);
                qb.setProjectionMap(Country.PROJECTION_MAP);
                break;

            case COUNTRY_ID:
                qb.setTables(Country.TABLE_NAME);
                qb.setProjectionMap(Country.PROJECTION_MAP);
                qb.appendWhere(Country.COLUMN__ID + "=" + uri.getPathSegments().get(1));
                break;

            case ACCOUNTS:
                qb.setTables(Account.TABLE_NAME);
                qb.setProjectionMap(Account.PROJECTION_MAP);

                break;

            case ACCOUNT_ID:
                qb.setTables(Account.TABLE_NAME);
                qb.setProjectionMap(Account.PROJECTION_MAP);
                qb.appendWhere(Account.COLUMN__ID + "=" + uri.getPathSegments().get(1));

                break;

            case SYSTEMDATAS:
                qb.setTables(SystemData.TABLE_NAME);
                qb.setProjectionMap(SystemData.PROJECTION_MAP);

                break;

            case SYSTEMDATA_ID:
                qb.setTables(SystemData.TABLE_NAME);
                qb.setProjectionMap(SystemData.PROJECTION_MAP);
                qb.appendWhere(SystemData.COLUMN__ID + "=" + uri.getPathSegments().get(1));

                break;

            case PREDICTIONS:
                qb.setTables(Prediction.TABLE_NAME);
                qb.setProjectionMap(Prediction.PROJECTION_MAP);

                break;

            case PREDICTION_ID:
                qb.setTables(Prediction.TABLE_NAME);
                qb.setProjectionMap(Prediction.PROJECTION_MAP);
                qb.appendWhere(Prediction.COLUMN__ID + "=" + uri.getPathSegments().get(1));

                break;

        }

        if (sortOrder == null || sortOrder.equals("")){
            /*
             * By default sort on student names
             */
            sortOrder = COLUMN__ID;
        }



        Cursor c = qb.query(db,	projection,	selection, selectionArgs, null, null, sortOrder);

        /*
         * register to watch a content URI for changes
         */
        if (getContext() != null)
            c.setNotificationUri(getContext().getContentResolver(), uri); /**/
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case MATCHES:
                break;

            case MATCH_ID:
                try {
                    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                    qb.setTables(SystemData.TABLE_NAME);
                    qb.setProjectionMap(SystemData.PROJECTION_MAP);
                    Cursor c = qb.query(db,	null, null, null, null, null, SystemData.COLUMN__ID);

                    if (c == null || c.getCount() != 1)
                        return 0;

                    int[] criteria = new int[4];
                    if (c.moveToFirst()) {
                        String[] criteriaAsString = c.getString(c.getColumnIndex(SystemData.COLUMN_RULES)).split(",");
                        criteria[0] = Integer.parseInt(criteriaAsString[0]);
                        criteria[1] = Integer.parseInt(criteriaAsString[1]);
                        criteria[2] = Integer.parseInt(criteriaAsString[2]);
                        criteria[3] = Integer.parseInt(criteriaAsString[3]);
                    }
                    c.close();

                    /* ***************************************************************** */


                    count = db.update(Match.TABLE_NAME, values, // null, null);
                            Match.COLUMN__ID + " = " + uri.getPathSegments().get(1) +
                                    (!TextUtils.isEmpty(selection) ? " AND (" +
                                            selection + ')' : ""), selectionArgs); /**/
                    if (count > 0) {
                        updatePredictionScores(values, criteria[0], criteria[1], criteria[2], criteria[3]);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                break;
            case COUNTRIES:
                break;

            case COUNTRY_ID:
                try {
                    count = db.update(Country.TABLE_NAME, values,
                            Country.COLUMN_NAME + " = \'" + uri.getPathSegments().get(1) + "\'" +
                                    (!TextUtils.isEmpty(selection) ? " AND (" +
                                            selection + ')' : ""), selectionArgs);

                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                break;
            case SYSTEMDATAS:
                break;

            case SYSTEMDATA_ID:
                try {

                    boolean haveRulesChanged = true;
                    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                    qb.setTables(SystemData.TABLE_NAME);
                    qb.setProjectionMap(SystemData.PROJECTION_MAP);
                    Cursor c = qb.query(db,	null, null, null, null, null, SystemData.COLUMN__ID);

                    if (c == null || c.getCount() != 1)
                        haveRulesChanged = true;
                    else {
                        if (c.moveToFirst()) {
                            String prevRules = c.getString(c.getColumnIndex(SystemData.COLUMN_RULES));
                            if (prevRules.equals(values.getAsString(SystemData.COLUMN_RULES)))
                                haveRulesChanged = false;
                        }
                        c.close();
                    }

                    count = db.update(SystemData.TABLE_NAME, values,
                            SystemData.COLUMN__ID + " = \'" + uri.getPathSegments().get(1) + "\'" +
                                    (!TextUtils.isEmpty(selection) ? " AND (" +
                                            selection + ')' : ""), selectionArgs);

                    if (count == 1 && haveRulesChanged) {
                        updateAllPredictionScores();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        return count;
    }

    private void updateAllPredictionScores() {
        Log.d(TAG, "updateAllPredictionScores");
        try {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(SystemData.TABLE_NAME);
            qb.setProjectionMap(SystemData.PROJECTION_MAP);
            Cursor c = qb.query(db,	null, null, null, null, null, SystemData.COLUMN__ID);

            if (c == null || c.getCount() != 1) return;

            int[] criteria = new int[4];
            if (c.moveToFirst()) {
                String[] criteriaAsString = c.getString(c.getColumnIndex(SystemData.COLUMN_RULES)).split(",");
                criteria[0] = Integer.parseInt(criteriaAsString[0]);
                criteria[1] = Integer.parseInt(criteriaAsString[1]);
                criteria[2] = Integer.parseInt(criteriaAsString[2]);
                criteria[3] = Integer.parseInt(criteriaAsString[3]);
            }
            c.close();
            /* ***************************************************************** */

            SQLiteQueryBuilder newQb = new SQLiteQueryBuilder();
            newQb.setTables(Match.TABLE_NAME);
            newQb.setProjectionMap(Match.PROJECTION_MAP);
            Cursor newC = newQb.query(db,	null, null, null, null, null, Match.COLUMN__ID);

            if (newC == null) return;

            if (newC.moveToFirst()) {
                do{
                    ContentValues values = fromCursorToContentValues(newC);
                    updatePredictionScores(values, criteria[0], criteria[1], criteria[2], criteria[3]);
                } while (newC.moveToNext());
            }
            newC.close();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private ContentValues fromCursorToContentValues(Cursor c) {
        ContentValues contentValues = new ContentValues();
        for (String columnName : c.getColumnNames())
            contentValues.put(columnName, c.getString(c.getColumnIndex(columnName)));
        return contentValues;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {

            case PREDICTIONS:
                //try {
            {
                int matchNo;
                String userID = values.getAsString(Prediction.COLUMN_USER_ID);
                if (userID == null)
                    return Uri.parse("Failed to retrieve userID of content.");
                try {
                    matchNo = Integer.parseInt(values.getAsString(Prediction.COLUMN_MATCH_NO));
                } catch (NumberFormatException e) {
                    return Uri.parse("Failed to retrieve MatchNo of content.");
                }

                // Check if Past Server Time
                Calendar systemDate = getSystemDate(db);
                if (systemDate == null)
                    return Uri.parse("Failed to retrieve systemDate.");
                Calendar matchDate = getMatchDate(db, matchNo);
                if (matchDate == null)
                    return Uri.parse("Failed to retrieve date of match.");

                if (matchDate.before(systemDate))
                    return Uri.parse("Past match date:\t" + ISO8601.fromCalendar(systemDate));

                // Insert or update prediction
                SQLiteQueryBuilder newQb = new SQLiteQueryBuilder();
                newQb.setTables(Prediction.TABLE_NAME);
                newQb.setProjectionMap(Prediction.PROJECTION_MAP);

                Cursor tempC = newQb.query(db, null,
                        Prediction.COLUMN_USER_ID + " = \"" + userID + "\" AND " +
                                Prediction.COLUMN_MATCH_NO + " = \"" + matchNo + "\"",
                        null, null, null, Prediction.COLUMN__ID);

                if (tempC.getCount() == 0) {
                    tempC.close();
                    // about to insert
                    Log.e(TAG, "about to insert");
                    Log.e(TAG, values.toString());
                    values.remove(Prediction.COLUMN__ID);
                    long rowID = db.insertOrThrow(Prediction.TABLE_NAME, "", values);
                    if (rowID > 0) {
                        Uri _uri = ContentUris.withAppendedId(Uri.parse(URL + "/" + Prediction.TABLE_NAME), rowID);
                        if (getContext() != null)
                            getContext().getContentResolver().notifyChange(_uri, null);

                        return _uri;
                    }
                    return Uri.parse("Failed to insert prediction into " + uri);
                } else {
                    tempC.moveToFirst();
                    String predictionID = tempC.getString(tempC.getColumnIndex(Prediction.COLUMN__ID));
                    tempC.close();
                    // about to update
                    Log.e(TAG, "about to update");
                    // remove _id
                    values.put(Prediction.COLUMN__ID, predictionID);
                    values.remove(Prediction.COLUMN__ID);
                    int count = db.update(Prediction.TABLE_NAME, values, Prediction.COLUMN__ID + " = " + predictionID, null);
                    if (count > 0) {
                        Uri _uri = ContentUris.withAppendedId(Uri.parse(URL + "/" + Prediction.TABLE_NAME), Long.parseLong(predictionID));
                        if (getContext() != null)
                            getContext().getContentResolver().notifyChange(_uri, null);

                        return _uri;
                    } else {
                        return Uri.parse("Failed to insert prediction into " + uri);
                    }
                }



                /*} catch (Exception e) {
                    return Uri.parse("Failed to put prediction: " + e.getMessage());
                }/**/
                //return Uri.parse("Failed to put prediction: ");

            }

            case ACCOUNTS:
                String requestType = null;
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    if (entry.getKey().equals("Parameter_RequestType"))
                        if (entry.getValue() instanceof String) {
                            requestType = (String) entry.getValue();
                            break;
                        }
                }
                if (requestType == null)
                    return Uri.parse("Request type not provided.");
                    //throw new SQLException("Request type not provided.");

                String username = values.getAsString(Account.COLUMN_USERNAME);
                String password = values.getAsString(Account.COLUMN_PASSWORD);
                values.remove("Parameter_RequestType");
                values.put(Account.COLUMN_SCORE, 0);

                switch (requestType) {
                    case "Login":
                        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                        qb.setTables(Account.TABLE_NAME);
                        qb.setProjectionMap(Account.PROJECTION_MAP);

                        Cursor tempC = qb.query(db,	null, Account.COLUMN_USERNAME +  " = \"" + username + "\"",
                                null, null, null, Account.COLUMN__ID);
                        if (tempC.getCount() == 0)
                            return Uri.parse("Username \'" + username + "\' does not exist.");
                        tempC.close();

                        // TODO Optional: Do hashing on password and save hashCode
                        Cursor c = qb.query(db,	null, Account.COLUMN_USERNAME +  " = \"" + username + "\" AND " +
                                Account.COLUMN_PASSWORD + " = \"" + password + "\"", null, null, null, Account.COLUMN__ID);
                        if (c.getCount() == 0)
                            //throw new SQLException("Username-Password combination not found");
                            return Uri.parse("Username-Password combination not found");
                        else if (c.getCount() == 1 && c.moveToFirst()) {
                            Uri _uri = ContentUris.withAppendedId(Uri.parse(URL + "/" + Account.TABLE_NAME),
                                    c.getInt(c.getColumnIndex(Account.COLUMN__ID)));
                            c.close();
                            if (getContext() != null)
                                getContext().getContentResolver().notifyChange(_uri, null);

                            return _uri;
                        } else
                            //throw new SQLException("Error finding combination");
                            return Uri.parse("Error finding combination");
                    case "SignUp":

                        // TODO Optional: Do hashing on password before insertion
                        try {
                            long rowID = db.insertOrThrow(Account.TABLE_NAME, "", values);
                            if (rowID > 0) {
                                Uri _uri = ContentUris.withAppendedId(Uri.parse(URL + "/" + Account.TABLE_NAME), rowID);
                                if (getContext() != null)
                                    getContext().getContentResolver().notifyChange(_uri, null);

                                return _uri;
                            }
                        }
                        catch (android.database.sqlite.SQLiteConstraintException e) {
                            return Uri.parse("Username \'" + username + "\' already exists.");
                        }

                        return Uri.parse("Failed to sign up into " + uri);
                        //throw new SQLException("Failed to sign up into " + uri);

                    default:
                        return Uri.parse("Request type not recognized.");
                        //throw new SQLException("Request type not recognized.");
                }

            case ACCOUNT_ID:
                break;

        }

        /*Log.d(TAG, "insert: " + uri.toString());
        long rowID = db.insert(TABLE_NAME_MATCH, "", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(_uri, null);

            return _uri;
        }/**/

        throw new SQLException("Failed to add a record into " + uri);
    }

    private static Calendar getMatchDate(SQLiteDatabase db, int matchNo) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Match.TABLE_NAME);
        qb.setProjectionMap(Match.PROJECTION_MAP);
        Cursor c = qb.query(db,	null, Match.COLUMN_MATCH_NO +  " = \"" + matchNo + "\"", null, null, null, Match.COLUMN__ID);

        if (c != null && c.getCount() > 0 && c.moveToFirst())
            return ISO8601.toCalendar(c.getString(c.getColumnIndex(Match.COLUMN_DATE_AND_TIME)));

        return null;
    }

    private static Calendar getSystemDate(SQLiteDatabase db) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SystemData.TABLE_NAME);
        qb.setProjectionMap(SystemData.PROJECTION_MAP);
        Cursor c = qb.query(db,	null, null, null, null, null, SystemData.COLUMN__ID);

        if (c != null && c.getCount() > 0) {
            if (c.moveToFirst()) {
                Calendar savedSystemDate = ISO8601.toCalendar(c.getString(c.getColumnIndex(SystemData.COLUMN_SYSTEM_DATE)));
                Calendar dateOfChange = ISO8601.toCalendar(c.getString(c.getColumnIndex(SystemData.COLUMN_DATE_OF_CHANGE)));

                if (savedSystemDate != null && dateOfChange != null) {
                    Calendar systemDate = Calendar.getInstance();
                    long diff = systemDate.getTimeInMillis() - dateOfChange.getTimeInMillis();
                    systemDate.setTimeInMillis(savedSystemDate.getTimeInMillis() + diff);
                    return systemDate;
                }
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new IllegalArgumentException("Delete operation not supported");
        /*int count = 0;
        switch (uriMatcher.match(uri)){
            case STUDENTS:
                count = db.delete(STUDENTS_TABLE_NAME, selection, selectionArgs);
                break;

            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( STUDENTS_TABLE_NAME, _ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count; /**/
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new IllegalArgumentException("getType operation not supported");
        /*Log.d(TAG, "getType operation not supported");
        switch (uriMatcher.match(uri)){
            case MATCHES:
                return "vnd.android.cursor.dir/vnd.example.students";
            case MATCH_ID:
                return "vnd.android.cursor.item/vnd.example.students";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }/**/
    }

    private void updatePredictionScores(ContentValues values,
                                        int incorrectPrediction, int correctOutcomeViaPenalties,
                                        int correctOutcome, int correctPrediction) {
        Log.d(TAG, "(0)updatePredictionScores: " + values.toString());
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Prediction.TABLE_NAME);
        qb.setProjectionMap(Prediction.PROJECTION_MAP);


        Cursor c = qb.query(db,	null, Prediction.COLUMN_MATCH_NO + " = " + values.get(Prediction.COLUMN_MATCH_NO),
                null, null, null, Prediction.COLUMN_MATCH_NO);

        if (c.moveToFirst()) {
            do{
                ContentValues predictionValues =
                        computeScore(values, c,
                                incorrectPrediction, correctOutcomeViaPenalties,
                                correctOutcome, correctPrediction);

                Log.d(TAG, "(1)updatePredictionScores: " + predictionValues.toString());
                db.update(Prediction.TABLE_NAME, predictionValues,
                        Prediction.COLUMN__ID + " = " + predictionValues.getAsString(Prediction.COLUMN__ID), null);

                int userID = c.getInt(c.getColumnIndex(Prediction.COLUMN_USER_ID));
                int preScore = c.isNull(c.getColumnIndex(Prediction.COLUMN_SCORE))?
                        0 : c.getInt(c.getColumnIndex(Prediction.COLUMN_SCORE));
                int newScore = predictionValues.getAsInteger(Prediction.COLUMN_SCORE);

                updateAccountScore(userID, newScore - preScore);
            } while (c.moveToNext());
        }
        c.close();
    }

    private void updateAccountScore(int userID, int diffScore) {
        Log.d(TAG, "updateAccountScore: " + userID);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Account.TABLE_NAME);
        qb.setProjectionMap(Account.PROJECTION_MAP);
        Cursor c = qb.query(db,	null, Account.COLUMN__ID + " = " + userID,
                null, null, null, Account.COLUMN__ID);
        if (c.moveToFirst()) {
            do{
                ContentValues values = new ContentValues();
                values.put(Account.COLUMN__ID, c.getInt(c.getColumnIndex(Account.COLUMN__ID)));
                values.put(Account.COLUMN_USERNAME, c.getString(c.getColumnIndex(Account.COLUMN_USERNAME)));
                values.put(Account.COLUMN_PASSWORD, c.getString(c.getColumnIndex(Account.COLUMN_PASSWORD)));
                values.put(Account.COLUMN_SCORE, c.getInt(c.getColumnIndex(Account.COLUMN_SCORE)) + diffScore);

                db.update(Account.TABLE_NAME, values, Account.COLUMN__ID + " = " + values.getAsInteger(Account.COLUMN__ID), null);
            } while (c.moveToNext());
        }
        c.close();
    }

    private ContentValues computeScore(ContentValues values, Cursor predictionCursor,
                                       int incorrectPrediction, int correctOutcomeViaPenalties,
                                       int correctOutcome, int correctPrediction) {
        ContentValues predictionValues = new ContentValues();
        predictionValues.put(Prediction.COLUMN__ID,
                predictionCursor.getInt(predictionCursor.getColumnIndex(Prediction.COLUMN__ID)));
        predictionValues.put(Prediction.COLUMN_MATCH_NO,
                predictionCursor.getInt(predictionCursor.getColumnIndex(Prediction.COLUMN_MATCH_NO)));
        predictionValues.put(Prediction.COLUMN_HOME_TEAM_GOALS,
                predictionCursor.getInt(predictionCursor.getColumnIndex(Prediction.COLUMN_HOME_TEAM_GOALS)));
        predictionValues.put(Prediction.COLUMN_AWAY_TEAM_GOALS,
                predictionCursor.getInt(predictionCursor.getColumnIndex(Prediction.COLUMN_AWAY_TEAM_GOALS)));
        predictionValues.put(Prediction.COLUMN_USER_ID,
                predictionCursor.getString(predictionCursor.getColumnIndex(Prediction.COLUMN_USER_ID)));
        predictionValues.put(Prediction.COLUMN_SCORE,
                predictionCursor.getInt(predictionCursor.getColumnIndex(Prediction.COLUMN_SCORE)));

        if (values.get(Match.COLUMN_HOME_TEAM_GOALS) == null || values.get(Match.COLUMN_AWAY_TEAM_GOALS) == null) {
            predictionValues.put(Prediction.COLUMN_SCORE, (String) null);
            return predictionValues;
        }
        if (predictionValues.get(Prediction.COLUMN_HOME_TEAM_GOALS) == null || predictionValues.get(Prediction.COLUMN_AWAY_TEAM_GOALS) == null) {
            predictionValues.put(Prediction.COLUMN_SCORE, incorrectPrediction);
            return predictionValues;
        }

        int homeGoals = values.getAsInteger(Match.COLUMN_HOME_TEAM_GOALS);
        int awayGoals = values.getAsInteger(Match.COLUMN_AWAY_TEAM_GOALS);
        String homeTeamNotes = values.getAsString(Match.COLUMN_HOME_TEAM_NOTES);
        String awayTeamNotes = values.getAsString(Match.COLUMN_AWAY_TEAM_NOTES);
        int predictionHomeGoals = predictionValues.getAsInteger(Prediction.COLUMN_HOME_TEAM_GOALS);
        int predictionAwayGoals = predictionValues.getAsInteger(Prediction.COLUMN_AWAY_TEAM_GOALS);

        if (homeGoals > awayGoals && predictionHomeGoals > predictionAwayGoals) {
            int points = correctOutcome;
            if (homeGoals == predictionHomeGoals && awayGoals == predictionAwayGoals &&
                    awayTeamNotes == null && homeTeamNotes == null) {
                points = correctPrediction;
            }
            predictionValues.put(Prediction.COLUMN_SCORE, points);
            return predictionValues;
        }
        else if (homeGoals < awayGoals && predictionHomeGoals < predictionAwayGoals) {
            int points = correctOutcome;
            if (homeGoals == predictionHomeGoals && awayGoals == predictionAwayGoals &&
                    awayTeamNotes == null && homeTeamNotes == null) {
                points = correctPrediction;
            }
            predictionValues.put(Prediction.COLUMN_SCORE, points);
            return predictionValues;
        }
        else if (homeGoals == awayGoals && predictionHomeGoals == predictionAwayGoals &&
                awayTeamNotes == null && homeTeamNotes == null) {
            int points = correctOutcome;
            if (homeGoals == predictionHomeGoals /*&& awayGoals == predictionAwayGoals/**/) {
                points = correctPrediction;
            }
            predictionValues.put(Prediction.COLUMN_SCORE, points);
            return predictionValues;
        }
        else if (homeGoals == awayGoals && homeTeamNotes != null && homeTeamNotes.equals("p")) {
            if (predictionHomeGoals > predictionAwayGoals) {
                predictionValues.put(Prediction.COLUMN_SCORE, correctOutcomeViaPenalties);
                return predictionValues;
            }
        }
        else if (homeGoals == awayGoals && awayTeamNotes != null && awayTeamNotes.equals("p")) {
            if (predictionAwayGoals > predictionHomeGoals) {
                predictionValues.put(Prediction.COLUMN_SCORE, correctOutcomeViaPenalties);
                return predictionValues;
            }
        }
        predictionValues.put(Prediction.COLUMN_SCORE, incorrectPrediction);
        return predictionValues;
    }

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */

    private static class DatabaseHelper extends SQLiteOpenHelper {

        @SuppressWarnings("unused")
        private final static String TAG = DatabaseHelper.class.getSimpleName();

        private final static int TOTAL_MATCHES = 51;
        private final static int TOTAL_COUNTRIES = 24;

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE_MATCH);
            db.execSQL(CREATE_DB_TABLE_PREDICTION);
            db.execSQL(CREATE_DB_TABLE_COUNTRY);
            db.execSQL(CREATE_DB_TABLE_ACCOUNT);
            db.execSQL(CREATE_DB_TABLE_SYSTEM_DATA);
            populateTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  Match.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " +  Prediction.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " +  Country.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " +  Account.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " +  SystemData.TABLE_NAME);
            onCreate(db);
        }

        private void populateTable(SQLiteDatabase db) {
            for (int i = 0 ; i < TOTAL_MATCHES ; i++) {
                ContentValues values = getMatchContentValues(i);
                db.insert(Match.TABLE_NAME, null, values);
            }
            // populate PredictionTable
            db.insert(Prediction.TABLE_NAME, null, getPredictionContentValue(1, 4, 2, 0));
            db.insert(Prediction.TABLE_NAME, null, getPredictionContentValue(2, 4, 1, 1));
            db.insert(Prediction.TABLE_NAME, null, getPredictionContentValue(3, 4, 1, 0));
            db.insert(Prediction.TABLE_NAME, null, getPredictionContentValue(4, 4, 0, 1));
            db.insert(Prediction.TABLE_NAME, null, getPredictionContentValue(1, 5, 0, 1));
            db.insert(Prediction.TABLE_NAME, null, getPredictionContentValue(1, 6, 0, 1));

            // populate AccountTable
            db.insert(Account.TABLE_NAME, null, getAccountContentValue(1, "USER_1", "PASSWORD", 0));
            db.insert(Account.TABLE_NAME, null, getAccountContentValue(2, "USER_2", "PASSWORD", 0));
            db.insert(Account.TABLE_NAME, null, getAccountContentValue(3, "USER_3", "PASSWORD", 0));
            db.insert(Account.TABLE_NAME, null, getAccountContentValue(4, "USER_4", "PASSWORD", 0));

            // populate CountryTable
            for (int i = 0 ; i < TOTAL_COUNTRIES ; i++) {
                ContentValues values = getCountryContentValues(i);
                db.insert(Country.TABLE_NAME, null, values);
            }

            // populate SystemData
            db.insert(SystemData.TABLE_NAME, null, getSystemDataContentValue(Calendar.getInstance(), "0,1,2,4", true));
        }

        private ContentValues getCountryContentValues(int i) {
            switch (i) {
                case 0:
                    return getCountryContentValue("France", "A");
                case 1:
                    return getCountryContentValue("Albania", "A");
                case 2:
                    return getCountryContentValue("Wales", "B");
                case 3:
                    return getCountryContentValue("England", "B");
                case 4:
                    return getCountryContentValue("Turkey", "D");
                case 5:
                    return getCountryContentValue("Poland", "C");
                case 6:
                    return getCountryContentValue("Germany", "C");
                case 7:
                    return getCountryContentValue("Spain", "D");
                case 8:
                    return getCountryContentValue("Ireland", "E");
                case 9:
                    return getCountryContentValue("Belgium", "E");
                case 10:
                    return getCountryContentValue("Austria", "F");
                case 11:
                    return getCountryContentValue("Portugal", "F");
                case 12:
                    return getCountryContentValue("Romania", "A");
                case 13:
                    return getCountryContentValue("Switzerland", "A");
                case 14:
                    return getCountryContentValue("Slovakia", "B");
                case 15:
                    return getCountryContentValue("Russia", "B");
                case 16:
                    return getCountryContentValue("Croatia", "D");
                case 17:
                    return getCountryContentValue("Northern Ireland", "C");
                case 18:
                    return getCountryContentValue("Ukraine", "C");
                case 19:
                    return getCountryContentValue("Czech Republic", "D");
                case 20:
                    return getCountryContentValue("Sweden", "E");
                case 21:
                    return getCountryContentValue("Italy", "E");
                case 22:
                    return getCountryContentValue("Hungary", "F");
                case 23:
                    return getCountryContentValue("Iceland", "F");
            }
            return null;
        }

        private ContentValues getAccountContentValue(int id, String username, String password, int score) {
            ContentValues values = new ContentValues();
            values.put(Account.COLUMN__ID,  id);
            values.put(Account.COLUMN_USERNAME, username);
            values.put(Account.COLUMN_PASSWORD, password);
            values.put(Account.COLUMN_SCORE, score);
            return values;
        }

        private ContentValues getSystemDataContentValue(Calendar date, String rules, boolean appState) {
            ContentValues values = new ContentValues();
            values.put(SystemData.COLUMN_SYSTEM_DATE, ISO8601.fromCalendar(date));
            values.put(SystemData.COLUMN_DATE_OF_CHANGE, ISO8601.fromCalendar(date));
            values.put(SystemData.COLUMN_APP_STATE, appState);
            values.put(SystemData.COLUMN_RULES, rules);
            return values;
        }

        private ContentValues getCountryContentValue(String name, String group) {

            ContentValues values = new ContentValues();
            values.put(Country.COLUMN_NAME,  name);
            values.put(Country.COLUMN_MATCHES_PLAYED, 0);
            values.put(Country.COLUMN_VICTORIES, 0);
            values.put(Country.COLUMN_DRAWS, 0);
            values.put(Country.COLUMN_DEFEATS, 0);
            values.put(Country.COLUMN_GOALS_FOR, 0);
            values.put(Country.COLUMN_GOALS_AGAINST, 0);
            values.put(Country.COLUMN_GOALS_DIFFERENCE, 0);
            values.put(Country.COLUMN_GROUP, group);
            values.put(Country.COLUMN_POSITION, 0);
            values.put(Country.COLUMN_POINTS, 0);
            return values;
        }

        private ContentValues getMatchContentValues(int i) {
            switch (i) {
                case 0:
                    return getAsContentValues(i + 1, "France", "Romania", 2, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(10, Calendar.JUNE, 2016, 20, 0)),
                            "Stade de France (Saint-Denis)", "A", "Group Stage");
                case 1:
                    return getAsContentValues(i + 1, "Albania", "Switzerland", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(11, Calendar.JUNE, 2016, 14, 0)),
                            "Stade Bollaert-Delelis (Lens)", "A", "Group Stage");
                case 2:
                    return getAsContentValues(i + 1, "Wales", "Slovakia", 2, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(11, Calendar.JUNE, 2016, 17, 0)),
                            "Nouveau Stade de Bordeaux (Bordeaux)", "B", "Group Stage");
                case 3:
                    return getAsContentValues(i + 1, "England", "Russia", 1, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(11, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Vélodrome (Marseille)", "B", "Group Stage");
                case 4:
                    return getAsContentValues(i + 1, "Turkey", "Croatia", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(12, Calendar.JUNE, 2016, 14, 0)),
                            "Parc des Princes (Paris)", "D", "Group Stage");
                case 5:
                    return getAsContentValues(i + 1, "Poland", "Northern Ireland", 1, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(12, Calendar.JUNE, 2016, 17, 0)),
                            "Stade de Nice (Nice)", "C", "Group Stage");
                case 6:
                    return getAsContentValues(i + 1, "Germany", "Ukraine", 2, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(12, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Pierre-Mauroy (Lille)", "C", "Group Stage");
                case 7:
                    return getAsContentValues(i + 1, "Spain", "Czech Republic", 1, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(13, Calendar.JUNE, 2016, 14, 0)),
                            "Stadium Municipal (Toulose)", "D", "Group Stage");
                case 8:
                    return getAsContentValues(i + 1, "Ireland", "Sweden", 1, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(13, Calendar.JUNE, 2016, 17, 0)),
                            "Stade de France (Saint-Denis)", "E", "Group Stage");
                case 9:
                    return getAsContentValues(i + 1, "Belgium", "Italy", 0, 2,
                            ISO8601.fromCalendar(ISO8601.getDate(13, Calendar.JUNE, 2016, 20, 0)),
                            "Parc Olympique Lyonnais (Lyon)", "E", "Group Stage");
                case 10:
                    return getAsContentValues(i + 1, "Austria", "Hungary", 0, 2,
                            ISO8601.fromCalendar(ISO8601.getDate(14, Calendar.JUNE, 2016, 17, 0)),
                            "Nouveau Stade de Bordeaux (Bordeaux)", "F", "Group Stage");
                case 11:
                    return getAsContentValues(i + 1, "Portugal", "Iceland", 1, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(14, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Geoffroy-Guichard (Saint-Étienne)", "F", "Group Stage");
                /* ******************************************************************************* */
                case 12:
                    return getAsContentValues(i + 1, "Russia", "Slovakia", 1, 2,
                            ISO8601.fromCalendar(ISO8601.getDate(15, Calendar.JUNE, 2016, 14, 0)),
                            "Stade Pierre-Mauroy (Lille)", "B", "Group Stage");
                case 13:
                    return getAsContentValues(i + 1, "Romania", "Switzerland", 1, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(15, Calendar.JUNE, 2016, 17, 0)),
                            "Parc des Princes (Paris)", "A", "Group Stage");
                case 14:
                    return getAsContentValues(i + 1, "France", "Albania", 2, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(15, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Vélodrome (Marseille)", "A", "Group Stage");
                case 15:
                    return getAsContentValues(i + 1, "England", "Wales", 2, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(16, Calendar.JUNE, 2016, 14, 0)),
                            "Stade Bollaert-Delelis (Lens)", "B", "Group Stage");
                case 16:
                    return getAsContentValues(i + 1, "Ukraine", "Northern Ireland", 0, 2,
                            ISO8601.fromCalendar(ISO8601.getDate(16, Calendar.JUNE, 2016, 17, 0)),
                            "Parc Olympique Lyonnais (Lyon)", "C", "Group Stage");
                case 17:
                    return getAsContentValues(i + 1, "Germany", "Poland", 0, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(16, Calendar.JUNE, 2016, 20, 0)),
                            "Stade de France (Saint-Denis)", "C", "Group Stage");
                case 18:
                    return getAsContentValues(i + 1, "Italy", "Sweden", 1, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(17, Calendar.JUNE, 2016, 14, 0)),
                            "Stadium Municipal (Toulose)", "E", "Group Stage");
                case 19:
                    return getAsContentValues(i + 1, "Czech Republic", "Croatia", 2, 2,
                            ISO8601.fromCalendar(ISO8601.getDate(17, Calendar.JUNE, 2016, 17, 0)),
                            "Stade Geoffroy-Guichard (Saint-Étienne)", "D", "Group Stage");
                case 20:
                    return getAsContentValues(i + 1, "Spain", "Turkey", 3, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(17, Calendar.JUNE, 2016, 20, 0)),
                            "Stade de Nice (Nice)", "D", "Group Stage");
                case 21:
                    return getAsContentValues(i + 1, "Belgium", "Ireland", 3, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(18, Calendar.JUNE, 2016, 14, 0)),
                            "Nouveau Stade de Bordeaux (Bordeaux)", "E", "Group Stage");
                case 22:
                    return getAsContentValues(i + 1, "Iceland", "Hungary", 1, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(18, Calendar.JUNE, 2016, 17, 0)),
                            "Stade Vélodrome (Marseille)", "F", "Group Stage");
                case 23:
                    return getAsContentValues(i + 1, "Portugal", "Austria", 0, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(18, Calendar.JUNE, 2016, 20, 0)),
                            "Parc des Princes (Paris)", "F", "Group Stage");
                /* ****************************************************************************** */
                case 24:
                    return getAsContentValues(i + 1, "Switzerland", "France", 0, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(19, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Pierre-Mauroy (Lille)", "A", "Group Stage");
                case 25:
                    return getAsContentValues(i + 1, "Romania", "Albania", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(19, Calendar.JUNE, 2016, 20, 0)),
                            "Parc Olympique Lyonnais (Lyon)", "A", "Group Stage");
                case 26:
                    return getAsContentValues(i + 1, "Slovakia", "England", 0, 0,
                            ISO8601.fromCalendar(ISO8601.getDate(20, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Geoffroy-Guichard (Saint-Étienne)", "B", "Group Stage");
                case 27:
                    return getAsContentValues(i + 1, "Russia", "Wales", 0, 3,
                            ISO8601.fromCalendar(ISO8601.getDate(20, Calendar.JUNE, 2016, 20, 0)),
                            "Stadium Municipal (Toulose)", "B", "Group Stage");
                case 28:
                    return getAsContentValues(i + 1, "Northern Ireland", "Germany", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(21, Calendar.JUNE, 2016, 17, 0)),
                            "Parc des Princes (Paris)", "C", "Group Stage");
                case 29:
                    return getAsContentValues(i + 1, "Ukraine", "Poland", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(21, Calendar.JUNE, 2016, 17, 0)),
                            "Stade Vélodrome (Marseille)", "C", "Group Stage");
                case 30:
                    return getAsContentValues(i + 1, "Croatia", "Spain", 2, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(21, Calendar.JUNE, 2016, 20, 0)),
                            "Nouveau Stade de Bordeaux (Bordeaux)", "D", "Group Stage");
                case 31:
                    return getAsContentValues(i + 1, "Czech Republic", "Turkey", 0, 2,
                            ISO8601.fromCalendar(ISO8601.getDate(21, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Bollaert-Delelis (Lens)", "D", "Group Stage");
                case 32:
                    return getAsContentValues(i + 1, "Hungary", "Portugal", 3, 3,
                            ISO8601.fromCalendar(ISO8601.getDate(22, Calendar.JUNE, 2016, 17, 0)),
                            "Parc Olympique Lyonnais (Lyon)", "F", "Group Stage");
                case 33:
                    return getAsContentValues(i + 1, "Iceland", "Austria", 2, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(22, Calendar.JUNE, 2016, 17, 0)),
                            "Stade de France (Saint-Denis)", "F", "Group Stage");
                case 34:
                    return getAsContentValues(i + 1, "Sweden", "Belgium", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(22, Calendar.JUNE, 2016, 20, 0)),
                            "Stade de Nice (Nice)", "E", "Group Stage");
                case 35:
                    return getAsContentValues(i + 1, "Italy", "Ireland", 0, 1,
                            ISO8601.fromCalendar(ISO8601.getDate(22, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Pierre-Mauroy (Lille)", "E", "Group Stage");
                /* ******************************************************************************* */
                case 36:
                    return getAsContentValues(i + 1, "Runner-up Group A", "Runner-up Group C",
                            ISO8601.fromCalendar(ISO8601.getDate(25, Calendar.JUNE, 2016, 14, 0)),
                            "Stade Geoffroy-Guichard (Saint-Étienne)", null, "Round of 16");
                case 37:
                    return getAsContentValues(i + 1, "Winner Group B", "3rd Place A, C or D",
                            ISO8601.fromCalendar(ISO8601.getDate(25, Calendar.JUNE, 2016, 17, 0)),
                            "Parc des Princes (Paris)", null, "Round of 16");
                case 38:
                    return getAsContentValues(i + 1, "Winner Group D", "3rd Place B, E or F",
                            ISO8601.fromCalendar(ISO8601.getDate(25, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Bollaert-Delelis (Lens)", null, "Round of 16");
                case 39:
                    return getAsContentValues(i + 1, "Winner Group A", "3rd Place C, D or E",
                            ISO8601.fromCalendar(ISO8601.getDate(26, Calendar.JUNE, 2016, 14, 0)),
                            "Parc Olympique Lyonnais (Lyon)", null, "Round of 16");
                case 40:
                    return getAsContentValues(i + 1, "Winner Group C", "3rd Place A, B or F",
                            ISO8601.fromCalendar(ISO8601.getDate(26, Calendar.JUNE, 2016, 17, 0)),
                            "Stade Pierre-Mauroy (Lille)", null, "Round of 16");
                case 41:
                    return getAsContentValues(i + 1, "Winner Group F", "Runner-up Group E",
                            ISO8601.fromCalendar(ISO8601.getDate(26, Calendar.JUNE, 2016, 20, 0)),
                            "Stadium Municipal (Toulose)", null, "Round of 16");
                case 42:
                    return getAsContentValues(i + 1, "Winner Group E", "Runner-up Group D",
                            ISO8601.fromCalendar(ISO8601.getDate(27, Calendar.JUNE, 2016, 17, 0)),
                            "Stade de France (Saint-Denis)", null, "Round of 16");
                case 43:
                    return getAsContentValues(i + 1, "Runner-up Group B", "Runner-up Group F",
                            ISO8601.fromCalendar(ISO8601.getDate(27, Calendar.JUNE, 2016, 20, 0)),
                            "Stade de Nice (Nice)", null, "Round of 16");
                /* ******************************************************************************* */
                case 44:
                    return getAsContentValues(i + 1, "Winner Match 37", "Winner Match 39",
                            ISO8601.fromCalendar(ISO8601.getDate(30, Calendar.JUNE, 2016, 20, 0)),
                            "Stade Vélodrome (Marseille)", null, "Quarter Finals");
                case 45:
                    return getAsContentValues(i + 1, "Winner Match 38", "Winner Match 42",
                            ISO8601.fromCalendar(ISO8601.getDate(1, Calendar.JULY, 2016, 20, 0)),
                            "Stade Pierre-Mauroy (Lille)", null, "Quarter Finals");
                case 46:
                    return getAsContentValues(i + 1, "Winner Match 41", "Winner Match 43",
                            ISO8601.fromCalendar(ISO8601.getDate(2, Calendar.JULY, 2016, 20, 0)),
                            "Nouveau Stade de Bordeaux (Bordeaux)", null, "Quarter Finals");
                case 47:
                    return getAsContentValues(i + 1, "Winner Match 40", "Winner Match 44",
                            ISO8601.fromCalendar(ISO8601.getDate(3, Calendar.JULY, 2016, 20, 0)),
                            "Stade de France (Saint-Denis)", null, "Quarter Finals");
                /* ******************************************************************************* */
                case 48:
                    return getAsContentValues(i + 1, "Winner Match 45", "Winner Match 46",
                            ISO8601.fromCalendar(ISO8601.getDate(6, Calendar.JULY, 2016, 20, 0)),
                            "Parc Olympique Lyonnais (Lyon)", null, "Semi Finals");
                case 49:
                    return getAsContentValues(i + 1, "Winner Match 47", "Winner Match 48",
                            ISO8601.fromCalendar(ISO8601.getDate(7, Calendar.JULY, 2016, 20, 0)),
                            "Stade Vélodrome (Marseille)", null, "Semi Finals");
                /* ******************************************************************************* */
                case 50:
                    return getAsContentValues(i + 1, "Winner Match 49", "Winner Match 50",
                            ISO8601.fromCalendar(ISO8601.getDate(10, Calendar.JULY, 2016, 20, 0)),
                            "Stade de France (Saint-Denis)", null, "Final");
                default:
                    return null;
            }
        }

        private ContentValues getAsContentValues(int matchNo, String homeTeam, String awayTeam,
                                                 String date, String stadium, String group,
                                                 String stage) {
            ContentValues values = new ContentValues();
            values.put(Match.COLUMN_MATCH_NO, matchNo);
            values.put(Match.COLUMN_HOME_TEAM,  homeTeam);
            values.put(Match.COLUMN_AWAY_TEAM, awayTeam);
            values.put(Match.COLUMN_HOME_TEAM_GOALS, (String) null);
            values.put(Match.COLUMN_AWAY_TEAM_GOALS, (String) null);
            values.put(Match.COLUMN_DATE_AND_TIME, date);
            values.put(Match.COLUMN_STADIUM, stadium);
            values.put(Match.COLUMN_GROUP, group);
            values.put(Match.COLUMN_STAGE, stage);
            return values;
        }

        private ContentValues getAsContentValues(int matchNo, String homeTeam, String awayTeam,
                                                 int homeTeamGoalsScored, int awayTeamGoalsScored,
                                                 String date, String stadium, String group,
                                                 String stage) {
            ContentValues values = new ContentValues();
            values.put(Match.COLUMN_MATCH_NO, matchNo);
            values.put(Match.COLUMN_HOME_TEAM,  homeTeam);
            values.put(Match.COLUMN_AWAY_TEAM, awayTeam);
            values.put(Match.COLUMN_HOME_TEAM_GOALS, homeTeamGoalsScored);
            values.put(Match.COLUMN_AWAY_TEAM_GOALS, awayTeamGoalsScored);
            values.put(Match.COLUMN_DATE_AND_TIME, date);
            values.put(Match.COLUMN_STADIUM, stadium);
            values.put(Match.COLUMN_GROUP, group);
            values.put(Match.COLUMN_STAGE, stage);
            return values;
        }
        private ContentValues getPredictionContentValue(int userID, int matchNo, int homeGoals, int awayGoals) {
            ContentValues values = new ContentValues();
            values.put(Prediction.COLUMN_USER_ID,  userID);
            values.put(Prediction.COLUMN_MATCH_NO, matchNo);
            values.put(Prediction.COLUMN_HOME_TEAM_GOALS, homeGoals);
            values.put(Prediction.COLUMN_AWAY_TEAM_GOALS, awayGoals);
            return values;
        }
    }
}
