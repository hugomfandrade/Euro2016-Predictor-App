package org.hugoandrade.euro2016.predictor.model.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.IMobileClientServiceCallback;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.network.MobileServiceAdapter;
import org.hugoandrade.euro2016.predictor.network.MobileServiceCallback;
import org.hugoandrade.euro2016.predictor.network.MobileServiceData;
import org.hugoandrade.euro2016.predictor.network.MultipleCloudStatus;
import org.hugoandrade.euro2016.predictor.utils.ErrorMessageUtils;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MobileService extends LifecycleLoggingService {

    private IMobileClientServiceCallback mCallback;

    private final Object syncObj = new Object();

    public static Intent makeIntent(Context context) {
        return new Intent(context, MobileService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            MobileServiceAdapter.Initialize(getApplicationContext());

            LoginData loginData
                    = SharedPreferencesUtils.getLastAuthenticatedLoginData(getApplicationContext());

            MobileServiceUser mobileServiceUser = new MobileServiceUser(loginData.getUserID());
            mobileServiceUser.setAuthenticationToken(loginData.getToken());
            MobileServiceAdapter.getInstance().setMobileServiceUser(mobileServiceUser);

        }
        catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MobileServiceAdapter.unInitialize(getApplicationContext());
    }

    /**
     *  Local-side IPC implementation stub class and constructs the stub
     *  and attaches it to the interface.
     *  */
    private final IMobileClientService.Stub mBinder = new IMobileClientService.Stub() {

        @Override
        public void registerCallback(IMobileClientServiceCallback cb) {
            mCallback = cb;
        }

        @Override
        public void unregisterCallback(IMobileClientServiceCallback cb) {
            if (mCallback == cb)
                mCallback = null;
        }

        @Override
        public void getSystemData() {

            MobileServiceAdapter.getInstance().setMobileServiceUser(null);
            MobileServiceCallback i = MobileServiceAdapter.getInstance().getSystemData();
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    int requestResult = data.wasSuccessful() ?
                            MobileClientData.REQUEST_RESULT_SUCCESS:
                            MobileClientData.REQUEST_RESULT_FAILURE;

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.GET_SYSTEM_DATA.ordinal(),
                            requestResult);
                    m.setSystemData(data.getSystemData());
                    m.setErrorMessage(data.getMessage());

                    sendMobileDataMessage(m);
                }
            });
        }

        @Override
        public void logout() {

            SharedPreferencesUtils.resetLastAuthenticatedLoginData(getApplicationContext());

            MobileServiceCallback i = MobileServiceAdapter.getInstance().logOut();
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    int requestResult = data.wasSuccessful() ?
                            MobileClientData.REQUEST_RESULT_SUCCESS :
                            MobileClientData.REQUEST_RESULT_FAILURE;

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.LOGOUT.ordinal(),
                            requestResult);
                    m.setErrorMessage(data.getMessage());

                    sendMobileDataMessage(m);
                }
            });
        }

        @Override
        public void login(final LoginData loginData) {

            MobileServiceAdapter.getInstance().setMobileServiceUser(null);
            SharedPreferencesUtils.resetLastAuthenticatedLoginData(getApplicationContext());

            MobileServiceCallback i = MobileServiceAdapter.getInstance().login(loginData);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    int requestResult = data.wasSuccessful() ?
                            MobileClientData.REQUEST_RESULT_SUCCESS:
                            MobileClientData.REQUEST_RESULT_FAILURE;

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.LOGIN.ordinal(),
                            requestResult);

                    if (data.wasSuccessful()) {
                        LoginData resultLoginData = data.getLoginData();
                        resultLoginData.setPassword(loginData.getPassword());

                        MobileServiceUser mobileServiceUser = new MobileServiceUser(resultLoginData.getUserID());
                        mobileServiceUser.setAuthenticationToken(resultLoginData.getToken());
                        MobileServiceAdapter.getInstance().setMobileServiceUser(mobileServiceUser);

                        m.setLoginData(resultLoginData);
                    }
                    else {
                        m.setErrorMessage(data.getMessage());
                    }

                    sendMobileDataMessage(m);
                }
            });
        }

        @Override
        public void signUp(final LoginData loginData) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().signUp(loginData);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    int requestResult = data.wasSuccessful() ?
                            MobileClientData.REQUEST_RESULT_SUCCESS:
                            MobileClientData.REQUEST_RESULT_FAILURE;

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.REGISTER.ordinal(),
                            requestResult);

                    if (data.wasSuccessful()) {
                        LoginData resultLoginData = data.getLoginData();
                        resultLoginData.setPassword(loginData.getPassword());
                        m.setLoginData(resultLoginData);
                    }
                    else {
                        m.setErrorMessage(data.getMessage());
                    }

                    sendMobileDataMessage(m);
                }
            });
        }

        @Override
        public void getInfo(String userID) {
            getInfo(userID, true);
        }

        private void getInfo(final String userID, final boolean tryRefresh) {

            final MobileClientData m = MobileClientData.makeMessage(
                    MobileClientData.OperationType.GET_INFO.ordinal(),
                    MobileClientData.REQUEST_RESULT_SUCCESS);

            final MultipleCloudStatus n = new MultipleCloudStatus(4);

            MobileServiceCallback iCountries = MobileServiceAdapter.getInstance().getCountries();
            MobileServiceCallback.addCallback(iCountries, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    synchronized (syncObj) {
                        Log.e(TAG, "iCountries finished");

                        if (n.isAborted()) return; // An error occurred
                        n.operationCompleted();

                        if (data.wasSuccessful()) {
                            ArrayList<Country> countryList = new ArrayList<>(data.getCountryList());

                            Collections.sort(countryList);

                            m.setCountryList(countryList);

                            if (n.isFinished())
                                sendMobileDataMessage(m);

                        } else {
                            n.abort();
                            Log.e(TAG, "sendErrorMessage: (" + 1 + ") " + data.getMessage());
                            if (tryRefresh && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {
                                MobileServiceCallback i = refreshToken();
                                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                                    @Override
                                    public void onResult(MobileServiceData data) {
                                        if (data.wasSuccessful()) {
                                            getInfo(userID, false);
                                        }
                                    }
                                });
                            }
                            else {
                                sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), data.getMessage());
                            }
                        }
                    }
                }
            });

            MobileServiceCallback iMatches = MobileServiceAdapter.getInstance().getMatches();
            MobileServiceCallback.addCallback(iMatches, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    synchronized (syncObj) {
                        Log.e(TAG, "iMatches finished");

                        if (n.isAborted()) return; // An error occurred
                        n.operationCompleted();

                        if (data.wasSuccessful()) {
                            ArrayList<Match> matchList = new ArrayList<>(data.getMatchList());

                            Collections.sort(matchList);

                            m.setMatchList(matchList);

                            if (n.isFinished())
                                sendMobileDataMessage(m);

                        } else {
                            n.abort();
                            Log.e(TAG, "sendErrorMessage: (" + 2 + ") " + data.getMessage());
                            if (tryRefresh && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {
                                MobileServiceCallback i = refreshToken();
                                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                                    @Override
                                    public void onResult(MobileServiceData data) {
                                        if (data.wasSuccessful()) {
                                            getInfo(userID, false);
                                        }
                                    }
                                });
                            }
                            else {
                                sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), data.getMessage());
                            }
                        }
                    }
                }
            });

            MobileServiceCallback iPredictions = MobileServiceAdapter.getInstance().getPredictions(userID);
            MobileServiceCallback.addCallback(iPredictions, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    synchronized (syncObj) {
                        Log.e(TAG, "iPredictions finished");

                        if (n.isAborted()) return; // An error occurred
                        n.operationCompleted();

                        if (data.wasSuccessful()) {

                            m.setPredictionList(data.getPredictionList());

                            if (n.isFinished())
                                sendMobileDataMessage(m);

                        } else {
                            n.abort();
                            Log.e(TAG, "sendErrorMessage: (" + 3 + ") " + data.getMessage());
                            if (tryRefresh && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {
                                MobileServiceCallback i = refreshToken();
                                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                                    @Override
                                    public void onResult(MobileServiceData data) {
                                        if (data.wasSuccessful()) {
                                            getInfo(userID, false);
                                        }
                                    }
                                });
                            }
                            else {
                                sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), data.getMessage());
                            }
                        }
                    }
                }
            });

            MobileServiceCallback iLeagues = MobileServiceAdapter.getInstance().getLeagues(userID);
            MobileServiceCallback.addCallback(iLeagues, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {
                    synchronized (syncObj) {
                        Log.e(TAG, "iLeagues finished");

                        if (n.isAborted()) return; // An error occurred
                        n.operationCompleted();

                        if (data.wasSuccessful()) {

                            m.setLeagueWrapperList(data.getLeagueWrapperList());

                            if (n.isFinished())
                                sendMobileDataMessage(m);

                        } else {
                            n.abort();
                            Log.e(TAG, "sendErrorMessage: (" + 4 + ") " + data.getMessage());
                            if (tryRefresh && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {
                                MobileServiceCallback i = refreshToken();
                                MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                                    @Override
                                    public void onResult(MobileServiceData data) {
                                        if (data.wasSuccessful()) getInfo(userID, false);
                                    }
                                });
                            }
                            else {
                                sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), data.getMessage());
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void putPrediction(final Prediction prediction) {
            putPrediction(prediction, true);
        }

        private void putPrediction(final Prediction prediction, final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().insertPrediction(prediction);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) putPrediction(prediction, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.PUT_PREDICTION.ordinal(),
                                requestResult);

                        m.setPrediction(data.getPrediction());
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void getPredictions(final User user) {
            getPredictions(user, true);
        }

        private void getPredictions(final User user, final boolean tryRefresh) {
            MobileServiceCallback i = MobileServiceAdapter.getInstance().getPredictions(user.getID());
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) getPredictions(user, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.GET_PREDICTIONS.ordinal(),
                                requestResult);
                        m.setUser(user);
                        m.setPredictionList(data.getPredictionList());
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void getPredictionsOfUsers(final List<User> userList, final int matchNumber) {
            getPredictionsOfUsers(userList, matchNumber, true);
        }

        private void getPredictionsOfUsers(final List<User> userList, final int matchNumber, final boolean tryRefresh) {

            String[] userIDs = new String[userList.size()];
            for (int i = 0 ; i < userList.size() ; i++) {
                userIDs[i] = userList.get(i).getID();
            }

            MobileServiceCallback i = MobileServiceAdapter.getInstance().getPredictions(userIDs, matchNumber);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {
                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) getPredictionsOfUsers(userList, matchNumber, false);
                            }
                        });
                    }
                    else {
                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.GET_PREDICTIONS_OF_USERS.ordinal(),
                                MobileClientData.REQUEST_RESULT_SUCCESS);
                        m.setInteger(matchNumber);
                        m.setUsers(userList);
                        m.setPredictionList(data.getPredictionList());
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void createLeague(String userID, String leagueName) {
            createLeague(userID, leagueName, true);
        }

        private void createLeague(final String userID, final String leagueName, final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().createLeague(userID, leagueName);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) createLeague(userID, leagueName, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.CREATE_LEAGUE.ordinal(),
                                requestResult);
                        m.setLeague(data.getLeague());
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void joinLeague(String userID, String leagueCode) {
            joinLeague(userID, leagueCode, true);
        }

        private void joinLeague(final String userID, final String leagueCode, final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().joinLeague(userID, leagueCode);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) joinLeague(userID, leagueCode, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.JOIN_LEAGUE.ordinal(),
                                requestResult);
                        m.setLeagueWrapper(data.getLeagueWrapper());
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void leaveLeague(String userID, String leagueID) {
            leaveLeague(userID, leagueID, true);
        }

        private void leaveLeague(final String userID, final String leagueID, final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().leaveLeague(userID, leagueID);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) leaveLeague(userID, leagueID, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.LEAVE_LEAGUE.ordinal(),
                                requestResult);
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void deleteLeague(String userID, String leagueID)  {
            deleteLeague(userID, leagueID, true);
        }

        private void deleteLeague(final String userID, final String leagueID, final boolean tryRefresh)  {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().deleteLeague(userID, leagueID);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) deleteLeague(userID, leagueID, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.DELETE_LEAGUE.ordinal(),
                                requestResult);
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void fetchMoreUsers(String leagueID, int skip, int top) {
            fetchMoreUsers(leagueID, skip, top, true);
        }

        private void fetchMoreUsers(final String leagueID, final int skip, final int top, final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().fetchMoreUsers(leagueID, skip, top);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) fetchMoreUsers(leagueID, skip, top, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.FETCH_MORE_USERS.ordinal(),
                                requestResult);
                        m.setLeagueUserList(data.getLeagueUserList());
                        m.setString(data.getString());
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void fetchMoreUsersByStage(final String leagueID,
                                          final int skip,
                                          final int top,
                                          final int stage,
                                          final int minMatchNumber,
                                          final int maxMatchNumber) {
            fetchMoreUsersByStage(leagueID, skip, top, stage, minMatchNumber, maxMatchNumber, true);
        }

        private void fetchMoreUsersByStage(final String leagueID,
                                           final int skip,
                                           final int top,
                                           final int stage,
                                           final int minMatchNumber,
                                           final int maxMatchNumber,
                                           final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().fetchMoreUsers(leagueID, skip, top, minMatchNumber, maxMatchNumber);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) fetchMoreUsersByStage(leagueID, skip, top, stage, minMatchNumber, maxMatchNumber, false);
                            }
                        });
                    }
                    else {
                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.FETCH_MORE_USERS_BY_STAGE.ordinal(),
                                requestResult);
                        m.setLeagueUserList(data.getLeagueUserList());
                        m.setString(data.getString());
                        m.setInteger(stage);
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        @Override
        public void fetchUsersByStage(final String leagueID,
                                      final String userID,
                                      final int skip,
                                      final int top,
                                      final int stage,
                                      final int minMatchNumber,
                                      final int maxMatchNumber) {

            fetchUsersByStage(leagueID, userID, skip, top, stage, minMatchNumber, maxMatchNumber, true);
        }

        private void fetchUsersByStage(final String leagueID,
                                      final String userID,
                                      final int skip,
                                      final int top,
                                      final int stage,
                                      final int minMatchNumber,
                                      final int maxMatchNumber,
                                      final boolean tryRefresh) {

            MobileServiceCallback i = MobileServiceAdapter.getInstance().fetchUsers(leagueID, userID, skip, top, minMatchNumber, maxMatchNumber);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {

                    if (!data.wasSuccessful()
                            && tryRefresh
                            && ErrorMessageUtils.isErrorValidatingAccessTokenError(data.getMessage())) {

                        MobileServiceCallback i = refreshToken();
                        MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                            @Override
                            public void onResult(MobileServiceData data) {
                                if (data.wasSuccessful()) fetchUsersByStage(leagueID, userID, skip, top, stage, minMatchNumber, maxMatchNumber, false);
                            }
                        });
                    }
                    else {

                        int requestResult = data.wasSuccessful() ?
                                MobileClientData.REQUEST_RESULT_SUCCESS :
                                MobileClientData.REQUEST_RESULT_FAILURE;

                        MobileClientData m = MobileClientData.makeMessage(
                                MobileClientData.OperationType.FETCH_USERS_BY_STAGE.ordinal(),
                                requestResult);
                        m.setLeagueWrapper(data.getLeagueWrapper());
                        m.setInteger(stage);
                        m.setErrorMessage(data.getMessage());

                        sendMobileDataMessage(m);
                    }
                }
            });
        }

        private MobileServiceCallback refreshToken() {
            Log.e(TAG, "refreshToken");

            final MobileServiceCallback callback = new MobileServiceCallback();

            final LoginData loginData = SharedPreferencesUtils.getLastAuthenticatedLoginData(getApplicationContext());

            MobileServiceCallback i = MobileServiceAdapter.getInstance().login(loginData);
            MobileServiceCallback.addCallback(i, new MobileServiceCallback.OnResult() {

                @Override
                public void onResult(MobileServiceData data) {
                    Log.e(TAG, "refreshToken::successful=" + data.wasSuccessful());

                    int requestResult = data.wasSuccessful() ?
                            MobileServiceData.REQUEST_RESULT_SUCCESS:
                            MobileServiceData.REQUEST_RESULT_FAILURE;

                    if (data.wasSuccessful()) {
                        LoginData resultLoginData = data.getLoginData();
                        resultLoginData.setPassword(loginData.getPassword());

                        MobileServiceUser mobileServiceUser = new MobileServiceUser(resultLoginData.getUserID());
                        mobileServiceUser.setAuthenticationToken(resultLoginData.getToken());
                        MobileServiceAdapter.getInstance().setMobileServiceUser(mobileServiceUser);
                    }
                    else {
                        sendLogOutMessage();
                    }

                    callback.set(MobileServiceData.Builder
                            .instance(MobileServiceData.REFRESH_TOKEN, requestResult)
                            .create());
                }
            });

            return callback;
        }
    };

    /**
     * Sends a callback error message with a failure operation result flag
     * and with the given operation type flag
     */
    private void sendErrorMessage(int operationType, String message) {
        MobileClientData m = MobileClientData.makeMessage(
                operationType,
                MobileClientData.REQUEST_RESULT_FAILURE);
        m.setErrorMessage(message);

        sendMobileDataMessage(m);
    }

    private void sendLogOutMessage() {
        MobileClientData m = MobileClientData.makeMessage(
                MobileClientData.OperationType.LOGOUT.ordinal(),
                MobileClientData.REQUEST_RESULT_SUCCESS);

        sendMobileDataMessage(m);
    }

    private void sendMobileDataMessage(MobileClientData mobileClientData) {
        try {
            if (mCallback != null)
                mCallback.sendResults(mobileClientData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
