package org.hugoandrade.euro2016.backend.presenter;

import android.content.Context;
import android.util.Log;

import org.hugoandrade.euro2016.backend.MVP;
import org.hugoandrade.euro2016.backend.model.MainModel;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.processing.BackEndProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainPresenter
        extends PresenterBase<MVP.RequiredViewOps,
                              MVP.RequiredPresenterOps,
                              MVP.ProvidedModelOps,
                              MainModel>
        implements MVP.ProvidedPresenterOps,
                   MVP.RequiredPresenterOps,
                   BackEndProcessing.OnProcessingFinished {

    private List<Match> mAllMatches = new ArrayList<>();
    private SystemData mSystemData;
    private BackEndProcessing mBackEndProcessing;

    private boolean hasRetrievedAllCountries = false;
    private boolean hasRetrievedSystemData = false;
    private boolean hasRetrievedAllMatches = false;
    private HashSet<Integer> mMatchNumberUpdating = new HashSet<>();

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
    public void notifyServiceConnectionStatus(boolean isConnected) {
        if (isConnected) {
            retrieveAllCountries();
            retrieveSystemData();
        }

        updateLoadingStatus();
    }

    private void updateLoadingStatus() {

        boolean hasCompletelyLoaded =
                hasRetrievedAllCountries &&
                        hasRetrievedSystemData &&
                        hasRetrievedAllMatches &&
                        mMatchNumberUpdating.isEmpty();

        getView().onLoadingUpdate(hasCompletelyLoaded);
    }

    private void retrieveAllCountries() {
        hasRetrievedAllCountries = false;
        getModel().getAllCountries();
    }

    @Override
    public void onUpdateCountries(List<Country> countryList) {
        for (Country country : countryList)
            getModel().updateCountry(country);
    }

    @Override
    public void reportGetAllCountriesRequestResult(boolean isRetrieved, String message, List<Country> allCountryList) {
        if (message != null)
            getView().reportMessage(message);

        hasRetrievedAllCountries = isRetrieved;

        if (allCountryList != null) {

            mBackEndProcessing = new BackEndProcessing(this, allCountryList);

            retrieveAllMatches();
        }
        updateLoadingStatus();
    }

    private void retrieveAllMatches() {
        hasRetrievedAllMatches = false;
        getModel().getAllMatches();
    }

    @Override
    public void reportGetAllMatchesRequestResult(boolean isRetrieved, String message, List<Match> allMatchesList) {
        if (message != null)
            getView().reportMessage(message);

        hasRetrievedAllMatches = isRetrieved;

        if (allMatchesList != null) {
            mAllMatches.clear();
            mAllMatches.addAll(allMatchesList);
            Collections.sort(mAllMatches);

            getView().setAllMatches(mAllMatches);

            mBackEndProcessing.startUpdateGroupsProcessing(mAllMatches);
        }
        updateLoadingStatus();
    }

    private void retrieveSystemData() {
        hasRetrievedSystemData = false;
        getModel().getSystemData();
    }

    @Override
    public void setSystemData(SystemData systemData) {
        getModel().setSystemData(systemData);
    }

    @Override
    public void reportSystemDataRequestResult(boolean isRetrieved, String message, SystemData systemData) {
        if (message != null)
            getView().reportMessage(message);

        hasRetrievedSystemData = isRetrieved;

        if (systemData != null)
            mSystemData = systemData;

        updateLoadingStatus();
    }

    @Override
    public SystemData getSystemData() {
        return mSystemData;
    }

    @Override
    public void onUpdateMatchUps(List<Match> matchList) {
        for (Match match : matchList) {
            Log.e(TAG, match.toString());
            mMatchNumberUpdating.add(match.getMatchNumber());
            getModel().updateMatchUp(match);
        }

        updateLoadingStatus();
    }

    @Override
    public void updateMatch(Match match) {
        getModel().updateMatch(match);
    }

    @Override
    public void reportUpdateMatchRequestResult(boolean isRetrieved, String message, Match match) {
        if (message != null)
            getView().reportMessage(message);

        if (match != null) {
            mMatchNumberUpdating.remove(match.getMatchNumber());
            for (int i = 0; i < mAllMatches.size() ; i++)
                if (mAllMatches.get(i).getMatchNumber() == match.getMatchNumber()) {
                    mAllMatches.set(i, match);
                    break;
                }

            Collections.sort(mAllMatches);
            getView().setMatch(match);

            mBackEndProcessing.startUpdateGroupsProcessing(mAllMatches);
        }
        updateLoadingStatus();
    }

    @Override
    public void onSetGroups(HashMap<String, List<Country>> allGroups) {
        getView().setGroups(allGroups);
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
