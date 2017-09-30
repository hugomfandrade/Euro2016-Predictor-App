package org.hugoandrade.euro2016.backend.presenter;

import android.content.Context;

import org.hugoandrade.euro2016.backend.MVP;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.model.EditSystemDataModel;

public class EditSystemDataPresenter extends PresenterBase<MVP.RequiredEditSystemDataViewOps,
                                                      MVP.RequiredEditSystemDataPresenterOps,
                                                      MVP.ProvidedEditSystemDataModelOps,
        EditSystemDataModel>
        implements MVP.ProvidedEditSystemDataPresenterOps,
                   MVP.RequiredEditSystemDataPresenterOps {


    @Override
    public void onCreate(MVP.RequiredEditSystemDataViewOps view) {
        // Invoke the special onCreate() method in PresenterBase,
        // passing in the SignUpModel class to instantiate/manage and
        // "this" to provide SignUpModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(EditSystemDataModel.class, view, this);
    }

    @Override
    public void onResume() { }

    @Override
    public void onPause() { }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);
    }

    @Override
    public void onConfigurationChange(MVP.RequiredEditSystemDataViewOps view) { }

    /*@Override
    public void reportRegisterOperationResult(String message, LoginData registerData) {
        if (message != null)
            getView().reportMessage(message);

        if (registerData != null) { // Register successful
            getView().successfulRegister(registerData);
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
    } /**/

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }

    private void getSystemData() {
        getModel().getSystemData();
    }

    @Override
    public void reportGetSystemDataOperationResult(String message, SystemData systemData) {
        if (message != null)
            getView().reportMessage(message);

        if (systemData != null) {
            getView().reportSystemData(systemData);
        }
    }

    @Override
    public void reportSetSystemDataOperationResult(String message, SystemData systemData) {
        if (message != null)
            getView().reportMessage(message);

        if (systemData != null) {
            getView().reportSystemData(systemData);
        }
    }

    @Override
    public void setSystemData(SystemData systemData) {
        getModel().setSystemData(systemData);
    }
}
