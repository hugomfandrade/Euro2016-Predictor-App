package hugoandrade.euro2016.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import hugoandrade.euro2016.FragmentCommunication;
import hugoandrade.euro2016.MVP;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.common.GenericActivity;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.presenter.MainPresenter;
import hugoandrade.euro2016.view.fragment.AllMatchesFragment;
import hugoandrade.euro2016.view.fragment.StandingsFragment;
import hugoandrade.euro2016.view.fragment.SetPredictionsFragment;
import hugoandrade.euro2016.view.fragment.UsersScoresFragment;

public class MainActivity extends GenericActivity<MVP.RequiredMainViewOps,
                                                  MVP.ProvidedMainPresenterOps,
                                                  MainPresenter>
        implements MVP.RequiredMainViewOps,
                   FragmentCommunication.RequiredActivityOps {

    /**
     * Layout to display SnackBar messages
     */
    private CoordinatorLayout mainLayout;
    /**
     * Touch-event-consuming layout located above all views.
     */
    private RelativeLayout progressBar;
    /**
     * FAB to refresh all Data of the app.
     */
    private FloatingActionButton fabResetData;

    /**
     * The Fragments to be displayed in the ViewPager.
     */
    public Fragment[] mFragmentArray = {
            new SetPredictionsFragment(), new AllMatchesFragment(),
            new StandingsFragment(), new UsersScoresFragment()};
    /**
     * The titles of each Fragment.
     */
    public CharSequence[] mFragmentTitleArray = {
            "Bets", "Matches", "Standings", "Scores"};

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
     * initialization and initializing the GenericActivity framework.
     *
     * @param savedInstanceState
     *            Object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Perform first part of initializing the super class.
        super.onCreate(savedInstanceState);

        // Set the default layout.
        setContentView(R.layout.activity_main_activity_temp);

        // (Re)initialize all the View fields.
        initializeViewFields();

        // Perform second part of initializing the super class,
        // passing in the MainPresenter class to instantiate/manage
        // and "this" to provide MainPresenter with the
        // MVP.RequiredMainViewOps instance.
        super.onCreate(MainPresenter.class, this);
    }

    /**
     * Hook method called by Android when this Activity becomes
     * invisible.
     */
    @Override
    protected void onDestroy() {
        getPresenter().onDestroy(isChangingConfigurations());

        super.onDestroy();
    }

    /**
     * Initialize the View fields.
     */
    private void initializeViewFields() {
        // initialize TabLayout.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);

        // initialize ViewPager.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);

        // Store the syncing FloatingActionButton for fast access when hiding or showing it.
        fabResetData = (FloatingActionButton) findViewById(R.id.fab_reset_data);
        fabResetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshAllData();
            }
        });

        // Store the CoordinatorLayout for fast access when showing SnackBar.
        mainLayout = (CoordinatorLayout) findViewById(R.id.activity_main);

        // Store the RelativeLayout for fast access when setting view available or not available.
        // This RelativeLayout is layout'ed over the remaining content and consumes all touch events.
        progressBar = (RelativeLayout) findViewById(R.id.progressBar_waiting_for_configuration);
        progressBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Just consume event
                return true;
            }
        });

        // Initialize sections adapter and et up the ViewPager with the sections adapter.
        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Display message in the SnackBar.
     */
    @Override
    public void reportMessage(String message) {
        showSnackBar(message);
    }

    /**
     * Finish app when "AppState" is false.
     */
    @Override
    public void finishApp() {
        finish();
    }

    /**
     * Get all Fragments associated with the ViewPager of MainActivity
     * that implement a given Interface.
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getAllFragmentsByInterfaceType(Class<T> interfaceType) {
        ArrayList<T> IFragmentList = new ArrayList<>();
        for (Fragment f : mFragmentArray) {
            if (interfaceType.isAssignableFrom(f.getClass()))
                IFragmentList.add((T) f);
        }

        return IFragmentList;
    }

    /**
     * Set view availability by setting the visibility to either GONE or
     * VISIBLE of a view that consumes any touch event of the user, and
     * stop or start the animation of the Syncing AnimationDrawable.
     */
    @Override
    public void updateViewAvailability(boolean viewAvailable) {
        if (viewAvailable) {
            progressBar.setVisibility(View.GONE);
            ((AnimationDrawable) fabResetData.getDrawable()).stop();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            ((AnimationDrawable) fabResetData.getDrawable()).start();
        }
    }
    /**
     * Start UsersPredictionActivity after successfully fetching the list
     * of Predictions of the User selected in the UsersScoresFragment.
     */
    @Override
    public void moveToUsersPredictionActivity(User selectedUser,
                                              ArrayList<Match> matchesList,
                                              ArrayList<Prediction> predictionList) {
        startActivity(UsersPredictionsActivity.makeIntent(
                this, selectedUser, matchesList, predictionList));
        overridePendingTransition(R.anim.right_to_center, R.anim.center_to_left);
    }

    /**
     * The child fragment sends the message to the Parent activity, MainActivity,
     * to be displayed as a SnackBar.
     */
    public void showSnackBar(String message) {
        Snackbar.make(mainLayout,
                message,
                Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Refresh all data by fetching the cloud database: SystemData, all Matches,
     * all Countries, all Predictions of the app user and all Users' scores and
     * username. Initiate by fetching SystemData when the user requests to
     * refresh data, either by "Swipe Refreshing" or by pressing the syncing
     * FloatingActionButton.
     */
    @Override
    public void refreshAllData() {
        getPresenter().refreshAllData();
    }
    /**
     * Initiate the asynchronous update of the provided Predictions when the
     * user presses "Set Prediction" button in the SetPredictionsFragment.
     */
    @Override
    public void putPrediction(Prediction prediction) {
        getPresenter().putPrediction(prediction);
    }

    /**
     * Disable the View layer, initiate the asynchronous Predictions lookup
     * of the selected user and, once all Predictions are fetched, start new
     * activity displaying all Predictions of Matches prior to server time.
     */
    @Override
    public void onUserSelected(User user) {
        getPresenter().onUserSelected(user);
    }

    /**
     * Show FloatingActionButton when the the Scrolling View (Recycler View or
     * SwipeRefreshLayout) is scrolling upwards.
     */
    @Override
    public void showFab() {
        fabResetData.animate().translationY(0)
                .setInterpolator(new DecelerateInterpolator(2)).start();
    }

    /**
     * Hide FloatingActionButton when the the Scrolling View (Recycler View or
     * SwipeRefreshLayout) is scrolling downwards.
     */
    @Override
    public void hideFab() {
        fabResetData.animate().translationY(fabResetData.getHeight() +
                ((ViewGroup.MarginLayoutParams) fabResetData.getLayoutParams()).bottomMargin)
                .setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * @class SectionsPagerAdapter
     *
     * @brief The Adapter that initializes, manages and displays the Fragments into the
     * ViewPager's layout.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

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

        /**
         * Returns the Title of the Fragment at the given position.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleArray[position];
        }
    }
}
