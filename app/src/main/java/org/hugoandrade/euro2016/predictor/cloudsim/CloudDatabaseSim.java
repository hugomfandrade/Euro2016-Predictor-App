package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.data.Country;
import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.SystemData;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonFormatter;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.utils.ISO8601;

import java.util.ArrayList;
import java.util.Collections;

public class CloudDatabaseSim {

    private static final String TAG = CloudDatabaseSim.class.getSimpleName();

    private static MobileClientDataJsonParser parser = new MobileClientDataJsonParser();
    private static MobileClientDataJsonFormatter formatter = new MobileClientDataJsonFormatter();

    private static Callback mCallback;

    private static final int CLOUD_SIM_DURATION = 1000; // 1 seconds

    // org mName in java package format
    private static final String ORGANIZATIONAL_NAME = "org.hugoandrade";
    // mName of this provider's project
    private static final String PROJECT_NAME = "euro2016.predictor";

    /**
     * ContentProvider Related Constants
     */
    private static final String AUTHORITY = ORGANIZATIONAL_NAME + "."
            + PROJECT_NAME + ".sim_provider";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static void initialize(Callback callback, ContentResolver contentResolver) {

        mCallback = callback;
        CloudDatabaseSimImpl.initialize(contentResolver, BASE_URI);
    }

    public interface Callback {
        void sendMobileDataMessage(MobileClientData mobileClientData);
    }

