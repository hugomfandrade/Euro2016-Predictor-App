package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

public class UsersPredictionsActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private final String TAG = getClass().getSimpleName();

    private static final String INTENT_EXTRA_USER = "intent_extra_user";
    private static final String INTENT_EXTRA_PREDICTION_LIST = "intent_extra_prediction_list";

    private User mUser;
    private List<Prediction> mPredictionList;

    private List<String> mPredictionFilter;
    private int currentFilter = 0;

    private TextView tvFilterText;
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

        mPredictionFilter = buildPredictionFilter();

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

        setContentView(R.layout.activity_user_predictions);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mUser.getEmail());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvFilterText = findViewById(R.id.tv_filter_title);
        View ivFilterNext = findViewById(R.id.iv_filter_next);
        ivFilterNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterNext();
            }
        });
        View ivFilterPrevious = findViewById(R.id.iv_filter_previous);
        ivFilterPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterPrevious();
            }
        });

        rvPredictions = findViewById(R.id.rv_predictions);
        mPredictionsAdapter = new PredictionListAdapter(GlobalData.getInstance().getMatchList(),
                                                        mPredictionList,
                                                        PredictionListAdapter.VIEW_TYPE_DISPLAY_ONLY);
        rvPredictions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mPredictionsAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());

        setupFilter();
    }

    private List<String> buildPredictionFilter() {
        List<String> predictionFilter = new ArrayList<>();
        predictionFilter.add(getString(R.string.prediction_filter_all));
        predictionFilter.add(getString(R.string.prediction_matchday_1));
        predictionFilter.add(getString(R.string.prediction_matchday_2));
        predictionFilter.add(getString(R.string.prediction_matchday_3));
        predictionFilter.add(getString(R.string.prediction_round_of_16));
        predictionFilter.add(getString(R.string.prediction_quarter_finals));
        predictionFilter.add(getString(R.string.prediction_semi_finals));
        predictionFilter.add(getString(R.string.prediction_final));
        return predictionFilter;
    }

    private void setupFilter() {
        List<Match> mMatchList = GlobalData.getInstance().getMatchList();
        tvFilterText.setText(mPredictionFilter.get(currentFilter));
        List<Match> matchList = new ArrayList<>();
        int startingPosition = 0;
        switch (currentFilter) {
            case 0:
                matchList = mMatchList;
                startingPosition = getStartingItemPosition();
                break;
            case 1:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.groupStage, 1);
                break;
            case 2:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.groupStage, 2);
                break;
            case 3:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.groupStage, 3);
                break;
            case 4:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.roundOf16);
                break;
            case 5:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.quarterFinals);
                break;
            case 6:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.semiFinals);
                break;
            case 7:
                matchList = MatchUtils.getMatchList(mMatchList, StaticVariableUtils.SStage.finals);
                break;
        }

        if (mPredictionsAdapter != null) {
            mPredictionsAdapter.setMatchList(matchList);
            mPredictionsAdapter.notifyDataSetChanged();
        }
        if (rvPredictions != null) {
            if (currentFilter == 0) {
                rvPredictions.setLayoutManager(
                        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            }
            rvPredictions.scrollToPosition(startingPosition);
        }
    }

    private void filterNext() {
        if ((currentFilter + 1) < mPredictionFilter.size()) {
            currentFilter = currentFilter + 1;
            setupFilter();
        }
    }

    private void filterPrevious() {
        if (currentFilter != 0) {
            currentFilter = currentFilter - 1;
            setupFilter();
        }
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
}
