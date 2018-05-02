package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.presenter.LeagueDetailsPresenter;
import org.hugoandrade.euro2016.predictor.utils.LeagueUtils;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StickyFooterUtils;
import org.hugoandrade.euro2016.predictor.view.listadapter.LeagueStandingFullListAdapter;
import org.hugoandrade.euro2016.predictor.view.listadapter.LeagueStandingListAdapter;

import java.util.ArrayList;
import java.util.List;

public class LeagueDetailsActivity extends ActivityBase<MVP.RequiredLeagueDetailsViewOps,
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeUI() {

        setContentView(R.layout.activity_league_details);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("League");
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

        tvLeagueName.setText(mLeagueWrapper.getLeague().getName());
        tvLeagueCode.setText(mLeagueWrapper.getLeague().getCode());
        tvLeagueCodeHeader.setVisibility(mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID)? View.GONE : View.VISIBLE);
        tvLeagueCode.setVisibility(mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID)? View.GONE : View.VISIBLE);
        tvLeagueMembers.setText(TextUtils.concat("(",
                String.valueOf(mLeagueWrapper.getLeague().getNumberOfMembers()),
                " ",
                getString(R.string.members),
                ")"));


        leagueStandingListAdapter = new LeagueStandingFullListAdapter();
        leagueStandingListAdapter.set(mLeagueWrapper.getUserList());
        leagueStandingListAdapter.setOnLeagueStandingClicked(new LeagueStandingFullListAdapter.OnLeagueStandingClicked() {
            @Override
            public void onUserSelected(User user) {
                getPresenter().fetchRemainingPredictions(user);
            }

            @Override
            public void onMoreClicked() {
                getPresenter().fetchMoreUsers(mLeagueWrapper.getLeague().getID(), mLeagueWrapper.getUserList().size());

            }
        });

        if (mLeagueWrapper.getLeague().getNumberOfMembers() == mLeagueWrapper.getUserList().size()) {
            leagueStandingListAdapter.disableMoreButton();
        }

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

        boolean isOverall = mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID);

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
                            mLeagueWrapper.getUserList()));
                }
            });
        }

        setupFooter();
    }

    @Override
    public void startUserPredictionsActivity(User user, List<Prediction> predictionList) {
        startActivity(UsersPredictionsActivity.makeIntent(this, user, predictionList));
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
                        getPresenter().deleteLeague(
                                GlobalData.getInstance().user.getID(),
                                mLeagueWrapper.getLeague().getID());
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
                        getPresenter().leaveLeague(
                                GlobalData.getInstance().user.getID(),
                                mLeagueWrapper.getLeague().getID());
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
}
