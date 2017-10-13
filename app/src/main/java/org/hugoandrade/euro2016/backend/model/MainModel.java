package org.hugoandrade.euro2016.backend.model;

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

import org.hugoandrade.euro2016.backend.MVP;
import org.hugoandrade.euro2016.backend.model.parser.MessageBase;
import org.hugoandrade.euro2016.backend.model.service.MobileService;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;


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
            mPresenter.get().notifyServiceConnectionStatus(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRequestMessengerRef = null;
            mPresenter.get().notifyServiceConnectionStatus(false);
        }
    };

    @Override
    public void getAllCountries() {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.GET_ALL_COUNTRIES.ordinal(),
                mReplyMessage
        );

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    @Override
    public void getAllMatches() {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.GET_ALL_MATCHES.ordinal(),
                mReplyMessage);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    @Override
    public void getSystemData() {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.GET_SYSTEM_DATA.ordinal(),
                mReplyMessage);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    @Override
    public void updateMatchUp(Match match) {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_MATCH_UP.ordinal(),
                mReplyMessage
        );
        requestMessage.setMatch(match);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    @Override
    public void updateMatch(Match match) {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal(),
                mReplyMessage);
        requestMessage.setMatch(match);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    @Override
    public void updateCountry(Country country) {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.UPDATE_COUNTRY.ordinal(),
                mReplyMessage
        );
        requestMessage.setCountry(country);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    @Override
    public void setSystemData(SystemData systemData) {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return;
        }

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.SET_SYSTEM_DATA.ordinal(),
                mReplyMessage);
        requestMessage.setSystemData(systemData);

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
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

            if (requestCode == MessageBase.OperationType.GET_ALL_MATCHES.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().addAllMatchesRequestResult(
                                true,
                                null,
                                requestMessage.getMatchList());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().addAllMatchesRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().addAllMatchesRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.GET_ALL_COUNTRIES.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().reportAllCountriesRequestResult(
                                true,
                                null,
                                requestMessage.getCountryList());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().reportAllCountriesRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().reportAllCountriesRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal() ||
                    requestCode == MessageBase.OperationType.UPDATE_MATCH_UP.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().updateMatchRequestResult(
                                true,
                                null,
                                requestMessage.getMatch());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().addAllMatchesRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().addAllMatchesRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.GET_SYSTEM_DATA.ordinal() ||
                    requestCode == MessageBase.OperationType.SET_SYSTEM_DATA.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().reportSystemDataRequestResult(
                                true,
                                null,
                                requestMessage.getSystemData());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().reportSystemDataRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().addAllMatchesRequestResult(
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

    private void reportSystemDataRequestResult(boolean isRetrieved, String message, SystemData systemData) {
        mPresenter.get().reportSystemDataRequestResult(isRetrieved, message, systemData);
    }

    private void reportAllCountriesRequestResult(boolean isRetrieved, String message, ArrayList<Country> allCountriesList) {
        mPresenter.get().reportGetAllCountriesRequestResult(isRetrieved, message, allCountriesList);
    }

    private void updateMatchRequestResult(boolean isRetrieved, String message, Match match) {
        mPresenter.get().reportUpdateMatchRequestResult(isRetrieved, message, match);

    }

    private void addAllMatchesRequestResult(boolean isRetrieved, String message, ArrayList<Match> allMatchesList) {
        mPresenter.get().reportGetAllMatchesRequestResult(isRetrieved, message, allMatchesList);
    }
}
