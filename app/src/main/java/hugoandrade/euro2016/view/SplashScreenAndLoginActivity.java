package hugoandrade.euro2016.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.common.GenericActivity;
import hugoandrade.euro2016.object.LoginData;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.model.AzureMobileService;
import hugoandrade.euro2016.presenter.LoginPresenter;

public class SplashScreenAndLoginActivity extends GenericActivity<MVP.RequiredLoginViewOps,
                                                   MVP.ProvidedLoginPresenterOps,
                                                   LoginPresenter>
        implements MVP.RequiredLoginViewOps {

    private final static int SPLASH_DURATION = 2000; // 2 seconds
    private final static int SPLASH_OFFSET = 200; // 0.2 seconds
    private final static int ANIMATION_DURATION = 500; // 0.5 seconds

    // Views
    private LinearLayout llInputFields;
    private RelativeLayout mainLayout, btLogin;
    private EditText etUsername, etPassword;
    private TextView tvSignUp;
    private ProgressBar progressBar;
    private ImageView ivLogo;

    public static final String REGISTER_DATA = "REGISTER_DATA";
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_and_login);

        initializeViews();

        GlobalData.resetUser();
        startService(AzureMobileService.makeIntent(getApplicationContext()));

        // run a thread after SPLASH_DURATION seconds to start the home screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Move to logo to top of input fields
                ivLogo.startAnimation(new MyAnimator(ivLogo,
                        (int) TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 100,
                                getResources().getDisplayMetrics()),
                        ANIMATION_DURATION));
            }
        }, SPLASH_DURATION);// run a thread after SPLASH_DURATION seconds to start the home screen

        // run a thread after SPLASH_DURATION + SPLASH_OFFSET seconds to start the home screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(ANIMATION_DURATION);
                llInputFields.startAnimation(anim);
                llInputFields.setVisibility(View.VISIBLE);
                tvSignUp.startAnimation(anim);
                tvSignUp.setVisibility(View.VISIBLE);
            }
        }, SPLASH_DURATION + SPLASH_OFFSET);

        super.onCreate(LoginPresenter.class, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateInputViews();
    }

    private void populateInputViews() {
        LoginData data = getPresenter().getLoginDataFromSharedPreferences();

        etUsername.setText(data.username);
        etPassword.setText(data.password);
    }

    @Override
    public void updateLayoutEnableState(boolean state) {
        progressBar.setVisibility(state? ProgressBar.GONE :  ProgressBar.VISIBLE );
        btLogin.setBackgroundResource(state?
                android.R.drawable.btn_default :
                R.drawable.btn_default_pressed);
        etUsername.setEnabled(state);
        etPassword.setEnabled(state);
        tvSignUp.setEnabled(state);
        btLogin.setEnabled(state);
    }

    @Override
    public void successfulLogin() {
        getPresenter().notifyMovingToNextActivity();
        startActivity(MainActivity.makeIntent(getActivityContext()));//, TempActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        getPresenter().onDestroy(isChangingConfigurations());

        super.onDestroy();
    }

    private void initializeViews() {
        mainLayout      = (RelativeLayout) findViewById(R.id.activity_login);
        tvSignUp        = (TextView) findViewById(R.id.tv_sign_up);
        etUsername      = (EditText) findViewById(R.id.et_username_login);
        etPassword      = (EditText) findViewById(R.id.et_password_login);
        btLogin         = (RelativeLayout) findViewById(R.id.bt_login);
        ivLogo          = (ImageView) findViewById(R.id.iv_logo);
        llInputFields   = (LinearLayout) findViewById(R.id.ll_login_input_fields);
        progressBar     = (ProgressBar) findViewById(R.id.progressBar_login);
        progressBar.setVisibility(ProgressBar.GONE);

        tvSignUp.setOnClickListener(loginClickListener);
        btLogin.setOnClickListener(loginClickListener);
    }

    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == tvSignUp) {
                Intent intent = new Intent(SplashScreenAndLoginActivity.this, SignUpActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            } else if (v == btLogin) {
                getPresenter().login(
                        etUsername.getText().toString(),
                        etPassword.getText().toString());
            }
        }
    };

    public void showSnackBar(String message) {
        Snackbar.make(mainLayout,
                message,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void reportMessage(String message) {
        showSnackBar(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            User user = data.getParcelableExtra(REGISTER_DATA);
            getPresenter().notifySuccessfulSignUp(user);
        }
    }

    class MyAnimator extends Animation {

        // The views to be animated
        private View view;

        // The dimensions and animation values (this is stored so other changes to layout don't interfere)
        private final int fromDimension; // Dimension to animate from
        private final int fromBottomMargin; // Dimension to animate from
        private int toDimension; // Dimension to animate to

        private RelativeLayout.LayoutParams params;

        // Constructor
        MyAnimator(View view, int toDimension, long duration) {
            // Setup references
            // the view to animate
            this.view = view;
            // Get the current starting point of the animation (the current width or height of the provided view)
            this.fromDimension = view.getLayoutParams().height;
            this.fromBottomMargin = ((RelativeLayout.LayoutParams) view.getLayoutParams()).bottomMargin;

            this.params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            // Dimension to animate to
            this.toDimension = toDimension;
            // See enum above, the type of animation
            // Set the duration of the animation
            this.setDuration(duration);
        }

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            // Used to apply the animation to the view
            // Animate given the height or width
            view.getLayoutParams().height =
                    fromDimension + (int) ((toDimension - fromDimension) * interpolatedTime);

            params.bottomMargin =
                    fromBottomMargin + (int) ((0 - fromBottomMargin) * interpolatedTime);
            view.setLayoutParams(params);

            // Ensure the views are measured appropriately
            view.requestLayout();
        }
    }
}
