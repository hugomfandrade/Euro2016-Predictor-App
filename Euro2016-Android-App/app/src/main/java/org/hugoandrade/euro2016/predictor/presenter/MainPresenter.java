package org.hugoandrade.euro2016.predictor.presenter;

import android.content.Context;
import android.os.RemoteException;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SStage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainPresenter extends MobileClientPresenterBase<MVP.RequiredMainViewOps>

        implements MVP.ProvidedMainPresenterOps {

    private ServiceManager mServiceManager;

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
        mServiceManager = new ServiceManager(getModel().getService());
        getView().notifyServiceIsBound();
        getView().disableUI();

        getInfo(GlobalData.getInstance().user.getID());
    }

    public void onInfoFetched(boolean isOk, String message) {
        onInfoFetched(isOk, message, null, null, null, null);
    }

    public void onInfoFetched(boolean isOk,
                              String message,
                              List<Country> countryList,
                              List<Match> matchList,
                              List<Prediction> predictionList,
                              List<LeagueWrapper> leagueWrapperList) {

        if (isOk) {

            // Set countries to each match
            for (Country c : countryList) {
                for (Match match : matchList) {
                    if (match.getHomeTeamID().equals(c.getID()))
                        match.setHomeTeam(c);
                    if (match.getAwayTeamID().equals(c.getID()))
                        match.setAwayTeam(c);
                }
            }

            GlobalData.getInstance().setMatchList(matchList);

            // group by stage
            HashMap<SStage, List<Match>> mMatchMap = setupMatches(matchList);

            for (Country c : countryList) {
                for (Match match : mMatchMap.get(SStage.roundOf16)) {
                    if (match.getHomeTeamID().equals(c.getID()))
                        c.setAdvancedGroupStage(true);
                    if (match.getAwayTeamID().equals(c.getID()))
                        c.setAdvancedGroupStage(true);
                }
            }

            GlobalData.getInstance().setCountryList(countryList);

            /* ******************************** */


            // Send the list of predictions to the UI
            GlobalData.getInstance().setPredictionList(predictionList);

            Collections.sort(leagueWrapperList, new Comparator<LeagueWrapper>() {
                @Override
                public int compare(LeagueWrapper o1, LeagueWrapper o2) {

                    if (o1.getLeague() != null && o1.getLeague().getID().equals(LeagueWrapper.OVERALL_ID)) {
                        return 1;
                    }
                    if (o2.getLeague() != null && o2.getLeague().getID().equals(LeagueWrapper.OVERALL_ID)) {
                        return -1;
                    }
                    return 0;
                }
            });
            GlobalData.getInstance().setLeagues(leagueWrapperList);


            Date serverTime = GlobalData.getInstance().getServerTime().getTime();

            int to = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(matchList, serverTime);
            to = to == 0? 0 : to - 1;

            int from = (to < 4) ? 0 : to - 4;

            // TODO getLatestPerformanceOfUsers(userList, from, to);

        } else {
            getView().reportMessage(message);
        }

        getView().enableUI();
    }

    public void onLatestPerformanceFetched(boolean operationResult,
                                           String message,
                                           List<User> userList,
                                           List<Prediction> predictionList) {

        if (operationResult) {

            int to = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                    GlobalData.getInstance().getMatchList(),
                    GlobalData.getInstance().getServerTime().getTime());
            to = to == 0? 0 : to - 1;

            int from = (to < 4) ? 0 : to - 4;

            for (int matchNumber = from ; matchNumber <= to ; matchNumber++) {
                GlobalData.getInstance().setPredictionsOfUsers(matchNumber, userList, predictionList);
            }

            GlobalData.getInstance().setLatestPerformanceOfUsers(predictionList);

        } else {

            if (message != null)
                getView().reportMessage(message);
        }
    }

    @Override
    public ServiceManager getServiceManager() {
        return mServiceManager;
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

    public void getInfo(String userID) {
        if (getMobileClientService() == null) {
            onInfoFetched(false, "Not bound to the service");
            return;
        }

        try {
            getMobileClientService().getInfo(userID);
        } catch (RemoteException e) {
            e.printStackTrace();
            onInfoFetched(false, "Error sending message");
        }
    }

    public void getLatestPerformanceOfUsers(List<User> userList, int firstMatchNumber, int lastMatchNumber) {
        if (getMobileClientService() == null) {
            onLatestPerformanceFetched(false, "Not bound to the service", null, null);
            return;
        }

        try {
            getMobileClientService().getLatestPerformanceOfUsers(userList, firstMatchNumber, lastMatchNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
            onLatestPerformanceFetched(false, "Error sending message", null, null);
        }
    }

    @Override
    public void sendResults(MobileClientData data) {
        mServiceManager.sendResults(data);
        int operationType = data.getOperationType();
        boolean isOperationSuccessful
                = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

        if (operationType == MobileClientData.OperationType.GET_INFO.ordinal()) {
            onInfoFetched(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getCountryList(),
                    data.getMatchList(),
                    data.getPredictionList(),
                    data.getLeagueWrapperList());
        }
        else if (operationType == MobileClientData.OperationType.GET_LATEST_PERFORMANCE.ordinal()) {
            onLatestPerformanceFetched(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getUserList(),
                    data.getPredictionList());
        }
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
