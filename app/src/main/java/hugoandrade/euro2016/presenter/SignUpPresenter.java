package hugoandrade.euro2016.presenter;

import android.content.Context;
import android.util.Log;

import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.common.GenericPresenter;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.model.SignUpModel;

public class SignUpPresenter extends GenericPresenter<MVP.RequiredSignUpViewOps,
                                                      MVP.RequiredSignUpPresenterOps,
                                                      MVP.ProvidedSignUpModelOps,
                                                      SignUpModel>
        implements MVP.ProvidedSignUpPresenterOps,
                   MVP.RequiredSignUpPresenterOps {


    @Override
    public void onCreate(MVP.RequiredSignUpViewOps view) {
        // Invoke the special onCreate() method in GenericPresenter,
        // passing in the SignUpModel class to instantiate/manage and
        // "this" to provide SignUpModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(SignUpModel.class, view, this);
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);
    }

    @Override
    public void onConfigurationChange(MVP.RequiredSignUpViewOps view) { }

    @Override
    public void reportRegisterOperationResult(String message, User user) {
        if (message != null)
            getView().reportMessage(message);

        if (user != null) { // Register successful
            getView().successfulRegister(user);
        }

        getView().updateLayoutEnableState(true);
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

        getView().updateLayoutEnableState(false);
        getModel().registerUser(new LoginData(username, password));
    }

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }

}
