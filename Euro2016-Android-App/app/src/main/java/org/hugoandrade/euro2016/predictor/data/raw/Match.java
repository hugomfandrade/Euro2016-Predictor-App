package org.hugoandrade.euro2016.predictor.data.raw;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

import org.hugoandrade.euro2016.predictor.utils.ISO8601;

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
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Match))
            return false;
        if (((Match) o).mID != this.mID)
            return false;
        if (((Match) o).mMatchNo != this.mMatchNo)
            return false;
        if (((Match) o).mHomeTeamGoals != this.mHomeTeamGoals)
            return false;
        if (((Match) o).mAwayTeamGoals != this.mAwayTeamGoals)
            return false;
        if (!areEqual(((Match) o).mDateAndTime, this.mDateAndTime))
            return false;
        if (!areEqual(((Match) o).mHomeTeamID, this.mHomeTeamID))
            return false;
        if (!areEqual(((Match) o).mAwayTeamID, this.mAwayTeamID))
            return false;
        if (!areEqual(((Match) o).mHomeTeamNotes, this.mHomeTeamNotes))
            return false;
        if (!areEqual(((Match) o).mAwayTeamNotes, this.mAwayTeamNotes))
            return false;
        if (!areEqual(((Match) o).mStage, this.mStage))
            return false;
        if (!areEqual(((Match) o).mGroup, this.mGroup))
            return false;
        if (!areEqual(((Match) o).mStadium, this.mStadium))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "id: " + mID
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

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean areEqual(String obj1, String obj2) {
        if (obj1 == null && obj2 == null)
            return true;
        if (obj1 != null && obj2 != null)
            return obj1.equals(obj2);

        return false;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean areEqual(Date date1, Date date2) {
        if (date1 == null && date2 == null)
            return true;
        if (date1 != null && date2 != null)
            return date1.equals(date2);

        return false;
    }
}
