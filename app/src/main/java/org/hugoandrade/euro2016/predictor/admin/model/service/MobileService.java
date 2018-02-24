package org.hugoandrade.euro2016.predictor.admin.model.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSim;
import org.hugoandrade.euro2016.predictor.admin.DevConstants;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MessageBase;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.utils.InitConfigUtils;

public class MobileService extends Service {

    @SuppressWarnings("unused") private static final String TAG = MobileService.class.getSimpleName();

    private Messenger mRequestMessenger = null;
    private RequestHandler mRequestHandler = null;
    private MobileServiceClient mClient = null;

    private MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    public static Intent makeIntent(Context activityContext) {
        return new Intent(activityContext, MobileService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRequestHandler = new RequestHandler(this);
        mRequestMessenger = new Messenger(mRequestHandler);

        subscribeToBroadcastReceiver();
        initializeMobileServiceClient();
        initializeCloudDatabaseSim();
    }

    private void initializeCloudDatabaseSim() {
        CloudDatabaseSim.initialize(getContentResolver());
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
        mRequestHandler.shutdown();
    }

    private void subscribeToBroadcastReceiver() {
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
                mClient = new MobileServiceClient(
                        DevConstants.appUrl,
                        DevConstants.appKey,
                        this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void networkState(boolean state) {
        if (state) {
            if (mClient == null)
                initializeMobileServiceClient();
        }
        else
            mClient = null;
    }

    private void login(final Messenger replyTo, final int requestCode, final LoginData loginData) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.login(replyTo, requestCode, loginData);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(LoginData.Entry.API_NAME_LOGIN,
                        formatter.getAsJsonObject(loginData),
                        "POST",
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                LoginData data = parser.parseLoginData(jsonObject.getAsJsonObject());
                data.setPassword(loginData.getPassword());
                requestMessage.setLoginData(data);

                MobileServiceUser mMobileServiceUser = new MobileServiceUser(data.getUserID());
                mMobileServiceUser.setAuthenticationToken(data.getToken());
                mClient.setCurrentUser(mMobileServiceUser);

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void getSystemData(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.getSystemData(replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(SystemData.Entry.API_NAME,
                        null,
                        "GET",
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                if (t.getMessage().equals("Error while processing request.")){
                    getSystemData(replyTo, requestCode);
                    return;
                }
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void updateSystemData(final Messenger replyTo, final int requestCode, final SystemData systemData) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.setSystemData(replyTo, requestCode, systemData);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(SystemData.Entry.API_NAME,
                        formatter.getAsJsonObject(systemData),
                        "POST",
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void reset(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            //CloudDatabaseSim.reset(replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        new ResetCloudAsyncTask(mClient)
                .setOnFinishedListener(new ResetCloudAsyncTask.OnFinished() {
                    @Override
                    public void onError(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }

                    @Override
                    public void onSuccess(ArrayList<Country> countryList, ArrayList<Match> matchList) {
                        getInfo(replyTo, requestCode);
                    }
                })
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateScoresOfPredictions(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            //CloudDatabaseSim.updateScoresOfPredictions(replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }


        ListenableFuture<JsonElement> future =
                mClient.invokeApi("UpdateScoresOfPredictions",
                        null,
                        "POST",
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void getInfo(final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.getInfo(replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        final MessageBase requestMessage = MessageBase.makeMessage(
                requestCode,
                MessageBase.REQUEST_RESULT_SUCCESS);

        final int[] n = {
                0 /* completed operations */,
                2, //3 /* total operations */,
                1/* isOk flag */};

        ListenableFuture<JsonElement> futureCountries =  MobileServiceJsonTableHelper
                .instance(Country.Entry.TABLE_NAME, mClient)
                .execute();
        Futures.addCallback(futureCountries, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.d(TAG, "Countries fetched");
                if (n[2] == 0) return; // An error occurred

                requestMessage.setCountryList(parser.parseCountryList(jsonElement));

                n[0]++;
                if (n[0] == n[1])
                    sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                if (n[2] == 0) return; // An error occurred

                n[2] = 0;
                sendErrorMessage(replyTo, requestCode, throwable.getMessage());
            }
        });

        ListenableFuture<JsonElement> futureMatches =  MobileServiceJsonTableHelper
                .instance(Match.Entry.TABLE_NAME, mClient)
                .execute();
        Futures.addCallback(futureMatches, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.d(TAG, "Matches fetched");
                if (n[2] == 0) return; // An error occurred

                ArrayList<Match> matchList = parser.parseMatchList(jsonElement);
                Collections.sort(matchList);
                requestMessage.setMatchList(matchList);

                n[0]++;
                if (n[0] == n[1])
                    sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                if (n[2] == 0) return; // An error occurred

                n[2] = 0;
                sendErrorMessage(replyTo, requestCode, throwable.getMessage());
            }
        });
    }

    private void updateCountry(Country country, final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.updateCountry(country, replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                        .update(formatter.getAsJsonObject(country));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setCountry(parser.parseCountry(result));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(replyTo, requestCode, t.getMessage());
            }
        });
    }

    private void updateMatch(final Messenger replyTo, final int requestCode, final Match match) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.updateMatch(match, replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient)
                        .update(formatter.getAsJsonObject(match));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setMatch(parser.parseMatch(result));
                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_FAILURE);
                requestMessage.setMatch(match);
                requestMessage.setErrorMessage(t.getMessage());
                sendRequestMessage(replyTo, requestMessage);
            }
        });
    }

    private void sendNoNetworkConnectionFailureMessage(Messenger replyTo, int requestCode) {
        sendErrorMessage(replyTo, requestCode, "No Network Connection");
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

        private WeakReference<MobileService> mService;
        private ExecutorService mExecutorService;

        RequestHandler(MobileService service) {
            mService = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            final MessageBase requestMessage = MessageBase.makeMessage(message);
            final Messenger messenger = requestMessage.getMessenger();

            final int requestCode = requestMessage.getRequestCode();

            if (requestCode == MessageBase.OperationType.RESET.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().reset(
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_SCORES_OF_PREDICTIONS.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateScoresOfPredictions(
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.GET_INFO.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getInfo(
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_UP.ordinal()) {
                final Match match = requestMessage.getMatch();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatch(
                                messenger,
                                requestCode,
                                match);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal()) {
                final Match match = requestMessage.getMatch();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatch(
                                messenger,
                                requestCode,
                                match);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_COUNTRY.ordinal()) {
                final Country country = requestMessage.getCountry();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateCountry(
                                country,
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.UPDATE_SYSTEM_DATA.ordinal()) {
                final SystemData systemData = requestMessage.getSystemData();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateSystemData(
                                messenger,
                                requestCode,
                                systemData);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.GET_SYSTEM_DATA.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getSystemData(
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            else if (requestCode == MessageBase.OperationType.LOGIN.ordinal()) {
                final LoginData loginData = requestMessage.getLoginData();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().login(
                                messenger,
                                requestCode,
                                loginData);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
        }

        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private static class ResetCloudAsyncTask extends AsyncTask<Void, Void, ListenableFuture<Boolean>> {

        private static int DELETE_COUNTRY_TABLE = 1;
        private static int POPULATE_COUNTRY_TABLE = 2;
        private static int DELETE_MATCH_TABLE = 3;
        private static int POPULATE_MATCH_TABLE = 4;
        private static int POPULATE_SYSTEM_DATE = 5;

        private final MobileServiceClient mClient;
        private MHandler mHandler;

        private MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
        private MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

        private OnFinished mListener;
        private ArrayList<Country> mCountryList = new ArrayList<>();
        private ArrayList<Match> mMatchList = new ArrayList<>();

        private SettableFuture<Boolean> mFuture;

        ResetCloudAsyncTask(MobileServiceClient client) {
            mClient = client;
        }

        ResetCloudAsyncTask setOnFinishedListener(OnFinished listener) {
            mListener = listener;
            return this;
        }

        @Override
        protected ListenableFuture<Boolean> doInBackground(Void... aVoid) {
            mHandler = new MHandler(this, Looper.getMainLooper());
            sendMessage(DELETE_COUNTRY_TABLE);

            mFuture = SettableFuture.create();
            return mFuture;
        }

        @Override
        protected void onPostExecute(ListenableFuture<Boolean> future) {
            Futures.addCallback(future, new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (mListener != null)
                        mListener.onSuccess(mCountryList, mMatchList);
                }

                @Override
                public void onFailure(@NonNull Throwable throwable) {
                    if (mListener != null)
                        mListener.onError(throwable.getMessage());
                }
            });

        }

        private void sendMessage(int what) {
            mHandler.obtainMessage(what).sendToTarget();
        }

        private void deleteCountryTable() {
            Log.d(TAG, "start to delete Country Table");
            try {
                JsonArray jsonArray = MobileServiceJsonTableHelper.instance(Country.Entry.TABLE_NAME, mClient).execute()
                        .get().getAsJsonArray();

                if (jsonArray.size() == 0){
                    Log.d(TAG, "Country Table was empty");
                    sendMessage(POPULATE_COUNTRY_TABLE);
                }

                final int[] n = {0, jsonArray.size(), 1};
                for (int i = 0 ; i < jsonArray.size() ; i++) {
                    Futures.addCallback(
                            new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                                    .delete(jsonArray.get(i).getAsJsonObject()),
                            new FutureCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.w(TAG, "country deleted: " + Integer.toString(n[0]));
                                    if (n[2] == 0) {
                                        Log.e(TAG, "Operation was aborted");
                                        return;
                                    }

                                    n[0]++;
                                    if (n[0] == n[1]) {
                                        Log.d(TAG, "deletion of Country Table successful");
                                        sendMessage(POPULATE_COUNTRY_TABLE);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Throwable throwable) {
                                    n[2] = 0;

                                    String errorMessage = "Exception deleting Country: " + throwable.getMessage();

                                    showErrorAndAbortMessage(errorMessage);
                                }
                            }
                    );
                }
            } catch (InterruptedException | ExecutionException  e) {
                String errorMessage = "Exception deleting Country (query all): " + e.getMessage();

                showErrorAndAbortMessage(errorMessage);
            }
        }

        private void populateCountryTable() {
            Log.d(TAG, "start to populate Country Table");
            List<Country> countries = InitConfigUtils.buildInitCountryList();

            mCountryList = new ArrayList<>();
            final int[] n = {0, countries.size(), 1};

            for (Country c : countries) {
                ListenableFuture<JsonObject> future = new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                        .insert(formatter.getAsJsonObject(c, Country.Entry.Cols.ID));
                Futures.addCallback(future, new FutureCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject jsonObject) {
                        Log.w(TAG, "country inserted: " + Integer.toString(n[0]));
                        if (n[2] == 0)
                            return;

                        n[0]++;
                        mCountryList.add(parser.parseCountry(jsonObject));

                        if (n[0] == n[1]) {
                            Log.d(TAG, "populate of Country Table successful");
                            sendMessage(DELETE_MATCH_TABLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Throwable throwable) {
                        n[2] = 0;

                        String errorMessage = "Exception populating Country: " + throwable.getMessage();

                        Log.e(TAG, errorMessage);
                        if (mListener != null)
                            mListener.onError(errorMessage);
                    }
                });
            }
        }

        private void deleteMatchTable() {
            Log.d(TAG, "start to delete Match Table");

            try {
                JsonArray jsonArray = MobileServiceJsonTableHelper.instance(Match.Entry.TABLE_NAME, mClient).execute()
                        .get().getAsJsonArray();

                if (jsonArray.size() == 0){
                    Log.d(TAG, "Country Table was empty");
                    sendMessage(POPULATE_MATCH_TABLE);
                }

                final int[] n = {0, jsonArray.size(), 1};
                for (int i = 0 ; i < jsonArray.size() ; i++) {
                    Futures.addCallback(
                            new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient)
                                    .delete(jsonArray.get(i).getAsJsonObject()),
                            new FutureCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.w(TAG, "match deleted: " + Integer.toString(n[0]));
                                    if (n[2] == 0) {
                                        Log.e(TAG, "Operation was aborted");
                                        return;
                                    }

                                    n[0]++;
                                    if (n[0] == n[1]) {
                                        Log.d(TAG, "deletion of Match Table successful");
                                        sendMessage(POPULATE_MATCH_TABLE);
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Throwable throwable) {
                                    n[2] = 0;

                                    String errorMessage = "Exception deleting Match: " + throwable.getMessage();

                                    showErrorAndAbortMessage(errorMessage);
                                }
                            }
                    );
                }

            } catch (InterruptedException | ExecutionException e) {
                String errorMessage = "Exception deleting Match (query all): " + e.getMessage();

                showErrorAndAbortMessage(errorMessage);
            }
        }

        private void populateMatchTable() {
            Log.d(TAG, "start to populate Match Table");
            List<Match> matches = InitConfigUtils.buildInitMatchList(mCountryList);

            final int[] n = {0, matches.size(), 1};

            for (Match m : matches) {
                ListenableFuture<JsonObject> future = new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient)
                        .insert(formatter.getAsJsonObject(m, Match.Entry.Cols.ID));
                Futures.addCallback(future, new FutureCallback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject jsonObject) {
                        Log.w(TAG, "match inserted: " + Integer.toString(n[0]));
                        if (n[2] == 0) {
                            Log.e(TAG, "Operation was aborted");
                            return;
                        }

                        n[0]++;
                        mMatchList.add(parser.parseMatch(jsonObject));

                        if (n[0] == n[1]) {
                            sendMessage(POPULATE_SYSTEM_DATE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Throwable throwable) {
                        n[2] = 0;

                        String errorMessage = "Exception populating Match: " + throwable.getMessage();

                        showErrorAndAbortMessage(errorMessage);
                    }
                });
            }
        }

        private void populateSystemDate() {
            Log.d(TAG, "start to populate System Date");
            ListenableFuture<JsonElement> future =
                    mClient.invokeApi(SystemData.Entry.API_NAME,
                            formatter.getAsJsonObject(InitConfigUtils.buildInitSystemData()),
                            "POST",
                            null);

            Futures.addCallback(future, new FutureCallback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonObject) {
                    successfulOperation();
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    showErrorAndAbortMessage(t.getMessage());
                }
            });

            successfulOperation();
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
            void onSuccess(ArrayList<Country> countryList, ArrayList<Match> matchList);
        }

        private static class MHandler extends Handler {

            private WeakReference<ResetCloudAsyncTask> mBackgroundTask;
            private ExecutorService mExecutorService;

            MHandler(ResetCloudAsyncTask backgroundTask, Looper looper) {
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
                            mBackgroundTask.get().deleteCountryTable();
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
                            mBackgroundTask.get().populateMatchTable();
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

    public class ConnectionStateBroadcastReceiver extends BroadcastReceiver {

        private WeakReference<MobileService> mService;

        public ConnectionStateBroadcastReceiver(MobileService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                mService.get().networkState(true);

            else
                mService.get().networkState(false);
        }
    }
}
