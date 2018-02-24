package org.hugoandrade.euro2016.predictor.presenter;

import android.content.Context;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.Country;
import org.hugoandrade.euro2016.predictor.data.Group;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.model.MainModel;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SGroup;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SStage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainPresenter extends PresenterBase<MVP.RequiredMainViewOps,
                                                     MVP.RequiredMainPresenterOps,
                                                     MVP.ProvidedMainModelOps,
                                                     MainModel>
        implements MVP.ProvidedMainPresenterOps,
                   MVP.RequiredMainPresenterOps {

    /**
     * The List of Matches
     */
    private List<Match> mMatchList = new ArrayList<>();

    /**
     * Hook method called when a new instance of MainPresenter is
     * created. One time initialization code goes here, e.g., storing
     * a WeakReference to the View layer and initializing the Model
     * layer.
     *
     * @param view
     *            A reference to the View layer.
     */
    @Override
    public void onCreate(MVP.RequiredMainViewOps view) {
        // Invoke the special onCreate() method in PresenterBase,
        // passing in the MainModel class to instantiate/manage and
        // "this" to provide MainModel with this MVP.RequiredMainModelOps
        // instance.
        super.onCreate(view, MainModel.class, this);
    }

    @Override
    public void onResume() {
        getModel().registerCallback();
    }

    @Override
    public void onPause() {
        // No-ops
    }

    /**
     * Hook method dispatched by the ActivityBase framework to
     * initialize the MainPresenter object after a runtime
     * configuration change.
     *
     * @param view
     *      The currently active MainPresenter.View.
     */
    @Override
    public void onConfigurationChange(MVP.RequiredMainViewOps view) {
        super.onConfigurationChange(view);
    }

    /**
     * Hook method called to shutdown the Presenter layer.
     *
     * @param isChangingConfiguration
     *        True if a runtime configuration triggered the onDestroy() call.
     */
    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);
    }

    /**
     * Notify the Presenter layer that AzureMobileService is connected
     * so that it can start fetching all the app data.
     */
    @Override
    public void notifyServiceIsBound() {
        getView().disableUI();

        getModel().getInfo(GlobalData.user.getID());
    }

    @Override
    public void onInfoFetched(boolean isOk,
                              String message,
                              List<Country> countryList,
                              List<Match> matchList,
                              List<Prediction> predictionList,
                              List<User> userList) {

        if (isOk) {

            // Sort by score
            Collections.sort(userList, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    return rhs.getScore() - lhs.getScore();
                }
            });

            // Send the list of Users to the UI
            getView().setUserList(userList);

            /* ******************************** */

            // Set countries to each match
            for (Country c : countryList) {
                for (Match match : matchList) {
                    if (match.getHomeTeamID().equals(c.getID()))
                        match.setHomeTeam(c);
                    if (match.getAwayTeamID().equals(c.getID()))
                        match.setAwayTeam(c);
                }
            }

            mMatchList = matchList;

            // group by stage
            HashMap<SStage, List<Match>> mMatchMap = setupMatches(matchList);

            // Send the hash map of countries to the UI
            getView().setMatches(mMatchMap);

            /* ******************************** */

            /*for (Map.Entry<SStage, List<Match>> e : mMatchMap.entrySet()) {
                Log.e(TAG, "------------");
                Log.e(TAG, e.getKey() == null ? "null" : e.getKey().name);
                for (Match m : e.getValue()) {
                    Log.e(TAG, m.toString());
                }
            } /**/

            for (Country c : countryList) {
                for (Match match : mMatchMap.get(SStage.roundOf16)) {
                    if (match.getHomeTeamID().equals(c.getID()))
                        c.setAdvancedGroupStage(true);
                    if (match.getAwayTeamID().equals(c.getID()))
                        c.setAdvancedGroupStage(true);
                }
            }

            // group by group stage
            HashMap<SGroup, Group> mGroupMap = setupGroups(countryList);

            // Send the hash map of countries to the UI
            getView().setGroups(mGroupMap);

            /* ******************************** */

            // Send the list of predictions to the UI
            getView().setPredictions(predictionList);


        } else {
            if (message != null)
                getView().reportMessage(message);
        }

        getView().enableUI();
    }

    @Override
    public void onPredictionUpdated(boolean operationResult, String message, Prediction prediction) {
        if (operationResult) {
            getView().updatePrediction(prediction);
        } else {

            getView().updateFailedPrediction(prediction);

            if (message != null)
                getView().reportMessage(message);
        }

    }

    @Override
    public void onPredictionsFetched(boolean operationResult, String message, User user, List<Prediction> predictionList) {

        if (operationResult) {

            getView().moveToUsersPredictionActivity(user, mMatchList, predictionList);
        } else {

            if (message != null)
                getView().reportMessage(message);
        }
        getView().enableUI();
    }

    /**
     * Refresh all data by fetching the cloud database: SystemData, all Matches,
     * all Countries, all Predictions of the app user and all Users' scores and
     * username. Initiate by fetching SystemData when the user requests to
     * refresh data.
     */
    @Override
    public void refreshAllData() {

        getView().disableUI();

        // Fetch system data
        getModel().getInfo(GlobalData.user.getID());
    }

    /**
     * Initiate the asynchronous update of the provided Predictions when the
     * user presses "Set Prediction" button in the SetPredictionsFragment.
     */
    @Override
    public void putPrediction(Prediction prediction){
        getModel().putPrediction(prediction);
    }

    /**
     * Disable the View layer, initiate the asynchronous Predictions lookup
     * of the selected user and, once all Predictions are fetched, start new
     * activity displaying all Predictions of Matches prior to server time.
     */
    @Override
    public void getPredictionsOfSelectedUser(User user) {

        // Disable User input
        getView().disableUI();

        // Fetch all his/her predictions that had been closed.
        getModel().getPredictions(user);
    }

    /**
     * Utility method to group countries according to the group stage.
     *
     * @param countryList List of countries.
     *
     * @return HashMap of the countries grouped together according to group stage
     */
    private HashMap<SGroup, Group> setupGroups(List<Country> countryList) {
        // Set groups
        HashMap<SGroup, Group> groupsMap = new HashMap<>();
        for (Country c : countryList) {
            SGroup group = SGroup.get(c.getGroup());

            if (groupsMap.containsKey(group)) {
                groupsMap.get(group).add(c);
            } else {
                groupsMap.put(group, new Group(group == null? null : group.name));
                groupsMap.get(group).add(c);
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

    /**
     * Utility method to group matches according to stage.
     *
     * @param matchList List of matches.
     *
     * @return HashMap of the matches grouped together according to stage
     */
    private HashMap<SStage, List<Match>> setupMatches(List<Match> matchList) {
        // Set groups
        HashMap<SStage, List<Match>> matchesMap = new HashMap<>();
        for (Match m : matchList) {
            SStage stage = SStage.get(m.getStage());

            if (matchesMap.containsKey(stage)) {
                matchesMap.get(stage).add(m);
            } else {
                matchesMap.put(stage, new ArrayList<Match>());
                matchesMap.get(stage).add(m);
            }
        }
        for (List<Match> matches : matchesMap.values())
            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match lhs, Match rhs) {
                    return lhs.getMatchNumber() - rhs.getMatchNumber();
                }
            });

        return matchesMap;
    }

    /**
     * Return the Activity context.
     */
    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    /**
     * Return the Application context.
     */
    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }
}
