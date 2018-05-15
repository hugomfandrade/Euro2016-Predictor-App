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
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils;
import org.hugoandrade.euro2016.predictor.view.helper.FilterWrapper;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

import java.util.ArrayList;
import java.util.List;

public class UsersPredictionsActivity extends SimpleActivityBase implements FilterWrapper.OnFilterSelectedListener {

    private static final String INTENT_EXTRA_USER = "intent_extra_user";
    private static final String INTENT_EXTRA_PREDICTION_LIST = "intent_extra_prediction_list";

    private User mUser;
    private List<Prediction> mPredictionList;

    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;

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

        FilterWrapper.Builder.instance(this)
                .setTheme(FilterWrapper.LIGHT)
                .setFilterText(findViewById(R.id.tv_filter_title))
                .setPreviousButton(findViewById(R.id.iv_filter_previous))
                .setNextButton(findViewById(R.id.iv_filter_next))
                .setListener(this)
                .create();

        rvPredictions = findViewById(R.id.rv_predictions);
        mPredictionsAdapter = new PredictionListAdapter(GlobalData.getInstance().getMatchList(),
                                                        mPredictionList,
                                                        PredictionListAdapter.VIEW_TYPE_DISPLAY_ONLY);
        rvPredictions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mPredictionsAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());
    }

    public int getStartingItemPosition() {
        List<Match> mMatchList = GlobalData.getInstance().getMatchList();
        if (mMatchList != null) {
            for (int i = 0; i < mMatchList.size(); i++) {
                if (mMatchList.get(i).getDateAndTime().after(GlobalData.getInstance().getServerTime().getTime())) {
                    return (i < 3)? 0 : (i - 3);
                }
            }
        }
        return 0;
    }

    @Override
    public void onFilterSelected(int stage) {

        List<Match> matchList = new ArrayList<>();
        int startingPosition = 0;

        switch (stage) {
            case 0:
                matchList = GlobalData.getInstance().getMatchList();
                startingPosition = getStartingItemPosition();
                break;
            case 1:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.groupStage, 1);
                break;
            case 2:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.groupStage, 2);
                break;
            case 3:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.groupStage, 3);
                break;
            case 4:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.roundOf16);
                break;
            case 5:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.quarterFinals);
                break;
            case 6:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.semiFinals);
                break;
            case 7:
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.finals);
                break;
        }

        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setMatchList(matchList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
        if (rvPredictions != null) {
            if (stage == 0) {
                rvPredictions.setLayoutManager(
                        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            }
            rvPredictions.scrollToPosition(startingPosition);
        }
    }
}
