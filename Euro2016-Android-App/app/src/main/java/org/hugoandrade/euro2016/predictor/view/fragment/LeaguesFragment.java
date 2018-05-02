package org.hugoandrade.euro2016.predictor.view.fragment;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.FragComm;
import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.ServiceManager;
import org.hugoandrade.euro2016.predictor.common.ServiceManagerOps;
import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.WaitingLeagueUser;
import org.hugoandrade.euro2016.predictor.model.IMobileClientService;
import org.hugoandrade.euro2016.predictor.model.parser.MobileClientData;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.LeagueDetailsActivity;
import org.hugoandrade.euro2016.predictor.view.dialog.CreateLeagueDialog;
import org.hugoandrade.euro2016.predictor.view.dialog.JoinLeagueDialog;
import org.hugoandrade.euro2016.predictor.view.listadapter.LeagueListAdapter;

import java.util.Collections;

public class LeaguesFragment extends FragmentBase<FragComm.RequiredActivityOps>

        implements ServiceManagerOps {

    // Views
    private LeagueListAdapter mLeagueListAdapter;

    private ServiceManager mServiceManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getParentActivity().getServiceManager() != null) {
            mServiceManager = getParentActivity().getServiceManager();
            mServiceManager.subscribeServiceCallback(mServiceCallback);
        }

        GlobalData.getInstance().addOnUsersChangedListener(mOnUsersChangedListener);
        GlobalData.getInstance().addOnLatestPerformanceChangedListener(mOnLatestPerformanceChangedListener);
        GlobalData.getInstance().addOnLeaguesChangedListener(mOnLeaguesChangedListener);

        return inflater.inflate(R.layout.fragment_leagues, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        TextView tvCreateLeague = view.findViewById(R.id.tv_create_league);
        tvCreateLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLeague();
            }
        });
        TextView tvJoinLeague = view.findViewById(R.id.tv_join_league);
        tvJoinLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinLeague();
            }
        });

        mLeagueListAdapter = new LeagueListAdapter();
        mLeagueListAdapter.setOnItemClickListener(new LeagueListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LeagueWrapper leagueWrapper) {
                startActivity(LeagueDetailsActivity.makeIntent(getActivity(), leagueWrapper));
                //ViewUtils.showToast(getActivity(), "Full Standing of league " + leagueWrapper.getLeague().getName());
            }
        });
        mLeagueListAdapter.set(GlobalData.getInstance().getLeagues());

        RecyclerView rvLeagues = view.findViewById(R.id.rv_leagues);
        rvLeagues.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvLeagues.setNestedScrollingEnabled(false);
        rvLeagues.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        rvLeagues.setAdapter(mLeagueListAdapter);
    }

    private void createLeague() {
        CreateLeagueDialog dialog = new CreateLeagueDialog(getActivity());
        dialog.setOnCreateListener(new CreateLeagueDialog.OnCreateListener() {
            @Override
            public void onCreate(String leagueName) {
                generateLeague(leagueName);
            }
        });
        dialog.show();
    }

    private void joinLeague() {
        JoinLeagueDialog dialog = new JoinLeagueDialog(getActivity());
        dialog.setOnJoinListener(new JoinLeagueDialog.OnJoinListener() {
            @Override
            public void onJoin(String leagueCode) {
                joinLeague(leagueCode);
            }
        });
        dialog.show();
    }

    @Override
    public void notifyServiceIsBound() {
        if (getParentActivity() != null) {
            mServiceManager = getParentActivity().getServiceManager();
            mServiceManager.subscribeServiceCallback(mServiceCallback);
        }
    }

    private void generateLeague(String leagueName) {

        getParentActivity().disableUI();

        if (mServiceManager == null) {
            onLeagueCreated(false, "No Network Connection", null);
        }

        IMobileClientService service = mServiceManager.getService();

        if (service == null) {
            onLeagueCreated(false, "Not bound to the service", null);
            return;
        }

        try {
            service.createLeague(GlobalData.getInstance().user.getID(), leagueName);
        } catch (RemoteException e) {
            e.printStackTrace();
            onLeagueCreated(false, "Error sending message", null);
        }
    }

    private void joinLeague(String leagueCode) {

        getParentActivity().disableUI();

        if (mServiceManager == null) {
            onLeagueJoined(false, "No Network Connection", null);
        }

        IMobileClientService service = mServiceManager.getService();

        if (service == null) {
            onLeagueJoined(false, "Not bound to the service", null);
            return;
        }

        try {
            service.joinLeague(new WaitingLeagueUser(GlobalData.getInstance().user.getID(), leagueCode));
        } catch (RemoteException e) {
            e.printStackTrace();
            onLeagueJoined(false, "Error sending message", null);
        }/**/
    }

    public void onLeagueCreated(boolean isOperationSuccessful, String errorMessage, League league) {
        if (isOperationSuccessful) {

            LeagueWrapper leagueWrapper = new LeagueWrapper(league, Collections.singletonList(GlobalData.getInstance().user));

            GlobalData.getInstance().addLeague(leagueWrapper);

        } else {

            getParentActivity().showSnackBar(errorMessage);
        }

        getParentActivity().enableUI();
    }

    public void onLeagueJoined(boolean isOperationSuccessful, String errorMessage, LeagueWrapper leagueWrapper) {
        if (isOperationSuccessful) {
            Log.e(TAG, "onLeagueJoined(finally)::" + leagueWrapper.toString());

            GlobalData.getInstance().addLeague(leagueWrapper);

        } else {

            getParentActivity().showSnackBar(errorMessage);
        }

        getParentActivity().enableUI();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        GlobalData.getInstance().removeOnUsersChangedListener(mOnUsersChangedListener);
        GlobalData.getInstance().removeOnLatestPerformanceChangedListener(mOnLatestPerformanceChangedListener);
        GlobalData.getInstance().removeOnLeaguesChangedListener(mOnLeaguesChangedListener);
    }

    private GlobalData.OnUsersChangedListener mOnUsersChangedListener
            = new GlobalData.OnUsersChangedListener() {

        @Override
        public void onUsersChanged() {
            /*if (mLeagueListAdapter != null) {
                mLeagueListAdapter.set(GlobalData.getInstance().getUserList());
            }/**/
        }
    };

    private GlobalData.OnLatestPerformanceChangedListener mOnLatestPerformanceChangedListener
            = new GlobalData.OnLatestPerformanceChangedListener() {

        @Override
        public void onLatestPerformanceChanged() {
            if (mLeagueListAdapter != null) {
                mLeagueListAdapter.notifyDataSetChanged();
            }
        }
    };

    private GlobalData.OnLeaguesChangedListener mOnLeaguesChangedListener
            = new GlobalData.OnLeaguesChangedListener() {
        @Override
        public void onLeaguesChanged() {
            if (mLeagueListAdapter != null) {
                mLeagueListAdapter.set(GlobalData.getInstance().getLeagues());
                mLeagueListAdapter.notifyDataSetChanged();
            }
        }
    };

    private ServiceManager.MobileServiceCallback mServiceCallback = new ServiceManager.MobileServiceCallback() {
        @Override
        public void sendResults(MobileClientData data) {
            int operationType = data.getOperationType();
            boolean isOperationSuccessful
                    = data.getOperationResult() == MobileClientData.REQUEST_RESULT_SUCCESS;

            if (operationType == MobileClientData.OperationType.CREATE_LEAGUE.ordinal()) {
                onLeagueCreated(
                        isOperationSuccessful,
                        data.getErrorMessage(),
                        data.getLeague());
            }
            else if (operationType == MobileClientData.OperationType.JOIN_LEAGUE.ordinal()) {
                onLeagueJoined(
                        isOperationSuccessful,
                        data.getErrorMessage(),
                        data.getLeagueWrapper());
            }
        }
    };

}
