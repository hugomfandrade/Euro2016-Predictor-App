package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesParser;
import org.hugoandrade.euro2016.predictor.admin.object.LoginData;
import org.hugoandrade.euro2016.predictor.admin.object.User;
import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.Match;
import org.hugoandrade.euro2016.predictor.admin.object.Prediction;
import org.hugoandrade.euro2016.predictor.admin.object.SystemData;
import org.hugoandrade.euro2016.predictor.admin.utils.ISO8601;
import org.hugoandrade.euro2016.predictor.admin.utils.InitConfigUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.PredictionUtils;

public class CloudDatabaseSimProvider extends ContentProvider {

    @SuppressWarnings("unused")
    private final static String TAG = CloudDatabaseSimProvider.class.getSimpleName();

    // org name in java package format
    public static final String ORGANIZATIONAL_NAME = "org.hugoandrade";
    // name of this provider's project
    private static final String PROJECT_NAME = "euro2016.predictor";

    /**
     * ContentProvider Related Constants
     */
    public static final String AUTHORITY = ORGANIZATIONAL_NAME + "."
            + PROJECT_NAME + ".sim_provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    static final UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        // add default 'no match' result to matcher
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Entries URIs
        matcher.addURI(AUTHORITY, Match.Entry.PATH, Match.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Match.Entry.PATH_FOR_ID, Match.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, Prediction.Entry.PATH, Prediction.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Prediction.Entry.PATH_FOR_ID, Prediction.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, Country.Entry.PATH, Country.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Country.Entry.PATH_FOR_ID, Country.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, User.Entry.PATH, User.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, User.Entry.PATH_FOR_ID, User.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, LoginData.Entry.PATH_LOGIN, LoginData.Entry.PATH_LOGIN_TOKEN);
        matcher.addURI(AUTHORITY, LoginData.Entry.PATH_REGISTER, LoginData.Entry.PATH_REGISTER_TOKEN);
        matcher.addURI(AUTHORITY, SystemData.Entry.PATH, SystemData.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, SystemData.Entry.PATH_FOR_ID, SystemData.Entry.PATH_FOR_ID_TOKEN);

