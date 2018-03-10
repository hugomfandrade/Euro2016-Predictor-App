package org.hugoandrade.euro2016.predictor.presenter;

import android.os.RemoteException;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.data.SystemData;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.model.service.MobileService;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;

public class LoginPresenter extends MobileClientPresenterBase<MVP.RequiredLoginViewOps>

        implements MVP.ProvidedLoginPresenterOps {

    private boolean isMovingToNextActivity = false;

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
    }

    @Override
    public void notifyServiceIsBound() {
        getSystemData();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);

        if (!isChangingConfiguration && !isMovingToNextActivity)
            getApplicationContext().stopService(
                    MobileService.makeIntent(getActivityContext()));
    }

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
            Log.w(TAG, "Service is still not bound");
            loginOperationResult(false, "Not bound to the service", null);
            return;
        }

        try {
            boolean isLoggingIn = getMobileClientService().login(loginData);
            if (!isLoggingIn) {
                loginOperationResult(false, "No Network Connection", null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            loginOperationResult(false, "Error sending message", null);
        }
    }

    private void getSystemData() {
        if (getMobileClientService() == null) {
            Log.w(TAG, "Service is still not bound");
            getSystemDataOperationResult(false, "Not bound to the service", null);
            return;
        }

        try {
            boolean isGettingSystemData = getMobileClientService().getSystemData();
            if (!isGettingSystemData) {
                getSystemDataOperationResult(false, "No Network Connection", null);
            }
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
        if (wasOperationSuccessful) {
            Log.e(TAG, "loginOperationResult: " + loginData.toString());

            SharedPreferencesUtils.putLoginData(getActivityContext(), loginData);

            GlobalData.initializeUser(loginData);

            getView().successfulLogin();
        }
        else {
            // operation failed, show error message
            if (message != null)
                getView().reportMessage(message);
        }

        getView().enableUI();

    }

    private void getSystemDataOperationResult(boolean wasOperationSuccessful, String message, SystemData systemData) {
        if (wasOperationSuccessful) {

            if (!systemData.getAppState()) {
                getView().finishApp();
                return;
            }

            GlobalData.setSystemData(systemData);
        }
        else {
            // operation failed, show error message
            if (message != null)
                getView().reportMessage(message);
        }

    }
}
