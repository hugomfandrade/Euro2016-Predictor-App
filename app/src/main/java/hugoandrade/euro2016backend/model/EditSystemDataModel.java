package hugoandrade.euro2016backend.model;

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

import hugoandrade.euro2016backend.MVP;
import hugoandrade.euro2016backend.common.MessageBase;
import hugoandrade.euro2016backend.object.SystemData;

public class EditSystemDataModel implements MVP.ProvidedEditSystemDataModelOps {

    protected final static String TAG =
            EditSystemDataModel.class.getSimpleName();

    private Messenger mReplyMessage = null;
    private Messenger mRequestMessengerRef = null;
    private ReplyHandler mReplyHandler = new ReplyHandler(this);

    /**
     * A WeakReference used to access methods in the Presenter layer.
     * The WeakReference enables garbage collection.
     */
    private WeakReference<MVP.RequiredEditSystemDataPresenterOps> mPresenter;

    @Override
    public void onCreate(MVP.RequiredEditSystemDataPresenterOps presenter) {
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
                    MobileService.makeIntent(mPresenter.get().getActivityContext()),
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
            getSystemData();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mRequestMessengerRef = null;
        }
    };

    @Override
    public void getSystemData() {
        if (mRequestMessengerRef == null) {
            mPresenter.get().reportGetSystemDataOperationResult("Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    MobileService.OperationType.GET_SYSTEM_DATA.ordinal(),
                    mReplyMessage);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().reportGetSystemDataOperationResult("Error sending message", null);
            }
        }
    }

    @Override
    public void setSystemData(SystemData systemData) {
        if (mRequestMessengerRef == null) {
            mPresenter.get().reportSetSystemDataOperationResult("Not bound to the service", null);
        }
        else {
            MessageBase requestMessage = MessageBase.makeMessage(
                    MobileService.OperationType.GET_SYSTEM_DATA.ordinal(),
                    mReplyMessage);
            requestMessage.putParcelable(MobileService.SYSTEM_DATA, systemData);

            try {
                mRequestMessengerRef.send(requestMessage.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message back to Activity.", e);
                mPresenter.get().reportSetSystemDataOperationResult("Error sending message", null);
            }
        }
    }

    private static class ReplyHandler extends android.os.Handler {

        @SuppressWarnings("unused") private final String TAG = getClass().getSimpleName();

        private WeakReference<EditSystemDataModel> mModel;
        private ExecutorService mExecutorService;

        ReplyHandler(EditSystemDataModel service) {
            mModel = new WeakReference<>(service);
            mExecutorService = Executors.newCachedThreadPool();
        }

        public void handleMessage(Message message){
            super.handleMessage(message);
            if (mModel.get() == null) // Do not handle incoming request
                return;

            final MessageBase requestMessage = MessageBase.makeMessage(message);

            final int requestCode = requestMessage.getRequestCode();
            final String requestResult = requestMessage.getRequestResult();

            if (requestCode == MobileService.OperationType.GET_SYSTEM_DATA.ordinal()) {
                switch (requestResult) {
                    case MobileService.REQUEST_RESULT_SUCCESS:
                        mModel.get().getSystemDataRequestResultSuccess(
                                requestMessage.<SystemData>getParcelable(MobileService.SYSTEM_DATA));
                        break;
                    case MobileService.REQUEST_RESULT_FAILURE:
                        mModel.get().getSystemDataRequestResultFailure(
                                requestMessage.getString(MobileService.ERROR_MESSAGE));
                        break;
                    default:
                        mModel.get().getSystemDataRequestResultFailure("No RequestResult provided");
                        break;
                }
            }
        }
        void shutdown() {
            mExecutorService.shutdown();
        }
    }

    private void getSystemDataRequestResultFailure(String errorMessage) {
        mPresenter.get().reportGetSystemDataOperationResult(errorMessage, null);
    }

    private void getSystemDataRequestResultSuccess(SystemData systemData) {
        mPresenter.get().reportGetSystemDataOperationResult(null, systemData);
    }
}
