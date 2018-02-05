package org.hugoandrade.euro2016.predictor.admin.model.service;

import android.os.AsyncTask;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;

import java.util.concurrent.ExecutionException;


public final class MobileServiceHelper {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = MobileServiceHelper.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private MobileServiceHelper() {
        throw new AssertionError();
    }


    public static ListenableFuture<JsonElement> queryAll(final String tableName, final MobileServiceClient client) {
        final SettableFuture<JsonElement> future = SettableFuture.create();

        final int[] parameters = {0 /* skip */, 50 /* */};

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {

                query(new MobileServiceJsonTable(tableName, client), future, parameters, new JsonArray());
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return future;
    }

    private static void query(MobileServiceJsonTable mobileServiceJsonTable,
                              SettableFuture<JsonElement> future,
                              int[] parameters,
                              JsonArray jsonElements) {

        try {
            JsonElement jsonElement = mobileServiceJsonTable.skip(parameters[0]).top(parameters[1]).execute().get();

            jsonElements.addAll(jsonElement.getAsJsonArray());

            if (jsonElement.getAsJsonArray().size() != parameters[1]) {
                future.set(jsonElements);
            }
            else {
                parameters[0] = parameters[0] + parameters[1];
                query(mobileServiceJsonTable, future, parameters, jsonElements);
            }
        } catch (InterruptedException | ExecutionException e) {
            future.setException(e.getCause());
        }

    }
}
