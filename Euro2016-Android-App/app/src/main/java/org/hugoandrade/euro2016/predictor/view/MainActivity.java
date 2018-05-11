package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.common.ServiceManagerOps;
import org.hugoandrade.euro2016.predictor.customview.IconTabLayout;
import org.hugoandrade.euro2016.predictor.customview.NonSwipeableViewPager;
import org.hugoandrade.euro2016.predictor.presenter.MainPresenter;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;
import org.hugoandrade.euro2016.predictor.view.fragment.FragComm;
import org.hugoandrade.euro2016.predictor.view.fragment.LeaguesFragment;
import org.hugoandrade.euro2016.predictor.view.fragment.PredictionsFragment;
import org.hugoandrade.euro2016.predictor.view.fragment.RulesFragment;
import org.hugoandrade.euro2016.predictor.view.fragment.StandingsFragment;

public class MainActivity extends MainActivityBase<MVP.RequiredMainViewOps,
                                                   MVP.ProvidedMainPresenterOps,
                                                   MainPresenter>
        implements MVP.RequiredMainViewOps,
                   FragComm.RequiredActivityOps {

    /**
     * Touch-event-consuming layout located above all views.
     */
    private View progressBar;

    /**
     * The Fragments to be displayed in the ViewPager.
     */
    public Fragment[] mFragmentArray = {
            new PredictionsFragment(),
            new StandingsFragment(),
            new LeaguesFragment(),
            new RulesFragment()
    };
    /**
     * The titles of each Fragment.
     */
    public int[] mFragmentTitleResArray = {
            R.string.predictions, R.string.standings, R.string.leagues, R.string.rules };
    /**
     * The titles of each Fragment.
     */
    public int[] mFragmentIconArray = {
            R.drawable.ic_soccer_field, R.drawable.ic_podium,
            R.drawable.ic_trophy, R.drawable.ic_rules};

    /**
     * Factory method that returns an implicit Intent for displaying
     * images.
     */
    public static Intent makeIntent(Context activityContext) {
        return new Intent(activityContext, MainActivity.class);
    }

    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., UI layout
     * initialization and initializing of the ActivityBase framework.
     *
     * @param savedInstanceState
     *            Object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Perform first part of initializing the super class.
        super.onCreate(savedInstanceState);

        // (Re)initialize all the View fields.
        initializeUI();

        // Perform second part of initializing the super class,
        // passing in the MainPresenter class to instantiate/manage
        // and "this" to provide MainPresenter with the
        // MVP.RequiredMainViewOps instance.
        super.onCreate(MainPresenter.class, this);
    }

    /**
     * Initialize the View fields.
     */
    private void initializeUI() {

        // Set the default layout.
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // initialize TabLayout.
        TabLayout tabLayout = findViewById(R.id.tabanim_tabs);

        // initialize ViewPager.
        NonSwipeableViewPager viewPager = findViewById(R.id.container);
        viewPager.setSmoothTransition(false);

        // Store the RelativeLayout for fast access when setting view available or not available.
        // This RelativeLayout is layout'ed over the remaining content and consumes all touch events.
        progressBar = findViewById(R.id.progressBar_waiting);

        // Initialize sections adapter and et up the ViewPager with the sections adapter.
        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Disable view availability by setting the visibility to VISIBLE of a
     * view that consumes any touch event of the user, and start the
     * animation of the Syncing AnimationDrawable. This methods is used while
     * fetching data from the cloud.
     */
    @Override
    public void disableUI() {
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Enable view availability by setting the visibility to INVISIBLE of a
     * view that consumes any touch event of the user, and stop the
     * animation of the Syncing AnimationDrawable.
     */
    @Override
    public void enableUI() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void reportMessage(String message) {
        showSnackBar(message);
    }

    /**
     * The child fragment sends the message to the Parent activity, MainActivity,
     * to be displayed as a SnackBar.
     */
    public void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public ServiceManager getServiceManager() {
        return getPresenter().getServiceManager();
    }

    @Override
    public void notifyServiceIsBound() {
        for (Fragment f : mFragmentArray) {
            if (f instanceof ServiceManagerOps) {
                ((ServiceManagerOps) f).notifyServiceIsBound();
            }
        }
    }

    @Override
    protected void logout() {
        getPresenter().logout();

        super.logout();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter implements IconTabLayout.IconTabLayoutListener {

        /**
         * Creates the SectionsPagerAdapter and provides the FragmentManager.
         */
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Returns the Fragment at the given position.
         */
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a Fragment listed in the Fragment Array.
            return mFragmentArray[position];
        }

        /**
         * Returns the count of Fragments in the list.
         */
        @Override
        public int getCount() {
            return mFragmentArray.length;
        }

        @Override
        public int getPageIcon(int position) {
            return mFragmentIconArray[position];
        }

        /**
         * Returns the Title of the Fragment at the given position.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            //return mFragmentTitleArray[position];
            return getString(mFragmentTitleResArray[position]);
        }
    }
}
