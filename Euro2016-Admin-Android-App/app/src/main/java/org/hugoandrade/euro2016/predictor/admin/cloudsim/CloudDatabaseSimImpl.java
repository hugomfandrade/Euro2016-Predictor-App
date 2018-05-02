package org.hugoandrade.euro2016.predictor.admin.cloudsim;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudContentValuesFormatter;
import org.hugoandrade.euro2016.predictor.admin.cloudsim.parser.CloudJsonObjectFormatter;
import org.hugoandrade.euro2016.predictor.admin.data.League;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;
import org.hugoandrade.euro2016.predictor.admin.network.HttpConstants;
import org.hugoandrade.euro2016.predictor.admin.utils.ISO8601;

import java.util.Calendar;

class CloudDatabaseSimImpl {

    private final static String TAG = CloudDatabaseSimImpl.class.getSimpleName();

    private final ContentResolver provider;

    private final String tableName;
    private final boolean isApi;
    private JsonObject apiJsonObject;
    private String apiType;
    private int skip = -1;
    private int top = -1;

    CloudDatabaseSimImpl(String table, ContentResolver contentResolver) {
        this.provider = contentResolver;
        this.tableName = table;
        this.isApi = false;
    }

    CloudDatabaseSimImpl(String tableName, JsonObject jsonObject, String apiType, ContentResolver contentResolver) {
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

    ListenableCallback<JsonElement> execute() {
        Log.d(TAG, "execute(getTable): " + tableName);
        ListenableCallback<JsonElement> task = new ListenableCallback<>(provider, tableName);

        if (isApi) {
            task.api(apiType, apiJsonObject);
        }
        else {
            task.skip(skip);
            task.top(top);
        }
        task.execute();
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

    static class ListenableCallback<V> extends AsyncTask<Void, Void, Void> {

        @SuppressWarnings("unused")
        private final String TAG = ListenableCallback.class.getSimpleName();

        private static final int TASK_GET = 1;
        private static final int TASK_UPDATE = 2;
        private static final int TASK_INSERT = 3;
        private static final int TASK_DELETE = 4;
        private static final int TASK_API_GET = 5;
        private static final int TASK_API_POST = 6;

        private final ContentResolver provider;

        private CloudContentValuesFormatter cvFormatter = new CloudContentValuesFormatter();
        private CloudJsonObjectFormatter jsonFormatter = new CloudJsonObjectFormatter();

        private final String tableName;
        private JsonObject jsonObject;
        private int taskType;
        private Callback<V> mFuture;
        private int skip = -1;
        private int top = -1;

        ListenableCallback(ContentResolver contentResolver, String tableName) {
            this(contentResolver, tableName, null, TASK_GET);
        }

        ListenableCallback(ContentResolver contentResolver, String tableName, JsonObject jsonObject) {
            this(contentResolver, tableName, jsonObject, TASK_UPDATE);
        }

        ListenableCallback(ContentResolver contentResolver, String tableName, JsonObject jsonObject, int taskType) {
            provider = contentResolver;
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

        private void postApiOperation(Uri baseUri) {
            Uri uri = provider.insert(baseUri, jsonObject == null ? null: cvFormatter.getAsContentValues(jsonObject));


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

        private void updateOperation(Uri baseUri) {
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

        private void deleteOperation(Uri baseUri) {
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

        private void insertOperation(Uri baseUri) {

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

        private void getOperation(Uri baseUri) {

            Uri.Builder builder = baseUri.buildUpon();
            if (top != -1) {
                builder.appendQueryParameter(CloudDatabaseSimVariables.QUERY_PARAMETER_LIMIT, String.valueOf(top));
            }
            if (skip != -1) {
                builder.appendQueryParameter(CloudDatabaseSimVariables.QUERY_PARAMETER_OFFSET, String.valueOf(skip));
            }

            Uri uri = builder.build();

            Cursor c = provider.query(uri, null, null, null, null);

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

        if (jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.RULES)
                && jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.APP_STATE)
                && jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.DATE_OF_CHANGE)
                && jsonElement.getAsJsonObject().has(SystemData.Entry.Cols.SYSTEM_DATE)) {

            Calendar dateOfChange =
                    ISO8601.toCalendar(getJsonPrimitive(jsonElement.getAsJsonObject(), SystemData.Entry.Cols.DATE_OF_CHANGE, null));
            Calendar systemDate =
                    ISO8601.toCalendar(getJsonPrimitive(jsonElement.getAsJsonObject(), SystemData.Entry.Cols.SYSTEM_DATE, null));

            if (dateOfChange != null && systemDate != null) {
                Calendar c = Calendar.getInstance();
                long diff = c.getTimeInMillis() - dateOfChange.getTimeInMillis();
                systemDate.setTimeInMillis(systemDate.getTimeInMillis() + diff);

                jsonElement.getAsJsonObject().remove(SystemData.Entry.Cols.DATE_OF_CHANGE);
                jsonElement.getAsJsonObject().addProperty(SystemData.Entry.Cols.SYSTEM_DATE,
                //jsonElement.getAsJsonObject().addProperty(SystemData.Entry.Cols.DATE_OF_CHANGE,
                        ISO8601.fromCalendar(systemDate));
            }
            else {
                return jsonElement;
            }
        }
        return jsonElement;
    }
}
