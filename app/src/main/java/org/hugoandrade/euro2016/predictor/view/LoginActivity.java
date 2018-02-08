package org.hugoandrade.euro2016.predictor.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.SplashScreenAnimation;
import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.presenter.LoginPresenter;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;

public class LoginActivity extends ActivityBase<MVP.RequiredLoginViewOps,
                                                MVP.ProvidedLoginPresenterOps,
                                                LoginPresenter>
        implements MVP.RequiredLoginViewOps {

    private final static int SPLASH_DURATION = 2000; // 2 seconds
    private final static int ANIMATION_DURATION = 500; // 0.5 seconds

    private static final int SIGN_UP_REQUEST_CODE = 100;

    private RelativeLayout btLogin;
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvSignUp;
    private ProgressBar progressBar;

    private SplashScreenAnimation mSplashScreenAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeUI();

        GlobalData.resetUser();

        // run a thread after SPLASH_DURATION seconds to start the home screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSplashScreenAnimation.stopHold();
            }
        }, SPLASH_DURATION);// run a thread after SPLASH_DURATION seconds to start the home screen

        super.onCreate(LoginPresenter.class, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateInputViews();
    }

    private void populateInputViews() {
        LoginData data = SharedPreferencesUtils.getLoginData(this);

        etUsername.setText(data.getEmail());
        etPassword.setText(data.getPassword());
    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btLogin.setBackgroundResource(R.drawable.btn_default_pressed);
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        tvSignUp.setEnabled(false);
        btLogin.setEnabled(false);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        btLogin.setBackgroundResource(android.R.drawable.btn_default);
        etUsername.setEnabled(true);
        etPassword.setEnabled(true);
        tvSignUp.setEnabled(true);
        btLogin.setEnabled(true);
    }

    @Override
    public void successfulLogin() {
        getPresenter().notifyMovingToNextActivity();
        startActivity(MainActivity.makeIntent(getActivityContext()));//, TempActivity.class));
        finish();
    }

    @Override
    public void finishApp() {
        finish();
    }

    private void initializeUI() {
        setContentView(R.layout.activity_login);

        tvSignUp        = (TextView) findViewById(R.id.tv_sign_up);
        etUsername      = (EditText) findViewById(R.id.et_username_login);
        etPassword      = (EditText) findViewById(R.id.et_password_login);
        btLogin         = (RelativeLayout) findViewById(R.id.bt_login);
        View ivLogo        = findViewById(R.id.iv_logo);
        View ivLogoSplash  = findViewById(R.id.iv_logo_splash);
        View llInputFields = findViewById(R.id.ll_login_input_fields);
        progressBar     = (ProgressBar) findViewById(R.id.progressBar_login);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        tvSignUp.setOnClickListener(loginClickListener);
        btLogin.setOnClickListener(loginClickListener);

        mSplashScreenAnimation = SplashScreenAnimation.Builder.instance(ivLogoSplash, ivLogo)
                .setAppearingViews(tvSignUp, etUsername, etPassword, btLogin, llInputFields)
                .setSplashDuration(SPLASH_DURATION)
                .setAnimationDuration(ANIMATION_DURATION)
                .start(true);

    }

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == tvSignUp) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent, SIGN_UP_REQUEST_CODE);
            } else if (v == btLogin) {
                getPresenter().login(
                        etUsername.getText().toString(),
                        etPassword.getText().toString());
            }
        }
    };

    public void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void reportMessage(String message) {
        showSnackBar(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_UP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                LoginData loginData = SignUpActivity.extractLoginDataFromIntent(data);
                etUsername.setText(loginData.getEmail());
                etPassword.setText(loginData.getPassword());
                getPresenter().login(
                        etUsername.getText().toString(),
                        etPassword.getText().toString());
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
