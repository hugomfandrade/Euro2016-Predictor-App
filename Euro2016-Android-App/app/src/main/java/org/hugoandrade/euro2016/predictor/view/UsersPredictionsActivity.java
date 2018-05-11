package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import org.hugoandrade.euro2016.predictor.view.dialog.FilterPopup;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

public class UsersPredictionsActivity extends SimpleActivityBase {

    private static final String INTENT_EXTRA_USER = "intent_extra_user";
    private static final String INTENT_EXTRA_PREDICTION_LIST = "intent_extra_prediction_list";

    private User mUser;
    private List<Prediction> mPredictionList;

    private List<String> mPredictionFilter;
    private int currentFilter = 0;

    private TextView tvFilterText;
    private RecyclerView rvPredictions;
    private PredictionListAdapter mPredictionsAdapter;
    private ImageView ivFilterPrevious;
    private ImageView ivFilterNext;

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

    private void initializeUI() {

        setContentView(R.layout.activity_user_predictions);

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mUser.getEmail());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvFilterText = findViewById(R.id.tv_filter_title);
        View filterTextHeader = findViewById(R.id.viewGroup_prediction_header);
        filterTextHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterPopup popup = new FilterPopup(v, mPredictionFilter, currentFilter);
                popup.setOnFilterItemClickedListener(new FilterPopup.OnFilterItemClickedListener() {
                    @Override
                    public void onFilterItemClicked(int position) {
                        setupFilter(position);
                    }
                });
            }
        });
        ivFilterNext = findViewById(R.id.iv_filter_next);
        ivFilterNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterNext();
            }
        });
        ivFilterPrevious = findViewById(R.id.iv_filter_previous);
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

        setupFilterUI();
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

    private void setupFilter(int position) {
        currentFilter = position;
        setupFilterUI();
    }

    private void filterNext() {
        if ((currentFilter + 1) < mPredictionFilter.size()) {
            setupFilter(currentFilter + 1);
        }
    }

    private void filterPrevious() {
        if (currentFilter != 0) {
            setupFilter(currentFilter - 1);
        }
    }

    private void setupFilterUI() {
        int colorMain = getResources().getColor(R.color.colorMain);
        ivFilterPrevious.getDrawable().setColorFilter(colorMain, PorterDuff.Mode.SRC_ATOP);
        ivFilterNext.getDrawable().setColorFilter(colorMain, PorterDuff.Mode.SRC_ATOP);
        tvFilterText.setText(mPredictionFilter.get(currentFilter));
        List<Match> matchList = new ArrayList<>();
        int startingPosition = 0;
        switch (currentFilter) {
            case 0:
                ivFilterPrevious.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
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
                ivFilterNext.getDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                matchList = GlobalData.getInstance().getMatchList(StaticVariableUtils.SStage.finals);
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
