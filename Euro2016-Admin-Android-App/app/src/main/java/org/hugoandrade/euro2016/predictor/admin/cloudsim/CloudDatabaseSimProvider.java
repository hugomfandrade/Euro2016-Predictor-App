package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudPOJOFormatter;
import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.Prediction;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.User;
import org.hugoandrade.euro2016.predictor.admin.utils.ISO8601;
import org.hugoandrade.euro2016.predictor.admin.utils.InitConfigUtils;

import java.util.Calendar;

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
    public static final String AUTHORITY = ORGANIZATIONAL_NAME + "." + PROJECT_NAME + ".sim_provider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    static final UriMatcher mUriMatcher = buildUriMatcher();

    private DatabaseHelper mDbHelper;
    private CloudDatabaseSimHelper mSimHelper;

    private CloudPOJOFormatter cvParser = new CloudPOJOFormatter();
    private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();

    private static UriMatcher buildUriMatcher() {
        // add default 'no match' result to matcher
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Entries URIs
        matcher.addURI(AUTHORITY, LoginData.Entry.PATH_LOGIN, LoginData.Entry.PATH_LOGIN_TOKEN);
        matcher.addURI(AUTHORITY, LoginData.Entry.PATH_REGISTER, LoginData.Entry.PATH_REGISTER_TOKEN);
        matcher.addURI(AUTHORITY, SystemData.Entry.PATH, SystemData.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, SystemData.Entry.PATH_UPDATE_SCORES, SystemData.Entry.PATH_UPDATE_SCORES_TOKEN);
        matcher.addURI(AUTHORITY, Match.Entry.PATH, Match.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Match.Entry.PATH_FOR_ID, Match.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, Prediction.Entry.PATH, Prediction.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Prediction.Entry.PATH_FOR_ID, Prediction.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, Country.Entry.PATH, Country.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Country.Entry.PATH_FOR_ID, Country.Entry.PATH_FOR_ID_TOKEN);
        matcher.addURI(AUTHORITY, User.Entry.PATH, User.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, User.Entry.PATH_FOR_ID, User.Entry.PATH_FOR_ID_TOKEN);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        mSimHelper = new CloudDatabaseSimHelper();
        return true;
    }

    @Override
    synchronized public String getType(@NonNull Uri uri) {
        throw new RuntimeException("getType is not implemented.");
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
    synchronized private Cursor query(@SuppressWarnings("unused") final Uri uri,
                                      final String tableName,
                                      final String[] projection, final String selection,
                                      final String[] selectionArgs, final String sortOrder) {
        // Make a new SQLiteQueryBuilder object.
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // set the table(s) to be queried upon.
        builder.setTables(tableName);

        // return the builder.query(....) result, after passing the appropriate values.
        return builder.query(mDbHelper.getReadableDatabase(),
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

        // switch on the results of 'mUriMatcher' matching the Uri passed in,
        switch (mUriMatcher.match(uri)) {

            case SystemData.Entry.PATH_UPDATE_SCORES_TOKEN : {
                mSimHelper.updateScoresOfPredictionsOfAllMatches(mDbHelper);
                return null;
            }

            case SystemData.Entry.PATH_TOKEN: {

                values.remove("_" + SystemData.Entry.Cols.ID);
                values.put(SystemData.Entry.Cols.DATE_OF_CHANGE, ISO8601.fromCalendar(Calendar.getInstance()));

                boolean haveRulesChanged = mSimHelper
                        .haveRulesChanged(mDbHelper, cvParser.parseSystemData(values));

                // Delete all
                delete(SystemData.Entry.TABLE_NAME, null, null);

                // Insert new SystemData
                long id = insert(SystemData.Entry.TABLE_NAME, null, values);

                if (id == -1) {
                    SystemData defaultInitConfig = InitConfigUtils.buildInitSystemData();

                    id = insert(SystemData.Entry.TABLE_NAME,
                                null,
                                cvFormatter.getAsContentValues(defaultInitConfig));
                }

                if (haveRulesChanged)
                    mSimHelper.updateScoresOfPredictionsOfAllMatches(mDbHelper);

                return ContentUris.withAppendedId(SystemData.Entry.CONTENT_URI, id);
            }
            case LoginData.Entry.PATH_LOGIN_TOKEN: {

                LoginData loginData = cvParser.parseLoginData(values);

                User dbUser = mSimHelper.getAccount(mDbHelper, loginData.getEmail());

                if (dbUser == null)
                    throw new IllegalArgumentException("Email \'" + loginData.getEmail() + "\' does not exist.");

                return ContentUris.withAppendedId(User.Entry.CONTENT_URI,
                        Long.parseLong(dbUser.getID()));
            }
            case LoginData.Entry.PATH_REGISTER_TOKEN: {

                LoginData loginData = cvParser.parseLoginData(values);

                User dbUser = mSimHelper.getAccount(mDbHelper, loginData.getEmail());

                if (dbUser != null)
                    throw new IllegalArgumentException("Username \'" + loginData.getEmail() + "\' already exists.");

                // Add new user
                User newUser = new User(null, loginData.getEmail(), loginData.getPassword(), 0);
                ContentValues cvUser = cvFormatter.getAsContentValues(newUser);
                cvUser.remove("_" + User.Entry.Cols.ID);

                // Insert
                long rowID = insert(User.Entry.TABLE_NAME, null, cvUser);

                if (rowID == -1) {
                    throw new IllegalArgumentException("Failed to sign up");
                }
                else {
                    return ContentUris.withAppendedId(User.Entry.CONTENT_URI, rowID);
                }
            }
            case Prediction.Entry.PATH_TOKEN: {
                Prediction prediction = cvParser.parsePrediction(values);

                // Check if Past Server Time
                Calendar systemTime = mSimHelper.getSystemTime(mDbHelper);

                if (systemTime == null)
                    throw new IllegalArgumentException("Failed to retrieve systemDate.");

                Calendar matchDate = mSimHelper.getMatchDate(mDbHelper, prediction.getMatchNumber());

                if (matchDate == null)
                    throw new IllegalArgumentException("Failed to retrieve date of match.");

                if (matchDate.before(systemTime))
                    throw new IllegalArgumentException("Past match date:\t" + ISO8601.fromCalendar(systemTime));

                Prediction oldPrediction
                        = mSimHelper.getPrediction(mDbHelper, prediction.getUserID(), prediction.getMatchNumber());

                // Insert or update prediction
                if (oldPrediction != null) {
                    // Update
                    String predictionID = oldPrediction.getID();

                    values.remove("_" + Prediction.Entry.Cols.ID);

                    int count = mDbHelper.getWritableDatabase().update(Prediction.Entry.TABLE_NAME,
                                          values,
                                          "_" + Prediction.Entry.Cols.ID + " = " + predictionID,
                                          null);

                    if (count <= 0)
                        throw new IllegalArgumentException("Failed to insert prediction into " + uri);

                    return ContentUris.withAppendedId(Prediction.Entry.CONTENT_URI,
                            Long.parseLong(predictionID));
                }
                else {

                    values.remove("_" + Prediction.Entry.Cols.ID);

                    // Insert
                    long rowID = insert(Prediction.Entry.TABLE_NAME, null, values);

                    if (rowID < 0)
                        throw new IllegalArgumentException("Failed to insert prediction into " + uri);

                    return ContentUris.withAppendedId(Prediction.Entry.CONTENT_URI, rowID);
                }
            }
            case Match.Entry.PATH_TOKEN: {

                values.remove("_" + Match.Entry.Cols.ID);

                // Insert
                long rowID = insert(Match.Entry.TABLE_NAME, null, values);

                if (rowID < 0)
                    throw new IllegalArgumentException("Failed to insert match into " + uri);

                return ContentUris.withAppendedId(Match.Entry.CONTENT_URI, rowID);
            }
            case Country.Entry.PATH_TOKEN: {

                values.remove("_" + Country.Entry.Cols.ID);

                // Insert
                long rowID = insert(Country.Entry.TABLE_NAME, null, values);

                if (rowID < 0)
                    throw new IllegalArgumentException("Failed to insert country into " + uri);

                return ContentUris.withAppendedId(Country.Entry.CONTENT_URI, rowID);
            }
            default: {
                throw new IllegalArgumentException(
                        "Unsupported URI, unable to insert into specific row: " +
                                uri);
            }
        }
    }

    synchronized private long insert(@NonNull String table,
                                     @SuppressWarnings("SameParameterValue")
                                     @Nullable String nullColumnHack,
                                     @NonNull ContentValues values) {

        return mDbHelper.getWritableDatabase().insert(table,
                                                      nullColumnHack,
                                                      values);
    }

    /**
     * Implement this to handle requests to delete one or more rows.
     */
    @Override
    synchronized public int delete(@NonNull Uri uri,
                                   @Nullable String whereClause,
                                   @Nullable String[] whereArgs) throws IllegalArgumentException  {

        int numRowsDeleted;

        // switch on mUriMatcher.match
        switch (mUriMatcher.match(uri)) {

            case Match.Entry.PATH_FOR_ID_TOKEN: {

                String idWhere = "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();

                if (whereClause == null)
                    whereClause = idWhere;
                else
                    whereClause += " AND " + idWhere;

                numRowsDeleted = delete(
                        Match.Entry.TABLE_NAME,
                        whereClause,
                        whereArgs);

                break;
            }
            case Country.Entry.PATH_FOR_ID_TOKEN: {

                String idWhere = "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();

                if (whereClause == null)
                    whereClause = idWhere;
                else
                    whereClause += " AND " + idWhere;

                numRowsDeleted = delete(
                        Country.Entry.TABLE_NAME,
                        whereClause,
                        whereArgs);

                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        return numRowsDeleted;
    }

    synchronized private int delete(@NonNull String tableName,
                                    @Nullable String whereClause,
                                    @Nullable String[] whereArgs) {

        return mDbHelper.getWritableDatabase().delete(tableName, whereClause, whereArgs);
    }

    /**
     * Implement this to handle requests to update one or more rows.
     */
    @Override
    synchronized public int update(@NonNull Uri uri,
                                   ContentValues values,
                                   @Nullable String whereClause,
                                   @Nullable String[] whereArgs) throws IllegalArgumentException {

        // WhereClause copy for use in modifying the whereClause
        String modifiedWhereClause = whereClause;

        switch (mUriMatcher.match(uri)) {
            case Country.Entry.PATH_FOR_ID_TOKEN: {
                if (whereClause == null)
                    modifiedWhereClause = "_" + Country.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + "_" + Country.Entry.Cols.ID + " = " + uri.getLastPathSegment();

                values.remove("_" + Country.Entry.Cols.ID);

                return update(Country.Entry.TABLE_NAME,
                                       values,
                                       modifiedWhereClause,
                                       whereArgs);
            }
            case Match.Entry.PATH_FOR_ID_TOKEN: {

                if (whereClause == null)
                    modifiedWhereClause = "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + "_" + Match.Entry.Cols.ID + " = " + uri.getLastPathSegment();

                Match match = cvParser.parseMatch(values);

                values.remove("_" + Match.Entry.Cols.ID);

                int count = update(Match.Entry.TABLE_NAME,
                                            values,
                                            modifiedWhereClause,
                                            whereArgs);

                if (count > 0) {
                    SystemData systemData = mSimHelper.getSystemData(mDbHelper);
                    mSimHelper.updateScoresOfPredictionsOfMatch(mDbHelper, match, systemData);
                }

                return count;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
    }

    /*
     * private update function that updates based on parameters, then notifies
     * change
     */
    private int update(final String tableName,
                       final ContentValues values,
                       final String whereClause,
                       final String[] whereArgs) {
        // call update(...) on the DBAdapter instance variable, and store the count of rows updated.
        return mDbHelper.getWritableDatabase().update(tableName, values, whereClause, whereArgs);
    }
}
