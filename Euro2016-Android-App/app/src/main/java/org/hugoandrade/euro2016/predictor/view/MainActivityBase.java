package org.hugoandrade.euro2016.predictor.view;

import android.view.Menu;
import android.view.MenuItem;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.PresenterOps;

public abstract class MainActivityBase<RequiredViewOps,
                                       ProvidedPresenterOps,
                                       PresenterType extends PresenterOps<RequiredViewOps>>

        extends ActivityBase<RequiredViewOps, ProvidedPresenterOps, PresenterType> {


    private boolean isPauseCalled;

    @Override
    protected void onResume() {
        isPauseCalled = false;
        super.onResume();
    }

    @Override
    protected void onPause() {
        isPauseCalled = true;
        super.onPause();
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

    protected abstract void logout();

}
