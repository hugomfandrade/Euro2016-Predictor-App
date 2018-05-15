package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import org.hugoandrade.euro2016.predictor.utils.StickyFooterUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.dialog.SimpleDialog;
import org.hugoandrade.euro2016.predictor.view.helper.FilterWrapper;
import org.hugoandrade.euro2016.predictor.view.listadapter.LeagueStandingFullListAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeagueDetailsActivity extends MainActivityBase<MVP.RequiredLeagueDetailsViewOps,
                                                            MVP.ProvidedLeagueDetailsPresenterOps,
                                                            LeagueDetailsPresenter>

        implements MVP.RequiredLeagueDetailsViewOps {

    private static final String INTENT_EXTRA_LEAGUE = "intent_extra_league";

    private LeagueWrapper mLeagueWrapper;

    private NestedScrollView mScrollViewContainer;
    private LeagueStandingFullListAdapter leagueStandingListAdapter;

    private TextView tvLatestMatch;
    private TextView tvLatestMatchFixed;

    private View progressBar;
    private int mSelectedStage = 0;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_league_details, menu);


        boolean isAdmin = GlobalData.getInstance().user.getID()
                .equals(mLeagueWrapper.getLeague().getAdminID());

        menu.findItem(R.id.action_delete_league).setVisible(isAdmin);
        menu.findItem(R.id.action_leave_league).setVisible(!isAdmin);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_leave_league:
                leaveLeague();
                return true;
            case R.id.action_delete_league:
                deleteLeague();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void initializeUI() {

        setContentView(R.layout.activity_league_details);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        boolean isOverall = mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.league));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FilterWrapper.Builder.instance(this)
                .setTheme(FilterWrapper.DARK)
                .setFilterText(findViewById(R.id.tv_filter_title))
                .setPreviousButton(findViewById(R.id.iv_filter_previous))
                .setNextButton(findViewById(R.id.iv_filter_next))
                .setListener(new FilterWrapper.OnFilterSelectedListener() {
                    @Override
                    public void onFilterSelected(int stage) {
                        mSelectedStage = stage;
                        getLeagueTopFive();
                    }
                })
                .create();

        mScrollViewContainer = findViewById(R.id.nestedScrollView);

        tvLatestMatch = findViewById(R.id.tv_latest_match);
        tvLatestMatchFixed = findViewById(R.id.tv_latest_match_fixed);

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
                getString(mLeagueWrapper.getLeague().getNumberOfMembers() == 1? R.string.member : R.string.members),
                ")"));


        leagueStandingListAdapter = new LeagueStandingFullListAdapter(mLeagueWrapper);
        leagueStandingListAdapter.setOnLeagueStandingClicked(new LeagueStandingFullListAdapter.OnLeagueStandingClicked() {
            @Override
            public void onUserSelected(User user) {
                getPresenter().fetchRemainingPredictions(user);
            }

            @Override
            public void onMoreClicked() {
                if (mSelectedStage == 0) {
                    getPresenter().fetchMoreUsers(mLeagueWrapper.getLeague().getID(), mLeagueWrapper.getLeagueUserList().size());
                }
                else {

                    getPresenter().fetchMoreUsers(
                            mLeagueWrapper.getLeague().getID(),
                            leagueStandingListAdapter.get().getLeagueUserList().size(),
                            mSelectedStage,
                            getMinMatchNumber(mSelectedStage),
                            getMaxMatchNumber(mSelectedStage));
                }
            }
        });

        RecyclerView rvLeagueStandings = findViewById(R.id.rv_league_standings);
        rvLeagueStandings.setNestedScrollingEnabled(false);
        rvLeagueStandings.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvLeagueStandings.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        rvLeagueStandings.setAdapter(leagueStandingListAdapter);

        progressBar = findViewById(R.id.progressBar_waiting);

        setupFooter();
    }

    @Override
    public void startUserPredictionsActivity(User user, List<Prediction> predictionList) {
        if (!isPaused()) {
            startActivity(UsersPredictionsActivity.makeIntent(this, user, predictionList));
        }
    }

    private void setupFooter() {

        int currentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                GlobalData.getInstance().getMatchList(),
                GlobalData.getInstance().getServerTime().getTime());

        boolean isOverall = mLeagueWrapper.getLeague().getID().equals(LeagueWrapper.OVERALL_ID);

        if (isOverall) {
            tvLatestMatch.setVisibility(View.GONE);
            tvLatestMatchFixed.setVisibility(View.GONE);
        }
        else {
            tvLatestMatch.setVisibility(currentMatchNumber <= 1 ? View.GONE : View.VISIBLE);
            tvLatestMatchFixed.setVisibility(currentMatchNumber <= 1 ? View.GONE : View.VISIBLE);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(MatchPredictionActivity.makeIntent(
                            LeagueDetailsActivity.this,
                            mLeagueWrapper.getLeagueUserList(),
                            mLeagueWrapper.getLeague().getName()));
                }
            };
            tvLatestMatch.setOnClickListener(listener);
            tvLatestMatchFixed.setOnClickListener(listener);
            StickyFooterUtils.initialize(mScrollViewContainer, tvLatestMatch, tvLatestMatchFixed);
            StickyFooterUtils.initialize(mScrollViewContainer, tvLatestMatch, tvLatestMatchFixed);
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

    private void getLeagueTopFive() {

        LeagueWrapper leagueWrapper = GlobalData.getInstance()
                .getLeagueByStage(mLeagueWrapper.getLeague().getID(), mSelectedStage);

        if (leagueWrapper != null) {
            leagueStandingListAdapter.set(leagueWrapper);
            leagueStandingListAdapter.notifyDataSetChanged();
            return;
        }

        getPresenter().fetchUsers(
                mLeagueWrapper.getLeague().getID(),
                mSelectedStage,
                getMinMatchNumber(mSelectedStage),
                getMaxMatchNumber(mSelectedStage));
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
        ViewUtils.showToast(this, message);
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
    public void updateListOfUsersByStage(int stage) {
        LeagueWrapper leagueWrapper = GlobalData.getInstance()
                .getLeagueByStage(mLeagueWrapper.getLeague().getID(), stage);
        if (leagueWrapper == null)
            leagueWrapper = new LeagueWrapper(null);
        leagueStandingListAdapter.set(leagueWrapper);
        leagueStandingListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void logout() {
        getPresenter().logout();

        super.logout();
    }

    private int getMinMatchNumber(int stage) {

        switch (stage) {
            case 0:
            case 1: // Matchday 1
                return 1;
            case 2: // Matchday 2
                return 13;
            case 3: // Matchday 3
                return 25;
            case 4: // Round of 16
                return 37;
            case 5: // QuarterFinals
                return 45;
            case 6: // SemiFinal
                return 49;
            case 7: // Final
                return 51;
            default:
                return 1;
        }
    }

    private int getMaxMatchNumber(int stage) {

        switch (stage) {
            case 0:
                return 51;
            case 1: // Matchday 1
                return 12;
            case 2: // Matchday 2
                return 24;
            case 3: // Matchday 3
                return 36;
            case 4: // Round of 16
                return 44;
            case 5: // QuarterFinals
                return 48;
            case 6: // SemiFinal
                return 50;
            case 7: // Final
                return 51;
            default:
                return 51;
        }
    }
}
