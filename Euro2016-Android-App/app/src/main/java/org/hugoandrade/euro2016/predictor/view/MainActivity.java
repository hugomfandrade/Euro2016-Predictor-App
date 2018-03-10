package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Group;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.presenter.MainPresenter;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.*;
import org.hugoandrade.euro2016.predictor.view.fragment.MatchesFragment;
import org.hugoandrade.euro2016.predictor.view.fragment.StandingsFragment;
import org.hugoandrade.euro2016.predictor.view.fragment.PredictionsFragment;
import org.hugoandrade.euro2016.predictor.view.fragment.UsersFragment;

public class MainActivity extends ActivityBase<MVP.RequiredMainViewOps,
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
            new PredictionsFragment(), new MatchesFragment(),
            new StandingsFragment(), new UsersFragment()};
    /**
     * The titles of each Fragment.
     */
    public CharSequence[] mFragmentTitleArray = {
            "Predictions", "Matches", "Standings", "Scores"};

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

        // initialize TabLayout.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);

        // initialize ViewPager.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);

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

    @Override
    public void setUserList(List<User> userList) {
        // Get all fragments which implement "FragmentCommunication.ProvidedAllUsersFragmentOps"
        // and send the list of Users to those Fragments
        for (FragComm.ProvidedUsersFragmentOps IAllUsersFrag :
                getAllFragmentsByInterfaceType(FragComm.ProvidedUsersFragmentOps.class))
            IAllUsersFrag.setUsers(userList);
    }

    @Override
    public void setGroups(HashMap<SGroup, Group> groupMap) {
        // Get all fragments which implement "FragmentCommunication.ProvidedAllCountriesFragmentOps"
        // and send the map of Groups to those Fragments
        for (FragComm.ProvidedCountriesFragmentOps IAllCountriesFrag :
                getAllFragmentsByInterfaceType(FragComm.ProvidedCountriesFragmentOps.class))
            IAllCountriesFrag.setGroups(groupMap);
    }

    @Override
    public void setMatches(HashMap<SStage, List<Match>> matchMap) {
        // Get all fragments which implement "FragmentCommunication.ProvidedAllMatchesFragmentOps"
        // and send the map of Matches to those Fragments
        for (FragComm.ProvidedMatchesFragmentOps IAllMatchesFrag :
                getAllFragmentsByInterfaceType(FragComm.ProvidedMatchesFragmentOps.class))
            IAllMatchesFrag.setMatches(matchMap);
    }

    @Override
    public void setPredictions(List<Prediction> predictionList) {

        // Get all fragments which implement "FragmentCommunication.ProvidedAllPredictionsFragmentOps"
        // and send the list of Predictions to those Fragments

        for (FragComm.ProvidedPredictionsFragmentOps IAllPredictionsFrag :
                getAllFragmentsByInterfaceType(FragComm.ProvidedPredictionsFragmentOps.class))
            IAllPredictionsFrag.setPredictions(predictionList);
    }

    @Override
    public void updatePrediction(Prediction prediction) {
        for (FragComm.ProvidedPredictionsFragmentOps IAllPredictionsFrag :
                getAllFragmentsByInterfaceType(FragComm.ProvidedPredictionsFragmentOps.class))
            IAllPredictionsFrag.updatePrediction(prediction);
    }

    @Override
    public void updateFailedPrediction(Prediction prediction) {
        for (FragComm.ProvidedPredictionsFragmentOps IAllPredictionsFrag :
                getAllFragmentsByInterfaceType(FragComm.ProvidedPredictionsFragmentOps.class))
            IAllPredictionsFrag.updateFailedPrediction(prediction);
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
     * Start UsersPredictionActivity after successfully fetching the list
     * of Predictions of the User selected in the UsersScoresFragment.
     */
    @Override
    public void moveToUsersPredictionActivity(User selectedUser,
                                              List<Match> matchesList,
                                              List<Prediction> predictionList) {
        startActivity(UsersPredictionsActivity.makeIntent(
                this, selectedUser, matchesList, predictionList));
        overridePendingTransition(R.anim.right_to_center, R.anim.center_to_left);
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
        getPresenter().getPredictionsOfSelectedUser(user);
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
