package org.hugoandrade.euro2016.predictor.admin.presenter;

import android.content.Context;

import org.hugoandrade.euro2016.predictor.admin.GlobalData;
import org.hugoandrade.euro2016.predictor.admin.MVP;
import org.hugoandrade.euro2016.predictor.admin.model.MainModel;
import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Group;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.processing.BackEndProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainPresenter
        extends PresenterBase<MVP.RequiredViewOps,
                              MVP.RequiredPresenterOps,
                              MVP.ProvidedModelOps,
                              MainModel>
        implements MVP.ProvidedPresenterOps,
                   MVP.RequiredPresenterOps,
                   BackEndProcessing.OnProcessingFinished {

    private List<Match> mMatchList = new ArrayList<>();
    private HashMap<String, Group> mGroupMap = new HashMap<>();

    private BackEndProcessing mBackEndProcessing;

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
    public void notifyServiceIsBound() {
        getInfo();
    }

    private void getInfo() {

        if (getModel().getInfo())
            getView().disableUI();
    }

    @Override
    public void reset() {

        if (getModel().reset())
            getView().disableUI();
    }

    @Override
    public void updateScoresOfPredictions() {

        if (getModel().updateScoresOfPredictions())
            getView().disableUI();
    }

    @Override
    public void updateSystemData(SystemData systemData) {
        getModel().updateSystemData(systemData);
    }

    @Override
    public void setMatch(Match match) {
        if (!getModel().updateMatch(match))
            getView().updateFailedMatch(match);
    }

    @Override
    public void getAllInfoRequestResult(boolean isRetrieved,
                                        String message,
                                        ArrayList<Country> countryList,
                                        ArrayList<Match> matchList) {
        if (isRetrieved) {
            mMatchList = matchList;
            mGroupMap = setupGroups(countryList);

            Collections.sort(mMatchList);

            // Set countries to each match
            for (Country c : countryList) {
                for (Match match : mMatchList) {
                    if (match.getHomeTeamID().equals(c.getID()))
                        match.setHomeTeam(c);
                    if (match.getAwayTeamID().equals(c.getID()))
                        match.setAwayTeam(c);
                }
            }

            // Update UI
            getView().displayMatches(mMatchList);

            // Update UI
            getView().displayGroups(mGroupMap);

            // Start processing countries to each match
            if (mBackEndProcessing != null)
                mBackEndProcessing.cancel();
            mBackEndProcessing = new BackEndProcessing(this, setupCountryList(mGroupMap));
            mBackEndProcessing.startUpdateGroupsProcessing(mMatchList);

        }
        else {
            getView().reportMessage(message);
        }

        getView().enableUI();
    }

    @Override
    public void updateCountryRequestResult(boolean isRetrieved,
                                           String message,
                                           Country country) {
        if (isRetrieved) {

            // Set new country in list and set and update UI of the matches of that country
            for (Group group : mGroupMap.values()) {
                List<Country> countryList = group.getCountryList();
                for (int i = 0; i < countryList.size(); i++) {
                    if (countryList.get(i).getID().equals(country.getID())) {
                        countryList.set(i, country);

                        for (Match match : mMatchList) {
                            if (match.getHomeTeamID().equals(country.getID())) {
                                match.setHomeTeam(country);
                                getView().updateMatch(match);
                            }
                            if (match.getAwayTeamID().equals(country.getID())) {
                                match.setAwayTeam(country);
                                getView().updateMatch(match);
                            }
                        }

                        Collections.sort(countryList, new Comparator<Country>() {
                            @Override
                            public int compare(Country lhs, Country rhs) {
                                return lhs.getPosition() - rhs.getPosition();
                            }
                        });
                        break;
                    }
                }
            }

            // Update UI
            getView().displayGroups(mGroupMap);

        }
        else {
            getView().reportMessage(message);
        }
    }

    @Override
    public void updateMatchRequestResult(boolean isRetrieved,
                                         String message,
                                         Match match) {
        if (isRetrieved) {

            // Set new match in list and set countries of that match
            for (int i = 0; i < mMatchList.size() ; i++)
                if (mMatchList.get(i).getID().equals(match.getID())) {
                    mMatchList.set(i, match);

                    for (Group group : mGroupMap.values()) {
                        for (Country country : group.getCountryList()) {
                            if (match.getHomeTeamID().equals(country.getID())) {
                                match.setHomeTeam(country);
                            }
                            if (match.getAwayTeamID().equals(country.getID())) {
                                match.setAwayTeam(country);
                            }
                        }
                    }
                    break;
                }

            // set in UI
            getView().updateMatch(match);

            // Start processing with updated match list
            if (mBackEndProcessing != null)
                mBackEndProcessing.cancel();
            mBackEndProcessing = new BackEndProcessing(this, setupCountryList(mGroupMap));
            mBackEndProcessing.startUpdateGroupsProcessing(mMatchList);

        } else {
            // set in UI
            getView().updateFailedMatch(match);

            getView().reportMessage(message);
        }
    }

    @Override
    public void updateMatchUpRequestResult(boolean isRetrieved,
                                           String message,
                                           Match match) {
        if (isRetrieved) {

            // Set new match in list and set countries of that match
            for (int i = 0; i < mMatchList.size() ; i++) {
                if (mMatchList.get(i).getID().equals(match.getID())) {
                    mMatchList.set(i, match);

                    for (Group group : mGroupMap.values()) {
                        for (Country country : group.getCountryList()) {
                            if (match.getHomeTeamID().equals(country.getID())) {
                                match.setHomeTeam(country);
                            }
                            if (match.getAwayTeamID().equals(country.getID())) {
                                match.setAwayTeam(country);
                            }
                        }
                    }
                    break;
                }
            }

            // set in UI
            getView().updateMatch(match);

        } else {
            getView().reportMessage(message);
        }
    }

    @Override
    public void updateSystemDataRequestResult(boolean isRetrieved,
                                              String message,
                                              SystemData systemData) {
        if (isRetrieved) {
            GlobalData.setSystemData(systemData);
        }
        else {
            getView().reportMessage(message);
        }
    }

    @Override
    public void updateScoresOfPredictionsRequestResult(boolean isRetrieved, String message) {
        if (!isRetrieved) {
            getView().reportMessage(message);
        }

        getView().enableUI();
    }

    @Override
    public void updateCountry(Country country) {
        getModel().updateCountry(country);
    }

    @Override
    public void updateMatchUp(Match match) {
        getModel().updateMatchUp(match);
    }

    @Override
    public void onProcessingFinished(List<Country> countryList, List<Match> matchList) {
        // No-ops
    }

    private HashMap<String, Group> setupGroups(List<Country> countryList) {
        // Set groups
        HashMap<String, Group> groupsMap = new HashMap<>();
        for (Country c : countryList) {
            if (groupsMap.containsKey(c.getGroup())) {
                groupsMap.get(c.getGroup()).add(c);
            } else {
                groupsMap.put(c.getGroup(), new Group(c.getGroup()));
                groupsMap.get(c.getGroup()).add(c);
            }
        }
        for (Group group : groupsMap.values())

            Collections.sort(group.getCountryList(), new Comparator<Country>() {
                        @Override
                        public int compare(Country lhs, Country rhs) {
                            return lhs.getPosition() - rhs.getPosition();
                        }
                    });

        return groupsMap;
    }

    private List<Country> setupCountryList(HashMap<String, Group> groupsMap) {
        // Set groups
        List<Country> countryList = new ArrayList<>();
        for (Group group : groupsMap.values()) {
            countryList.addAll(group.getCountryList());
        }
        return countryList;
    }

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }

}
