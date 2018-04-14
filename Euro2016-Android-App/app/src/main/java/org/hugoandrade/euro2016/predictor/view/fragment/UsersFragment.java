package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.view.listadapter.UserListAdapter;

public class UsersFragment extends FragmentBase<FragComm.RequiredActivityOps> {

    // Views
    private UserListAdapter mUserAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GlobalData.getInstance().addOnUsersChangedListener(mOnUsersChangedListener);
        GlobalData.getInstance().addOnLatestPerformanceChangedListener(mOnLatestPerformanceChangedListener);

        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mUserAdapter = new UserListAdapter(GlobalData.getInstance().getUserList());
        mUserAdapter.setOnItemClickListener(new UserListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                getParentActivity().onUserSelected(user);
            }
        });

        RecyclerView rvUsers = view.findViewById(R.id.rv_all_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvUsers.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        rvUsers.setAdapter(mUserAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        GlobalData.getInstance().removeOnUsersChangedListener(mOnUsersChangedListener);
        GlobalData.getInstance().removeOnLatestPerformanceChangedListener(mOnLatestPerformanceChangedListener);
    }

    private GlobalData.OnUsersChangedListener mOnUsersChangedListener
            = new GlobalData.OnUsersChangedListener() {

        @Override
        public void onUsersChanged() {
            if (mUserAdapter != null) {
                mUserAdapter.set(GlobalData.getInstance().getUserList());
            }
        }
    };

    private GlobalData.OnLatestPerformanceChangedListener mOnLatestPerformanceChangedListener
            = new GlobalData.OnLatestPerformanceChangedListener() {

        @Override
        public void onLatestPerformanceChanged() {
            if (mUserAdapter != null) {
                mUserAdapter.notifyDataSetChanged();
            }
        }
    };
}
