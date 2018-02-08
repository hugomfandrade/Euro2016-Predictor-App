package org.hugoandrade.euro2016.predictor.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.view.listadapter.UserListAdapter;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements FragComm.ProvidedUsersFragmentOps {

    // Views
    private UserListAdapter mUserAdapter;

    // Data
    private List<User> mUserList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RecyclerView lvAllUsers = (RecyclerView) view.findViewById(R.id.rv_all_users);
        lvAllUsers.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        lvAllUsers.setOverScrollMode(ViewCompat.OVER_SCROLL_IF_CONTENT_SCROLLS);

        mUserAdapter = new UserListAdapter(mUserList);
        mUserAdapter.setOnItemClickListener(new UserListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                getParentActivity().onUserSelected(user);
            }
        });
        lvAllUsers.setAdapter(mUserAdapter);

        ((TextView) view.findViewById(R.id.tv_rule_correct_prediction)).setText(
                "* Correct prediction: " + GlobalData.systemData.getRules().getRuleCorrectPrediction() + " point" +
                        (GlobalData.systemData.getRules().getRuleCorrectPrediction() != 1? "s" : "") +
                        ".");
        ((TextView) view.findViewById(R.id.tv_rule_correct_outcome)).setText(
                "* Correct outcome: " + GlobalData.systemData.getRules().getRuleCorrectOutcome() + " point" +
                        (GlobalData.systemData.getRules().getRuleCorrectOutcome() != 1? "s" : "") +
                        ".");
        ((TextView) view.findViewById(R.id.tv_rule_correct_outcome_via_penalties)).setText(
                "* Correct outcome via penalty shootout: " + GlobalData.systemData.getRules().getRuleCorrectOutcomeViaPenalties() + " point" +
                        (GlobalData.systemData.getRules().getRuleCorrectOutcomeViaPenalties() != 1? "s" : "") +
                        ".");
        ((TextView) view.findViewById(R.id.tv_rule_incorrect_prediction)).setText(
                "* Incorrect prediction: " + GlobalData.systemData.getRules().getRuleIncorrectPrediction() + " point" +
                        (GlobalData.systemData.getRules().getRuleIncorrectPrediction() != 1? "s" : "") +
                        ".");

    }
    /**
     * Display the List of Users in the appropriate View
     */
    @Override
    public void setUsers(List<User> userList) {
        mUserList = userList;
        if (mUserAdapter != null)
            mUserAdapter.set(userList);
    }
}
