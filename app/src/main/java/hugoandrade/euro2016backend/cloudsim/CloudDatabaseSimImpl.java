package hugoandrade.euro2016backend.cloudsim;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import hugoandrade.euro2016backend.object.SystemData;
import hugoandrade.euro2016backend.utils.NetworkUtils;

class CloudDatabaseSimImpl {

    private final static String TAG = CloudDatabaseSimImpl.class.getSimpleName();

    @SuppressWarnings("unused") static final int CLOUD_SIM_DURATION = 1000; // 1 seconds

    private static ContentResolver provider;
    private final String tableName;

    CloudDatabaseSimImpl(String table) {
        tableName = table;
    }

    ListenableCallback<JsonElement> execute() {
        Log.d(TAG, "execute(getTable): " + tableName);
        ListenableCallback<JsonElement> task = new ListenableCallback<>(tableName);
        task.execute();
        return task;
    }

    ListenableCallback<JsonObject> update(JsonObject jsonObject) {
        Log.d(TAG, "update(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject);
        task.execute();
        return task;
    }

    ListenableCallback<JsonObject> insert(JsonObject jsonObject) {
        Log.d(TAG, "insert(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject);
        task.execute();
        return task;
    }

    static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.provider = contentResolver;
    }

    class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused") private final String TAG = ListenableCallback.class.getSimpleName();

        @SuppressWarnings("unused") private static final int TASK_UNKNOWN = 0;
        private static final int TASK_GET = 1;
        private static final int TASK_UPDATE = 2;

        private final String tableName;
        private final JsonObject jsonObjectToUpdate;
        private final int taskType;
        private Callback<V> future;

        ListenableCallback(String tableName) {
            this.tableName = tableName;
            this.jsonObjectToUpdate = null;
            this.taskType = TASK_GET;
        }

        ListenableCallback(String tableName, JsonObject jsonObject) {
            this.tableName = tableName;
            this.jsonObjectToUpdate = jsonObject;
            this.taskType = TASK_UPDATE;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Void... params) {
            String URL = "content://hugoandrade.euro2016app.CloudDatabaseSimProvider";

            if (taskType == TASK_GET)
                getOperation(URL);
            else if (taskType == TASK_UPDATE)
                updateOperation(URL);

            return null;
        }

        @SuppressWarnings("unchecked")
        private void updateOperation(String URL) {
            Uri objects;
            if (tableName.equals("Country"))
                objects = Uri.parse(URL + "/" + tableName + "/" + NetworkUtils.getJsonPrimitive(jsonObjectToUpdate, "Name", "null"));
            else
                objects = Uri.parse(URL + "/" + tableName + "/" + NetworkUtils.getJsonPrimitive(jsonObjectToUpdate, "_id", "null"));

            int c = provider.update(objects, fromJsonObjectToContentValues(jsonObjectToUpdate), null, null);
            if (future == null)
                return;
            if (c == 0)
                future.onFailure("No item updated");
            else if (c == 1)
                future.onSuccess((V) jsonObjectToUpdate);
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

            JsonArray jsonArray = new JsonArray();

            if (c.moveToFirst()) {
                do{
                    jsonArray.add(fromCursorToJsonObject(c));
                } while (c.moveToNext());
            }
            c.close();

            try {
                if (future != null)
                    future.onSuccess((V) jsonArray);
            } catch (ClassCastException e) {
                future.onFailure("Cursor not found");
            }
        }

        private JsonObject fromCursorToJsonObject(Cursor c) {
            JsonObject jsonObject = new JsonObject();
            for (String columnName : c.getColumnNames())
                if (columnName.equals(SystemData.COLUMN_APP_STATE)) {
                    Log.e(TAG, "APP_STATE: " + Integer.toString(c.getInt(c.getColumnIndex(columnName))));
                    jsonObject.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)) == 1);
                }
                else
                    jsonObject.addProperty(columnName, c.getString(c.getColumnIndex(columnName)));
            return jsonObject;
        }

        private ContentValues fromJsonObjectToContentValues(JsonObject jsonObject) {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet())
                if (entry.getKey().equals(SystemData.COLUMN_APP_STATE)) {
                    values.put(entry.getKey(),
                            NetworkUtils.getJsonPrimitive(jsonObject, entry.getKey(), false)? 1 : 0);
                }
                else
                    values.put(entry.getKey(), NetworkUtils.getJsonPrimitive(jsonObject, entry.getKey(), null));
            return values;
        }

        void addListener(Callback<V> future) {
            this.future = future;
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
