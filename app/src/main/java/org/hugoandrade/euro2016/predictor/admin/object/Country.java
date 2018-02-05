package org.hugoandrade.euro2016.predictor.admin.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimProvider;

public class Country implements Comparable<Country>, Parcelable {

    @SuppressWarnings("unused") private static final String TAG = Country.class.getSimpleName();

    private String mID;
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

    private List<Integer> mGoalsForList;
    private List<Integer> mGoalsAgainstList;
    private List<String> mOpponentList;

    public static class Entry {

        public static final String TABLE_NAME = "Country";

        public static class Cols {
            public static final String ID = "id";
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
            public static final String COEFFICIENT = "Coefficient";
            public static final String FAIR_PLAY_POINTS = "FairPlayPoints";
        }


        // SQLite table mName
        // PATH_LOGIN & TOKEN for entire table
        public static final String PATH = TABLE_NAME;
        public static final int PATH_TOKEN = 150;

        // PATH_LOGIN & TOKEN for single row of table
        public static final String PATH_FOR_ID = PATH + "/#";
        public static final int PATH_FOR_ID_TOKEN = 160;

        // CONTENT/MIME TYPE for this content
        private final static String MIME_TYPE_END = PATH;
        public static final String CONTENT_TYPE_DIR = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.dir/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
        public static final String CONTENT_ITEM_TYPE = CloudDatabaseSimProvider.ORGANIZATIONAL_NAME
                + ".cursor.item/" + CloudDatabaseSimProvider.ORGANIZATIONAL_NAME + "." + MIME_TYPE_END;
    }

    public Country(String id,
                   String name,
                   int matchesPlayed,
                   int victories,
                   int draws,
                   int defeats,
                   int goalsFor,
                   int goalsAgainst,
                   int goalsDifference,
                   String group,
                   int points,
                   int position,
                   float coefficient,
                   int fairPlayPoints) {
        mID = id;
        mName = name;
        mMatchesPlayed = matchesPlayed;
        mVictories = victories;
        mDraws = draws;
        mDefeats = defeats;
        mGoalsFor = goalsFor;
        mGoalsAgainst = goalsAgainst;
        mGoalsDifference = goalsDifference;
        mGroup = group;
        mPoints = points;
        mPosition = position;
        mCoefficient = coefficient;
        mFairPlayPoints = fairPlayPoints;
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
        mGoalsForList = new ArrayList<>();
        mGoalsAgainstList = new ArrayList<>();
        mOpponentList = new ArrayList<>();
        if (goalsForList != null)
            mGoalsForList.addAll(goalsForList);
        if (goalsAgainstList != null)
            mGoalsAgainstList.addAll(goalsAgainstList);
        if (opponentList != null)
            mOpponentList.addAll(opponentList);

        updateGroupStats();
    }

