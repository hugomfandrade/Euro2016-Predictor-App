package org.hugoandrade.euro2016.predictor.admin.object;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

public class SystemData implements Parcelable {

    @SuppressWarnings("unused") private static final String TAG = SystemData.class.getSimpleName();

    private String mID;
    private String mRules;
    private boolean mAppState;
    private Calendar mSystemDate;
    private Calendar mDateOfChange;

    public static class Entry {

        public static final String TABLE_NAME = "SystemData";
        public static final String API_NAME = "SystemData";

        public static class Cols {
            public static final String ID = "id";
            public static final String RULES = "Rules";
            public static final String SYSTEM_DATE = "SystemDate";
            public static final String DATE_OF_CHANGE = "DateOfChange";
            public static final String APP_STATE = "AppState";
        }

        // SQLite table mName
        // PATH & TOKEN for entire table
        public static final String PATH = API_NAME;
        public static final int PATH_TOKEN = 190;

        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 200;

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

    public SystemData(String id, String rules, boolean appState, Calendar systemDate, Calendar dateOfChange) {
        mID = id;
        mRules = rules;
        mAppState = appState;
        mSystemDate = systemDate;
        mDateOfChange = dateOfChange;
    }

    public String getID() {
        return mID;
    }

    public String getRawRules() {
        return mRules;
    }

    public boolean getAppState() {
        return mAppState;
    }

    public Calendar getSystemDate() {
        return mSystemDate;
    }

    public Calendar getDateOfChange() {
        return mDateOfChange;
    }

    public Rules getRules() {
        try {
            String[] s = mRules.split(",");
            return new Rules(Integer.parseInt(s[0]),
                    Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]),
                    Integer.parseInt(s[3]));
        } catch (Exception e) {
            return new Rules(-1, -1, -1, -1);
        }
    }

    public void setDateOfChange(Calendar dateOfChange) {
        mDateOfChange = dateOfChange;
    }

    public void setAppState(boolean state) {
        mAppState = state;
    }

    public void setRules(String rules) {
        mRules = rules;
    }

    public void setSystemDate(int year, int month, int day, int hour, int minute) {
        mSystemDate.set(year, month, day, hour, minute);
    }

    public void setSystemDate(int year, int month, int day) {
        mSystemDate.set(year, month, day);
    }

    public void setSystemDate(int field, int val) {
        mSystemDate.set(field, val);
    }

    private SystemData(Parcel in) {
        mID = in.readString();
        mRules = in.readString();
        mAppState = in.readByte() != 0;
        mSystemDate = (Calendar) in.readSerializable();
        mDateOfChange = (Calendar) in.readSerializable();
    }

    @Override
    public String toString() {
        return "SystemData{" +
                "mID='" + mID + '\'' +
                ", mRules='" + mRules + '\'' +
                ", mAppState=" + mAppState +
                ", mSystemDate=" + mSystemDate +
                ", mDateOfChange=" + mDateOfChange +
                '}';
    }

    public static final Creator<SystemData> CREATOR = new Creator<SystemData>() {
        @Override
        public SystemData createFromParcel(Parcel in) {
            return new SystemData(in);
        }

        @Override
        public SystemData[] newArray(int size) {
            return new SystemData[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mRules);
        dest.writeByte((byte) (mAppState ? 1 : 0));
        dest.writeSerializable(mSystemDate);
        dest.writeSerializable(mDateOfChange);
    }

    public Calendar getDate() {
        Calendar c = Calendar.getInstance();
        long diff = c.getTimeInMillis() - mDateOfChange.getTimeInMillis();
        c.setTimeInMillis(mDateOfChange.getTimeInMillis() + diff);
        return c;
    }

    public class Rules {

        private final int mRuleCorrectPrediction;
        private final int mRuleCorrectOutcome;
        private final int mRuleCorrectOutcomeViaPenalties;
        private final int mRuleIncorrectPrediction;

        public Rules(int incorrectPrediction,
                     int correctOutcomeViaPenalties,
                     int correctOutcome,
                     int correctPrediction) {
            mRuleIncorrectPrediction = incorrectPrediction;
            mRuleCorrectOutcomeViaPenalties = correctOutcomeViaPenalties;
            mRuleCorrectOutcome = correctOutcome;
            mRuleCorrectPrediction = correctPrediction;
        }

        public int getRuleCorrectPrediction() {
            return mRuleCorrectPrediction;
        }

        public int getRuleCorrectOutcome() {
            return mRuleCorrectOutcome;
        }

        public int getRuleCorrectOutcomeViaPenalties() {
            return mRuleCorrectOutcomeViaPenalties;
        }

        public int getRuleIncorrectPrediction() {
            return mRuleIncorrectPrediction;
        }
    }
}
