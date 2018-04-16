package org.hugoandrade.euro2016.predictor.network;

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
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import org.hugoandrade.euro2016.predictor.DevConstants;
import org.hugoandrade.euro2016.predictor.cloudsim.CloudDatabaseSimAdapter;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.helper.MobileServiceJsonTableHelper;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.utils.NetworkBroadcastReceiverUtils;
import org.hugoandrade.euro2016.predictor.utils.NetworkUtils;

import java.net.MalformedURLException;

public class MobileServiceAdapter implements NetworkBroadcastReceiverUtils.INetworkBroadcastReceiver {

    @SuppressWarnings("unused")
    private static final String TAG = MobileServiceAdapter.class.getSimpleName();

    private static MobileServiceAdapter mInstance = null;

    private MobileServiceClient mClient = null;

    private MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    private BroadcastReceiver mNetworkBroadcastReceiver;

    private boolean mIsNetworkAvailable = false;

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
                    null,
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

    public MobileServiceCallback signUp(final LoginData loginData) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().signUp(callback, loginData) ||
                !isNetworkAvailable(callback, MobileServiceData.SIGN_UP))
            return callback;

        ListenableFuture<JsonElement> future =
                mClient.invokeApi(LoginData.Entry.API_NAME_REGISTER,
                        formatter.getAsJsonObject(loginData),
                        HttpConstants.PostMethod,
                        null);

        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.SIGN_UP, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLoginData(parser.parseLoginData(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                sendErrorMessage(callback, MobileServiceData.SIGN_UP, t.getMessage());
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

        ListenableFuture<JsonElement> futureCountries = MobileServiceJsonTableHelper
                .instance(Match.Entry.TABLE_NAME, mClient)
                .orderBy(Match.Entry.Cols.MATCH_NUMBER, QueryOrder.Ascending)
                .execute();
        Futures.addCallback(futureCountries, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                for (Match m : parser.parseMatchList(jsonElement)) {
                    Log.e(TAG, "Get Matches: " + Integer.toString(m.getMatchNumber()));
                }
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

    public MobileServiceCallback getUsers() {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getUsers(callback) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_USERS))
            return callback;

        ListenableFuture<JsonElement> future =
                //new MobileServiceJsonTable(User.Entry.TABLE_NAME, mClient)
                //.orderBy(User.Entry.Cols.SCORE, QueryOrder.Descending)
                MobileServiceJsonTableHelper.instance(User.Entry.TABLE_NAME, mClient)
                        .orderBy(User.Entry.Cols.SCORE, QueryOrder.Descending)
                        .execute();
        Futures.addCallback(future, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_USERS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setUserList(parser.parseUserList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_USERS, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback getPredictions(String userID) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getPredictions(callback, userID) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_PREDICTIONS))
            return callback;

        ListenableFuture<JsonElement> i = MobileServiceJsonTableHelper
                .instance(Prediction.Entry.TABLE_NAME, mClient)
                .where().field(Prediction.Entry.Cols.USER_ID).eq(userID)
                .execute();
        Futures.addCallback(i, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPredictionList(parser.parsePredictionList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback getPredictions(String userID, int firstMatchNumber, int lastMatchNumber) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getPredictions(callback, userID, firstMatchNumber, lastMatchNumber) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_PREDICTIONS))
            return callback;

        ListenableFuture<JsonElement> i = new MobileServiceJsonTable(Prediction.Entry.TABLE_NAME, mClient)
                .where().field(Prediction.Entry.Cols.USER_ID).eq(userID)
                .and().field(Prediction.Entry.Cols.MATCH_NO).ge(firstMatchNumber)
                .and().field(Prediction.Entry.Cols.MATCH_NO).le(firstMatchNumber)
                .execute();
        Futures.addCallback(i, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPredictionList(parser.parsePredictionList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback getPredictions(String[] users, int firstMatchNumber, int lastMatchNumber) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().getPredictions(callback, users, firstMatchNumber, lastMatchNumber) ||
                !isNetworkAvailable(callback, MobileServiceData.GET_PREDICTIONS))
            return callback;

        ListenableFuture<JsonElement> i = MobileServiceJsonTableHelper.instance(Prediction.Entry.TABLE_NAME, mClient)
                .where().field(Prediction.Entry.Cols.USER_ID).eq(users)
                .and().field(Prediction.Entry.Cols.MATCH_NO).ge(firstMatchNumber)
                .and().field(Prediction.Entry.Cols.MATCH_NO).le(lastMatchNumber)
                .execute();
        Futures.addCallback(i, new FutureCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPredictionList(parser.parsePredictionList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable.getMessage());
            }
        });

        return callback;
    }

    public MobileServiceCallback insertPrediction(final Prediction prediction) {

        final MobileServiceCallback callback = new MobileServiceCallback();

        if (CloudDatabaseSimAdapter.getInstance().insertPrediction(callback, prediction) ||
                !isNetworkAvailable(callback, MobileServiceData.INSERT_PREDICTION))
            return callback;

        ListenableFuture<JsonObject> future = new MobileServiceJsonTable(Prediction.Entry.TABLE_NAME, mClient)
                .insert(formatter.getAsJsonObject(prediction, Prediction.Entry.Cols.ID));
        Futures.addCallback(future, new FutureCallback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_PREDICTION, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPrediction(prediction)
                        .create());
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_PREDICTION, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setPrediction(prediction)
                        .setMessage(t.getMessage())
                        .create());
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
