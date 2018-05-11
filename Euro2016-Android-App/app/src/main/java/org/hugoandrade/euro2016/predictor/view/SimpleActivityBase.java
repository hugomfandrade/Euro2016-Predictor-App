package org.hugoandrade.euro2016.predictor.view;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.utils.NetworkBroadcastReceiverUtils;
import org.hugoandrade.euro2016.predictor.utils.NetworkUtils;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

public abstract class SimpleActivityBase extends AppCompatActivity {

    protected final String TAG = getClass().getSimpleName();

    private BroadcastReceiver mNetworkBroadcastReceiver;
    private View tvNoNetworkConnection;

    private boolean isPauseCalled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetworkBroadcastReceiver = NetworkBroadcastReceiverUtils.register(this, iNetworkListener);

        tvNoNetworkConnection = findViewById(R.id.tv_no_network_connection);

        ViewUtils.setHeightDp(this, tvNoNetworkConnection,
                NetworkUtils.isNetworkAvailable(this)? 0 : 20);

    }

    @Override
    protected void onResume() {
        isPauseCalled = false;
        super.onResume();

        if (tvNoNetworkConnection == null) {
            tvNoNetworkConnection = findViewById(R.id.tv_no_network_connection);

            ViewUtils.setHeightDp(this, tvNoNetworkConnection,
                    NetworkUtils.isNetworkAvailable(this) ? 0 : 20);
        }
    }

    @Override
    protected void onPause() {
        isPauseCalled = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mNetworkBroadcastReceiver != null) {
            NetworkBroadcastReceiverUtils.unregister(this, mNetworkBroadcastReceiver);
            mNetworkBroadcastReceiver = null;
        }

    }

    protected final boolean isPaused() {
        return isPauseCalled;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_logout:
                logout();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void logout() {
        SharedPreferencesUtils.resetLastAuthenticatedLoginData(this);
        startActivity(LoginActivity.makeIntent(this));
        finish();
    }

    private NetworkBroadcastReceiverUtils.INetworkBroadcastReceiver iNetworkListener
            = new NetworkBroadcastReceiverUtils.INetworkBroadcastReceiver() {
        @Override
        public void setNetworkAvailable(boolean isNetworkAvailable) {
            ViewUtils.setHeightDpAnim(SimpleActivityBase.this, tvNoNetworkConnection,
                    NetworkUtils.isNetworkAvailable(SimpleActivityBase.this)? 0 : 20);
        }
    };

}
