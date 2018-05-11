package org.hugoandrade.euro2016.predictor.presenter;

import android.content.BroadcastReceiver;
import android.os.RemoteException;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.data.raw.SystemData;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.service.MobileService;
import org.hugoandrade.euro2016.predictor.utils.ErrorMessageUtils;
import org.hugoandrade.euro2016.predictor.utils.NetworkBroadcastReceiverUtils;
import org.hugoandrade.euro2016.predictor.utils.NetworkUtils;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;

public class LoginPresenter extends MobileClientPresenterBase<MVP.RequiredLoginViewOps>

        implements MVP.ProvidedLoginPresenterOps {

    private boolean isMovingToNextActivity = false;
    private boolean[] mSplashScreenEnabledBooleans = new boolean[2];
    // (0) Get SystemData
    // (1) Do Login (with Stored Credentials)

    private BroadcastReceiver mNetworkBroadcastReceiver;

    @Override
    public void onCreate(MVP.RequiredLoginViewOps view) {

        // Start service
        view.getApplicationContext()
                .startService(MobileService.makeIntent(view.getApplicationContext()));

        // Invoke the special onCreate() method in PresenterBase,
        // passing in the ImageModel class to instantiate/manage and
        // "this" to provide ImageModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(view);

        mNetworkBroadcastReceiver = NetworkBroadcastReceiverUtils.register(getActivityContext(), iNetworkListener);
    }

    private void finishSplashScreenAnimation() {
        if (mSplashScreenEnabledBooleans[0] && mSplashScreenEnabledBooleans[1]) {
            getView().stopHoldingSplashScreenAnimation();
        }
    }

    @Override
    public void notifyServiceIsBound() {
        getSystemData();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);

        if (mNetworkBroadcastReceiver != null) {
            NetworkBroadcastReceiverUtils.unregister(getActivityContext(), mNetworkBroadcastReceiver);
            mNetworkBroadcastReceiver = null;
        }

        if (!isChangingConfiguration && !isMovingToNextActivity)
            getApplicationContext().stopService(
                    MobileService.makeIntent(getActivityContext()));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (NetworkUtils.isNetworkAvailable(getActivityContext()) && isServiceBound() ) {
            if (GlobalData.getInstance().systemData == null) {
                getSystemData();
            }
            else {
                finishSplashScreenAnimation();
            }
        }
    }

    private NetworkBroadcastReceiverUtils.INetworkBroadcastReceiver iNetworkListener
            = new NetworkBroadcastReceiverUtils.INetworkBroadcastReceiver() {
        @Override
        public void setNetworkAvailable(boolean isNetworkAvailable) {
            if (isNetworkAvailable && isServiceBound() ) {
                if (GlobalData.getInstance().systemData == null) {
                    getSystemData();
                }
                else {
                    finishSplashScreenAnimation();
                }
            }
        }
    };

    @Override
    public void login(String username, String password) {
        if (username.equals("")) {
            Log.w(TAG, "Username not entered");
            getView().reportMessage("Empty Username field");
            return;
        }
        if (password.equals("")) {
            Log.w(TAG, "Password not entered");
            getView().reportMessage("Empty Username field");
            return;
        }

        getView().disableUI();

        doLogin(new LoginData(username, password));

    }

    private void doLogin(LoginData loginData) {
        if (getMobileClientService() == null) {
            loginOperationResult(false, ErrorMessageUtils.genNotBoundMessage() + ": doLogin", null);
            return;
        }

        try {
            getMobileClientService().login(loginData);
        } catch (RemoteException e) {
            e.printStackTrace();
            loginOperationResult(false, "Error sending message", null);
        }
    }

    private void getSystemData() {
        if (getMobileClientService() == null) {
            getSystemDataOperationResult(false, ErrorMessageUtils.genNotBoundMessage() + ": getSystemData", null);
            return;
        }

        try {
            getMobileClientService().getSystemData();
        } catch (RemoteException e) {
            e.printStackTrace();
            getSystemDataOperationResult(false, "Error sending message", null);
        }
    }

    @Override
    public void notifyMovingToNextActivity() {
        isMovingToNextActivity = true;
    }

    @Override
    public void sendResults(MobileClientData data) {

        int operationType = data.getOperationType();
        boolean isOperationSuccessful
                = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

        if (operationType == MobileClientData.OperationType.LOGIN.ordinal()) {
            loginOperationResult(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getLoginData());
        }
        else if (operationType == MobileClientData.OperationType.GET_SYSTEM_DATA.ordinal()) {
            getSystemDataOperationResult(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getSystemData());
        }
    }

    private void loginOperationResult(boolean wasOperationSuccessful, String message, LoginData loginData) {
        mSplashScreenEnabledBooleans[1] = true;

        if (wasOperationSuccessful) {

            SharedPreferencesUtils.putLoginData(getActivityContext(), loginData);
            SharedPreferencesUtils.putLastAuthenticatedLoginData(getActivityContext(), loginData);

            GlobalData.getInstance().setUser(new User(loginData.getUserID(), loginData.getEmail()));

            getView().successfulLogin();
        }
        else {
            showErrorMessage(message);
            finishSplashScreenAnimation();
        }


        getView().enableUI();

    }

    private void getSystemDataOperationResult(boolean wasOperationSuccessful, String message, SystemData systemData) {
        if (wasOperationSuccessful) {
            // avoid fetching twice
            if (mSplashScreenEnabledBooleans[0]) return;

            mSplashScreenEnabledBooleans[0] = true;

            if (!systemData.getAppState()) {
                getView().finishApp();
                return;
            }

            GlobalData.getInstance().setSystemData(systemData);

            LoginData loginData = SharedPreferencesUtils.getLastAuthenticatedLoginData(getActivityContext());

            if (loginData.getEmail() != null && loginData.getPassword() != null) {
                doLogin(loginData);
            }
            else {
                mSplashScreenEnabledBooleans[1] = true;
                finishSplashScreenAnimation();
            }
        }
        else {
            showErrorMessage(message);
        }
    }

    private void showErrorMessage(String message) {
        if (NetworkUtils.isNetworkUnavailableError(getActivityContext(), message)) {
            //ViewUtils.showToast(getActivityContext(), message);
            return;
        }
        // operation failed, show error message
        if (message != null)
            getView().reportMessage(message);
    }
}
