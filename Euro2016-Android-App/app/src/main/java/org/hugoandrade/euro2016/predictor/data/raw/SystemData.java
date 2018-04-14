package org.hugoandrade.euro2016.predictor.data.raw;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class SystemData implements Parcelable {

    private String mID;
    private String mRules;
    private boolean mAppState;
    private Calendar mSystemDate;
    private Calendar mDateOfLastRecordedSystemDate;

    public static class Entry {

        public static final String API_NAME = "SystemData";

        public static class Cols {
            public static final String ID = "id";
            public static final String RULES = "Rules";
            public static final String APP_STATE = "AppState";
            public static final String SYSTEM_DATE = "SystemDate";
        }
    }

    public SystemData(String id, String rules, boolean appState, Calendar systemDate) {
        mID = id;
        mRules = rules;
        mAppState = appState;
        mSystemDate = systemDate;
        mDateOfLastRecordedSystemDate = Calendar.getInstance();
    }

    private SystemData(Parcel in) {
        mID = in.readString();
        mRules = in.readString();
        mAppState = in.readByte() != 0;
        mSystemDate = (Calendar) in.readSerializable();
        mDateOfLastRecordedSystemDate = (Calendar) in.readSerializable();
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
        dest.writeSerializable(mDateOfLastRecordedSystemDate);
    }

    public boolean getAppState() {
        return mAppState;
    }

    public void setSystemDate(Calendar systemDate) {
        mSystemDate = systemDate;
        mDateOfLastRecordedSystemDate = Calendar.getInstance();
    }

    public Calendar getDate() {
        Calendar c = Calendar.getInstance();
        long diff = c.getTimeInMillis() - mDateOfLastRecordedSystemDate.getTimeInMillis();
        c.setTimeInMillis(mSystemDate.getTimeInMillis() + diff);
        return c;
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

    @Override
    public String toString() {
        return "SystemData{" +
                "mID='" + mID + '\'' +
                ", mRules='" + mRules + '\'' +
                ", mAppState=" + mAppState +
                ", mSystemDate=" + mSystemDate +
                ", mDateOfLastRecordedSystemDate=" + mDateOfLastRecordedSystemDate +
                '}';
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
