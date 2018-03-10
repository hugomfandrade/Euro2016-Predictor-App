package org.hugoandrade.euro2016.predictor.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.LoginData;
import org.hugoandrade.euro2016.predictor.presenter.SignUpPresenter;

public class SignUpActivity extends ActivityBase<MVP.RequiredSignUpViewOps,
                                                    MVP.ProvidedSignUpPresenterOps,
                                                    SignUpPresenter>
        implements MVP.RequiredSignUpViewOps {

    public static final String REGISTER_DATA = "REGISTER_DATA";

    private EditText etUsername, etPassword, etConfirmPassword;
    private RelativeLayout btSignUp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        setContentView(R.layout.activity_sign_up);
        getWindow().setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        setResult(Activity.RESULT_CANCELED);

        initializeViews();

        super.onCreate(SignUpPresenter.class, this);
    }

    private void initializeViews() {
        etUsername        = (EditText) findViewById(R.id.editext_new_username);
        etPassword        = (EditText) findViewById(R.id.editext_new_password);
        etConfirmPassword = (EditText) findViewById(R.id.editext_confirm_password);
        etUsername.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        btSignUp          = (RelativeLayout)   findViewById(R.id.button_signup);
        progressBar       = (ProgressBar) findViewById(R.id.progressBar_signup);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        btSignUp.setOnClickListener(signUpClickListener);
    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        btSignUp.setBackgroundResource(R.drawable.btn_default_pressed);
        etUsername.setEnabled(false);
        etPassword.setEnabled(false);
        etConfirmPassword.setEnabled(false);
        btSignUp.setEnabled(false);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        btSignUp.setBackgroundResource(android.R.drawable.btn_default);
        etUsername.setEnabled(true);
        etPassword.setEnabled(true);
        etConfirmPassword.setEnabled(true);
        btSignUp.setEnabled(true);
    }

    @Override
    public void successfulRegister(LoginData loginData) {
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, new Intent().putExtra(REGISTER_DATA, loginData));
        finish();
    }

    private View.OnClickListener signUpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getPresenter().registerUser(
                    etUsername.getText().toString(),
                    etPassword.getText().toString(),
                    etConfirmPassword.getText().toString()
                    );
        }
    };

    @Override
    public void reportMessage(String message) {
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public static LoginData extractLoginDataFromIntent(Intent intent) {
        return intent.getParcelableExtra(REGISTER_DATA);
    }
}
