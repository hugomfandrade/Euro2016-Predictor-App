package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudDataJsonParser;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudJsonFormatter;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MobileClientDataJsonParser;
import org.hugoandrade.euro2016.predictor.admin.object.SystemData;

import java.util.Map;

class CloudDatabaseSimImpl {

    private final static String TAG = CloudDatabaseSimImpl.class.getSimpleName();

    @SuppressWarnings("unused") static final int CLOUD_SIM_DURATION = 1000; // 1 seconds

    private static ContentResolver provider;

    private final String tableName;
    private final boolean isApi;
    private JsonObject apiJsonObject;
    private String apiType;

    CloudDatabaseSimImpl(String table) {
        tableName = table;
        isApi = false;
    }

    CloudDatabaseSimImpl(String tableName, JsonObject jsonObject, String apiType) {
        this.tableName = tableName;
        this.apiJsonObject = jsonObject;
        this.apiType = apiType;
        isApi = true;
    }

    ListenableCallback<JsonElement> execute() {
        Log.d(TAG, "execute(getTable): " + tableName);
        ListenableCallback<JsonElement> task;
        if (isApi)  {
            task = new ListenableCallback<JsonElement>(tableName)
                    .api(apiType, apiJsonObject);
        }
        else {
            task = new ListenableCallback<>(tableName);
        }
        task.execute();
        return task;
    }

    ListenableCallback<JsonObject> update(JsonObject jsonObject) {
        Log.d(TAG, "update(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject);
        task.execute();
        return task;
    }

    @SuppressWarnings("unused")
    ListenableCallback<JsonObject> insert(JsonObject jsonObject) {
        Log.d(TAG, "insert(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject);
        task.execute();
        return task;
    }

    static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.provider = contentResolver;
    }

    static class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused") private final String TAG = ListenableCallback.class.getSimpleName();

        @SuppressWarnings("unused") private static final int TASK_UNKNOWN = 0;
        private static final int TASK_GET = 1;
        private static final int TASK_UPDATE = 2;
        private static final int TASK_INSERT = 3;
        private static final int TASK_API_GET = 4;
        private static final int TASK_API_POST = 5;

        private CloudDataJsonParser parser = new CloudDataJsonParser();
        private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();
        private CloudJsonFormatter jsonFormatter = new CloudJsonFormatter();

        private final String tableName;
        private JsonObject jsonObject;
        private int taskType;
        private Callback<V> future;

        ListenableCallback(String tableName) {
            this.tableName = tableName;
            this.jsonObject = null;
            this.taskType = TASK_GET;
        }

        ListenableCallback(String tableName, JsonObject jsonObject) {
            this.tableName = tableName;
            this.jsonObject = jsonObject;
            this.taskType = TASK_UPDATE;
        }

        ListenableCallback<V> api(String apiType, JsonObject apiJsonObject) {
            this.jsonObject = apiJsonObject;
            if (apiType.equals("POST"))
                this.taskType = TASK_API_POST;
            else if (apiType.equals("GET"))
                this.taskType = TASK_API_GET;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Void... params) {
            String URL = CloudDatabaseSimProvider.BASE_URI.toString();

            if (taskType == TASK_GET)
                getOperation(URL);
            else if (taskType == TASK_UPDATE)
                updateOperation(URL);
            else if (taskType == TASK_API_POST)
                postApiOperation(URL);
            else if (taskType == TASK_API_GET)
                getApiOperation(URL);

            return null;
        }

        private void getApiOperation(String URL) {
            Uri objects = Uri.parse(URL + "/" + tableName);

            Cursor c = provider.query(objects, null, null, null, null);

            if (c == null) {
                if (future != null)
                    future.onFailure("Cursor not found");
                return;
            }

            JsonArray jsonArray = jsonFormatter.getAsJsonArray(c);

            try {
                if (future != null) {
                    if (jsonArray.size() == 0)
                        future.onSuccess(null);
                    else
                        future.onSuccess((V) jsonArray.get(0));
                }
            } catch (ClassCastException e) {
                future.onFailure("Cursor not found");
            }
        }

        private void postApiOperation(String URL) {
            Uri url = Uri.parse(URL + "/" + tableName);

            Uri uri = provider.insert(url, cvFormatter.getAsContentValues(jsonObject));

            if (future == null)
                return;
            if (uri == null)
                future.onFailure("Operation failed");
            else {
                if (!uri.toString().startsWith("content://"))
                    future.onFailure(uri.toString());
                else {

                    Cursor c = provider.query(uri, null, null, null, null);
                    if (c == null) {
                        if (future != null)
                            future.onFailure("Cursor not found");
                        return;
                    }

                    if (c.moveToFirst()) {
                        try {
                            if (future != null)
                                future.onSuccess((V) fromCursorToJsonObject(c));
                        } catch (ClassCastException e) {
                            future.onFailure("Could not cast");
                        }
                    }
                    c.close();
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void updateOperation(String URL) {
            Uri url = Uri.parse(URL +
                                "/" +
                                tableName +
                                "/" +
                                parser.parseString(jsonObject, "id"));

            int c = provider.update(url,
                                    cvFormatter.getAsContentValues(jsonObject),
                                    null,
                                    null);
            if (future == null)
                return;
            if (c == 0)
                future.onFailure("No item updated");
            else if (c == 1)
                future.onSuccess((V) jsonObject);
            else
                future.onFailure("Multiple items updated. Please, retrieve all matches again.");
        }

        @SuppressWarnings("unchecked")
        private void getOperation(String URL) {
            Uri objects = Uri.parse(URL + "/" + tableName);
            Cursor c = provider.query(objects, null, null, null, null);
            if (c == null) {
                if (future != null)
                    future.onFailure("Cursor not found");
                return;
            }

            JsonArray jsonArray = jsonFormatter.getAsJsonArray(c);

            try {
                if (future != null)
                    future.onSuccess((V) jsonArray);
            } catch (ClassCastException e) {
                future.onFailure("Cursor not found");
            }
        }

        void addListener(Callback<V> future) {
            this.future = future;
        }

        private JsonObject fromCursorToJsonObject(Cursor c) {
            JsonObject jsonObject = new JsonObject();
            for (String columnName : c.getColumnNames()) {
                if (columnName.equals(SystemData.Entry.Cols.APP_STATE))
                    jsonObject.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)) == 1);
                else {
                    if (columnName.equals("_id"))
                        jsonObject.addProperty("id", c.getInt(c.getColumnIndex(columnName)) == 1);
                    else
                        jsonObject.addProperty(columnName, c.getString(c.getColumnIndex(columnName)));
                }
            }
            return jsonObject;
        }

        private ContentValues fromJsonObjectToContentValues(JsonObject jsonObject) {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet())
                values.put(entry.getKey(), getJsonPrimitive(jsonObject, entry.getKey(), null));
            return values;
        }
    }

    private static String getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, String defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static <V>  void addCallback(ListenableCallback<V> listenable, Callback<V> future) {
        listenable.addListener(future);
    }

    interface Callback<V> {
        void onSuccess(V result);
        void onFailure(String errorMessage);
    }
}
