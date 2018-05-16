package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Group;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SGroup;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SStage;
import org.hugoandrade.euro2016.predictor.view.CountryDetailsActivity;
import org.hugoandrade.euro2016.predictor.view.listadapter.GroupListAdapter;
import org.hugoandrade.euro2016.predictor.view.listadapter.KnockoutListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class StandingsFragment extends FragmentBase<FragComm.RequiredActivityBaseOps> {

    // Views
    private HashMap<SGroup, GroupViewStruct> mGroupViewStructMap = buildGroupViewStructMap();
    private HashMap<SStage, KnockOutViewStruct> mKnockOutViewStructMap = buildKnockOutViewStructMap();

    private NestedScrollView nestedScrollView;

    private HashMap<SGroup, GroupViewStruct> buildGroupViewStructMap() {
        HashMap<SGroup, GroupViewStruct> viewStructMap = new HashMap<>();
        viewStructMap.put(SGroup.A, new GroupViewStruct(R.string.group_a));
        viewStructMap.put(SGroup.B, new GroupViewStruct(R.string.group_b));
        viewStructMap.put(SGroup.C, new GroupViewStruct(R.string.group_c));
        viewStructMap.put(SGroup.D, new GroupViewStruct(R.string.group_d));
        viewStructMap.put(SGroup.E, new GroupViewStruct(R.string.group_e));
        viewStructMap.put(SGroup.F, new GroupViewStruct(R.string.group_f));
        return viewStructMap;
    }

    private HashMap<SStage, KnockOutViewStruct> buildKnockOutViewStructMap() {
        HashMap<SStage, KnockOutViewStruct> viewStructMap = new HashMap<>();
        viewStructMap.put(SStage.roundOf16, new KnockOutViewStruct(R.string.round_of_16));
        viewStructMap.put(SStage.quarterFinals, new KnockOutViewStruct(R.string.quarter_finals));
        viewStructMap.put(SStage.semiFinals, new KnockOutViewStruct(R.string.semi_finals));
        viewStructMap.put(SStage.finals, new KnockOutViewStruct(R.string.finals));
        return viewStructMap;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().addOnCountriesChangedListener(mOnCountriesChangedListener);

        return inflater.inflate(R.layout.fragment_standings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        nestedScrollView = view.findViewById(R.id.nsv_standings);

        setupGroupLayout(view.findViewById(R.id.layout_group_a),
                mGroupViewStructMap.get(SGroup.A));
        setupGroupLayout(view.findViewById(R.id.layout_group_b),
                mGroupViewStructMap.get(SGroup.B));
        setupGroupLayout(view.findViewById(R.id.layout_group_c),
                mGroupViewStructMap.get(SGroup.C));
        setupGroupLayout(view.findViewById(R.id.layout_group_d),
                mGroupViewStructMap.get(SGroup.D));
        setupGroupLayout(view.findViewById(R.id.layout_group_e),
                mGroupViewStructMap.get(SGroup.E));
        setupGroupLayout(view.findViewById(R.id.layout_group_f),
                mGroupViewStructMap.get(SGroup.F));

        setupKnockOutLayout(view.findViewById(R.id.layout_round_of_16),
                mKnockOutViewStructMap.get(SStage.roundOf16));
        setupKnockOutLayout(view.findViewById(R.id.layout_quarter_finals),
                mKnockOutViewStructMap.get(SStage.quarterFinals));
        setupKnockOutLayout(view.findViewById(R.id.layout_semi_finals),
                mKnockOutViewStructMap.get(SStage.semiFinals));
        setupKnockOutLayout(view.findViewById(R.id.layout_final),
                mKnockOutViewStructMap.get(SStage.finals));

        updateKnockOutView();
        updateGroupView();

        setupInitialScrollPosition();
    }

    private void setupInitialScrollPosition() {

        if (nestedScrollView == null)
            return;

        if (true)
            return;

        nestedScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                nestedScrollView.post(setupInitialScrollPositionRunnable());
                //nestedScrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private Runnable setupInitialScrollPositionRunnable() {

        return new Runnable() {
            @Override
            public void run() {

                int currentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatch(
                        GlobalData.getInstance().getMatchList(),
                        GlobalData.getInstance().getServerTime().getTime());

                if (currentMatchNumber == 0) {
                    nestedScrollView.smoothScrollTo(0, 0);
                }
                else if (currentMatchNumber == 52) {
                    nestedScrollView.smoothScrollTo(0, 0);
                }
                else {
                    Match match = GlobalData.getInstance().getMatch(currentMatchNumber);
                    SStage stage = null;

                    android.util.Log.e(TAG, "match number::" + currentMatchNumber);
                    android.util.Log.e(TAG, "match ::" + match);

                    if (match == null)
                        return;

                    if (SStage.roundOf16.name.equals(match.getStage())) {
                        stage = SStage.roundOf16;
                    }
                    else if (SStage.quarterFinals.name.equals(match.getStage())) {
                        stage = SStage.quarterFinals;
                    }
                    else if (SStage.semiFinals.name.equals(match.getStage())) {
                        stage = SStage.semiFinals;
                    }
                    else if (SStage.finals.name.equals(match.getStage())) {
                        stage = SStage.finals;
                    }

                    if (stage != null && mKnockOutViewStructMap.containsKey(stage)) {
                        int scrollTo = mKnockOutViewStructMap.get(stage).tvTitle.getTop();
                        android.util.Log.e(TAG, "scrollTo::" + scrollTo);
                        nestedScrollView.smoothScrollTo(0, //nestedScrollView.getTop() -
                                scrollTo);
                    }
                    else {
                        nestedScrollView.smoothScrollTo(0, 0);
                    }
                }
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        GlobalData.getInstance().removeOnMatchesChangedListener(mOnMatchesChangedListener);
        GlobalData.getInstance().removeOnCountriesChangedListener(mOnCountriesChangedListener);
    }

    private GlobalData.OnMatchesChangedListener mOnMatchesChangedListener
            = new GlobalData.OnMatchesChangedListener() {

        @Override
        public void onMatchesChanged() {
            updateKnockOutView();

            setupInitialScrollPosition();
        }
    };

    private GlobalData.OnCountriesChangedListener mOnCountriesChangedListener
            = new GlobalData.OnCountriesChangedListener() {

        @Override
        public void onCountriesChanged() {
            updateGroupView();

            setupInitialScrollPosition();
        }
    };

    private void updateGroupView() {
        HashMap<SGroup, Group> groupsMap = setupGroups(GlobalData.getInstance().getCountryList());

        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.A), groupsMap.get(SGroup.A));
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.B), groupsMap.get(SGroup.B));
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.C), groupsMap.get(SGroup.C));
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.D), groupsMap.get(SGroup.D));
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.E), groupsMap.get(SGroup.E));
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.F), groupsMap.get(SGroup.F));
    }

    private void updateKnockOutView() {

        HashMap<SStage, List<Match>> matchMap = setupMatches(GlobalData.getInstance().getMatchList());

        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.roundOf16), matchMap.get(SStage.roundOf16));
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.quarterFinals), matchMap.get(SStage.quarterFinals));
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.semiFinals), matchMap.get(SStage.semiFinals));
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.finals), matchMap.get(SStage.finals));
    }

    private void setupGroupLayout(View view, GroupViewStruct groupViewStruct) {
        // Setup title
        TextView tvGroupTitle = view.findViewById(R.id.tv_group);
        tvGroupTitle.setText(getString(groupViewStruct.getTitleResource()));

        // Setup recycler view
        RecyclerView recyclerView = view.findViewById(R.id.rv_group);
        recyclerView.setAdapter(groupViewStruct.getAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void setupKnockOutLayout(View view, KnockOutViewStruct knockOutViewStruct) {
        // Setup title
        TextView tvKnockOutTitle = view.findViewById(R.id.tv_knockout_name);
        tvKnockOutTitle.setText(getString(knockOutViewStruct.getTitleResource()));
        knockOutViewStruct.tvTitle = tvKnockOutTitle;

        // Setup recycler view
        RecyclerView recyclerView = view.findViewById(R.id.rv_knockout);
        recyclerView.setAdapter(knockOutViewStruct.getAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void updateGroupViewStruct(GroupViewStruct groupViewStruct, Group group) {
        if (group != null) {
            groupViewStruct.set(group.getCountryList());
        }
    }

    private void updateKnockOutViewStruct(KnockOutViewStruct knockOutViewStruct, List<Match> matchList) {
        if (matchList != null) {
            knockOutViewStruct.set(matchList);
        }
    }

    /**
     * Utility method to group countries according to the group stage.
     *
     * @param countryList List of countries.
     *
     * @return HashMap of the countries grouped together according to group stage
     */
    private HashMap<SGroup, Group> setupGroups(List<Country> countryList) {
        // Set groups
        HashMap<SGroup, Group> groupsMap = new HashMap<>();
        for (Country c : countryList) {
            SGroup group = SGroup.get(c.getGroup());

            if (groupsMap.containsKey(group)) {
                groupsMap.get(group).add(c);
            } else {
                groupsMap.put(group, new Group(group == null? null : group.name));
                groupsMap.get(group).add(c);
            }
        }
        for (Group group : groupsMap.values())
            Collections.sort(group.getCountryList(), new Comparator<Country>() {
                @Override
                public int compare(Country lhs, Country rhs) {
                    return lhs.getPosition() - rhs.getPosition();
                }
            });

        return groupsMap;
    }

    /**
     * Utility method to group matches according to stage.
     *
     * @param matchList List of matches.
     *
     * @return HashMap of the matches grouped together according to stage
     */
    private HashMap<SStage, List<Match>> setupMatches(List<Match> matchList) {
        // Set groups
        HashMap<SStage, List<Match>> matchesMap = new HashMap<>();
        for (Match m : matchList) {
            SStage stage = SStage.get(m.getStage());

            if (matchesMap.containsKey(stage)) {
                matchesMap.get(stage).add(m);
            } else {
                matchesMap.put(stage, new ArrayList<Match>());
                matchesMap.get(stage).add(m);
            }
        }
        for (List<Match> matches : matchesMap.values())
            Collections.sort(matches, new Comparator<Match>() {
                @Override
                public int compare(Match lhs, Match rhs) {
                    return lhs.getMatchNumber() - rhs.getMatchNumber();
                }
            });

        return matchesMap;
    }

    private class GroupViewStruct {
        private final int mTitleResID;
        private final GroupListAdapter mGroupAdapter;
        private List<Country> mCountryList;

        GroupViewStruct(int titleResID) {
            mTitleResID = titleResID;
            mCountryList = new ArrayList<>();
            mGroupAdapter = new GroupListAdapter(mCountryList);
        }


        void set(List<Country> countryList) {
            mCountryList = countryList;
            mGroupAdapter.set(mCountryList);
        }

        RecyclerView.Adapter getAdapter() {
            return mGroupAdapter;
        }

        int getTitleResource() {
            return mTitleResID;
        }
    }

    private class KnockOutViewStruct {
        private final int mTitleResID;
        private final KnockoutListAdapter mKnockoutAdapter;
        private List<Match> mMatchList;
        private TextView tvTitle;

        KnockOutViewStruct(int titleResID) {
            mTitleResID = titleResID;
            mMatchList = new ArrayList<>();
            mKnockoutAdapter = new KnockoutListAdapter(mMatchList);
            mKnockoutAdapter.setOnKnockoutListAdapterListener(new KnockoutListAdapter.OnKnockoutListAdapterListener() {
                @Override
                public void onCountryClicked(Country country) {

                    startActivity(CountryDetailsActivity.makeIntent(getActivity(), country));

                }
            });
        }


        void set(List<Match> matchList) {
            mMatchList = matchList;
            mKnockoutAdapter.set(mMatchList);
            mKnockoutAdapter.notifyDataSetChanged();
        }

        RecyclerView.Adapter getAdapter() {
            return mKnockoutAdapter;
        }

        int getTitleResource() {
            return mTitleResID;
        }
    }
}