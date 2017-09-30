package org.hugoandrade.euro2016.backend.cloudsim.parser;


import android.database.Cursor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.backend.object.SystemData;

/**
 * Parses the objects to Json data.
 */
public class CloudJsonFormatter {


    public JsonElement getAsJsonObject(Cursor c) {
        JsonObjectBuilder builder = JsonObjectBuilder.instance();
        for (String columnName : c.getColumnNames()) {
            // Because it is a boolean
            if (columnName.equals(SystemData.Entry.COLUMN_APP_STATE)) {
                builder.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)) == 1);
            }
            else {
                int type = c.getType(c.getColumnIndex(columnName));
                if (type == Cursor.FIELD_TYPE_INTEGER)
                    builder.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)));
                else
                    builder.addProperty(columnName, c.getString(c.getColumnIndex(columnName)));
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
}
