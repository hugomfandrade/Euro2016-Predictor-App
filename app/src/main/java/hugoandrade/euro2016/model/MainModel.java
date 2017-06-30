package hugoandrade.euro2016.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.common.MessageBase;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.object.User;

import static hugoandrade.euro2016.model.AzureMobileService.ALL_COUNTRIES_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.ALL_MATCHES_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.ALL_USERS_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.ERROR_MESSAGE;
import static hugoandrade.euro2016.model.AzureMobileService.REQUEST_RESULT_FAILURE;
import static hugoandrade.euro2016.model.AzureMobileService.REQUEST_RESULT_SUCCESS;
import static hugoandrade.euro2016.model.AzureMobileService.SYSTEM_DATA;

public class MainModel implements MVP.ProvidedMainModelOps {

    protected final static String TAG =
            MainModel.class.getSimpleName();

    private Messenger mReplyMessage = null;
    private Messenger mRequestMessengerRef = null;
    private ReplyHandler mReplyHandler = new ReplyHandler(this);

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    private WeakReference<MVP.RequiredMainPresenterOps> mPresenter;

    @Override
    public void onCreate(MVP.RequiredMainPresenterOps presenter) {
        // Set the WeakReference.
        mPresenter =
                new WeakReference<>(presenter);

        mReplyMessage = new Messenger(mReplyHandler);

        // Bind to the Service.
        bindService();
    }
    @Override
    public void onDestroy(boolean isChangingConfigurations) {
        if (isChangingConfigurations)
            Log.d(TAG,
                    "just a configuration change - unbindService() not called");
        else {
            // Unbind from the Services only if onDestroy() is not
            // triggered by a runtime configuration change.
            unbindService();
            mReplyHandler.shutdown();
        }
    }

