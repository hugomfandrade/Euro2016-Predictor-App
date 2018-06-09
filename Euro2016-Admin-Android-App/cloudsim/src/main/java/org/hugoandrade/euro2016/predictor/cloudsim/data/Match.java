package org.hugoandrade.euro2016.predictor.cloudsim.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.hugoandrade.euro2016.predictor.cloudsim.CloudDatabaseSimProvider;
import org.hugoandrade.euro2016.predictor.cloudsim.utils.ISO8601;

import java.util.Date;

public class Match implements Comparable<Match>, Parcelable {

    private String mID;
    private int mMatchNo;
    private String mHomeTeamID;
    private String mAwayTeamID;
    private Country mHomeTeam;
    private Country mAwayTeam;
    private int mHomeTeamGoals;
    private int mAwayTeamGoals;
    private String mHomeTeamNotes;
    private String mAwayTeamNotes;
    private String mStage;
    private String mGroup;
    private String mStadium;
    private Date mDateAndTime;

    public static class Entry {

        public static final String TABLE_NAME = "Match";

        public static class Cols {
            public static final String ID = "id";
            public static final String MATCH_NUMBER = "MatchNumber";
            public static final String HOME_TEAM_ID = "HomeTeamID";
            public static final String AWAY_TEAM_ID = "AwayTeamID";
            public static final String HOME_TEAM_GOALS = "HomeTeamGoals";
            public static final String AWAY_TEAM_GOALS = "AwayTeamGoals";
            public static final String HOME_TEAM_NOTES = "HomeTeamNotes";
            public static final String AWAY_TEAM_NOTES = "AwayTeamNotes";
            public static final String GROUP = "GroupLetter";
            public static final String STAGE = "Stage";
            public static final String STADIUM = "Stadium";
            public static final String DATE_AND_TIME = "DateAndTime";
        }

        // SQLite table mName
        // PATH_LOGIN & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 110;

        // PATH_LOGIN & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 120;

        public static final Uri CONTENT_URI = CloudDatabaseSimProvider.BASE_URI.buildUpon()
                .appendPath(PATH).build();
    }

    public Match(String id,
                 int matchNo,
                 String homeTeamID,
                 String awayTeamID,
                 int homeGoals,
                 int awayGoals,
                 String homeTeamNotes,
                 String awayTeamNotes,
                 String group,
                 String stage,
                 String stadium,
                 Date dateAndTime) {

        mID = id;
        mMatchNo = matchNo;
        mHomeTeamID = homeTeamID;
        mAwayTeamID = awayTeamID;
        mHomeTeamGoals = homeGoals;
        mAwayTeamGoals = awayGoals;
        mHomeTeamNotes = homeTeamNotes;
        mAwayTeamNotes = awayTeamNotes;
        mGroup = group;
        mStadium = stadium;
        mStage = stage;
        mDateAndTime = dateAndTime;
    }

    public String getID() {
        return mID;
    }

    public int getMatchNumber() {
        return mMatchNo;
    }

    public String getHomeTeamID() {
        return mHomeTeamID;
    }

    public void setHomeTeamID(String homeTeamID) {
        mHomeTeamID = homeTeamID;
    }

    public String getAwayTeamID() {
        return mAwayTeamID;
    }

    public void setAwayTeamID(String awayTeamID) {
        mAwayTeamID = awayTeamID;
    }

    public int getHomeTeamGoals() {
        return mHomeTeamGoals;
    }

    public void setHomeTeamGoals(int homeTeamGoals) {
        mHomeTeamGoals = homeTeamGoals;
    }

    public int getAwayTeamGoals() {
        return mAwayTeamGoals;
    }

    public void setAwayTeamGoals(int awayTeamGoals) {
        mAwayTeamGoals = awayTeamGoals;
    }

    public String getHomeTeamNotes() {
        return mHomeTeamNotes;
    }

