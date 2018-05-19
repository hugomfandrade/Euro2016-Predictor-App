package org.hugoandrade.euro2016.predictor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.data.raw.LoginData;

public final class SharedPreferencesUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = SharedPreferencesUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private SharedPreferencesUtils() {
        throw new AssertionError();
    }

    private static final String AUTHENTICATED_SHARED_PREFERENCES_NAME = "authenticated_shared_preferences_name";
    private final static String LOGIN_DATA_SHARED_PREFERENCES_NAME = "login_data_shared_preferences_name";
    private final static String TOKEN_LOGIN_DATA_SHARED_PREFERENCES_NAME = "token_login_data_shared_preferences_name";
    private final static String LOGIN_DATA_KEY_EMAIL = "login_data_key_email";
    private final static String LOGIN_DATA_KEY_PASSWORD = "login_data_key_password";
    private final static String LOGIN_DATA_KEY_USER_ID = "login_data_key_user_id";
    private final static String LOGIN_DATA_KEY_TOKEN = "login_data_key_token";

    public static LoginData getLoginData(Context context) {
        SharedPreferences settings = context.getSharedPreferences(LOGIN_DATA_SHARED_PREFERENCES_NAME, 0);

        return new LoginData(
                settings.getString(LOGIN_DATA_KEY_EMAIL, null),
                settings.getString(LOGIN_DATA_KEY_PASSWORD, null));
    }

    public static void putLoginData(Context context, LoginData loginData) {

        SharedPreferences settings = context.getSharedPreferences(LOGIN_DATA_SHARED_PREFERENCES_NAME, 0);

        settings.edit()
                .putString(LOGIN_DATA_KEY_EMAIL, loginData.getEmail())
                .putString(LOGIN_DATA_KEY_PASSWORD, loginData.getPassword())
                .apply();
    }

    public static void resetLoginData(Context context) {
        context.getSharedPreferences(LOGIN_DATA_SHARED_PREFERENCES_NAME, 0)
                .edit()
                .clear()
                .apply();
    }

    public static void putLastAuthenticatedLoginData(Context context, LoginData loginData) {

        context.getSharedPreferences(AUTHENTICATED_SHARED_PREFERENCES_NAME, 0)
                .edit()
                .putString(LOGIN_DATA_KEY_USER_ID, loginData.getUserID())
                .putString(LOGIN_DATA_KEY_EMAIL, loginData.getEmail())
                .putString(LOGIN_DATA_KEY_PASSWORD, loginData.getPassword())
                .putString(LOGIN_DATA_KEY_TOKEN, loginData.getToken())
                .apply();
    }

    public static LoginData getLastAuthenticatedLoginData(Context context) {
        SharedPreferences settings = context
                .getSharedPreferences(AUTHENTICATED_SHARED_PREFERENCES_NAME, 0);

        return new LoginData(
                settings.getString(LOGIN_DATA_KEY_USER_ID, null),
                settings.getString(LOGIN_DATA_KEY_EMAIL, null),
                settings.getString(LOGIN_DATA_KEY_PASSWORD, null),
                settings.getString(LOGIN_DATA_KEY_TOKEN, null));
    }

    public static void resetLastAuthenticatedLoginData(Context context) {
        context.getSharedPreferences(AUTHENTICATED_SHARED_PREFERENCES_NAME, 0)
                .edit()
                .clear()
                .apply();
    }

    /*
    public static void putTokenLoginData(Context context, LoginData loginData) {

        context.getSharedPreferences(TOKEN_LOGIN_DATA_SHARED_PREFERENCES_NAME, 0)
                .edit()
                .putString(LOGIN_DATA_KEY_USER_ID, loginData.getUserID())
                .putString(LOGIN_DATA_KEY_EMAIL, loginData.getEmail())
                .putString(LOGIN_DATA_KEY_PASSWORD, loginData.getPassword())
                .putString(LOGIN_DATA_KEY_TOKEN, loginData.getToken())
                .apply();

    }

    public static LoginData getTokenLoginData(Context context) {
        SharedPreferences settings = context
                .getSharedPreferences(TOKEN_LOGIN_DATA_SHARED_PREFERENCES_NAME, 0);

        return new LoginData(
                settings.getString(LOGIN_DATA_KEY_USER_ID, null),
                settings.getString(LOGIN_DATA_KEY_EMAIL, null),
                settings.getString(LOGIN_DATA_KEY_PASSWORD, null),
                settings.getString(LOGIN_DATA_KEY_TOKEN, null));
    }

    public static void resetTokenLoginData(Context context) {

        context.getSharedPreferences(TOKEN_LOGIN_DATA_SHARED_PREFERENCES_NAME, 0)
                .edit()
                .clear()
                .apply();
    }/**/
}
