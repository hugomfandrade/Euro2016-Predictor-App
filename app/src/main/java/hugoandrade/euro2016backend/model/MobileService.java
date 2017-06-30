package hugoandrade.euro2016backend.model;

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
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hugoandrade.euro2016backend.cloudsim.CloudDatabaseSim;
import hugoandrade.euro2016backend.DevConstants;
import hugoandrade.euro2016backend.common.MessageBase;
import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.object.Match;
import hugoandrade.euro2016backend.object.SystemData;

public class MobileService extends Service {

    @SuppressWarnings("unused") private static final String TAG = MobileService.class.getSimpleName();

    enum OperationType {
        @SuppressWarnings("unused") OPERATION_UNKNOWN,

        GET_ALL_COUNTRIES,
        GET_ALL_MATCHES,
        UPDATE_MATCH_UP,
        UPDATE_MATCH_RESULT,
        UPDATE_COUNTRY,
        GET_SYSTEM_DATA,
        SET_SYSTEM_DATA
    }

    // Data Extras Key
    public static final String REQUEST_RESULT_SUCCESS = "REQUEST_RESULT_SUCCESS";
    public static final String REQUEST_RESULT_FAILURE = "REQUEST_RESULT_FAILURE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String ALL_MATCHES_DATA = "ALL_MATCHES_DATA";
    public static final String ALL_COUNTRIES_DATA = "ALL_COUNTRIES_DATA";
    public static final String SYSTEM_DATA = "SYSTEM_DATA";
    public static final String MATCH_DATA = "MATCH_DATA";
    public static final String COUNTRY_DATA = "COUNTRY_DATA";

    private Messenger mRequestMessenger = null;
    private RequestHandler mRequestHandler = null;
    private MobileServiceClient mClient = null;


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

    private void setSystemData(final Messenger replyTo, final int requestCode, final SystemData systemData) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.setSystemData(replyTo, requestCode, systemData);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(SystemData.TABLE_NAME, mClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                final JsonArray results = result.getAsJsonArray();
                if (results.size() > 1) {
                    MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                    requestMessage.putParcelable(SYSTEM_DATA,
                            SystemData.getInstanceFromJsonObject(results.get(0).getAsJsonObject()));

                    try {
                        replyTo.send(requestMessage.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "Exception while sending message back to Activity.", e);
                    }
                } else {
                    sendErrorMessage(replyTo, requestCode, "Could not retrieve SystemData");
                }
            }

            @Override
            public void onFailure(Throwable t) {
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
                new MobileServiceJsonTable(SystemData.TABLE_NAME, mClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                final JsonArray results = result.getAsJsonArray();
                if (results.size() > 1) {
                    MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                    requestMessage.putParcelable(SYSTEM_DATA,
                            SystemData.getInstanceFromJsonObject(results.get(0).getAsJsonObject()));

                    try {
                        replyTo.send(requestMessage.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "Exception while sending message back to Activity.", e);
                    }
                } else {
                    sendErrorMessage(replyTo, requestCode, "Could not retrieve SystemData");
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

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(Country.TABLE_NAME, mClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                ArrayList<Country> allCountryList = new ArrayList<>();
                for (JsonElement item : result.getAsJsonArray())
                    allCountryList.add(Country.getInstanceFromJsonObject(item.getAsJsonObject()));

                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelableArrayList(ALL_COUNTRIES_DATA, allCountryList);

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

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonElement> future =
                new MobileServiceJsonTable(Match.TABLE_NAME, mClient).top(60).execute();
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
                new MobileServiceJsonTable(Country.TABLE_NAME, mClient).update(country.getAsJsonObject());
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelable(COUNTRY_DATA, Country.getInstanceFromJsonObject(result));

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

    private void updateMatchUp(final Match match, final Messenger replyTo, final int requestCode) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.updateMatchUp(match, replyTo, requestCode);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(Match.TABLE_NAME, mClient).update(match.getAsJsonObject());
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelable(MATCH_DATA, Match.getInstanceFromJsonObject(result));

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
            if (requestCode == OperationType.GET_ALL_COUNTRIES.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getAllCountries(
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            if (requestCode == OperationType.GET_ALL_MATCHES.ordinal()) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().getAllMatches(
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            if (requestCode == OperationType.UPDATE_MATCH_UP.ordinal()) {
                final Match match = requestMessage.getParcelable(MATCH_DATA);
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatchUp(
                                match,
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            if (requestCode == OperationType.UPDATE_MATCH_RESULT.ordinal()) {
                final Match match = requestMessage.getParcelable(MATCH_DATA);
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatchUp(
                                match,
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            if (requestCode == OperationType.UPDATE_COUNTRY.ordinal()) {
                final Country country = requestMessage.getParcelable(COUNTRY_DATA);
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
            if (requestCode == OperationType.GET_SYSTEM_DATA.ordinal()) {
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
            if (requestCode == OperationType.SET_SYSTEM_DATA.ordinal()) {
                final SystemData systemData = requestMessage.getParcelable(SYSTEM_DATA);
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().setSystemData(
                                messenger,
                                requestCode,
                                systemData);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
        }

        void shutdown() {
            mExecutorService.shutdown();
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
            if (networkInfo != null && networkInfo.isConnected()) {
                mService.get().networkState(true);
                return;
            }
            mService.get().networkState(false);
        }
    }
}
