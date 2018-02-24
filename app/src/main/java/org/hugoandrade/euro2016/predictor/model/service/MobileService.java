package org.hugoandrade.euro2016.predictor.model.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;

import org.hugoandrade.euro2016.predictor.DevConstants;
import org.hugoandrade.euro2016.predictor.cloudsim.CloudDatabaseSim;
import org.hugoandrade.euro2016.predictor.data.Country;
import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.SystemData;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.IMobileClientServiceCallback;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.utils.ISO8601;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

public class MobileService extends LifecycleLoggingService

        implements CloudDatabaseSim.Callback {

    private IMobileClientServiceCallback mCallback;

    private MobileServiceClient mMobileServiceClient;

    private MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

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

        initializeMobileServiceClient();
        initializeNetworkBroadcastReceiver();
        initializeCloudDatabaseSim();
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
    }

    private void initializeCloudDatabaseSim() {
        CloudDatabaseSim.initialize(this, getContentResolver());
    }

    private void initializeNetworkBroadcastReceiver() {
        ConnectionStateBroadcastReceiver broadcastReceiver = new ConnectionStateBroadcastReceiver(this);
        getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void initializeMobileServiceClient() {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                mMobileServiceClient = new MobileServiceClient(
                        DevConstants.appUrl,
                        null,
                        this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *  Local-side IPC implementation stub class and constructs the stub
     *  and attaches it to the interface.
     *  */
    private final IMobileClientService.Stub mBinder = new IMobileClientService.Stub() {

        @Override
        public void registerCallback(IMobileClientServiceCallback cb) throws RemoteException {
            mCallback = cb;
        }

        @Override
        public void unregisterCallback(IMobileClientServiceCallback cb) throws RemoteException {
            if (mCallback == cb)
                mCallback = null;
        }

        @Override
        public boolean getSystemData() {
            if (DevConstants.CLOUD_DATABASE_SIM) {
                CloudDatabaseSim.getSystemData();
                return true;
            }

            if (mMobileServiceClient == null)
                return false;

            ListenableFuture<JsonElement> future =
                    mMobileServiceClient.invokeApi(SystemData.Entry.API_NAME,
                            null,
                            "GET",
                            null);

            Futures.addCallback(future, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonObject) {
                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.GET_SYSTEM_DATA.ordinal(),
                            MobileClientData.REQUEST_RESULT_SUCCESS);
                    m.setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()));

                    sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    if (t.getMessage().equals("Error while processing request.")){
                        getSystemData();
                        return;
                    }
                    sendErrorMessage(MobileClientData.OperationType.GET_SYSTEM_DATA.ordinal(), t.getMessage());
                }
            });
            return true;
        }

        @Override
        public boolean login(final LoginData loginData) {
            if (DevConstants.CLOUD_DATABASE_SIM) {
                CloudDatabaseSim.login(loginData);
                return true;
            }

            if (mMobileServiceClient == null)
                return false;

            ListenableFuture<JsonElement> future =
                    mMobileServiceClient.invokeApi(LoginData.Entry.API_NAME_LOGIN,
                            formatter.getAsJsonObject(loginData),
                            "POST",
                            null);

            Futures.addCallback(future, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonObject) {

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.LOGIN.ordinal(),
                            MobileClientData.REQUEST_RESULT_SUCCESS);
                    LoginData data = parser.parseLoginData(jsonObject.getAsJsonObject());
                    data.setPassword(loginData.getPassword());
                    m.setLoginData(data);

                    MobileServiceUser mMobileServiceUser = new MobileServiceUser(data.getUserID());
                    mMobileServiceUser.setAuthenticationToken(data.getToken());
                    mMobileServiceClient.setCurrentUser(mMobileServiceUser);

                    sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    sendErrorMessage(MobileClientData.OperationType.LOGIN.ordinal(), t.getMessage());
                }
            });
            return true;
        }

        @Override
        public boolean signUp(final LoginData loginData) {
            if (DevConstants.CLOUD_DATABASE_SIM) {
                CloudDatabaseSim.signUp(loginData);
                return true;
            }

            if (mMobileServiceClient == null)
                return false;

            ListenableFuture<JsonElement> future =
                    mMobileServiceClient.invokeApi(LoginData.Entry.API_NAME_REGISTER,
                            formatter.getAsJsonObject(loginData),
                            "POST",
                            null);

            Futures.addCallback(future, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonObject) {

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.REGISTER.ordinal(),
                            MobileClientData.REQUEST_RESULT_SUCCESS);
                    LoginData data = parser.parseLoginData(jsonObject.getAsJsonObject());
                    m.setLoginData(data);

                    sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    sendErrorMessage(MobileClientData.OperationType.REGISTER.ordinal(), t.getMessage());
                }
            });
            return true;
        }

        @Override
        public boolean getInfo(String userID) {
            if (DevConstants.CLOUD_DATABASE_SIM) {
                CloudDatabaseSim.getInfo(userID);
                return true;
            }

            if (mMobileServiceClient == null)
                return false;

            final MobileClientData m = MobileClientData.makeMessage(
                    MobileClientData.OperationType.GET_INFO.ordinal(),
                    MobileClientData.REQUEST_RESULT_SUCCESS);

            final int[] n = {
                    0 /* completed operations */,
                    4 /* total operations */,
                    1 /* isOk flag */};


            ListenableFuture<JsonElement> futureCountries =  MobileServiceJsonTableHelper
                    .instance(Country.Entry.TABLE_NAME, mMobileServiceClient)
                    .execute();
            Futures.addCallback(futureCountries, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {
                    Log.d(TAG, "Countries fetched");
                    if (n[2] == 0) return; // An error occurred

                    m.setCountryList(parser.parseCountryList(jsonElement));

                    n[0]++;
                    if (n[0] == n[1])
                        sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    Log.d(TAG, "Error fetching Countries: " + throwable.getMessage());
                    if (n[2] == 0) return; // An error occurred

                    n[2] = 0;
                    sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable.getMessage());
                }
            });

            ListenableFuture<JsonElement> futureMatches =  MobileServiceJsonTableHelper
                    .instance(Match.Entry.TABLE_NAME, mMobileServiceClient)
                    .execute();
            Futures.addCallback(futureMatches, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {
                    Log.d(TAG, "Matches fetched");
                    if (n[2] == 0) return; // An error occurred

                    ArrayList<Match> matchList = parser.parseMatchList(jsonElement);
                    Collections.sort(matchList);
                    m.setMatchList(matchList);

                    n[0]++;
                    if (n[0] == n[1])
                        sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    Log.d(TAG, "Error fetching Matches: " + throwable.getMessage());
                    if (n[2] == 0) return; // An error occurred

                    n[2] = 0;
                    sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable.getMessage());
                }
            });

            ListenableFuture<JsonElement> futureUsers =  MobileServiceJsonTableHelper
                    .instance(User.Entry.TABLE_NAME, mMobileServiceClient)
                    .execute();
            Futures.addCallback(futureUsers, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {
                    Log.d(TAG, "Users fetched");
                    if (n[2] == 0) return; // An error occurred

                    m.setUsers(parser.parseUserList(jsonElement));

                    n[0]++;
                    if (n[0] == n[1])
                        sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    Log.d(TAG, "Error fetching Users: " + throwable.getMessage());
                    if (n[2] == 0) return; // An error occurred

                    n[2] = 0;
                    sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable.getMessage());
                }
            });

            ListenableFuture<JsonElement> futurePredictions =  MobileServiceJsonTableHelper
                    .instance(Prediction.Entry.TABLE_NAME, mMobileServiceClient)
                    .where(Prediction.Entry.Cols.USER_ID, userID)
                    .execute();
            Futures.addCallback(futurePredictions, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {
                    Log.d(TAG, "Predictions fetched");
                    if (n[2] == 0) return; // An error occurred

                    m.setPredictionList(parser.parsePredictionList(jsonElement));

                    n[0]++;
                    if (n[0] == n[1])
                        sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    Log.d(TAG, "Error fetching Predictions: " + throwable.getMessage());
                    if (n[2] == 0) return; // An error occurred

                    n[2] = 0;
                    sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable.getMessage());
                }
            });
            return true;
        }

        @Override
        public boolean putPrediction(final Prediction prediction) {
            if (DevConstants.CLOUD_DATABASE_SIM) {
                CloudDatabaseSim.putPrediction(prediction);
                return true;
            }

            if (mMobileServiceClient == null)
                return false;

            ListenableFuture<JsonObject> future
                    = new MobileServiceJsonTable(Prediction.Entry.TABLE_NAME, mMobileServiceClient)
                    .insert(formatter.getAsJsonObject(prediction));
            Futures.addCallback(future, new FutureCallback<JsonObject>() {
                @Override
                public void onSuccess(JsonObject result) {

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.PUT_PREDICTION.ordinal(),
                            MobileClientData.REQUEST_RESULT_SUCCESS);
                    m.setPrediction(parser.parsePrediction(result));

                    sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    Log.d(TAG, "putPrediction(FAILURE): " + t.getMessage());

                    String errorMessage = t.getMessage();

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.PUT_PREDICTION.ordinal(),
                            MobileClientData.REQUEST_RESULT_FAILURE);
                    m.setPrediction(prediction);
                    m.setErrorMessage(errorMessage);
                    if (errorMessage.contains(Prediction.Entry.PastMatchDate))
                        m.setServerTime(ISO8601.toCalendar(errorMessage.split(":")[1]));

                    sendMobileDataMessage(m);
                }
            });
            return true;
        }

        @Override
        public boolean getPredictions(final User user) throws RemoteException {
            if (DevConstants.CLOUD_DATABASE_SIM) {
                //CloudDatabaseSim.getPredictions(user);
                return true;
            }

            if (mMobileServiceClient == null)
                return false;

            ListenableFuture<JsonElement> futurePredictions =  MobileServiceJsonTableHelper
                    .instance(Prediction.Entry.TABLE_NAME, mMobileServiceClient)
                    .where(Prediction.Entry.Cols.USER_ID, user.getID())
                    .execute();
            Futures.addCallback(futurePredictions, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {

                    MobileClientData m = MobileClientData.makeMessage(
                            MobileClientData.OperationType.GET_PREDICTIONS.ordinal(),
                            MobileClientData.REQUEST_RESULT_SUCCESS);
                    m.setUser(user);
                    m.setPredictionList(parser.parsePredictionList(jsonElement));

                    sendMobileDataMessage(m);
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    Log.d(TAG, "Error fetching Predictions: " + throwable.getMessage());
                    sendErrorMessage(MobileClientData.OperationType.GET_PREDICTIONS.ordinal(), throwable.getMessage());
                }
            });
            return true;
        }

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
    };

    @Override
    public void sendMobileDataMessage(MobileClientData mobileClientData) {
        try {
            if (mCallback != null)
                mCallback.sendResults(mobileClientData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void networkState(boolean state) {
        if (state) {
            if (mMobileServiceClient == null) {
                initializeMobileServiceClient();
            }
        }
        else
            mMobileServiceClient = null;
    }

    private class ConnectionStateBroadcastReceiver extends BroadcastReceiver {
        private WeakReference<MobileService> mService;

        public ConnectionStateBroadcastReceiver(MobileService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (cm != null) {
                networkInfo = cm.getActiveNetworkInfo();
            }
            mService.get().networkState(networkInfo != null && networkInfo.isConnected());
        }
    }
}
