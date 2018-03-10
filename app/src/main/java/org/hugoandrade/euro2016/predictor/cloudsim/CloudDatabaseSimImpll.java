package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hugoandrade.euro2016.predictor.data.SystemData;

class CloudDatabaseSimImpll {

    private final static String TAG = CloudDatabaseSimImpll.class.getSimpleName();

    @IntDef({TASK_UNKNOWN, TASK_GET, TASK_UPDATE, TASK_INSERT, TASK_API})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TaskType {}

    @SuppressWarnings("unused") private static final int TASK_UNKNOWN = 0;
    private static final int TASK_GET = 1;
    private static final int TASK_UPDATE = 2;
    private static final int TASK_INSERT = 3;
    private static final int TASK_API = 4;
    private static final int TASK_API_GET = 5;
    private static final int TASK_API_POST = 6;


    @SuppressWarnings("unused") private static final int CLOUD_SIM_DURATION = 0; // 1 seconds

    private static ContentProviderClient provider;
    private static Uri BASE_URL;
    private final String tableName;

    private final boolean isApi;
    private JsonObject apiJsonObject;
    private String apiType;

    private List<FilteringOperation> filteringOperationList;
    private String[] selectFields;

    CloudDatabaseSimImpll(String table) {
        tableName = table;
        isApi = false;
    }

    CloudDatabaseSimImpll(String tableName, JsonObject jsonObject, String apiType) {
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
        task.execute(filteringOperationList, selectFields);
        return task;
    }

