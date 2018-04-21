package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.data.raw.User;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;

import java.util.List;

public class MatchPredictionListAdapter extends RecyclerView.Adapter<MatchPredictionListAdapter.ViewHolder> {

    private static final int COLOR_DEFAULT = Color.parseColor("#aaffffff");
    private static final int COLOR_INCORRECT_PREDICTION = Color.parseColor("#aaff0000");
    private static final int COLOR_CORRECT_MARGIN_OF_VICTORY = Color.parseColor("#aaAAAA00");
    private static final int COLOR_CORRECT_OUTCOME = Color.parseColor("#aaFF5500");
    private static final int COLOR_CORRECT_PREDICTION = Color.parseColor("#aa00AA00");

    private Match mMatch;

    private List<Pair<User, Prediction>> mPredictionList;

    private RecyclerView mRecyclerView;

    public MatchPredictionListAdapter() {
    }

    public MatchPredictionListAdapter(Match match, List<Pair<User, Prediction>> predictionList) {
        mMatch = match;
        mPredictionList = predictionList;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_match_prediction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Prediction prediction = mPredictionList.get(holder.getAdapterPosition()).second;
        User user = mPredictionList.get(holder.getAdapterPosition()).first;

        boolean isPast = mMatch.getDateAndTime().before(GlobalData.getInstance().getServerTime().getTime());

        holder.isBinding = true;

        holder.cardView.setCardBackgroundColor(isPast? getCardColor(prediction) : COLOR_DEFAULT);
        holder.ivHomeTeam.setImageResource(Country.getImageID(mMatch.getHomeTeam()));
        holder.ivAwayTeam.setImageResource(Country.getImageID(mMatch.getAwayTeam()));
        holder.etHomeTeamGoals.setText(MatchUtils.getAsString(prediction.getHomeTeamGoals()));
        holder.etAwayTeamGoals.setText(MatchUtils.getAsString(prediction.getAwayTeamGoals()));
        holder.tvPoints.setText(getPointsText(prediction));
        holder.tvUser.setText(user.getEmail());

        holder.isBinding = false;
    }

    @Override
    public int getItemCount() {
        return mPredictionList == null ? 0 : mPredictionList.size();
    }

    public void setMatch(Match match) {
        mMatch = match;
    }

    public void setPredictionList(List<Pair<User, Prediction>> predictionList) {
        mPredictionList = predictionList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView ivHomeTeam;
        ImageView ivAwayTeam;
        EditText etHomeTeamGoals;
        EditText etAwayTeamGoals;
        TextView tvPoints;
        TextView tvUser;
        boolean isBinding;

        ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_container);
            ivHomeTeam = itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = itemView.findViewById(R.id.iv_match_away_team);
            etHomeTeamGoals = itemView.findViewById(R.id.et_home_team_goals);
            etAwayTeamGoals = itemView.findViewById(R.id.et_away_team_goals);
            tvPoints = itemView.findViewById(R.id.tv_points);
            tvUser = itemView.findViewById(R.id.tv_user);
        }
    }

    private int getCardColor(Prediction prediction) {
        if (prediction == null) {
            return COLOR_INCORRECT_PREDICTION;
        }
        else {
            if (prediction.getScore() == GlobalData.getInstance().systemData.getRules().getRuleCorrectMarginOfVictory()) {
                return COLOR_CORRECT_MARGIN_OF_VICTORY;
            }
            else if (prediction.getScore() == GlobalData.getInstance().systemData.getRules().getRuleCorrectOutcome()) {
                return COLOR_CORRECT_OUTCOME;
            }
            else if (prediction.getScore() == GlobalData.getInstance().systemData.getRules().getRuleCorrectPrediction()) {
                return COLOR_CORRECT_PREDICTION;
            }
            else {
                return COLOR_INCORRECT_PREDICTION;
            }
        }
    }

    private String getPointsText(Prediction prediction) {
        if (prediction == null || prediction.getScore() == -1) {
            return "0";
        }
        else {
            return String.valueOf(prediction.getScore());
        }
    }
}
