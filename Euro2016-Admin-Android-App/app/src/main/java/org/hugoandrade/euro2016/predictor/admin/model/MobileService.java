package org.hugoandrade.euro2016.predictor.admin.model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.League;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.WaitingLeagueUser;
import org.hugoandrade.euro2016.predictor.admin.network.MultipleCloudStatus;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MessageBase;
import org.hugoandrade.euro2016.predictor.admin.network.MobileServiceAdapter;
import org.hugoandrade.euro2016.predictor.admin.network.MobileServiceCallback;
import org.hugoandrade.euro2016.predictor.admin.network.MobileServiceData;
import org.hugoandrade.euro2016.predictor.admin.utils.InitConfigUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobileService extends Service {

    @SuppressWarnings("unused")
    private static final String TAG = MobileService.class.getSimpleName();

    private Messenger mRequestMessenger = null;
    private RequestHandler mRequestHandler = null;

    private final Object syncObj = new Object();

    public static Intent makeIntent(Context context) {
        return new Intent(context, MobileService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestHandler = new RequestHandler(this);
        mRequestMessenger = new Messenger(mRequestHandler);

        try {
            MobileServiceAdapter.Initialize(getApplicationContext());
        }
        catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mRequestMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MobileServiceAdapter.unInitialize(getApplicationContext());
        mRequestHandler.shutdown();
    }

    private void login(final Messenger replyTo, final int requestCode, final LoginData loginData) {

        MobileServiceCallback i = MobileServiceAdapter.getInstance().login(loginData);
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS:
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);

                if (data.wasSuccessful()) {
                    LoginData resultLoginData = data.getLoginData();
                    resultLoginData.setPassword(loginData.getPassword());

                    MobileServiceUser mobileServiceUser = new MobileServiceUser(resultLoginData.getUserID());
                    mobileServiceUser.setAuthenticationToken(resultLoginData.getToken());
                    MobileServiceAdapter.getInstance().setMobileServiceUser(mobileServiceUser);

                    requestMessage.setLoginData(loginData);
                }
                else {
                    requestMessage.setErrorMessage(data.getMessage());
                }


                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void getSystemData(final Messenger replyTo, final int requestCode) {
        MobileServiceCallback i = MobileServiceAdapter.getInstance().getSystemData();
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS:
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setSystemData(data.getSystemData());
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void getInfo(final Messenger replyTo, final int requestCode) {
        final MessageBase requestMessage = MessageBase.makeMessage(
                requestCode,
                MessageBase.REQUEST_RESULT_SUCCESS);

        final MultipleCloudStatus n = new MultipleCloudStatus(2);

        MobileServiceCallback iCountries = MobileServiceAdapter.getInstance().getCountries();
        MobileServiceCallback.addCallback(iCountries, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {
                synchronized (syncObj) {

                    if (n.isAborted()) return; // An error occurred
                    n.operationCompleted();

                    if (data.wasSuccessful()) {
                        ArrayList<Country> countryList = new ArrayList<>(data.getCountryList());

                        Collections.sort(countryList);

                        requestMessage.setCountryList(countryList);

                        if (n.isFinished())
                            sendRequestMessage(replyTo, requestMessage);

                    } else {
                        n.abort();
                        sendErrorMessage(replyTo, requestCode, data.getMessage());
                    }
                }
            }
        });

        MobileServiceCallback iMatches = MobileServiceAdapter.getInstance().getMatches();
        MobileServiceCallback.addCallback(iMatches, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {
                synchronized (syncObj) {

                    if (n.isAborted()) return; // An error occurred
                    n.operationCompleted();

                    if (data.wasSuccessful()) {
                        ArrayList<Match> matchList = new ArrayList<>(data.getMatchList());

                        Collections.sort(matchList);

                        requestMessage.setMatchList(matchList);

                        if (n.isFinished())
                            sendRequestMessage(replyTo, requestMessage);

                    } else {
                        n.abort();
                        sendErrorMessage(replyTo, requestCode, data.getMessage());
                    }
                }
            }
        });
    }

    private void updateSystemData(final Messenger replyTo, final int requestCode, final SystemData systemData) {

        MobileServiceCallback i = MobileServiceAdapter.getInstance().updateSystemData(systemData);
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS :
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setSystemData(data.getSystemData());
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void updateCountry(final Messenger replyTo, final int requestCode, Country country) {

        MobileServiceCallback i = MobileServiceAdapter.getInstance().updateCountry(country);
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS :
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setCountry(data.getCountry());
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void updateMatch(final Messenger replyTo, final int requestCode, final Match match) {

        MobileServiceCallback i = MobileServiceAdapter.getInstance().updateMatch(match);
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS :
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setMatch(data.getMatch());
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void reset(final Messenger replyTo, final int requestCode) {
        ResetCloudTask.instance()
                .setOnFinishedListener(new ResetCloudTask.OnFinished() {
                    @Override
                    public void onError(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }

                    @Override
                    public void onSuccess() {
                        getInfo(replyTo, requestCode);
                    }
                })
                .start();
    }

    private void updateScoresOfPredictions(final Messenger replyTo, final int requestCode) {
        MobileServiceCallback i = MobileServiceAdapter.getInstance().updateScoresOfPredictions();
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS :
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void createLeague(final Messenger replyTo, final int requestCode, final League league) {

        MobileServiceCallback i = MobileServiceAdapter.getInstance().createLeague(league);
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS:
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setLeague(data.getLeague());
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void joinLeague(final Messenger replyTo, final int requestCode, final WaitingLeagueUser waitingLeagueUser) {


        MobileServiceCallback i = MobileServiceAdapter.getInstance().joinLeague(waitingLeagueUser);
        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

            @Override
            public void onResult(MobileServiceData data) {

                int requestResult = data.wasSuccessful() ?
                        MessageBase.REQUEST_RESULT_SUCCESS:
                        MessageBase.REQUEST_RESULT_FAILURE;

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, requestResult);
                requestMessage.setLeague(data.getLeague());
                requestMessage.setErrorMessage(data.getMessage());

                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void sendErrorMessage(Messenger replyTo, int requestCode, String errorMessage) {
        MessageBase requestMessage = MessageBase.makeMessage(
                requestCode,
                MessageBase.REQUEST_RESULT_FAILURE
        );
        requestMessage.setErrorMessage(errorMessage);

        sendRequestMessage(replyTo, requestMessage);
    }

    private void sendRequestMessage(Messenger replyTo, MessageBase requestMessage) {
        try {
            replyTo.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    private static class RequestHandler extends Handler {

        @SuppressWarnings("unused")
        private static final String TAG = RequestHandler.class.getSimpleName();

        private WeakReference<MobileService> mService;
        private ExecutorService mExecutorService;
        private boolean isAsync = true;

        RequestHandler(MobileService service) {
            mService = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            final MessageBase requestMessage = MessageBase.makeMessage(message);
            final Messenger messenger = requestMessage.getMessenger();

            final int requestCode = requestMessage.getRequestCode();
            final Runnable sendDataToHub;

            if (requestCode == MessageBase.OperationType.RESET.ordinal()) {
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().reset(
                                messenger,
                                requestCode);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_SCORES_OF_PREDICTIONS.ordinal()) {
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateScoresOfPredictions(
                                messenger,
                                requestCode);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.GET_INFO.ordinal()) {
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getInfo(
                                messenger,
                                requestCode);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_UP.ordinal()) {
                final Match match = requestMessage.getMatch();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatch(
                                messenger,
                                requestCode,
                                match);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal()) {
                final Match match = requestMessage.getMatch();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatch(
                                messenger,
                                requestCode,
                                match);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_COUNTRY.ordinal()) {
                final Country country = requestMessage.getCountry();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateCountry(
                                messenger,
                                requestCode,
                                country);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_SYSTEM_DATA.ordinal()) {
                final SystemData systemData = requestMessage.getSystemData();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateSystemData(
                                messenger,
                                requestCode,
                                systemData);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.GET_SYSTEM_DATA.ordinal()) {
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getSystemData(
                                messenger,
                                requestCode);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.LOGIN.ordinal()) {
                final LoginData loginData = requestMessage.getLoginData();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().login(
                                messenger,
                                requestCode,
                                loginData);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.CREATE_LEAGUE.ordinal()) {
                final League league = requestMessage.getLeague();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().createLeague(
                                messenger,
                                requestCode,
                                league);
                    }
                };
            }
            else if (requestCode == MessageBase.OperationType.JOIN_LEAGUE.ordinal()) {
                final WaitingLeagueUser waitingLeagueUser = requestMessage.getWaitingLeagueUser();
                sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().joinLeague(
                                messenger,
                                requestCode,
                                waitingLeagueUser);
                    }
                };
            }
            else {
                return;
            }

            if (isAsync)
                mExecutorService.execute(sendDataToHub);
            else
                sendDataToHub.run();
        }

        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private static class ResetCloudTask {

        private static int DELETE_COUNTRY_TABLE = 1;
        private static int DELETE_MATCH_TABLE = 2;
        private static int POPULATE_COUNTRY_TABLE = 3;
        private static int POPULATE_MATCH_TABLE = 4;
        private static int POPULATE_SYSTEM_DATE = 5;

        private final Object syncObj = new Object();
        private final MHandler mHandler;

        private final SettableFuture<Boolean> mFuture;
        private OnFinished mListener;


        static ResetCloudTask instance() {
            return new ResetCloudTask();
        }

        ResetCloudTask() {
            mHandler = new MHandler(this, Looper.getMainLooper());
            mFuture = SettableFuture.create();
        }

        ResetCloudTask setOnFinishedListener(OnFinished listener) {
            mListener = listener;
            return this;
        }

        void start() {
            Futures.addCallback(mFuture, new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (mListener != null)
                        mListener.onSuccess();
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    if (mListener != null)
                        mListener.onError(throwable.getMessage());
                }
            });
            sendMessage(DELETE_COUNTRY_TABLE);
        }

        private void sendMessage(int what) {
            mHandler.obtainMessage(what).sendToTarget();
        }

        private void getExistingListOfCountries() {
            Log.d(TAG, "start to delete Country Table");

            Log.d(TAG, "getting Country Table");

            MobileServiceCallback iCountries = MobileServiceAdapter.getInstance().getCountries();
            MobileServiceCallback.addCallback(iCountries, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    Log.d(TAG, "getting Country Table operation ended");

                    if (!data.wasSuccessful()) {
                        String errorMessage = "Exception getting list of Countries: " + data.getMessage();

                        showErrorAndAbortMessage(errorMessage);
                        return;
                    }

                    deleteListOfCountries(data.getCountryList());
                }
            });
        }

        private void deleteListOfCountries(List<Country> countryList) {
            Log.d(TAG, "deleting Country Table");

            if (countryList == null || countryList.size() == 0) {
                Log.d(TAG, "deletion of Country Table successful");
                sendMessage(POPULATE_COUNTRY_TABLE);
                return;
            }

            final MultipleCloudStatus n = new MultipleCloudStatus(countryList.size());

            for (Country country : countryList) {
                Log.d(TAG, "deleting Country " + country.getName());

                MobileServiceCallback i = MobileServiceAdapter.getInstance().deleteCountry(country);
                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                    @Override
                    public void onResult(MobileServiceData data) {
                        synchronized (syncObj) {
                            if (n.isAborted()) {
                                Log.e(TAG, "Operation was previously aborted");
                                return;
                            }

                            if (!data.wasSuccessful()) {
                                n.abort();

                                String errorMessage = "Exception deleting a Country: " + data.getMessage();

                                showErrorAndAbortMessage(errorMessage);
                                return;
                            }

                            n.operationCompleted();
                            Log.d(TAG, "country deleted: " + data.getCountry().getName() + " - " + n.toString());

                            if (n.isFinished()) {
                                Log.d(TAG, "deletion of Country Table successful");
                                sendMessage(POPULATE_COUNTRY_TABLE);
                            }
                        }
                    }
                });
            }
        }

        private void populateCountryTable() {
            Log.d(TAG, "start to populate Country Table");
            List<Country> countries = InitConfigUtils.buildInitCountryList();

            final MultipleCloudStatus n = new MultipleCloudStatus(countries.size());

            for (Country country : countries) {

                MobileServiceCallback i = MobileServiceAdapter.getInstance().insertCountry(country);
                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                    @Override
                    public void onResult(MobileServiceData data) {
                        synchronized (syncObj) {
                            if (n.isAborted()) {
                                Log.e(TAG, "Operation was previously aborted");
                                return;
                            }

                            if (!data.wasSuccessful()) {
                                n.abort();

                                String errorMessage = "Exception populating Country: " + data.getMessage();

                                showErrorAndAbortMessage(errorMessage);
                                return;
                            }

                            n.operationCompleted();
                            Log.d(TAG, "country inserted: " + data.getCountry().getName() + " - " + n.toString());

                            if (n.isFinished()) {
                                Log.d(TAG, "populate of Country Table successful");
                                sendMessage(DELETE_MATCH_TABLE);
                            }
                        }
                    }
                });
            }
        }

        private void deleteMatchTable() {
            Log.d(TAG, "start to delete Match Table");

            Log.d(TAG, "getting Match Table");

            MobileServiceCallback iMatches = MobileServiceAdapter.getInstance().getMatches();
            MobileServiceCallback.addCallback(iMatches, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    Log.d(TAG, "getting Match Table operation ended");

                    if (!data.wasSuccessful()) {
                        String errorMessage = "Exception getting list of Matches: " + data.getMessage();

                        showErrorAndAbortMessage(errorMessage);
                        return;
                    }

                    deleteListOfMatches(data.getMatchList());
                }
            });
            Log.d(TAG, "start to delete Match Table");

        }

        private void deleteListOfMatches(List<Match> matchList) {
            Log.d(TAG, "deleting Match Table");

            if (matchList == null || matchList.size() == 0) {
                Log.d(TAG, "deletion of Match Table successful");
                sendMessage(POPULATE_MATCH_TABLE);
                return;
            }

            final MultipleCloudStatus n = new MultipleCloudStatus(matchList.size());

            for (Match match : matchList) {
                Log.d(TAG, "deleting Match " + match.getMatchNumber());

                MobileServiceCallback i = MobileServiceAdapter.getInstance().deleteMatch(match);
                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                    @Override
                    public void onResult(MobileServiceData data) {
                        synchronized (syncObj) {
                            if (n.isAborted()) {
                                Log.e(TAG, "Operation was previously aborted");
                                return;
                            }

                            if (!data.wasSuccessful()) {
                                n.abort();

                                String errorMessage = "Exception deleting a Match: " + data.getMessage();

                                showErrorAndAbortMessage(errorMessage);
                                return;
                            }
                            n.operationCompleted();
                            Log.d(TAG, "match deleted: " + data.getMatch().getMatchNumber() + " - " + n.toString());

                            if (n.isFinished()) {
                                Log.d(TAG, "deletion of Match Table successful");
                                sendMessage(POPULATE_MATCH_TABLE);
                            }
                        }
                    }
                });
            }
        }

        private void getNewListOfCountries() {
            Log.d(TAG, "start to getting Country table for init config of Matches");

            MobileServiceCallback i = MobileServiceAdapter.getInstance().getCountries();
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    Log.d(TAG, "getting Country Table operation ended");

                    if (!data.wasSuccessful()) {
                        String errorMessage = "Exception getting list of Countries: " + data.getMessage();

                        showErrorAndAbortMessage(errorMessage);
                        return;
                    }

                    populateMatchTable(data.getCountryList());
                }
            });
        }

        private void populateMatchTable(List<Country> countryList) {

            Log.d(TAG, "start to populate Match Table");
            List<Match> matches = InitConfigUtils.buildInitMatchList(countryList);

            final MultipleCloudStatus n = new MultipleCloudStatus(matches.size());

            for (Match match : matches) {

                MobileServiceCallback i = MobileServiceAdapter.getInstance().insertMatch(match);
                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                    @Override
                    public void onResult(MobileServiceData data) {
                        synchronized (syncObj) {
                            if (n.isAborted()) {
                                Log.e(TAG, "Operation was previously aborted");
                                return;
                            }

                            if (!data.wasSuccessful()) {
                                n.abort();

                                String errorMessage = "Exception populating Country: " + data.getMessage();

                                showErrorAndAbortMessage(errorMessage);
                                return;
                            }
                            n.operationCompleted();
                            Log.d(TAG, "match inserted: " + data.getMatch().getMatchNumber() + " - " + n.toString());

                            if (n.isFinished()) {
                                Log.d(TAG, "populate of Match Table successful");
                                sendMessage(POPULATE_SYSTEM_DATE);
                            }
                        }
                    }
                });
            }
        }

        private void populateSystemDate() {
            Log.d(TAG, "start to populate System Data");

            SystemData initSystemData = InitConfigUtils.buildInitSystemData();
            MobileServiceCallback i = MobileServiceAdapter.getInstance().updateSystemData(initSystemData);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()) {

                        String errorMessage = "Exception populating System Data: " + data.getMessage();

                        showErrorAndAbortMessage(errorMessage);
                        return;
                    }
                    successfulOperation();
                }
            });
        }

        private void successfulOperation() {
            Log.e(TAG, "Reset successfully completed");

            mHandler.shutdown();
            if (mFuture.set(true))
                Log.d(TAG, "result successfully set");
        }

        private void showErrorAndAbortMessage(String errorMessage) {
            Log.e(TAG, errorMessage);

            mHandler.shutdown();
            if (mFuture.setException(new Throwable(errorMessage)))
                Log.d(TAG, "exception successfully set");
        }

        public interface OnFinished {
            void onError(String errorMessage);
            void onSuccess();
        }

        private static class MHandler extends Handler {

            private WeakReference<ResetCloudTask> mBackgroundTask;
            private ExecutorService mExecutorService;

            MHandler(ResetCloudTask backgroundTask, Looper looper) {
                super(looper);
                mBackgroundTask = new WeakReference<>(backgroundTask);
                mExecutorService = Executors.newCachedThreadPool();
            }

            @Override
            public void handleMessage(Message message){
                Message m = Message.obtain(message);

                final int requestCode = m.what;

                if (requestCode == DELETE_COUNTRY_TABLE) {
                    final Runnable sendDataToHub = new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundTask.get().getExistingListOfCountries();
                        }
                    };
                    mExecutorService.execute(sendDataToHub);
                }
                else if (requestCode == POPULATE_COUNTRY_TABLE) {
                    final Runnable sendDataToHub = new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundTask.get().populateCountryTable();
                        }
                    };
                    mExecutorService.execute(sendDataToHub);
                }
                else if (requestCode == DELETE_MATCH_TABLE) {
                    final Runnable sendDataToHub = new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundTask.get().deleteMatchTable();
                        }
                    };
                    mExecutorService.execute(sendDataToHub);
                }
                else if (requestCode == POPULATE_MATCH_TABLE) {
                    final Runnable sendDataToHub = new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundTask.get().getNewListOfCountries();
                        }
                    };
                    mExecutorService.execute(sendDataToHub);
                }
                else if (requestCode == POPULATE_SYSTEM_DATE) {
                    final Runnable sendDataToHub = new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundTask.get().populateSystemDate();
                        }
                    };
                    mExecutorService.execute(sendDataToHub);
                }
            }

            void shutdown() {
                mExecutorService.shutdown();
            }
        }
    }
}
