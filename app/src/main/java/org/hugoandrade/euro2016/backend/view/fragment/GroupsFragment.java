package org.hugoandrade.euro2016.backend.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.backend.FragmentCommunication;
import org.hugoandrade.euro2016.backend.R;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.view.listadapter.GroupListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupsFragment
        extends FragmentBase<FragmentCommunication.ProvidedParentActivityOps>
        implements FragmentCommunication.ProvidedGroupsChildFragmentOps {

    @SuppressWarnings("unused")
    private static final String TAG = GroupsFragment.class.getSimpleName();

    private HashMap<String, ViewStruct> mViewStructMap = new HashMap<String, ViewStruct>() {{
        put("A", new ViewStruct(R.string.group_a));
        put("B", new ViewStruct(R.string.group_b));
        put("C", new ViewStruct(R.string.group_c));
        put("D", new ViewStruct(R.string.group_d));
        put("E", new ViewStruct(R.string.group_e));
        put("F", new ViewStruct(R.string.group_f));
    }};

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

        View editSystemData = view.findViewById(R.id.tv_edit_system_data);
        editSystemData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupEditSystemDataDialog();
            }
        });
    }

    @Override
    public void setGroups(HashMap<String, List<Country>> allGroups) {
        updateViewStruct(mViewStructMap.get("A"), allGroups.get("A"));
        updateViewStruct(mViewStructMap.get("B"), allGroups.get("B"));
        updateViewStruct(mViewStructMap.get("C"), allGroups.get("C"));
        updateViewStruct(mViewStructMap.get("D"), allGroups.get("D"));
        updateViewStruct(mViewStructMap.get("E"), allGroups.get("E"));
        updateViewStruct(mViewStructMap.get("F"), allGroups.get("F"));
    }

    private void setupGroupLayout(View view, ViewStruct viewStruct) {
        // Setup title
        TextView tvGroupTitle = (TextView) view.findViewById(R.id.tv_group);
        tvGroupTitle.setText(getString(viewStruct.getTitleResource()));

        // Setup recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_group);
        recyclerView.setAdapter(viewStruct.getGroupAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void updateViewStruct(ViewStruct viewStruct, List<Country> countryList) {
        viewStruct.setAll(countryList);
    }

    private void popupEditSystemDataDialog() {
        getParentActivity().popupEditSystemDataDialog();
    }

    private class ViewStruct {
        private final int mTitleResID;
        private final GroupListAdapter mGroupAdapter;
        private final List<Country> mCountryList;

        ViewStruct(int titleResID) {
            mTitleResID = titleResID;
            mCountryList = new ArrayList<>();
            mGroupAdapter = new GroupListAdapter(mCountryList);
        }


        void setAll(List<Country> countryList) {
            mCountryList.clear();
            mCountryList.addAll(countryList);
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
