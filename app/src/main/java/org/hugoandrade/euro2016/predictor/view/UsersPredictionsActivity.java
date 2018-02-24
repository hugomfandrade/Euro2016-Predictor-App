package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.data.User;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

public class UsersPredictionsActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String INTENT_EXTRA_USER = "user";
    private static final String INTENT_EXTRA_MATCH_LIST = "match_list";
    private static final String INTENT_EXTRA_PREDICTION_LIST = "prediction_list";

    private List<Match> mMatchList;
    private List<Prediction> mPredictionList;
    private User mUser;

    public static Intent makeIntent(Context activityContext,
                                    User selectedUser,
                                    List<Match> matchList,
                                    List<Prediction> predictionList) {

        return new Intent(activityContext, UsersPredictionsActivity.class)
                .putExtra(INTENT_EXTRA_USER, selectedUser)
                .putParcelableArrayListExtra(INTENT_EXTRA_MATCH_LIST, new ArrayList<>(matchList))
                .putParcelableArrayListExtra(INTENT_EXTRA_PREDICTION_LIST, new ArrayList<>(predictionList));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_predictions);

        if (getIntent() != null && getIntent().getExtras() != null) {
            mUser = getIntent().getExtras().getParcelable(INTENT_EXTRA_USER);
            mMatchList = getIntent().getExtras().getParcelableArrayList(INTENT_EXTRA_MATCH_LIST);
            mPredictionList = getIntent().getExtras().getParcelableArrayList(INTENT_EXTRA_PREDICTION_LIST);
        }
        else {
            finish();
            return;
        }

        initializeUI();
    }

    private void initializeUI() {
        TextView tvUsername = (TextView) findViewById(R.id.tv_username);
        tvUsername.setText(mUser.getEmail());

        RecyclerView rvPredictions = (RecyclerView) findViewById(R.id.rv_all_predictions);
        PredictionListAdapter mAdapter = new PredictionListAdapter(mMatchList,
                                                                   mPredictionList,
                                                                   PredictionListAdapter.VIEW_TYPE_DISPLAY_ONLY);
        rvPredictions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());
    }


    public int getStartingItemPosition() {
        int selection = 0;
        if (mMatchList != null) {
            selection = 0;
            for (int i = 0; i < mMatchList.size(); i++) {
                if (mMatchList.get(i).getDateAndTime().after(GlobalData.getServerTime().getTime())) {
                    selection = i;
                    break;
                }
            }
            selection = (selection - 3) < 0? 0 : (selection - 3);
        }
        return selection;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
    }
}
