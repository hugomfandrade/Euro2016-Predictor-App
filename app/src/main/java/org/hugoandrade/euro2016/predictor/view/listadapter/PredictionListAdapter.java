package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Country;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.data.Prediction;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class PredictionListAdapter extends RecyclerView.Adapter<PredictionListAdapter.ViewHolder> {

    public static final int VIEW_TYPE_DISPLAY_ONLY = 0;
    public static final int VIEW_TYPE_DISPLAY_AND_UPDATE = 1;

    private static final int COLOR_DEFAULT = Color.parseColor("#aaffffff");
    private static final int COLOR_INCORRECT_PREDICTION = Color.parseColor("#7Aff0000");
    private static final int COLOR_CORRECT_OUTCOME_VIA_PENALTIES = Color.parseColor("#7AFF5500");
    private static final int COLOR_CORRECT_OUTCOME = Color.parseColor("#7AAAAA00");
    private static final int COLOR_CORRECT_PREDICTION = Color.parseColor("#7A00AA00");

    private static final int COLOR_CORAL_RED = Color.parseColor("#ffff4444");
    private static final int COLOR_BLACK = Color.parseColor("#ff000000");

    private static final String TEMPLATE = "dd-MM-yyyy HH:mm";

    private final int mViewType;

    private List<Prediction> mPredictionList;
    private List<InputPrediction> mInputPredictionList;

    private OnPredictionSetListener mListener;


    public PredictionListAdapter(List<Match> matchList,
                                 List<Prediction> predictionList,
                                 int taskType) {
        setPredictionList(predictionList);
        setMatchList(matchList);
        mViewType = taskType;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_prediction, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        InputPrediction inputPrediction = mInputPredictionList.get(holder.getAdapterPosition());
        Match match = inputPrediction.mMatch;
        Prediction prediction = inputPrediction.mPrediction;
        boolean isEnabled = inputPrediction.mIsEnabled;
        boolean isPast = match.getDateAndTime().before(GlobalData.getServerTime().getTime());
        boolean viewOnly = mViewType == VIEW_TYPE_DISPLAY_ONLY;

        holder.cardView.setCardBackgroundColor(isPast? getCardColor(prediction) : COLOR_DEFAULT);
        holder.tvMatchNo.setText(String.valueOf(match.getMatchNumber()));
        holder.tvHomeTeam.setText(match.getHomeTeamName());
        holder.tvAwayTeam.setText(match.getAwayTeamName());
        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);
        holder.ivHomeTeam.setImageResource(Country.getImageID(match.getHomeTeam()));
        holder.ivAwayTeam.setImageResource(Country.getImageID(match.getAwayTeam()));
        holder.etPredictionHomeTeamGoals.setText(inputPrediction.mHomeTeamGoals);
        holder.etPredictionHomeTeamGoals.setEnabled(!isPast && isEnabled && !viewOnly);
        holder.etPredictionAwayTeamGoals.setText(inputPrediction.mAwayTeamGoals);
        holder.etPredictionAwayTeamGoals.setEnabled(!isPast && isEnabled && !viewOnly);
        holder.tvDateAndTime.setText(DateFormat.format(TEMPLATE, match.getDateAndTime()).toString());
        holder.tvDateAndTime.setVisibility(viewOnly? GONE : VISIBLE);
        holder.tvMatchUpResult.setText(MatchUtils.getShortDescription(match));
        holder.btSetResult.setEnabled(!isPast && isEnabled && !viewOnly);
        holder.progressBar.setVisibility(!isPast && !viewOnly && !isEnabled? VISIBLE : INVISIBLE);

        if (!isEnabled || isPast || viewOnly)
            holder.btSetResult.setEnabled(false);
        else
            holder.checkIfThereAreNewValues();

        if (isPast) {
            holder.btSetResult.setText(prediction == null? "0" : String.valueOf(prediction.getScore()));
        } else {
            switch (mViewType) {
                case VIEW_TYPE_DISPLAY_AND_UPDATE:
                    holder.btSetResult.setText(R.string.set);
                    break;
                case VIEW_TYPE_DISPLAY_ONLY:
                    holder.btSetResult.setText("0");
                    break;
                default:
                    throw new IllegalArgumentException("Invalid view type, value of " + mViewType);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mInputPredictionList.size();
    }

    public void setMatchList(List<Match> matchList) {
        mInputPredictionList = new ArrayList<>();
        for (Match match : matchList) {
            InputPrediction inputPrediction = new InputPrediction(match);

            if (mPredictionList != null) {
                for (Prediction prediction : mPredictionList) {
                    if (prediction.getMatchNumber() == match.getMatchNumber()) {
                        inputPrediction.setPrediction(prediction);
                    }
                }
            }
            mInputPredictionList.add(inputPrediction);
        }
    }

    public void setPredictionList(List<Prediction> predictionList) {
        mPredictionList = predictionList;
        if (mInputPredictionList == null) return;
        for (InputPrediction inputPrediction : mInputPredictionList) {
            for (Prediction prediction : mPredictionList) {
                if (prediction.getMatchNumber() == inputPrediction.mMatch.getMatchNumber()) {
                    inputPrediction.setPrediction(prediction);
                }
            }
        }
    }

    public void updatePrediction(Prediction prediction) {
        for (int l = 0; l < mInputPredictionList.size() ; l++)
            if (mInputPredictionList.get(l).mMatch.getMatchNumber() == prediction.getMatchNumber()) {
                mInputPredictionList.get(l).setPrediction(prediction);
                mInputPredictionList.get(l).mIsEnabled = true;
                notifyItemChanged(l);
                break;
            }
    }

    public void updateFailedPrediction(Prediction prediction) {
        for (int l = 0; l < mInputPredictionList.size() ; l++)
            if (mInputPredictionList.get(l).mMatch.getMatchNumber() == prediction.getMatchNumber()) {
                mInputPredictionList.get(l).mIsEnabled = true;
                notifyItemChanged(l);
                break;
            }
    }

    public void reportNewServerTime(Calendar serverTime) {
        for (int l = 0; l < mInputPredictionList.size() ; l++)
            if (mInputPredictionList.get(l).mMatch.getDateAndTime().before(serverTime.getTime()))
                notifyItemChanged(l);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        TextView tvMatchNo;
        TextView tvHomeTeam;
        TextView tvAwayTeam;
        TextView tvMatchUpResult;
        TextView tvDateAndTime;
        ImageView ivHomeTeam;
        ImageView ivAwayTeam;
        EditText etPredictionHomeTeamGoals;
        EditText etPredictionAwayTeamGoals;
        Button btSetResult;
        View progressBar;

        ViewHolder(View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressBar_waiting_for_response);

            cardView = (CardView) itemView;
            tvMatchNo = (TextView) itemView.findViewById(R.id.tv_match_no);
            tvHomeTeam = (TextView) itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = (TextView) itemView.findViewById(R.id.tv_match_away_team);
            ivHomeTeam = (ImageView) itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = (ImageView) itemView.findViewById(R.id.iv_match_away_team);
            tvMatchUpResult = (TextView) itemView.findViewById(R.id.tv_match_result);
            tvDateAndTime = (TextView) itemView.findViewById(R.id.tv_match_date_time);
            etPredictionHomeTeamGoals = (EditText) itemView.findViewById(R.id.et_prediction_home_team_goals);
            etPredictionAwayTeamGoals = (EditText) itemView.findViewById(R.id.et_prediction_away_team_goals);

            btSetResult = (Button) itemView.findViewById(R.id.bt_set_prediction);
            btSetResult.setOnClickListener(this);

            etPredictionHomeTeamGoals.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    mInputPredictionList.get(getAdapterPosition()).mHomeTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etPredictionAwayTeamGoals.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    mInputPredictionList.get(getAdapterPosition()).mAwayTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
        }

        private void checkIfThereAreNewValues() {

            boolean isEnabled = mInputPredictionList.get(getAdapterPosition()).haveValuesChanged();
            int color = isEnabled? COLOR_CORAL_RED : COLOR_BLACK;

            etPredictionHomeTeamGoals.setTextColor(color);
            etPredictionAwayTeamGoals.setTextColor(color);
            btSetResult.setEnabled(isEnabled);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                InputPrediction inputPrediction = mInputPredictionList.get(getAdapterPosition());
                inputPrediction.mIsEnabled = false;
                Prediction prediction = new Prediction(
                        GlobalData.user.getID(),
                        inputPrediction.mMatch.getMatchNumber(),
                        MatchUtils.getInt(inputPrediction.mHomeTeamGoals),
                        MatchUtils.getInt(inputPrediction.mAwayTeamGoals));
                notifyItemChanged(getAdapterPosition());
                mListener.onPredictionSet(prediction);
            }
        }
    }

    public void setOnButtonClickedListener(OnPredictionSetListener listener) {
        mListener = listener;
    }

    public interface OnPredictionSetListener {
        void onPredictionSet(Prediction prediction);
    }

    private int getCardColor(Prediction prediction) {
        if (prediction == null) {
            return COLOR_INCORRECT_PREDICTION;
        }
        else {
            if (prediction.getScore() == GlobalData.systemData.getRules().getRuleCorrectOutcomeViaPenalties()) {
                return COLOR_CORRECT_OUTCOME_VIA_PENALTIES;
            }
            else if (prediction.getScore() == GlobalData.systemData.getRules().getRuleCorrectOutcome()) {
                return COLOR_CORRECT_OUTCOME;
            }
            else if (prediction.getScore() == GlobalData.systemData.getRules().getRuleCorrectPrediction()) {
                return COLOR_CORRECT_PREDICTION;
            }
            else {
                return COLOR_INCORRECT_PREDICTION;
            }
        }
    }

    static class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No-ops
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // No-ops
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No-ops
        }
    }

    static class InputPrediction {

        Match mMatch;
        Prediction mPrediction;
        String mHomeTeamGoals;
        String mAwayTeamGoals;
        boolean mIsEnabled;

        InputPrediction(Match match) {
            mMatch = match;
            mPrediction = null;
            mHomeTeamGoals = "";
            mAwayTeamGoals = "";
            mIsEnabled = true;
        }

        boolean haveValuesChanged() {

            if (mPrediction == null) {
                return MatchUtils.getInt(mHomeTeamGoals) != -1 || MatchUtils.getInt(mAwayTeamGoals) != -1;
            }
            else {
                return MatchUtils.getInt(mHomeTeamGoals) != mPrediction.getHomeTeamGoals() ||
                        MatchUtils.getInt(mAwayTeamGoals) != mPrediction.getAwayTeamGoals();
            }
        }

        public void setPrediction(Prediction prediction) {
            mPrediction = prediction;
            mHomeTeamGoals = MatchUtils.getAsString(prediction.getHomeTeamGoals());
            mAwayTeamGoals = MatchUtils.getAsString(prediction.getAwayTeamGoals());
        }
    }
}
