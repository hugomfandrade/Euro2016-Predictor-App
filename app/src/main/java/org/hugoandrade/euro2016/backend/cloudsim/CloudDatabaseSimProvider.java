package org.hugoandrade.euro2016.backend.cloudsim;

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

import java.util.Calendar;

import org.hugoandrade.euro2016.backend.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.backend.cloudsim.parser.CloudContentValuesParser;
import org.hugoandrade.euro2016.backend.object.Account;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.Prediction;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.utils.ISO8601;

public class CloudDatabaseSimProvider extends ContentProvider {

    @SuppressWarnings("unused")
    private final static String TAG = CloudDatabaseSimProvider.class.getSimpleName();

    // org mName in java package format
    public static final String ORGANIZATIONAL_NAME = "org.hugoandrade";
    // mName of this provider's project
    private static final String PROJECT_NAME = "euro_2016";

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
        matcher.addURI(AUTHORITY, Account.Entry.PATH, Account.Entry.PATH_TOKEN);
        matcher.addURI(AUTHORITY, Account.Entry.PATH_FOR_ID, Account.Entry.PATH_FOR_ID_TOKEN);
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
            case Account.Entry.PATH_TOKEN:
                return Account.Entry.CONTENT_TYPE_DIR;
            case Account.Entry.PATH_FOR_ID_TOKEN:
                return Account.Entry.CONTENT_ITEM_TYPE;
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
            case Match.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = Match.Entry.COLUMN__ID + " ASC";

                tableName = Match.Entry.TABLE_NAME;

