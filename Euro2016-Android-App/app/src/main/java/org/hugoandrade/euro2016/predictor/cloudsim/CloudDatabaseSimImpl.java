package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.cloudsim.parser.CloudJsonObjectFormatter;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.network.HttpConstants;
import org.hugoandrade.euro2016.predictor.utils.ISO8601;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class CloudDatabaseSimImpl {

    private final static String TAG = CloudDatabaseSimImpl.class.getSimpleName();

    private final ContentProviderClient provider;

    private final String tableName;
    private final boolean isApi;
    private JsonObject apiJsonObject;
    private String apiType;

    private List<FilteringOperation> filteringOperationList;
    private String[] selectFields;
    private Pair<String, SortOrder> sortField;

    CloudDatabaseSimImpl(String table, ContentProviderClient contentProviderClient) {
        this.provider = contentProviderClient;
        this.tableName = table;
        this.isApi = false;
    }

    CloudDatabaseSimImpl(String tableName, JsonObject jsonObject, String apiType, ContentProviderClient contentProviderClient) {
        this.provider = contentProviderClient;
        this.tableName = tableName;
        this.apiJsonObject = jsonObject;
        this.apiType = apiType;
        this.isApi = true;
    }

    ListenableCallback<JsonElement> execute() {
        Log.d(TAG, "execute(getTable): " + tableName);
        ListenableCallback<JsonElement> task = new ListenableCallback<>(provider, tableName);

        if (isApi) {
            task.api(apiType, apiJsonObject);
        }
        task.execute(filteringOperationList, selectFields, sortField);
        return task;
    }

    ListenableCallback<JsonObject> update(JsonObject jsonObject) {
        Log.d(TAG, "update(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(provider, tableName, jsonObject);
        task.execute();
        return task;
    }

    ListenableCallback<JsonObject> insert(JsonObject jsonObject) {
        Log.d(TAG, "insert(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(provider, tableName, jsonObject, ListenableCallback.TASK_INSERT);
        task.execute();
        return task;
    }

    ListenableCallback<Void> delete(JsonObject jsonObject) {
        Log.d(TAG, "delete(" + tableName + "): " + jsonObject);
        ListenableCallback<Void> task = new ListenableCallback<>(provider, tableName, jsonObject, ListenableCallback.TASK_DELETE);
        task.execute();
        return task;
    }

    FilteringOperation where() {
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

        FilteringOperation(CloudDatabaseSimImpl cloudDatabaseSim) {
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
        FilteringOperation eq(int value) {
            this.operation = "=";
            this.value = String.valueOf(value);
            return this;
        }
        FilteringOperation lt(Number matchNo) {
            this.operation = "<";
            this.value = String.valueOf(matchNo);
            return this;
        }
        FilteringOperation ge(Number matchNo) {
            this.operation = ">=";
            this.value = String.valueOf(matchNo);
            return this;
        }
        FilteringOperation le(Number matchNo) {
            this.operation = "<=";
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

    public CloudDatabaseSimImpl orderBy(String field, SortOrder sortOrder) {
        sortField = new Pair<>(field, sortOrder);
        return this;
    }

    CloudDatabaseSimImpl select(String... fields) {
        this.selectFields = new String[fields.length];
        System.arraycopy(fields, 0, this.selectFields, 0, fields.length);
        return this;
    }

    static class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused")
        private final String TAG = ListenableCallback.class.getSimpleName();

        private static final int TASK_GET = 1;
        private static final int TASK_UPDATE = 2;
        private static final int TASK_INSERT = 3;
        private static final int TASK_DELETE = 4;
        private static final int TASK_API_GET = 5;
        private static final int TASK_API_POST = 6;

        private final ContentProviderClient provider;

        private List<Pair<String, String>> parameters;
        private List<FilteringOperation> filteringOperationList;
        private String[] selectFields;

        private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();
        private CloudJsonObjectFormatter jsonFormatter = new CloudJsonObjectFormatter();

        private final String tableName;
        private JsonObject jsonObject;
        private int taskType;
        private Callback<V> mFuture;
        private Pair<String, SortOrder> sortField;

        ListenableCallback(ContentProviderClient contentProviderClient, String tableName) {
            this(contentProviderClient, tableName, null, TASK_GET);
        }

        ListenableCallback(ContentProviderClient contentProviderClient, String tableName, JsonObject jsonObject) {
            this(contentProviderClient, tableName, jsonObject, TASK_UPDATE);
        }

        ListenableCallback(ContentProviderClient contentProviderClient, String tableName, JsonObject jsonObject, int taskType) {
            provider = contentProviderClient;
            this.tableName = tableName;
            this.jsonObject = jsonObject;
            this.taskType = taskType;
        }

        ListenableCallback<V> api(String apiType, JsonObject apiJsonObject) {
            this.jsonObject = apiJsonObject;
            if (apiType.equals(HttpConstants.PostMethod))
                this.taskType = TASK_API_POST;
            else if (apiType.equals(HttpConstants.GetMethod))
                this.taskType = TASK_API_GET;
            return this;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Uri uri = CloudDatabaseSimAdapter.BASE_URI.buildUpon()
                    .appendPath(tableName)
                    .build();

            try {

                if (taskType == TASK_GET)
                    getOperation(uri);
                else if (taskType == TASK_UPDATE)
                    updateOperation(uri);
                else if (taskType == TASK_DELETE)
                    deleteOperation(uri);
                else if (taskType == TASK_INSERT)
                    insertOperation(uri);
                else if (taskType == TASK_API_POST)
                    postApiOperation(uri);
                else if (taskType == TASK_API_GET)
                    getApiOperation(uri);

            } catch (Exception e) {
                if (mFuture != null)
                    mFuture.onFailure(e.getMessage());
            }

            return null;
        }

        void execute(List<FilteringOperation> filteringOperationList, String[] selectFields, Pair<String, SortOrder> sortField) {
            this.filteringOperationList = filteringOperationList;
            this.selectFields = selectFields;
            this.sortField = sortField;
            this.execute();
        }

        private void getApiOperation(Uri baseUri) throws RemoteException {

            Cursor c = provider.query(baseUri, null, null, null, null);

            if (c == null) {
                throw new IllegalArgumentException("Cursor not found");
            }

            JsonArray jsonArray = jsonFormatter.getAsJsonArray(c);

            try {
                if (mFuture != null) {
                    if (jsonArray.size() == 0)
                        mFuture.onSuccess(null);
                    else
                        mFuture.onSuccess((V) adjustIfItIsSystemData(jsonArray.get(0)));
                }
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Cursor not found");
            }
        }

        private void postApiOperation(Uri baseUri) throws RemoteException {
            Uri uri = provider.insert(baseUri, jsonObject == null ? null: cvFormatter.getAsContentValues(jsonObject));

            if (uri == null)
                throw new IllegalArgumentException("Operation failed");

            Cursor c = provider.query(uri, null, null, null, null);

            if (c == null) {
                throw new IllegalArgumentException("Cursor not found");
            }

            if (c.moveToFirst()) {
                try {
                    if (mFuture != null)
                        mFuture.onSuccess((V) adjustIfItIsLoginData(jsonFormatter.getAsJsonObject(c)));
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Could not cast");
                }
            }
            c.close();
        }

        private void updateOperation(Uri baseUri) throws RemoteException {
            ContentValues contentValues = cvFormatter.getAsContentValues(jsonObject);

            Uri url = baseUri.buildUpon()
                    .appendPath(contentValues.getAsString("_id"))
                    .build();

            int count = provider.update(url,
                    contentValues,
                    null,
                    null);

            if (mFuture == null)
                return;
            if (count == 0)
                throw new IllegalArgumentException("No item updated");
            else if (count == 1) {

                Cursor c = provider.query(url, null, null, null, null);

                if (c == null) {
                    throw new IllegalArgumentException("Cursor not found");
                }

                JsonArray jsonArray = jsonFormatter.getAsJsonArray(c);

                try {
                    if (jsonArray.size() != 1)
                        throw new IllegalArgumentException("Multiple items updated. Please, retrieve all items again.");

                    if (mFuture != null)
                        mFuture.onSuccess((V) jsonArray.get(0));

                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Cursor not found");
                }
            }
            else
                throw new IllegalArgumentException("Multiple items updated. Please, retrieve all items again.");
        }

        private void deleteOperation(Uri baseUri) throws RemoteException {
            Uri url = baseUri.buildUpon()
                    .appendPath(cvFormatter.getAsContentValues(jsonObject).getAsString("_id"))
                    .build();

            int c = provider.delete(url,
                    null,
                    null);
            if (mFuture == null)
                return;
            if (c == 0)
                throw new IllegalArgumentException("No item deleted");
            else if (c == 1)
                mFuture.onSuccess(null);
            else
                throw new IllegalArgumentException("Multiple items deleted. Please, retrieve all items again.");
        }

        private void insertOperation(Uri baseUri) throws RemoteException {

            Uri uri = provider.insert(baseUri, cvFormatter.getAsContentValues(jsonObject));

            if (mFuture == null)
                return;

            if (uri == null)
                throw new IllegalArgumentException("No item inserted");

            Cursor c = provider.query(uri, null, null, null, null);

            if (c == null || c.getCount() == 0) {
                throw new IllegalArgumentException("No item inserted");
            }

            c.moveToFirst();

            mFuture.onSuccess((V) jsonFormatter.getAsJsonObject(c));

            c.close();
        }

        private void getOperation(Uri baseUri) throws RemoteException {

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

            String sortField = null;
            if (this.sortField != null) {
                sortField = this.sortField.first + " " + (this.sortField.second == SortOrder.Ascending ? "ASC" : "DESC");
            }

            Cursor c = provider.query(baseUri, selectFields, selection, selectionArgs, sortField);

            if (c == null) {
                throw new IllegalArgumentException("Cursor not found");
            }

            JsonArray jsonArray = jsonFormatter.getAsJsonArray(c);

            try {
                if (mFuture != null)
                    mFuture.onSuccess((V) jsonArray);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Could not cast");
            }
        }

        void addListener(Callback<V> future) {
            this.mFuture = future;
        }
    }

    static <V> void addCallback(ListenableCallback<V> listenable, Callback<V> future) {
        listenable.addListener(future);
    }

    interface Callback<V> {
        void onSuccess(V result);
        void onFailure(String errorMessage);
    }

    private static String getJsonPrimitive(JsonObject jsonObject, String jsonMemberName, String defaultValue) {
        try {
            return jsonObject.getAsJsonPrimitive(jsonMemberName).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static JsonObject adjustIfItIsLoginData(JsonObject jsonObject) {
        Log.e(TAG, "adjustIfItIsLoginData: " + jsonObject.toString());

        if (jsonObject.has(LoginData.Entry.Cols.EMAIL) &&
                jsonObject.has(LoginData.Entry.Cols.PASSWORD)) {

            jsonObject.addProperty(LoginData.Entry.Cols.USER_ID, jsonObject.get("id").getAsString());
            jsonObject.remove("id");

            Log.e(TAG, "adjustIfItIsLoginData: " + jsonObject.toString());
            return jsonObject;
        }
        return jsonObject;
    }

    private static JsonElement adjustIfItIsSystemData(JsonElement jsonElement) {

        if (!jsonElement.isJsonObject())
            return jsonElement;

        final String DATE_OF_CHANGE = "DateOfChange";
        if (jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.RULES)
                && jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.APP_STATE)
                && jsonElement.getAsJsonObject().has(DATE_OF_CHANGE)
                && jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.SYSTEM_DATE)) {

            Calendar dateOfChange =
                    ISO8601.toCalendar(getJsonPrimitive(jsonElement.getAsJsonObject(), DATE_OF_CHANGE, null));
            Calendar systemDate =
                    ISO8601.toCalendar(getJsonPrimitive(jsonElement.getAsJsonObject(), SystemData.Entry.Cols.SYSTEM_DATE, null));

            if (dateOfChange != null && systemDate != null) {
                Calendar c = Calendar.getInstance();
                long diff = c.getTimeInMillis() - dateOfChange.getTimeInMillis();
                systemDate.setTimeInMillis(systemDate.getTimeInMillis() + diff);

                jsonElement.getAsJsonObject().remove(DATE_OF_CHANGE);
                jsonElement.getAsJsonObject().addProperty(SystemData.Entry.Cols.SYSTEM_DATE, ISO8601.fromCalendar(systemDate));
            }
            else {
                return jsonElement;
            }
        }/**/
        return jsonElement;
    }/**/

    public enum SortOrder {
        Ascending, Descending
    }
}
