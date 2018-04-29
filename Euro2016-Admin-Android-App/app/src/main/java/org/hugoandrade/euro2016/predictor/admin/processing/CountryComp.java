package org.hugoandrade.euro2016.predictor.admin.processing;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.utils.MatchUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CountryComp implements Comparable<CountryComp> {

    public final Country mCountry;
    public final List<Match> mMatchList;

    public CountryComp(Country c) {
        mCountry = copy(c);
        mMatchList = new ArrayList<>();
    }

    public void add(Match match) {
        mMatchList.add(copy(match));
    }

    public Country getCountry() {
        return mCountry;
    }

    public String getID() {
        return mCountry.getID();
    }

    public String getGroup() {
        return mCountry.getGroup();
    }

    public int getMatchesPlayed() {
        return mCountry.getMatchesPlayed();
    }

    private Country copy(Country c) {
        Parcel p1 = Parcel.obtain();
        Parcel p2 = Parcel.obtain();
        c.writeToParcel(p1, 0);
        byte[] bytes = p1.marshall();
        p2.unmarshall(bytes, 0, bytes.length);
        p2.setDataPosition(0);
        Country m = new Country(p2);
        p1.recycle();
        p2.recycle();
        return m;
    }

    private Match copy(Match c) {
        Parcel p1 = Parcel.obtain();
        Parcel p2 = Parcel.obtain();
        c.writeToParcel(p1, 0);
        byte[] bytes = p1.marshall();
        p2.unmarshall(bytes, 0, bytes.length);
        p2.setDataPosition(0);
        Match m = new Match(p2);
        p1.recycle();
        p2.recycle();
        return m;
    }

    @Override
    public int compareTo(@NonNull CountryComp o) {
        if (mCountry.getPoints() != o.getCountry().getPoints())
            return mCountry.getPoints() - o.getCountry().getPoints();
        if (mCountry.getGoalsDifference() != o.getCountry().getGoalsDifference())
            return mCountry.getGoalsDifference() - o.getCountry().getGoalsDifference();
        if (mCountry.getGoalsFor() != o.getCountry().getGoalsFor())
            return mCountry.getGoalsFor() - o.getCountry().getGoalsFor();
        if (mCountry.getFairPlayPoints() != o.getCountry().getFairPlayPoints())
            return mCountry.getFairPlayPoints() - o.getCountry().getFairPlayPoints();
        if (mCountry.getCoefficient() != o.getCountry().getCoefficient())
            return ((int) (mCountry.getCoefficient() - o.getCountry().getCoefficient()));
        return 0;
    }

    public boolean equalsRanking(CountryComp o) {
        if (mCountry.getPoints() != o.getCountry().getPoints())
            return false;
        if (mCountry.getGoalsDifference() != o.getCountry().getGoalsDifference())
            return false;
        if (mCountry.getGoalsFor() != o.getCountry().getGoalsFor())
            return false;
        return true;
    }

    public void compute() {

        int mMatchesPlayed = 0;
        int mVictories = 0;
        int mDefeats = 0;
        int mDraws = 0;
        int mGoalsFor = 0;
        int mGoalsAgainst = 0;
        int mGoalsDifference = 0;
        int mPoints = 0;

        // Set \"Matches Played\"
        for (Match match : mMatchList) {
            if (!MatchUtils.isMatchPlayed(match))
                continue;

            mMatchesPlayed++;

            if (match.getHomeTeamID().equals(mCountry.getID())) {
                mGoalsFor += match.getHomeTeamGoals();
                mGoalsAgainst += match.getAwayTeamGoals();

                if (MatchUtils.didHomeTeamWin(match)) {
                    mPoints = mPoints + 3;
                    mVictories += 1;
                }
                else if (MatchUtils.didAwayTeamWin(match)) {
                    mDefeats += 1;
                }
                else if (MatchUtils.didTeamsTied(match)) {
                    mPoints = mPoints + 1;
                    mDraws += 1;
                }

            }
            else if (match.getAwayTeamID().equals(mCountry.getID())) {
                mGoalsFor += match.getAwayTeamGoals();
                mGoalsAgainst += match.getHomeTeamGoals();

                if (MatchUtils.didHomeTeamWin(match)) {
                    mDefeats += 1;
                }
                else if (MatchUtils.didAwayTeamWin(match)) {
                    mPoints = mPoints + 3;
                    mVictories += 1;
                }
                else if (MatchUtils.didTeamsTied(match)) {
                    mPoints = mPoints + 1;
                    mDraws += 1;
                }
            }
        }

        mGoalsDifference = mGoalsFor - mGoalsAgainst;

        mCountry.setMatchesPlayed(mMatchesPlayed);
        mCountry.setVictories(mVictories);
        mCountry.setDefeats(mDefeats);
        mCountry.setDraws(mDraws);
        mCountry.setGoalsFor(mGoalsFor);
        mCountry.setGoalsAgainst(mGoalsAgainst);
        mCountry.setGoalsDifference(mGoalsDifference);
        mCountry.setPoints(mPoints);
    }

    public void compute(String... countryIDs) {

        List<String> countryIDList = Arrays.asList(countryIDs);

        int mMatchesPlayed = 0;
        int mVictories = 0;
        int mDefeats = 0;
        int mDraws = 0;
        int mGoalsFor = 0;
        int mGoalsAgainst = 0;
        int mGoalsDifference = 0;
        int mPoints = 0;

        // Set \"Matches Played\"
        for (Match match : mMatchList) {
            if (!MatchUtils.isMatchPlayed(match))
                continue;

            if (match.getHomeTeamID().equals(mCountry.getID())) {
                if (!countryIDList.contains(match.getAwayTeamID()))
                    continue;

                mMatchesPlayed++;
                mGoalsFor += match.getHomeTeamGoals();
                mGoalsAgainst += match.getAwayTeamGoals();

                if (MatchUtils.didHomeTeamWin(match)) {
                    mPoints = mPoints + 3;
                    mVictories += 1;
                } else if (MatchUtils.didAwayTeamWin(match)) {
                    mDefeats += 1;
                } else if (MatchUtils.didTeamsTied(match)) {
                    mPoints = mPoints + 1;
                    mDraws += 1;
                }

            } else if (match.getAwayTeamID().equals(mCountry.getID())) {
                if (!countryIDList.contains(match.getHomeTeamID()))
                    continue;

                mMatchesPlayed++;
                mGoalsFor += match.getAwayTeamGoals();
                mGoalsAgainst += match.getHomeTeamGoals();

                if (MatchUtils.didHomeTeamWin(match)) {
                    mDefeats += 1;
                } else if (MatchUtils.didAwayTeamWin(match)) {
                    mPoints = mPoints + 3;
                    mVictories += 1;
                } else if (MatchUtils.didTeamsTied(match)) {
                    mPoints = mPoints + 1;
                    mDraws += 1;
                }
            }
        }

        mGoalsDifference = mGoalsFor - mGoalsAgainst;

        mCountry.setMatchesPlayed(mMatchesPlayed);
        mCountry.setVictories(mVictories);
        mCountry.setDefeats(mDefeats);
        mCountry.setDraws(mDraws);
        mCountry.setGoalsFor(mGoalsFor);
        mCountry.setGoalsAgainst(mGoalsAgainst);
        mCountry.setGoalsDifference(mGoalsDifference);
        mCountry.setPoints(mPoints);
    }
}
