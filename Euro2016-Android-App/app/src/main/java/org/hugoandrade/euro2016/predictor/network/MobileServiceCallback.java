package org.hugoandrade.euro2016.predictor.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobileServiceCallback {

    private MobileServiceData mHoldingData;
    private OnResult iResult;

    private final MHandler mHandler;
    private boolean isMainThread = false;

    public MobileServiceCallback() {
        isMainThread = Looper.getMainLooper().getThread() == Thread.currentThread();

        mHandler = new MHandler(this);
    }

    private void sendResult(MobileServiceData mobileServiceData) {
        iResult.onResult(mobileServiceData);
        mHandler.shutdown();
    }

    public void set(MobileServiceData mobileServiceData) {
        if (iResult != null)
            mHandler.sendToTarget(mobileServiceData);
        else
            mHoldingData = mobileServiceData;
    }

    public void addListener(OnResult iOnResult) {
        iResult = iOnResult;

        if (mHoldingData != null)
            mHandler.sendToTarget(mHoldingData);
    }

    public static void addCallback(MobileServiceCallback i, OnResult iOnResult) {
        i.addListener(iOnResult);
    }

    public static class MHandler extends Handler {

        private final WeakReference<MobileServiceCallback> mBackgroundTask;
        private final ExecutorService mExecutorService;

        MHandler(MobileServiceCallback iOnCallback) {
            super(Looper.getMainLooper());
            mBackgroundTask = new WeakReference<>(iOnCallback);
            mExecutorService = Executors.newCachedThreadPool();
        }

        void sendToTarget(MobileServiceData m) {
            Message message = obtainMessage();
            message.obj = m;
            message.sendToTarget();
        }

        @Override
        public void handleMessage(Message message){
            final Message m = Message.obtain(message);

            if (mBackgroundTask.get() != null) {
                if (mBackgroundTask.get().isMainThread) {
                    // Run on main thread
                    mBackgroundTask.get().sendResult((MobileServiceData) m.obj);
                }
                else {
                    // Run on another thread
                    mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (mBackgroundTask.get() != null)
                                mBackgroundTask.get().sendResult((MobileServiceData) m.obj);
                        }
                    });
                }
            }
        }

        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    public interface OnResult {
        void onResult(MobileServiceData data);
    }
}