    ListenableCallback<JsonObject> update(JsonObject jsonObject) {
        Log.d(TAG, "update(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject, TASK_UPDATE);
        task.execute();
        return task;
    }

    ListenableCallback<JsonObject> insert(JsonObject jsonObject) {
        Log.d(TAG, "insert(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject, TASK_INSERT);
        task.execute();
        return task;
    }

    ListenableCallback<JsonObject> insert(JsonObject jsonObject, List<Pair<String, String>> parameters) {
        Log.d(TAG, "insert(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(tableName, jsonObject, parameters, TASK_INSERT);
        task.execute();
        return task;
    }

    static ListenableCallback<JsonObject> invokeApi(String apiName, JsonObject jsonObject) {
        ListenableCallback<JsonObject> task = new ListenableCallback<>(apiName, jsonObject, TASK_API);
        task.execute();
        return task;
    }

    static void initialize(ContentResolver contentResolver, Uri baseURL) {
        CloudDatabaseSimImpll.BASE_URL = baseURL;
        CloudDatabaseSimImpll.provider = contentResolver.acquireContentProviderClient(CloudDatabaseSimImpll.BASE_URL);
    }

    FilteringOperation where() {
        if (filteringOperationList == null)
            filteringOperationList = new ArrayList<>();

        FilteringOperation filteringOperation = new FilteringOperation(this);
        filteringOperationList.add(filteringOperation);
        return filteringOperation;
    }

    class FilteringOperation {
        CloudDatabaseSimImpll base;
        String field;
        String value;
        String operation;

        FilteringOperation(CloudDatabaseSimImpll cloudDatabaseSim) {
            base = cloudDatabaseSim;
        }

        FilteringOperation field(String field) {
            this.field = field;
            return this;
        }
        FilteringOperation eq(String value) {
            this.operation = "=";
            this.value = value;
            return this;
        }
        FilteringOperation lt(Number matchNo) {
            this.operation = "<";
            this.value = String.valueOf(matchNo);
            return this;
        }
        FilteringOperation and() {
            return this.base.where();
        }
        ListenableCallback<JsonElement> execute() {
            return base.execute();
        }
    }

    CloudDatabaseSimImpll select(String... fields) {
        this.selectFields = new String[fields.length];
        System.arraycopy(fields, 0, this.selectFields, 0, fields.length);
        return this;
    }

    static class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused") private final String TAG = ListenableCallback.class.getSimpleName();

        private final String tableName;
        private JsonObject jsonObjectToUpdate;
        private final List<Pair<String, String>> parameters;
        private List<FilteringOperation> filteringOperationList;
        private String[] selectFields;
        private Callback<V> future;
        @TaskType private int taskType;

        private CloudJsonFormatter jsonFormatter = new CloudJsonFormatter();

        ListenableCallback(String tableName) {
            this.tableName = tableName;
            this.jsonObjectToUpdate = null;
            this.taskType = TASK_GET;
            this.parameters = null;
        }

        ListenableCallback(String tableName, JsonObject jsonObject, @TaskType int taskType) {
            this.tableName = tableName;
            this.jsonObjectToUpdate = jsonObject;
            this.taskType = taskType;
            this.parameters = null;
        }

        ListenableCallback(String tableName, JsonObject jsonObject, List<Pair<String, String>> parameters, @TaskType int taskType) {
            this.tableName = tableName;
            this.jsonObjectToUpdate = jsonObject;
            this.taskType = taskType;
            this.parameters = parameters;
        }

        void execute(List<FilteringOperation> filteringOperationList, String[] selectFields) {
            this.filteringOperationList = filteringOperationList;
            this.selectFields = selectFields;
            this.execute();
        }

        ListenableCallback<V> api(String apiType, JsonObject apiJsonObject) {
            this.jsonObjectToUpdate = apiJsonObject;
            if (apiType.equals("POST"))
                this.taskType = TASK_API_POST;
            else if (apiType.equals("GET"))
                this.taskType = TASK_API_GET;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Void... params) {
            String URL = CloudDatabaseSimImpll.BASE_URL.toString();//"content://hugoandrade.euro2016app.CloudDatabaseSimProvider";

            if (taskType == TASK_GET)
                getOperation(URL);
            else if (taskType == TASK_UPDATE)
                updateOperation(URL);
            else if (taskType == TASK_INSERT)
                insertOperation(URL);
            else if (taskType == TASK_API_POST)
                postApiOperation(URL);
            else if (taskType == TASK_API_GET)
                getApiOperation(URL);

            return null;
        }

        private void getApiOperation(String URL) {
            Uri objects = Uri.parse(URL + "/" + tableName);

            Cursor c = null;
            try {
                c = provider.query(objects, null, null, null, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (c == null) {
                if (future != null)
                    future.onFailure("Cursor not found");
                return;
            }

            Log.e(TAG, Integer.toString(c.getCount()));
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

            Uri uri = null;
            try {
                uri = provider.insert(url, jsonFormatter.getAsContentValues(jsonObjectToUpdate));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (future == null)
                return;
            if (uri == null)
                future.onFailure("Operation failed");
            else {
                if (!uri.toString().startsWith("content://"))
                    future.onFailure(uri.toString());
                else {

                    Cursor c = null;
                    try {
                        c = provider.query(uri, null, null, null, null);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (c == null) {
                        if (future != null)
                            future.onFailure("Cursor not found");
                        return;
                    }

                    if (c.moveToFirst()) {
                        try {
                            if (future != null)
                                future.onSuccess((V) parseUserID(fromCursorToJsonObject(c)));
                        } catch (ClassCastException e) {
                            future.onFailure("Could not cast");
                        }
                    }
                    c.close();
                }
            }
        }

        private JsonObject parseUserID(JsonObject jsonObject) {
            jsonObject.addProperty("UserID", jsonObject.get("id").getAsString());
            jsonObject.remove("id");
            return jsonObject;
        }

        @SuppressWarnings("unchecked")
        private void insertOperation(String URL) {
            Uri objects;
            //if (tableName.equals("Country"))
                objects = Uri.parse(URL + "/" + tableName);// + "/" + NetworkUtils.getJsonPrimitive(jsonObjectToUpdate, "Name", "null"));
            //else
              //  objects = Uri.parse(URL + "/" + tableName + "/" + NetworkUtils.getJsonPrimitive(jsonObjectToUpdate, "_id", "null"));

            if (parameters != null && jsonObjectToUpdate != null)
                for (Pair<String, String> entry : parameters)
                    jsonObjectToUpdate.addProperty(entry.first, entry.second);

            Uri uri = null;
            try {
                uri = provider.insert(objects, fromJsonObjectToContentValues(jsonObjectToUpdate));
            } catch (RemoteException e) {
                future.onFailure("Operation error: " + e.getMessage());
            }
            if (future == null)
                return;
            if (uri == null)
                future.onFailure("Operation error");
            else {
                if (!uri.toString().startsWith("content"))
                    future.onFailure(uri.toString());
                else {

                    Cursor c = null;
                    try {
                        c = provider.query(uri, null, null, null, null);
                    } catch (RemoteException e) {
                        future.onFailure("Operation error: " + e.getMessage());
                    }
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
            Uri objects;
            if (tableName.equals("Country"))
                objects = Uri.parse(URL + "/" + tableName + "/" + getJsonPrimitive(jsonObjectToUpdate, "Name", "null"));
            else
                objects = Uri.parse(URL + "/" + tableName + "/" + getJsonPrimitive(jsonObjectToUpdate, "_id", "null"));

            int c = 0;
            try {
                c = provider.update(objects, fromJsonObjectToContentValues(jsonObjectToUpdate), null, null);
            } catch (RemoteException e) {
                future.onFailure("Operation error: " + e.getMessage());
            }
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

            String selection = null;
            String[] selectionArgs = null;
            if (filteringOperationList != null){
                selection = "";
                selectionArgs = new String[filteringOperationList.size()];
                for (int i = 0 ; i < filteringOperationList.size() ; i++) {
                    FilteringOperation f = filteringOperationList.get(i);
                    if (i != 0)
                        selection = selection + " AND ";
                    selection = selection + f.field + " " + f.operation + " ? ";
                    selectionArgs[i] = f.value;
                }
            }
            Cursor c = null;
            try {
                c = provider.query(objects, selectFields, selection, selectionArgs, null);
            } catch (RemoteException e) {
                future.onFailure("Operation error: " + e.getMessage());
            }
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
                if (columnName.equals(SystemData.Entry.Cols.APP_STATE))
                    jsonObject.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)) == 1);
                else {
                    if (columnName.equals("_id"))
                        jsonObject.addProperty("id", Integer.toString(c.getInt(c.getColumnIndex(columnName))));
                    else
                        jsonObject.addProperty(columnName, c.getString(c.getColumnIndex(columnName)));
                }
            return jsonObject;
        }

        private ContentValues fromJsonObjectToContentValues(JsonObject jsonObject) {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet())
                values.put(entry.getKey(), getJsonPrimitive(jsonObject, entry.getKey(), null));
            return values;
        }

        void addListener(Callback<V> future) {
            this.future = future;
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
