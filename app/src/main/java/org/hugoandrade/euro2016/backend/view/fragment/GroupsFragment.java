package org.hugoandrade.euro2016.backend.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import org.hugoandrade.euro2016.backend.FragmentCommunication;
import org.hugoandrade.euro2016.backend.R;
import org.hugoandrade.euro2016.backend.object.Country;
import org.hugoandrade.euro2016.backend.utils.Utility;
import org.hugoandrade.euro2016.backend.view.EditSystemDataDialog;
import org.hugoandrade.euro2016.backend.view.MainActivity;
import org.hugoandrade.euro2016.backend.view.listadapter.GroupListAdapter;

public class GroupsFragment
        extends Fragment
        implements FragmentCommunication.ProvidedGroupsChildFragmentOps {

    @SuppressWarnings("unused")
    private static final String TAG = GroupsFragment.class.getSimpleName();

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private MainActivity mCommChListener;

    public GroupListAdapter mAdapterA, mAdapterB, mAdapterC, mAdapterD, mAdapterE, mAdapterF;
    public ListView listViewA, listViewB, listViewC, listViewD, listViewE, listViewF;
    private ArrayList<Country>
            mGroupAList = new ArrayList<>(),
            mGroupBList = new ArrayList<>(),
            mGroupCList = new ArrayList<>(),
            mGroupDList = new ArrayList<>(),
            mGroupEList = new ArrayList<>(),
            mGroupFList = new ArrayList<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommChListener = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        listViewA = (ListView) view.findViewById(R.id.listView_groupA);
        listViewB = (ListView) view.findViewById(R.id.listView_groupB);
        listViewC = (ListView) view.findViewById(R.id.listView_groupC);
        listViewD = (ListView) view.findViewById(R.id.listView_groupD);
        listViewE = (ListView) view.findViewById(R.id.listView_groupE);
        listViewF = (ListView) view.findViewById(R.id.listView_groupF);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_update_matches_1);
        fab.setVisibility(View.INVISIBLE);

        view.findViewById(R.id.tv_edit_system_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popUpEditSystemDataDialog();
            }
        });

        mAdapterA = new GroupListAdapter(getActivity(), R.layout.list_item_group, mGroupAList);
        mAdapterB = new GroupListAdapter(getActivity(), R.layout.list_item_group, mGroupBList);
        mAdapterC = new GroupListAdapter(getActivity(), R.layout.list_item_group, mGroupCList);
        mAdapterD = new GroupListAdapter(getActivity(), R.layout.list_item_group, mGroupDList);
        mAdapterE = new GroupListAdapter(getActivity(), R.layout.list_item_group, mGroupEList);
        mAdapterF = new GroupListAdapter(getActivity(), R.layout.list_item_group, mGroupFList);

        listViewA.setAdapter(mAdapterA);
        listViewB.setAdapter(mAdapterB);
        listViewC.setAdapter(mAdapterC);
        listViewD.setAdapter(mAdapterD);
        listViewE.setAdapter(mAdapterE);
        listViewF.setAdapter(mAdapterF);

        updateListViewHeight();
    }

    private void popUpEditSystemDataDialog() {
        startActivityForResult(EditSystemDataDialog.makeIntent(getActivity()), 0);
    }

    @Override
    public void setGroups(HashMap<String, ArrayList<Country>> allGroups) {
        mGroupAList.clear();
        mGroupAList.addAll(allGroups.get("A"));
        mGroupBList.clear();
        mGroupBList.addAll(allGroups.get("B"));
        mGroupCList.clear();
        mGroupCList.addAll(allGroups.get("C"));
        mGroupDList.clear();
        mGroupDList.addAll(allGroups.get("D"));
        mGroupEList.clear();
        mGroupEList.addAll(allGroups.get("E"));
        mGroupFList.clear();
        mGroupFList.addAll(allGroups.get("F"));

        if (mAdapterA != null) mAdapterA.setAll(mGroupAList);
        if (mAdapterB != null) mAdapterB.setAll(mGroupBList);
        if (mAdapterC != null) mAdapterC.setAll(mGroupCList);
        if (mAdapterD != null) mAdapterD.setAll(mGroupDList);
        if (mAdapterE != null) mAdapterE.setAll(mGroupEList);
        if (mAdapterF != null) mAdapterF.setAll(mGroupFList);

        updateListViewHeight();
    }

    public void updateListViewHeight() {
        Utility.setListViewHeightBasedOnChildren(listViewA);
        Utility.setListViewHeightBasedOnChildren(listViewB);
        Utility.setListViewHeightBasedOnChildren(listViewC);
        Utility.setListViewHeightBasedOnChildren(listViewD);
        Utility.setListViewHeightBasedOnChildren(listViewE);
        Utility.setListViewHeightBasedOnChildren(listViewF);
    }
}
