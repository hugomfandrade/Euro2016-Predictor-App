package org.hugoandrade.euro2016.predictor.presenter;

import android.os.RemoteException;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.utils.ErrorMessageUtils;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.NetworkUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class LeagueDetailsPresenter extends MobileClientPresenterBase<MVP.RequiredLeagueDetailsViewOps>

        implements MVP.ProvidedLeagueDetailsPresenterOps {

    @Override
    public void onCreate(MVP.RequiredLeagueDetailsViewOps view) {

        // Invoke the special onCreate() method in PresenterBase,
        // passing in the ImageModel class to instantiate/manage and
        // "this" to provide ImageModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(view);
    }

    @Override
    public void notifyServiceIsBound() {

    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);
    }

    @Override
    public void sendResults(MobileClientData data) {

        int operationType = data.getOperationType();
        boolean isOperationSuccessful
                = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

        if (operationType == MobileClientData.OperationType.DELETE_LEAGUE.ordinal()) {
            onLeagueDeleted(isOperationSuccessful,
                    data.getErrorMessage());
        }
        else if (operationType == MobileClientData.OperationType.LEAVE_LEAGUE.ordinal()) {
            onLeagueLeft(isOperationSuccessful,
                    data.getErrorMessage());
        }
        else if (operationType == MobileClientData.OperationType.GET_PREDICTIONS.ordinal()) {
            onPredictionsFetched(isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getUser(),
                    data.getPredictionList());
        }
        else if (operationType == MobileClientData.OperationType.FETCH_MORE_USERS.ordinal()) {
            onUsersFetched(isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getString(),
                    data.getLeagueUserList());
        }
    }

    @Override
    public void fetchRemainingPredictions(User user) {

        int to = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                GlobalData.getInstance().getMatchList(),
                GlobalData.getInstance().getServerTime().getTime());
        to = to == 0? 0 : to - 1;

        if (to == 0) {
            getView().startUserPredictionsActivity(user, new ArrayList<Prediction>());
        }
        else {
            List<Prediction> predictionList = GlobalData.getInstance().getPredictionsOfUser(user.getID());

            if (predictionList.size() == to) {
                getView().startUserPredictionsActivity(user, predictionList);
            }
            else {
                getPredictionsOfSelectedUser(user);
            }
        }

    }

    @Override
    public void fetchMoreUsers(String leagueID, int numberOfMembers) {

        getView().disableUI();

        if (getMobileClientService() == null) {
            onUsersFetched(false, ErrorMessageUtils.genNotBoundMessage(), leagueID, null);
            return;
        }

        try {
            getMobileClientService().fetchMoreUsers(leagueID, numberOfMembers, 10);
        } catch (RemoteException e) {
            e.printStackTrace();
            onUsersFetched(false, "Error sending message", leagueID, null);
        }
    }

    @Override
    public void logout() {
        if (getMobileClientService() == null) {
            return;
        }

        try {
            getMobileClientService().logout();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteLeague(String userID, String leagueID) {

        getView().disableUI();

        if (getMobileClientService() == null) {
            onLeagueDeleted(false, ErrorMessageUtils.genNotBoundMessage());
            return;
        }

        try {
            getMobileClientService().deleteLeague(userID, leagueID);
        } catch (RemoteException e) {
            e.printStackTrace();
            onLeagueDeleted(false, "Error sending message");
        }
    }

    @Override
    public void leaveLeague(String userID, String leagueID) {

        getView().disableUI();

        if (getMobileClientService() == null) {
            onLeagueLeft(false, ErrorMessageUtils.genNotBoundMessage());
            return;
        }

        try {
            getMobileClientService().leaveLeague(userID, leagueID);
        } catch (RemoteException e) {
            e.printStackTrace();
            onLeagueLeft(false, "Error sending message");
        }
    }

    private void getPredictionsOfSelectedUser(User user) {

        getView().disableUI();

        if (getMobileClientService() == null) {
            onPredictionsFetched(false, ErrorMessageUtils.genNotBoundMessage(), user, null);
            return;
        }

        try {
            getMobileClientService().getPredictions(user);
        } catch (RemoteException e) {
            e.printStackTrace();
            onPredictionsFetched(false, "Error sending message", user, null);
        }
    }

    private void onPredictionsFetched(boolean isOk, String errorMessage, User user, List<Prediction> predictionList) {
        if (isOk) {

            int from = 1;
            int to = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                    GlobalData.getInstance().getMatchList(),
                    GlobalData.getInstance().getServerTime().getTime());
            to = to == 0? 0 : to - 1;

            for (int matchNumber = from ; matchNumber <= to ; matchNumber++) {
                GlobalData.getInstance().setPredictionsOfUser(matchNumber, user, predictionList);
            }

            getView().startUserPredictionsActivity(user, GlobalData.getInstance().getPredictionsOfUser(user.getID()));
        }
        else {
            showErrorMessage(errorMessage);
        }

        getView().enableUI();
    }

    private void onLeagueDeleted(boolean isOk, String errorMessage) {
        if (isOk) {
            getView().leagueLeft();
        }
        else {
            showErrorMessage(errorMessage);
        }

        getView().enableUI();
    }

    private void onLeagueLeft(boolean isOk, String errorMessage) {
        if (isOk) {
            getView().leagueLeft();
        }
        else {
            showErrorMessage(errorMessage);
        }

        getView().enableUI();
    }

    private void onUsersFetched(boolean isOk, String errorMessage, String leagueID, List<LeagueUser> userList) {
        if (isOk) {
            GlobalData.getInstance().addUsersToLeague(leagueID, userList);

            getView().updateListOfUsers(userList);
            //getView().leagueLeft();
        }
        else {
            showErrorMessage(errorMessage);
        }

        getView().enableUI();
    }

    private void showErrorMessage(String message) {
        if (NetworkUtils.isNetworkUnavailableError(getActivityContext(), message)) {
            ViewUtils.showToast(getActivityContext(), message);
            return;
        }
        // operation failed, show error message
        if (message != null)
            getView().reportMessage(message);
    }
}
