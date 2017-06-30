package hugoandrade.euro2016.view.listadapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.IntDef;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import hugoandrade.euro2016.R;
import hugoandrade.euro2016.GlobalData;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;

import static android.view.View.GONE;

public class PredictionListAdapter extends RecyclerView.Adapter<PredictionListAdapter.ViewHolder> {

    @SuppressWarnings("unused") private final static String TAG = PredictionListAdapter.class.getSimpleName();

    public static final int TASK_DISPLAY_AND_UPDATE = 1;
    public static final int TASK_DISPLAY_ONLY = 2;

    @IntDef({TASK_DISPLAY_AND_UPDATE, TASK_DISPLAY_ONLY})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TaskType {}

    private static int COLOR_CORAL_RED = Color.parseColor("#ffff4444");
    private static int COLOR_BLACK = Color.parseColor("#ff000000");

    private final @TaskType int taskType;
    private final ArrayList<Match> mAllMatchesList;
    private final SparseArray<Prediction> mAllPredictionMap;
    private OnButtonClickedListener mListener;

    private HashSet<Integer> mDisabledPredictionSet = new HashSet<>();
    private HashSet<Integer> mWaitingForResponsePredictionSet = new HashSet<>();

    public PredictionListAdapter(ArrayList<Match> allMatchesList,
                                 ArrayList<Prediction> allPredictionList,
                                 @TaskType int taskType) {
        this.mAllMatchesList = new ArrayList<>();
        this.mAllMatchesList.addAll(allMatchesList);
        this.mAllPredictionMap = new SparseArray<>();
        for (Prediction prediction : allPredictionList)
            this.mAllPredictionMap.append(prediction.matchNo, prediction);
        this.taskType = taskType;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(
                vi.inflate(R.layout.list_item_prediction, parent, false),
                mAllMatchesList.get(viewType)/*,
                new ViewHolder.OnButtonClickedListener() {
                    @Override
                    public void onClick(int position, String homeValue, String awayValue) {
                        if (mListener != null) {
                            Prediction prediction
                                    = mAllPredictionMap.get(
                                        mAllMatchesList.get(position).matchNo,
                                        new Prediction(
                                                GlobalData.user.id,
                                                mAllMatchesList.get(position).matchNo,
                                                getInt(homeValue, -1),
                                                getInt(awayValue, -1))).cloneInstance();
                            prediction.homeTeamGoals = getInt(homeValue, -1);
                            prediction.awayTeamGoals = getInt(awayValue, -1);

                            mListener.onClick(prediction);
                        }

                    }
                }/**/
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Match mMatchInfo = mAllMatchesList.get(holder.getAdapterPosition());
        Prediction mPredictionInfo = mAllPredictionMap.get(mMatchInfo.matchNo, null);

        if (mPredictionInfo != null && !mPredictionInfo.equals(holder.mPrediction)) {
            holder.currentPredictionHomeTeamGoals = mPredictionInfo.homeTeamGoals == -1?
                    "" : String.valueOf(mPredictionInfo.homeTeamGoals);
            holder.currentPredictionAwayTeamGoals = mPredictionInfo.awayTeamGoals == -1?
                    "" : String.valueOf(mPredictionInfo.awayTeamGoals);
        }

        holder.mPrediction = mPredictionInfo;
        holder.progressBar.setMinimumHeight(holder.itemView.getMeasuredHeight());
        holder.tvMatchNo.setText(String.valueOf(mMatchInfo.matchNo));
        holder.tvHomeTeam.setText(mMatchInfo.homeTeam);
        holder.tvAwayTeam.setText(mMatchInfo.awayTeam);
        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);
        holder.ivHomeTeam.setImageResource(mMatchInfo.homeTeamImageID);
        holder.ivAwayTeam.setImageResource(mMatchInfo.awayTeamImageID);
        holder.etPredictionHomeTeamGoals.setText(holder.currentPredictionHomeTeamGoals);
        holder.etPredictionAwayTeamGoals.setText(holder.currentPredictionAwayTeamGoals);

        if (mMatchInfo.dateAndTime.before(GlobalData.getServerTime().getTime())) {
            holder.btSetResult.setEnabled(false);
            holder.etPredictionHomeTeamGoals.setEnabled(false);
            holder.etPredictionAwayTeamGoals.setEnabled(false);
            holder.progressBar.setVisibility(GONE);
        } else {
            if (mDisabledPredictionSet.contains(mMatchInfo.matchNo))
                holder.btSetResult.setEnabled(false);
            holder.btSetResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        Prediction prediction = mAllPredictionMap.get(
                                mMatchInfo.matchNo,
                                new Prediction(
                                        GlobalData.user.id,
                                        mMatchInfo.matchNo,
                                        getInt(holder.currentPredictionHomeTeamGoals, -1),
                                        getInt(holder.currentPredictionAwayTeamGoals, -1))).cloneInstance();
                        prediction.homeTeamGoals = getInt(holder.currentPredictionHomeTeamGoals, -1);
                        prediction.awayTeamGoals = getInt(holder.currentPredictionAwayTeamGoals, -1);

                        mListener.onClick(prediction);
                    }
                }
            });
            holder.etPredictionHomeTeamGoals.setEnabled(!mDisabledPredictionSet.contains(mMatchInfo.matchNo));
            holder.etPredictionAwayTeamGoals.setEnabled(!mDisabledPredictionSet.contains(mMatchInfo.matchNo));
            holder.progressBar.setVisibility(
                    mWaitingForResponsePredictionSet.contains(mMatchInfo.matchNo)?
                            View.VISIBLE: GONE);

            if (mMatchInfo.homeTeamGoals != -1 && mMatchInfo.awayTeamGoals != -1) {
                if (mMatchInfo.homeTeamGoals == mMatchInfo.awayTeamGoals) {
                    if (mMatchInfo.homeTeamNotes != null && mMatchInfo.awayTeamNotes == null)
                        holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
                    else if (mMatchInfo.homeTeamNotes == null && mMatchInfo.awayTeamNotes != null)
                        holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
                }
                else if (mMatchInfo.homeTeamGoals > mMatchInfo.awayTeamGoals)
                    holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
                else if (mMatchInfo.homeTeamGoals < mMatchInfo.awayTeamGoals)
                    holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
            }
        }

        if (mMatchInfo.dateAndTime.after(GlobalData.getServerTime().getTime()))
            holder.btSetResult.setText("SET");
        else {
            if (mPredictionInfo == null || mPredictionInfo.score == -1)
                holder.btSetResult.setText("0");
            else
                holder.btSetResult.setText(String.valueOf(mPredictionInfo.score));

            if (mPredictionInfo == null || mPredictionInfo.score == -1 || mPredictionInfo.score == 0)
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor("#7Aff0000"));
            else if (mPredictionInfo.score == 1)
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor("#7AFF5500"));
            else if (mPredictionInfo.score == 2)
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor("#7AAAAA00"));
            else if (mPredictionInfo.score == 4)
                ((CardView) holder.itemView).setCardBackgroundColor(Color.parseColor("#7A00AA00"));
        }

        holder.tvDateAndTime.setText(
                DateFormat.format("dd-MM-yyyy HH:mm", mMatchInfo.dateAndTime).toString());
        holder.tvMatchUpResult.setText(
                (mMatchInfo.awayTeamGoals != -1 && mMatchInfo.homeTeamGoals != -1)? (
                        (mMatchInfo.homeTeamNotes == null? "" : mMatchInfo.homeTeamNotes) +
                                Integer.toString(mMatchInfo.homeTeamGoals) + " - " +
                                Integer.toString(mMatchInfo.awayTeamGoals) +
                                (mMatchInfo.awayTeamNotes == null? "" : mMatchInfo.awayTeamNotes)) :
                        ""
        );

        // Disable all child views and do not show DateAndTime
        if (taskType == TASK_DISPLAY_ONLY) {
            holder.tvDateAndTime.setVisibility(GONE);
            holder.btSetResult.setEnabled(false);
            holder.etPredictionAwayTeamGoals.setEnabled(false);
            holder.etPredictionHomeTeamGoals.setEnabled(false);
            if (mMatchInfo.dateAndTime.after(GlobalData.getServerTime().getTime()))
                holder.btSetResult.setText("0");
        }

        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);
        if (mMatchInfo.homeTeamGoals != -1 && mMatchInfo.awayTeamGoals != -1) {
            if (mMatchInfo.homeTeamGoals == mMatchInfo.awayTeamGoals) {
                if (mMatchInfo.homeTeamNotes != null && mMatchInfo.awayTeamNotes == null)
                    holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
                else if (mMatchInfo.homeTeamNotes == null && mMatchInfo.awayTeamNotes != null)
                    holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
            }
            else if (mMatchInfo.homeTeamGoals > mMatchInfo.awayTeamGoals)
                holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
            else if (mMatchInfo.homeTeamGoals < mMatchInfo.awayTeamGoals)
                holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return mAllMatchesList.size();
    }

    public void setAllMatches(ArrayList<Match> matchList) {
        synchronized (this) {
            if (mAllMatchesList.size() == 0 || mAllMatchesList.size() != matchList.size()) {
                mAllMatchesList.clear();
                mAllMatchesList.addAll(matchList);
                notifyDataSetChanged();
            } else {
                for (int i = 0; i < mAllMatchesList.size(); i++)
                    if (!mAllMatchesList.get(i).equals(matchList.get(i))) {
                        mAllMatchesList.set(i, matchList.get(i));
                        notifyItemChanged(i);
                        break;
                    }
            }
        }
    }

    public void setAllPredictions(ArrayList<Prediction> predictionList) {
        synchronized (this) {
            mAllPredictionMap.clear();
            for (Prediction prediction : predictionList) {
                mAllPredictionMap.append(prediction.matchNo, prediction);

                if (mAllMatchesList.size() != 0) {
                    for (int i = 0; i < mAllMatchesList.size(); i++)
                        if (mAllMatchesList.get(i).matchNo == prediction.matchNo) {
                            notifyItemChanged(i);
                            break;
                        }
                }
            }
        }
    }

    public void updatePrediction(Prediction prediction) {
        mAllPredictionMap.append(prediction.matchNo, prediction);
        for (int l = 0 ; l < mAllMatchesList.size() ; l++)
            if (mAllMatchesList.get(l).matchNo == prediction.matchNo) {
                notifyItemChanged(l);
                break;
            }
    }

    public void setChildViewDisabled(int matchNo, boolean disabled) {
        if (disabled) {
            mDisabledPredictionSet.add(matchNo);
            for (int l = 0 ; l < mAllMatchesList.size() ; l++)
                if (mAllMatchesList.get(l).matchNo == matchNo) {
                    notifyItemChanged(l);
                    break;
                }
        }
        else {
            if (mDisabledPredictionSet.contains(matchNo)) {
                mDisabledPredictionSet.remove(matchNo);
                for (int l = 0 ; l < mAllMatchesList.size() ; l++)
                    if (mAllMatchesList.get(l).matchNo == matchNo) {
                        notifyItemChanged(l);
                        break;
                    }
            }
        }
    }

    public void setChildViewWaitingForResponse(int matchNo, boolean waitingForResponse) {
        if (waitingForResponse) {
            mWaitingForResponsePredictionSet.add(matchNo);
            for (int l = 0 ; l < mAllMatchesList.size() ; l++)
                if (mAllMatchesList.get(l).matchNo == matchNo) {
                    notifyItemChanged(l);
                    break;
                }
        }
        else {
            if (mWaitingForResponsePredictionSet.contains(matchNo)) {
                mWaitingForResponsePredictionSet.remove(matchNo);
                for (int l = 0 ; l < mAllMatchesList.size() ; l++)
                    if (mAllMatchesList.get(l).matchNo == matchNo) {
                        notifyItemChanged(l);
                        break;
                    }
            }
        }
    }

    public void reportNewServerTime(Calendar serverTime) {
        for (int l = 0 ; l < mAllMatchesList.size() ; l++)
            if (mAllMatchesList.get(l).dateAndTime.before(serverTime.getTime()))
                notifyItemChanged(l);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMatchNo, tvHomeTeam, tvAwayTeam, tvMatchUpResult, tvDateAndTime;
        ImageView ivHomeTeam, ivAwayTeam;
        EditText etPredictionHomeTeamGoals, etPredictionAwayTeamGoals;
        Button btSetResult;

        Match mMatch;
        Prediction mPrediction;
        RelativeLayout progressBar;
        String currentPredictionHomeTeamGoals, currentPredictionAwayTeamGoals;

        //OnButtonClickedListener mButtonClickedListener;

        boolean enableTextChangedListener = true;

        ViewHolder(View itemView, Match match/*, OnButtonClickedListener buttonClickedListener/**/) {
            super(itemView);
            mMatch = match;
            //mButtonClickedListener = buttonClickedListener;

            progressBar = (RelativeLayout) itemView.findViewById(R.id.progressBar_waiting_for_response);
            progressBar.setOnTouchListener(mConsumeEventTouchListener);

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
            /*btSetResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mButtonClickedListener != null)
                        mButtonClickedListener.onClick(
                                getAdapterPosition(),
                                currentPredictionHomeTeamGoals,
                                currentPredictionAwayTeamGoals
                        );
                }
            });/**/
            btSetResult.setEnabled(false);

            etPredictionHomeTeamGoals.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentPredictionHomeTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etPredictionAwayTeamGoals.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentPredictionAwayTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
        }

        private void checkIfThereAreNewValues() {
            if (mPrediction == null) {
                if (getInt(currentPredictionHomeTeamGoals, -1) == -1 &&
                    getInt(currentPredictionAwayTeamGoals, -1) == -1){

                    etPredictionHomeTeamGoals.setTextColor(COLOR_BLACK);
                    etPredictionAwayTeamGoals.setTextColor(COLOR_BLACK);
                    btSetResult.setEnabled(false);
                }
                else {

                    etPredictionHomeTeamGoals.setTextColor(COLOR_CORAL_RED);
                    etPredictionAwayTeamGoals.setTextColor(COLOR_CORAL_RED);
                    btSetResult.setEnabled(true);
                }
                return;
            }

            if (getInt(currentPredictionHomeTeamGoals, -1) != mPrediction.homeTeamGoals ||
                    getInt(currentPredictionAwayTeamGoals, -1) != mPrediction.awayTeamGoals) {
                etPredictionHomeTeamGoals.setTextColor(COLOR_CORAL_RED);
                etPredictionAwayTeamGoals.setTextColor(COLOR_CORAL_RED);
                btSetResult.setEnabled(true);
            }
            else {
                etPredictionHomeTeamGoals.setTextColor(COLOR_BLACK);
                etPredictionAwayTeamGoals.setTextColor(COLOR_BLACK);
                btSetResult.setEnabled(false);
            }
        }

        /*interface OnButtonClickedListener {
            void onClick(int position, String homeValue, String awayValue);
        }/**/

        private static final View.OnTouchListener mConsumeEventTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };

    }

    public void setOnButtonClickedListener(OnButtonClickedListener listener) {
        mListener = listener;
    }

    public interface OnButtonClickedListener {
        void onClick(Prediction prediction);
    }

    private static int getInt(String value, int defaultValue) {
        if (value == null)
            return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
