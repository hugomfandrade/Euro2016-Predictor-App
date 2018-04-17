package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.DevConstants;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.network.HttpConstants;
import org.hugoandrade.euro2016.predictor.network.MobileServiceCallback;
import org.hugoandrade.euro2016.predictor.network.MobileServiceData;
import org.hugoandrade.euro2016.predictor.network.MultipleCloudStatus;

import java.util.ArrayList;
import java.util.List;

public class CloudDatabaseSimAdapter {

    @SuppressWarnings("unused")
    private final static String TAG = CloudDatabaseSimAdapter.class.getSimpleName();

    private static CloudDatabaseSimAdapter mInstance = null;
    private final Object syncObj = new Object();

    // org name in java package format
    private static final String ORGANIZATIONAL_NAME = "org.hugoandrade";
    // name of this provider's project
    private static final String PROJECT_NAME = "euro2016.predictor";

    private static final String AUTHORITY = ORGANIZATIONAL_NAME + "." + PROJECT_NAME + ".sim_provider";

    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private ContentProviderClient mContentProviderClient;

    private MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    public static CloudDatabaseSimAdapter getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("CloudDatabaseSimAdapter is not initialized");
        }
        return mInstance;
    }

    public static void Initialize(Context context) {
        if (mInstance == null) {
            mInstance = new CloudDatabaseSimAdapter(context, BASE_URI);
        } else {
            throw new IllegalStateException("CloudDatabaseSimAdapter is already initialized");
        }
    }

    private CloudDatabaseSimAdapter(Context context, Uri url) {
        mContentProviderClient = context.getContentResolver().acquireContentProviderClient(url);
    }

    public boolean login(final MobileServiceCallback callback, final LoginData loginData) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        LoginData.Entry.API_NAME_LOGIN,
                        formatter.getAsJsonObject(loginData),
                        HttpConstants.PostMethod,
                        mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                Log.e(TAG, "login: " + result.toString());
                LoginData data = parser.parseLoginData(result.getAsJsonObject());
                data.setPassword(loginData.getPassword());

                Log.e(TAG, "login: " + data.toString());

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.LOGIN, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLoginData(data)
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.LOGIN, errorMessage);
            }
        });
        return true;
    }

    public boolean signUp(final MobileServiceCallback callback, final LoginData loginData) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        LoginData.Entry.API_NAME_REGISTER,
                        formatter.getAsJsonObject(loginData),
                        HttpConstants.PostMethod,
                        mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.SIGN_UP, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLoginData(parser.parseLoginData(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.SIGN_UP, errorMessage);
            }
        });
        return true;
    }

    public boolean getSystemData(final MobileServiceCallback callback) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.Entry.API_NAME, null,
                HttpConstants.GetMethod,
                mContentProviderClient)
                .execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_SYSTEM_DATA,
                                  MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.GET_SYSTEM_DATA, errorMessage);
            }
        });
        return true;
    }

    public boolean getMatches(final MobileServiceCallback callback) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME, mContentProviderClient)
                .orderBy(Match.Entry.Cols.MATCH_NUMBER, CloudDatabaseSimImpl.SortOrder.Ascending)
                .execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_MATCHES, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatchList(parser.parseMatchList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull String throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_MATCHES, throwable);
            }
        });

        return true;
    }

    public boolean getCountries(final MobileServiceCallback callback) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> fCountry
                = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME, mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(fCountry, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_COUNTRIES, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountryList(parser.parseCountryList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull String throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_COUNTRIES, throwable);
            }
        });

        return true;
    }

    public boolean getUsers(final MobileServiceCallback callback) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                = new CloudDatabaseSimImpl(User.Entry.TABLE_NAME, mContentProviderClient)
                .orderBy(User.Entry.Cols.SCORE, CloudDatabaseSimImpl.SortOrder.Descending).execute();
        CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_USERS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setUserList(parser.parseUserList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull String throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_USERS, throwable);
            }
        });

        return true;
    }

    public boolean getPredictions(final MobileServiceCallback callback, String userID) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        Log.e(TAG, "getPredictions: " + userID);

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                = new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME, mContentProviderClient)
                .where().field(Prediction.Entry.Cols.USER_ID).eq(userID)
                .execute();
        CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPredictionList(parser.parsePredictionList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull String throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable);
            }
        });

        return true;
    }

    public boolean getPredictions(final MobileServiceCallback callback, String userID, int firstMatchNumber, int lastMatchNumber) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                = new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME, mContentProviderClient)
                .where().field(Prediction.Entry.Cols.USER_ID).eq(userID)
                .and().field(Prediction.Entry.Cols.MATCH_NO).ge(firstMatchNumber)
                .and().field(Prediction.Entry.Cols.MATCH_NO).le(lastMatchNumber)
                .execute();
        CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPredictionList(parser.parsePredictionList(jsonElement))
                        .create());
            }

            @Override
            public void onFailure(@NonNull String throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable);
            }
        });

        return true;
    }

    public boolean getPredictions(final MobileServiceCallback callback, String[] users, int firstMatchNumber, int lastMatchNumber) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        final MultipleCloudStatus n = new MultipleCloudStatus(users.length);
        final List<Prediction> predictionList = new ArrayList<>();

        for (String userID : users) {
            CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                    = new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME, mContentProviderClient)
                    .where().field(Prediction.Entry.Cols.USER_ID).eq(userID)
                    .and().field(Prediction.Entry.Cols.MATCH_NO).ge(firstMatchNumber)
                    .and().field(Prediction.Entry.Cols.MATCH_NO).le(lastMatchNumber)
                    .execute();
            CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {
                    synchronized (syncObj) {

                        if (n.isAborted()) return;

                        n.operationCompleted();

                        predictionList.addAll(parser.parsePredictionList(jsonElement));

                        if (n.isFinished()) {

                            callback.set(MobileServiceData.Builder
                                    .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                                    .setPredictionList(predictionList)
                                    .create());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull String throwable) {
                    synchronized (syncObj) {
                        n.abort();

                        sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable);
                    }
                }
            });
        }

        return true;
    }

    public boolean getPredictions(final MobileServiceCallback callback, String[] users, int matchNumber) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        final MultipleCloudStatus n = new MultipleCloudStatus(users.length);
        final List<Prediction> predictionList = new ArrayList<>();

        for (String userID : users) {
            CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                    = new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME, mContentProviderClient)
                    .where().field(Prediction.Entry.Cols.USER_ID).eq(userID)
                    .and().field(Prediction.Entry.Cols.MATCH_NO).eq(matchNumber)
                    .execute();
            CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                @Override
                public void onSuccess(JsonElement jsonElement) {
                    synchronized (syncObj) {

                        if (n.isAborted()) return;

                        n.operationCompleted();

                        predictionList.addAll(parser.parsePredictionList(jsonElement));

                        if (n.isFinished()) {

                            callback.set(MobileServiceData.Builder
                                    .instance(MobileServiceData.GET_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                                    .setPredictionList(predictionList)
                                    .create());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull String throwable) {
                    synchronized (syncObj) {
                        n.abort();

                        sendErrorMessage(callback, MobileServiceData.GET_PREDICTIONS, throwable);
                    }
                }
            });
        }

        return true;
    }

    public boolean insertPrediction(final MobileServiceCallback callback, final Prediction prediction) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future =
                new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME, mContentProviderClient)
                .insert(formatter.getAsJsonObject(prediction, Prediction.Entry.Cols.ID));

        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_PREDICTION, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setPrediction(parser.parsePrediction(result))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_PREDICTION, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setPrediction(prediction)
                        .setMessage(errorMessage)
                        .create());
            }
        });
        return true;
    }

    private static void sendErrorMessage(MobileServiceCallback callback,
                                         int requestCode,
                                         String errorMessage) {
        callback.set(MobileServiceData.Builder
                .instance(requestCode, MobileServiceData.REQUEST_RESULT_FAILURE)
                .setMessage(errorMessage)
                .create());
    }
}