    public void setHomeTeamNotes(String homeTeamNotes) {
        mHomeTeamNotes = homeTeamNotes;
    }

    public String getAwayTeamNotes() {
        return mAwayTeamNotes;
    }

    public void setAwayTeamNotes(String awayTeamNotes) {
        mAwayTeamNotes = awayTeamNotes;
    }

    public Country getHomeTeam() {
        return mHomeTeam;
    }

    public void setHomeTeam(Country homeTeam) {
        mHomeTeam = homeTeam;
    }

    public Country getAwayTeam() {
        return mAwayTeam;
    }

    public void setAwayTeam(Country awayTeam) {
        mAwayTeam= awayTeam;
    }

    public String getHomeTeamName() {
        return mHomeTeam == null ? getHomeTeamID() : mHomeTeam.getName();
    }

    public String getAwayTeamName() {
        return mAwayTeam == null ? getAwayTeamID() : mAwayTeam.getName();
    }

    public String getStage() {
        return mStage;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getStadium() {
        return mStadium;
    }

    public Date getDateAndTime() {
        return mDateAndTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeInt(mMatchNo);
        dest.writeString(mHomeTeamID);
        dest.writeString(mAwayTeamID);
        dest.writeInt(mHomeTeamGoals);
        dest.writeInt(mAwayTeamGoals);
        dest.writeString(mHomeTeamNotes);
        dest.writeString(mAwayTeamNotes);
        dest.writeString(mGroup);
        dest.writeString(mStadium);
        dest.writeString(mStage);
        dest.writeSerializable(mDateAndTime);
        dest.writeParcelable(mHomeTeam, flags);
        dest.writeParcelable(mAwayTeam, flags);
    }

    public Match(Parcel in) {
        mID = in.readString();
        mMatchNo = in.readInt();
        mHomeTeamID = in.readString();
        mAwayTeamID = in.readString();
        mHomeTeamGoals = in.readInt();
        mAwayTeamGoals = in.readInt();
        mHomeTeamNotes = in.readString();
        mAwayTeamNotes = in.readString();
        mGroup = in.readString();
        mStadium = in.readString();
        mStage = in.readString();
        mDateAndTime = (Date) in.readSerializable();
        mHomeTeam = in.readParcelable(Country.class.getClassLoader());
        mAwayTeam = in.readParcelable(Country.class.getClassLoader());
    }

    public static final Creator<Match> CREATOR = new Creator<Match>() {
        @Override
        public Match createFromParcel(Parcel in) {
            return new Match(in);
        }

        @Override
        public Match[] newArray(int size) {
            return new Match[size];
        }
    };

    @Override
    public int compareTo(@NonNull Match o) {
        return this.mMatchNo - o.mMatchNo;
    }

    @Override
    public String toString() {
        return "_id: " + mID
                + ", MatchNo: " + Integer.toString(mMatchNo)
                + ", HomeTeamID: " + mHomeTeamID
                + ", AwayTeamID: " + mAwayTeamID
                + ", HomeTeamGoals: " + Integer.toString(mHomeTeamGoals)
                + ", AwayTeamGoals: " + Integer.toString(mAwayTeamGoals)
                + ", HomeTeamNotes: " + mHomeTeamNotes
                + ", AwayTeamNotes: " + mAwayTeamNotes
                + ", Group: " + mGroup
                + ", Stage: " + mStage
                + ", Stadium: " + mStadium
                + ", DateTime: " + ISO8601.fromDate(mDateAndTime);
    }

    public static Match instance(Match match) {
        Parcel p1 = Parcel.obtain();
        Parcel p2 = Parcel.obtain();
        match.writeToParcel(p1, 0);
        byte[] bytes = p1.marshall();
        p2.unmarshall(bytes, 0, bytes.length);
        p2.setDataPosition(0);
        Match m = new Match(p2);
        p1.recycle();
        p2.recycle();
        return m;
    }
}