    private void bindService() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().getApplicationContext().bindService(
                    AzureMobileService.makeIntent(mPresenter.get().getActivityContext()),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }

    }

    private void unbindService() {
        if (mRequestMessengerRef != null) {
            mPresenter.get().getApplicationContext().unbindService(mServiceConnection);
            mRequestMessengerRef = null;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mRequestMessengerRef = new Messenger(binder);
            mPresenter.get().notifyServiceConnectionStatus(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRequestMessengerRef = null;
            mPresenter.get().notifyServiceConnectionStatus(false);
        }
    };

    @Override
    public void getSystemData() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().onSystemDataFetched(false, "Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.SYSTEM_TIME.ordinal(),
                    mReplyMessage);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().onSystemDataFetched(false, "Error sending message", null);
            }
        }
    }

    public void getAllMatches() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().onAllMatchesFetched(false, "Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.ALL_MATCHES.ordinal(),
                    mReplyMessage);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().onAllMatchesFetched(false, "Error sending message", null);
            }
        }
    }

    public void getAllCountries() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().onAllCountriesFetched(false, "Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.ALL_COUNTRIES.ordinal(),
                    mReplyMessage);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().onAllCountriesFetched(false, "Error sending message", null);
            }
        }
    }

    @Override
    public void getAllUsersScores() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().onAllUsersFetched(false, "Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.ALL_USERS.ordinal(),
                    mReplyMessage);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().onAllUsersFetched(false, "Error sending message", null);
            }
        }
    }

    @Override
    public void getAllPredictions(String userID, int matchNo) {
        if (mRequestMessengerRef == null) {
            mPresenter.get().onAllPredictionsFetched(false, "Not bound to the service", userID, null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.ALL_PREDICTIONS.ordinal(),
                    mReplyMessage);
            requestMessage.putString(Prediction.COL_NAME_USER_ID, userID);
            requestMessage.putInt(Prediction.COL_NAME_MATCH_NO, matchNo);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().onAllPredictionsFetched(false, "Error sending message", userID, null);
            }
        }
    }
    @Override
    public void putPrediction(Prediction prediction) {
        if (mRequestMessengerRef == null) {
            mPresenter.get().onPredictionUpdated(false, "Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.ALL_PREDICTIONS.ordinal(),
                    mReplyMessage);
            requestMessage.putParcelable(AzureMobileService.PREDICTION_DATA, prediction);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().onPredictionUpdated(false, "Error sending message", null);
            }
        }
    }

    private static class ReplyHandler extends android.os.Handler {

        @SuppressWarnings("unused") private final String TAG = getClass().getSimpleName();

        private WeakReference<MainModel> mModel;
        private ExecutorService mExecutorService;

        ReplyHandler(MainModel service) {
            mModel = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            super.handleMessage(message);
            if (mModel.get() == null) // Do not handle incoming request
                return;

            final MessageBase requestMessage = MessageBase.makeMessage(message);

            final int requestCode = requestMessage.getRequestCode();
            final String requestResult = requestMessage.getRequestResult();

            if (requestCode == AzureMobileService.OperationType.SYSTEM_TIME.ordinal()) {
                switch (requestResult) {
                    case REQUEST_RESULT_SUCCESS:
                        mModel.get().reportGetSystemDataResult(
                                true,
                                null,
                                requestMessage.<SystemData>getParcelable(SYSTEM_DATA));
                        break;
                    case REQUEST_RESULT_FAILURE:
                        mModel.get().reportGetSystemDataResult(
                                false,
                                requestMessage.getString(ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().reportGetSystemDataResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == AzureMobileService.OperationType.ALL_MATCHES.ordinal()) {
                switch (requestResult) {
                    case REQUEST_RESULT_SUCCESS:
                        mModel.get().reportGetAllMatchesRequestResult(
                                true,
                                null,
                                requestMessage.<Match>getParcelableArrayList(ALL_MATCHES_DATA));
                        break;
                    case REQUEST_RESULT_FAILURE:
                        mModel.get().reportGetAllMatchesRequestResult(
                                false,
                                requestMessage.getString(ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().reportGetAllMatchesRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == AzureMobileService.OperationType.ALL_PREDICTIONS.ordinal()) {
                switch (requestResult) {
                    case REQUEST_RESULT_SUCCESS:
                        mModel.get().reportGetAllPredictionsRequestResult(
                                true,
                                null,
                                requestMessage.getString(Prediction.COL_NAME_USER_ID),
                                requestMessage.<Prediction>getParcelableArrayList(
                                        AzureMobileService.ALL_PREDICTIONS_DATA));
                        break;
                    case REQUEST_RESULT_FAILURE:
                        mModel.get().reportGetAllPredictionsRequestResult(
                                false,
                                requestMessage.getString(ERROR_MESSAGE),
                                requestMessage.getString(Prediction.COL_NAME_USER_ID),
                                null);
                        break;
                    default:
                        mModel.get().reportGetAllPredictionsRequestResult(
                                false,
                                "No RequestResult provided",
                                null,
                                null);
                        break;
                }
            }
            else if (requestCode == AzureMobileService.OperationType.PUT_PREDICTION.ordinal()) {
                switch (requestResult) {
                    case REQUEST_RESULT_SUCCESS:
                        mModel.get().reportPutPredictionRequestResult(
                                true,
                                null,
                                requestMessage.<Prediction>getParcelable(
                                        AzureMobileService.PREDICTION_DATA));
                        break;
                    case REQUEST_RESULT_FAILURE:
                        mModel.get().reportPutPredictionRequestResult(
                                false,
                                requestMessage.getString(ERROR_MESSAGE),
                                requestMessage.<Prediction>getParcelable(
                                        AzureMobileService.PREDICTION_DATA));
                        break;
                    default:
                        mModel.get().reportPutPredictionRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == AzureMobileService.OperationType.ALL_COUNTRIES.ordinal()) {
                switch (requestResult) {
                    case REQUEST_RESULT_SUCCESS:
                        mModel.get().reportGetAllCountriesRequestResult(
                                true,
                                null,
                                requestMessage.<Country>getParcelableArrayList(ALL_COUNTRIES_DATA));
                        break;
                    case REQUEST_RESULT_FAILURE:
                        mModel.get().reportGetAllCountriesRequestResult(
                                false,
                                requestMessage.getString(ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().reportGetAllCountriesRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == AzureMobileService.OperationType.ALL_USERS.ordinal()) {
                switch (requestResult) {
                    case REQUEST_RESULT_SUCCESS:
                        mModel.get().reportGetAllUsersRequestResult(
                                true,
                                null,
                                requestMessage.<User>getParcelableArrayList(ALL_USERS_DATA));
                        break;
                    case REQUEST_RESULT_FAILURE:
                        mModel.get().reportGetAllUsersRequestResult(
                                false,
                                requestMessage.getString(ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().reportGetAllUsersRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
        }
        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private void reportGetAllUsersRequestResult(boolean operationResult, String message, ArrayList<User> usersList) {
        mPresenter.get().onAllUsersFetched(operationResult, message, usersList);
    }

    private void reportGetAllCountriesRequestResult(boolean operationResult, String message, ArrayList<Country> countriesList) {
        mPresenter.get().onAllCountriesFetched(operationResult, message, countriesList);
    }

    private void reportPutPredictionRequestResult(boolean operationResult, String message, Prediction prediction) {
        mPresenter.get().onPredictionUpdated(operationResult, message, prediction);
    }

    private void reportGetAllPredictionsRequestResult(boolean operationResult, String message, String userID, ArrayList<Prediction> predictionList) {
        mPresenter.get().onAllPredictionsFetched(operationResult, message, userID, predictionList);
    }

    private void reportGetSystemDataResult(boolean operationResult, String message, SystemData systemData) {
        mPresenter.get().onSystemDataFetched(operationResult, message, systemData);
    }

    private void reportGetAllMatchesRequestResult(boolean operationResult, String message, ArrayList<Match> allMatchesList) {
        mPresenter.get().onAllMatchesFetched(operationResult, message, allMatchesList);
    }
}
