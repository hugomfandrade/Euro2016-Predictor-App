package org.hugoandrade.euro2016.backend.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

import org.hugoandrade.euro2016.backend.cloudsim.CloudDatabaseSimProvider;

public class Country implements Comparable<Country>, Parcelable {

    @SuppressWarnings("unused") private static final String TAG = Country.class.getSimpleName();

    private int mID;
    private String mName;
    private String mGroup;
    private float mCoefficient;
    private int mMatchesPlayed;
    private int mGoalsFor;
    private int mGoalsAgainst;
    private int mGoalsDifference;
    private int mPoints;
    private int mPosition;
    private int mVictories;
    private int mDraws;
    private int mDefeats;
    private int mFairPlayPoints;

    private List<Integer> goalsForList;
    private List<Integer> goalsAgainstList;
    private List<String> opponentList;
    
    public static class Entry {

        public static final String TABLE_NAME = "Country";

        public static class Cols {
            public static final String _ID = "_id";
            public static final String NAME = "Name";
            public static final String MATCHES_PLAYED = "MatchesPlayed";
            public static final String VICTORIES = "Victories";
            public static final String DRAWS = "Draws";
            public static final String DEFEATS = "Defeats";
            public static final String GOALS_FOR = "GoalsFor";
            public static final String GOALS_AGAINST = "GoalsAgainst";
            public static final String GOALS_DIFFERENCE = "GoalsDifference";
            public static final String GROUP = "GroupLetter";
            public static final String POSITION = "Position";
            public static final String POINTS = "Points";
        }


        // SQLite table mName
        // PATH & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 150;

        // PATH & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 160;

        // CONTENT/MIME TYPE for this content
        private final static String MIME_TYPE_END = PATH;
        public static final String CONTENT_TYPE_DIR = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.dir/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
        public static final String CONTENT_ITEM_TYPE = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.item/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
    }

    public Country(int id, String name, int matchesPlayed, int victories, int draws, int defeats,
                    int goalsFor, int goalsAgainst, int goalsDifference, String group, int points,
                    int position) {
        this.mID = id;
        this.mName = name;
        this.mMatchesPlayed = matchesPlayed;
        this.mVictories = victories;
        this.mDraws = draws;
        this.mDefeats = defeats;
        this.mGoalsFor = goalsFor;
        this.mGoalsAgainst = goalsAgainst;
        this.mGoalsDifference = goalsDifference;
        this.mGroup = group;
        this.mPoints = points;
        this.mPosition = position;
    }

