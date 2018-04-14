package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.data.raw.SystemData;

import java.util.Map;

/**
 * Parses the objects to Json data.
 */
public class CloudJsonFormatter {

    public ContentValues getAsContentValues(JsonObject jsonObject) {
        ContentValuesBuilder builder = ContentValuesBuilder.instance();
        for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet()) {
            // Because it is a boolean
            if (entry.getKey().equals(SystemData.Entry.Cols.APP_STATE)) {
                builder.put(entry.getKey(), getJsonPrimitive(jsonObject, entry.getKey(), false) ? 1 : 0);
            } else {
                String cname = entry.getKey();
                if (cname.equals("id"))
                    cname = "_id";

                builder.put(cname, getJsonPrimitive(jsonObject, entry.getKey(), null));
            }
        }
        return builder.create();

    }

    private static class ContentValuesBuilder {

        private final ContentValues mContentValues;

        private static ContentValuesBuilder instance() {
            return new ContentValuesBuilder();
        }

        private ContentValuesBuilder() {
            mContentValues = new ContentValues();
        }

        ContentValuesBuilder put(String key, String value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValuesBuilder put(String key, Integer value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValuesBuilder put(String key, float value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValuesBuilder put(String key, Boolean value) {
            mContentValues.put(key, value);
            return this;
        }

        ContentValues create() {
            return mContentValues;
        }
    }

    public JsonElement getAsJsonObject(Cursor c) {
        JsonObjectBuilder builder = JsonObjectBuilder.instance();
        for (String columnName : c.getColumnNames()) {
            // Because it is a boolean
            if (columnName.equals(SystemData.Entry.Cols.APP_STATE)) {
                builder.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)) == 1);
            }
            else {
                String cname = columnName;
                if (cname.equals("_id"))
                    cname = "id";
                int type = c.getType(c.getColumnIndex(columnName));
                if (type == Cursor.FIELD_TYPE_INTEGER)
                    builder.addProperty(cname, c.getInt(c.getColumnIndex(columnName)));
                else
                    builder.addProperty(cname, c.getString(c.getColumnIndex(columnName)));
            }

        }
        return builder.create();
    }

    public JsonArray getAsJsonArray(Cursor c) {

        JsonArray jsonArray = new JsonArray();

        if (c.moveToFirst()) {
            do{
                jsonArray.add(getAsJsonObject(c));
            } while (c.moveToNext());
        }
        c.close();
        return jsonArray;
    }

    private static class JsonObjectBuilder {

        private final JsonObject mJsonObject;

        private static JsonObjectBuilder instance() {
            return new JsonObjectBuilder();
        }

        private JsonObjectBuilder() {
            mJsonObject = new JsonObject();
        }

        JsonObjectBuilder addProperty(String property, String value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        JsonObjectBuilder addProperty(String property, Number value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        JsonObjectBuilder addProperty(String property, Boolean value) {
            mJsonObject.addProperty(property, value);
            return this;
        }

        JsonObject create() {
            return mJsonObject;
        }
    }

    private static int getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, int defaultValue) {
        try {
            return (int) jsonObject.getAsJsonPrimitive(jsonMemberName).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static float getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, float defaultValue) {
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

    public static boolean getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, boolean defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
