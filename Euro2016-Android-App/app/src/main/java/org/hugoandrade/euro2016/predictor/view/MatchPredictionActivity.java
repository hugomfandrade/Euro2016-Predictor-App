package org.hugoandrade.euro2016.predictor.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.MVP;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.presenter.MatchPredictionPresenter;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.SharedPreferencesUtils;
import org.hugoandrade.euro2016.predictor.utils.TranslationUtils;
import org.hugoandrade.euro2016.predictor.view.dialog.FilterPopup;
import org.hugoandrade.euro2016.predictor.view.listadapter.MatchPredictionListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MatchPredictionActivity extends MainActivityBase<MVP.RequiredMatchPredictionViewOps,
                                                              MVP.ProvidedMatchPredictionPresenterOps,
                                                              MatchPredictionPresenter>

        implements MVP.RequiredMatchPredictionViewOps {

    private static final String INTENT_EXTRA_USER_LIST = "intent_extra_user_list";
    private static final String INTENT_EXTRA_LEAGUE_NAME = "intent_extra_league_name";

    private static final String TIME_TEMPLATE = "d MMMM - HH:mm";

    private String mLeagueName;
    private List<LeagueUser> mUserList;
    private int mMaxMatchNumber;

    private List<String> mMatchFilterList;
    private int mCurrentMatchNumber;

    private View progressBar;
    private TextView tvMatchText;
    private MatchPredictionListAdapter mMatchPredictionsAdapter;

    private TextView tvHomeTeam;
    private TextView tvAwayTeam;
    private ImageView ivHomeTeam;
    private ImageView ivAwayTeam;
    private TextView etHomeTeamGoals;
    private TextView etAwayTeamGoals;
    private View detailsInfoContainer;
    private TextView tvMatchNumber;
    private TextView tvStadium;
    private TextView tvStage;
    private TextView tvDateAndTime;
    private ImageView ivMatchPrevious;
    private ImageView ivMatchNext;

    public static Intent makeIntent(Context context, List<LeagueUser> userList, String leagueName) {
        return new Intent(context, MatchPredictionActivity.class)
                .putExtra(INTENT_EXTRA_USER_LIST, new ArrayList<>(userList))
                .putExtra(INTENT_EXTRA_LEAGUE_NAME, leagueName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent() != null && getIntent() != null) {
            mLeagueName = getIntent().getStringExtra(INTENT_EXTRA_LEAGUE_NAME);
            mUserList = getIntent().getParcelableArrayListExtra(INTENT_EXTRA_USER_LIST);
            mCurrentMatchNumber = MatchUtils.getMatchNumberOfFirstNotPlayedMatched(
                    GlobalData.getInstance().getMatchList(),
                    GlobalData.getInstance().getServerTime().getTime()) - 1;

            mMaxMatchNumber = mCurrentMatchNumber;

            buildMatchFilterList();
        }
        else {
            finish();
            return;
        }

        initializeUI();

        super.onCreate(MatchPredictionPresenter.class, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeUI() {

        setContentView(R.layout.activity_match_predictions);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mLeagueName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progressBar = findViewById(R.id.progressBar_waiting);

        // Match header
        View matchFilterHeader = findViewById(R.id.viewGroup_match_header);
        matchFilterHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterPopup popup = new FilterPopup(v, mMatchFilterList, mCurrentMatchNumber - 1, 5);
                popup.setOnFilterItemClickedListener(new FilterPopup.OnFilterItemClickedListener() {
                    @Override
                    public void onFilterItemClicked(int position) {
                        filterSpecificMatchNumber(position + 1);
                    }
                });
            }
        });
        tvMatchText = findViewById(R.id.tv_match_title);
        ivMatchNext = findViewById(R.id.iv_match_next);
        ivMatchNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterNext();
            }
        });
        ivMatchPrevious = findViewById(R.id.iv_match_previous);
        ivMatchPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterPrevious();
            }
        });

        // Match info
        tvHomeTeam = findViewById(R.id.tv_match_home_team);
        tvAwayTeam = findViewById(R.id.tv_match_away_team);
        ivHomeTeam = findViewById(R.id.iv_match_home_team);
        ivAwayTeam = findViewById(R.id.iv_match_away_team);
        etHomeTeamGoals = findViewById(R.id.et_home_team_goals);
        etAwayTeamGoals = findViewById(R.id.et_away_team_goals);
        detailsInfoContainer = findViewById(R.id.viewGroup_info_details_container);
        tvDateAndTime = findViewById(R.id.tv_match_date_time);

        tvMatchNumber = findViewById(R.id.tv_match_number);
        ImageView ivInfo = findViewById(R.id.iv_info);
        tvStadium = findViewById(R.id.tv_match_stadium);
        tvStage = findViewById(R.id.tv_stage);
        detailsInfoContainer = findViewById(R.id.viewGroup_info_details_container);
        ivInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        detailsInfoContainer.setVisibility(View.VISIBLE);
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        detailsInfoContainer.setVisibility(View.INVISIBLE);
                        break;
                }

                return true;
            }
        });


        // Prediction list
        RecyclerView rvPredictions = findViewById(R.id.rv_predictions_of_users);
        mMatchPredictionsAdapter = new MatchPredictionListAdapter();
        rvPredictions.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mMatchPredictionsAdapter);

    }

    @Override
    public void disableUI() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void enableUI() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void buildMatchFilterList() {
        mMatchFilterList = new ArrayList<>();

        List<Match> matchList = GlobalData.getInstance().getMatchList();

        for (int i = 0 ; i < matchList.size() && matchList.get(i).getMatchNumber() <= mMaxMatchNumber ; i++) {
            mMatchFilterList.add(TextUtils.concat(
                    String.valueOf(matchList.get(i).getMatchNumber()),
                    ": ",
                    MatchUtils.getShortMatchUp(this, matchList.get(i))).toString());
        }
    }

    @Override
    protected void logout() {
        getPresenter().logout();

        super.logout();
    }

    private void filterNext() {
        if (mCurrentMatchNumber < mMaxMatchNumber) {
            filterSpecificMatchNumber(mCurrentMatchNumber + 1);
        }
    }

    private void filterPrevious() {
        if (mCurrentMatchNumber > 1) {
            filterSpecificMatchNumber(mCurrentMatchNumber - 1);
        }
    }

    private void filterSpecificMatchNumber(int matchNumber) {
        getPresenter().getPredictions(mUserList, matchNumber);
    }

    @Override
    public List<LeagueUser> getUserList() {
        return mUserList;
    }

    @Override
    public void setMatchPredictionList(int matchNumber, List<User> userList) {
        mCurrentMatchNumber = matchNumber;

        int colorMain = getResources().getColor(R.color.colorMain);
        ivMatchPrevious.getDrawable().setColorFilter(colorMain, PorterDuff.Mode.SRC_ATOP);
        ivMatchNext.getDrawable().setColorFilter(colorMain, PorterDuff.Mode.SRC_ATOP);

        if (matchNumber == 1) {
            ivMatchPrevious.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        }
        if (matchNumber == mMaxMatchNumber) {
            ivMatchNext.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        }

        Match match = GlobalData.getInstance().getMatch(matchNumber);

        tvMatchText.setText(TextUtils.concat(getString(R.string.match_number), " ", String.valueOf(matchNumber)));
        mMatchPredictionsAdapter.setMatch(GlobalData.getInstance().getMatch(matchNumber));
        mMatchPredictionsAdapter.setPredictionList(GlobalData.getInstance().getPredictionsOfUsers(matchNumber, userList));
        mMatchPredictionsAdapter.notifyDataSetChanged();

        tvHomeTeam.setText(TranslationUtils.translateCountryName(this, match.getHomeTeamName()));
        tvAwayTeam.setText(TranslationUtils.translateCountryName(this, match.getAwayTeamName()));
        ivHomeTeam.setImageResource(Country.getImageID(match.getHomeTeam()));
        ivAwayTeam.setImageResource(Country.getImageID(match.getAwayTeam()));

        boolean hasHomeCountryFlag = Country.getImageID(match.getHomeTeam()) != 0;
        boolean hasAwayCountryFlag = Country.getImageID(match.getAwayTeam()) != 0;

        ((View) ivHomeTeam.getParent()).setVisibility(hasHomeCountryFlag ? View.VISIBLE : View.GONE);
        ((View) ivAwayTeam.getParent()).setVisibility(hasAwayCountryFlag ? View.VISIBLE : View.GONE);
        tvHomeTeam.setGravity(hasHomeCountryFlag ? Gravity.TOP | Gravity.CENTER_HORIZONTAL : Gravity.CENTER);
        tvAwayTeam.setGravity(hasAwayCountryFlag ? Gravity.TOP | Gravity.CENTER_HORIZONTAL : Gravity.CENTER);

        etHomeTeamGoals.setText(MatchUtils.getScoreOfHomeTeam(match));
        etAwayTeamGoals.setText(MatchUtils.getScoreOfAwayTeam(match));

        tvDateAndTime.setText(DateFormat.format(TIME_TEMPLATE, match.getDateAndTime()).toString());

        tvMatchNumber.setText(TextUtils.concat(getString(R.string.match_number), ": ", String.valueOf(match.getMatchNumber())));
        detailsInfoContainer.setVisibility(View.INVISIBLE);
        tvStage.setText(
                String.format("%s%s", TranslationUtils.translateStage(this, match.getStage()), match.getGroup() == null ? "" : (" - " + match.getGroup())));
        tvStadium.setText(match.getStadium());

    }

    public void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void reportMessage(String message) {
        showSnackBar(message);
    }
}
