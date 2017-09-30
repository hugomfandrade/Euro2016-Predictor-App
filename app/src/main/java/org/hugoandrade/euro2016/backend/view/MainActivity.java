package org.hugoandrade.euro2016.backend.view;

import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

import org.hugoandrade.euro2016.backend.FragmentCommunication;
import org.hugoandrade.euro2016.backend.MVP;
import org.hugoandrade.euro2016.backend.R;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.object.Match;
import org.hugoandrade.euro2016.backend.presenter.MainPresenter;
import org.hugoandrade.euro2016.backend.view.fragment.GroupsFragment;
import org.hugoandrade.euro2016.backend.view.fragment.SetResultsFragment;


public class MainActivity extends ActivityBase<
            MVP.RequiredViewOps,
            MVP.ProvidedPresenterOps,
            MainPresenter>
        implements MVP.RequiredViewOps, FragmentCommunication.ProvidedParentActivityOps {

    @SuppressWarnings("unused") private final static String TAG = MainActivity.class.getSimpleName();

    private LinearLayout mainLayout;

    private final CharSequence[] mFragmentTitleArray = {"Set Results", "Groups"};
    private final Fragment[] mFragmentArray = {
            new SetResultsFragment(),
            new GroupsFragment()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        super.onCreate(MainPresenter.class, this);

        initializeViews();
    }

    private void initializeViews() {
        mainLayout = (LinearLayout) findViewById(R.id.main_content);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabanim_tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    // Method accessed by Fragment
    @Override
    public void updateMatch(Match match) {
        getPresenter().updateMatch(match);
    }

    @Override
    public void reportMessage(String message) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setAllMatches(ArrayList<Match> allMatchesList) {
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
    public void setGroups(HashMap<String, ArrayList<Country>> allGroups) {
        for (Fragment fragment : mFragmentArray)
            if (fragment instanceof FragmentCommunication.ProvidedGroupsChildFragmentOps)
                ((FragmentCommunication.ProvidedGroupsChildFragmentOps) fragment)
                        .setGroups(allGroups);
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
