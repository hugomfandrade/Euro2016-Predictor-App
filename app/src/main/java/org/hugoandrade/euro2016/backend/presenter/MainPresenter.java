package org.hugoandrade.euro2016.backend.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.hugoandrade.euro2016.backend.MVP;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.model.MainModel;

public class MainPresenter
        extends PresenterBase<
                    MVP.RequiredViewOps,
                    MVP.RequiredPresenterOps,
                    MVP.ProvidedModelOps,
                    MainModel>
        implements MVP.ProvidedPresenterOps, MVP.RequiredPresenterOps {

    private static final String TAG = MainPresenter.class.getSimpleName();

    private static final String[] countryNameArray = {
            "France" , "Romania", "Albania", "Switzerland",
            "England" , "Russia", "Wales", "Slovakia",
            "Germany" , "Ukraine", "Poland", "Northern Ireland",
            "Spain" , "Czech Republic", "Turkey", "Croatia",
            "Belgium" , "Italy", "Ireland", "Sweden",
            "Portugal" , "Iceland", "Austria", "Hungary"
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

    private ArrayList<Match> allMatches = new ArrayList<>();
    private HashMap<String, Country> allCountries = new HashMap<>();
    private HashMap<String, ArrayList<Country> > allGroups = new HashMap<>();

    @Override
    public void onCreate(MVP.RequiredViewOps view) {
        super.onCreate(MainModel.class, view, this);
    }

    @Override
    public void onConfigurationChange(MVP.RequiredViewOps view) { }

    @Override
    public void onResume() { }

    @Override
    public void onPause() { }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);
    }

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }

    private void getAllCountries() {
        getModel().getAllCountries();
    }

    private void getAllMatches() {
        getModel().getAllMatches();
    }

    @Override
    public void updateMatch(Match match) {
        getModel().updateMatch(match);
    }

    @Override
    public void notifyServiceConnectionStatus(boolean isConnected) {
        if (isConnected) {
            getAllCountries();
        }
    }

    @Override
    public void reportGetAllCountriesRequestResult(String message, ArrayList<Country> allCountriesList) {
        if (message != null)
            getView().reportMessage(message);
        if (allCountriesList != null) {
            allCountries.clear();
            for (Country c : allCountriesList)
                allCountries.put(c.getName(), c);

            getAllMatches();
        }
    }

    @Override
    public void reportGetAllMatchesRequestResult(String message, ArrayList<Match> allMatchesList) {
        if (message != null)
            getView().reportMessage(message);
        if (allMatchesList != null) {
            allMatches.clear();
            allMatches.addAll(allMatchesList);
            Collections.sort(allMatches);

            getView().setAllMatches(allMatchesList);
            updateAllGroups();
        }
    }


    @Override
    public void reportUpdateMatchRequestResult(String message, Match match) {
        if (message != null)
            getView().reportMessage(message);
        if (match != null) {
            for (int i = 0 ; i < allMatches.size() ; i++)
                if (allMatches.get(i).getMatchNumber() == match.getMatchNumber()) {
                    allMatches.set(i, match);
                    break;
                }

            Collections.sort(allMatches);
            getView().setMatch(match);
            updateAllGroups();
        }
    }

    private void updateAllGroups() {
        // Do processing asynchronously
        new AsyncTask<Void, Void, HashMap<String, ArrayList<Country>> >() {

            @Override
            protected HashMap<String, ArrayList<Country>>  doInBackground(Void... params) {

                // Create List of countries object, and group them by Group
                updateCountries();

                // Order each group and put on same Container
                allGroups.put("A", orderGroup(allGroups.get("A")));
                allGroups.put("B", orderGroup(allGroups.get("B")));
                allGroups.put("C", orderGroup(allGroups.get("C")));
                allGroups.put("D", orderGroup(allGroups.get("D")));
                allGroups.put("E", orderGroup(allGroups.get("E")));
                allGroups.put("F", orderGroup(allGroups.get("F")));

                // Put each country in database so that the users can retrieve the group information
                putCountriesInDatabase(allGroups);

                // Check if all matches of each group have been played. If yes, update the matches
                // of the knockout mStage appropriately
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allGroups.get("A"), "A");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allGroups.get("B"), "B");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allGroups.get("C"), "C");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allGroups.get("D"), "D");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allGroups.get("E"), "E");
                updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(allGroups.get("F"), "F");

                // Check if all matches of group mStage have been played. If yes, compute the third-
                // tied team and update the matches of the knockout mStage appropriately
                updateKnockOutMatchUpWhenAllGroupStageMatchesHaveBeenPlayed(allGroups);

                return allGroups;
            }


            @Override
            protected void onPostExecute(HashMap<String, ArrayList<Country>>  allGroups) {
                super.onPostExecute(allGroups);

                getView().setGroups(allGroups);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateCountries() {
        if (allGroups == null)
            allGroups = new HashMap<>();
        //if (allCountries == null) allCountries = new HashMap<>();

        allGroups.clear();
        //allCountries.clear();

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

            // Add new "Country" to allCountries List
            // allCountries.put(countryNameArray[i], t);

            // Add new "Country" to HashMap entry of its respective group
            if (allGroups.containsKey(t.getGroup())) {
                allGroups.get(t.getGroup()).add(t);
            } else {
                allGroups.put(t.getGroup(), new ArrayList<Country>());
                allGroups.get(t.getGroup()).add(t);
            }
        }
    }

    private void updateKnockOutMatchUpWhenAllGroupStageMatchesHaveBeenPlayed(HashMap<String, ArrayList<Country>> allGroups) {
        // Check if all countries in each group have played 3 (all) mGroup mStage matches.
        // If not, return
        for (HashMap.Entry<String, ArrayList<Country>> entry : allGroups.entrySet())
            for (Country country : entry.getValue())
                if (country.getMatchesPlayed() != 3) {
                    // Do not compute third-tied tiebreaker. Set to default!
                    updateMatchUpInCloud(allMatches.get(40 - 1), "3rd Place C, D or E", "AWAY");
                    updateMatchUpInCloud(allMatches.get(38 - 1), "3rd Place A, C or D", "AWAY");
                    updateMatchUpInCloud(allMatches.get(41 - 1), "3rd Place A, B or F", "AWAY");
                    updateMatchUpInCloud(allMatches.get(39 - 1), "3rd Place B, E or F", "AWAY");
                    return;
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
                    updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WA vs 3C
                    updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WB vs 3D
                    updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WC vs 3A
                    updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WD vs 3B
                }
                else { //A-C-D-*(E/F)
                    updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WA vs 3C
                    updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WB vs 3D
                    updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WC vs 3A
                    updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WD vs 3(E/F)
                }
            }
            else { // A-B-*(C/D/E)-*(E/F)
                if (bestThirdPlaceGroups.contains("B")) {
                    updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WA vs 3(C-D-E)
                    updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WB vs 3A
                    updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WC vs 3B
                    updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WD vs 3(E-F) (always a group later than vs WA)
                }
                else { // A-*(C/D)-E-F
                    updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WA vs 3(C-D)
                    updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WB vs 3A
                    updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WC vs 3E
                    updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WD vs 3F
                }
            }
        }
        else if (bestThirdPlaceGroups.contains("B")) {
            if (bestThirdPlaceGroups.contains("C") && bestThirdPlaceGroups.contains("D")) { //B-C-D-*(E/F)
                updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WA vs 3C
                updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WB vs 3D
                updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WC vs 3B
                updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WD vs 3(E-F)
            }
            else {//B-*(C/D)-E-F
                updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WA vs 3E
                updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WB vs 3(C-D)
                updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WC vs 3B
                updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WD vs 3F
            }
        } else { //C-D-E-F
            updateMatchUpInCloud(allMatches.get(40 - 1), bestThirdPlaceTeams.get(0).getName(), "AWAY"); // WA vs 3C
            updateMatchUpInCloud(allMatches.get(38 - 1), bestThirdPlaceTeams.get(1).getName(), "AWAY"); // WB vs 3D
            updateMatchUpInCloud(allMatches.get(41 - 1), bestThirdPlaceTeams.get(3).getName(), "AWAY"); // WC vs 3E
            updateMatchUpInCloud(allMatches.get(39 - 1), bestThirdPlaceTeams.get(2).getName(), "AWAY"); // WD vs 3F
        }
    }

    private void updateKnockOutMatchUpWhenAllGroupMatchesHaveBeenPlayed(ArrayList<Country> countries,
                                                                        String group) {
        // Check if all countries have played 3 matches
        boolean areAllMatchesPlayed = true;
        for (Country c : countries)
            if (c.getMatchesPlayed() != 3)
                areAllMatchesPlayed = false;

        // Update knockout mStage matches only if necessary
        switch (group) {
            case "A": {
                if (areAllMatchesPlayed) {
                    updateMatchUpInCloud(allMatches.get(40 - 1), countries.get(0).getName(), "HOME");
                    updateMatchUpInCloud(allMatches.get(37 - 1), countries.get(1).getName(), "HOME");
                } else {
                    updateMatchUpInCloud(allMatches.get(40 - 1), "Winner Group " + group, "HOME");
                    updateMatchUpInCloud(allMatches.get(37 - 1), "Runner-up Group " + group, "HOME");
                }
                break;
            }
            case "B": {
                if (areAllMatchesPlayed) {
                    updateMatchUpInCloud(allMatches.get(38 - 1), countries.get(0).getName(), "HOME");
                    updateMatchUpInCloud(allMatches.get(44 - 1), countries.get(1).getName(), "HOME");
                } else {
                    updateMatchUpInCloud(allMatches.get(38 - 1), "Winner Group " + group, "HOME");
                    updateMatchUpInCloud(allMatches.get(44 - 1), "Runner-up Group " + group, "HOME");
                }
                break;
            }
            case "C": {
                if (areAllMatchesPlayed) {
                    updateMatchUpInCloud(allMatches.get(41 - 1), countries.get(0).getName(), "HOME");
                    updateMatchUpInCloud(allMatches.get(37 - 1), countries.get(1).getName(), "AWAY");
                } else {
                    updateMatchUpInCloud(allMatches.get(41 - 1), "Winner Group " + group, "HOME");
                    updateMatchUpInCloud(allMatches.get(37 - 1), "Runner-up Group " + group, "AWAY");
                }
                break;
            }
            case "D": {
                if (areAllMatchesPlayed) {
                    updateMatchUpInCloud(allMatches.get(39 - 1), countries.get(0).getName(), "HOME");
                    updateMatchUpInCloud(allMatches.get(43 - 1), countries.get(1).getName(), "AWAY");
                } else {
                    updateMatchUpInCloud(allMatches.get(39 - 1), "Winner Group " + group, "HOME");
                    updateMatchUpInCloud(allMatches.get(43 - 1), "Runner-up Group " + group, "AWAY");
                }
                break;
            }
            case "E": {
                if (areAllMatchesPlayed) {
                    updateMatchUpInCloud(allMatches.get(43 - 1), countries.get(0).getName(), "HOME");
                    updateMatchUpInCloud(allMatches.get(42 - 1), countries.get(1).getName(), "AWAY");
                } else {
                    updateMatchUpInCloud(allMatches.get(43 - 1), "Winner Group " + group, "HOME");
                    updateMatchUpInCloud(allMatches.get(42 - 1), "Runner-up Group " + group, "AWAY");
                }
                break;
            }
            case "F": {
                if (areAllMatchesPlayed) {
                    updateMatchUpInCloud(allMatches.get(42 - 1), countries.get(0).getName(), "HOME");
                    updateMatchUpInCloud(allMatches.get(44 - 1), countries.get(1).getName(), "AWAY");
                } else {
                    updateMatchUpInCloud(allMatches.get(42 - 1), "Winner Group " + group, "HOME");
                    updateMatchUpInCloud(allMatches.get(44 - 1), "Runner-up Group " + group, "AWAY");
                }
                break;
            }
        }
    }

    private void putCountriesInDatabase(HashMap<String, ArrayList<Country>> allGroups) {
        for (Country country : allGroups.get("A"))
            if (!country.equalsInstance(allCountries.get(country.getName()))) {
                allCountries.get(country.getName()).set(country);
                getModel().updateCountry(allCountries.get(country.getName()));
            }
        for (Country country : allGroups.get("B"))
            if (!country.equalsInstance(allCountries.get(country.getName()))) {
                allCountries.get(country.getName()).set(country);
                getModel().updateCountry(allCountries.get(country.getName()));
            }
        for (Country country : allGroups.get("C"))
            if (!country.equalsInstance(allCountries.get(country.getName()))) {
                allCountries.get(country.getName()).set(country);
                getModel().updateCountry(allCountries.get(country.getName()));
            }
        for (Country country : allGroups.get("D"))
            if (!country.equalsInstance(allCountries.get(country.getName()))) {
                allCountries.get(country.getName()).set(country);
                getModel().updateCountry(allCountries.get(country.getName()));
            }
        for (Country country : allGroups.get("E"))
            if (!country.equalsInstance(allCountries.get(country.getName()))) {
                allCountries.get(country.getName()).set(country);
                getModel().updateCountry(allCountries.get(country.getName()));
            }
        for (Country country : allGroups.get("F"))
            if (!country.equalsInstance(allCountries.get(country.getName()))) {
                allCountries.get(country.getName()).set(country);
                getModel().updateCountry(allCountries.get(country.getName()));
            }
    }

    private void updateMatchUpInCloud(Match match, String country, String matchUpPosition) {
        // Check if there is any need to update match-up
        if (matchUpPosition.equals("HOME") && match.getHomeTeam().equals(country))
            return;
        if (matchUpPosition.equals("AWAY") && match.getAwayTeam().equals(country))
            return;

        // Update match-up accordingly
        switch (matchUpPosition) {
            case "HOME":
                match.setHomeTeam(country);
                break;
            case "AWAY":
                match.setAwayTeam(country);
                break;
            default:
                Log.e(TAG, "MatchUp position (home or away) not recognized");
                break;
        }

        getModel().updateMatchUp(match);//, country, matchUpPosition);
    }

    private ArrayList<Country> orderGroup(ArrayList<Country> group) {
        // Sort Group
        Collections.sort(group, Collections.<Country>reverseOrder());

        ArrayList<Country> sortedGroup = new ArrayList<>();
        ArrayList<Country> countriesWithEqualNumberOfPoints = new ArrayList<>();
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

    private ArrayList<Country> computeTieBreaker(ArrayList<Country> countriesTiedList) {

        // One Country only. Return it;
        if (countriesTiedList.size() == 1) {
            return countriesTiedList;
        }
        // Two Countries that were tied. Compute tiebreaker between two teams;
        else if (countriesTiedList.size() == 2) {
            // Clone List (It is not necessary)
            ArrayList<Country> cloneCountriesTiedList = new ArrayList<>();
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
            ArrayList<Country> cloneCountriesTiedList = new ArrayList<>();
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
            ArrayList<Country> countriesStillTiedList = new ArrayList<>();
            ArrayList<Country> countriesAfterTieBreakerList = new ArrayList<>();

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
            ArrayList<Country> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Initialize two arrayList's. One to store the countries still tied (Equal-Ranking)
            // after applying the tie-breakers criteria above. The other to store the countries
            // according to the tie-breaking criteria.
            ArrayList<Country> countriesStillTiedList = new ArrayList<>();
            ArrayList<Country> countriesAfterTieBreakerList = new ArrayList<>();

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

    private static ArrayList<Match> getGroupStageMatches(ArrayList<Match> allMatches) {
        ArrayList<Match> allGroupStageMatches = new ArrayList<>();
        for (Match match : allMatches)
            if (match.getStage().equals("Group Stage"))
                allGroupStageMatches.add(match);
        return allGroupStageMatches;
    }
}
