package hugoandrade.euro2016.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;
import hugoandrade.euro2016.object.User;
import hugoandrade.euro2016.view.listadapter.PredictionListAdapter;

public class UsersPredictionsActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private static final String USER = "User";
    private static final String MATCH_LIST = "MatchList";
    private static final String PREDICTION_LIST = "PredictionList";

    private ArrayList<Match> allMatchesList;
    private ArrayList<Prediction> allPredictionsList;
    private User selectedUser;

    public static Intent makeIntent(Context activityContext,
                                    User selectedUser,
                                    ArrayList<Match> matchList,
                                    ArrayList<Prediction> predictionList) {

        Intent intent = new Intent(activityContext, UsersPredictionsActivity.class);
        intent.putExtra(USER, selectedUser);
        intent.putParcelableArrayListExtra(MATCH_LIST, matchList);
        intent.putParcelableArrayListExtra(PREDICTION_LIST, predictionList);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_predictions);

        if (getIntent() != null && getIntent().getExtras() != null) {
            selectedUser = getIntent().getExtras().getParcelable(USER);
            allMatchesList = getIntent().getExtras().getParcelableArrayList(MATCH_LIST);
            allPredictionsList = getIntent().getExtras().getParcelableArrayList(PREDICTION_LIST);
        }
        else {
            finish();
            return;
        }

        initializeViews();
    }

    private void initializeViews() {
        ((TextView) findViewById(R.id.tv_username)).setText(selectedUser.username);
        RecyclerView lvAllPredictions = (RecyclerView) findViewById(R.id.rv_all_predictions);
        lvAllPredictions.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        PredictionListAdapter mAdapter = new PredictionListAdapter(
                allMatchesList, allPredictionsList, PredictionListAdapter.TASK_DISPLAY_ONLY);
        lvAllPredictions.setAdapter(mAdapter);
        lvAllPredictions.scrollToPosition(getStartingItemPosition());
    }


    public int getStartingItemPosition() {
        int selection = 0;
        if (allMatchesList != null) {
            selection = 0;
            for (int i = 0; i < allMatchesList.size(); i++) {
                if (allMatchesList.get(i).dateAndTime.after(GlobalData.getServerTime().getTime())) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
