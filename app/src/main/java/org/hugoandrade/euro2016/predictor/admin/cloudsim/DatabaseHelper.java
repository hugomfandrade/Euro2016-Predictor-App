package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesParser;
import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.Prediction;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.User;
import org.hugoandrade.euro2016.predictor.admin.utils.InitConfigUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that actually creates and manages
 * the provider's underlying data repository.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    @SuppressWarnings("unused")
    private final static String TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "Euro2016Predictor";
    private static final int DATABASE_VERSION = 6;

    private static final String CREATE_DB_TABLE_MATCH =
            " CREATE TABLE " + Match.Entry.TABLE_NAME +
                    " (" + "_" + Match.Entry.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                    ", " + Match.Entry.Cols.MATCH_NUMBER + " INTEGER UNIQUE NOT NULL " +
                    ", " + Match.Entry.Cols.HOME_TEAM_ID + " TEXT NOT NULL " +
                    ", " + Match.Entry.Cols.AWAY_TEAM_ID + " TEXT NOT NULL " +
                    ", " + Match.Entry.Cols.HOME_TEAM_GOALS + " INTEGER NULL " +
                    ", " + Match.Entry.Cols.AWAY_TEAM_GOALS + " INTEGER NULL " +
                    ", " + Match.Entry.Cols.HOME_TEAM_NOTES + " TEXT NULL " +
                    ", " + Match.Entry.Cols.AWAY_TEAM_NOTES + " TEXT NULL " +
                    ", " + Match.Entry.Cols.GROUP + " TEXT NULL " +
                    ", " + Match.Entry.Cols.STAGE + " TEXT NOT NULL " +
                    ", " + Match.Entry.Cols.STADIUM + " TEXT NOT NULL " +
                    ", " + Match.Entry.Cols.DATE_AND_TIME + " TEXT NOT NULL " +
                    " );";

    private static final String CREATE_DB_TABLE_SYSTEM_DATA =
            " CREATE TABLE " + SystemData.Entry.TABLE_NAME +
                    " (" + "_" + SystemData.Entry.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                    ", " + SystemData.Entry.Cols.SYSTEM_DATE + " TEXT NOT NULL " +
                    ", " + SystemData.Entry.Cols.DATE_OF_CHANGE + " TEXT NOT NULL " +
                    ", " + SystemData.Entry.Cols.APP_STATE + " BOOLEAN NOT NULL " +
                    ", " + SystemData.Entry.Cols.RULES + " TEXT NOT NULL " +
                    " );";

    private static final String CREATE_DB_TABLE_PREDICTION =
            " CREATE TABLE " + Prediction.Entry.TABLE_NAME +
                    " (" + "_" + Prediction.Entry.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                    ", " + Prediction.Entry.Cols.USER_ID + " TEXT NOT NULL " +
                    ", " + Prediction.Entry.Cols.MATCH_NO + " INTEGER NOT NULL " +
                    ", " + Prediction.Entry.Cols.HOME_TEAM_GOALS + " INTEGER NULL " +
                    ", " + Prediction.Entry.Cols.AWAY_TEAM_GOALS + " INTEGER NULL " +
                    ", " + Prediction.Entry.Cols.SCORE + " INTEGER NULL" +
                    " );";

    private static final String CREATE_DB_TABLE_COUNTRY =
            " CREATE TABLE " + Country.Entry.TABLE_NAME +
                    " (" + "_" + Country.Entry.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                    ", " + Country.Entry.Cols.NAME + " TEXT UNIQUE NOT NULL " +
                    ", " + Country.Entry.Cols.MATCHES_PLAYED + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.GOALS_FOR + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.GOALS_AGAINST + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.GOALS_DIFFERENCE + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.VICTORIES + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.DRAWS + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.DEFEATS + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.GROUP + " TEXT NOT NULL " +
                    ", " + Country.Entry.Cols.POSITION + " INTEGER NOT NULL " +
                    ", " + Country.Entry.Cols.POINTS + " INTEGER NOT NULL" +
                    ", " + Country.Entry.Cols.COEFFICIENT + " REAL NOT NULL" +
                    ", " + Country.Entry.Cols.FAIR_PLAY_POINTS + " INTEGER NOT NULL" +
                    " );";

    private static final String CREATE_DB_TABLE_ACCOUNT =
            " CREATE TABLE " + User.Entry.TABLE_NAME +
                    " (" + "_" + User.Entry.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT " +
                    ", " + User.Entry.Cols.EMAIL + " TEXT UNIQUE NOT NULL " +
                    ", " + User.Entry.Cols.PASSWORD + " TEXT NOT NULL " +
                    ", " + User.Entry.Cols.SCORE + " INTEGER NOT NULL" +
                    " );";

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
        db.execSQL("DROP TABLE IF EXISTS " +  Match.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  Prediction.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  Country.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  User.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  SystemData.Entry.TABLE_NAME);
        onCreate(db);
    }

    private CloudContentValuesFormatter formatter = new CloudContentValuesFormatter();

    private void populateTable(SQLiteDatabase db) {
        // populate SystemData
        db.insert(SystemData.Entry.TABLE_NAME, null, formatter.getAsContentValues(InitConfigUtils.buildInitSystemData()));

        for (Country country : InitConfigUtils.buildInitCountryList()) {
            db.insert(Country.Entry.TABLE_NAME, null, formatter.getAsContentValues(country));
        }

        for (Match match : InitConfigUtils.buildInitMatchList(getCountryList(db))) {
            db.insert(Match.Entry.TABLE_NAME, null, formatter.getAsContentValues(match));
        }

        db.insert(User.Entry.TABLE_NAME, null, formatter.getAsContentValues(new User("admin", "password")));
    }

    private List<Country> getCountryList(SQLiteDatabase db) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(Country.Entry.TABLE_NAME);
        Cursor c = qb.query(db, null, null, null, null, null, null);

        CloudContentValuesParser parser = new CloudContentValuesParser();

        List<Country> countryList = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                countryList.add(parser.parseCountry(c));
            } while (c.moveToNext());
        }
        c.close();

        return countryList;
    }
}
