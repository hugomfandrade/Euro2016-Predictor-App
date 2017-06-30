package hugoandrade.euro2016;

import android.content.ContentResolver;
import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import hugoandrade.euro2016.common.MessageBase;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.utils.ISO8601;

import static hugoandrade.euro2016.model.AzureMobileService.REQUEST_RESULT_FAILURE;
import static hugoandrade.euro2016.model.AzureMobileService.REQUEST_RESULT_SUCCESS;
import static hugoandrade.euro2016.model.AzureMobileService.ALL_COUNTRIES_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.ALL_MATCHES_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.ALL_PREDICTIONS_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.ALL_USERS_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.PREDICTION_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.SYSTEM_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.USER_DATA;
import static hugoandrade.euro2016.model.AzureMobileService.NEW_SERVER_TIME;
import static hugoandrade.euro2016.model.AzureMobileService.ERROR_MESSAGE;

public class CloudDatabaseSim {

    private static final String TAG = CloudDatabaseSim.class.getSimpleName();

    private static final int CLOUD_SIM_DURATION = 1000; // 1 seconds

    public static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.initialize(contentResolver);
    }

    public static void loginOrSignUp(final String operationType, final LoginData loginData,
                                     final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Pair<String, String>> parameters = new ArrayList<>();
                parameters.add(new Pair<>(LoginData.REQUEST_TYPE, operationType));

                CloudDatabaseSimImpl.ListenableCallback<JsonObject> future =
                        new CloudDatabaseSimImpl(User.TABLE_NAME).insert(loginData.getAsJsonObject(), parameters);

                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                        requestMessage.putParcelable(USER_DATA, User.instanceFromJsonObject(result));

                        try {
                            replyTo.send(requestMessage.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while sending message back to Activity.", e);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void requestSystemTime(final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                        = new CloudDatabaseSimImpl(SystemData.TABLE_NAME).execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        final JsonArray results = result.getAsJsonArray();
                        if (results.size() == 0) {
                            sendErrorMessage(replyTo, requestCode, "Could not retrieve SystemData");
                            return;
                        }
                        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                        requestMessage.putParcelable(SYSTEM_DATA,
                                SystemData.getInstanceFromJsonObject(results.get(0).getAsJsonObject()));

                        try {
                            replyTo.send(requestMessage.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while sending message back to Activity.", e);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void getAllCountries(final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                        = new CloudDatabaseSimImpl(Country.TABLE_NAME).execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        ArrayList<Country> allCountriesList = new ArrayList<>();
                        for (JsonElement item : result.getAsJsonArray())
                            allCountriesList.add(Country.getInstanceFromJsonObject(item.getAsJsonObject()));

                        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                        requestMessage.putParcelableArrayList(ALL_COUNTRIES_DATA, allCountriesList);

                        try {
                            replyTo.send(requestMessage.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while sending message back to Activity.", e);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void getAllUsers(final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                        new CloudDatabaseSimImpl(User.TABLE_NAME).select(
                                User.COL_NAME_ID,
                                User.COL_NAME_USERNAME,
                                User.COL_NAME_SCORE).execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        ArrayList<User> allUsersList = new ArrayList<>();
                        for (JsonElement item : result.getAsJsonArray())
                            allUsersList.add(User.instanceFromJsonObject(item.getAsJsonObject()));

                        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                        requestMessage.putParcelableArrayList(ALL_USERS_DATA, allUsersList);

                        try {
                            replyTo.send(requestMessage.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while sending message back to Activity.", e);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void getAllPredictions(final String userID, final int matchNo,
                                         final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future =
                        new CloudDatabaseSimImpl(Prediction.TABLE_NAME).where()
                                .field(Prediction.COL_NAME_USER_ID).eq(userID).and().
                                field(Prediction.COL_NAME_MATCH_NO).lt(matchNo).execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
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
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void getAllMatches(final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                        = new CloudDatabaseSimImpl(Match.TABLE_NAME).execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
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
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(replyTo, requestCode, errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void putPrediction(final Prediction prediction,
                                     final Messenger replyTo, final int requestCode) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                        = new CloudDatabaseSimImpl(Prediction.TABLE_NAME).insert(prediction.getAsJsonObject());
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        Log.d(TAG, "putPrediction: " + result.toString());
                        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                        requestMessage.putParcelable(PREDICTION_DATA,
                                Prediction.getInstanceFromJsonObject(result));

                        try {
                            replyTo.send(requestMessage.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while sending message back to Activity.", e);
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.d(TAG, "putPrediction(FAILURE): " + errorMessage);

                        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                        requestMessage.putParcelable(PREDICTION_DATA, prediction);
                        requestMessage.putString(ERROR_MESSAGE, errorMessage);
                        if (errorMessage.contains("Past match date"))
                            requestMessage.putSerializable(NEW_SERVER_TIME,
                                    ISO8601.toCalendar(errorMessage.split(":")[1]));

                        try {
                            replyTo.send(requestMessage.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, "Exception while sending message back to Activity.", e);
                        }
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    private static void sendErrorMessage(Messenger replyTo, int requestCode, String errorMessage) {
        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_FAILURE);
        requestMessage.putString(ERROR_MESSAGE, errorMessage);

        try {
            replyTo.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }
}
