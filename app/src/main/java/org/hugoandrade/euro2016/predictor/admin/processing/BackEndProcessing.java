package org.hugoandrade.euro2016.predictor.admin.processing;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.Match;
import org.hugoandrade.euro2016.predictor.admin.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.StaticVariableUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackEndProcessing {

    private final static String TAG = BackEndProcessing.class.getSimpleName();

    private final WeakReference<OnProcessingFinished> mOnProcessingFinished;
    private final List<Country> mCountryList;

    private GroupProcessing mTask;

    public BackEndProcessing(OnProcessingFinished onProcessingFinished, List<Country> allCountryList) {
        mOnProcessingFinished = new WeakReference<>(onProcessingFinished);
        mCountryList = allCountryList;
    }

    public void startUpdateGroupsProcessing(List<Match> matchList) {
        // Do processing asynchronously
        mTask = new GroupProcessing(this, mCountryList, matchList);
        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class GroupProcessing extends AsyncTask<Void, ProgressContainer, ResultContainer> {

        private final WeakReference<BackEndProcessing> mBackEndProcessing;
        private final SparseArray<Match> mMatchMap = new SparseArray<>();
        private final HashMap<String, Country> mCountryMap = new HashMap<>();

        GroupProcessing(BackEndProcessing backEndProcessing, List<Country> countryList, List<Match> matchList) {
            mBackEndProcessing = new WeakReference<>(backEndProcessing);
            for (Country c : countryList)
                mCountryMap.put(c.getID(), c);
            for (Match match : matchList)
                mMatchMap.append(match.getMatchNumber(), match);
        }

        @Override
        protected ResultContainer doInBackground(Void... aVoid) {

            // Create List of countries object, and group them by Group
            HashMap<String, GroupComp> allGroups = updateCountries(mMatchMap, mCountryMap);

            // Order each group
            List<Country> updatedCountryList =  new ArrayList<>();
            for (GroupComp group : allGroups.values()) {
                group.orderGroup();
                updatedCountryList.addAll(group.getCountryList());
            }

            // Find countries whose info changed from the original one.
            for (Country updatedCountry : updatedCountryList) {
                Country originalCountry = mCountryMap.get(updatedCountry.getID());
                if (!updatedCountry.equals(originalCountry))
                    publishProgress(new ProgressContainer(updatedCountry));
            }

            // Put in database only the countries that were whose info was modified
            // Check if all matches of each group have been played. If yes, update the matches
            // of the knockout stage appropriately (The first- and second-place teams in each group)
            for (GroupComp group : allGroups.values()) {
                for (Match match : updateRoundOf16WhenGroupWasPlayed(mMatchMap, group))
                    publishProgress(new ProgressContainer(match));
            }

            // Check if all matches of group stage have been played. If yes, compute the third-
            // tied team and update the matches of the knockout stage appropriately
            // (The third-place teams in each group)
            for (Match match : updateRoundOf16WhenAllGroupsWerePlayed(mMatchMap, allGroups))
                publishProgress(new ProgressContainer(match));

            for (Match match : updateRemainingKnockOutMatchUps(mMatchMap))
                publishProgress(new ProgressContainer(match));

            return new ResultContainer(toList(mCountryMap), toList(mMatchMap));
        }

        private List<Match> toList(SparseArray<Match> mMatchMap) {
            List<Match> tList = new ArrayList<>();
            for (int i = 0 ; i < mMatchMap.size() ; i++)
                tList.add(mMatchMap.valueAt(i));
            return tList;
        }

        private List<Country> toList(HashMap<String, Country> mCountryMap) {
            return new ArrayList<>(mCountryMap.values());
        }

        @Override
        protected void onProgressUpdate(ProgressContainer... progressContainers) {
            if (mBackEndProcessing.get() == null)
                return;

            for (ProgressContainer progressContainer : progressContainers) {
                if (progressContainer.mCountry != null)
                    mBackEndProcessing.get().onUpdateCountry(progressContainer.mCountry);
                if (progressContainer.mMatch != null)
                    mBackEndProcessing.get().onUpdateMatchUp(progressContainer.mMatch);
            }
        }

        @Override
        protected void onPostExecute(ResultContainer resultContainer) {
            super.onPostExecute(resultContainer);

            if (mBackEndProcessing.get() != null)
                mBackEndProcessing.get().onProcessingFinished(resultContainer.mCountryList,
                                                              resultContainer.mMatchList);
        }

        private HashMap<String, GroupComp> updateCountries(SparseArray<Match> matchMap,
                                                           HashMap<String, Country> countryMap) {

            HashMap<String, GroupComp> allGroups = new HashMap<>();
            // Iterate over all 32 countries. We gonna make a Country object for each one.
            for (Map.Entry<String, Country> c : countryMap.entrySet()) {
                CountryComp countryComp = new CountryComp(c.getValue());

                for (Match m : getGroupStageMatches(matchMap)) {
                    if (m.getHomeTeamID().equals(countryComp.getID())
                            || m.getAwayTeamID().equals(countryComp.getID())) {
                        countryComp.add(m);
                    }
                }

                if (allGroups.containsKey(countryComp.getGroup())) {
                    allGroups.get(countryComp.getGroup()).add(countryComp);
                } else {
                    allGroups.put(countryComp.getGroup(), new GroupComp(countryComp.getGroup()));
                    allGroups.get(countryComp.getGroup()).add(countryComp);
                }
            }

            return allGroups;
        }

        private List<Match> updateRoundOf16WhenGroupWasPlayed(SparseArray<Match> allMatches,
                                                              GroupComp groupComp) {
            String group = groupComp.getGroup();
            // Check if all countries have played 3 matches
            boolean areAllMatchesPlayed = true;
            for (Country c : groupComp.getCountryList())
                if (c.getMatchesPlayed() != 3)
                    areAllMatchesPlayed = false;

            ListContainer<Match> matchListContainer = new ListContainer<>();
            // Update knockout mStage matches only if necessary
            switch (group) {
                case "A": {
                    if (areAllMatchesPlayed) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), groupComp.get(0).getID(), "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(37), groupComp.get(1).getID(), "HOME"));
                    } else {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), "Winner Group " + group, "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(37), "Runner-up Group " + group, "HOME"));
                    }
                    break;
                }
                case "B": {
                    if (areAllMatchesPlayed) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), groupComp.get(0).getID(), "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(44), groupComp.get(1).getID(), "HOME"));
                    } else {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), "Winner Group " + group, "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(44), "Runner-up Group " + group, "HOME"));
                    }
                    break;
                }
                case "C": {
                    if (areAllMatchesPlayed) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), groupComp.get(0).getID(), "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(37), groupComp.get(1).getID(), "AWAY"));
                    } else {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), "Winner Group " + group, "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(37), "Runner-up Group " + group, "AWAY"));
                    }
                    break;
                }
                case "D": {
                    if (areAllMatchesPlayed) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), groupComp.get(0).getID(), "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(43), groupComp.get(1).getID(), "AWAY"));
                    } else {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), "Winner Group " + group, "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(43), "Runner-up Group " + group, "AWAY"));
                    }
                    break;
                }
                case "E": {
                    if (areAllMatchesPlayed) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(43), groupComp.get(0).getID(), "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(42), groupComp.get(1).getID(), "AWAY"));
                    } else {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(43), "Winner Group " + group, "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(42), "Runner-up Group " + group, "AWAY"));
                    }
                    break;
                }
                case "F": {
                    if (areAllMatchesPlayed) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(42), groupComp.get(0).getID(), "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(44), groupComp.get(1).getID(), "AWAY"));
                    } else {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(42), "Winner Group " + group, "HOME"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(44), "Runner-up Group " + group, "AWAY"));
                    }
                    break;
                }
            }

            return matchListContainer.getList();
        }

        private List<Match> updateRoundOf16WhenAllGroupsWerePlayed(SparseArray<Match> allMatches,
                                                                   HashMap<String, GroupComp> allGroups) {

            ListContainer<Match> matchListContainer = new ListContainer<>();
            // Check if all countries in each group have played 3 (all) Group Stage matches.
            // If not, return
            for (GroupComp group : allGroups.values())
                for (CountryComp country : group.getCountryCompList())
                    if (country.getMatchesPlayed() != 3) {
                        // Do not compute third-tied tiebreaker. Set to default!
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), "3rd Place C, D or E", "AWAY"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), "3rd Place A, C or D", "AWAY"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), "3rd Place A, B or F", "AWAY"));
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), "3rd Place B, E or F", "AWAY"));
                        return matchListContainer.getList();
                    }

            // Create 6-country group of all third place teams
            ArrayList<CountryComp> thirdPlaceGroup = new ArrayList<>();
            thirdPlaceGroup.add(allGroups.get("A").get(2));
            thirdPlaceGroup.add(allGroups.get("B").get(2));
            thirdPlaceGroup.add(allGroups.get("C").get(2));
            thirdPlaceGroup.add(allGroups.get("D").get(2));
            thirdPlaceGroup.add(allGroups.get("E").get(2));
            thirdPlaceGroup.add(allGroups.get("F").get(2));

            // Sort 6-countries group
            Collections.sort(thirdPlaceGroup, Collections.<CountryComp>reverseOrder());

            // Add 4 best third-place teams to a new ArrayList a sort it by Group
            ArrayList<CountryComp> bestThirdPlaceTeams = new ArrayList<>();
            bestThirdPlaceTeams.add(thirdPlaceGroup.get(0));
            bestThirdPlaceTeams.add(thirdPlaceGroup.get(1));
            bestThirdPlaceTeams.add(thirdPlaceGroup.get(2));
            bestThirdPlaceTeams.add(thirdPlaceGroup.get(3));
            Collections.sort(bestThirdPlaceTeams, new Comparator<CountryComp>() {
                @Override
                public int compare(CountryComp lhs, CountryComp rhs) {
                    return lhs.getGroup().toCharArray()[0] - rhs.getGroup().toCharArray()[0];
                }
            });
            ArrayList<String> bestThirdPlaceGroups = new ArrayList<>();
            bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(0).getGroup());
            bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(1).getGroup());
            bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(2).getGroup());
            bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(3).getGroup());

            if (bestThirdPlaceGroups.contains("A")){ //A - - -
                if (bestThirdPlaceGroups.contains("C") && bestThirdPlaceGroups.contains("D")) { //A-C-D-
                    if (bestThirdPlaceGroups.contains("B")) { //A-B-C-D
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WA vs 3C
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WB vs 3D
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WC vs 3A
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WD vs 3B
                    }
                    else { //A-C-D-*(E/F)
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WA vs 3C
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WB vs 3D
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WC vs 3A
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WD vs 3(E/F)
                    }
                }
                else { // A-B-*(C/D/E)-*(E/F)
                    if (bestThirdPlaceGroups.contains("B")) {
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WA vs 3(C-D-E)
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WB vs 3A
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WC vs 3B
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WD vs 3(E-F) (always a group later than vs WA)
                    }
                    else { // A-*(C/D)-E-F
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WA vs 3(C-D)
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WB vs 3A
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WC vs 3E
                        matchListContainer.add(
                                updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WD vs 3F
                    }
                }
            }
            else if (bestThirdPlaceGroups.contains("B")) {
                if (bestThirdPlaceGroups.contains("C") && bestThirdPlaceGroups.contains("D")) { //B-C-D-*(E/F)
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WA vs 3C
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WB vs 3D
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WC vs 3B
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WD vs 3(E-F)
                }
                else {//B-*(C/D)-E-F
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WA vs 3E
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WB vs 3(C-D)
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WC vs 3B
                    matchListContainer.add(
                            updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WD vs 3F
                }
            } else if (bestThirdPlaceGroups.contains("C")) { //C-D-E-F
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(40), bestThirdPlaceTeams.get(0).getID(), "AWAY")); // WA vs 3C
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(38), bestThirdPlaceTeams.get(1).getID(), "AWAY")); // WB vs 3D
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(41), bestThirdPlaceTeams.get(3).getID(), "AWAY")); // WC vs 3E
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(39), bestThirdPlaceTeams.get(2).getID(), "AWAY")); // WD vs 3F
            }
            else { // Set to default!! An error occurred

                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(40), "3rd Place C, D or E", "AWAY"));
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(38), "3rd Place A, C or D", "AWAY"));
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(41), "3rd Place A, B or F", "AWAY"));
                matchListContainer.add(
                        updateRoundOf16MatchUp(allMatches.get(39), "3rd Place B, E or F", "AWAY"));

            }
            return matchListContainer.getList();
        }

        private Match updateRoundOf16MatchUp(Match match, String teamID, String matchUpPosition) {
            // Check if there is any need to update match-up
            if (matchUpPosition.equals("HOME") && match.getHomeTeamID().equals(teamID))
                return null;
            if (matchUpPosition.equals("AWAY") && match.getAwayTeamID().equals(teamID))
                return null;

            // Update match-up accordingly
            switch (matchUpPosition) {
                case "HOME":
                    match.setHomeTeamID(teamID);
                    break;
                case "AWAY":
                    match.setAwayTeamID(teamID);
                    break;
                default:
                    Log.e(TAG, "MatchUp position (home or away) not recognized");
                    return null;
            }

            return match;
        }

        private List<Match> updateRemainingKnockOutMatchUps(SparseArray<Match> allMatches) {

            ListContainer<Match> matchListContainer = new ListContainer<>();
            // Quarter Finals
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 37, 45, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 39, 45, "AWAY"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 38, 46, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 42, 46, "AWAY"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 41, 47, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 43, 47, "AWAY"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 40, 48, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 44, 48, "AWAY"));

            // Semi Finals
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 45, 49, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 46, 49, "AWAY"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 47, 50, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 48, 50, "AWAY"));

            // Final
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 49, 51, "HOME"));
            matchListContainer.add(updateKnockOutMatchUp(allMatches, 50, 51, "AWAY"));

            return matchListContainer.getList();
        }

        private Match updateKnockOutMatchUp(SparseArray<Match> allMatches,
                                            int matchUpNumber,
                                            int matchUpToUpdate,
                                            String matchUpToUpdatePosition) {
            Match match = allMatches.get(matchUpNumber);

            String teamName;
            if (!MatchUtils.isMatchPlayed(match) && MatchUtils.didTeamsTied(match)) {
                teamName = "Winner Match " + Integer.toString(matchUpNumber);
            }
            else {
                if (MatchUtils.didHomeTeamWin(match) || MatchUtils.didHomeTeamWinByPenaltyShootout(match))
                    teamName = match.getHomeTeamID();
                else if (MatchUtils.didAwayTeamWin(match) || MatchUtils.didAwayTeamWinByPenaltyShootout(match))
                    teamName = match.getAwayTeamID();
                else
                    teamName = "Winner Match " + Integer.toString(matchUpNumber);
            }

            Match matchToUpdate = allMatches.get(matchUpToUpdate);


            if (matchUpToUpdatePosition.equals("AWAY")) {
                if (!matchToUpdate.getAwayTeamID().equals(teamName)) {
                    matchToUpdate.setAwayTeamID(teamName);
                    return matchToUpdate;
                }
            } else if (matchUpToUpdatePosition.equals("HOME")) {
                if (!matchToUpdate.getHomeTeamID().equals(teamName)) {
                    matchToUpdate.setHomeTeamID(teamName);
                    return matchToUpdate;
                }
            }
            return null;
        }

        private static List<Match> getGroupStageMatches(SparseArray<Match> matchMap) {
            List<Match> allGroupStageMatches = new ArrayList<>();
            for (int i = 0 ; i < matchMap.size() ; i++) {
                Match match = matchMap.valueAt(i);
                if (match.getStage().equals(StaticVariableUtils.SStage.groupStage.name))
                    allGroupStageMatches.add(match);
            }
            return allGroupStageMatches;
        }
    }

    private void onUpdateMatchUp(Match match) {
        if (mOnProcessingFinished.get() != null)
            mOnProcessingFinished.get().updateMatchUp(match);
    }

    private void onUpdateCountry(Country country) {
        if (mOnProcessingFinished.get() != null)
            mOnProcessingFinished.get().updateCountry(country);
    }

    private void onProcessingFinished(List<Country> countryList, List<Match> matchList) {
        if (mOnProcessingFinished.get() != null)
            mOnProcessingFinished.get().onProcessingFinished(countryList, matchList);
    }

    public void cancel() {
        mTask.cancel(true);
        mTask = null;
    }

    public interface OnProcessingFinished {
        void onProcessingFinished(List<Country> countryList, List<Match> matchList);
        void updateCountry(Country country);
        void updateMatchUp(Match match);
    }
}