    public static void getSystemData() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future
                        = new CloudDatabaseSimImpl(SystemData.Entry.API_NAME, null, "GET")
                        .execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement jsonObject) {
                        MobileClientData requestMessage = MobileClientData.makeMessage(
                                MobileClientData.OperationType.GET_SYSTEM_DATA.ordinal(),
                                MobileClientData.REQUEST_RESULT_SUCCESS);
                        requestMessage.setSystemData(parser.parseSystemData(jsonObject.getAsJsonObject()));

                        if (mCallback != null)
                            mCallback.sendMobileDataMessage(requestMessage);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(MobileClientData.OperationType.GET_SYSTEM_DATA.ordinal(), errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void signUp(final LoginData loginData) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future = new CloudDatabaseSimImpl(
                        LoginData.Entry.API_NAME_REGISTER,
                        formatter.getAsJsonObject(loginData),
                        "POST").execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        MobileClientData requestMessage = MobileClientData.makeMessage(
                                MobileClientData.OperationType.REGISTER.ordinal(),
                                MobileClientData.REQUEST_RESULT_SUCCESS);
                        LoginData data = parser.parseLoginData(result.getAsJsonObject());
                        data.setPassword(loginData.getPassword());
                        requestMessage.setLoginData(data);

                        if (mCallback != null)
                            mCallback.sendMobileDataMessage(requestMessage);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(MobileClientData.OperationType.REGISTER.ordinal(), errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void login(final LoginData loginData) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonElement> future = new CloudDatabaseSimImpl(
                        LoginData.Entry.API_NAME_LOGIN,
                        formatter.getAsJsonObject(loginData),
                        "POST").execute();
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        MobileClientData requestMessage = MobileClientData.makeMessage(
                                MobileClientData.OperationType.LOGIN.ordinal(),
                                MobileClientData.REQUEST_RESULT_SUCCESS);
                        LoginData data = parser.parseLoginData(result.getAsJsonObject());
                        data.setPassword(loginData.getPassword());
                        requestMessage.setLoginData(data);

                        if (mCallback != null)
                            mCallback.sendMobileDataMessage(requestMessage);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        sendErrorMessage(MobileClientData.OperationType.LOGIN.ordinal(), errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void getInfo(final String userID) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                final MobileClientData m = MobileClientData.makeMessage(
                        MobileClientData.OperationType.GET_INFO.ordinal(),
                        MobileClientData.REQUEST_RESULT_SUCCESS);

                final int[] n = {
                        0 /* completed operations */,
                        4, //3 /* total operations */,
                        1/* isOk flag */};

                CloudDatabaseSimImpl.ListenableCallback<JsonElement> fCountry
                        = new CloudDatabaseSimImpl(Country.Entry.TABLE_NAME).execute();
                CloudDatabaseSimImpl.addCallback(fCountry, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement jsonElement) {
                        if (n[2] == 0) return; // An error occurred

                        m.setCountryList(parser.parseCountryList(jsonElement));

                        n[0]++;
                        if (n[0] == n[1])
                            if (mCallback != null)
                                mCallback.sendMobileDataMessage(m);
                    }

                    @Override
                    public void onFailure(@NonNull String throwable) {
                        if (n[2] == 0) return; // An error occurred

                        n[2] = 0;
                        sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable);
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
                        m.setMatchList(matchList);

                        n[0]++;
                        if (n[0] == n[1])
                            if (mCallback != null)
                                mCallback.sendMobileDataMessage(m);
                    }

                    @Override
                    public void onFailure(@NonNull String throwable) {
                        if (n[2] == 0) return; // An error occurred

                        n[2] = 0;
                        sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable);
                    }
                });

                CloudDatabaseSimImpl.ListenableCallback<JsonElement> fUser
                        = new CloudDatabaseSimImpl(User.Entry.TABLE_NAME).execute();
                CloudDatabaseSimImpl.addCallback(fUser, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement jsonElement) {
                        if (n[2] == 0) return; // An error occurred

                        m.setUsers(parser.parseUserList(jsonElement));

                        n[0]++;
                        if (n[0] == n[1])
                            if (mCallback != null)
                                mCallback.sendMobileDataMessage(m);
                    }

                    @Override
                    public void onFailure(@NonNull String throwable) {
                        if (n[2] == 0) return; // An error occurred

                        n[2] = 0;
                        sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), throwable);
                    }
                });

                CloudDatabaseSimImpl.ListenableCallback<JsonElement> fPrediction =
                        new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME).where()
                                .field(Prediction.Entry.Cols.USER_ID).eq(userID).execute();
                CloudDatabaseSimImpl.addCallback(fPrediction, new CloudDatabaseSimImpl.Callback<JsonElement>() {
                    @Override
                    public void onSuccess(JsonElement result) {
                        if (n[2] == 0) return; // An error occurred

                        m.setPredictionUserID(userID);
                        m.setPredictionList(parser.parsePredictionList(result));

                        n[0]++;
                        if (mCallback != null)
                            mCallback.sendMobileDataMessage(m);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (n[2] == 0) return; // An error occurred

                        n[2] = 0;
                        sendErrorMessage(MobileClientData.OperationType.GET_INFO.ordinal(), errorMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    public static void putPrediction(final Prediction prediction) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloudDatabaseSimImpl.ListenableCallback<JsonObject> future
                        = new CloudDatabaseSimImpl(Prediction.Entry.TABLE_NAME)
                        .insert(formatter.getAsJsonObject(prediction));
                CloudDatabaseSimImpl.addCallback(future, new CloudDatabaseSimImpl.Callback<JsonObject>() {
                    @Override
                    public void onSuccess(JsonObject result) {
                        MobileClientData requestMessage = MobileClientData.makeMessage(
                                MobileClientData.OperationType.PUT_PREDICTION.ordinal(),
                                MobileClientData.REQUEST_RESULT_SUCCESS);
                        requestMessage.setPrediction(parser.parsePrediction(result));

                        if (mCallback != null)
                            mCallback.sendMobileDataMessage(requestMessage);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        MobileClientData requestMessage = MobileClientData.makeMessage(
                                MobileClientData.OperationType.PUT_PREDICTION.ordinal(),
                                MobileClientData.REQUEST_RESULT_FAILURE);
                        requestMessage.setPrediction(prediction);
                        requestMessage.setErrorMessage(errorMessage);

                        if (errorMessage.contains(Prediction.Entry.PastMatchDate))
                            requestMessage.setServerTime(ISO8601.toCalendar(errorMessage.split(":")[1]));

                        if (mCallback != null)
                            mCallback.sendMobileDataMessage(requestMessage);
                    }
                });
            }
        }, CLOUD_SIM_DURATION);
    }

    private static void sendErrorMessage(int requestCode, String errorMessage) {
        MobileClientData requestMessage = MobileClientData.makeMessage(
                requestCode,
                MobileClientData.REQUEST_RESULT_FAILURE);
        requestMessage.setErrorMessage(errorMessage);

        if (mCallback != null)
            mCallback.sendMobileDataMessage(requestMessage);
    }
}