                break;
            }

            case Match.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = Match.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Match.Entry.TABLE_NAME;

                break;
            }

            case Country.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = Country.Entry.COLUMN__ID + " ASC";

                tableName = Country.Entry.TABLE_NAME;

                break;
            }

            case Country.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = Country.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Country.Entry.TABLE_NAME;

                break;
            }

            case Account.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = Account.Entry.COLUMN__ID + " ASC";

                tableName = Account.Entry.TABLE_NAME;

                break;
            }

            case Account.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = Account.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Account.Entry.TABLE_NAME;

                break;
            }

            case SystemData.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = SystemData.Entry.COLUMN__ID + " ASC";

                tableName = SystemData.Entry.TABLE_NAME;

                break;
            }

            case SystemData.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = SystemData.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = SystemData.Entry.TABLE_NAME;

                break;
            }

            case Prediction.Entry.PATH_TOKEN: {
                if (sortOrder == null || sortOrder.isEmpty())
                    updatedSortOrder = Prediction.Entry.COLUMN__ID + " ASC";

                tableName = Prediction.Entry.TABLE_NAME;

                break;
            }

            case Prediction.Entry.PATH_FOR_ID_TOKEN: {
                String idSelection = Prediction.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                if (selection == null)
                    modifiedSelection = idSelection;
                else
                    modifiedSelection += " AND " + idSelection;

                tableName = Prediction.Entry.TABLE_NAME;

                break;
            }

            case UriMatcher.NO_MATCH:
            default:
                throw new IllegalArgumentException("Invalid URI");

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

        values.remove(Prediction.Entry.COLUMN__ID);

        // switch on the results of 'mUriMatcher' matching the Uri passed in,
        switch (mUriMatcher.match(uri)) {

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
                                          Prediction.Entry.COLUMN__ID + " = " + predictionID,
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

            case Account.Entry.PATH_TOKEN:
                String requestType = getRequestType(values);

                if (requestType == null)
                    return Uri.parse("Request type not provided.");

                values.remove(Account.Entry.REQUEST_TYPE);

                Account account = cvParser.parseAccount(values);

                switch (requestType) {
                    case Account.Entry.REQUEST_TYPE_LOG_IN: {

                        Account dbAccount = getAccount(account.getUsername());

                        if (dbAccount == null)
                            return Uri.parse("Username \'" + account.getUsername() + "\' does not exist.");

                        if (!dbAccount.getPassword().equals(account.getPassword()))
                            return Uri.parse("Password incorrect.");

                        // TODO Optional: hashing

                        Uri url = ContentUris.withAppendedId(Account.Entry.CONTENT_URI,
                                Long.parseLong(dbAccount.getID()));

                        notifyChanges(url, null);

                        return url;
                    }

                    case Account.Entry.REQUEST_TYPE_SIGN_UP: {

                        account.setScore(0);

                        // Insert
                        try {
                            long rowID = db.insert(Account.Entry.TABLE_NAME, null, values);

                            Uri url = ContentUris.withAppendedId(Account.Entry.CONTENT_URI, rowID);

                            notifyChanges(url, null);

                        } catch (android.database.sqlite.SQLiteConstraintException e) {
                            return Uri.parse("Username \'" + account.getUsername() + "\' already exists.");
                        }

                        return Uri.parse("Failed to sign up into " + uri);
                    }

                    default:
                        return Uri.parse("Request type not recognized.");
                }

            case Prediction.Entry.PATH_FOR_ID_TOKEN:
            case Account.Entry.PATH_FOR_ID_TOKEN:
            case Match.Entry.PATH_TOKEN:
            case Match.Entry.PATH_FOR_ID_TOKEN:
            case SystemData.Entry.PATH_TOKEN:
            case SystemData.Entry.PATH_FOR_ID_TOKEN:
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
            case Account.Entry.PATH_TOKEN:
            case Account.Entry.PATH_FOR_ID_TOKEN:
            case Match.Entry.PATH_TOKEN:
            case Match.Entry.PATH_FOR_ID_TOKEN:
            case SystemData.Entry.PATH_TOKEN:
            case SystemData.Entry.PATH_FOR_ID_TOKEN:
            case Country.Entry.PATH_TOKEN:
            case Country.Entry.PATH_FOR_ID_TOKEN:
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * Implement this to handle requests to update one or more rows.
     */
    @Override
    synchronized public int update(@NonNull Uri uri,
                                   @NonNull ContentValues values,
                                   @Nullable String whereClause,
                                   @Nullable String[] whereArgs
    ) throws IllegalArgumentException {

        // WhereClause copy for use in modifying the whereClause
        String modifiedWhereClause = whereClause;

        switch (mUriMatcher.match(uri)) {
            case Country.Entry.PATH_FOR_ID_TOKEN: {
                if (whereClause == null)
                    modifiedWhereClause = Country.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + Country.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();

                return updateAndNotify(uri,
                        Country.Entry.TABLE_NAME,
                        values,
                        modifiedWhereClause,
                        whereArgs);
            }
            case Match.Entry.PATH_FOR_ID_TOKEN: {
                if (whereClause == null)
                    modifiedWhereClause = Match.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + Match.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();

                SystemData systemData = getSystemData();

                Match match = cvParser.parseMatch(values);


                int count = updateAndNotify(uri,
                                            Match.Entry.TABLE_NAME,
                                            values,
                                            modifiedWhereClause,
                                            whereArgs);

                if (count > 0)
                    updatePredictionScoresOfMatch(match, systemData);

                return count;
            }
            case SystemData.Entry.PATH_FOR_ID_TOKEN: {

                if (whereClause == null)
                    modifiedWhereClause = SystemData.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();
                else
                    modifiedWhereClause += " AND " + SystemData.Entry.COLUMN__ID + " = " + uri.getLastPathSegment();

                SystemData preSystemData = getSystemData();
                SystemData newSystemData = cvParser.parseSystemData(values);


                boolean haveRulesChanged =
                        !preSystemData.getRules().equals(newSystemData.getRules());

                int count = updateAndNotify(uri,
                                            SystemData.Entry.TABLE_NAME,
                                            values,
                                            modifiedWhereClause,
                                            whereArgs);

                if (count > 0 && haveRulesChanged)
                    updateAllPredictionScores();
            }
            case Prediction.Entry.PATH_FOR_ID_TOKEN:
            case Prediction.Entry.PATH_TOKEN:
            case Account.Entry.PATH_FOR_ID_TOKEN:
            case Account.Entry.PATH_TOKEN:
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
        Cursor c = qb.query(db,	null, null, null, null, null, SystemData.Entry.COLUMN__ID);

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
                Match.Entry.COLUMN_MATCH_NO +  " = \"" + matchNo + "\"",
                null, null, null,
                Match.Entry.COLUMN__ID);

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
                Prediction.Entry.COLUMN_USER_ID + " = \"" + userID + "\" AND " +
                        Prediction.Entry.COLUMN_MATCH_NO + " = \"" + matchNo + "\"",
                null, null, null,
                Prediction.Entry.COLUMN__ID);


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
        Cursor c = qb.query(db,	null, null, null, null, null, SystemData.Entry.COLUMN__ID);

        if (c != null && c.getCount() > 0) {
            if (c.moveToFirst()) {
                SystemData systemData = cvParser.parseSystemData(c);
                c.close();
                return systemData.getDate();
                /*Calendar savedSystemDate = systemData.getSystemDate();
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

    private Account getAccount(String username) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Account.Entry.TABLE_NAME);

        Cursor c = qb.query(db,
                null,
                Account.Entry.COLUMN_USERNAME + " = \"" + username + "\"",
                null, null, null,
                Account.Entry.COLUMN__ID);


        if (c.getCount() > 0 && c.moveToFirst()) {
            Account account = cvParser.parseAccount(c);
            c.close();
            return account;
        }

        c.close();
        return null;
    }

    private String getRequestType(ContentValues values) {
        return values.getAsString(Account.Entry.REQUEST_TYPE);
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
                Prediction.Entry.COLUMN_USER_ID + " = \"" + prediction.getUserID() + "\" AND " +
                        Prediction.Entry.COLUMN_MATCH_NO + " = \"" + prediction.getMatchNumber() + "\"",
                null, null, null,
                Prediction.Entry.COLUMN__ID);

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
        Cursor c = qb.query(db,	null, null, null, null, null, Match.Entry.COLUMN__ID);

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
                Prediction.Entry.COLUMN_MATCH_NO + " = " + Integer.toString(match.getMatchNumber()),
                null, null, null,
                Prediction.Entry.COLUMN_MATCH_NO);

        if (c.moveToFirst()) {
            do{
                // Get prediction
                Prediction prediction = cvParser.parsePrediction(c);

                // Store previous score
                int preScore = prediction.getScore() == -1? 0 : prediction.getScore();

                // Get new prediction score
                prediction =
                        computePredictionScore(match, prediction, systemData);

                // Update entry
                ContentValues predictionValues = cvFormatter.getAsContentValues(prediction);
                predictionValues.remove(Prediction.Entry.COLUMN__ID);
                int count = db.update(
                        Prediction.Entry.TABLE_NAME,
                        predictionValues,
                        Prediction.Entry.COLUMN__ID + " = " + Integer.toString(prediction.getID()),
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
        // Query account table
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Account.Entry.TABLE_NAME);
        Cursor c = qb.query(db,
                null,
                Account.Entry.COLUMN__ID + " = " + userID,
                null, null, null,
                Account.Entry.COLUMN__ID);

        if (c.moveToFirst()) {
            do {
                // Update Account score
                Account account = cvParser.parseAccount(c);
                account.setScore(account.getScore() + diffScore);

                db.update(Account.Entry.TABLE_NAME,
                          cvFormatter.getAsContentValues(account),
                          Account.Entry.COLUMN__ID + " = " + account.getID(),
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
        resolver.notifyChange(uri, null);
    }

    private Prediction computePredictionScore(Match match, Prediction prediction, SystemData systemData) {

        int incorrectPrediction = Integer.parseInt(systemData.getRules().split(",")[0]);
        int correctOutcomeViaPenalties = Integer.parseInt(systemData.getRules().split(",")[1]);
        int correctOutcome = Integer.parseInt(systemData.getRules().split(",")[2]);
        int correctPrediction = Integer.parseInt(systemData.getRules().split(",")[3]);

        if (!isMatchPlayed(match)) {
            prediction.setScore(-1);
            return prediction;
        }
        if (!isPredictionSet(prediction)) {
            prediction.setScore(incorrectPrediction);
            return prediction;
        }

        // Both (match and prediction) home teams win
        if (didHomeTeamWin(match) && didPredictHomeTeamWin(prediction)) {
            if (isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (didAwayTeamWin(match) && didPredictAwayTeamWin(prediction)) {
            if (isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (didTeamsTied(match) && didPredictTie(prediction) && !wasThereAPenaltyShootout(match)) {
            if (isPredictionCorrect(match, prediction))
                prediction.setScore(correctPrediction);
            else
                prediction.setScore(correctOutcome);
            return prediction;
        }
        else if (didTeamsTied(match) && wasThereAPenaltyShootout(match)) {
            if (didHomeTeamWinByPenaltyShootout(match) && didPredictHomeTeamWin(prediction)) {
                prediction.setScore(correctOutcomeViaPenalties);
                return prediction;
            }
            if (didAwayTeamWinByPenaltyShootout(match) && didPredictAwayTeamWin(prediction)) {
                prediction.setScore(correctOutcomeViaPenalties);
                return prediction;
            }
        }
        prediction.setScore(incorrectPrediction);
        return prediction;
    }

    private boolean isPredictionSet(Prediction prediction) {
        return prediction.getHomeTeamGoals() != -1 && prediction.getAwayTeamGoals() != -1;
    }

    private boolean isMatchPlayed(Match match) {
        return match.getHomeTeamGoals() != -1 && match.getAwayTeamGoals() != -1;
    }

    private boolean didHomeTeamWin(Match match) {
        return match.getHomeTeamGoals() > match.getAwayTeamGoals();
    }

    private boolean didPredictHomeTeamWin(Prediction prediction) {
        return prediction.getHomeTeamGoals() > prediction.getAwayTeamGoals();
    }

    private boolean didAwayTeamWin(Match match) {
        return match.getAwayTeamGoals() > match.getHomeTeamGoals();
    }

    private boolean didPredictAwayTeamWin(Prediction prediction) {
        return prediction.getAwayTeamGoals() > prediction.getHomeTeamGoals();
    }

    private boolean isPredictionCorrect(Match match, Prediction prediction) {
        return prediction.getHomeTeamGoals() == match.getHomeTeamGoals()
                && prediction.getAwayTeamGoals() == match.getAwayTeamGoals();
    }

    private boolean didTeamsTied(Match match) {
        return match.getHomeTeamGoals() == match.getAwayTeamGoals();
    }

    private boolean didPredictTie(Prediction prediction) {
        return prediction.getHomeTeamGoals() == prediction.getAwayTeamGoals();
    }

    private boolean wasThereAPenaltyShootout(Match match) {
        return match.getHomeTeamNotes().equals("p") || match.getAwayTeamNotes().equals("p");
    }

    private boolean didHomeTeamWinByPenaltyShootout(Match match) {
        return match.getHomeTeamNotes() != null && match.getHomeTeamNotes().equals("p");
    }

    private boolean didAwayTeamWinByPenaltyShootout(Match match) {
        return match.getAwayTeamNotes() != null && match.getAwayTeamNotes().equals("p");
    }
}
