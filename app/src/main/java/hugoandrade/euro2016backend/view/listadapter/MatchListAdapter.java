package hugoandrade.euro2016backend.view.listadapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016backend.R;
import hugoandrade.euro2016backend.object.Match;

public class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {

    @SuppressWarnings("unused") private final static String TAG = MatchListAdapter.class.getSimpleName();

    private static int COLOR_CORAL_RED = Color.parseColor("#ffff4444");
    private static int COLOR_BLACK = Color.parseColor("#ff000000");

    private ArrayList<Match> mAllMatchesList;
    private OnButtonClickedListener mListener;

    public MatchListAdapter(ArrayList<Match> allMatchesList) {
        this.mAllMatchesList = new ArrayList<>();
        this.mAllMatchesList.addAll(allMatchesList);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(
                vi.inflate(R.layout.list_item_match, parent, false),
                mAllMatchesList.get(viewType),
                new ViewHolder.OnButtonClickedListener() {
                    @Override
                    public void onClick(int position, String homeValue, String awayValue, String homeTeamNotes, String awayTeamNotes) {
                        if (mListener != null){
                            Match match = mAllMatchesList.get(position).cloneInstance();
                            match.homeTeamGoals = getInt(homeValue, -1);
                            match.awayTeamGoals = getInt(awayValue, -1);
                            match.homeTeamNotes = homeTeamNotes.trim().equals("")? null : homeTeamNotes;
                            match.awayTeamNotes = awayTeamNotes.trim().equals("")? null : awayTeamNotes;
                            mListener.onClick(match);
                        }

                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Match mMatchInfo = mAllMatchesList.get(holder.getAdapterPosition());

        if (holder.currentHomeTeamGoals == null)
            holder.currentHomeTeamGoals = mMatchInfo.homeTeamGoals == -1?
                    "" : String.valueOf(mMatchInfo.homeTeamGoals);
        if (holder.currentAwayTeamGoals == null)
            holder.currentAwayTeamGoals = mMatchInfo.awayTeamGoals == -1?
                    "" : String.valueOf(mMatchInfo.awayTeamGoals);
        if (holder.currentHomeTeamNotes == null)
            holder.currentHomeTeamNotes = mMatchInfo.homeTeamNotes == null?
                    "" : mMatchInfo.homeTeamNotes;
        if (holder.currentAwayTeamNotes == null)
            holder.currentAwayTeamNotes = mMatchInfo.awayTeamNotes == null?
                    "" : mMatchInfo.awayTeamNotes;

        holder.tvMatchNo.setText(String.valueOf(mMatchInfo.matchNo));
        holder.tvHomeTeam.setText(mMatchInfo.homeTeam);
        holder.tvAwayTeam.setText(mMatchInfo.awayTeam);
        holder.etHomeTeamGoals.setText(holder.currentHomeTeamGoals);
        holder.etAwayTeamGoals.setText(holder.currentAwayTeamGoals);
        holder.etHomeTeamNotes.setText(holder.currentHomeTeamNotes);
        holder.etAwayTeamNotes.setText(holder.currentAwayTeamNotes);

    }

    @Override
    public int getItemCount() {
        return mAllMatchesList.size();
    }

    public void setAll(ArrayList<Match> matchCollection) {
        mAllMatchesList.clear();
        mAllMatchesList.addAll(matchCollection);
        notifyDataSetChanged();
    }

    public void updateMatch(Match match) {
        for (int i = 0 ; i < mAllMatchesList.size() ; i++)
            if (mAllMatchesList.get(i).matchNo == match.matchNo) {
                mAllMatchesList.set(i, match);
                notifyItemChanged(i);
                break;
            }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatchNo, tvHomeTeam, tvAwayTeam;
        EditText etHomeTeamGoals, etAwayTeamGoals, etHomeTeamNotes, etAwayTeamNotes;
        Button btSetResult;

        Match mMatch;
        String currentHomeTeamGoals, currentHomeTeamNotes;
        String currentAwayTeamGoals, currentAwayTeamNotes;

        OnButtonClickedListener mButtonClickedListener;

        boolean enableTextChangedListener = true;

        ViewHolder(View itemView, Match match, OnButtonClickedListener buttonClickedListener) {
            super(itemView);
            mMatch = match;
            mButtonClickedListener = buttonClickedListener;

            tvMatchNo = (TextView) itemView.findViewById(R.id.tv_match_no);
            tvHomeTeam = (TextView) itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = (TextView) itemView.findViewById(R.id.tv_match_away_team);
            etHomeTeamGoals = (EditText) itemView.findViewById(R.id.ed_match_home_team_goals);
            etAwayTeamGoals = (EditText) itemView.findViewById(R.id.ed_match_away_team_goals);
            etHomeTeamNotes = (EditText) itemView.findViewById(R.id.ed_match_home_team_notes);
            etAwayTeamNotes = (EditText) itemView.findViewById(R.id.ed_match_away_team_notes);

            btSetResult = (Button) itemView.findViewById(R.id.bt_set_match);
            btSetResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mButtonClickedListener != null)
                        mButtonClickedListener.onClick(
                                getAdapterPosition(),
                                currentHomeTeamGoals,
                                currentAwayTeamGoals,
                                currentHomeTeamNotes,
                                currentAwayTeamNotes
                        );
                }
            });
            btSetResult.setEnabled(false);

            etHomeTeamGoals.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentHomeTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamGoals.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentAwayTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etHomeTeamNotes.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentHomeTeamNotes = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamNotes.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void afterTextChanged(Editable s) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentAwayTeamNotes = s.toString();

                    checkIfThereAreNewValues();
                }
            });
        }

        private void checkIfThereAreNewValues() {
            if (getInt(currentHomeTeamGoals, -1) != mMatch.homeTeamGoals ||
                    getInt(currentAwayTeamGoals, -1) != mMatch.awayTeamGoals ||
                    !areEqual(currentHomeTeamNotes.equals("")? null: currentHomeTeamNotes, mMatch.homeTeamNotes) ||
                    !areEqual(currentAwayTeamNotes.equals("")? null: currentAwayTeamNotes, mMatch.awayTeamNotes)) {
                etHomeTeamGoals.setTextColor(COLOR_CORAL_RED);
                etAwayTeamGoals.setTextColor(COLOR_CORAL_RED);
                etHomeTeamNotes.setTextColor(COLOR_CORAL_RED);
                etAwayTeamNotes.setTextColor(COLOR_CORAL_RED);
                btSetResult.setEnabled(true);
            }
            else {
                etHomeTeamGoals.setTextColor(COLOR_BLACK);
                etAwayTeamGoals.setTextColor(COLOR_BLACK);
                etHomeTeamNotes.setTextColor(COLOR_BLACK);
                etAwayTeamNotes.setTextColor(COLOR_BLACK);
                btSetResult.setEnabled(false);
            }
        }

        interface OnButtonClickedListener {
            void onClick(int position, String homeValue, String awayValue, String homeTeamNotes, String awayTeamNotes);
        }
    }

    public void setOnButtonClickedListener(OnButtonClickedListener listener) {
        mListener = listener;
    }

    public interface OnButtonClickedListener {
        void onClick(Match match);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean areEqual(String obj1, String obj2) {
        if (obj1 == null && obj2 == null)
            return true;
        if (obj1 != null && obj2 != null)
            return obj1.equals(obj2);

        return false;
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
