package hugoandrade.euro2016.view;

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

import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.common.GenericActivity;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.presenter.SignUpPresenter;

public class SignUpActivity extends GenericActivity<MVP.RequiredSignUpViewOps,
                                                    MVP.ProvidedSignUpPresenterOps,
                                                    SignUpPresenter>
        implements MVP.RequiredSignUpViewOps {

    private EditText etUsername, etPassword, etConfirmPassword;
    private RelativeLayout btSignUp;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.90);
        setContentView(R.layout.activity_signup);
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
        progressBar.setVisibility(ProgressBar.GONE);

        btSignUp.setOnClickListener(signUpClickListener);
    }

    @Override
    public void updateLayoutEnableState(boolean state) {
        progressBar.setVisibility(state?  ProgressBar.GONE : ProgressBar.VISIBLE);
        btSignUp.setBackgroundResource(state?
                android.R.drawable.btn_default :
                R.drawable.btn_default_pressed);
        etUsername.setEnabled(state);
        etPassword.setEnabled(state);
        etConfirmPassword.setEnabled(state);
        btSignUp.setEnabled(state);
    }

    @Override
    public void successfulRegister(User user) {
        Intent intent = new Intent();
        intent.putExtra(SplashScreenAndLoginActivity.REGISTER_DATA, user);

        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
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
    protected void onDestroy() {
        getPresenter().onDestroy(isChangingConfigurations());

        super.onDestroy();
    }

    @Override
    public void reportMessage(String message) {
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
