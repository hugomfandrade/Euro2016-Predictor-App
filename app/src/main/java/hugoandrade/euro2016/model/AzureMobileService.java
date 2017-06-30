package hugoandrade.euro2016.model;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hugoandrade.euro2016.CloudDatabaseSim;
import hugoandrade.euro2016.DevConstants;
import hugoandrade.euro2016.common.MessageBase;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.utils.NetworkUtils;

public class AzureMobileService extends Service {

    private final String TAG = getClass().getSimpleName();

    public static final String NETWORK_STATE = "NETWORK_STATE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";

    public static final String USER_DATA = "UserData";
    public static final String LOGIN_DATA = "LoginData";
    public static final String ALL_COUNTRIES_DATA = "AllCountriesData";
    public static final String ALL_USERS_DATA = "AllUsersData";
    public static final String ALL_PREDICTIONS_DATA = "AllPredictionData";
    public static final String ALL_MATCHES_DATA = "AllMatchesData";
    public static final String PREDICTION_DATA = "Prediction";
    public static final String SYSTEM_DATA = "SystemData";
    public static final String IS_PAST_SERVER_TIME = "IsPastServerTime";
    public static final String NEW_SERVER_TIME = "NewServerTime";

    public static final String REQUEST_RESULT_FAILURE = "FAILURE";
    public static final String REQUEST_RESULT_SUCCESS = "SUCCESS";

    enum OperationType {
        @SuppressWarnings("unused") OPERATION_UNKNOWN,

        LOGIN,
        REGISTER,
        ALL_MATCHES,
        ALL_PREDICTIONS,
        PUT_PREDICTION,
        SYSTEM_TIME,
        NOTIFY_NETWORK_CHANGE,
        ALL_COUNTRIES,
        ALL_USERS
    }

    private RequestHandler mRequestHandler;
    private Messenger mRequestMessenger;

    private MobileServiceClient mMobileServiceClient;
    private HashSet<Messenger> mClients = new HashSet<>();

    public static Intent makeIntent(Context context) {
        return new Intent(context, AzureMobileService.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        super.onCreate();
        mRequestHandler = new RequestHandler(this);
        mRequestMessenger = new Messenger(mRequestHandler);

        initializeMobileServiceClient();
        initializeNetworkBroadcastReceiver();
        initializeCloudDatabaseSim();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, Thread.currentThread().getStackTrace()[2].getMethodName());
        return mRequestMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRequestHandler.shutdown();
    }