    public String getID() {
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

    public void setMatchesPlayed(int matchesPlayed) {
        mMatchesPlayed = matchesPlayed;
    }

    public void setGoalsFor(int goalsFor) {
        mGoalsFor = goalsFor;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        mGoalsAgainst = goalsAgainst;
    }

    public void setGoalsDifference(int goalsDifference) {
        mGoalsDifference = goalsDifference;
    }

    public void setPoints(int points) {
        mPoints = points;
    }

    public void setVictories(int victories) {
        mVictories = victories;
    }

    public void setDraws(int draws) {
        mDraws = draws;
    }

    public void setDefeats(int defeats) {
        mDefeats = defeats;
    }

    public int getFairPlayPoints() {
        return mFairPlayPoints;
    }

    public float getCoefficient() {
        return mCoefficient;
    }

    public Country(Parcel in) {
        mID = in.readString();
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
                + ", id: " + this.mID
                + ", Points: " + Integer.toString(this.mPoints)
                + ", GD: " + Integer.toString(this.mGoalsDifference)
                + ", GF: " + Integer.toString(this.mGoalsFor)
                + ", GA: " + Integer.toString(this.mGoalsAgainst)
                + ", MP: " + Integer.toString(this.mMatchesPlayed)
                + ", P: " + Integer.toString(this.mPosition)
                + ", G: " + this.mGroup;
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
    public boolean equals(Object o) {
        if (o != null && o instanceof Country) {
            Country c = (Country) o;
            if (!mID.equals(c.mID))
                return false;
            if (!mName.equals(c.mName))
                return false;
            if (mMatchesPlayed != c.mMatchesPlayed)
                return false;
            if (mVictories != c.mVictories)
                return false;
            if (mDraws != c.mDraws)
                return false;
            if (mDefeats != c.mDefeats)
                return false;
            if (mGoalsFor != c.mGoalsFor)
                return false;
            if (mGoalsAgainst != c.mGoalsAgainst)
                return false;
            if (mGoalsDifference != c.mGoalsDifference)
                return false;
            if (!mGroup.equals(c.mGroup))
                return false;
            if (mPoints != c.mPoints)
                return false;
            if (mPosition != c.mPosition)
                return false;
            if (mFairPlayPoints != c.mFairPlayPoints)
                return false;
            if (mCoefficient != c.mCoefficient)
                return false;
            return true;
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
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
        if (mGoalsAgainstList.size() != 3 && mGoalsForList.size() != 3 && mOpponentList.size() != 3)
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
        for (int goalsF : mGoalsForList)
            if (goalsF != -1)
                mMatchesPlayed++;

        // Set \"Goals For\"
        for (int goalsF : mGoalsForList)
            if (goalsF != -1)
                mGoalsFor += goalsF;

        // Set \"Goals Against\"
        for (int goalsA : mGoalsAgainstList)
            if (goalsA != -1)
                mGoalsAgainst += goalsA;

        // Set \"Goal Difference\"
        mGoalsDifference = mGoalsFor - mGoalsAgainst;

        // Set \"Victories\", \"Draws\", \"Defeats\" and, consequently, \"Points\"
        for (int i = 0 ; i < 3 ; i++)
            if (mGoalsForList.get(i) != -1) {
                // if Goals scored are bigger than Goals conceded (or not set at all) in match "i"...
                // ... Victory
                if (mGoalsForList.get(i) >
                        (mGoalsAgainstList.get(i) == -1 ? 0 : mGoalsAgainstList.get(i))) {
                    mPoints = mPoints + 3;
                    mVictories += 1;
                }
                // ... are equal ... Draw
                else if(mGoalsForList.get(i) ==
                        (mGoalsAgainstList.get(i) == -1 ? 0 : mGoalsAgainstList.get(i))) {
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
                if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (mGoalsForList.get(i) != -1)
                        mMatchesPlayed++;
                }
            mGoalsFor = 0;
            for (int i = 0; i < 3; i++)
                if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (mGoalsForList.get(i) != -1)
                        mGoalsFor += mGoalsForList.get(i);
                }
            mGoalsAgainst = 0;
            for (int i = 0; i < 3; i++)
                if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (mGoalsAgainstList.get(i) != -1)
                        mGoalsAgainst += mGoalsAgainstList.get(i);
                }
            mGoalsDifference = mGoalsFor - mGoalsAgainst;
            mPoints = 0;
            mVictories = 0;
            mDefeats = 0;
            mDraws = 0;
            for (int i = 0; i < 3; i++)
                if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (mGoalsForList.get(i) != -1) {
                        if (mGoalsForList.get(i) > (mGoalsAgainstList.get(i) == -1 ? 0 : mGoalsForList.get(i))) {
                            mPoints = mPoints + 3;
                            mVictories += 1;
                        } else if (mGoalsForList.get(i) == (mGoalsAgainstList.get(i) == -1 ? 0 : mGoalsForList.get(i))) {
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
            if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    mOpponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (mGoalsForList.get(i) != -1)
                    mMatchesPlayed++;
            }
        mGoalsFor = 0;
        for (int i = 0; i < 3; i++)
            if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    mOpponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (mGoalsForList.get(i) != -1)
                    mGoalsFor += mGoalsForList.get(i);
            }
        mGoalsAgainst = 0;
        for (int i = 0; i < 3; i++)
            if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    mOpponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (mGoalsAgainstList.get(i) != -1)
                    mGoalsAgainst += mGoalsAgainstList.get(i);
            }
        mGoalsDifference = mGoalsFor - mGoalsAgainst;
        mPoints = 0;
        mVictories = 0;
        mDefeats = 0;
        mDraws = 0;
        for (int i = 0; i < 3; i++)
            if (mOpponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    mOpponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (mGoalsForList.get(i) != -1) {
                    if (mGoalsForList.get(i) > (mGoalsAgainstList.get(i) == -1 ? 0 : mGoalsForList.get(i))) {
                        mPoints = mPoints + 3;
                        mVictories += 1;
                    } else if (mGoalsForList.get(i) == (mGoalsAgainstList.get(i) == -1 ? 0 : mGoalsForList.get(i))) {
                        mPoints = mPoints + 1;
                        mDraws += 1;
                    } else
                        mDefeats += 1;
                }
            }
    }
}
