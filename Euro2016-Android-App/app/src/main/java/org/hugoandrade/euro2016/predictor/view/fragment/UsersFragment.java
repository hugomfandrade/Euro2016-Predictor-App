package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.MatchPredictionActivity;
import org.hugoandrade.euro2016.predictor.view.listadapter.UserListAdapter;

public class UsersFragment extends FragmentBase<FragComm.RequiredActivityOps> {

    // Views
    private UserListAdapter mUserAdapter;
    private View tvLatestMatch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GlobalData.getInstance().addOnUsersChangedListener(mOnUsersChangedListener);
        GlobalData.getInstance().addOnLatestPerformanceChangedListener(mOnLatestPerformanceChangedListener);
        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);

        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        int currentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                GlobalData.getInstance().getMatchList(),
                GlobalData.getInstance().getServerTime().getTime());

        tvLatestMatch = view.findViewById(R.id.tv_latest_match);
        tvLatestMatch.setVisibility(currentMatchNumber <= 1 ? View.GONE : View.VISIBLE);
        tvLatestMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(MatchPredictionActivity.makeIntent(getActivity(), GlobalData.getInstance().getUserList()));

                //ViewUtils.showToast(getActivity(), "View latest match");
            }
        });

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
        GlobalData.getInstance().removeOnMatchesChangedListener(mOnMatchesChangedListener);
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

    private GlobalData.OnMatchesChangedListener mOnMatchesChangedListener
            = new GlobalData.OnMatchesChangedListener() {
        @Override
        public void onMatchesChanged() {

            int currentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                    GlobalData.getInstance().getMatchList(),
                    GlobalData.getInstance().getServerTime().getTime());

            if (tvLatestMatch != null) {
                tvLatestMatch.setVisibility(currentMatchNumber <= 1 ?  View.GONE : View.VISIBLE);
            }
        }
    };
}
