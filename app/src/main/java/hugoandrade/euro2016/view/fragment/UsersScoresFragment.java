package hugoandrade.euro2016.view.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016.FragmentCommunication;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.common.CustomRecyclerScroll;
import hugoandrade.euro2016.common.GenericFragment;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.view.listadapter.UserListAdapter;

public class UsersScoresFragment
        extends GenericFragment<FragmentCommunication.RequiredActivityOps>
        implements FragmentCommunication.ProvidedAllUsersFragmentOps {

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private UserListAdapter mAllUsersAdapter;

    // Data
    private final ArrayList<User> mAllUsersList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_all_users, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_fragment_users);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getParentActivity().refreshAllData();
            }
        });
        RecyclerView lvAllUsers = (RecyclerView) view.findViewById(R.id.rv_all_users);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            lvAllUsers.addOnScrollListener(new CustomRecyclerScroll() {
                @Override
                public void show() {
                    getParentActivity().showFab();
                }

                @Override
                public void hide() {
                    getParentActivity().hideFab();
                }
            });
        lvAllUsers.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        lvAllUsers.setOverScrollMode(ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS);

        mAllUsersAdapter = new UserListAdapter(mAllUsersList);
        lvAllUsers.setAdapter(mAllUsersAdapter);

        ((TextView) view.findViewById(R.id.tv_rule_correct_prediction)).setText(
                "* Correct prediction: " + GlobalData.systemData.ruleCorrectPrediction + " point" +
                        (GlobalData.systemData.ruleCorrectPrediction != 1? "s" : "") +
                        ".");
        ((TextView) view.findViewById(R.id.tv_rule_correct_outcome)).setText(
                "* Correct outcome: " + GlobalData.systemData.ruleCorrectOutcome + " point" +
                        (GlobalData.systemData.ruleCorrectOutcome != 1? "s" : "") +
                        ".");
        ((TextView) view.findViewById(R.id.tv_rule_correct_outcome_via_penalties)).setText(
                "* Correct outcome via penalty shootout: " + GlobalData.systemData.ruleCorrectOutcomeViaPenalties + " point" +
                        (GlobalData.systemData.ruleCorrectOutcomeViaPenalties != 1? "s" : "") +
                        ".");
        ((TextView) view.findViewById(R.id.tv_rule_incorrect_prediction)).setText(
                "* Incorrect prediction: " + GlobalData.systemData.ruleIncorrectPrediction + " point" +
                        (GlobalData.systemData.ruleIncorrectPrediction != 1? "s" : "") +
                        ".");

        mAllUsersAdapter.setOnItemClickListener(new UserListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                getParentActivity().onUserSelected(user);
            }
        });
    }
    /**
     * Display the List of Users in the appropriate View
     */
    @Override
    public void setAllUsers(ArrayList<User> allUsersList) {
        mAllUsersList.clear();
        mAllUsersList.addAll(allUsersList);
        if (mAllUsersAdapter != null)
            mAllUsersAdapter.setAllUsers(mAllUsersList);
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }
}
