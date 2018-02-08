package org.hugoandrade.euro2016.predictor.presenter;

import android.os.RemoteException;
import android.util.Log;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;

public class SignUpPresenter extends MobileClientPresenterBase<MVP.RequiredSignUpViewOps>

        implements MVP.ProvidedSignUpPresenterOps {


    @Override
    public void onCreate(MVP.RequiredSignUpViewOps view) {
        // Invoke the special onCreate() method in PresenterBase,
        // passing in the SignUpModel class to instantiate/manage and
        // "this" to provide SignUpModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(view);
    }

    @Override
    public void notifyServiceIsBound() {
        // No-ops
    }

    @Override
    public void registerUser(String username, String password, String confirmPassword) {
        if (username.equals("")) {
            //We're just logging this here, we should show something to the user
            getView().reportMessage("Username not entered");
            Log.w(TAG, "Username not entered");
            return;
        }
        if (password.equals("")) {
            //We're just logging this here, we should show something to the user
            getView().reportMessage("Password not entered");
            Log.w(TAG, "Password not entered");
            return;
        }
        if (!password.equals(confirmPassword)) {
            //We're just logging this here, we should show something to the user
            getView().reportMessage("Passwords do not match");
            Log.w(TAG, "Passwords do not match");
            return;
        }

        getView().disableUI();

        doRegisterUser(new LoginData(username, password));
    }

    private void doRegisterUser(LoginData loginData) {
        if (getMobileClientService() == null) {
            Log.w(TAG, "Service is still not bound");
            signUpOperationResult(false, "Not bound to the service", null);
            return;
        }

        try {
            boolean isSigningUp = getMobileClientService().signUp(loginData);
            if (!isSigningUp) {
                signUpOperationResult(false, "No Network Connection", null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            signUpOperationResult(false, "Error sending message", null);
        }
    }

    @Override
    public void sendResults(MobileClientData data) {

        int operationType = data.getOperationType();
        boolean isOperationSuccessful
                = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

        if (operationType == MobileClientData.OperationType.REGISTER.ordinal()) {
            signUpOperationResult(
                    isOperationSuccessful,
                    data.getErrorMessage(),
                    data.getLoginData());
        }
    }

    private void signUpOperationResult(boolean wasOperationSuccessful, String message, LoginData loginData) {
        if (wasOperationSuccessful) {
            getView().successfulRegister(loginData);
        }
        else {
            // operation failed, show error message
            if (message != null)
                getView().reportMessage(message);
        }

        getView().enableUI();
    }
}
