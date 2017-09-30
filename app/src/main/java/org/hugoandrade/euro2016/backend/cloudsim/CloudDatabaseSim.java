package org.hugoandrade.euro2016.backend.cloudsim;

import android.content.ContentResolver;
import android.os.Messenger;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.backend.model.parser.MessageBase;
import org.hugoandrade.euro2016.backend.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.backend.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;

public class CloudDatabaseSim {

    @SuppressWarnings("unused") private final static String TAG = CloudDatabaseSim.class.getSimpleName();

    private static MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private static MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    public static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.initialize(contentResolver);
    }

    public static void getAllCountries(final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void getAllMatches(final Messenger replyTo, final int requestCode) {
            CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                    = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME).execute();
            CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
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
                public void onFailure(String errorMessage) {
                    sendErrorMessage(replyTo, requestCode, errorMessage);
                }
            });
    }

    public static void updateMatch(Match match, final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(Match.Entry.TABLE_NAME)
                .update(formatter.getAsJsonObject(match));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void updateCountry(Country country, final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME)
                .update(formatter.getAsJsonObject(country));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void getSystemData(final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.Entry.TABLE_NAME).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                final JsonArray results = result.getAsJsonArray();
                if (results.size() > 0) {
                    MessageBase requestMessage = MessageBase.makeMessage(
                            requestCode,
                            MessageBase.REQUEST_RESULT_SUCCESS
                    );
                    requestMessage.setSystemData(parser.parseSystemData(results.get(0).getAsJsonObject()));

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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void setSystemData(final Messenger replyTo, final int requestCode, SystemData systemData) {
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(SystemData.Entry.TABLE_NAME)
                .update(formatter.getAsJsonObject(systemData));
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    private static void sendErrorMessage(Messenger replyTo, int requestCode, String errorMessage) {
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
}
