package org.hugoandrade.euro2016.backend.cloudsim;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

import org.hugoandrade.euro2016.backend.object.Account;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.Prediction;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.utils.ISO8601;

/**
 * Helper class that actually creates and manages
 * the provider's underlying data repository.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Euro2016App";
    private static final int DATABASE_VERSION = 1;


    private static final String CREATE_DB_TABLE_MATCH =
            " CREATE TABLE " + Match.Entry.TABLE_NAME + " (" +
                    " " + Match.Entry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Match.Entry.Cols.MATCH_NO + " INTEGER UNIQUE NOT NULL, " +
                    " " + Match.Entry.Cols.HOME_TEAM + " TEXT NOT NULL, " +
                    " " + Match.Entry.Cols.AWAY_TEAM + " TEXT NOT NULL, " +
                    " " + Match.Entry.Cols.HOME_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Match.Entry.Cols.AWAY_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Match.Entry.Cols.HOME_TEAM_NOTES + " TEXT NULL, " +
                    " " + Match.Entry.Cols.AWAY_TEAM_NOTES + " TEXT NULL, " +
                    " " + Match.Entry.Cols.DATE_AND_TIME + " TEXT NOT NULL, " +
                    " " + Match.Entry.Cols.STADIUM + " TEXT NOT NULL, " +
                    " " + Match.Entry.Cols.GROUP + " TEXT NULL, " +
                    " " + Match.Entry.Cols.STAGE + " TEXT NOT NULL " +
                    " );";

    private static final String CREATE_DB_TABLE_SYSTEM_DATA =
            " CREATE TABLE " + SystemData.Entry.TABLE_NAME + " (" +
                    " " + SystemData.Entry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + SystemData.Entry.Cols.SYSTEM_DATE + " TEXT NOT NULL, " +
                    " " + SystemData.Entry.Cols.DATE_OF_CHANGE + " TEXT NOT NULL, " +
                    " " + SystemData.Entry.Cols.APP_STATE + " BOOLEAN NOT NULL, " +
                    " " + SystemData.Entry.Cols.RULES + " TEXT NOT NULL " +
                    " );";

    private static final String CREATE_DB_TABLE_PREDICTION =
            " CREATE TABLE " + Prediction.Entry.TABLE_NAME + " (" +
                    " " + Prediction.Entry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Prediction.Entry.Cols.USER_ID + " TEXT NOT NULL, " +
                    " " + Prediction.Entry.Cols.MATCH_NO + " INTEGER NOT NULL, " +
                    " " + Prediction.Entry.Cols.HOME_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Prediction.Entry.Cols.AWAY_TEAM_GOALS + " INTEGER NULL, " +
                    " " + Prediction.Entry.Cols.SCORE + " INTEGER NULL" +
                    " );";

    private static final String CREATE_DB_TABLE_COUNTRY =
            " CREATE TABLE " + Country.Entry.TABLE_NAME + " (" +
                    " " + Country.Entry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Country.Entry.Cols.NAME + " TEXT UNIQUE NOT NULL, " +
                    " " + Country.Entry.Cols.MATCHES_PLAYED + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.GOALS_FOR + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.GOALS_AGAINST + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.GOALS_DIFFERENCE + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.VICTORIES + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.DRAWS + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.DEFEATS + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.GROUP + " TEXT NOT NULL, " +
                    " " + Country.Entry.Cols.POSITION + " INTEGER NOT NULL, " +
                    " " + Country.Entry.Cols.POINTS + " INTEGER NOT NULL" +
                    " );";

    private static final String CREATE_DB_TABLE_ACCOUNT =
            " CREATE TABLE " + Account.Entry.TABLE_NAME + " (" +
                    " " + Account.Entry.Cols._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " " + Account.Entry.Cols.USERNAME + " TEXT UNIQUE NOT NULL, " +
                    " " + Account.Entry.Cols.PASSWORD + " TEXT NOT NULL, " +
                    " " + Account.Entry.Cols.SCORE + " INTEGER NOT NULL" +
                    " );";

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
        db.execSQL("DROP TABLE IF EXISTS " +  Match.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  Prediction.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  Country.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  Account.Entry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " +  SystemData.Entry.TABLE_NAME);
        onCreate(db);
    }

    private void populateTable(SQLiteDatabase db) {
        for (int i = 0 ; i < TOTAL_MATCHES ; i++) {
            ContentValues values = getMatchContentValues(i);
            db.insert(Match.Entry.TABLE_NAME, null, values);
        }
        // populate PredictionTable
        db.insert(Prediction.Entry.TABLE_NAME, null, getPredictionContentValue(1, 4, 2, 0));
        db.insert(Prediction.Entry.TABLE_NAME, null, getPredictionContentValue(2, 4, 1, 1));
        db.insert(Prediction.Entry.TABLE_NAME, null, getPredictionContentValue(3, 4, 1, 0));
        db.insert(Prediction.Entry.TABLE_NAME, null, getPredictionContentValue(4, 4, 0, 1));
        db.insert(Prediction.Entry.TABLE_NAME, null, getPredictionContentValue(1, 5, 0, 1));
        db.insert(Prediction.Entry.TABLE_NAME, null, getPredictionContentValue(1, 6, 0, 1));

        // populate AccountTable
        db.insert(Account.Entry.TABLE_NAME, null, getAccountContentValue(1, "USER_1", "PASSWORD", 0));
        db.insert(Account.Entry.TABLE_NAME, null, getAccountContentValue(2, "USER_2", "PASSWORD", 0));
        db.insert(Account.Entry.TABLE_NAME, null, getAccountContentValue(3, "USER_3", "PASSWORD", 0));
        db.insert(Account.Entry.TABLE_NAME, null, getAccountContentValue(4, "USER_4", "PASSWORD", 0));

        // populate CountryTable
        for (int i = 0 ; i < TOTAL_COUNTRIES ; i++) {
            ContentValues values = getCountryContentValues(i);
            db.insert(Country.Entry.TABLE_NAME, null, values);
        }

        // populate SystemData
        db.insert(SystemData.Entry.TABLE_NAME, null, getSystemDataContentValue(Calendar.getInstance(), "0,1,2,4", true));
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
        values.put(Account.Entry.Cols._ID,  id);
        values.put(Account.Entry.Cols.USERNAME, username);
        values.put(Account.Entry.Cols.PASSWORD, password);
        values.put(Account.Entry.Cols.SCORE, score);
        return values;
    }

    private ContentValues getSystemDataContentValue(Calendar date, String rules, boolean appState) {
        ContentValues values = new ContentValues();
        values.put(SystemData.Entry.Cols.SYSTEM_DATE, ISO8601.fromCalendar(date));
        values.put(SystemData.Entry.Cols.DATE_OF_CHANGE, ISO8601.fromCalendar(date));
        values.put(SystemData.Entry.Cols.APP_STATE, appState);
        values.put(SystemData.Entry.Cols.RULES, rules);
        return values;
    }

    private ContentValues getCountryContentValue(String name, String group) {

        ContentValues values = new ContentValues();
        values.put(Country.Entry.Cols.NAME,  name);
        values.put(Country.Entry.Cols.MATCHES_PLAYED, 0);
        values.put(Country.Entry.Cols.VICTORIES, 0);
        values.put(Country.Entry.Cols.DRAWS, 0);
        values.put(Country.Entry.Cols.DEFEATS, 0);
        values.put(Country.Entry.Cols.GOALS_FOR, 0);
        values.put(Country.Entry.Cols.GOALS_AGAINST, 0);
        values.put(Country.Entry.Cols.GOALS_DIFFERENCE, 0);
        values.put(Country.Entry.Cols.GROUP, group);
        values.put(Country.Entry.Cols.POSITION, 0);
        values.put(Country.Entry.Cols.POINTS, 0);
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
        values.put(Match.Entry.Cols.MATCH_NO, matchNo);
        values.put(Match.Entry.Cols.HOME_TEAM,  homeTeam);
        values.put(Match.Entry.Cols.AWAY_TEAM, awayTeam);
        values.put(Match.Entry.Cols.HOME_TEAM_GOALS, (String) null);
        values.put(Match.Entry.Cols.AWAY_TEAM_GOALS, (String) null);
        values.put(Match.Entry.Cols.DATE_AND_TIME, date);
        values.put(Match.Entry.Cols.STADIUM, stadium);
        values.put(Match.Entry.Cols.GROUP, group);
        values.put(Match.Entry.Cols.STAGE, stage);
        return values;
    }

    private ContentValues getAsContentValues(int matchNo, String homeTeam, String awayTeam,
                                             int homeTeamGoalsScored, int awayTeamGoalsScored,
                                             String date, String stadium, String group,
                                             String stage) {
        ContentValues values = new ContentValues();
        values.put(Match.Entry.Cols.MATCH_NO, matchNo);
        values.put(Match.Entry.Cols.HOME_TEAM,  homeTeam);
        values.put(Match.Entry.Cols.AWAY_TEAM, awayTeam);
        values.put(Match.Entry.Cols.HOME_TEAM_GOALS, homeTeamGoalsScored);
        values.put(Match.Entry.Cols.AWAY_TEAM_GOALS, awayTeamGoalsScored);
        values.put(Match.Entry.Cols.DATE_AND_TIME, date);
        values.put(Match.Entry.Cols.STADIUM, stadium);
        values.put(Match.Entry.Cols.GROUP, group);
        values.put(Match.Entry.Cols.STAGE, stage);
        return values;
    }
    private ContentValues getPredictionContentValue(int userID, int matchNo, int homeGoals, int awayGoals) {
        ContentValues values = new ContentValues();
        values.put(Prediction.Entry.Cols.USER_ID,  userID);
        values.put(Prediction.Entry.Cols.MATCH_NO, matchNo);
        values.put(Prediction.Entry.Cols.HOME_TEAM_GOALS, homeGoals);
        values.put(Prediction.Entry.Cols.AWAY_TEAM_GOALS, awayGoals);
        return values;
    }
}