    public Country(String name,
                   String group,
                   float coefficient,
                   ArrayList<Integer> goalsForList,
                   ArrayList<Integer> goalsAgainstList,
                   ArrayList<String> opponentList){
        mName = name;
        mGroup = group;
        mCoefficient = coefficient;
        this.goalsForList = new ArrayList<>();
        this.goalsAgainstList = new ArrayList<>();
        this.opponentList = new ArrayList<>();
        if (goalsForList != null)
            this.goalsForList.addAll(goalsForList);
        if (goalsAgainstList != null)
            this.goalsAgainstList.addAll(goalsAgainstList);
        if (opponentList != null)
            this.opponentList.addAll(opponentList);

        updateGroupStats();
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getGroup() {
        return mGroup;
    }

    public int getMatchesPlayed() {
        return mMatchesPlayed;
    }

    public int getGoalsFor() {
        return mGoalsFor;
    }

    public int getGoalsAgainst() {
        return mGoalsAgainst;
    }

    public int getGoalsDifference() {
        return mGoalsDifference;
    }

    public int getPoints() {
        return mPoints;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public int getVictories() {
        return mVictories;
    }

    public int getDraws() {
        return mDraws;
    }

    public int getDefeats() {
        return mDefeats;
    }

    private Country(Parcel in) {
        mID = in.readInt();
        mName = in.readString();
        mGroup = in.readString();
        mCoefficient = in.readFloat();
        mMatchesPlayed = in.readInt();
        mGoalsFor = in.readInt();
        mGoalsAgainst = in.readInt();
        mGoalsDifference = in.readInt();
        mPoints = in.readInt();
        mPosition = in.readInt();
        mVictories = in.readInt();
        mDraws = in.readInt();
        mDefeats = in.readInt();
        mFairPlayPoints = in.readInt();
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };

    public String toString() {
        return this.mName
                + ", Points: " + Integer.toString(this.mPoints)
                + ", GD: " + Integer.toString(this.mGoalsDifference)
                + ", GF: " + Integer.toString(this.mGoalsFor)
                + ", GA: " + Integer.toString(this.mGoalsAgainst)
                + ", MP: " + Integer.toString(this.mMatchesPlayed);
    }

    @Override
    public int compareTo(@NonNull Country o) {
        if (mPoints != o.mPoints)
            return mPoints - o.mPoints;
        if (mGoalsDifference != o.mGoalsDifference)
            return mGoalsDifference - o.mGoalsDifference;
        if (mGoalsFor != o.mGoalsFor)
            return mGoalsFor - o.mGoalsFor;
        if (mFairPlayPoints != o.mFairPlayPoints)
            return mFairPlayPoints - o.mFairPlayPoints;
        if (mCoefficient != o.mCoefficient)
            return ((int) (mCoefficient - o.mCoefficient));
        return 0;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean equalsRanking(Country o) {
        if (this.mPoints != o.mPoints)
            return false;
        if (this.mGoalsDifference != o.mGoalsDifference)
            return false;
        if (this.mGoalsFor != o.mGoalsFor)
            return false;
        return true;

    }

    public void set(Country o) {
        if (!this.mName.equals(o.mName) || !this.mGroup.equals(o.mGroup))
            return;

        mMatchesPlayed = o.mMatchesPlayed;
        mVictories = o.mVictories;
        mDraws = o.mDraws;
        mDefeats = o.mDefeats;
        mGoalsFor = o.mGoalsFor;
        mGoalsAgainst = o.mGoalsAgainst;
        mGoalsDifference = o.mGoalsDifference;
        mPoints = o.mPoints;
        mPosition = o.mPosition;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean equalsInstance(Country o) {
        if (!mName.equals(o.mName))
            return false;
        if (mMatchesPlayed != o.mMatchesPlayed)
            return false;
        if (mVictories != o.mVictories)
            return false;
        if (mDraws != o.mDraws)
            return false;
        if (mDefeats != o.mDefeats)
            return false;
        if (mGoalsFor != o.mGoalsFor)
            return false;
        if (mGoalsAgainst != o.mGoalsAgainst)
            return false;
        if (mGoalsDifference != o.mGoalsDifference)
            return false;
        if (!mGroup.equals(o.mGroup))
            return false;
        if (mPoints != o.mPoints)
            return false;
        if (mPosition != o.mPosition)
            return false;
        return true;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mName);
        dest.writeString(mGroup);
        dest.writeFloat(mCoefficient);
        dest.writeInt(mMatchesPlayed);
        dest.writeInt(mGoalsFor);
        dest.writeInt(mGoalsAgainst);
        dest.writeInt(mGoalsDifference);
        dest.writeInt(mPoints);
        dest.writeInt(mPosition);
        dest.writeInt(mVictories);
        dest.writeInt(mDraws);
        dest.writeInt(mDefeats);
        dest.writeInt(mFairPlayPoints);
    }

    public void updateGroupStats() {
        if (goalsAgainstList.size() != 3 && goalsForList.size() != 3 && opponentList.size() != 3)
            return;

        mMatchesPlayed = 0;
        mVictories = 0;
        mDefeats = 0;
        mDraws = 0;
        mGoalsFor = 0;
        mGoalsAgainst = 0;
        mGoalsDifference = 0;
        mPoints = 0;

        // Set \"Matches Played\"
        for (int goalsF : goalsForList)
            if (goalsF != -1)
                mMatchesPlayed++;

        // Set \"Goals For\"
        for (int goalsF : goalsForList)
            if (goalsF != -1)
                mGoalsFor += goalsF;

        // Set \"Goals Against\"
        for (int goalsA : goalsAgainstList)
            if (goalsA != -1)
                mGoalsAgainst += goalsA;

        // Set \"Goal Difference\"
        mGoalsDifference = mGoalsFor - mGoalsAgainst;

        // Set \"Victories\", \"Draws\", \"Defeats\" and, consequently, \"Points\"
        for (int i = 0 ; i < 3 ; i++)
            if (goalsForList.get(i) != -1) {
                // if Goals scored are bigger than Goals conceded (or not set at all) in match "i"...
                // ... Victory
                if (goalsForList.get(i) >
                        (goalsAgainstList.get(i) == -1 ? 0 : goalsAgainstList.get(i))) {
                    mPoints = mPoints + 3;
                    mVictories += 1;
                }
                // ... are equal ... Draw
                else if(goalsForList.get(i) ==
                        (goalsAgainstList.get(i) == -1 ? 0 : goalsAgainstList.get(i))) {
                    mPoints = mPoints + 1;
                    mDraws += 1;
                }
                // ... are lower ... Defeat
                else {
                    mDefeats += 1;
                }
            }
    }

    public void updateStatsFilterByCountry(ArrayList<String> filterCountryNames) {
        if (filterCountryNames.size() == 1) {
            mMatchesPlayed = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsForList.get(i) != -1)
                        mMatchesPlayed++;
                }
            mGoalsFor = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsForList.get(i) != -1)
                        mGoalsFor += goalsForList.get(i);
                }
            mGoalsAgainst = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsAgainstList.get(i) != -1)
                        mGoalsAgainst += goalsAgainstList.get(i);
                }
            mGoalsDifference = mGoalsFor - mGoalsAgainst;
            mPoints = 0;
            mVictories = 0;
            mDefeats = 0;
            mDraws = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsForList.get(i) != -1) {
                        if (goalsForList.get(i) > (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                            mPoints = mPoints + 3;
                            mVictories += 1;
                        } else if (goalsForList.get(i) == (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                            mPoints = mPoints + 1;
                            mDraws += 1;
                        }
                        else
                            mDefeats += 1;

                    }
                }
            return;
        }
        if (filterCountryNames.size() != 2)
            return;
        mMatchesPlayed = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsForList.get(i) != -1)
                    mMatchesPlayed++;
            }
        mGoalsFor = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsForList.get(i) != -1)
                    mGoalsFor += goalsForList.get(i);
            }
        mGoalsAgainst = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsAgainstList.get(i) != -1)
                    mGoalsAgainst += goalsAgainstList.get(i);
            }
        mGoalsDifference = mGoalsFor - mGoalsAgainst;
        mPoints = 0;
        mVictories = 0;
        mDefeats = 0;
        mDraws = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsForList.get(i) != -1) {
                    if (goalsForList.get(i) > (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                        mPoints = mPoints + 3;
                        mVictories += 1;
                    } else if (goalsForList.get(i) == (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                        mPoints = mPoints + 1;
                        mDraws += 1;
                    } else
                        mDefeats += 1;
                }
            }
    }
}
