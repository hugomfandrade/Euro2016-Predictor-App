package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Country;
import org.hugoandrade.euro2016.predictor.data.Group;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SGroup;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils.SStage;
import org.hugoandrade.euro2016.predictor.view.listadapter.GroupListAdapter;
import org.hugoandrade.euro2016.predictor.view.listadapter.KnockoutListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StandingsFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements FragComm.ProvidedCountriesFragmentOps,
                   FragComm.ProvidedMatchesFragmentOps {

    // Views
    private HashMap<SGroup, GroupViewStruct> mGroupViewStructMap = buildGroupViewStructMap();
    private HashMap<SStage, KnockOutViewStruct> mKnockOutViewStructMap = buildKnockOutViewStructMap();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_standings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        initializeUI(view);
    }

    private void initializeUI(View view) {

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
    }

    /**
     * Display the List of Countries in the appropriate View. Split them
     * by Group, sort them by position
     */
    @Override
    public void setGroups(HashMap<SGroup, Group> groupsMap) {
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.A), groupsMap.get(SGroup.A).getCountryList());
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.B), groupsMap.get(SGroup.B).getCountryList());
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.C), groupsMap.get(SGroup.C).getCountryList());
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.D), groupsMap.get(SGroup.D).getCountryList());
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.E), groupsMap.get(SGroup.E).getCountryList());
        updateGroupViewStruct(mGroupViewStructMap.get(SGroup.F), groupsMap.get(SGroup.F).getCountryList());
    }

    /**
     * Display the List of Matches in the appropriate View
     */
    @Override
    public void setMatches(HashMap<SStage, List<Match>> matchMap) {
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.roundOf16), matchMap.get(SStage.roundOf16));
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.quarterFinals), matchMap.get(SStage.quarterFinals));
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.semiFinals), matchMap.get(SStage.semiFinals));
        updateKnockOutViewStruct(mKnockOutViewStructMap.get(SStage.finals), matchMap.get(SStage.finals));
    }

    private void setupGroupLayout(View view, GroupViewStruct groupViewStruct) {
        // Setup title
        TextView tvGroupTitle = (TextView) view.findViewById(R.id.tv_group);
        tvGroupTitle.setText(getString(groupViewStruct.getTitleResource()));

        // Setup recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_group);
        recyclerView.setAdapter(groupViewStruct.getAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void setupKnockOutLayout(View view, KnockOutViewStruct knockOutViewStruct) {
        // Setup title
        TextView tvKnockOutTitle = (TextView) view.findViewById(R.id.tv_knockout_name);
        tvKnockOutTitle.setText(getString(knockOutViewStruct.getTitleResource()));

        // Setup recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_knockout);
        recyclerView.setAdapter(knockOutViewStruct.getAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void updateGroupViewStruct(GroupViewStruct groupViewStruct, List<Country> countryList) {
        groupViewStruct.set(countryList);
    }

    private void updateKnockOutViewStruct(KnockOutViewStruct knockOutViewStruct, List<Match> matchList) {
        knockOutViewStruct.set(matchList);
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

        KnockOutViewStruct(int titleResID) {
            mTitleResID = titleResID;
            mMatchList = new ArrayList<>();
            mKnockoutAdapter = new KnockoutListAdapter(mMatchList);
        }


        void set(List<Match> matchList) {
            mMatchList = matchList;
            mKnockoutAdapter.set(mMatchList);
        }

        RecyclerView.Adapter getAdapter() {
            return mKnockoutAdapter;
        }

        int getTitleResource() {
            return mTitleResID;
        }
    }
}