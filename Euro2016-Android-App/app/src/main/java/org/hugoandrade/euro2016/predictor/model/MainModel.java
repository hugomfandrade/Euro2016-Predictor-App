package org.hugoandrade.euro2016.predictor.model;

import android.os.RemoteException;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

import java.util.List;

public class MainModel extends MobileClientModelBase<MVP.RequiredMainPresenterOps>

        implements MVP.ProvidedMainModelOps {

    @Override
    public void onCreate(MVP.RequiredMainPresenterOps presenter) {

        super.onCreate(presenter);
    }

    @Override
    public void onDestroy(boolean isChangingConfigurations) {
        super.onDestroy(isChangingConfigurations);
    }

    @Override
    public void sendResults(MobileClientData data) {
        int operationType = data.getOperationType();
        boolean isOperationSuccessful
                = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

        if (operationType == MobileClientData.OperationType.GET_INFO.ordinal()) {
            getPresenter().onInfoFetched(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getCountryList(),
                    data.getMatchList(),
                    data.getPredictionList(),
                    data.getUserList());
        }
        else if (operationType == MobileClientData.OperationType.PUT_PREDICTION.ordinal()) {
            getPresenter().onPredictionUpdated(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getPrediction());
        }
        else if (operationType == MobileClientData.OperationType.GET_PREDICTIONS.ordinal()) {
            getPresenter().onPredictionsFetched(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getUser(),
                    data.getPredictionList());
        }
        else if (operationType == MobileClientData.OperationType.GET_LATEST_PERFORMANCE.ordinal()) {
            getPresenter().onLatestPerformanceFetched(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getUserList(),
                    data.getPredictionList());
        }
    }

    @Override
    public void getInfo(String userID) {
        if (getService() == null) {
            getPresenter().onInfoFetched(false, "Not bound to the service",
                    null, null, null, null);
            return;
        }

        try {
            boolean isRequesting = getService().getInfo(userID);
            if (!isRequesting) {
                getPresenter().onInfoFetched(false, "No Network Connection",
                        null, null, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            getPresenter().onInfoFetched(false, "Error sending message",
                    null, null, null, null);
        }
    }

    @Override
    public void putPrediction(Prediction prediction) {
        if (getService() == null) {
            getPresenter().onPredictionUpdated(false, "Not bound to the service", prediction);
            return;
        }

        try {
            boolean isPutting = getService().putPrediction(prediction);
            if (!isPutting) {
                getPresenter().onPredictionUpdated(false, "No Network Connection", prediction);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            getPresenter().onPredictionUpdated(false, "Error sending message", prediction);
        }
    }

    @Override
    public void getPredictions(User user) {
        if (getService() == null) {
            getPresenter().onPredictionsFetched(false, "Not bound to the service", user, null);
            return;
        }

        try {
            boolean isPutting = getService().getPredictions(user);
            if (!isPutting) {
                getPresenter().onPredictionsFetched(false, "No Network Connection", user, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            getPresenter().onPredictionsFetched(false, "Error sending message", user, null);
        }
    }

    @Override
    public void getLatestPerformanceOfUsers(List<User> userList, int firstMatchNumber, int lastMatchNumber) {
        if (getService() == null) {
            getPresenter().onLatestPerformanceFetched(false, "Not bound to the service", null, null);
            return;
        }

        try {
            boolean isFetching = getService().getLatestPerformanceOfUsers(userList, firstMatchNumber, lastMatchNumber);
            if (!isFetching) {
                getPresenter().onLatestPerformanceFetched(false, "No Network Connection", null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            getPresenter().onLatestPerformanceFetched(false, "Error sending message", null, null);
        }
    }
}
