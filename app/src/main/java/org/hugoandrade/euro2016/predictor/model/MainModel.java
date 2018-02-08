package org.hugoandrade.euro2016.predictor.model;

import android.os.RemoteException;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

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
}
