package org.hugoandrade.euro2016.predictor.model.helper;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.table.query.ExecutableJsonQuery;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobileServiceJsonTableHelper {

    /**
     * Logging tag.
     */
    private static final String TAG = MobileServiceJsonTableHelper.class.getSimpleName();

    private ExecutableJsonQuery mExecutableJsonQuery;

    private SettableFuture<JsonElement> mFuture;
    private JsonArray mJsonArray;

    private Pair<String, String[]> mSpecialWhere;
    private List<WhereClause> whereClauseList;

    //private String mOrderField;
    //private QueryOrder mOrderOrder;

    private MHandler mHandler;

    public static MobileServiceJsonTableHelper instance(String tableName, MobileServiceClient client) {
        ExecutableJsonQuery executableJsonQuery = new ExecutableJsonQuery();
        executableJsonQuery.setTable(new MobileServiceJsonTable(tableName, client));
        return instance(executableJsonQuery);
    }

    public static MobileServiceJsonTableHelper instance(ExecutableJsonQuery executableJsonQuery) {
        return new MobileServiceJsonTableHelper(executableJsonQuery);
    }

    private MobileServiceJsonTableHelper(ExecutableJsonQuery executableJsonQuery) {
        mExecutableJsonQuery = executableJsonQuery;
    }

    public MobileServiceJsonTableHelper orderBy(String field, QueryOrder order) {
        mExecutableJsonQuery.orderBy(field, order);
        return this;
    }

    public MobileServiceJsonTableHelper select(String... selectFields) {
        mExecutableJsonQuery.select(selectFields);
        return this;
    }

    /*public MobileServiceJsonTableHelper where(String field, String... values) {
        if (getNumberOfWhereClauses() >= 10) {
            throw new IllegalArgumentException("Too many where clauses");
        }
        mSpecialWhere = new Pair<>(field, values);
        return this;
    }/**/

    public WhereClause where() {
        if (getNumberOfWhereClauses() >= 10) {
            throw new IllegalArgumentException("Too many where clauses");
        }
        if (whereClauseList == null)
            whereClauseList = new ArrayList<>();

        WhereClause filteringOperation = new WhereClause(this);
        whereClauseList.add(filteringOperation);
        return filteringOperation;
    }

    public ListenableFuture<JsonElement> execute() {
        mHandler = new MHandler(this, Looper.getMainLooper());

        mJsonArray = new JsonArray();

        mFuture = SettableFuture.create();
        if (mSpecialWhere == null) {
            startQueryAllAsync();
        }
        else {
            queryWhere();
        }
        return mFuture;
    }

    private void queryWhere() {

        buildQuery();

        final int[] whereParameters = {0 /* skip */, 1 /* n times */, 10 /* max where values */, 1/* was aborted */};

        final boolean hasRegularWhereClauses = whereClauseList != null && whereClauseList.size() != 0;

        if (whereClauseList != null && whereClauseList.size() != 0) {

            whereParameters[2] = 10 - whereClauseList.size();
        }


        String mWhereField = mSpecialWhere.first;
        String[] mWhereValues = mSpecialWhere.second;

        whereParameters[1] = mWhereValues.length / whereParameters[2] + 1;

        for (int i = 0; i < whereParameters[1]; i++) {

            final int from = i * whereParameters[2];
            final int to = (i != (whereParameters[1] - 1)) ?
                    (i + 1) * whereParameters[2] :
                    mWhereValues.length;

            final ExecutableJsonQuery executableJsonQuery = buildExecutableJsonWhereQuery(
                    mExecutableJsonQuery.deepClone(),
                    hasRegularWhereClauses,
                    mWhereField,
                    Arrays.copyOfRange(mWhereValues, from, to));

            Futures.addCallback(MobileServiceJsonTableHelper.instance(executableJsonQuery).execute(),

                    new FutureCallback<JsonElement>() {
                        @Override
                        public void onSuccess(@Nullable JsonElement result) {
                            if (whereParameters[3] == 0) {
                                Log.d(TAG, "Operation was aborted");
                                return;
                            }

                            if (result != null && result.isJsonArray())
                                mJsonArray.addAll(result.getAsJsonArray());

                            whereParameters[0]++;
                            if (whereParameters[0] == whereParameters[1]) {
                                successfulOperation();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Throwable t) {
                            if (whereParameters[3] == 0) {
                                Log.d(TAG, "Operation was aborted");
                                return;
                            }
                            whereParameters[3] = 0;

                            showErrorAndAbortMessage(t);
                        }
                    });
        }
    }

    private void startQueryAllAsync() {
        buildQuery();

        queryMore(new QueryParameters(0, 50));
    }

    private void querySync(final QueryParameters queryParameters) {

        try {

            JsonElement jsonElement = mExecutableJsonQuery.skip(queryParameters.skip).top(queryParameters.top).execute().get();

            if (jsonElement != null && jsonElement.isJsonArray())
                mJsonArray.addAll(jsonElement.getAsJsonArray());

            if (jsonElement == null
                    || !jsonElement.isJsonArray()
                    || jsonElement.getAsJsonArray().size() != queryParameters.top) {
                successfulOperation();
            }
            else {
                queryParameters.skip = queryParameters.skip + queryParameters.top;
                queryMore(queryParameters);
            }
        } catch (InterruptedException | ExecutionException e) {
            showErrorAndAbortMessage(e.getCause());
        }
    }

    private void queryMore(final QueryParameters queryParameters) {
        Message message = mHandler.obtainMessage(MHandler.QUERY_MORE_REQUEST_CODE);
        message.obj = queryParameters;
        message.sendToTarget();
    }

    private void successfulOperation() {
        mHandler.shutdown();
        if (mFuture.set(mJsonArray))
            Log.d(TAG, "result successfully set");
    }

    private void showErrorAndAbortMessage(Throwable throwable) {
        mHandler.shutdown();
        if (mFuture.setException(throwable))
            Log.d(TAG, "exception successfully set");
    }

    private void buildQuery() {

        if (whereClauseList != null && whereClauseList.size() != 0) {

            for (int i = 0 ; i < whereClauseList.size() ; i++) {
                if (i != 0) {
                    mExecutableJsonQuery.and();
                }
                String field = whereClauseList.get(i).field;

                mExecutableJsonQuery.field(field);

                String operation = whereClauseList.get(i).operation;
                String valueString = whereClauseList.get(i).valueString;
                Number valueNumber = whereClauseList.get(i).valueNumber;

                if (operation.equals(">=")) {
                    if (valueString != null)
                        mExecutableJsonQuery.ge(valueString);
                    else
                        mExecutableJsonQuery.ge(valueNumber);
                }
                else if (operation.equals("<=")) {
                    mExecutableJsonQuery.le(valueNumber);
                }
                else if (operation.equals("=")) {
                    if (valueString != null)
                        mExecutableJsonQuery.eq(valueString);
                    else
                        mExecutableJsonQuery.eq(valueNumber);
                }
                else {
                    if (valueString != null)
                        mExecutableJsonQuery.eq(valueString);
                    else
                        mExecutableJsonQuery.eq(valueNumber);
                }
            }
        }
    }

    private static ExecutableJsonQuery buildExecutableJsonWhereQuery(ExecutableJsonQuery executableJsonQuery,
                                                                     boolean hasRegularWhereClauses,
                                                                     String field,
                                                                     String... values) {


        if (values.length == 0) {
            return executableJsonQuery;
        }

        //Log.e(TAG, executableJsonQuery.getTableName() + " --> " + Integer.toString(values.length));
        if (hasRegularWhereClauses) {
            Query query = QueryOperations.query(null);

            //Log.e(TAG, executableJsonQuery.getTableName() + " --> " + Integer.toString(values.length));
            for (int i = 0 ; i < values.length ; i++) {
                if (i != 0)
                    query.or();
                query.field(field).eq(values[i]);
            }


            return executableJsonQuery.and(query);
        }
        else {
            for (int i = 0 ; i < values.length ; i++) {
                if (i != 0)
                    executableJsonQuery.or();
                executableJsonQuery.field(field).eq(values[i]);
            }

            return executableJsonQuery;
        }
    }

    private int getNumberOfWhereClauses() {
        return (whereClauseList == null ? 0 : whereClauseList.size()) +
                (mSpecialWhere == null ? 0 : 1);
    }

    public class WhereClause {

        MobileServiceJsonTableHelper base;

        String field;
        String operation;
        String valueString;
        Number valueNumber;
        String[] values;
        boolean isSpecial;

        WhereClause(MobileServiceJsonTableHelper base) {
            this.base = base;
        }

        public WhereClause field(String field) {
            this.field = field;
            return this;
        }

        public WhereClause eq(String value) {
            this.operation = "=";
            this.valueString = value;
            return this;
        }
        public WhereClause eq(String... values) {
            isSpecial = true;
            this.operation = "=";
            this.values = values;
            return this;
        }/**/
        public WhereClause lt(Number matchNo) {
            this.operation = "<";
            this.valueNumber = matchNo;
            return this;
        }
        public WhereClause ge(Number matchNo) {
            this.operation = ">=";
            this.valueNumber = matchNo;
            return this;
        }
        public WhereClause le(Number matchNo) {
            this.operation = "<=";
            this.valueNumber = matchNo;
            return this;
        }

        public WhereClause and() {
            if (isSpecial) {
                base.mSpecialWhere = new Pair<>(field, values);
                base.whereClauseList.remove(this);
            }
            return this.base.where();
        }

        public ListenableFuture<JsonElement> execute() {
            return base.execute();
        }
    }

    private static class MHandler extends Handler {

        private final static int QUERY_MORE_REQUEST_CODE = 10;

        private final WeakReference<MobileServiceJsonTableHelper> mBackgroundTask;
        private final ExecutorService mExecutorService;

        MHandler(MobileServiceJsonTableHelper mobileServiceJsonTableHelper, Looper looper) {
            super(looper);
            mBackgroundTask = new WeakReference<>(mobileServiceJsonTableHelper);
            mExecutorService = Executors.newCachedThreadPool();
        }

        @Override
        public void handleMessage(Message message){
            final Message m = Message.obtain(message);

            final int requestCode = m.what;

            if (requestCode == QUERY_MORE_REQUEST_CODE) {
                final Runnable sendDataToHub = new Runnable() {
                    @Override
                    public void run() {

                        if (mBackgroundTask.get() != null)
                            mBackgroundTask.get().querySync((QueryParameters) m.obj);
                    }
                };
                mExecutorService.execute(sendDataToHub);
            }
        }

        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private static class QueryParameters implements Parcelable {

        int skip;
        int top;

        QueryParameters(int skip, int top) {
            this.skip = skip;
            this.top = top;
        }

        QueryParameters(Parcel in) {
            skip = in.readInt();
            top = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(skip);
            dest.writeInt(top);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<QueryParameters> CREATOR = new Creator<QueryParameters>() {
            @Override
            public QueryParameters createFromParcel(Parcel in) {
                return new QueryParameters(in);
            }

            @Override
            public QueryParameters[] newArray(int size) {
                return new QueryParameters[size];
            }
        };
    }
}
