package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.presenter.LeagueDetailsPresenter;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;
import org.hugoandrade.euro2016.predictor.utils.StickyFooterUtils;
import org.hugoandrade.euro2016.predictor.view.dialog.SimpleDialog;
import org.hugoandrade.euro2016.predictor.view.listadapter.LeagueStandingFullListAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeagueDetailsActivity extends MainActivityBase<MVP.RequiredLeagueDetailsViewOps,
                                                            MVP.ProvidedLeagueDetailsPresenterOps,
                                                            LeagueDetailsPresenter>

        implements MVP.RequiredLeagueDetailsViewOps {

    @SuppressWarnings("unused")
    private final String TAG = LeagueDetailsActivity.class.getSimpleName();

    private static final String INTENT_EXTRA_LEAGUE = "intent_extra_league";

    private LeagueWrapper mLeagueWrapper;

    private NestedScrollView mScrollViewContainer;
    private LeagueStandingFullListAdapter leagueStandingListAdapter;

    private TextView tvLeaveLeague;
    private TextView tvLeaveLeagueFixed;
    private TextView tvDeleteLeague;
    private TextView tvDeleteLeagueFixed;

    private View progressBar;

    public static Intent makeIntent(Context context, LeagueWrapper leagueWrapper) {

        return new Intent(context, LeagueDetailsActivity.class)
                .putExtra(INTENT_EXTRA_LEAGUE, leagueWrapper);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getExtras() != null) {

            mLeagueWrapper = getIntent().getParcelableExtra(INTENT_EXTRA_LEAGUE);

            initializeUI();
        }
        else {
            finish();
        }

        super.onCreate(LeagueDetailsPresenter.class, this);
    }

    private void initializeUI() {

        setContentView(R.layout.activity_league_details);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        boolean isOverall = mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isOverall? getString(R.string.app_name) : mLeagueWrapper.getLeague().getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int currentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                GlobalData.getInstance().getMatchList(),
                GlobalData.getInstance().getServerTime().getTime());

        mScrollViewContainer = findViewById(R.id.nestedScrollView);

        View tvLatestMatch = findViewById(R.id.tv_latest_match);

        TextView tvLeagueName = findViewById(R.id.tv_league_name);
        TextView tvLeagueMembers = findViewById(R.id.tv_league_members);
        TextView tvLeagueCode = findViewById(R.id.tv_league_code);
        TextView tvLeagueCodeHeader = findViewById(R.id.tv_league_code_header);

        tvLeagueName.setText(isOverall? getString(R.string.app_name) : mLeagueWrapper.getLeague().getName());
        tvLeagueCode.setText(mLeagueWrapper.getLeague().getCode());
        tvLeagueCodeHeader.setVisibility(isOverall? View.GONE : View.VISIBLE);
        tvLeagueCode.setVisibility(isOverall? View.GONE : View.VISIBLE);
        tvLeagueMembers.setText(TextUtils.concat("(",
                String.valueOf(mLeagueWrapper.getLeague().getNumberOfMembers()),
                " ",
                getString(R.string.members),
                ")"));


        leagueStandingListAdapter = new LeagueStandingFullListAdapter(mLeagueWrapper);
        leagueStandingListAdapter.setOnLeagueStandingClicked(new LeagueStandingFullListAdapter.OnLeagueStandingClicked() {
            @Override
            public void onUserSelected(User user) {
                getPresenter().fetchRemainingPredictions(user);
            }

            @Override
            public void onMoreClicked() {
                getPresenter().fetchMoreUsers(mLeagueWrapper.getLeague().getID(), mLeagueWrapper.getLeagueUserList().size());

            }
        });

        RecyclerView rvLeagueStandings = findViewById(R.id.rv_league_standings);
        rvLeagueStandings.setNestedScrollingEnabled(false);
        rvLeagueStandings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvLeagueStandings.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        rvLeagueStandings.setAdapter(leagueStandingListAdapter);

        tvLeaveLeague = findViewById(R.id.tv_leave_league);
        tvLeaveLeagueFixed = findViewById(R.id.tv_leave_league_fixed);
        tvDeleteLeague = findViewById(R.id.tv_delete_league);
        tvDeleteLeagueFixed = findViewById(R.id.tv_delete_league_fixed);

        progressBar = findViewById(R.id.progressBar_waiting);

        if (isOverall) {
            tvLatestMatch.setVisibility(View.GONE);
        }
        else {
            tvLatestMatch.setVisibility(currentMatchNumber <= 1 ? View.GONE : View.VISIBLE);
            tvLatestMatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(MatchPredictionActivity.makeIntent(
                            LeagueDetailsActivity.this,
                            mLeagueWrapper.getLeagueUserList(),
                            mLeagueWrapper.getLeague().getName()));
                }
            });
        }

        setupFooter();
    }

    @Override
    public void startUserPredictionsActivity(User user, List<Prediction> predictionList) {
        if (!isPaused()) {
            startActivity(UsersPredictionsActivity.makeIntent(this, user, predictionList));
        }
    }

    private void setupFooter() {

        boolean isAdmin = GlobalData.getInstance().user.getID().equals(mLeagueWrapper.getLeague().getAdminID());
        boolean isOverall = mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID);

        if (isOverall) {
            tvLeaveLeague.setVisibility(View.GONE);
            tvLeaveLeagueFixed.setVisibility(View.GONE);
            tvDeleteLeague.setVisibility(View.GONE);
            tvDeleteLeagueFixed.setVisibility(View.GONE);
        }
        else {

            if (isAdmin) {
                View.OnClickListener mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteLeague();
                    }
                };
                StickyFooterUtils.initialize(mScrollViewContainer, tvDeleteLeague, tvDeleteLeagueFixed);
                tvDeleteLeague.setOnClickListener(mOnClickListener);
                tvDeleteLeagueFixed.setOnClickListener(mOnClickListener);
                tvLeaveLeague.setVisibility(View.GONE);
                tvLeaveLeagueFixed.setVisibility(View.GONE);
            }
            else {
                View.OnClickListener mOnClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveLeague();
                    }
                };
                StickyFooterUtils.initialize(mScrollViewContainer, tvLeaveLeague, tvLeaveLeagueFixed);
                tvLeaveLeague.setOnClickListener(mOnClickListener);
                tvLeaveLeagueFixed.setOnClickListener(mOnClickListener);
                tvDeleteLeague.setVisibility(View.GONE);
                tvDeleteLeagueFixed.setVisibility(View.GONE);
            }
        }
    }

    private void deleteLeague() {
        String title = getString(R.string.delete_league);
        String message = getString(R.string.delete_league_details);
        SimpleDialog mDeleteLeagueDialog = new SimpleDialog(getActivityContext(), title, message);
        mDeleteLeagueDialog.setOnDialogResultListener(new SimpleDialog.OnDialogResult() {
            @Override
            public void onResult(DialogInterface dialog, @SimpleDialog.Result int result) {
                if (result == SimpleDialog.YES) {
                    getPresenter().deleteLeague(
                            GlobalData.getInstance().user.getID(),
                            mLeagueWrapper.getLeague().getID());
                } else if (result == SimpleDialog.BACK) {
                    dialog.dismiss();
                }
            }
        });
        mDeleteLeagueDialog.show();
    }

    private void leaveLeague() {
        String title = getString(R.string.leave_league);
        String message = getString(R.string.leave_league_details);
        SimpleDialog mLeaveLeagueDialog = new SimpleDialog(getActivityContext(), title, message);
        mLeaveLeagueDialog.setOnDialogResultListener(new SimpleDialog.OnDialogResult() {
            @Override
            public void onResult(DialogInterface dialog, @SimpleDialog.Result int result) {
                if (result == SimpleDialog.YES) {
                    getPresenter().leaveLeague(
                            GlobalData.getInstance().user.getID(),
                            mLeagueWrapper.getLeague().getID());
                } else if (result == SimpleDialog.BACK) {
                    dialog.dismiss();
                }
            }
        });
        mLeaveLeagueDialog.show();
    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void reportMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void leagueLeft() {
        GlobalData.getInstance().removeLeague(mLeagueWrapper);
        finish();
    }

    @Override
    public void updateListOfUsers(List<LeagueUser> userList) {
        if (leagueStandingListAdapter != null) {
            for (LeagueUser newUser : userList) {

                boolean isUserOnList = false;
                for (LeagueUser user : mLeagueWrapper.getLeagueUserList()) {
                    if (user.getUser().getID().equals(newUser.getUser().getID())) {
                        isUserOnList = true;
                        break;
                    }
                }

                if (!isUserOnList) {
                    mLeagueWrapper.getLeagueUserList().add(newUser);
                }

            }

            Collections.sort(mLeagueWrapper.getLeagueUserList(), new Comparator<LeagueUser>() {
                @Override
                public int compare(LeagueUser o1, LeagueUser o2) {
                    return o1.getRank() - o2.getRank();
                }
            });

            leagueStandingListAdapter.updateMoreButton();
            leagueStandingListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void logout() {
        getPresenter().logout();

        super.logout();
    }
}
