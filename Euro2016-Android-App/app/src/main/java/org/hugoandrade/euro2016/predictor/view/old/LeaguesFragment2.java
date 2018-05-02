package org.hugoandrade.euro2016.predictor.view.old;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.common.ServiceManagerOps;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.MatchPredictionActivity;
import org.hugoandrade.euro2016.predictor.view.UsersPredictionsActivity;
import org.hugoandrade.euro2016.predictor.view.fragment.FragmentBase;
import org.hugoandrade.euro2016.predictor.view.listadapter.UserListAdapter;

import java.util.List;

public class LeaguesFragment2 extends FragmentBase<FragComm.RequiredActivityOps>

        implements ServiceManagerOps {

    // Views
    private UserListAdapter mUserAdapter;
    private View tvLatestMatch;

    private ServiceManager mServiceManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mServiceManager = getParentActivity().getServiceManager();
        mServiceManager.subscribeServiceCallback(mServiceCallback);

        GlobalData.getInstance().addOnUsersChangedListener(mOnUsersChangedListener);
        GlobalData.getInstance().addOnLatestPerformanceChangedListener(mOnLatestPerformanceChangedListener);
        GlobalData.getInstance().addOnMatchesChangedListener(mOnMatchesChangedListener);

        return inflater.inflate(R.layout.fragment_leagues_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        TextView tvCreateLeague = view.findViewById(R.id.tv_create_league);
        tvCreateLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.showToast(getActivity(), "Create league");
            }
        });
        TextView tvJoinLeague = view.findViewById(R.id.tv_join_league);
        tvJoinLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.showToast(getActivity(), "Join league");
            }
        });


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
                getPredictionsOfSelectedUser(user);
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

    @Override
    public void notifyServiceIsBound() {
        if (getParentActivity() != null) {
            mServiceManager = getParentActivity().getServiceManager();
            mServiceManager.subscribeServiceCallback(mServiceCallback);
        }
    }

    private void getPredictionsOfSelectedUser(User user) {

        getParentActivity().disableUI();

        if (mServiceManager == null) {
            onPredictionsFetched(false, "Not bound to the service", user, null);
        }

        IMobileClientService service = mServiceManager.getService();

        if (service == null) {
            onPredictionsFetched(false, "Not bound to the service", user, null);
            return;
        }

        try {
            service.getPredictions(user);
        } catch (RemoteException e) {
            e.printStackTrace();
            onPredictionsFetched(false, "Error sending message", user, null);
        }

    }
    private void onPredictionsFetched(boolean operationResult, String message, User user, List<Prediction> predictionList) {

        startActivity(UsersPredictionsActivity.makeIntent(
                getActivity(), user, predictionList));

        getParentActivity().enableUI();
    }

    private ServiceManager.MobileServiceCallback mServiceCallback = new ServiceManager.MobileServiceCallback() {
        @Override
        public void sendResults(MobileClientData data) {
            int operationType = data.getOperationType();
            boolean isOperationSuccessful
                    = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

            if (operationType == MobileClientData.OperationType.GET_PREDICTIONS.ordinal()) {
                onPredictionsFetched(
                        isOperationSuccessful,
                        data.getErrorMessage(),
                        data.getUser(),
                        data.getPredictionList());
            }
        }
    };

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
