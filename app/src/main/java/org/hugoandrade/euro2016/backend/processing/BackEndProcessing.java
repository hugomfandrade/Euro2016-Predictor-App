package org.hugoandrade.euro2016.backend.processing;

import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.utils.MatchUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackEndProcessing {

    private final static String TAG = BackEndProcessing.class.getSimpleName();

    private static final String[] countryNameArray = {
            "France",   "Romania",          "Albania",  "Switzerland",
            "England",  "Russia",           "Wales",    "Slovakia",
            "Germany",  "Ukraine",          "Poland",   "Northern Ireland",
            "Spain",    "Czech Republic",   "Turkey",   "Croatia",
            "Belgium",  "Italy",            "Ireland",  "Sweden",
            "Portugal", "Iceland",          "Austria",  "Hungary"
    };
    private static final String[] countryGroupArray = {
            "A" , "A", "A", "A",
            "B" , "B", "B", "B",
            "C" , "C", "C", "C",
            "D" , "D", "D", "D",
            "E" , "E", "E", "E",
            "F" , "F", "F", "F"
    };
    private static final float[] countryCoefficientArray = {
            33.599f , 28.038f, 23.216f, 31.254f,
            35.963f , 31.345f, 24.531f, 27.171f,
            40.236f , 30.313f, 28.306f, 22.961f,
            37.962f , 29.403f, 27.033f, 30.642f,
            34.442f , 34.345f, 26.902f, 29.028f,
            35.138f , 25.388f, 30.932f, 27.142f
    };

    private final OnProcessingFinished mOnProcessingFinished;
    private final HashMap<String, Country> mAllCountries;

    public BackEndProcessing(OnProcessingFinished onProcessingFinished, List<Country> allCountryList) {
        HashMap<String, Country> allCountries = new HashMap<>();

        for (Country country : allCountryList)
            allCountries.put(country.getName(), country);

        mOnProcessingFinished = onProcessingFinished;

        mAllCountries = new HashMap<>();
        mAllCountries.putAll(allCountries);
    }

    public void startUpdateGroupsProcessing(final List<Match> allMatches) {

        // Do processing asynchronously
        new AsyncTask<Match, Country, List<Match>>() {

            @Override
            protected List<Match> doInBackground(Match... matches) {

                SparseArray<Match> allMatches = new SparseArray<>();
                for (Match match : matches)
                    allMatches.append(match.getMatchNumber(), match);

                HashMap<String, List<Country>> allGroups = new HashMap<>();
                // Create List of countries object, and group them by Group
                updateCountries(allGroups,  allMatches);

                // Order each group and put on same Container
                allGroups.put("A", orderGroup(allGroups.get("A")));
                allGroups.put("B", orderGroup(allGroups.get("B")));
                allGroups.put("C", orderGroup(allGroups.get("C")));
                allGroups.put("D", orderGroup(allGroups.get("D")));
                allGroups.put("E", orderGroup(allGroups.get("E")));
                allGroups.put("F", orderGroup(allGroups.get("F")));

                List<Country> countryList = new ArrayList<>();
                for (Map.Entry<String, List<Country>> groupEntry : allGroups.entrySet())
                    countryList.addAll(groupEntry.getValue());

                publishProgress(countryList.toArray(new Country[countryList.size()]));

                // Put each country in database so that the users can retrieve the group information
                List<Country> allCountriesToUpdate = putCountriesInDatabase(allGroups);
                onUpdateCountries(allCountriesToUpdate);

                // Check if all matches of each group have been played. If yes, update the matches
                // of the knockout stage appropriately (The first- and second-place teams in each group)
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allMatches, allGroups.get("A"), "A");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allMatches, allGroups.get("B"), "B");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allMatches, allGroups.get("C"), "C");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allMatches, allGroups.get("D"), "D");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allMatches, allGroups.get("E"), "E");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allMatches, allGroups.get("F"), "F");

                // Check if all matches of group stage have been played. If yes, compute the third-
                // tied team and update the matches of the knockout stage appropriately
                // (The third-place teams in each group)
                List<Match> allMatchesToUpdate = new ArrayList<>();

                allMatchesToUpdate.addAll(
                        updateKnockOutMatchUpWhenAllGroupStageMatchesHaveBeenPlayed(allGroups, allMatches));

                allMatchesToUpdate.addAll(
                        updateKnockOutMatchUps(allMatches));

                return allMatchesToUpdate;
            }

            @Override
            protected void onProgressUpdate(Country... countries) {
                super.onProgressUpdate(countries);

                HashMap<String, List<Country>> allGroups = new HashMap<>();
                for (Country country : countries) {

                    // Add new "Country" to HashMap entry of its respective group
                    if (allGroups.containsKey(country.getGroup())) {
                        allGroups.get(country.getGroup()).add(country);
                    } else {
                        allGroups.put(country.getGroup(), new ArrayList<Country>());
                        allGroups.get(country.getGroup()).add(country);
                    }
                }
                onSetGroups(allGroups);
            }

            @Override
            protected void onPostExecute(List<Match> allMatchesToUpdate) {
                super.onPostExecute(allMatchesToUpdate);

                onUpdateMatchUps(allMatchesToUpdate);

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, allMatches.toArray(new Match[allMatches.size()]));
    }

    private void updateCountries(HashMap<String, List<Country>> allGroups, SparseArray<Match> allMatches) {

        // Iterate over all 32 countries. We gonna make a Country object for each one.
        for (int i = 0; i < countryNameArray.length; i++) {
            ArrayList<Integer> goalsForList = new ArrayList<>();
            ArrayList<Integer> goalsAgainstList = new ArrayList<>();
            ArrayList<String> opponentList = new ArrayList<>();
            // Iterate over all Group-Stage matches.
            for (Match match : getGroupStageMatches(allMatches)) {
                // Add to list the matches that "Country" has disputed, including the goals scored,
                // the goals conceded and the opponent. This last one is for Tie-Breaking purposes only
                if (match.getHomeTeam().equals(countryNameArray[i])) {
                    goalsForList.add(match.getHomeTeamGoals());
                    goalsAgainstList.add(match.getAwayTeamGoals());
                    opponentList.add(match.getAwayTeam());
                }
                else if (match.getAwayTeam().equals(countryNameArray[i])) {
                    goalsForList.add(match.getAwayTeamGoals());
                    goalsAgainstList.add(match.getHomeTeamGoals());
                    opponentList.add(match.getHomeTeam());
                }
            }
            Country t = new Country(
                    countryNameArray[i],
                    countryGroupArray[i],
                    countryCoefficientArray[i],
                    goalsForList,
                    goalsAgainstList,
                    opponentList
            );

            // Add new "Country" to HashMap entry of its respective group
            if (allGroups.containsKey(t.getGroup())) {
                allGroups.get(t.getGroup()).add(t);
            } else {
                allGroups.put(t.getGroup(), new ArrayList<Country>());
                allGroups.get(t.getGroup()).add(t);
            }
        }
    }

    private List<Match> updateKnockOutMatchUpWhenAllGroupStageMatchesHaveBeenPlayed(
            HashMap<String, List<Country>> allGroups,
            SparseArray<Match> allMatches) {

        ListContainer<Match> matchListContainer = new ListContainer<>();
        // Check if all countries in each group have played 3 (all) mGroup mStage matches.
        // If not, return
        for (HashMap.Entry<String, List<Country>> entry : allGroups.entrySet())
            for (Country country : entry.getValue())
                if (country.getMatchesPlayed() != 3) {
                    // Do not compute third-tied tiebreaker. Set to default!
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), "3rd Place C, D or E", "AWAY"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), "3rd Place A, C or D", "AWAY"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), "3rd Place A, B or F", "AWAY"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), "3rd Place B, E or F", "AWAY"));
                    return matchListContainer.getList();
                }

        // Create 6-country group of all third place teams
        ArrayList<Country> thirdPlaceGroup = new ArrayList<>();
        thirdPlaceGroup.add(allGroups.get("A").get(2));
        thirdPlaceGroup.add(allGroups.get("B").get(2));
        thirdPlaceGroup.add(allGroups.get("C").get(2));
        thirdPlaceGroup.add(allGroups.get("D").get(2));
        thirdPlaceGroup.add(allGroups.get("E").get(2));
        thirdPlaceGroup.add(allGroups.get("F").get(2));

        // Sort 6-countries group
        Collections.sort(thirdPlaceGroup, Collections.<Country>reverseOrder());

        // Add 4 best third-place teams to a new ArrayList a sort it by Group
        ArrayList<Country> bestThirdPlaceTeams = new ArrayList<>();
        bestThirdPlaceTeams.add(thirdPlaceGroup.get(0));
        bestThirdPlaceTeams.add(thirdPlaceGroup.get(1));
        bestThirdPlaceTeams.add(thirdPlaceGroup.get(2));
        bestThirdPlaceTeams.add(thirdPlaceGroup.get(3));
        Collections.sort(bestThirdPlaceTeams, new Comparator<Country>() {
            @Override
            public int compare(Country lhs, Country rhs) {
                return lhs.getGroup().toCharArray()[0] - rhs.getGroup().toCharArray()[0];
            }
        });
        ArrayList<String> bestThirdPlaceGroups = new ArrayList<>();
        bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(0).getGroup());
        bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(1).getGroup());
        bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(2).getGroup());
        bestThirdPlaceGroups.add(bestThirdPlaceTeams.get(3).getGroup());

        //for (Country country : bestThirdPlaceTeams) Log.e("3RD PLACE GROUP 3", country.toString());

        if (bestThirdPlaceGroups.contains("A")){ //A - - -
            if (bestThirdPlaceGroups.contains("C") && bestThirdPlaceGroups.contains("D")) { //A-C-D-
                if (bestThirdPlaceGroups.contains("B")) { //A-B-C-D
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WA vs 3C
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WB vs 3D
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WC vs 3A
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WD vs 3B
                }
                else { //A-C-D-*(E/F)
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WA vs 3C
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WB vs 3D
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WC vs 3A
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WD vs 3(E/F)
                }
            }
            else { // A-B-*(C/D/E)-*(E/F)
                if (bestThirdPlaceGroups.contains("B")) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WA vs 3(C-D-E)
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WB vs 3A
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WC vs 3B
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WD vs 3(E-F) (always a group later than vs WA)
                }
                else { // A-*(C/D)-E-F
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WA vs 3(C-D)
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WB vs 3A
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WC vs 3E
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WD vs 3F
                }
            }
        }
        else if (bestThirdPlaceGroups.contains("B")) {
            if (bestThirdPlaceGroups.contains("C") && bestThirdPlaceGroups.contains("D")) { //B-C-D-*(E/F)
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WA vs 3C
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WB vs 3D
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WC vs 3B
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WD vs 3(E-F)
            }
            else {//B-*(C/D)-E-F
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WA vs 3E
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WB vs 3(C-D)
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WC vs 3B
                matchListContainer.add(
                        updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WD vs 3F
            }
        } else { //C-D-E-F
            matchListContainer.add(
                    updateMatchUpInCloud(allMatches.get(40), bestThirdPlaceTeams.get(0).getName(), "AWAY")); // WA vs 3C
            matchListContainer.add(
                    updateMatchUpInCloud(allMatches.get(38), bestThirdPlaceTeams.get(1).getName(), "AWAY")); // WB vs 3D
            matchListContainer.add(
                    updateMatchUpInCloud(allMatches.get(41), bestThirdPlaceTeams.get(3).getName(), "AWAY")); // WC vs 3E
            matchListContainer.add(
                    updateMatchUpInCloud(allMatches.get(39), bestThirdPlaceTeams.get(2).getName(), "AWAY")); // WD vs 3F
        }
        return matchListContainer.getList();
    }

    private List<Match> updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(SparseArray<Match> allMatches,
                                                                               List<Country> countryList,
                                                                               String group) {
        // Check if all countries have played 3 matches
        boolean areAllMatchesPlayed = true;
        for (Country c : countryList)
            if (c.getMatchesPlayed() != 3)
                areAllMatchesPlayed = false;

        ListContainer<Match> matchListContainer = new ListContainer<>();
        // Update knockout mStage matches only if necessary
        switch (group) {
            case "A": {
                if (areAllMatchesPlayed) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), countryList.get(0).getName(), "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(37), countryList.get(1).getName(), "HOME"));
                } else {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(40), "Winner Group " + group, "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(37), "Runner-up Group " + group, "HOME"));
                }
                break;
            }
            case "B": {
                if (areAllMatchesPlayed) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), countryList.get(0).getName(), "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(44), countryList.get(1).getName(), "HOME"));
                } else {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(38), "Winner Group " + group, "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(44), "Runner-up Group " + group, "HOME"));
                }
                break;
            }
            case "C": {
                if (areAllMatchesPlayed) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), countryList.get(0).getName(), "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(37), countryList.get(1).getName(), "AWAY"));
                } else {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(41), "Winner Group " + group, "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(37), "Runner-up Group " + group, "AWAY"));
                }
                break;
            }
            case "D": {
                if (areAllMatchesPlayed) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), countryList.get(0).getName(), "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(43), countryList.get(1).getName(), "AWAY"));
                } else {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(39), "Winner Group " + group, "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(43), "Runner-up Group " + group, "AWAY"));
                }
                break;
            }
            case "E": {
                if (areAllMatchesPlayed) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(43), countryList.get(0).getName(), "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(42), countryList.get(1).getName(), "AWAY"));
                } else {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(43), "Winner Group " + group, "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(42), "Runner-up Group " + group, "AWAY"));
                }
                break;
            }
            case "F": {
                if (areAllMatchesPlayed) {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(42), countryList.get(0).getName(), "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(44), countryList.get(1).getName(), "AWAY"));
                } else {
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(42), "Winner Group " + group, "HOME"));
                    matchListContainer.add(
                            updateMatchUpInCloud(allMatches.get(44), "Runner-up Group " + group, "AWAY"));
                }
                break;
            }
        }

        return matchListContainer.getList();
    }

    private List<Match> updateKnockOutMatchUps(SparseArray<Match> allMatches) {

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

    private Match updateMatchUpInCloud(Match match, String teamName, String matchUpPosition) {
        // Check if there is any need to update match-up
        if (matchUpPosition.equals("HOME") && match.getHomeTeam().equals(teamName))
            return null;
        if (matchUpPosition.equals("AWAY") && match.getAwayTeam().equals(teamName))
            return null;

        // Update match-up accordingly
        switch (matchUpPosition) {
            case "HOME":
                match.setHomeTeam(teamName);
                break;
            case "AWAY":
                match.setAwayTeam(teamName);
                break;
            default:
                Log.e(TAG, "MatchUp position (home or away) not recognized");
                return null;
        }

        return match;
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
                teamName = match.getHomeTeam();
            else if (MatchUtils.didAwayTeamWin(match) || MatchUtils.didAwayTeamWinByPenaltyShootout(match))
                teamName = match.getAwayTeam();
            else
                teamName = "Winner Match " + Integer.toString(matchUpNumber);
        }

        Match matchToUpdate = allMatches.get(matchUpToUpdate);


        if (matchUpToUpdatePosition.equals("AWAY")) {
            if (!matchToUpdate.getAwayTeam().equals(teamName)) {
                matchToUpdate.setAwayTeam(teamName);
                return matchToUpdate;
            }
        } else if (matchUpToUpdatePosition.equals("HOME")) {
            if (!matchToUpdate.getHomeTeam().equals(teamName)) {
                matchToUpdate.setHomeTeam(teamName);
                return matchToUpdate;
            }
        }
        return null;
    }

    private List<Country> putCountriesInDatabase(HashMap<String, List<Country>> allGroups) {
        ListContainer<Country> countryListContainer = new ListContainer<>();
        for (Country country : allGroups.get("A"))
            if (!country.equalsInstance(mAllCountries.get(country.getName()))) {
                mAllCountries.get(country.getName()).set(country);
                countryListContainer.add(country);
            }
        for (Country country : allGroups.get("B"))
            if (!country.equalsInstance(mAllCountries.get(country.getName()))) {
                mAllCountries.get(country.getName()).set(country);
                countryListContainer.add(country);
            }
        for (Country country : allGroups.get("C"))
            if (!country.equalsInstance(mAllCountries.get(country.getName()))) {
                mAllCountries.get(country.getName()).set(country);
                countryListContainer.add(country);
            }
        for (Country country : allGroups.get("D"))
            if (!country.equalsInstance(mAllCountries.get(country.getName()))) {
                mAllCountries.get(country.getName()).set(country);
                countryListContainer.add(country);
            }
        for (Country country : allGroups.get("E"))
            if (!country.equalsInstance(mAllCountries.get(country.getName()))) {
                mAllCountries.get(country.getName()).set(country);
                countryListContainer.add(country);
            }
        for (Country country : allGroups.get("F"))
            if (!country.equalsInstance(mAllCountries.get(country.getName()))) {
                mAllCountries.get(country.getName()).set(country);
                countryListContainer.add(country);
            }

        return countryListContainer.getList();
    }

    private List<Country> orderGroup(List<Country> group) {
        // Sort Group
        Collections.sort(group, Collections.<Country>reverseOrder());

        List<Country> sortedGroup = new ArrayList<>();
        List<Country> countriesWithEqualNumberOfPoints = new ArrayList<>();
        countriesWithEqualNumberOfPoints.add(group.get(0));

        for (int i = 1 ; i < 4 ; i++) {
            // The country "i" has equal number of points as the previous country. Store it in the
            // CountriesStillTied List
            if (group.get(i - 1).getPoints() == group.get(i).getPoints()) {
                countriesWithEqualNumberOfPoints.add(group.get(i));
            }
            // The country "i" does not have an equal number of points as the previous country.
            // Add the previous countries that were tied (which were stored in the
            // countriesWithEqualNumberOfPoints List) to the sortedGroup List after applying the
            // Tie-Breaking criteria to those countries; and clear and add country "i" to
            // countriesWithEqualNumberOfPoints List
            else {
                sortedGroup.addAll(computeTieBreaker(countriesWithEqualNumberOfPoints));
                countriesWithEqualNumberOfPoints.clear();
                countriesWithEqualNumberOfPoints.add(group.get(i));
            }
            if (i == 3) { // last iteration
                sortedGroup.addAll(computeTieBreaker(countriesWithEqualNumberOfPoints));
            }
        }

        for (int i = 0 ; i < sortedGroup.size() ; i++)
            sortedGroup.get(i).setPosition(i + 1);

        return sortedGroup;
    }

    private List<Country> computeTieBreaker(List<Country> countriesTiedList) {

        // One Country only. Return it;
        if (countriesTiedList.size() == 1) {
            return countriesTiedList;
        }
        // Two Countries that were tied. Compute tiebreaker between two teams;
        else if (countriesTiedList.size() == 2) {
            // Clone List (It is not necessary)
            List<Country> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Update Stats between the two teams. ie Compute Head to Head match-up (if it
            cloneCountriesTiedList.get(0).updateStatsFilterByCountry(
                    new ArrayList<>(Collections.singleton(cloneCountriesTiedList.get(1).getName())));
            cloneCountriesTiedList.get(1).updateStatsFilterByCountry(
                    new ArrayList<>(Collections.singleton(cloneCountriesTiedList.get(0).getName())));

            // Sort countries
            Collections.sort(cloneCountriesTiedList, Collections.<Country>reverseOrder());

            // Check if, after having applied the 3 following 3 tie-breaking criteria, teams were
            // separated
            // 1. Higher number of points obtained in the matches played between the teams in question;
            // 2. Superior goal difference resulting from the matches played between the teams in question;
            // 3. Higher number of goals scored in the matches played between the teams in question;
            if (!cloneCountriesTiedList.get(0).equalsRanking(cloneCountriesTiedList.get(1))) {
                // if they were separated, it this sorting. just a quick update of GroupStats and return.
                for (Country c : cloneCountriesTiedList)
                    c.updateGroupStats();
                return cloneCountriesTiedList;
            }
            else {
                // if they have an equal ranking, untie them with the following criteria
                // 5. Superior goal difference in all group matches;
                // 6. Higher number of goals scored in all group matches;
                // (7. Penalty shootout between teams if they are playing each other in final fixture)
                //     is not applicable in this app
                // 8. Fair play conduct
                // 9. Position in the UEFA national team coefficient ranking system.

                // update GroupStats, sort them and return.
                for (Country c : cloneCountriesTiedList)
                    c.updateGroupStats();
                Collections.sort(cloneCountriesTiedList, Collections.<Country>reverseOrder());
                return cloneCountriesTiedList;
            }
        }
        else if (countriesTiedList.size() == 3) {
            // Clone List (It is not necessary)
            List<Country> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Update Stats between the three teams
            cloneCountriesTiedList.get(0).updateStatsFilterByCountry(
                    new ArrayList<>(Arrays.asList(
                            cloneCountriesTiedList.get(1).getName(),
                            cloneCountriesTiedList.get(2).getName())));
            cloneCountriesTiedList.get(1).updateStatsFilterByCountry(
                    new ArrayList<>(Arrays.asList(
                            cloneCountriesTiedList.get(0).getName(),
                            cloneCountriesTiedList.get(2).getName())));
            cloneCountriesTiedList.get(2).updateStatsFilterByCountry(
                    new ArrayList<>(Arrays.asList(
                            cloneCountriesTiedList.get(0).getName(),
                            cloneCountriesTiedList.get(1).getName())));

            // Sort countries
            Collections.sort(cloneCountriesTiedList, Collections.<Country>reverseOrder());

            // Initialize two arrayList's. One to store the countries still tied (Equal-Ranking)
            // after applying the tie-breakers criteria above. The other to store the countries
            // according to the tie-breaking criteria.
            List<Country> countriesStillTiedList = new ArrayList<>();
            List<Country> countriesAfterTieBreakerList = new ArrayList<>();

            // Add first country to CountriesStillTied List and iterate the remaining
            countriesStillTiedList.add(cloneCountriesTiedList.get(0));
            for (int i = 1 ; i < 3 ; i++) {
                // The country "i" has equal ranking has the previous country. Store it in the
                // CountriesStillTied List
                if (cloneCountriesTiedList.get(i - 1).equalsRanking(cloneCountriesTiedList.get(i))) {
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
                // The country "i" does not have an equal ranking has the previous country. Add the
                // previous countries that were tied (which were stored in the CountriesStillTied
                // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking
                // criteria to those countries; and clear and add country "i" to CountriesStillTied
                // List
                else {
                    countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                    countriesStillTiedList.clear();
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
            }

            // If all 3 countries are still tied, untie them with the following criteria
            // 5. Superior goal difference in all group matches;
            // 6. Higher number of goals scored in all group matches;
            // (7. Penalty shootout between teams if they are playing each other in final fixture)
            //     is not applicable in this app
            // 8. Fair play conduct
            // 9. Position in the UEFA national team coefficient ranking system.
            if (countriesStillTiedList.size() == 3) {
                for (Country c : countriesStillTiedList)
                    c.updateGroupStats();
                Collections.sort(countriesStillTiedList, Collections.<Country>reverseOrder());
                return countriesStillTiedList;

            }
            // Add the last countries that were tied (which are stored in the CountriesStillTied
            // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking criteria
            // to those countries.
            else {
                countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                for (Country c : countriesAfterTieBreakerList)
                    c.updateGroupStats();
                return countriesAfterTieBreakerList;
            }
        }
        else {
            // Clone List (It is not necessary)
            List<Country> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Initialize two arrayList's. One to store the countries still tied (Equal-Ranking)
            // after applying the tie-breakers criteria above. The other to store the countries
            // according to the tie-breaking criteria.
            List<Country> countriesStillTiedList = new ArrayList<>();
            List<Country> countriesAfterTieBreakerList = new ArrayList<>();

            // Add first country to CountriesStillTied List and iterate the remaining
            countriesStillTiedList.add(cloneCountriesTiedList.get(0));
            for (int i = 1 ; i < 4 ; i++) {
                // The country "i" has equal ranking has the previous country. Store it in the
                // CountriesStillTied List
                if (cloneCountriesTiedList.get(i - 1).equalsRanking(cloneCountriesTiedList.get(i))) {
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
                // The country "i" does not have an equal ranking has the previous country. Add the
                // previous countries that were tied (which were stored in the CountriesStillTied
                // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking
                // criteria to those countries; and clear and add country "i" to CountriesStillTied
                // List
                else {
                    countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                    countriesStillTiedList.clear();
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
            }

            // If all 4 countries are still tied, untie them with the following criteria
            // 5. Superior goal difference in all group matches;
            // 6. Higher number of goals scored in all group matches;
            // (7. Penalty shootout between teams if they are playing each other in final fixture)
            //     is not applicable in this app
            // 8. Fair play conduct
            // 9. Position in the UEFA national team coefficient ranking system.
            if (countriesStillTiedList.size() == 4) {
                for (Country c : countriesStillTiedList)
                    c.updateGroupStats();
                Collections.sort(countriesStillTiedList, Collections.<Country>reverseOrder());
                return countriesStillTiedList;

            }
            // Add the last countries that were tied (which are stored in the CountriesStillTied
            // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking criteria
            // to those countries.
            else {
                countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                for (Country c : countriesAfterTieBreakerList)
                    c.updateGroupStats();
                return countriesAfterTieBreakerList;
            }
        }
    }

    private static List<Match> getGroupStageMatches(SparseArray<Match> allMatches) {
        List<Match> allGroupStageMatches = new ArrayList<>();
        for(int i = 0; i < allMatches.size(); i++) {
            Match match = allMatches.valueAt(i);
            if (match.getStage().equals("Group Stage"))
                allGroupStageMatches.add(match);
        }
        /*for (Match match : allMatches)
            if (match.getStage().equals("Group Stage"))
                allGroupStageMatches.add(match);/**/
        return allGroupStageMatches;
    }

    private void onUpdateMatchUps(List<Match> matchList) {
        mOnProcessingFinished.onUpdateMatchUps(matchList);
    }

    private void onUpdateCountries(List<Country> countryList) {
        mOnProcessingFinished.onUpdateCountries(countryList);
    }

    private void onSetGroups(HashMap<String, List<Country>> allGroups) {
        mOnProcessingFinished.onSetGroups(allGroups);
    }

    public interface OnProcessingFinished {
        void onSetGroups(HashMap<String, List<Country>> allGroups);
        void onUpdateCountries(List<Country> countryList);
        void onUpdateMatchUps(List<Match> matchList);
    }

    private class ListContainer<T> {
        private List<T> mList;

        ListContainer() {
            mList = new ArrayList<>();
        }

        void add(T obj) {
            if (obj != null)
                mList.add(obj);
        }

        public List<T> getList() {
            return mList;
        }
    }

}
