package hugoandrade.euro2016;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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

import hugoandrade.euro2016.object.SystemData;
import hugoandrade.euro2016.utils.NetworkUtils;

class CloudDatabaseSimImpl {

    private final static String TAG = CloudDatabaseSimImpl.class.getSimpleName();

    @IntDef({TASK_UNKNOWN, TASK_GET, TASK_UPDATE, TASK_INSERT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TaskType {}

    @SuppressWarnings("unused") private static final int TASK_UNKNOWN = 0;
    private static final int TASK_GET = 1;
    private static final int TASK_UPDATE = 2;
    private static final int TASK_INSERT = 3;


    @SuppressWarnings("unused") private static final int CLOUD_SIM_DURATION = 0; // 1 seconds

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

    static void initialize(ContentResolver contentResolver) {
        CloudDatabaseSimImpl.provider = contentResolver;
    }

    public FilteringOperation where() {
        if (filteringOperationList == null)
            filteringOperationList = new ArrayList<>();

        FilteringOperation filteringOperation = new FilteringOperation(this);
        filteringOperationList.add(filteringOperation);
        return filteringOperation;
    }

    class FilteringOperation {
        CloudDatabaseSimImpl base;
        String field;
        String value;
        String operation;

        public FilteringOperation(CloudDatabaseSimImpl cloudDatabaseSim) {
            base = cloudDatabaseSim;
        }
        public FilteringOperation field(String field) {
            this.field = field;
            return this;
        }
        public FilteringOperation eq(String value) {
            this.operation = "=";
            this.value = value;
            return this;
        }
        public FilteringOperation lt(Number matchNo) {
            this.operation = "<";
            this.value = String.valueOf(matchNo);
            return this;
        }
        public FilteringOperation and() {
            return this.base.where();
            /*if (filteringOperationList == null)
                filteringOperationList = new ArrayList<>();

            FilteringOperation filteringOperation = new FilteringOperation(this);
            filteringOperationList.add(filteringOperation);
            return filteringOperation;/**/
        }

        public ListenableCallback<JsonElement> execute() {
            return base.execute();
        }
    }
    private List<FilteringOperation> filteringOperationList;

    private String[] selectFields;

    CloudDatabaseSimImpl select(String... fields) {
        this.selectFields = new String[fields.length];
        System.arraycopy(fields, 0, this.selectFields, 0, fields.length);
        return this;
    }

    class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused") private final String TAG = ListenableCallback.class.getSimpleName();

        private final String tableName;
        private final JsonObject jsonObjectToUpdate;
        private final List<Pair<String, String>> parameters;
        private Callback<V> future;
        @TaskType private final int taskType;

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

        @Override
        @SuppressWarnings("unchecked")
        protected Void doInBackground(Void... params) {
            String URL = "content://hugoandrade.euro2016app.CloudDatabaseSimProvider";

            if (taskType == TASK_GET)
                getOperation(URL);
            else if (taskType == TASK_UPDATE)
                updateOperation(URL);
            else if (taskType == TASK_INSERT)
                insertOperation(URL);

            return null;
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
                    jsonObjectToUpdate.addProperty("Parameter_" + entry.first, entry.second);

            Uri uri = provider.insert(objects, fromJsonObjectToContentValues(jsonObjectToUpdate));
            if (future == null)
                return;
            if (uri == null)
                future.onFailure("Operation error");
            else {
                if (!uri.toString().startsWith("content"))
                    future.onFailure(uri.toString());
                else {

                    Log.e(TAG, uri.toString());
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
            Cursor c = provider.query(objects, selectFields, selection, selectionArgs, null);
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
                if (columnName.equals(SystemData.COL_NAME_APP_STATE))
                    jsonObject.addProperty(columnName, c.getInt(c.getColumnIndex(columnName)) == 1);
                else
                    jsonObject.addProperty(columnName, c.getString(c.getColumnIndex(columnName)));
            return jsonObject;
        }

        private ContentValues fromJsonObjectToContentValues(JsonObject jsonObject) {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, JsonElement> entry: jsonObject.entrySet())
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
