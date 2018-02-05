package org.hugoandrade.euro2016.predictor.admin.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hugoandrade.euro2016.predictor.admin.MVP;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MessageBase;
import org.hugoandrade.euro2016.predictor.admin.model.service.MobileService;
import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.Match;
import org.hugoandrade.euro2016.predictor.admin.object.SystemData;

public class MainModel implements MVP.ProvidedModelOps {

    protected final static String TAG = MainModel.class.getSimpleName();

    private ReplyHandler mReplyHandler = null;
    private Messenger mReplyMessage = null;
    private Messenger mRequestMessengerRef = null;

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    private WeakReference<MVP.RequiredPresenterOps> mPresenter;

    @Override
    public void onCreate(MVP.RequiredPresenterOps presenter) {

        // Set the WeakReference.
        mPresenter =
                new WeakReference<>(presenter);

        mReplyHandler = new ReplyHandler(this);
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
            stopService();
            mReplyHandler.shutdown();
        }
    }

    private void bindService() {
        if (mRequestMessengerRef == null) {
            final Intent intent = MobileService.makeIntent(mPresenter.get().getActivityContext());
            mPresenter.get().getApplicationContext().bindService(
                    intent,
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

    private void stopService() {
        mPresenter.get().getApplicationContext().stopService(
                MobileService.makeIntent(mPresenter.get().getActivityContext()));

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mRequestMessengerRef = new Messenger(binder);
            mPresenter.get().notifyServiceIsBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRequestMessengerRef = null;
        }
    };

    private boolean areObjectsSet() {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return false;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return false;
        }
        return true;
    }

    @Override
    public boolean getInfo() {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.GET_INFO.ordinal(),
                mReplyMessage
        );

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean reset() {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.RESET.ordinal(),
                mReplyMessage
        );

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateMatchUp(Match match) {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_MATCH_UP.ordinal(),
                mReplyMessage
        );
        requestMessage.setMatch(match);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateMatch(Match match) {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal(),
                mReplyMessage);
        requestMessage.setMatch(match);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateCountry(Country country) {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_COUNTRY.ordinal(),
                mReplyMessage
        );
        requestMessage.setCountry(country);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean updateSystemData(SystemData systemData) {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_SYSTEM_DATA.ordinal(),
                mReplyMessage);
        requestMessage.setSystemData(systemData);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    private static class ReplyHandler extends android.os.Handler {

        private WeakReference<MainModel> mModel;
        private ExecutorService mExecutorService;

        ReplyHandler(MainModel service) {
            mModel = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            super.handleMessage(message);
            if (mModel == null || mModel.get() == null) // Do not handle incoming request
                return;

            final MessageBase requestMessage = MessageBase.makeMessage(message);
            final int requestCode = requestMessage.getRequestCode();
            final int requestResult = requestMessage.getRequestResult();

            if (requestCode == MessageBase.OperationType.RESET.ordinal() ||
                    requestCode == MessageBase.OperationType.GET_INFO.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().getAllInfoRequestResult(
                                true,
                                null,
                                requestMessage.getCountryList(),
                                requestMessage.getMatchList());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().getAllInfoRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null,
                                null);
                        break;
                    default:
                        mModel.get().getAllInfoRequestResult(
                                false,
                                "No RequestResult provided",
                                null,
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_COUNTRY.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().updateCountryRequestResult(
                                true,
                                null,
                                requestMessage.getCountry());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().updateCountryRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().updateCountryRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().updateMatchRequestResult(
                                true,
                                null,
                                requestMessage.getMatch());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().updateMatchRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().updateMatchRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_UP.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().updateMatchUpRequestResult(
                                true,
                                null,
                                requestMessage.getMatch());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().updateMatchUpRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().updateMatchUpRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_SYSTEM_DATA.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().updateSystemDataRequestResult(
                                true,
                                null,
                                requestMessage.getSystemData());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().updateSystemDataRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().updateSystemDataRequestResult(
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

    private void getAllInfoRequestResult(boolean isRetrieved,
                                         String message,
                                         ArrayList<Country> countryList,
                                         ArrayList<Match> matchList) {
        mPresenter.get().getAllInfoRequestResult(isRetrieved, message, countryList, matchList);
    }

    private void updateSystemDataRequestResult(boolean isRetrieved, String message, SystemData systemData) {
        mPresenter.get().updateSystemDataRequestResult(isRetrieved, message, systemData);
    }

    private void updateMatchRequestResult(boolean isRetrieved, String message, Match match) {
        mPresenter.get().updateMatchRequestResult(isRetrieved, message, match);
    }

    private void updateMatchUpRequestResult(boolean isRetrieved, String message, Match match) {
        mPresenter.get().updateMatchUpRequestResult(isRetrieved, message, match);
    }

    private void updateCountryRequestResult(boolean isRetrieved, String message, Country country) {
        mPresenter.get().updateCountryRequestResult(isRetrieved, message, country);
    }

}
