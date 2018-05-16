package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.common.TextWatcherAdapter;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.data.raw.Prediction;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.TranslationUtils;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class PredictionListAdapter extends RecyclerView.Adapter<PredictionListAdapter.ViewHolder> {

    private static final String DAY_MONTH_TEMPLATE = "d MMMM";
    private static final String TIME_TEMPLATE = "HH:mm";

    public static final int VIEW_TYPE_DISPLAY_ONLY = 0;
    public static final int VIEW_TYPE_DISPLAY_AND_UPDATE = 1;

    private static final int COLOR_DEFAULT = Color.parseColor("#aaffffff");
    private static final int COLOR_INCORRECT_PREDICTION = Color.parseColor("#aaff0000");
    private static final int COLOR_CORRECT_MARGIN_OF_VICTORY = Color.parseColor("#aaAAAA00");
    private static final int COLOR_CORRECT_OUTCOME = Color.parseColor("#aaFF5500");
    private static final int COLOR_CORRECT_PREDICTION = Color.parseColor("#aa00AA00");

    private static final int TEXT_COLOR = Color.parseColor("#222222");
    private static final int TEXT_COLOR_2 = Color.parseColor("#444444");
    private static final int TEXT_COLOR_DEFAULT = Color.WHITE;//parseColor("#c0d7ed");

    private final int mViewType;

    private List<Prediction> mPredictionList;
    private List<InputPrediction> mInputPredictionList;
    private SparseArray<Match> mMatchSet;

    private RecyclerView mRecyclerView;
    private OnPredictionSetListener mListener;
    private Handler mHandler;
    private Runnable mRunnable;

    public PredictionListAdapter(List<Match> matchList,
                                 List<Prediction> predictionList,
                                 int taskType) {
        setPredictionList(predictionList);
        setMatchList(matchList);
        mViewType = taskType;
        mHandler = new Handler();
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
        return new ViewHolder(vi.inflate(R.layout.list_item_prediction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        InputPrediction inputPrediction = mInputPredictionList.get(holder.getAdapterPosition());
        Match match = inputPrediction.mMatch;
        Prediction prediction = inputPrediction.mPrediction;

        boolean isEnabled = inputPrediction.mIsEnabled;
        boolean isPast = match.getDateAndTime().before(GlobalData.getInstance().getServerTime().getTime());
        boolean viewOnly = mViewType == VIEW_TYPE_DISPLAY_ONLY;
        boolean isSameDayAsPrevious = position != 0
                && DateFormat.format(DAY_MONTH_TEMPLATE, match.getDateAndTime()).toString()
                .equals(DateFormat.format(DAY_MONTH_TEMPLATE, mInputPredictionList.get(holder.getAdapterPosition() - 1).mMatch.getDateAndTime()).toString());

        holder.isBinding = true;

        holder.cardView.setCardBackgroundColor(isPast? getCardColor(prediction) : COLOR_DEFAULT);

        holder.tvHomeTeam.setText(MatchUtils.tryGetTemporaryHomeTeam(context, mMatchSet, match));
        holder.tvAwayTeam.setText(MatchUtils.tryGetTemporaryAwayTeam(context, mMatchSet, match));

        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvHomeTeam.setTextColor(isPast ? TEXT_COLOR_DEFAULT : TEXT_COLOR);
        holder.tvAwayTeam.setTextColor(isPast ? TEXT_COLOR_DEFAULT : TEXT_COLOR);

        holder.ivHomeTeam.setImageResource(Country.getImageID(match.getHomeTeam()));
        holder.ivAwayTeam.setImageResource(Country.getImageID(match.getAwayTeam()));

        boolean hasHomeCountryFlag = Country.getImageID(match.getHomeTeam()) != 0;
        boolean hasAwayCountryFlag = Country.getImageID(match.getAwayTeam()) != 0;
        ((View) holder.ivHomeTeam.getParent()).setVisibility(hasHomeCountryFlag ? View.VISIBLE : View.GONE);
        ((View) holder.ivAwayTeam.getParent()).setVisibility(hasAwayCountryFlag ? View.VISIBLE : View.GONE);
        holder.tvHomeTeam.setGravity(hasHomeCountryFlag ? Gravity.TOP | Gravity.CENTER_HORIZONTAL : Gravity.CENTER);
        holder.tvAwayTeam.setGravity(hasAwayCountryFlag ? Gravity.TOP | Gravity.CENTER_HORIZONTAL : Gravity.CENTER);

        holder.etHomeTeamGoals.setText(inputPrediction.mHomeTeamGoals);
        holder.etHomeTeamGoals.setEnabled(!isPast && isEnabled && !viewOnly);
        holder.etAwayTeamGoals.setText(inputPrediction.mAwayTeamGoals);
        holder.etAwayTeamGoals.setEnabled(!isPast && isEnabled && !viewOnly);

        holder.tvDayMonth.setText(DateFormat.format(DAY_MONTH_TEMPLATE, match.getDateAndTime()).toString());
        holder.tvDayMonth.setVisibility(isSameDayAsPrevious? View.GONE : View.VISIBLE);
        holder.tvTime.setText(DateFormat.format(TIME_TEMPLATE, match.getDateAndTime()).toString());
        holder.tvTime.setTextColor(isPast ? TEXT_COLOR_DEFAULT : TEXT_COLOR_2);
        holder.tvTime.setVisibility(isPast ? View.INVISIBLE : View.VISIBLE);
        holder.tvMatchUpResult.setText(MatchUtils.getShortDescription(match));

        holder.detailsContainer.setVisibility(isPast ? View.VISIBLE: View.GONE);

        holder.tvPoints.setText(getPointsText(prediction));

        holder.tvMatchNumber.setText(TextUtils.concat(
                context.getString(R.string.match_number),
                ": ",
                String.valueOf(match.getMatchNumber())));
        holder.detailsInfoContainer.setVisibility(View.INVISIBLE);
        holder.tvStage.setText(
                String.format("%s%s", TranslationUtils.translateStage(context, match.getStage()), match.getGroup() == null ? "" : (" - " + match.getGroup())));
        holder.tvStadium.setText(match.getStadium());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.ivInfo.setImageTintList(ColorStateList.valueOf(isPast ? TEXT_COLOR_DEFAULT : TEXT_COLOR));
        }
        holder.ivInfo.setVisibility(viewOnly ? View.GONE : View.VISIBLE);

        holder.isBinding = false;
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
        mMatchSet = new SparseArray<>();
        for (Match m : GlobalData.getInstance().getMatchList()) {
            mMatchSet.put(m.getMatchNumber(), m);
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
                //notifyItemChanged(l);
                break;
            }
    }

    public void updateFailedPrediction(Prediction prediction) {
        for (int l = 0; l < mInputPredictionList.size() ; l++)
            if (mInputPredictionList.get(l).mMatch.getMatchNumber() == prediction.getMatchNumber()) {
                mInputPredictionList.get(l).mIsEnabled = true;
                mInputPredictionList.get(l).failed();
                notifyItemChanged(l);
                break;
            }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMatchNumber;
        View detailsInfoContainer;
        TextView tvStage;
        TextView tvStadium;
        ImageView ivInfo;

        CardView cardView;
        TextView tvDayMonth;
        TextView tvTime;
        TextView tvHomeTeam;
        TextView tvAwayTeam;
        TextView tvMatchUpResult;
        ImageView ivHomeTeam;
        ImageView ivAwayTeam;
        EditText etHomeTeamGoals;
        EditText etAwayTeamGoals;
        TextView tvPoints;
        View detailsContainer;
        boolean isBinding;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(View itemView) {
            super(itemView);

            tvDayMonth = itemView.findViewById(R.id.tv_month);
            tvTime = itemView.findViewById(R.id.tv_time);
            cardView = itemView.findViewById(R.id.cardView_container);
            tvHomeTeam = itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = itemView.findViewById(R.id.tv_match_away_team);
            ivHomeTeam = itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = itemView.findViewById(R.id.iv_match_away_team);
            tvMatchUpResult = itemView.findViewById(R.id.tv_match_result);
            etHomeTeamGoals = itemView.findViewById(R.id.et_home_team_goals);
            etAwayTeamGoals = itemView.findViewById(R.id.et_away_team_goals);
            tvPoints = itemView.findViewById(R.id.tv_points);
            detailsContainer = itemView.findViewById(R.id.viewGroup_details_container);

            ivHomeTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Country c = mInputPredictionList.get(getAdapterPosition()).mMatch.getHomeTeam();

                    if (c != null && mListener != null) {
                        mListener.onCountryClicked(c);
                    }
                }
            });
            ivAwayTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Country c = mInputPredictionList.get(getAdapterPosition()).mMatch.getAwayTeam();

                    if (c != null && mListener != null) {
                        mListener.onCountryClicked(c);
                    }
                }
            });

            etHomeTeamGoals.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isBinding) return;

                    mInputPredictionList.get(getAdapterPosition()).mHomeTeamGoals = s.toString();

                    onPredictionChanged();
                }
            });
            etAwayTeamGoals.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isBinding) return;

                    mInputPredictionList.get(getAdapterPosition()).mAwayTeamGoals = s.toString();

                    onPredictionChanged();
                }
            });
            View.OnFocusChangeListener l = new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(final View v, boolean hasFocus) {
                    if (hasFocus) {
                        if (mRunnable != null) {
                            mHandler.removeCallbacks(mRunnable);
                        }
                    }
                    else {

                        mRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (mRecyclerView != null)
                                    ViewUtils.hideSoftKeyboardAndClearFocus(mRecyclerView);

                            }
                        };
                        mHandler.postDelayed(mRunnable, 200);/**/
                    }
                }
            };

            etHomeTeamGoals.setOnFocusChangeListener(l);
            etAwayTeamGoals.setOnFocusChangeListener(l);
            etAwayTeamGoals.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        ViewUtils.hideSoftKeyboardAndClearFocus(etAwayTeamGoals);
                        //return true;
                    }
                    return false;
                }
            });

            tvMatchNumber = itemView.findViewById(R.id.tv_match_number);
            ivInfo = itemView.findViewById(R.id.iv_info);
            tvStadium = itemView.findViewById(R.id.tv_match_stadium);
            tvStage = itemView.findViewById(R.id.tv_stage);
            detailsInfoContainer = itemView.findViewById(R.id.viewGroup_info_details_container);
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

        }

        private void onPredictionChanged() {
            if (mListener != null) {
                InputPrediction inputPrediction = mInputPredictionList.get(getAdapterPosition());
                inputPrediction.mIsEnabled = false;
                Prediction prediction = new Prediction(
                        GlobalData.getInstance().user.getID(),
                        inputPrediction.mMatch.getMatchNumber(),
                        MatchUtils.getInt(inputPrediction.mHomeTeamGoals),
                        MatchUtils.getInt(inputPrediction.mAwayTeamGoals));
                //notifyItemChanged(getAdapterPosition());
                mListener.onPredictionSet(prediction);
            }
        }
    }

    public void setOnPredictionSetListener(OnPredictionSetListener listener) {
        mListener = listener;
    }

    public interface OnPredictionSetListener {
        void onPredictionSet(Prediction prediction);
        void onCountryClicked(Country country);
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
            return "+0pts";
        }
        else {
            return "+" + String.valueOf(prediction.getScore()) + "pts";
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

        public void setPrediction(Prediction prediction) {
            mPrediction = prediction;
            mHomeTeamGoals = MatchUtils.getAsString(prediction.getHomeTeamGoals());
            mAwayTeamGoals = MatchUtils.getAsString(prediction.getAwayTeamGoals());
        }

        public void failed() {
            if (mPrediction == null) {
                mHomeTeamGoals = "";
                mAwayTeamGoals = "";
            }
            else {
                mHomeTeamGoals = MatchUtils.getAsString(mPrediction.getHomeTeamGoals());
                mAwayTeamGoals = MatchUtils.getAsString(mPrediction.getAwayTeamGoals());
            }
        }
    }
}
