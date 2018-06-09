package org.hugoandrade.euro2016.predictor.cloudsim;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.content.ContentResolverCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.cloudsim.parser.CloudJsonObjectFormatter;
import org.hugoandrade.euro2016.predictor.cloudsim.data.League;
import org.hugoandrade.euro2016.predictor.cloudsim.data.LoginData;
import org.hugoandrade.euro2016.predictor.cloudsim.data.SystemData;
import org.hugoandrade.euro2016.predictor.cloudsim.network.HttpConstants;
import org.hugoandrade.euro2016.predictor.cloudsim.utils.ISO8601;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CloudDatabaseSimImpl {

    private final static String TAG = CloudDatabaseSimImpl.class.getSimpleName();

    private final ContentResolver provider;
    private final ContentProviderClient providerClient;

    private final String tableName;
    private final boolean isApi;
    private JsonObject apiJsonObject;
    private String apiType;

    private List<FilteringOperation> filteringOperationList;
    private String[] selectFields;
    private Pair<String, SortOrder> sortField;
    private int skip = -1;
    private int top = -1;

    public CloudDatabaseSimImpl(String table, ContentResolver contentResolver) {
        this.providerClient = null;
        this.provider = contentResolver;
        this.tableName = table;
        this.isApi = false;
    }

    public CloudDatabaseSimImpl(String table, ContentProviderClient contentResolverClient) {
        this.providerClient = contentResolverClient;
        this.provider = null;
        this.tableName = table;
        this.isApi = false;
    }

    public CloudDatabaseSimImpl(String tableName,
                         JsonObject jsonObject,
                         String apiType,
                         ContentProviderClient contentResolverClient) {
        this.providerClient = contentResolverClient;
        this.provider = null;
        this.tableName = tableName;
        this.apiJsonObject = jsonObject;
        this.apiType = apiType;
        this.isApi = true;
    }

    public CloudDatabaseSimImpl(String tableName,
                         JsonObject jsonObject,
                         String apiType,
                         ContentResolver contentResolver) {
        this.providerClient = null;
        this.provider = contentResolver;
        this.tableName = tableName;
        this.apiJsonObject = jsonObject;
        this.apiType = apiType;
        this.isApi = true;
    }

    public CloudDatabaseSimImpl skip(int skip) {
        this.skip = skip;
        return this;
    }

    public CloudDatabaseSimImpl top(int top) {
        this.top = top;
        return this;
    }

    public FilteringOperation where() {
        if (filteringOperationList == null)
            filteringOperationList = new ArrayList<>();

        FilteringOperation filteringOperation = new FilteringOperation(this);
        filteringOperationList.add(filteringOperation);
        return filteringOperation;
    }

    public class FilteringOperation {
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
        public FilteringOperation eq(int value) {
            this.operation = "=";
            this.value = String.valueOf(value);
            return this;
        }
        public FilteringOperation lt(Number matchNo) {
            this.operation = "<";
            this.value = String.valueOf(matchNo);
            return this;
        }
        public FilteringOperation ge(Number matchNo) {
            this.operation = ">=";
            this.value = String.valueOf(matchNo);
            return this;
        }
        public FilteringOperation le(Number matchNo) {
            this.operation = "<=";
            this.value = String.valueOf(matchNo);
            return this;
        }

        public FilteringOperation and() {
            return this.base.where();
        }

        public ListenableCallback<JsonElement> execute() {
            return base.execute();
        }
    }

    public CloudDatabaseSimImpl orderBy(String field, SortOrder sortOrder) {
        sortField = new Pair<>(field, sortOrder);
        return this;
    }

    public CloudDatabaseSimImpl select(String... fields) {
        this.selectFields = new String[fields.length];
        System.arraycopy(fields, 0, this.selectFields, 0, fields.length);
        return this;
    }

    public ListenableCallback<JsonElement> execute() {
        Log.d(TAG, "execute(getTable): " + tableName);
        ListenableCallback<JsonElement> task = new ListenableCallback<>(provider, tableName);

        if (isApi) {
            task.api(apiType, apiJsonObject);
        }
        else {
            task.skip(skip);
            task.top(top);
        }
        task.execute(filteringOperationList, selectFields, sortField);
        return task;
    }

    public ListenableCallback<JsonObject> update(JsonObject jsonObject) {
        Log.d(TAG, "update(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(provider, tableName, jsonObject);
        task.execute();
        return task;
    }

    public ListenableCallback<JsonObject> insert(JsonObject jsonObject) {
        Log.d(TAG, "insert(" + tableName + "): " + jsonObject);
        ListenableCallback<JsonObject> task = new ListenableCallback<>(provider, tableName, jsonObject, ListenableCallback.TASK_INSERT);
        task.execute();
        return task;
    }

    public ListenableCallback<Void> delete(JsonObject jsonObject) {
        Log.d(TAG, "delete(" + tableName + "): " + jsonObject);
        ListenableCallback<Void> task = new ListenableCallback<>(provider, tableName, jsonObject, ListenableCallback.TASK_DELETE);
        task.execute();
        return task;
    }

    public static class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused")
        private final String TAG = ListenableCallback.class.getSimpleName();

        private static final int TASK_GET = 1;
        private static final int TASK_UPDATE = 2;
        private static final int TASK_INSERT = 3;
        private static final int TASK_DELETE = 4;
        private static final int TASK_API_GET = 5;
        private static final int TASK_API_POST = 6;

        private final ContentResolver provider;
        private final ContentProviderClient providerClient;

        private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();
        private CloudJsonObjectFormatter jsonFormatter = new CloudJsonObjectFormatter();

        private final String tableName;
        private JsonObject jsonObject;
        private int taskType;
        private Callback<V> mFuture;
        private Pair<String, SortOrder> sortField;
        private int skip = -1;
        private int top = -1;

        private List<Pair<String, String>> parameters;
        private List<FilteringOperation> filteringOperationList;
        private String[] selectFields;

        ListenableCallback(ContentResolver contentResolver, String tableName) {
            this(contentResolver, tableName, null, TASK_GET);
        }

        ListenableCallback(ContentResolver contentResolver, String tableName, JsonObject jsonObject) {
            this(contentResolver, tableName, jsonObject, TASK_UPDATE);
        }

        ListenableCallback(ContentResolver contentResolver, String tableName, JsonObject jsonObject, int taskType) {
            this.providerClient = null;
            this.provider = contentResolver;
            this.tableName = tableName;
            this.jsonObject = jsonObject;
            this.taskType = taskType;
        }

        ListenableCallback(ContentProviderClient contentProviderClient, String tableName) {
            this(contentProviderClient, tableName, null, TASK_GET);
        }

        ListenableCallback(ContentProviderClient contentProviderClient, String tableName, JsonObject jsonObject) {
            this(contentProviderClient, tableName, jsonObject, TASK_UPDATE);
        }

        ListenableCallback(ContentProviderClient contentProviderClient, String tableName, JsonObject jsonObject, int taskType) {
            this.providerClient = contentProviderClient;
            this.provider = null;
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

        public void skip(int skip) {
            this.skip = skip;
        }

        public void top(int top) {
            this.top = top;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Uri uri = CloudDatabaseSimProvider.BASE_URI.buildUpon()
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

        private void getApiOperation(Uri baseUri) {

            /*Uri.Builder builder = baseUri.buildUpon();
            if (top != -1) {
                builder.appendQueryParameter(CloudDatabaseSimVariables.QUERY_PARAMETER_LIMIT, String.valueOf(top));
            }
            if (skip != -1) {
                builder.appendQueryParameter(CloudDatabaseSimVariables.QUERY_PARAMETER_OFFSET, String.valueOf(skip));
            }

            Uri uri = builder.build();
            Log.e(TAG, "::" + uri);

            Cursor c = provider.query(uri, null, null, null, null);/**/
            Cursor c;
            if (provider != null)
                c = provider.query(baseUri, null, null, null, null);
            else if (providerClient != null)
            {
                try {
                    c = providerClient.query(baseUri, null, null, null, null);
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("Cursor not found");
                }
            }
            else {
                throw new IllegalArgumentException("Cursor not found");
            }

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

        private void postApiOperation(Uri baseUri) {
            Uri uri;
            if (provider != null) {
                uri = provider.insert(baseUri, jsonObject == null ? null : cvFormatter.getAsContentValues(jsonObject));
            }
            else if (providerClient != null) {
                try {
                    uri = providerClient.insert(baseUri, jsonObject == null ? null: cvFormatter.getAsContentValues(jsonObject));
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("Cursor not found");
                }
            }
            else {
                throw new IllegalArgumentException("Cursor not found");
            }

            if (tableName.contains(SystemData.Entry.API_NAME_UPDATE_SCORES) ||
                    tableName.contains(League.Entry.API_NAME_DELETE_LEAGUE) ||
                    tableName.contains(League.Entry.API_NAME_LEAVE_LEAGUE)) {
                if (mFuture != null)
                    mFuture.onSuccess(null);

                return;
            }

            if (uri == null)
                throw new IllegalArgumentException("Operation failed");

            if (tableName.contains(SystemData.Entry.API_NAME)) {
                getApiOperation(baseUri);
                return;
            }

            Cursor c;
            if (provider != null) {
                c = provider.query(uri, null, null, null, null);
            }
            else if (providerClient != null) {
                try {
                    c = providerClient.query(uri, null, null, null, null);
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("Cursor not found");
                }
            }
            else {
                throw new IllegalArgumentException("Cursor not found");
            }

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

        private void updateOperation(Uri baseUri) {
            ContentValues contentValues = cvFormatter.getAsContentValues(jsonObject);

            Uri url = baseUri.buildUpon()
                    .appendPath(contentValues.getAsString("_id"))
                    .build();

            int count;
            if (provider != null) {
                count = provider.update(url, contentValues, null, null);
            }
            else if (providerClient != null) {
                try {
                    count = providerClient.update(url, contentValues, null, null);
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("Cursor not found");
                }
            }
            else {
                throw new IllegalArgumentException("No item updated");
            }

            if (mFuture == null)
                return;
            if (count == 0)
                throw new IllegalArgumentException("No item updated");
            else if (count == 1) {
                Cursor c;
                if (provider != null) {
                    c = provider.query(url, null, null, null, null);
                }
                else if (providerClient != null) {
                    try {
                        c = providerClient.query(url, null, null, null, null);
                    } catch (RemoteException | NullPointerException e) {
                        throw new IllegalArgumentException("Cursor not found");
                    }
                }
                else {
                    throw new IllegalArgumentException("Cursor not found");
                }


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

        private void deleteOperation(Uri baseUri) {
            Uri url = baseUri.buildUpon()
                    .appendPath(cvFormatter.getAsContentValues(jsonObject).getAsString("_id"))
                    .build();

            int c;
            if (provider != null) {
                c = provider.delete(url, null, null);
            }
            else if (providerClient != null) {
                try {
                    c = providerClient.delete(url, null, null);
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("No item deleted");
                }
            }
            else {
                throw new IllegalArgumentException("No item deleted");
            }

            if (mFuture == null)
                return;
            if (c == 0)
                throw new IllegalArgumentException("No item deleted");
            else if (c == 1)
                mFuture.onSuccess(null);
            else
                throw new IllegalArgumentException("Multiple items deleted. Please, retrieve all items again.");
        }

        private void insertOperation(Uri baseUri) {

            Uri uri;
            if (provider != null) {
                uri = provider.insert(baseUri, cvFormatter.getAsContentValues(jsonObject));
            }
            else if (providerClient != null) {
                try {
                    uri = providerClient.insert(baseUri, cvFormatter.getAsContentValues(jsonObject));
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("No item inserted");
                }
            }
            else {
                throw new IllegalArgumentException("No item inserted");
            }

            if (mFuture == null)
                return;

            if (uri == null)
                throw new IllegalArgumentException("No item inserted");

            Cursor c;
            if (provider != null) {
                c = provider.query(uri, null, null, null, null);
            }
            else if (providerClient != null) {
                try {
                    c = providerClient.query(uri, null, null, null, null);
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("No item inserted");
                }
            }
            else {
                throw new IllegalArgumentException("No item inserted");
            }

            if (c == null || c.getCount() == 0) {
                throw new IllegalArgumentException("No item inserted");
            }

            c.moveToFirst();

            mFuture.onSuccess((V) jsonFormatter.getAsJsonObject(c));

            c.close();
        }

        private void getOperation(Uri baseUri) {

            Uri.Builder builder = baseUri.buildUpon();
            if (top != -1) {
                builder.appendQueryParameter(CloudDatabaseSimVariables.QUERY_PARAMETER_LIMIT, String.valueOf(top));
            }
            if (skip != -1) {
                builder.appendQueryParameter(CloudDatabaseSimVariables.QUERY_PARAMETER_OFFSET, String.valueOf(skip));
            }

            Uri uri = builder.build();

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

            Cursor c;
            if (provider != null) {
                c = provider.query(uri, selectFields, selection, selectionArgs, sortField);
            }
            else if (providerClient != null) {
                try {
                    c = providerClient.query(uri, selectFields, selection, selectionArgs, sortField);
                } catch (RemoteException | NullPointerException e) {
                    throw new IllegalArgumentException("Cursor not found");
                }
            }
            else {
                throw new IllegalArgumentException("Cursor not found");
            }

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

    public static <V> void addCallback(ListenableCallback<V> listenable, Callback<V> future) {
        listenable.addListener(future);
    }

    public interface Callback<V> {
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

        if (jsonObject.has(LoginData.Entry.Cols.EMAIL) &&
                jsonObject.has(LoginData.Entry.Cols.PASSWORD)) {

            jsonObject.addProperty(LoginData.Entry.Cols.USER_ID, jsonObject.get("id").getAsString());
            jsonObject.remove("id");

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
