package org.hugoandrade.euro2016.predictor.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.service.MobileService;

import java.lang.ref.WeakReference;

public abstract class MobileClientModelBase<RequiredPresenterOps extends MVP.RequiredServicePresenterBaseOps>

        implements MVP.ProvidedServiceModelBaseOps<RequiredPresenterOps> {

    protected final String TAG = getClass().getSimpleName();

    private WeakReference<RequiredPresenterOps> mPresenter;

    private final Handler mHandler = new MHandler(this);

    private IMobileClientService mService;

    private boolean isServiceBound = false;

    MobileClientModelBase() {
    }

    @Override
    public void onCreate(RequiredPresenterOps presenter) {
        // Set the WeakReference.
        mPresenter =
                new WeakReference<>(presenter);

        // Bind to the Service.
        bindService();
    }

    public void onDestroy(boolean isChangingConfigurations) {
        if (isChangingConfigurations)
            Log.d(TAG,
                    "just a configuration change - unbindService() not called");
        else
            // Unbind from the Services only if onDestroy() is not
            // triggered by a runtime configuration change.
            unbindService();

    }

    private void bindService() {
        if (!isServiceBound) {
            mPresenter.get().getApplicationContext().bindService(
                    MobileService.makeIntent(mPresenter.get().getActivityContext()),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            isServiceBound = true;
        }
    }

    private void unbindService() {
        if (isServiceBound) {
            if (mService != null) {
                try {
                    mService.unregisterCallback(mCallback);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            mPresenter.get().getApplicationContext().unbindService(mServiceConnection);
            isServiceBound = false;
        }
    }

    @Override
    public boolean isServiceBound() {
        return isServiceBound && mService != null;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mService = IMobileClientService.Stub.asInterface(binder);
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mPresenter.get().notifyServiceIsBound();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            isServiceBound = false;
        }
    };

    @Override
    public void registerCallback() {
        if (mService != null)
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
    }

    protected IMobileClientService getService() {
        return mService;
    }

    protected RequiredPresenterOps getPresenter() {
        return mPresenter.get();
    }

    public abstract void sendResults(MobileClientData data);

    // -------------------------------
    // MobileClientService Communication callback
    // -------------------------------

    private IMobileClientServiceCallback mCallback = new IMobileClientServiceCallback.Stub() {

        @Override
        public void sendResults(MobileClientData data) throws RemoteException {
            Message requestMessage = Message.obtain();
            requestMessage.obj = data;
            mHandler.sendMessage(requestMessage);
        }
    };

    private static class MHandler extends Handler {

        private final WeakReference<MobileClientModelBase> mRef;

        MHandler(MobileClientModelBase ref) {
            mRef = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            mRef.get().sendResults((MobileClientData) msg.obj);
        }
    }
}
