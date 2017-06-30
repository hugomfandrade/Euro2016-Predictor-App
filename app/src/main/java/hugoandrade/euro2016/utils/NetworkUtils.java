package hugoandrade.euro2016.utils;

import com.google.gson.JsonObject;

/**
 * Helper class for retrieving member values of a JsonObject and convert them to either Float,
 * String or Boolean.
 */
public class NetworkUtils {

    /**
     * This returns the value of the specified member of the provided JsonObject as a Float. In case
     * any error is thrown (ie. NumberFormatException, ClassCastException or NullPointerException)
     * the default value is returned.
     *
     * @param jsonObject jsonObject to be acted on.
     * @param memberName name of the member being requested.
     * @param defaultValue default value to be returned in case any error is thrown
     * @return get the value of the specified member as a float
     */
    public static float getJsonPrimitive(JsonObject jsonObject, String memberName, float defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(memberName).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * This returns the value of the specified member of the provided JsonObject as a String. In
     * case any error is thrown (ie. ClassCastException or NullPointerException) the default value
     * is returned.
     *
     * @param jsonObject jsonObject to be acted on.
     * @param memberName name of the member being requested.
     * @param defaultValue default value to be returned in case any error is thrown
     * @return get the value of the specified member as a String
     */
    public static String getJsonPrimitive(JsonObject jsonObject, String memberName, String defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(memberName).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * This returns the value of the specified member of the provided JsonObject as a Boolean. In
     * case any error is thrown (ie. NumberFormatException or ClassCastException) the default value
     * is returned.
     *
     * @param jsonObject jsonObject to be acted on.
     * @param memberName name of the member being requested.
     * @param defaultValue default value to be returned in case any error is thrown
     * @return get the value of the specified member as a boolean
     */
    public static boolean getJsonPrimitive(JsonObject jsonObject, String memberName, boolean defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(memberName).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
