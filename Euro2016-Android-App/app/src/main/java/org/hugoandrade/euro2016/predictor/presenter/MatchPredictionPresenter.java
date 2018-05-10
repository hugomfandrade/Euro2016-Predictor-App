package org.hugoandrade.euro2016.predictor.presenter;

import android.os.RemoteException;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;

import java.util.ArrayList;
import java.util.List;

public class MatchPredictionPresenter extends MobileClientPresenterBase<MVP.RequiredMatchPredictionViewOps>

        implements MVP.ProvidedMatchPredictionPresenterOps {

    @Override
    public void onCreate(MVP.RequiredMatchPredictionViewOps view) {

        // Invoke the special onCreate() method in PresenterBase,
        // passing in the ImageModel class to instantiate/manage and
        // "this" to provide ImageModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(view);

        getView().disableUI();
    }

    @Override
    public void notifyServiceIsBound() {

        int currentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                GlobalData.getInstance().getMatchList(),
                GlobalData.getInstance().getServerTime().getTime()) - 1;

        getPredictions(getView().getUserList(), currentMatchNumber);
    }

    @Override
    public void getPredictions(List<LeagueUser> userList, int matchNumber) {

        // filter users whose predictions need to be fetched
        List<User> uList = new ArrayList<>();
        for (LeagueUser user : userList) {
            if (!GlobalData.getInstance().wasPredictionFetched(user.getUser(), matchNumber)) {
                uList.add(user.getUser());
            }
        }

        if (uList.size() == 0) {
            List<User> t = new ArrayList<>();
            for (LeagueUser u : userList) {
                t.add(u.getUser());
            }
            getView().setMatchPredictionList(matchNumber, t);
            getView().enableUI();
            return;
        }

        getView().disableUI();

        if (getMobileClientService() == null) {
            Log.w(TAG, "Service is still not bound");
            onGettingPredictionsOperationResult(false, "Not bound to the service");
            return;
        }

        try {
            getMobileClientService().getPredictionsOfUsers(uList, matchNumber);
        } catch (RemoteException e) {
            e.printStackTrace();
            onGettingPredictionsOperationResult(false, "Error sending message");
        }
    }

    private void onGettingPredictionsOperationResult(boolean wasOperationSuccessful, String message) {
        onGettingPredictionsOperationResult(wasOperationSuccessful, message, 0, null, null);
    }

    private void onGettingPredictionsOperationResult(boolean wasOperationSuccessful,
                                                     String message,
                                                     int matchNumber,
                                                     List<User> userList,
                                                     List<Prediction> predictionList) {
        getView().enableUI();

        if (wasOperationSuccessful) {

            GlobalData.getInstance().setPredictionsOfUsers(matchNumber, userList, predictionList);

            getView().setMatchPredictionList(matchNumber, userList);
        }
        else {
            getView().reportMessage(message);
        }
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

        if (operationType == MobileClientData.OperationType.GET_PREDICTIONS_OF_USERS.ordinal()) {
            onGettingPredictionsOperationResult(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getInteger(),
                    data.getUserList(),
                    data.getPredictionList());
        }
    }
}
