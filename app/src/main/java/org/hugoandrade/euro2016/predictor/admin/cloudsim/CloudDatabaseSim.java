package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.ContentResolver;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.admin.model.parser.MessageBase;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.LoginData;
import org.hugoandrade.euro2016.predictor.admin.object.Match;
import org.hugoandrade.euro2016.predictor.admin.object.SystemData;

import java.util.ArrayList;
import java.util.Collections;

public class CloudDatabaseSim {

    @SuppressWarnings("unused") private final static String TAG = CloudDatabaseSim.class.getSimpleName();

    private static MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private static MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    public static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.initialize(contentResolver);
    }

    public static void login(final Messenger replyTo, final int requestCode, final LoginData loginData) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future = new CloudDatabaseSimImpl(
                LoginData.Entry.API_NAME_LOGIN,
                formatter.getAsJsonObject(loginData),
                "POST").execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                Log.e(TAG, result.toString());
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setLoginData(parser.parseLoginData(result.getAsJsonObject()));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void getInfo(final Messenger replyTo, final int requestCode) {

        final MessageBase requestMessage = MessageBase.makeMessage(
                requestCode,
                MessageBase.REQUEST_RESULT_SUCCESS);

        final int[] n = {
                0 /* completed operations */,
                2, //3 /* total operations */,
                1/* isOk flag */};

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> fCountry
                = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME).execute();
        CloudDatabaseSimImpl.addCallback(fCountry, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement jsonElement) {
                        if (n[2] == 0) return; // An error occurred

                        requestMessage.setCountryList(parser.parseCountryList(jsonElement));

                        n[0]++;
                        if (n[0] == n[1])
                            try {
                                replyTo.send(requestMessage.getMessage());
                            } catch (Exception e) {
                                Log.e(TAG, "Exception while sending message back to Activity.", e);
                            }
                    }

                    @Override
                    public void onFailure(@NonNull String throwable) {
                        if (n[2] == 0) return; // An error occurred

                        n[2] = 0;
                        sendErrorMessage(replyTo, requestCode, throwable);
                    }
                });

        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement jsonElement) {
                        if (n[2] == 0) return; // An error occurred

                        ArrayList<Match> matchList = parser.parseMatchList(jsonElement);
                        Collections.sort(matchList);
                        requestMessage.setMatchList(matchList);

                        n[0]++;
                        if (n[0] == n[1])
                            try {
                                replyTo.send(requestMessage.getMessage());
                            } catch (Exception e) {
                                Log.e(TAG, "Exception while sending message back to Activity.", e);
                            }
                    }

                    @Override
                    public void onFailure(@NonNull String throwable) {
                        if (n[2] == 0) return; // An error occurred

                        n[2] = 0;
                        sendErrorMessage(replyTo, requestCode, throwable);
                    }
                });
    }

    public static void updateMatch(Match match, final Messenger replyTo, final int requestCode) {
        Log.e(TAG, "Match: " + formatter.getAsJsonObject(match).toString());
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME)
                .update(formatter.getAsJsonObject(match));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                Log.e(TAG, "updateMatch Success: " + result.toString());
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setMatch(parser.parseMatch(result));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void updateCountry(Country country, final Messenger replyTo, final int requestCode) {
        Log.e(TAG, "updateCountry: " );
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME)
                .update(formatter.getAsJsonObject(country));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setCountry(parser.parseCountry(result));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void getSystemData(final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.Entry.API_NAME, null, "GET")
                .execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement jsonObject) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void setSystemData(final Messenger replyTo, final int requestCode, SystemData systemData) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.Entry.API_NAME,
                formatter.getAsJsonObject(systemData),
                "POST").execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                MessageBase requestMessage
                        = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_SUCCESS);
                requestMessage.setSystemData(parser.parseSystemData(result.getAsJsonObject()));

                sendRequestMessage(replyTo, requestMessage);
            }

            @Override
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    private static void sendErrorMessage(Messenger replyTo, int requestCode, String errorMessage) {
        MessageBase requestMessage
                = MessageBase.makeMessage(requestCode, MessageBase.REQUEST_RESULT_FAILURE);
        requestMessage.setErrorMessage(errorMessage);

        sendRequestMessage(replyTo, requestMessage);
    }

    private static void sendRequestMessage(Messenger replyTo, MessageBase requestMessage) {
        try {
            replyTo.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }
}