        return matcher;
    }

    private SQLiteDatabase db;
    private CloudContentValuesParser cvParser = new CloudContentValuesParser();
    private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();

    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());

        db = dbHelper.getWritableDatabase();

        return db != null;
    }

    /**
     * Implement this to handle requests for the MIME type of the data at the given URI.
     * The returned MIME type should start with vnd.android.cursor.item for a single record,
     * or vnd.android.cursor.dir/ for multiple items. This method can be called from multiple
     * threads, as described in Processes and Threads.
     */
    @Override
    synchronized public String getType(@NonNull Uri uri) {
        // Based on the Uri passed in, return the appropriate ContentType (Found in FeedContract)
        // I recommend you use a Switch statement.
        // You will need to use 'mUriMatcher' to match if the Uri matches SingleRow or MultipleRow
        // throw a new 'UnsupportedOperationException("URI: " + uri + " is not supported.")
        // if mUriMatcher returns an unsupported Uri.
        switch (mUriMatcher.match(uri)) {
            case User.Entry.PATH_TOKEN:
                return User.Entry.CONTENT_TYPE_DIR;
            case User.Entry.PATH_FOR_ID_TOKEN:
                return User.Entry.CONTENT_ITEM_TYPE;
            case Country.Entry.PATH_TOKEN:
                return Country.Entry.CONTENT_TYPE_DIR;
            case Country.Entry.PATH_FOR_ID_TOKEN:
                return Country.Entry.CONTENT_ITEM_TYPE;
            case Match.Entry.PATH_TOKEN:
                return Match.Entry.CONTENT_TYPE_DIR;
            case Match.Entry.PATH_FOR_ID_TOKEN:
                return Match.Entry.CONTENT_ITEM_TYPE;
            case Prediction.Entry.PATH_TOKEN:
                return Prediction.Entry.CONTENT_TYPE_DIR;
            case Prediction.Entry.PATH_FOR_ID_TOKEN:
                return Prediction.Entry.CONTENT_ITEM_TYPE;
            case SystemData.Entry.PATH_TOKEN:
                return SystemData.Entry.CONTENT_TYPE_DIR;
            case SystemData.Entry.PATH_FOR_ID_TOKEN:
                return SystemData.Entry.CONTENT_ITEM_TYPE;
            case LoginData.Entry.PATH_LOGIN_TOKEN:
                return LoginData.Entry.CONTENT_TYPE_DIR;
            case LoginData.Entry.PATH_REGISTER_TOKEN:
                return LoginData.Entry.CONTENT_TYPE_DIR;
            default:
                throw new UnsupportedOperationException("URI: " + uri + " is not supported.");
        }
    }

    /**
     * Retrieve data from your provider. Use the arguments to select the table to query,
     * the rows and columns to return, and the sort order of the result. Return the data as
     * a Cursor object.
     */
    @Override
    synchronized public Cursor query(@NonNull final Uri uri, final String[] projection,
                                     final String selection, final String[] selectionArgs,
                                     final String sortOrder) {

        String modifiedSelection = selection;
        String updatedSortOrder = sortOrder;
        String tableName;

        switch (mUriMatcher.match(uri)) {

            case SystemData.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = "_" + SystemData.Entry.Cols.ID + " ASC";

                tableName = SystemData.Entry.TABLE_NAME;

                break;
            }
            case SystemData.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();

                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = SystemData.Entry.TABLE_NAME;

                break;
            }

            case Match.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = "_" + Match.Entry.Cols.ID + " ASC";

                tableName = Match.Entry.TABLE_NAME;

                break;
            }

            case Match.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Match.Entry.TABLE_NAME;

                break;
            }

            case Country.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = "_" + Country.Entry.Cols.ID + " ASC";

                tableName = Country.Entry.TABLE_NAME;

                break;
            }

            case Country.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = "_" + Country.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Country.Entry.TABLE_NAME;

                break;
            }

            case User.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = "_" + User.Entry.Cols.ID + " ASC";

                tableName = User.Entry.TABLE_NAME;

                break;
            }

            case User.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = "_" + User.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = User.Entry.TABLE_NAME;

                break;
            }

            case Prediction.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = "_" + Prediction.Entry.Cols.ID + " ASC";

                tableName = Prediction.Entry.TABLE_NAME;

                break;
            }

            case Prediction.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = "_" + Prediction.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Prediction.Entry.TABLE_NAME;

                break;
            }

            case UriMatcher.NO_MATCH:
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri.toString());

        }

        Cursor cursor = query(uri,
                              tableName,
                              projection,
                              modifiedSelection,
                              selectionArgs,
                              updatedSortOrder);
        //Cursor cursor = qb.query(db,	projection,	selection, selectionArgs, null, null, sortOrder);

        /*
         * register to watch a content URI for changes
         */
        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri); /**/
        return cursor;
    }

    /**
     * Private query that does the actual query based on the table.
     * <p>
     * This method makes use of SQLiteQueryBuilder to build a simple query.
     */
    synchronized private Cursor query(final Uri uri, final String tableName,
                                      final String[] projection, final String selection,
                                      final String[] selectionArgs, final String sortOrder) {
        // Make a new SQLiteQueryBuilder object.
        // TODO -- you fill in here.
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // set the table(s) to be queried upon.
        // TODO -- you fill in here.
        builder.setTables(tableName);

        // return the builder.query(....) result, after passing the appropriate values.
        // TODO -- you fill in here.
        return builder.query(db,
                             projection,
                             selection,
                             selectionArgs,
                             null,
                             null,
                             sortOrder);
    }

    /**
     * Implement this to handle requests to insert a new row. As a courtesy,
     * call notifyChange() after inserting. This method can be called from multiple threads,
     * as described in Processes and Threads.
     * <p>
     * (non-Javadoc)
     *
     * @see ContentProvider#insert(Uri, ContentValues)
     */
    @Override
    synchronized public Uri insert(@NonNull Uri uri, ContentValues values) {

        values.remove("_id");

        // switch on the results of 'mUriMatcher' matching the Uri passed in,
        switch (mUriMatcher.match(uri)) {

            case SystemData.Entry.PATH_TOKEN: {

                SystemData preSystemData = getSystemData();
                SystemData newSystemData = cvParser.parseSystemData(values);

                boolean haveRulesChanged =
                        !preSystemData.getRawRules().equals(newSystemData.getRawRules());

                // Delete all
                db.delete(SystemData.Entry.TABLE_NAME, null, null);

                // Insert new SystemData
                long id = db.insert(SystemData.Entry.TABLE_NAME, null, values);

                if (id == -1) {
                    id = db.insert(SystemData.Entry.TABLE_NAME, null,
                            cvFormatter.getAsContentValues(InitConfigUtils.buildInitSystemData()));
                }

                if (haveRulesChanged)
                    updateAllPredictionScores();

                return ContentUris.withAppendedId(SystemData.Entry.CONTENT_URI, id);
            }
            case LoginData.Entry.PATH_LOGIN_TOKEN: {

                LoginData loginData = cvParser.parseLoginData(values);

                User dbUser = getAccount(loginData.getEmail());

                if (dbUser == null)
                    return Uri.parse("Email \'" + loginData.getEmail() + "\' does not exist.");

                return ContentUris.withAppendedId(User.Entry.CONTENT_URI,
                        Long.parseLong(dbUser.getID()));
            }
            case LoginData.Entry.PATH_REGISTER_TOKEN: {

                LoginData loginData = cvParser.parseLoginData(values);

                User dbUser = getAccount(loginData.getEmail());

                if (dbUser != null)
                    return Uri.parse("Username \'" + loginData.getEmail() + "\' already exists.");

                ContentValues cvUser = cvFormatter.getAsContentValues(
                        new User(null, loginData.getEmail(), loginData.getPassword(), 0));
                cvUser.remove("_" + User.Entry.Cols.ID);

                // Insert
                long rowID = db.insert(User.Entry.TABLE_NAME, null, cvUser);

                if (rowID == -1) {
                    return Uri.parse("Failed to sign up");
                }
                else {
                    return ContentUris.withAppendedId(User.Entry.CONTENT_URI, rowID);
                }
            }
            case Prediction.Entry.PATH_TOKEN: {
                Prediction prediction = cvParser.parsePrediction(values);

                // Check if Past Server Time
                Calendar systemDate = getSystemDate();
                Calendar matchDate = getMatchDate(prediction.getMatchNumber());

                if (systemDate == null)
                    return Uri.parse("Failed to retrieve systemDate.");

                if (matchDate == null)
                    return Uri.parse("Failed to retrieve date of match.");

                if (matchDate.before(systemDate))
                    return Uri.parse("Past match date:\t" + ISO8601.fromCalendar(systemDate));

                // Insert or update prediction
                if (isPredictionInDatabase(prediction)) {
                    // Update
                    int predictionID = getPrediction(prediction.getUserID(),
                                                     prediction.getMatchNumber()).getID();

                    int count = db.update(Prediction.Entry.TABLE_NAME,
                                          values,
                                          "_" + Prediction.Entry.Cols.ID + " = " + predictionID,
                                          null);

                    if (count <= 0)
                        return Uri.parse("Failed to insert prediction into " + uri);

                    Uri url = ContentUris.withAppendedId(Prediction.Entry.CONTENT_URI, predictionID);

                    notifyChanges(url, null);

                    return url;
                }
                else {

                    // Insert
                    long rowID = db.insert(Prediction.Entry.TABLE_NAME, null, values);

                    if (rowID < 0)
                        return Uri.parse("Failed to insert prediction into " + uri);

                    Uri url = ContentUris.withAppendedId(Prediction.Entry.CONTENT_URI, rowID);

                    notifyChanges(url, null);

                    return url;
                }
            }

            case User.Entry.PATH_TOKEN: {
                String requestType = getRequestType(values);

                if (requestType == null)
                    return Uri.parse("Request type not provided.");

                values.remove(User.Entry.REQUEST_TYPE);

                User user = cvParser.parseAccount(values);

                switch (requestType) {
                    case User.Entry.REQUEST_TYPE_LOG_IN: {

                        User dbUser = getAccount(user.getEmail());

                        if (dbUser == null)
                            return Uri.parse("Username \'" + user.getEmail() + "\' does not exist.");

                        //if (!dbUser.getPassword().equals(user.getPassword())) return Uri.parse("Password incorrect.");

                        // TODO Optional: hashing

                        return ContentUris.withAppendedId(User.Entry.CONTENT_URI,
                                Long.parseLong(dbUser.getID()));
                    }

                    case User.Entry.REQUEST_TYPE_SIGN_UP: {

                        User dbUser = getAccount(user.getEmail());

                        if (dbUser != null)
                            return Uri.parse("Username \'" + user.getEmail() + "\' already exists.");

                        user.setScore(0);

                        // Insert
                        long rowID = db.insert(User.Entry.TABLE_NAME, null, cvFormatter.getAsContentValues(user));

                        Uri url = ContentUris.withAppendedId(User.Entry.CONTENT_URI, rowID);

                        notifyChanges(url, null);

                        return Uri.parse("Failed to sign up into " + uri);
                    }

                    default:
                        return Uri.parse("Request type not recognized.");
                }
            }

            case Prediction.Entry.PATH_FOR_ID_TOKEN:
            case User.Entry.PATH_FOR_ID_TOKEN:
            case Match.Entry.PATH_TOKEN:
            case Match.Entry.PATH_FOR_ID_TOKEN:
            case Country.Entry.PATH_TOKEN:
            case Country.Entry.PATH_FOR_ID_TOKEN:
            default: {
                throw new IllegalArgumentException(
                        "Unsupported URI, unable to insert into specific row: " +
                                uri);
            }
        }
    }

    /**
     * Implement this to handle requests to delete one or more rows.
     */
    @Override
    synchronized public int delete(@NonNull Uri uri,
                                   @Nullable String whereClause,
                                   @Nullable String[] whereArgs
    ) throws IllegalArgumentException  {

        // switch on mUriMatcher.match
        switch (mUriMatcher.match(uri)) {
            case Prediction.Entry.PATH_TOKEN:
            case Prediction.Entry.PATH_FOR_ID_TOKEN:
            case User.Entry.PATH_TOKEN:
            case User.Entry.PATH_FOR_ID_TOKEN:
            case Match.Entry.PATH_TOKEN:
            case Match.Entry.PATH_FOR_ID_TOKEN:
            case SystemData.Entry.PATH_TOKEN:
            case Country.Entry.PATH_TOKEN:
            case Country.Entry.PATH_FOR_ID_TOKEN:
            case LoginData.Entry.PATH_LOGIN_TOKEN:
            case LoginData.Entry.PATH_REGISTER_TOKEN:
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * Implement this to handle requests to update one or more rows.
     */
    @Override
    synchronized public int update(@NonNull Uri uri,
                                   ContentValues values,
                                   @Nullable String whereClause,
                                   @Nullable String[] whereArgs
    ) throws IllegalArgumentException {

        // WhereClause copy for use in modifying the whereClause
        String modifiedWhereClause = whereClause;

        switch (mUriMatcher.match(uri)) {
            case Country.Entry.PATH_FOR_ID_TOKEN: {
                Log.e(TAG, "PathForID Country: " + values.toString());
                if (whereClause == null)
                    modifiedWhereClause = "_" + Country.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + "_" + Country.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                /*if (whereClause == null)
                    modifiedWhereClause = Country.Entry.Cols._ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + Country.Entry.Cols._ID + " = " + uri.getLastPathSegment();/**/

                values.remove("_" + Country.Entry.Cols.ID);

                return updateAndNotify(uri,
                                       Country.Entry.TABLE_NAME,
                                       values,
                                       modifiedWhereClause,
                                       whereArgs);
            }
            case Match.Entry.PATH_FOR_ID_TOKEN: {
                /*if (whereClause == null)
                    modifiedWhereClause = "_" + Match.Entry.Cols.ID + " = " + Integer.toString(values.getAsInteger("_" + Match.Entry.Cols.MATCH_NUMBER));
                else
                    modifiedWhereClause += " AND " + "_" + Match.Entry.Cols.ID + " = " + Integer.toString(values.getAsInteger("_" + Match.Entry.Cols.ID));

                /**/
                if (whereClause == null)
                    modifiedWhereClause = "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();/**/

                Match match = cvParser.parseMatch(values);

                values.remove("_" + Match.Entry.Cols.ID);

                int count = updateAndNotify(uri,
                                            Match.Entry.TABLE_NAME,
                                            values,
                                            modifiedWhereClause,
                                            whereArgs);

                if (count > 0)
                    updatePredictionScoresOfMatch(match, getSystemData());

                return count;
            }
            case Prediction.Entry.PATH_FOR_ID_TOKEN:
            case Prediction.Entry.PATH_TOKEN:
            case User.Entry.PATH_FOR_ID_TOKEN:
            case User.Entry.PATH_TOKEN:
            case Match.Entry.PATH_TOKEN:
            case SystemData.Entry.PATH_TOKEN:
            case Country.Entry.PATH_TOKEN:
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
    }

    private SystemData getSystemData() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SystemData.Entry.TABLE_NAME);
        Cursor c = qb.query(db,	null, null, null, null, null, "_" + SystemData.Entry.Cols.ID);

        SystemData systemData = null;
        if (c.getCount() > 0 && c.moveToFirst()) {
            systemData = cvParser.parseSystemData(c);
        }
        c.close();
        return systemData;
    }

    private Calendar getMatchDate(int matchNo) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Match.Entry.TABLE_NAME);
        Cursor c = qb.query(db,
                null,
                Match.Entry.Cols.MATCH_NUMBER +  " = \"" + matchNo + "\"",
                null, null, null,
                "_" + Match.Entry.Cols.ID);

        if (c.getCount() > 0 && c.moveToFirst()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(cvParser.parseMatch(c).getDateAndTime());
            c.close();
            return calendar;
        }

        c.close();
        return null;
    }

    private Prediction getPrediction(String userID, int matchNo) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Prediction.Entry.TABLE_NAME);

        Cursor c = qb.query(db,
                null,
                Prediction.Entry.Cols.USER_ID + " = \"" + userID + "\" AND " +
                        Prediction.Entry.Cols.MATCH_NO + " = \"" + matchNo + "\"",
                null, null, null,
                Prediction.Entry.Cols._ID);


        if (c.getCount() > 0 && c.moveToFirst()) {
            Prediction prediction = cvParser.parsePrediction(c);
            c.close();
            return prediction;
        }

        c.close();
        return null;
    }

    private Calendar getSystemDate() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SystemData.Entry.TABLE_NAME);
        Cursor c = qb.query(db,	null, null, null, null, null, "_" + SystemData.Entry.Cols.ID);

        if (c != null && c.getCount() > 0) {
            if (c.moveToFirst()) {
                SystemData systemData = cvParser.parseSystemData(c);
                c.close();
                return systemData.getSystemDate();
                /*Calendar savedSystemDate = systemData.getSystemData();
                Calendar dateOfChange = systemData.getDateOfChange();

                if (savedSystemDate != null && dateOfChange != null) {
                    Calendar systemDate = Calendar.getInstance();
                    long diff = systemDate.getTimeInMillis() - dateOfChange.getTimeInMillis();
                    systemDate.setTimeInMillis(savedSystemDate.getTimeInMillis() + diff);
                    return systemDate;
                }/**/
            }
        }
        c.close();
        return null;
    }

    private User getAccount(String username) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(User.Entry.TABLE_NAME);

        Cursor c = qb.query(db,
                null,
                User.Entry.Cols.EMAIL + " = \"" + username + "\"",
                null, null, null,
                "_" + User.Entry.Cols.ID);


        if (c.getCount() > 0 && c.moveToFirst()) {
            User user = cvParser.parseAccount(c);
            c.close();
            return user;
        }

        c.close();
        return null;
    }

    private String getRequestType(ContentValues values) {
        return values.getAsString(User.Entry.REQUEST_TYPE);
        /*for (Map.Entry<String, Object> entry : values.valueSet()) {
            if (entry.getKey().equals(Account.Entry.REQUEST_TYPE))
                if (entry.getValue() instanceof String) {
                    return (String) entry.getValue();
                }
        }
        return null; /**/
    }

    private boolean isPredictionInDatabase(Prediction prediction) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Prediction.Entry.TABLE_NAME);

        Cursor c = qb.query(db,
                null,
                Prediction.Entry.Cols.USER_ID + " = \"" + prediction.getUserID() + "\" AND " +
                        Prediction.Entry.Cols.MATCH_NO + " = \"" + prediction.getMatchNumber() + "\"",
                null, null, null,
                Prediction.Entry.Cols._ID);

        boolean isPredictionInDatabase = c.getCount() != 0;
        c.close();

        return isPredictionInDatabase;
    }

    private void updateAllPredictionScores() {
        // Get System Data
        SystemData systemData = getSystemData();

        // Query by all Matches and update predictions scores of matches
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Match.Entry.TABLE_NAME);
        Cursor c = qb.query(db,	null, null, null, null, null, "_" + Match.Entry.Cols.ID);

        if (c.moveToFirst()) {
            do {
                Match match = cvParser.parseMatch(c);
                updatePredictionScoresOfMatch(match, systemData);
            } while (c.moveToNext());
        }
        c.close();
    }

    private void updatePredictionScoresOfMatch(@NonNull Match match,
                                               @NonNull SystemData systemData) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Prediction.Entry.TABLE_NAME);

        // Get all Predictions with the MATCH_NUMBER
        Cursor c = qb.query(db,
                            null,
                            Prediction.Entry.Cols.MATCH_NO + " = " + Integer.toString(match.getMatchNumber()),
                            null, null, null,
                            Prediction.Entry.Cols.MATCH_NO);

        if (c.moveToFirst()) {
            do {
                // Get prediction
                Prediction prediction = cvParser.parsePrediction(c);

                // Store previous score
                int preScore = prediction.getScore() == -1? 0 : prediction.getScore();

                // Get new prediction score
                prediction =
                        computePredictionScore(match, prediction, systemData);

                // Update entry
                ContentValues predictionValues = cvFormatter.getAsContentValues(prediction);
                predictionValues.remove(Prediction.Entry.Cols._ID);
                int count = db.update(
                        Prediction.Entry.TABLE_NAME,
                        predictionValues,
                        Prediction.Entry.Cols._ID + " = " + Integer.toString(prediction.getID()),
                        null);

                // If there was a row updated, update account score
                if (count > 0) {

                    String userID = prediction.getUserID();
                    int newScore = prediction.getScore();

                    updateAccountScore(userID, newScore - preScore);
                }
            } while (c.moveToNext());
        }
        c.close();
    }


    private void updateAccountScore(String userID, int diffScore) {
        // TODO
        if (true)
            return;
        // Query account table
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(User.Entry.TABLE_NAME);
        Cursor c = qb.query(db,
                null,
                "_" + User.Entry.Cols.ID + " = " + userID,
                null, null, null,
                "_" + User.Entry.Cols.ID);

        if (c.moveToFirst()) {
            do {
                // Update Account score
                User user = cvParser.parseAccount(c);
                user.setScore(user.getScore() + diffScore);

                db.update(User.Entry.TABLE_NAME,
                          cvFormatter.getAsContentValues(user),
                          "_" + User.Entry.Cols.ID + " = " + user.getID(),
                          null);
            } while (c.moveToNext());
        }
        c.close();
    }

    /*
     * private update function that updates based on parameters, then notifies
     * change
     */
    private int updateAndNotify(@NonNull final Uri uri,
                                final String tableName,
                                final ContentValues values,
                                final String whereClause,
                                final String[] whereArgs) {
        // call update(...) on the DBAdapter instance variable, and store the count of rows updated.
        int count = db.update(tableName, values, whereClause, whereArgs);

        // if count > 0 then call notifyChanges(...) on the Uri (null observer), and return the
        // count.
        if (count > 0)
            notifyChanges(uri, null);

        return count;
    }

    private void notifyChanges(Uri uri, ContentObserver contentObserver) {
        Context context = getContext();
        if (context == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            return;
        }
        resolver.notifyChange(uri, contentObserver);
    }

    private Prediction computePredictionScore(Match match, Prediction prediction, SystemData systemData) {

        int incorrectPrediction = systemData.getRules().getRuleIncorrectPrediction();
        int correctOutcomeViaPenalties = systemData.getRules().getRuleCorrectOutcomeViaPenalties();
        int correctOutcome = systemData.getRules().getRuleCorrectOutcome();
        int correctPrediction = systemData.getRules().getRuleCorrectPrediction();

        if (!MatchUtils.isMatchPlayed(match)) {
            prediction.setScore(-1);
            return prediction;
        }
        if (!PredictionUtils.isPredictionSet(prediction)) {
            prediction.setScore(incorrectPrediction);
            return prediction;
        }

        // Both (match and prediction) home teams win
        if (MatchUtils.didHomeTeamWin(match) && PredictionUtils.didPredictHomeTeamWin(prediction)) {
            if (PredictionUtils.isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (MatchUtils.didAwayTeamWin(match) && PredictionUtils.didPredictAwayTeamWin(prediction)) {
            if (PredictionUtils.isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (MatchUtils.didTeamsTied(match) && PredictionUtils.didPredictTie(prediction) && !MatchUtils.wasThereAPenaltyShootout(match)) {
            if (PredictionUtils.isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (MatchUtils.didTeamsTied(match) && MatchUtils.wasThereAPenaltyShootout(match)) {
            if (MatchUtils.didHomeTeamWinByPenaltyShootout(match) && PredictionUtils.didPredictHomeTeamWin(prediction)) {
                prediction.setScore(correctOutcomeViaPenalties);
                return prediction;
            }
            if (MatchUtils.didAwayTeamWinByPenaltyShootout(match) && PredictionUtils.didPredictAwayTeamWin(prediction)) {
                prediction.setScore(correctOutcomeViaPenalties);
                return prediction;
            }
        }
        prediction.setScore(incorrectPrediction);
        return prediction;
    }
}
