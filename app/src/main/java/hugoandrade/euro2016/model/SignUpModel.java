package hugoandrade.euro2016.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.common.MessageBase;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.object.User;

public class SignUpModel implements MVP.ProvidedSignUpModelOps {

    protected final static String TAG =
            SignUpModel.class.getSimpleName();

    private Messenger mReplyMessage = null;
    private Messenger mRequestMessengerRef = null;
    private ReplyHandler mReplyHandler = new ReplyHandler(this);

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    private WeakReference<MVP.RequiredSignUpPresenterOps> mPresenter;

    @Override
    public void onCreate(MVP.RequiredSignUpPresenterOps presenter) {
        // Set the WeakReference.
        mPresenter =
                new WeakReference<>(presenter);

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
            mReplyHandler.shutdown();
        }
    }

    private void bindService() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().getApplicationContext().bindService(
                    AzureMobileService.makeIntent(mPresenter.get().getActivityContext()),
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

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mRequestMessengerRef = new Messenger(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRequestMessengerRef = null;
        }
    };

    @Override
    public void registerUser(LoginData loginData) {
        if (mRequestMessengerRef == null) {
            mPresenter.get().reportRegisterOperationResult("Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    AzureMobileService.OperationType.REGISTER.ordinal(),
                    mReplyMessage);
            requestMessage.putParcelable(AzureMobileService.LOGIN_DATA, loginData);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().reportRegisterOperationResult("Error sending message", null);
            }
        }
    }

    private static class ReplyHandler extends android.os.Handler {

        @SuppressWarnings("unused") private final String TAG = getClass().getSimpleName();

        private WeakReference<SignUpModel> mModel;
        private ExecutorService mExecutorService;

        ReplyHandler(SignUpModel service) {
            mModel = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            super.handleMessage(message);
            if (mModel.get() == null) // Do not handle incoming request
                return;
            if (mModel.get().hasLeftSignUpActivity()) // Do not handle incoming request
                return;

            final MessageBase requestMessage = MessageBase.makeMessage(message);

            final int requestCode = requestMessage.getRequestCode();
            final String requestResult = requestMessage.getRequestResult();

            if (requestCode == AzureMobileService.OperationType.REGISTER.ordinal()) {
                if (requestResult.equals(AzureMobileService.REQUEST_RESULT_SUCCESS)) {
                    mModel.get().registerRequestResultSuccess(
                            requestMessage.<User>getParcelable(AzureMobileService.USER_DATA));
                }
                else if (requestResult.equals(AzureMobileService.REQUEST_RESULT_FAILURE)) {
                    mModel.get().registerRequestResultFailure(
                            requestMessage.getString(AzureMobileService.ERROR_MESSAGE));
                }
            }
        }
        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private void registerRequestResultFailure(String errorMessage) {
        mPresenter.get().reportRegisterOperationResult(errorMessage, null);
    }

    private void registerRequestResultSuccess(User user) {
        mPresenter.get().reportRegisterOperationResult(null, user);
    }

    private boolean hasLeftSignUpActivity() {
        return mRequestMessengerRef == null;
    }
}
