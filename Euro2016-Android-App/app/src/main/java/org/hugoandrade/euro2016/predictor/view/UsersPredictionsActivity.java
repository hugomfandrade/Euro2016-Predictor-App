package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.view.listadapter.PredictionListAdapter;

public class UsersPredictionsActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String INTENT_EXTRA_USER = "user";
    private static final String INTENT_EXTRA_MATCH_LIST = "match_list";
    private static final String INTENT_EXTRA_PREDICTION_LIST = "prediction_list";

    private List<Match> mMatchList;
    private List<Prediction> mPredictionList;
    private User mUser;

    public static Intent makeIntent(Context context,
                                    User selectedUser,
                                    List<Match> matchList,
                                    List<Prediction> predictionList) {

        return new Intent(context, UsersPredictionsActivity.class)
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

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mUser.getEmail());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvPredictions = findViewById(R.id.rv_all_predictions);
        PredictionListAdapter mAdapter = new PredictionListAdapter(mMatchList,
                                                                   mPredictionList,
                                                                   PredictionListAdapter.VIEW_TYPE_DISPLAY_ONLY);
        rvPredictions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mAdapter);
        rvPredictions.scrollToPosition(getStartingItemPosition());
    }


    public int getStartingItemPosition() {
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
