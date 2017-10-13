package org.hugoandrade.euro2016.backend.view;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import org.hugoandrade.euro2016.backend.FragmentCommunication;
import org.hugoandrade.euro2016.backend.MVP;
import org.hugoandrade.euro2016.backend.R;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.object.SystemData;
import org.hugoandrade.euro2016.backend.presenter.MainPresenter;
import org.hugoandrade.euro2016.backend.utils.UIUtils;
import org.hugoandrade.euro2016.backend.view.fragment.GroupsFragment;
import org.hugoandrade.euro2016.backend.view.fragment.SetResultsFragment;

import java.util.HashMap;
import java.util.List;

public class MainActivity extends ActivityBase<MVP.RequiredViewOps,
                                               MVP.ProvidedPresenterOps,
                                               MainPresenter>
        implements MVP.RequiredViewOps, FragmentCommunication.ProvidedParentActivityOps {

    @SuppressWarnings("unused") private final static String TAG = MainActivity.class.getSimpleName();

    private static final int EDIT_SYSTEM_DATA_REQUEST_CODE = 100;

    private final CharSequence[] mFragmentTitleArray = {"Set Results", "Groups"};
    private final Fragment[] mFragmentArray = {
            new SetResultsFragment(),
            new GroupsFragment()};

    private View vProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        super.onCreate(MainPresenter.class, this);

        initializeUI();
    }

    private void initializeUI() {
        vProgressBar = findViewById(R.id.progressBar_waiting);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    // Method accessed by Fragment
    @Override
    public void popupEditSystemDataDialog() {

        Bundle options = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection unchecked
            options = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        }

        startActivityForResult(
                EditSystemDataActivity.makeIntent(this, getPresenter().getSystemData()),
                EDIT_SYSTEM_DATA_REQUEST_CODE,
                options);
    }

    // Method accessed by Fragment
    @Override
    public void updateMatch(Match match) {
        getPresenter().updateMatch(match);
    }

    // Method accessed by Fragment
    @Override
    public void showSnackBar(String message) {
        reportMessage(message);
    }

    @Override
    public void setAllMatches(List<Match> allMatchesList) {
        for (Fragment fragment : mFragmentArray)
            if (fragment instanceof FragmentCommunication.ProvidedSetResultsChildFragmentOps)
                ((FragmentCommunication.ProvidedSetResultsChildFragmentOps) fragment)
                        .setAllMatches(allMatchesList);
    }

    @Override
    public void setMatch(Match match) {
        for (Fragment fragment : mFragmentArray)
            if (fragment instanceof FragmentCommunication.ProvidedSetResultsChildFragmentOps)
                ((FragmentCommunication.ProvidedSetResultsChildFragmentOps) fragment)
                        .setMatch(match);
    }

    @Override
    public void setGroups(HashMap<String, List<Country>> allGroups) {
        for (Fragment fragment : mFragmentArray)
            if (fragment instanceof FragmentCommunication.ProvidedGroupsChildFragmentOps)
                ((FragmentCommunication.ProvidedGroupsChildFragmentOps) fragment)
                        .setGroups(allGroups);
    }

    @Override
    public void onLoadingUpdate(boolean hasCompletelyLoaded) {
        vProgressBar.setVisibility(hasCompletelyLoaded? View.INVISIBLE: View.VISIBLE);
    }

    @Override
    public void reportMessage(String message) {
        UIUtils.showSnackBar(findViewById(android.R.id.content), message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_SYSTEM_DATA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                SystemData systemData = EditSystemDataActivity.extractSystemDataFromIntent(data);

                getPresenter().setSystemData(systemData);
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return mFragmentArray[position];//SetResultsFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return mFragmentArray.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleArray[position];
        }
    }

}
