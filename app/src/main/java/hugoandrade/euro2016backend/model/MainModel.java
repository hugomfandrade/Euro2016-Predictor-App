package hugoandrade.euro2016backend.model;

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

import hugoandrade.euro2016backend.MVP;
import hugoandrade.euro2016backend.common.MessageBase;
import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.object.Match;

import static hugoandrade.euro2016backend.model.MobileService.ALL_COUNTRIES_DATA;
import static hugoandrade.euro2016backend.model.MobileService.ALL_MATCHES_DATA;
import static hugoandrade.euro2016backend.model.MobileService.COUNTRY_DATA;
import static hugoandrade.euro2016backend.model.MobileService.MATCH_DATA;

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
                MobileService.OperationType.GET_ALL_COUNTRIES.ordinal(),
                mReplyMessage);

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
                MobileService.OperationType.GET_ALL_MATCHES.ordinal(),
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
                MobileService.OperationType.UPDATE_MATCH_UP.ordinal(),
                mReplyMessage);
        requestMessage.putParcelable(MATCH_DATA, match);

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
                MobileService.OperationType.UPDATE_MATCH_RESULT.ordinal(),
                mReplyMessage);
        requestMessage.putParcelable(MATCH_DATA, match);

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
                MobileService.OperationType.UPDATE_COUNTRY.ordinal(),
                mReplyMessage);
        requestMessage.putParcelable(COUNTRY_DATA, country);

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
            final String requestResult = requestMessage.getRequestResult();

            if (requestCode == MobileService.OperationType.GET_ALL_MATCHES.ordinal()) {
                switch (requestResult) {
                    case MobileService.REQUEST_RESULT_SUCCESS:
                        mModel.get().addAllMatchesRequestResult(
                                null,
                                requestMessage.<Match>getParcelableArrayList(ALL_MATCHES_DATA));
                        break;
                    case MobileService.REQUEST_RESULT_FAILURE:
                        mModel.get().addAllMatchesRequestResult(
                                requestMessage.getString(MobileService.ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().addAllMatchesRequestResult(
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            if (requestCode == MobileService.OperationType.GET_ALL_COUNTRIES.ordinal()) {
                switch (requestResult) {
                    case MobileService.REQUEST_RESULT_SUCCESS:
                        mModel.get().reportAllCountriesRequestResult(
                                null,
                                requestMessage.<Country>getParcelableArrayList(ALL_COUNTRIES_DATA));
                        break;
                    case MobileService.REQUEST_RESULT_FAILURE:
                        mModel.get().reportAllCountriesRequestResult(
                                requestMessage.getString(MobileService.ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().reportAllCountriesRequestResult(
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            if (requestCode == MobileService.OperationType.UPDATE_MATCH_RESULT.ordinal()) {
                switch (requestResult) {
                    case MobileService.REQUEST_RESULT_SUCCESS:
                        mModel.get().updateMatchRequestResult(
                                null,
                                requestMessage.<Match>getParcelable(MATCH_DATA));
                        break;
                    case MobileService.REQUEST_RESULT_FAILURE:
                        mModel.get().addAllMatchesRequestResult(
                                requestMessage.getString(MobileService.ERROR_MESSAGE),
                                null);
                        break;
                    default:
                        mModel.get().addAllMatchesRequestResult(
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

    private void reportAllCountriesRequestResult(String message, ArrayList<Country> allCountriesList) {
        mPresenter.get().reportGetAllCountriesRequestResult(message, allCountriesList);
    }

    private void updateMatchRequestResult(String message, Match match) {
        mPresenter.get().reportUpdateMatchRequestResult(message, match);

    }

    private void addAllMatchesRequestResult(String message, ArrayList<Match> allMatchesList) {
        mPresenter.get().reportGetAllMatchesRequestResult(message, allMatchesList);
    }
}
