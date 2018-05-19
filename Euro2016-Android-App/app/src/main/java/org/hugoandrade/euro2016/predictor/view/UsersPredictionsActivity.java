package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StageUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;
import org.hugoandrade.euro2016.predictor.view.helper.FilterWrapper;
import org.hugoandrade.euro2016.predictor.view.helper.FilterTheme;
import org.hugoandrade.euro2016.predictor.view.helper.StageFilterWrapper;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

import java.util.ArrayList;
import java.util.List;

public class UsersPredictionsActivity extends SimpleActivityBase

        //implements FilterWrapper.OnFilterSelectedListener
        implements FilterWrapper.OnFilterSelectedListener
{

    private static final String INTENT_EXTRA_USER = "intent_extra_user";
    private static final String INTENT_EXTRA_PREDICTION_LIST = "intent_extra_prediction_list";

    private User mUser;
    private List<Prediction> mPredictionList;

    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;
    private StageFilterWrapper mFilterWrapper;

    public static Intent makeIntent(Context context,
                                    User selectedUser,
                                    List<Prediction> predictionList) {

        return new Intent(context, UsersPredictionsActivity.class)
                .putExtra(INTENT_EXTRA_USER, selectedUser)
                .putParcelableArrayListExtra(INTENT_EXTRA_PREDICTION_LIST, new ArrayList<>(predictionList));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getExtras() != null) {
            mUser = getIntent().getExtras().getParcelable(INTENT_EXTRA_USER);
            mPredictionList = getIntent().getExtras().getParcelableArrayList(INTENT_EXTRA_PREDICTION_LIST);
        }
        else {
            finish();
            return;
        }

        initializeUI();
    }

    private void initializeUI() {

        setContentView(R.layout.activity_user_predictions);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mUser.getEmail());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mFilterWrapper = StageFilterWrapper.Builder.instance(this)
                .setTheme(FilterTheme.LIGHT)
                .setFilterText(findViewById(R.id.tv_filter_title))
                .setPreviousButton(findViewById(R.id.iv_filter_previous))
                .setNextButton(findViewById(R.id.iv_filter_next))
                .setListener(this)
                .build();

        rvPredictions = findViewById(R.id.rv_predictions);
        mPredictionsAdapter = new PredictionListAdapter(GlobalData.getInstance().getMatchList(),
                                                        mPredictionList,
                                                        PredictionListAdapter.VIEW_TYPE_DISPLAY_ONLY);
        rvPredictions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mPredictionsAdapter);

        int startingItemPosition =
                MatchUtils.getPositionOfFirstNotPlayedMatch(
                        GlobalData.getInstance().getMatchList(),
                        GlobalData.getInstance().getServerTime().getTime(),
                        3);
        rvPredictions.scrollToPosition(startingItemPosition);

        Match match =
                MatchUtils.getFirstMatchOfYesterday(
                        GlobalData.getInstance().getMatchList(),
                        GlobalData.getInstance().getServerTime().getTime());

        if (match == null) {
            onFilterSelected(mFilterWrapper.getSelectedFilter());
        }
        else {
            int stageNumber = StageUtils.getStageNumber(match);

            mFilterWrapper.setSelectedFilter(stageNumber);
            onFilterSelected(stageNumber);
        }
    }

    @Override
    public void onFilterSelected(int stage) {

        int minMatchNumber = StageUtils.getMinMatchNumber(stage);
        int maxMatchNumber = StageUtils.getMaxMatchNumber(stage);

        List<Match> matchList = GlobalData.getInstance().getMatchList(minMatchNumber, maxMatchNumber);

        int startingPosition = 0;
        //if (stage == 0) {
            startingPosition = MatchUtils.getPositionOfFirstNotPlayedMatch(
                    matchList,
                    GlobalData.getInstance().getServerTime().getTime(),
                    2);
        //}

        /*if (startingPosition != 0 && startingPosition == matchList.size()) {
            startingPosition--;
        }/**/

        ViewUtils.showToast(this, "Starting::" + startingPosition);

        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setMatchList(matchList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
        if (rvPredictions != null) {
            //if (stage == 0) {
                rvPredictions.setLayoutManager(
                        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            //}
            rvPredictions.scrollToPosition(startingPosition);
        }
    }
}
