package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentProviderClient;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.DevConstants;
import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;
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

    public boolean fetchMoreUsers(final MobileServiceCallback callback, final String leagueID, int skip, int top) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                = new CloudDatabaseSimImpl(LeagueUser.Entry.TABLE_NAME, mContentProviderClient)
                .top(top)
                .skip(skip)
                .orderBy(User.Entry.Cols.SCORE, CloudDatabaseSimImpl.SortOrder.Descending)
                .where().field(LeagueUser.Entry.Cols.LEAGUE_ID).eq(leagueID)
                .execute();
        CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.e(TAG, "leagueTop::" + jsonElement.toString());

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.FETCH_MORE_USERS, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setUserList(parser.parseUserList(jsonElement))
                        .setString(leagueID)
                        .create());

            }

            @Override
            public void onFailure(@NonNull String throwable) {
                Log.e(TAG, "leagueTop::(e)::" + throwable);
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.FETCH_MORE_USERS, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setUserList(new ArrayList<User>())
                        .setString(leagueID)
                        .setMessage(throwable)
                        .create());
            }
        });

        return true;
    }

    public boolean fetchRankOfUser(final MobileServiceCallback callback, final String leagueID, String userID) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                = new CloudDatabaseSimImpl(LeagueUser.Entry.TABLE_NAME, mContentProviderClient)
                .orderBy(User.Entry.Cols.SCORE, CloudDatabaseSimImpl.SortOrder.Descending)
                .where().field(League.Entry.Cols.ID).eq(userID)
                .and().field(LeagueUser.Entry.Cols.LEAGUE_ID).eq(leagueID)
                .execute();
        CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonElement) {
                Log.e(TAG, "leagueTop::" + jsonElement.toString());

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.FETCH_RANK_OF_USER, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setUserList(parser.parseUserList(jsonElement))
                        .setString(leagueID)
                        .create());

            }

            @Override
            public void onFailure(@NonNull String throwable) {
                Log.e(TAG, "leagueTop::(e)::" + throwable);
                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.FETCH_RANK_OF_USER, MobileServiceData.REQUEST_RESULT_FAILURE)
                        .setUserList(new ArrayList<User>())
                        .setString(leagueID)
                        .setMessage(throwable)
                        .create());
            }
        });

        return true;
    }

    public boolean getLeagues(final MobileServiceCallback callback, String userID) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> f
                = new CloudDatabaseSimImpl(League.Entry.TABLE_NAME, mContentProviderClient)
                .where().field(LeagueUser.Entry.Cols.USER_ID).eq(userID)
                .execute();
        CloudDatabaseSimImpl.addCallback(f, new CloudDatabaseSimImpl.Callback<JsonElement>() {

            private List<LeagueWrapper> leagueWrapperList = new ArrayList<>();
            private List<League> leagueList = new ArrayList<>();

            @Override
            public void onSuccess(JsonElement jsonElement) {

                Log.e(TAG, "getLeagues::" + jsonElement.toString());

                leagueList = parser.parseLeagueList(jsonElement);

                tryOnFinished();

                for (League league : leagueList) {

                    MobileServiceCallback c = new MobileServiceCallback();

                    final LeagueWrapper leagueWrapper = new LeagueWrapper(league);

                    if (!fetchMoreUsers(c, league.getID(), 0, 5)) {
                        leagueWrapperList.add(leagueWrapper);

                        tryOnFinished();

                    } else {

                        MobileServiceCallback.addCallback(c, new MobileServiceCallback.OnResult() {
                            @Override
                            public void onResult(MobileServiceData data) {
                                leagueWrapper.setLeagueUserList(data.getLeagueUserList());
                                leagueWrapperList.add(leagueWrapper);

                                tryOnFinished();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull String throwable) {
                sendErrorMessage(callback, MobileServiceData.GET_LEAGUES, throwable);
            }

            private void tryOnFinished() {
                if (leagueWrapperList.size() == leagueList.size()) {
                    for (LeagueWrapper leagueWrapper : leagueWrapperList) {
                        Log.e(TAG, "league::" + leagueWrapper.toString());
                    }
                    callback.set(MobileServiceData.Builder
                            .instance(MobileServiceData.GET_LEAGUES, MobileServiceData.REQUEST_RESULT_SUCCESS)
                            .setLeagueWrapperList(leagueWrapperList)
                            .create());
                }
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
                        formatter.getAsJsonObject(league, League.Entry.Cols.ID, League.Entry.Cols.CODE),
                        HttpConstants.PostMethod,
                        mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                Log.e(TAG, "createLeague::s::" + result.toString());

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.CREATE_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLeague(parser.parseLeague(result.getAsJsonObject()))
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "createLeague::e::" + errorMessage);
                sendErrorMessage(callback, MobileServiceData.CREATE_LEAGUE, errorMessage);
            }
        });
        return true;
    }

    public boolean joinLeague(final MobileServiceCallback callback, String userID, String leagueCode) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        League.Entry.API_NAME_JOIN_LEAGUE,
                        formatter.build()
                                .addProperty(League.Entry.Cols.USER_ID, userID)
                                .addProperty(League.Entry.Cols.CODE, leagueCode)
                                .create(),
                        HttpConstants.PostMethod,
                        mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {

            private LeagueWrapper leagueWrapper;

            @Override
            public void onSuccess(JsonElement result) {
                android.util.Log.e(TAG, "joinLeague(s)::" + result.toString());

                League league = parser.parseLeague(result.getAsJsonObject());

                MobileServiceCallback c = new MobileServiceCallback();

                leagueWrapper = new LeagueWrapper(league);

                if (!fetchMoreUsers(c, league.getID(), 0, 5)) {

                    tryOnFinished();

                } else {

                    MobileServiceCallback.addCallback(c, new MobileServiceCallback.OnResult() {
                        @Override
                        public void onResult(MobileServiceData data) {
                            leagueWrapper.setLeagueUserList(data.getLeagueUserList());

                            tryOnFinished();
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                android.util.Log.e(TAG, "joinLeague(r)::" + errorMessage);
                sendErrorMessage(callback, MobileServiceData.JOIN_LEAGUE, errorMessage);
            }

            private void tryOnFinished() {
                Log.e(TAG, "joinLeague(finally)::" + leagueWrapper.toString());

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.JOIN_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .setLeagueWrapper(leagueWrapper)
                        .create());
            }
        });
        return true;
    }

    public boolean deleteLeague(final MobileServiceCallback callback, String userID, String leagueID) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        League.Entry.API_NAME_DELETE_LEAGUE,
                        formatter.build()
                                .addProperty(League.Entry.Cols.USER_ID, userID)
                                .addProperty(League.Entry.Cols.ID, leagueID)
                                .create(),
                        HttpConstants.PostMethod,
                        mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.DELETE_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(callback, MobileServiceData.DELETE_LEAGUE, errorMessage);
            }
        });
        return true;
    }

    public boolean leaveLeague(final MobileServiceCallback callback, String userID, String leagueID) {
        if (!DevConstants.CLOUD_DATABASE_SIM)
            return false;

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                new CloudDatabaseSimImpl(
                        League.Entry.API_NAME_LEAVE_LEAGUE,
                        formatter.build()
                                .addProperty(League.Entry.Cols.USER_ID, userID)
                                .addProperty(League.Entry.Cols.ID, leagueID)
                                .create(),
                        HttpConstants.PostMethod,
                        mContentProviderClient).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                Log.e(TAG, "leaveLeague::s::" + result.toString());

                callback.set(MobileServiceData.Builder
                        .instance(MobileServiceData.LEAVE_LEAGUE, MobileServiceData.REQUEST_RESULT_SUCCESS)
                        .create());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "leaveLeague::e::" + errorMessage);
                sendErrorMessage(callback, MobileServiceData.LEAVE_LEAGUE, errorMessage);
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
