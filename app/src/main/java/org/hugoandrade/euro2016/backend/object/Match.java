package org.hugoandrade.euro2016.backend.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

import org.hugoandrade.euro2016.backend.cloudsim.CloudDatabaseSimProvider;
import org.hugoandrade.euro2016.backend.utils.ISO8601;

public class Match implements Comparable<Match>, Parcelable {

    @SuppressWarnings("unused") private static final String TAG = Match.class.getSimpleName();

    public static class Entry {

        public static final String TABLE_NAME = "Match";

        public static final String COLUMN__ID = "_id";
        public static final String COLUMN_MATCH_NO = "MatchNo";
        public static final String COLUMN_HOME_TEAM = "HomeTeamID";
        public static final String COLUMN_AWAY_TEAM = "AwayTeamID";
        public static final String COLUMN_HOME_TEAM_GOALS = "HomeTeamGoals";
        public static final String COLUMN_AWAY_TEAM_GOALS = "AwayTeamGoals";
        public static final String COLUMN_HOME_TEAM_NOTES = "HomeTeamNotes";
        public static final String COLUMN_AWAY_TEAM_NOTES = "AwayTeamNotes";
        public static final String COLUMN_GROUP = "GroupLetter";
        public static final String COLUMN_STAGE = "Stage";
        public static final String COLUMN_STADIUM = "Stadium";
        public static final String COLUMN_DATE_AND_TIME = "DateAndTime";

        // SQLite table mName
        // PATH & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 110;

        // PATH & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 120;

        // CONTENT/MIME TYPE for this content
        private final static String MIME_TYPE_END = PATH;
        public static final String CONTENT_TYPE_DIR = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.dir/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
        public static final String CONTENT_ITEM_TYPE = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.item/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
    }

    private int mID;
    private int mMatchNo;
    private String mHomeTeam;
    private String mAwayTeam;
    private int mHomeTeamGoals;
    private int mAwayTeamGoals;
    private String mHomeTeamNotes;
    private String mAwayTeamNotes;
    private String mStage;
    private String mGroup;
    private String mStadium;
    private Date mDateAndTime;

    public Match(int id, int matchNo,
                 String homeTeam, String awayTeam,
                 int homeGoals, int awayGoals,
                 String homeTeamNotes, String awayTeamNotes,
                 String group, String stage,
                 String stadium, Date dateAndTime) {

        this.mID = id;
        this.mMatchNo = matchNo;
        this.mHomeTeam = homeTeam;
        this.mAwayTeam = awayTeam;
        this.mHomeTeamGoals = homeGoals;
        this.mAwayTeamGoals = awayGoals;
        this.mHomeTeamNotes = homeTeamNotes;
        this.mAwayTeamNotes = awayTeamNotes;
        this.mGroup = group;
        this.mStadium = stadium;
        this.mStage = stage;
        this.mDateAndTime = dateAndTime;
    }

    public int getID() {
        return mID;
    }

    public int getMatchNumber() {
        return mMatchNo;
    }

    public String getHomeTeam() {
        return mHomeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        mHomeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return mAwayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        mAwayTeam = awayTeam;
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
        dest.writeInt(mID);
        dest.writeInt(mMatchNo);
        dest.writeString(mHomeTeam);
        dest.writeString(mAwayTeam);
        dest.writeInt(mHomeTeamGoals);
        dest.writeInt(mAwayTeamGoals);
        dest.writeString(mHomeTeamNotes);
        dest.writeString(mAwayTeamNotes);
        dest.writeString(mGroup);
        dest.writeString(mStadium);
        dest.writeString(mStage);
        dest.writeSerializable(mDateAndTime);
    }

    protected Match(Parcel in) {
        mID = in.readInt();
        mMatchNo = in.readInt();
        mHomeTeam = in.readString();
        mAwayTeam = in.readString();
        mHomeTeamGoals = in.readInt();
        mAwayTeamGoals = in.readInt();
        mHomeTeamNotes = in.readString();
        mAwayTeamNotes = in.readString();
        mGroup = in.readString();
        mStadium = in.readString();
        mStage = in.readString();
        mDateAndTime = (Date) in.readSerializable();
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

    public Match cloneInstance() {
        return new Match(mID, mMatchNo, mHomeTeam, mAwayTeam, mHomeTeamGoals, mAwayTeamGoals,
                mHomeTeamNotes, mAwayTeamNotes, mGroup, mStage, mStadium, mDateAndTime);
    }

    @Override
    public String toString() {
        return "_id: " + Integer.toString(mID)
                + ", MatchNo: " + Integer.toString(mMatchNo)
                + ", HomeTeam: " + mHomeTeam
                + ", AwayTeam: " + mAwayTeam
                + ", HomeTeamGoals: " + Integer.toString(mHomeTeamGoals)
                + ", AwayTeamGoals: " + Integer.toString(mAwayTeamGoals)
                + ", HomeTeamNotes: " + mHomeTeamNotes
                + ", AwayTeamNotes: " + mAwayTeamNotes
                + ", Group: " + mGroup
                + ", Stage: " + mStage
                + ", Stadium: " + mStadium
                + ", DateTime: " + ISO8601.fromDate(mDateAndTime);
    }
}
