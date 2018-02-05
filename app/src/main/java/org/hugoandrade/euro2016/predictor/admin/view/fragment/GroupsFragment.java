package org.hugoandrade.euro2016.predictor.admin.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.admin.FragmentCommunication;
import org.hugoandrade.euro2016.predictor.admin.R;
import org.hugoandrade.euro2016.predictor.admin.object.Country;
import org.hugoandrade.euro2016.predictor.admin.object.Group;
import org.hugoandrade.euro2016.predictor.admin.view.listadapter.GroupListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsFragment extends FragmentBase<FragmentCommunication.ProvidedParentActivityOps>

        implements FragmentCommunication.ProvidedGroupsChildFragmentOps {

    private HashMap<String, ViewStruct> mViewStructMap = buildViewStructMap();

    private HashMap<String, ViewStruct> buildViewStructMap() {
        HashMap<String, ViewStruct> viewStructMap = new HashMap<>();
        viewStructMap.put("A", new ViewStruct(R.string.group_a));
        viewStructMap.put("B", new ViewStruct(R.string.group_b));
        viewStructMap.put("C", new ViewStruct(R.string.group_c));
        viewStructMap.put("D", new ViewStruct(R.string.group_d));
        viewStructMap.put("E", new ViewStruct(R.string.group_e));
        viewStructMap.put("F", new ViewStruct(R.string.group_f));
        return viewStructMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        initializeUI(view);
    }

    private void initializeUI(View view) {
        setupGroupLayout(view.findViewById(R.id.layout_group_a), mViewStructMap.get("A"));
        setupGroupLayout(view.findViewById(R.id.layout_group_b), mViewStructMap.get("B"));
        setupGroupLayout(view.findViewById(R.id.layout_group_c), mViewStructMap.get("C"));
        setupGroupLayout(view.findViewById(R.id.layout_group_d), mViewStructMap.get("D"));
        setupGroupLayout(view.findViewById(R.id.layout_group_e), mViewStructMap.get("E"));
        setupGroupLayout(view.findViewById(R.id.layout_group_f), mViewStructMap.get("F"));
    }

    @Override
    public void setGroups(HashMap<String, Group> allGroups) {
        updateViewStruct(mViewStructMap.get("A"), allGroups.get("A").getCountryList());
        updateViewStruct(mViewStructMap.get("B"), allGroups.get("B").getCountryList());
        updateViewStruct(mViewStructMap.get("C"), allGroups.get("C").getCountryList());
        updateViewStruct(mViewStructMap.get("D"), allGroups.get("D").getCountryList());
        updateViewStruct(mViewStructMap.get("E"), allGroups.get("E").getCountryList());
        updateViewStruct(mViewStructMap.get("F"), allGroups.get("F").getCountryList());
    }

    private void setupGroupLayout(View view, ViewStruct viewStruct) {
        // Setup title
        TextView tvGroupTitle = (TextView) view.findViewById(R.id.tv_group);
        tvGroupTitle.setText(getString(viewStruct.getTitleResource()));

        // Setup recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_group);
        recyclerView.setAdapter(viewStruct.getGroupAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void updateViewStruct(ViewStruct viewStruct, List<Country> countryList) {
        viewStruct.setAll(countryList);
    }

    private class ViewStruct {
        private final int mTitleResID;
        private final GroupListAdapter mGroupAdapter;
        private List<Country> mCountryList;

        ViewStruct(int titleResID) {
            mTitleResID = titleResID;
            mCountryList = new ArrayList<>();
            mGroupAdapter = new GroupListAdapter(mCountryList);
        }


        void setAll(List<Country> countryList) {
            mCountryList = countryList;
            mGroupAdapter.setAll(mCountryList);
        }

        RecyclerView.Adapter getGroupAdapter() {
            return mGroupAdapter;
        }

        int getTitleResource() {
            return mTitleResID;
        }
    }
}
