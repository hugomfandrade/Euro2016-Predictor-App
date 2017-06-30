package hugoandrade.euro2016backend.cloudsim;

import android.content.ContentResolver;
import android.os.Messenger;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import hugoandrade.euro2016backend.common.MessageBase;
import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.object.Match;
import hugoandrade.euro2016backend.object.SystemData;

import static hugoandrade.euro2016backend.model.MobileService.ALL_MATCHES_DATA;
import static hugoandrade.euro2016backend.model.MobileService.ALL_COUNTRIES_DATA;
import static hugoandrade.euro2016backend.model.MobileService.COUNTRY_DATA;
import static hugoandrade.euro2016backend.model.MobileService.MATCH_DATA;
import static hugoandrade.euro2016backend.model.MobileService.ERROR_MESSAGE;
import static hugoandrade.euro2016backend.model.MobileService.REQUEST_RESULT_FAILURE;
import static hugoandrade.euro2016backend.model.MobileService.REQUEST_RESULT_SUCCESS;
import static hugoandrade.euro2016backend.model.MobileService.SYSTEM_DATA;

public class CloudDatabaseSim {

    @SuppressWarnings("unused") private final static String TAG = CloudDatabaseSim.class.getSimpleName();

    public static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.initialize(contentResolver);
    }

    public static void getAllCountries(final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(Country.TABLE_NAME).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
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

    public static void updateMatchUp(Match match, final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(Match.TABLE_NAME).update(match.getAsJsonObject());
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void updateCountry(Country country, final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(Country.TABLE_NAME).update(country.getAsJsonObject());
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void getSystemData(final Messenger replyTo, final int requestCode) {
        CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                = new CloudDatabaseSimImpl(SystemData.TABLE_NAME).execute();
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                final JsonArray results = result.getAsJsonArray();
                if (results.size() > 0) {
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
            public void onFailure(String errorMessage) {
                sendErrorMessage(replyTo, requestCode, errorMessage);
            }
        });
    }

    public static void setSystemData(final Messenger replyTo, final int requestCode, SystemData systemData) {
        CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                = new CloudDatabaseSimImpl(SystemData.TABLE_NAME).update(systemData.getAsJsonObject());
        CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
            @Override
            public void onSuccess(JsonObject result) {
                MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_SUCCESS);
                requestMessage.putParcelable(SYSTEM_DATA, SystemData.getInstanceFromJsonObject(result));

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
        MessageBase requestMessage = MessageBase.makeMessage(requestCode, REQUEST_RESULT_FAILURE);
        requestMessage.putString(ERROR_MESSAGE, errorMessage);

        try {
            replyTo.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
        }
    }
}
