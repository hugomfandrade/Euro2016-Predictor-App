package org.hugoandrade.euro2016.predictor.admin.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.hugoandrade.euro2016.predictor.admin.MVP;
import org.hugoandrade.euro2016.predictor.admin.R;
import org.hugoandrade.euro2016.predictor.admin.data.LoginData;
import org.hugoandrade.euro2016.predictor.admin.presenter.LoginPresenter;
import org.hugoandrade.euro2016.predictor.admin.utils.NetworkBroadcastReceiverUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.SharedPreferencesUtils;
import org.hugoandrade.euro2016.predictor.admin.utils.UIUtils;
import org.hugoandrade.euro2016.predictor.admin.view.main.MainActivity;

public class LoginActivity extends ActivityBase<MVP.RequiredLoginViewOps,
                                                MVP.ProvidedLoginPresenterOps,
                                                LoginPresenter>
        implements MVP.RequiredLoginViewOps {

    // Views
    private RelativeLayout btLogin;
    private EditText etEmail;
    private EditText etPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        initializeViews();

        enableUI();

        super.onCreate(LoginPresenter.class, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateInputViews();
    }

    private void populateInputViews() {
        LoginData loginData = SharedPreferencesUtils.getLoginData(this);

        etEmail.setText(loginData.getEmail());
        etPassword.setText(loginData.getPassword());
    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btLogin.setBackgroundResource(R.drawable.btn_default_pressed);
        etEmail.setEnabled(false);
        etPassword.setEnabled(false);
        btLogin.setEnabled(false);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        btLogin.setBackgroundResource(android.R.drawable.btn_default);
        etEmail.setEnabled(true);
        etPassword.setEnabled(true);
        btLogin.setEnabled(true);
    }

    @Override
    public void successfulLogin() {
        startActivity(MainActivity.makeIntent(getActivityContext()));
        finish();
    }

    @Override
    protected void onDestroy() {
        getPresenter().onDestroy(isChangingConfigurations());

        super.onDestroy();
    }

    private void initializeViews() {
        etEmail = (EditText) findViewById(R.id.et_email_login);
        etPassword      = (EditText) findViewById(R.id.et_password_login);
        btLogin         = (RelativeLayout) findViewById(R.id.bt_login);
        progressBar     = (ProgressBar) findViewById(R.id.progressBar_login);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().login(
                        etEmail.getText().toString(),
                        etPassword.getText().toString());
            }
        });
    }

    public void showSnackBar(String message) {
        UIUtils.showSnackBar(findViewById(android.R.id.content), message);
    }

    @Override
    public void reportMessage(String message) {
        showSnackBar(message);
    }
}
