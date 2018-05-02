package org.hugoandrade.euro2016.predictor.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.customview.ImeEditText;
import org.hugoandrade.euro2016.predictor.data.raw.LoginData;
import org.hugoandrade.euro2016.predictor.presenter.SignUpPresenter;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

public class SignUpActivity extends ActivityBase<MVP.RequiredSignUpViewOps,
                                                 MVP.ProvidedSignUpPresenterOps,
                                                 SignUpPresenter>
        implements MVP.RequiredSignUpViewOps {

    private static final String INTENT_EXTRA_SIGN_UP = "intent_extra_sign_up";

    private ImeEditText etUsername, etPassword, etConfirmPassword;
    private RelativeLayout btSignUp;
    private ProgressBar progressBar;

    public static Intent makeIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    public static LoginData extractLoginDataFromIntent(Intent intent) {
        return intent.getParcelableExtra(INTENT_EXTRA_SIGN_UP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(Activity.RESULT_CANCELED);

        initializeUI();

        super.onCreate(SignUpPresenter.class, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        ViewUtils.showToast(this, "BackPRessed");
        finish();
    }

    private void initializeUI() {
        setContentView(R.layout.activity_sign_up);

        etUsername        = findViewById(R.id.editext_new_username);
        etPassword        = findViewById(R.id.editext_new_password);
        etConfirmPassword = findViewById(R.id.editext_confirm_password);
        etConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    getPresenter().registerUser(
                            etUsername.getText().toString(),
                            etPassword.getText().toString(),
                            etConfirmPassword.getText().toString());
                    return true;
                }
                return false;
            }
        });
        ImeEditText.OnKeyPreImeListener listener = new ImeEditText.OnKeyPreImeListener() {
            @Override
            public boolean onKeyPreIme(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                    return true;
                }
                return false;
            }
        };
        etUsername.setOnKeyPreImeListener(listener);
        etPassword.setOnKeyPreImeListener(listener);
        etConfirmPassword.setOnKeyPreImeListener(listener);
        btSignUp          = findViewById(R.id.button_signup);
        progressBar       = findViewById(R.id.progressBar_signup);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        btSignUp.setOnClickListener(signUpClickListener);
    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btSignUp.setBackgroundColor(Color.parseColor("#3d000000"));
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        etConfirmPassword.setEnabled(false);
        btSignUp.setEnabled(false);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        btSignUp.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        etUsername.setEnabled(true);
        etPassword.setEnabled(true);
        etConfirmPassword.setEnabled(true);
        btSignUp.setEnabled(true);
    }

    @Override
    public void successfulRegister(LoginData loginData) {
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, new Intent().putExtra(INTENT_EXTRA_SIGN_UP, loginData));
        finish();
    }

    private View.OnClickListener signUpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getPresenter().registerUser(
                    etUsername.getText().toString(),
                    etPassword.getText().toString(),
                    etConfirmPassword.getText().toString());
        }
    };

    @Override
    public void reportMessage(String message) {
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
