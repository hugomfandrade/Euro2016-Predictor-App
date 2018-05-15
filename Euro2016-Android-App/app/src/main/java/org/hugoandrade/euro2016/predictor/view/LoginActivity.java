package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.SplashScreenAnimation;
import org.hugoandrade.euro2016.predictor.common.TextWatcherAdapter;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.presenter.LoginPresenter;
import org.hugoandrade.euro2016.predictor.utils.LoginUtils;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

public class LoginActivity extends ActivityBase<MVP.RequiredLoginViewOps,
                                                MVP.ProvidedLoginPresenterOps,
                                                LoginPresenter>
        implements MVP.RequiredLoginViewOps, SplashScreenAnimation.OnSplashScreenAnimationEndListener {

    private final static int SPLASH_DURATION = 2000; // 2 seconds
    private final static int ANIMATION_DURATION = 500; // 0.5 seconds

    private static final int SIGN_UP_REQUEST_CODE = 100;

    private RelativeLayout btLogin;
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvSignUp;
    private ProgressBar progressBar;

    private View tryAgainContainer;
    private View tvTryAgain;
    private View appStateContainer;
    private TextView tvAppStateMessage;
    private TextView tvAppStateMessageDetails;

    private SplashScreenAnimation mSplashScreenAnimation;

    public static Intent makeIntent(Context context) {
        return new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GlobalData.unInitialize();

        initializeUI();

        super.onCreate(LoginPresenter.class, this);
    }

    private void initializeUI() {

        setContentView(R.layout.activity_login);

        tryAgainContainer = findViewById(R.id.try_again_container);
        tvTryAgain = findViewById(R.id.tv_try_again);
        appStateContainer = findViewById(R.id.app_state_message_container);
        tvAppStateMessage = findViewById(R.id.tv_app_state_message);
        tvAppStateMessageDetails = findViewById(R.id.tv_app_state_message_details);
        appStateContainer.setVisibility(View.GONE);

        tvSignUp        = findViewById(R.id.tv_sign_up);
        etUsername      = findViewById(R.id.et_username_login);
        etPassword      = findViewById(R.id.et_password_login);
        btLogin         = findViewById(R.id.bt_login);
        View ivLogo        = findViewById(R.id.iv_logo);
        View ivLogoSplash  = findViewById(R.id.iv_logo_splash);
        View llInputFields = findViewById(R.id.ll_login_input_fields);
        progressBar     = findViewById(R.id.progressBar_login);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        etUsername.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                areLoginInputFieldsValid();
            }
        });
        etPassword.addTextChangedListener(new TextWatcherAdapter() {

            @Override
            public void afterTextChanged(Editable s) {
                areLoginInputFieldsValid();
            }
        });

        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        tvSignUp.setEnabled(false);
        btLogin.setEnabled(false);

        tvSignUp.setOnClickListener(loginClickListener);
        btLogin.setOnClickListener(loginClickListener);
        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == EditorInfo.IME_ACTION_DONE) {

                    String email = etUsername.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    if (LoginUtils.isValid(email, password)) {
                        getPresenter().login(
                                etUsername.getText().toString(),
                                etPassword.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });

        mSplashScreenAnimation =SplashScreenAnimation.Builder.instance(ivLogoSplash, ivLogo)
                .setAppearingViews(tvSignUp, etUsername, etPassword, btLogin, llInputFields)
                .setSplashDuration(SPLASH_DURATION)
                .setAnimationDuration(ANIMATION_DURATION)
                .withEndAction(this)
                .start(true);

    }

    private void areLoginInputFieldsValid() {

        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!LoginUtils.isValid(email, password)) {
            btLogin.setClickable(false);
            btLogin.setBackgroundColor(Color.parseColor("#3d000000"));
        }
        else {
            btLogin.setClickable(true);
            btLogin.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    public void onAnimEnded() {

        populateInputViews();

        enableUI();
    }

    private void populateInputViews() {
        LoginData data = SharedPreferencesUtils.getLoginData(this);

        etUsername.setText(data.getEmail());
        etPassword.setText(data.getPassword());

        areLoginInputFieldsValid();
    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btLogin.setBackgroundColor(Color.parseColor("#3d000000"));
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        tvSignUp.setEnabled(false);
        btLogin.setEnabled(false);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        btLogin.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        etUsername.setEnabled(true);
        etPassword.setEnabled(true);
        tvSignUp.setEnabled(true);
        btLogin.setEnabled(true);
    }

    @Override
    public void successfulLogin() {
        getPresenter().notifyMovingToNextActivity();
        startActivity(MainActivity.makeIntent(getActivityContext()));
        finish();
    }

    @Override
    public void showAppStateDisabledMessage() {
        appStateContainer.setVisibility(View.VISIBLE);
        tvAppStateMessage.setText(getString(R.string.app_state_unavailable_updating_scores));
        tvAppStateMessageDetails.setText(getString(R.string.app_state_unavailable_come_back_later));
        tvAppStateMessageDetails.setVisibility(View.VISIBLE);
        tryAgainContainer.setVisibility(View.GONE);
    }

    @Override
    public void showAppStateErrorGettingSystemDataMessage() {
        appStateContainer.setVisibility(View.VISIBLE);
        tvAppStateMessage.setText(getString(R.string.app_state_error));
        tvAppStateMessageDetails.setText(getString(R.string.app_state_unavailable_details));
        tryAgainContainer.setVisibility(View.VISIBLE);
        tvTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().getSystemData();
                appStateContainer.setVisibility(View.GONE);
                tvTryAgain.setOnClickListener(null);
            }
        });
    }

    @Override
    public void stopHoldingSplashScreenAnimation() {
        if (mSplashScreenAnimation != null && !mSplashScreenAnimation.hasFinished()) {
            mSplashScreenAnimation.stopHold();
        }
    }

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == tvSignUp) {
                startActivityForResult(SignUpActivity.makeIntent(LoginActivity.this),
                                       SIGN_UP_REQUEST_CODE);
            } else if (v == btLogin) {
                getPresenter().login(
                        etUsername.getText().toString().trim(),
                        etPassword.getText().toString().trim());
            }
        }
    };

    @Override
    public void reportMessage(String message) {
        if (etUsername.hasFocus())
            ViewUtils.showSoftKeyboardAndRequestFocus(etUsername);
        if (etPassword.hasFocus())
            ViewUtils.showSoftKeyboardAndRequestFocus(etPassword);
        //Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
        ViewUtils.showToast(this, message);
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

            ViewUtils.hideSoftKeyboardAndClearFocus(etUsername);
            ViewUtils.hideSoftKeyboardAndClearFocus(etPassword);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
