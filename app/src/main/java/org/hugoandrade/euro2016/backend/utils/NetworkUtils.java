package org.hugoandrade.euro2016.backend.utils;

import com.google.gson.JsonObject;

public class NetworkUtils {
    public static float getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, float defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public static String getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, String defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    @SuppressWarnings("unused")
    public static boolean getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, boolean defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
