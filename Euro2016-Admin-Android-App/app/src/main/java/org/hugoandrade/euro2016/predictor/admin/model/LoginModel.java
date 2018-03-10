package org.hugoandrade.euro2016.predictor.admin.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.admin.MVP;
import org.hugoandrade.euro2016.predictor.admin.model.parser.MessageBase;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoginModel implements MVP.ProvidedLoginModelOps {

    protected final static String TAG = LoginModel.class.getSimpleName();

    private ReplyHandler mReplyHandler = null;
    private Messenger mReplyMessage = null;
    private Messenger mRequestMessengerRef = null;

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    private WeakReference<MVP.RequiredLoginPresenterOps> mPresenter;

    @Override
    public void onCreate(MVP.RequiredLoginPresenterOps presenter) {

        // Set the WeakReference.
        mPresenter =
                new WeakReference<>(presenter);

        mReplyHandler = new ReplyHandler(this);
        mReplyMessage = new Messenger(mReplyHandler);

        // Bind to the Service.
        bindService();
    }

    @Override
    public void onDestroy(boolean isChangingConfigurations) {
        if (isChangingConfigurations)
            Log.d(TAG,
                    "just a configuration change - unbindService() not called");
        else {
            // Unbind from the Services only if onDestroy() is not
            // triggered by a runtime configuration change.
            unbindService();
            stopService();
            mReplyHandler.shutdown();
        }
    }

    private void bindService() {
        if (mRequestMessengerRef == null) {
            final Intent intent = MobileService.makeIntent(mPresenter.get().getActivityContext());
            mPresenter.get().getApplicationContext().bindService(
                    intent,
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
        }

    }

    private void unbindService() {
        if (mRequestMessengerRef != null) {
            mPresenter.get().getApplicationContext().unbindService(mServiceConnection);
            mRequestMessengerRef = null;
        }
    }

    private void stopService() {
        mPresenter.get().getApplicationContext().stopService(
                MobileService.makeIntent(mPresenter.get().getActivityContext()));

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mRequestMessengerRef = new Messenger(binder);
            mPresenter.get().notifyServiceIsBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRequestMessengerRef = null;
        }
    };

    @Override
    public boolean login(String email, String password) {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.LOGIN.ordinal(),
                mReplyMessage
        );
        requestMessage.setLoginData(new LoginData(email, password));

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean getSystemData() {
        if (!areObjectsSet())
            return false;

        MessageBase requestMessage = MessageBase.makeMessage(
                MessageBase.OperationType.GET_SYSTEM_DATA.ordinal(),
                mReplyMessage
        );

        try {
            mRequestMessengerRef.send(requestMessage.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message back to Activity.", e);
            return false;
        }
        return true;
    }

    private boolean areObjectsSet() {
        if (mRequestMessengerRef == null) {
            Log.e(TAG, "mRequestMessengerRef is null when requesting");
            return false;
        }
        if (mReplyMessage == null) {
            Log.e(TAG, "replyMessage is null when requesting");
            return false;
        }
        return true;
    }


    private static class ReplyHandler extends android.os.Handler {

        private WeakReference<LoginModel> mModel;
        private ExecutorService mExecutorService;

        ReplyHandler(LoginModel service) {
            mModel = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            super.handleMessage(message);
            if (mModel == null || mModel.get() == null) // Do not handle incoming request
                return;

            final MessageBase requestMessage = MessageBase.makeMessage(message);
            final int requestCode = requestMessage.getRequestCode();
            final int requestResult = requestMessage.getRequestResult();

            if (requestCode == MessageBase.OperationType.LOGIN.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().reportLoginRequestResult(
                                true,
                                null,
                                requestMessage.getLoginData());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().reportLoginRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().reportLoginRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
            else if (requestCode == MessageBase.OperationType.GET_SYSTEM_DATA.ordinal()) {
                switch (requestResult) {
                    case MessageBase.REQUEST_RESULT_SUCCESS:
                        mModel.get().getSystemDataRequestResult(
                                true,
                                null,
                                requestMessage.getSystemData());
                        break;
                    case MessageBase.REQUEST_RESULT_FAILURE:
                        mModel.get().getSystemDataRequestResult(
                                false,
                                requestMessage.getErrorMessage(),
                                null);
                        break;
                    default:
                        mModel.get().getSystemDataRequestResult(
                                false,
                                "No RequestResult provided",
                                null);
                        break;
                }
            }
        }
        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private void getSystemDataRequestResult(boolean isOk, String message, SystemData systemData) {
        mPresenter.get().getSystemDataRequestResult(isOk, message, systemData);
    }

    private void reportLoginRequestResult(boolean isOk, String message, LoginData loginData) {
        mPresenter.get().loginRequestResult(isOk, message, loginData);
    }
}
