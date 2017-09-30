package org.hugoandrade.euro2016.backend.model.service;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hugoandrade.euro2016.backend.cloudsim.CloudDatabaseSim;
import org.hugoandrade.euro2016.backend.DevConstants;
import org.hugoandrade.euro2016.backend.model.parser.MessageBase;
import org.hugoandrade.euro2016.backend.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.backend.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;

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

    private void setSystemData(final Messenger replyTo, final int requestCode, final SystemData systemData) {
        if (DevConstants.CLOUD_DATABASE_SIM) {
            CloudDatabaseSim.setSystemData(replyTo, requestCode, systemData);
            return;
        }

        if (mClient == null) {
            sendNoNetworkConnectionFailureMessage(replyTo, requestCode);
            return;
        }

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(SystemData.Entry.TABLE_NAME, mClient)
                        .update(formatter.getAsJsonObject(systemData));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage = MessageBase.makeMessage(
                        requestCode,
                        MessageBase.REQUEST_RESULT_SUCCESS
                );
                requestMessage.setSystemData(parser.parseSystemData(result));

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
                new MobileServiceJsonTable(SystemData.Entry.TABLE_NAME, mClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                final JsonArray results = result.getAsJsonArray();
                if (results.size() > 1) {
                    MessageBase requestMessage = MessageBase.makeMessage(
                            requestCode,
                            MessageBase.REQUEST_RESULT_SUCCESS
                    );

                    requestMessage.setSystemData(parser.parseSystemData(results.get(0).getAsJsonObject()));

                    //putParcelable(SYSTEM_DATA,
                      //      SystemData.getInstanceFromJsonObject(results.get(0).getAsJsonObject()));

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
                new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {

                MessageBase requestMessage = MessageBase.makeMessage(
                        requestCode,
                        MessageBase.REQUEST_RESULT_SUCCESS
                );

                requestMessage.setCountryList(parser.parseCountryList(result));

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
                new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient).top(60).execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {

                MessageBase requestMessage = MessageBase.makeMessage(
                        requestCode,
                        MessageBase.REQUEST_RESULT_SUCCESS
                );

                requestMessage.setMatchList(parser.parseMatchList(result));

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
                new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                        .update(formatter.getAsJsonObject(country));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage = MessageBase.makeMessage(
                        requestCode,
                        MessageBase.REQUEST_RESULT_SUCCESS
                );

                requestMessage.setCountry(parser.parseCountry(result));

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

    private void updateMatch(final Match match, final Messenger replyTo, final int requestCode) {
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
                MessageBase requestMessage = MessageBase.makeMessage(
                        requestCode,
                        MessageBase.REQUEST_RESULT_SUCCESS
                );

                requestMessage.setMatch(parser.parseMatch(result));

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
        MessageBase requestMessage = MessageBase.makeMessage(
                requestCode,
                MessageBase.REQUEST_RESULT_FAILURE
        );
        requestMessage.setErrorMessage(errorMessage);

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
            if (requestCode == MessageBase.OperationType.GET_ALL_COUNTRIES.ordinal()) {
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
            if (requestCode == MessageBase.OperationType.GET_ALL_MATCHES.ordinal()) {
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
            if (requestCode == MessageBase.OperationType.UPDATE_MATCH_UP.ordinal()) {
                final Match match = requestMessage.getMatch();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatch(
                                match,
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            if (requestCode == MessageBase.OperationType.UPDATE_MATCH_RESULT.ordinal()) {
                final Match match = requestMessage.getMatch();
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {
                        mService.get().updateMatch(
                                match,
                                messenger,
                                requestCode);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
            if (requestCode == MessageBase.OperationType.UPDATE_COUNTRY.ordinal()) {
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
            if (requestCode == MessageBase.OperationType.GET_SYSTEM_DATA.ordinal()) {
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
            if (requestCode == MessageBase.OperationType.SET_SYSTEM_DATA.ordinal()) {
                final SystemData systemData = requestMessage.getSystemData();
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
            if (networkInfo != null && networkInfo.isConnected())
                mService.get().networkState(true);

            else
                mService.get().networkState(false);
        }
    }
}
