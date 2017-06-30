package hugoandrade.euro2016.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import hugoandrade.euro2016.FragmentCommunication;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.common.CustomScrollChangeListener;
import hugoandrade.euro2016.common.GenericFragment;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.view.listadapter.GroupListAdapter;
import hugoandrade.euro2016.view.listadapter.KnockoutListAdapter;

public class StandingsFragment
        extends GenericFragment<FragmentCommunication.RequiredActivityOps>
        implements FragmentCommunication.ProvidedAllCountriesFragmentOps,
                   FragmentCommunication.ProvidedAllMatchesFragmentOps {

    // Views
    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private GroupListAdapter mGroupAAdapter, mGroupBAdapter, mGroupCAdapter,
            mGroupDAdapter, mGroupEAdapter, mGroupFAdapter;
    private KnockoutListAdapter mRoundOf16Adapter, mQuarterFinalAdapter,
            mSemiFinalAdapter, mFinalAdapter;

    // Data
    private final HashMap<String, ArrayList<Country>> mAllGroupsMap = new HashMap<>();
    private final HashMap<String, ArrayList<Match>> mMatchesMap = new HashMap<>();
    private final HashMap<String, Country> mCountriesMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_fragment_standings);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getParentActivity().refreshAllData();
            }
        });
        swipeRefreshLayout.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            view.findViewById(R.id.nsv_standings)
                    .setOnScrollChangeListener(new CustomScrollChangeListener() {
                @Override
                public void show() {
                    getParentActivity().showFab();
                }

                @Override
                public void hide() {
                    getParentActivity().hideFab();
                }
            });

        RecyclerView lvGroupA = (RecyclerView) view.findViewById(R.id.lv_group_a);
        RecyclerView lvGroupB = (RecyclerView) view.findViewById(R.id.lv_group_b);
        RecyclerView lvGroupC = (RecyclerView) view.findViewById(R.id.lv_group_c);
        RecyclerView lvGroupD = (RecyclerView) view.findViewById(R.id.lv_group_d);
        RecyclerView lvGroupE = (RecyclerView) view.findViewById(R.id.lv_group_e);
        RecyclerView lvGroupF = (RecyclerView) view.findViewById(R.id.lv_group_f);
        RecyclerView lvRoundOf16 = (RecyclerView) view.findViewById(R.id.lv_round_of_16);
        RecyclerView lvQuarterFinal = (RecyclerView) view.findViewById(R.id.lv_quarter_finals);
        RecyclerView lvSemiFinal = (RecyclerView) view.findViewById(R.id.lv_semi_finals);
        RecyclerView lvFinal = (RecyclerView) view.findViewById(R.id.lv_final);

        lvGroupA.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvGroupB.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvGroupC.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvGroupD.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvGroupE.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvGroupF.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvRoundOf16.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvQuarterFinal.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvSemiFinal.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        lvFinal.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        lvGroupA.setNestedScrollingEnabled(false);
        lvGroupB.setNestedScrollingEnabled(false);
        lvGroupC.setNestedScrollingEnabled(false);
        lvGroupD.setNestedScrollingEnabled(false);
        lvGroupE.setNestedScrollingEnabled(false);
        lvGroupF.setNestedScrollingEnabled(false);
        lvRoundOf16.setNestedScrollingEnabled(false);
        lvQuarterFinal.setNestedScrollingEnabled(false);
        lvSemiFinal.setNestedScrollingEnabled(false);
        lvFinal.setNestedScrollingEnabled(false);

        if (mGroupAAdapter == null) mGroupAAdapter = new GroupListAdapter(mAllGroupsMap.get("A"));
        if (mGroupBAdapter == null) mGroupBAdapter = new GroupListAdapter(mAllGroupsMap.get("B"));
        if (mGroupCAdapter == null) mGroupCAdapter = new GroupListAdapter(mAllGroupsMap.get("C"));
        if (mGroupDAdapter == null) mGroupDAdapter = new GroupListAdapter(mAllGroupsMap.get("D"));
        if (mGroupEAdapter == null) mGroupEAdapter = new GroupListAdapter(mAllGroupsMap.get("E"));
        if (mGroupFAdapter == null) mGroupFAdapter = new GroupListAdapter(mAllGroupsMap.get("F"));

        if (mRoundOf16Adapter == null) mRoundOf16Adapter = new KnockoutListAdapter(mMatchesMap.get(Match.ROUND_OF_16));
        if (mQuarterFinalAdapter == null)  mQuarterFinalAdapter = new KnockoutListAdapter(mMatchesMap.get(Match.QUARTER_FINALS));
        if (mSemiFinalAdapter == null) mSemiFinalAdapter = new KnockoutListAdapter(mMatchesMap.get(Match.SEMI_FINALS));
        if (mFinalAdapter == null) mFinalAdapter = new KnockoutListAdapter(mMatchesMap.get(Match.FINAL));

        lvGroupA.setAdapter(mGroupAAdapter);
        lvGroupB.setAdapter(mGroupBAdapter);
        lvGroupC.setAdapter(mGroupCAdapter);
        lvGroupD.setAdapter(mGroupDAdapter);
        lvGroupE.setAdapter(mGroupEAdapter);
        lvGroupF.setAdapter(mGroupFAdapter);
        lvRoundOf16.setAdapter(mRoundOf16Adapter);
        lvQuarterFinal.setAdapter(mQuarterFinalAdapter);
        lvSemiFinal.setAdapter(mSemiFinalAdapter);
        lvFinal.setAdapter(mFinalAdapter);
    }

    /**
     * Display the List of Countries in the appropriate View. Split them
     * by Group, sort them by position
     */
    @Override
    public void setAllCountries(ArrayList<Country> allCountriesList) {
        mAllGroupsMap.clear();
        mCountriesMap.clear();

        for (Country c : allCountriesList){
            if (mAllGroupsMap.containsKey(c.group)) {
                mAllGroupsMap.get(c.group).add(c);
            } else {
                mAllGroupsMap.put(c.group, new ArrayList<Country>());
                mAllGroupsMap.get(c.group).add(c);
            }
            mCountriesMap.put(c.name, c);
        }

        updateGroupAdapter();

        Collections.sort(mAllGroupsMap.get("A"), countryComparator);
        Collections.sort(mAllGroupsMap.get("B"), countryComparator);
        Collections.sort(mAllGroupsMap.get("C"), countryComparator);
        Collections.sort(mAllGroupsMap.get("D"), countryComparator);
        Collections.sort(mAllGroupsMap.get("E"), countryComparator);
        Collections.sort(mAllGroupsMap.get("F"), countryComparator);
        if (mGroupAAdapter != null) mGroupAAdapter.setAllCountries(mAllGroupsMap.get("A"));
        if (mGroupBAdapter != null) mGroupBAdapter.setAllCountries(mAllGroupsMap.get("B"));
        if (mGroupCAdapter != null) mGroupCAdapter.setAllCountries(mAllGroupsMap.get("C"));
        if (mGroupDAdapter != null) mGroupDAdapter.setAllCountries(mAllGroupsMap.get("D"));
        if (mGroupEAdapter != null) mGroupEAdapter.setAllCountries(mAllGroupsMap.get("E"));
        if (mGroupFAdapter != null) mGroupFAdapter.setAllCountries(mAllGroupsMap.get("F"));
    }

    /**
     * Display the List of Matches in the appropriate View
     */
    @Override
    public void setAllMatches(ArrayList<Match> allMatchesList) {
        mMatchesMap.clear();

        for (Match m : allMatchesList){
            switch (m.stage) {
                case Match.ROUND_OF_16:
                case Match.QUARTER_FINALS:
                case Match.SEMI_FINALS:
                case Match.FINAL:
                    if (mMatchesMap.containsKey(m.stage)) {
                        mMatchesMap.get(m.stage).add(m);
                    } else {
                        mMatchesMap.put(m.stage, new ArrayList<Match>());
                        mMatchesMap.get(m.stage).add(m);
                    }
                    break;

            }
        }

        if (mRoundOf16Adapter != null) mRoundOf16Adapter.setAllMatches(mMatchesMap.get(Match.ROUND_OF_16));
        if (mQuarterFinalAdapter != null) mQuarterFinalAdapter.setAllMatches(mMatchesMap.get(Match.QUARTER_FINALS));
        if (mSemiFinalAdapter != null) mSemiFinalAdapter.setAllMatches(mMatchesMap.get(Match.SEMI_FINALS));
        if (mFinalAdapter != null) mFinalAdapter.setAllMatches(mMatchesMap.get(Match.FINAL));

        updateGroupAdapter();

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }

    private void updateGroupAdapter() {
        for (Match match : mMatchesMap.get(Match.ROUND_OF_16)) {
            // Home Team
            Country homeCountry = mCountriesMap.get(match.homeTeam);
            if (homeCountry != null) {
                ArrayList<Country> groupHomeCountry = mAllGroupsMap.get(homeCountry.group);
                if (groupHomeCountry != null)
                    for (Country c : groupHomeCountry)
                        if (c.name.equals(homeCountry.name)) {
                            c.advancedGroupStage();
                            break;
                        }
            }
            // Away Team
            Country awayCountry = mCountriesMap.get(match.awayTeam);
            if (awayCountry != null) {
                ArrayList<Country> groupAwayCountry = mAllGroupsMap.get(awayCountry.group);
                if (groupAwayCountry != null)
                    for (Country c : groupAwayCountry)
                        if (c.name.equals(awayCountry.name)) {
                            c.advancedGroupStage();
                            break;
                        }
            }
        }
        if (mGroupAAdapter != null) mGroupAAdapter.setAllCountries(mAllGroupsMap.get("A"));
        if (mGroupBAdapter != null) mGroupBAdapter.setAllCountries(mAllGroupsMap.get("B"));
        if (mGroupCAdapter != null) mGroupCAdapter.setAllCountries(mAllGroupsMap.get("C"));
        if (mGroupDAdapter != null) mGroupDAdapter.setAllCountries(mAllGroupsMap.get("D"));
        if (mGroupEAdapter != null) mGroupEAdapter.setAllCountries(mAllGroupsMap.get("E"));
        if (mGroupFAdapter != null) mGroupFAdapter.setAllCountries(mAllGroupsMap.get("F"));
    }

    /**
     * Comparator to sort Countries by position.
     */
    private static Comparator<Country> countryComparator = new Comparator<Country>() {
        @Override
        public int compare(Country lhs, Country rhs) {
            return lhs.position - rhs.position;
        }
    };
}