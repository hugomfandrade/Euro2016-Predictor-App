package hugoandrade.euro2016.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.common.GenericPresenter;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.model.LoginModel;

public class LoginPresenter extends GenericPresenter<MVP.RequiredLoginViewOps,
                                                     MVP.RequiredLoginPresenterOps,
                                                     MVP.ProvidedLoginModelOps,
                                                     LoginModel>
        implements MVP.ProvidedLoginPresenterOps,
                   MVP.RequiredLoginPresenterOps {

    private boolean isMovingToNextActivity = false;

    @Override
    public void onCreate(MVP.RequiredLoginViewOps view) {
        // Invoke the special onCreate() method in GenericPresenter,
        // passing in the ImageModel class to instantiate/manage and
        // "this" to provide ImageModel with this MVP.RequiredModelOps
        // instance.
        super.onCreate(LoginModel.class, view,
                this);
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        getModel().onDestroy(isChangingConfiguration);

        if (!isChangingConfiguration && !isMovingToNextActivity)
            getModel().stopService();
    }

    @Override
    public void onConfigurationChange(MVP.RequiredLoginViewOps view) { }

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

        getView().updateLayoutEnableState(false);
        getModel().login(new LoginData(username, password));

    }

    @Override
    public LoginData getLoginDataFromSharedPreferences() {

        SharedPreferences settings = getApplicationContext().
                getSharedPreferences(LoginData.LOGIN_SHARED_PREFERENCES_NAME, 0);

        return new LoginData(
                settings.getString(LoginData.USERNAME, ""),
                settings.getString(LoginData.PASSWORD, ""));
    }

    private void saveLoginDataInSharedPreferences(User user) {
        SharedPreferences settings = getApplicationContext().
                getSharedPreferences(LoginData.LOGIN_SHARED_PREFERENCES_NAME, 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString(LoginData.USERNAME, user.username);
        preferencesEditor.putString(LoginData.PASSWORD, user.password);
        preferencesEditor.apply();
    }

    @Override
    public void notifySuccessfulSignUp(User user) {
        reportLoginOperationResult(null, user);
    }
    @Override
    public void notifyMovingToNextActivity() {
        isMovingToNextActivity = true;
    }

    @Override
    public Context getActivityContext() {
        return getView().getActivityContext();
    }

    @Override
    public Context getApplicationContext() {
        return getView().getApplicationContext();
    }

    @Override
    public void reportLoginOperationResult(String message, User user) {
        if (message != null)
            getView().reportMessage(message);

        if (user != null) {
            saveLoginDataInSharedPreferences(user);
            GlobalData.initializeUser(user);

            getView().successfulLogin();
        }

        getView().updateLayoutEnableState(true);

    }
}
