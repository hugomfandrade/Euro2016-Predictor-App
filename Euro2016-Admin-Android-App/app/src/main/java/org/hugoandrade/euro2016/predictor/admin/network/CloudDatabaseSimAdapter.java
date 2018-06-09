package org.hugoandrade.euro2016.predictor.admin.network;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.admin.DevConstants;
import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.League;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.data.WaitingLeagueUser;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.cloudsim.CloudDatabaseSimImpl;

public class CloudDatabaseSimAdapter {

    @SuppressWarnings("unused")
    private final static String TAG = CloudDatabaseSimAdapter.class.getSimpleName();

    private static CloudDatabaseSimAdapter mInstance = null;

    private ContentResolver mContentResolver;

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
            mInstance = new CloudDatabaseSimAdapter(context);
        } else {
            throw new IllegalStateException("CloudDatabaseSimAdapter is already initialized");
        }
    }

    private CloudDatabaseSimAdapter(Context context) {
        mContentResolver = context.getContentResolver();
    }

    public boolean login(final MobileServiceCallback callback, final LoginData loginData) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        LoginData.Entry.API_NAME_LOGIN,
                        formatter.getAsJsonObject(loginData),
                        HttpConstants.PostMethod,
                        mContentResolver).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                LoginData data = parser.parseLoginData(result.getAsJsonObject());
                data.setPassword(loginData.getPassword());

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

    public boolean getSystemData(final MobileServiceCallback callback) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.Entry.API_NAME, null,
                HttpConstants.GetMethod,
                mContentResolver)
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
                = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME, mContentResolver).execute();
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
                = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME, mContentResolver).execute();
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

    public boolean updateSystemData(final MobileServiceCallback callback, SystemData systemData) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.Entry.API_NAME,
                formatter.getAsJsonObject(systemData),
                HttpConstants.PostMethod, mContentResolver).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_SYSTEM_DATA, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setSystemData(parser.parseSystemData(result.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.UPDATE_SYSTEM_DATA, errorMessage);
            }
        });

        return true;
    }

    public boolean updateCountry(final MobileServiceCallback callback, Country country) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME, mContentResolver)
                .update(formatter.getAsJsonObject(country));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_COUNTRY, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountry(parser.parseCountry(result))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.UPDATE_COUNTRY, errorMessage);
            }
        });
        return true;
    }

    public boolean updateMatch(final MobileServiceCallback callback, final Match match) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future =
                new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME, mContentResolver)
                .update(formatter.getAsJsonObject(match));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_MATCH, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatch(parser.parseMatch(result))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_MATCH, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setMatch(match)
                        .setMessage(errorMessage)
                        .create());
            }
        });
        return true;
    }

    public boolean deleteCountry(final MobileServiceCallback callback, final Country country) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<Void> future =
                new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME, mContentResolver)
                .delete(formatter.getAsJsonObject(country));

        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_COUNTRY, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountry(country)
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_COUNTRY, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setCountry(country)
                        .setMessage(errorMessage)
                        .create());
            }
        });
        return true;
    }

    public boolean deleteMatch(final MobileServiceCallback callback, final Match match) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<Void> future = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME, mContentResolver)
                .delete(formatter.getAsJsonObject(match));

        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_MATCH, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatch(match)
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_MATCH, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setMatch(match)
                        .setMessage(errorMessage)
                        .create());
            }
        });
        return true;
    }

    public boolean insertCountry(final MobileServiceCallback callback, final Country country) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME, mContentResolver)
                .insert(formatter.getAsJsonObject(country, Country.Entry.Cols.ID));

        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_COUNTRY, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setCountry(parser.parseCountry(result))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_COUNTRY, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setCountry(country)
                        .setMessage(errorMessage)
                        .create());
            }
        });
        return true;
    }

    public boolean insertMatch(final MobileServiceCallback callback, final Match match) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME, mContentResolver)
                .insert(formatter.getAsJsonObject(match, Match.Entry.Cols.ID));

        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_MATCH, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setMatch(parser.parseMatch(result))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.INSERT_MATCH, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setMatch(match)
                        .setMessage(errorMessage)
                        .create());
            }
        });
        return true;
    }

    public boolean updateScoresOfPredictions(final MobileServiceCallback callback) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(SystemData.Entry.API_NAME_UPDATE_SCORES,
                                         null,
                                         HttpConstants.PostMethod,
                                         mContentResolver)
                        .execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.UPDATE_SCORES_OF_PREDICTIONS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.UPDATE_SCORES_OF_PREDICTIONS, errorMessage);
            }
        });
        return true;
    }

    public boolean createLeague(final MobileServiceCallback callback, final League league) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        League.Entry.API_NAME_CREATE_LEAGUE,
                        formatter.getAsJsonObject(league),
                        HttpConstants.PostMethod,
                        mContentResolver).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.CREATE_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLeague(parser.parseLeague(result.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.CREATE_LEAGUE, errorMessage);
            }
        });
        return true;
    }

    public boolean joinLeague(final MobileServiceCallback callback, final WaitingLeagueUser waitingLeagueUser) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        League.Entry.API_NAME_JOIN_LEAGUE,
                        formatter.getAsJsonObject(waitingLeagueUser),
                        HttpConstants.PostMethod,
                        mContentResolver).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.JOIN_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLeague(parser.parseLeague(result.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.JOIN_LEAGUE, errorMessage);
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