    private void initializeCloudDatabaseSim() {
        CloudDatabaseSim.initialize(getContentResolver());
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
                        DevConstants.appKey,
                        this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAllUsers(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.getAllUsers(replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }
        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(User.TABLE_NAME, mMobileServiceClient)
                        .select(User.COL_NAME_USERNAME, User.COL_NAME_SCORE).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                ArrayList<User> userList = new ArrayList<>();

                for (JsonElement item : result.getAsJsonArray())
                    userList.add(User.instanceFromJsonObject(item.getAsJsonObject()));

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelableArrayList(ALL_USERS_DATA, userList);

                try {
                    replyTo.send(requestMessage.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message back to Activity.", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void getAllPredictions(final String userID, int matchNo, final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.getAllPredictions(userID, matchNo, replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(Prediction.TABLE_NAME, mMobileServiceClient)
                        .where().field(Prediction.COL_NAME_USER_ID).eq(userID).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                ArrayList<Prediction> predictionList = new ArrayList<>();

                for (JsonElement item : result.getAsJsonArray())
                    predictionList.add(Prediction.getInstanceFromJsonObject(item.getAsJsonObject()));

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putString(Prediction.COL_NAME_USER_ID, userID);
                requestMessage.putParcelableArrayList(ALL_PREDICTIONS_DATA, predictionList);

                try {
                    replyTo.send(requestMessage.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message back to Activity.", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void putPrediction(final Prediction prediction, final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.putPrediction(prediction, replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(Prediction.TABLE_NAME, mMobileServiceClient).where()
                        .field(Prediction.COL_NAME_USER_ID).eq(prediction.userID).and()
                        .field(Prediction.COL_NAME_MATCH_NO).eq(prediction.matchNo).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(final JsonElement jsonElement) {
                if (jsonElement.getAsJsonArray().size() == 0) {
                    JsonObject newPrediction = prediction.getAsJsonObject();
                    ListenableFuture<JsonObject> future =
                            new MobileServiceJsonTable(Prediction.TABLE_NAME, mMobileServiceClient)
                                    .insert(newPrediction);
                    Futures.addCallback(future, new FutureCallback<JsonObject>() {
                        @Override
                        public void onSuccess(JsonObject result) {
                            MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                            requestMessage.putBoolean(IS_PAST_SERVER_TIME,
                                    NetworkUtils.getJsonPrimitive(result, IS_PAST_SERVER_TIME, false));
                            requestMessage.putParcelable(PREDICTION_DATA,
                                    Prediction.getInstanceFromJsonObject(result));

                            try {
                                replyTo.send(requestMessage.getMessage());
                            } catch (Exception e) {
                                Log.e(TAG, "Exception while sending message back to Activity.", e);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            sendErrorMessage(replyTo, requestCode, t.getMessage());
                        }
                    });
                } else if (jsonElement.getAsJsonArray().size() == 1) {
                    JsonObject updatePrediction = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
                    ListenableFuture<JsonObject> future =
                            new MobileServiceJsonTable(Prediction.TABLE_NAME, mMobileServiceClient)
                                    .update(updatePrediction);
                    Futures.addCallback(future, new FutureCallback<JsonObject>() {
                        @Override
                        public void onSuccess(JsonObject result) {
                            MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                            requestMessage.putBoolean(IS_PAST_SERVER_TIME,
                                    NetworkUtils.getJsonPrimitive(result, IS_PAST_SERVER_TIME, false));
                            requestMessage.putParcelable(PREDICTION_DATA,
                                    Prediction.getInstanceFromJsonObject(result));

                            try {
                                replyTo.send(requestMessage.getMessage());
                            } catch (Exception e) {
                                Log.e(TAG, "Exception while sending message back to Activity.", e);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            sendErrorMessage(replyTo, requestCode, t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }

        });
    }

    private void getAllCountries(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.getAllCountries(replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(Country.TABLE_NAME, mMobileServiceClient).top(100).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                ArrayList<Country> countryList = new ArrayList<>();
                for (JsonElement item : result.getAsJsonArray())
                    countryList.add(Country.getInstanceFromJsonObject(item.getAsJsonObject()));

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelableArrayList(ALL_COUNTRIES_DATA, countryList);

                try {
                    replyTo.send(requestMessage.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message back to Activity.", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void getAllMatches(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.getAllMatches(replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(Match.TABLE_NAME, mMobileServiceClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                ArrayList<Match> allMatchesList = new ArrayList<>();
                for (JsonElement item : result.getAsJsonArray())
                    allMatchesList.add(Match.getInstanceFromJsonObject(item.getAsJsonObject()));

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelableArrayList(ALL_MATCHES_DATA, allMatchesList);

                try {
                    replyTo.send(requestMessage.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message back to Activity.", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void requestSystemTime(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.requestSystemTime(replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }
        ArrayList<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>(SystemData.REQUEST_TYPE, SystemData.SYSTEM_TIME));

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(SystemData.TABLE_NAME, mMobileServiceClient)
                        .insert(null, parameters);
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelable(SYSTEM_DATA,
                        SystemData.getInstanceFromJsonObject(jsonObject));

                try {
                    replyTo.send(requestMessage.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message back to Activity.", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void loginOrSignUp(String operationType, final LoginData loginData, final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.loginOrSignUp(operationType, loginData, replyTo, requestCode);
            return;
        }

        if (mMobileServiceClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ArrayList<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>(LoginData.REQUEST_TYPE, operationType));

        JsonObject newUser = loginData.getAsJsonObject();
        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(LoginData.TABLE_NAME, mMobileServiceClient)
                        .insert(newUser, parameters);
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelable(USER_DATA,
                        User.instanceFromJsonObject(jsonObject));

                try {
                    replyTo.send(requestMessage.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "Exception while sending message back to Activity.", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void sendNoNetworkConnectionFailureMessage(Messenger replyTo, int requestCode) {
        sendErrorMessage(replyTo, requestCode, "No Network Connection");
    }

    private void sendErrorMessage(Messenger replyTo, int requestCode, String errorMessage) {
        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_FAILURE);
        requestMessage.putString(ERROR_MESSAGE, errorMessage);

        try {
            replyTo.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }

    private static class RequestHandler extends Handler {

        @SuppressWarnings("unused") private final String TAG = getClass().getSimpleName();

        private WeakReference<AzureMobileService> mService;
        private ExecutorService mExecutorService;

        RequestHandler(AzureMobileService service) {
            mService = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message) {
            final MessageBase requestMessage = MessageBase.makeMessage(message);
            final Messenger messenger = requestMessage.getMessenger();

            mService.get().mClients.add(messenger);

            final int requestCode = requestMessage.getRequestCode();
            if (requestCode == OperationType.LOGIN.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().loginOrSignUp(
                                LoginData.OPERATION_TYPE_LOGIN,
                                (LoginData) requestMessage.getParcelable(AzureMobileService.LOGIN_DATA),
                                messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.REGISTER.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().loginOrSignUp(
                                LoginData.OPERATION_TYPE_SIGN_UP,
                                (LoginData) requestMessage.getParcelable(AzureMobileService.LOGIN_DATA),
                                messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.SYSTEM_TIME.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().requestSystemTime(messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.ALL_COUNTRIES.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getAllCountries(messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.ALL_MATCHES.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getAllMatches(messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.ALL_PREDICTIONS.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getAllPredictions(
                                requestMessage.getString(Prediction.COL_NAME_USER_ID),
                                requestMessage.getInt(Prediction.COL_NAME_MATCH_NO),
                                messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.ALL_USERS.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getAllUsers(messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == OperationType.PUT_PREDICTION.ordinal()) {
                final Prediction prediction = (Prediction) requestMessage.getParcelable(PREDICTION_DATA);
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().putPrediction(prediction, messenger, requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
        }

        void shutdown() {
            mExecutorService.shutdown();
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
        notifyBoundActivityOfNetworkStateChanged(state);
    }

    private void notifyBoundActivityOfNetworkStateChanged(boolean state) {
        MessageBase requestMessage = MessageBase.makeMessage(
                OperationType.NOTIFY_NETWORK_CHANGE.ordinal(), REQUEST_RESULT_SUCCESS);
        requestMessage.putBoolean(NETWORK_STATE, state);
        for (Messenger mClient : mClients) {
            try {
                mClient.send(requestMessage.getMessage());
            } catch (RemoteException e) {
                mClients.remove(mClient);
                Log.e(TAG, "Exception while sending message back to Activity.", e);
            }
        }
    }

    private class ConnectionStateBroadcastReceiver extends BroadcastReceiver {
        private WeakReference<AzureMobileService> mService;

        public ConnectionStateBroadcastReceiver(AzureMobileService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                mService.get().networkState(true);
                return;
            }
            mService.get().networkState(false);
        }
    }
}
