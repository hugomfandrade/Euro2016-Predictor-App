package org.hugoandrade.euro2016.predictor.admin.network;

import android.content.BroadcastReceiver;
import android.content.Context;
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

import org.hugoandrade.euro2016.predictor.admin.DevConstants;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.CloudDatabaseSimAdapter;
import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.League;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.WaitingLeagueUser;
import org.hugoandrade.euro2016.predictor.admin.model.helper.MobileServiceJsonTableHelper;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.admin.utils.NetworkBroadcastReceiverUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.NetworkUtils;

import java.net.MalformedURLException;

public class MobileServiceAdapter implements NetworkBroadcastReceiverUtils.INetworkBroadcastReceiver {

    @SuppressWarnings("unused")
    private static final String TAG = MobileServiceAdapter.class.getSimpleName();

    private static MobileServiceAdapter mInstance = null;

    private MobileServiceClient mClient = null;

    private MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    private BroadcastReceiver mNetworkBroadcastReceiver;

    private boolean mIsNetworkAvailable;

    public static MobileServiceAdapter getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("MobileServiceAdapter is not initialized");
        }
        return mInstance;
    }

    public static void Initialize(Context context) {
        if (mInstance == null) {
            mInstance = new MobileServiceAdapter(context);
        } else {
            throw new IllegalStateException("MobileServiceAdapter is already initialized");
        }
    }

    public static void unInitialize(Context context) {
        try {
            getInstance().destroy(context);
        } catch (IllegalStateException e) {
            Log.e(TAG, "unInitialize error: " + e.getMessage());
        }
    }

    private MobileServiceAdapter(Context context) {
        CloudDatabaseSimAdapter.Initialize(context);

        try {
            mClient = new MobileServiceClient(
                    DevConstants.appUrl,
                    DevConstants.appKey,
                    context);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mIsNetworkAvailable = NetworkUtils.isNetworkAvailable(context);
        mNetworkBroadcastReceiver = NetworkBroadcastReceiverUtils.register(context, this);
    }

    private void destroy(Context context) {
        if (mNetworkBroadcastReceiver != null) {
            NetworkBroadcastReceiverUtils.unregister(context, mNetworkBroadcastReceiver);
            mNetworkBroadcastReceiver = null;
        }
    }

    public void setMobileServiceUser(MobileServiceUser mobileServiceUser) {
        mClient.setCurrentUser(mobileServiceUser);
    }

    public MobileServiceCallback login(final LoginData loginData) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().login(callback, loginData) ||
                !isNetworkAvailable(callback, MobileServiceData.LOGIN))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(LoginData.Entry.API_NAME_LOGIN,
                        formatter.getAsJsonObject(loginData),
                        HttpConstants.PostMethod,
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.LOGIN, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLoginData(parser.parseLoginData(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.LOGIN, t.getMessage());
            }
        });
        return callback;
    }

    public MobileServiceCallback getSystemData() {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getSystemData(callback) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_SYSTEM_DATA))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(SystemData.Entry.API_NAME,
                        null,
                        HttpConstants.GetMethod,
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_SYSTEM_DATA, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.GET_SYSTEM_DATA, t.getMessage());
            }
        });
        return callback;
    }

    public MobileServiceCallback getMatches() {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getMatches(callback) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_MATCHES))
            return callback;

        ListenableFuture<JsonElement> futureCountries =  MobileServiceJsonTableHelper
                .instance(Match.Entry.TABLE_NAME, mClient)
                .execute();
        Futures.addCallback(futureCountries, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_MATCHES, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatchList(parser.parseMatchList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_MATCHES, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback getCountries() {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getCountries(callback) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_COUNTRIES))
            return callback;

        ListenableFuture<JsonElement> futureCountries =  MobileServiceJsonTableHelper
                .instance(Country.Entry.TABLE_NAME, mClient)
                .execute();
        Futures.addCallback(futureCountries, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_COUNTRIES, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountryList(parser.parseCountryList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_COUNTRIES, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback updateSystemData(final SystemData systemData) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().updateSystemData(callback, systemData) ||
                !isNetworkAvailable(callback, MobileServiceData.UPDATE_SYSTEM_DATA))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(SystemData.Entry.API_NAME,
                        formatter.getAsJsonObject(systemData),
                        HttpConstants.PostMethod,
                        null);
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_SYSTEM_DATA, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setSystemData(parser.parseSystemData(jsonElement.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.UPDATE_SYSTEM_DATA, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback updateCountry(Country country) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().updateCountry(callback, country) ||
                !isNetworkAvailable(callback, MobileServiceData.UPDATE_COUNTRY))
            return callback;

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                        .update(formatter.getAsJsonObject(country));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_COUNTRY, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountry(parser.parseCountry(result))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.UPDATE_COUNTRY, t.getMessage());
            }
        });
        return callback;
    }

    public MobileServiceCallback updateMatch(final Match match) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().updateMatch(callback, match) ||
                !isNetworkAvailable(callback, MobileServiceData.UPDATE_MATCH))
            return callback;

        ListenableFuture<JsonObject> future =
                new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient)
                        .update(formatter.getAsJsonObject(match));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_MATCH, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatch(parser.parseMatch(result))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_MATCH, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setMatch(match)
                        .setMessage(t.getMessage())
                        .create());
            }
        });
        return callback;
    }

    public MobileServiceCallback deleteCountry(final Country country) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().deleteCountry(callback, country) ||
                !isNetworkAvailable(callback, MobileServiceData.DELETE_COUNTRY))
            return callback;

        ListenableFuture<Void> future = new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                .delete(formatter.getAsJsonObject(country));

        Futures.addCallback(future, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        callback.set(MobileServiceData.Builder
                                .instance(MobileServiceData.DELETE_COUNTRY, MobileServiceData.REQUEST_RESULT_SUCCESS)
                                .setCountry(country)
                                .create());
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        callback.set(MobileServiceData.Builder
                                .instance(MobileServiceData.DELETE_COUNTRY, MobileServiceData.REQUEST_RESULT_FAILURE)
                                .setCountry(country)
                                .setMessage(t.getMessage())
                                .create());
                    }
                }
        );
        return callback;
    }

    public MobileServiceCallback deleteMatch(final Match match) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().deleteMatch(callback, match) ||
                !isNetworkAvailable(callback, MobileServiceData.DELETE_MATCH))
            return callback;

        ListenableFuture<Void> future = new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient)
                .delete(formatter.getAsJsonObject(match));

        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_MATCH, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatch(match)
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_MATCH, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setMatch(match)
                        .setMessage(t.getMessage())
                        .create());
            }
        });
        return callback;
    }

    public MobileServiceCallback insertCountry(final Country country) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().insertCountry(callback, country) ||
                !isNetworkAvailable(callback, MobileServiceData.INSERT_COUNTRY))
            return callback;

        ListenableFuture<JsonObject> future = new MobileServiceJsonTable(Country.Entry.TABLE_NAME, mClient)
                .insert(formatter.getAsJsonObject(country, Country.Entry.Cols.ID));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_COUNTRY, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountry(parser.parseCountry(jsonObject))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_COUNTRY, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setCountry(country)
                        .setMessage(t.getMessage())
                        .create());
            }
        });
        return callback;
    }

    public MobileServiceCallback insertMatch(final Match match) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().insertMatch(callback, match) ||
                !isNetworkAvailable(callback, MobileServiceData.INSERT_MATCH))
            return callback;

        ListenableFuture<JsonObject> future = new MobileServiceJsonTable(Match.Entry.TABLE_NAME, mClient)
                .insert(formatter.getAsJsonObject(match, Match.Entry.Cols.ID));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_MATCH, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatch(parser.parseMatch(jsonObject))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_MATCH, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setMatch(match)
                        .setMessage(t.getMessage())
                        .create());
            }
        });
        return callback;
    }

    public MobileServiceCallback updateScoresOfPredictions() {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().updateScoresOfPredictions(callback) ||
                !isNetworkAvailable(callback, MobileServiceData.UPDATE_SCORES_OF_PREDICTIONS))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(SystemData.Entry.API_NAME_UPDATE_SCORES,
                        null,
                        HttpConstants.PostMethod,
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_SCORES_OF_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.UPDATE_SCORES_OF_PREDICTIONS, t.getMessage());
            }
        });
        return callback;
    }

    public MobileServiceCallback createLeague(final League league) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().createLeague(callback, league) ||
                !isNetworkAvailable(callback, MobileServiceData.CREATE_LEAGUE))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(League.Entry.API_NAME_CREATE_LEAGUE,
                        formatter.getAsJsonObject(league),
                        HttpConstants.PostMethod,
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.CREATE_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLeague(parser.parseLeague(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.CREATE_LEAGUE, t.getMessage());
            }
        });
        return callback;
    }

    public MobileServiceCallback joinLeague(final WaitingLeagueUser waitingLeagueUser) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().joinLeague(callback, waitingLeagueUser) ||
                !isNetworkAvailable(callback, MobileServiceData.JOIN_LEAGUE))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(League.Entry.API_NAME_JOIN_LEAGUE,
                        formatter.getAsJsonObject(waitingLeagueUser),
                        HttpConstants.PostMethod,
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.JOIN_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLeague(parser.parseLeague(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.JOIN_LEAGUE, t.getMessage());
            }
        });
        return callback;
    }

    private static void sendErrorMessage(MobileServiceCallback callback, int requestCode, String errorMessage) {
        callback.set(MobileServiceData.Builder
                .instance(requestCode, MobileServiceData.REQUEST_RESULT_FAILURE)
                .setMessage(errorMessage)
                .create());
    }

    private boolean isNetworkAvailable(final MobileServiceCallback callback, int requestCode) {
        if (!mIsNetworkAvailable) {
            callback.set(MobileServiceData.Builder.instance(requestCode, MobileServiceData.REQUEST_RESULT_FAILURE)
                    .setMessage("No Network Connection")
                    .create());
        }
        return mIsNetworkAvailable;
    }

    @Override
    public void setNetworkAvailable(boolean isNetworkAvailable) {
        mIsNetworkAvailable = isNetworkAvailable;
    }
}
