package org.hugoandrade.euro2016.predictor.admin.view.listadapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.admin.R;
import org.hugoandrade.euro2016.predictor.admin.object.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {

    private static int COLOR_CORAL_RED = Color.parseColor("#ffff4444");
    private static int COLOR_BLACK = Color.parseColor("#ff000000");

    private List<Match> mMatchList;
    private List<Boolean> mIsMatchEnabledList;
    private OnSetButtonClickListener mListener;

    public MatchListAdapter(List<Match> matchList) {
        mMatchList = matchList;
        mIsMatchEnabledList = new ArrayList<>();
        for (int i = 0 ; i < getItemCount(); i++)
            mIsMatchEnabledList.add(true);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_match, parent, false),
                              mMatchList.get(viewType));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Match match = mMatchList.get(holder.getAdapterPosition());
        boolean isEnabled = mIsMatchEnabledList.get(holder.getAdapterPosition());

        holder.tvMatchNo.setText(String.valueOf(match.getMatchNumber()));
        holder.tvHomeTeam.setText(match.getHomeTeamName());
        holder.tvAwayTeam.setText(match.getAwayTeamName());
        holder.etHomeTeamGoals.setText(holder.currentHomeTeamGoals);
        holder.etAwayTeamGoals.setText(holder.currentAwayTeamGoals);
        holder.etHomeTeamNotes.setText(holder.currentHomeTeamNotes);
        holder.etAwayTeamNotes.setText(holder.currentAwayTeamNotes);

        holder.etHomeTeamGoals.setEnabled(isEnabled);
        holder.etAwayTeamGoals.setEnabled(isEnabled);
        holder.etHomeTeamNotes.setEnabled(isEnabled);
        holder.etAwayTeamNotes.setEnabled(isEnabled);
        if (!isEnabled)
            holder.btSetResult.setEnabled(false);
        else
            holder.checkIfThereAreNewValues();
        holder.progressBar.setVisibility(isEnabled? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return mMatchList.size();
    }

    public void set(List<Match> matchList) {
        mMatchList = matchList;
        mIsMatchEnabledList = new ArrayList<>();
        for (int i = 0 ; i < getItemCount(); i++)
            mIsMatchEnabledList.add(true);
    }

    public void updateMatch(Match match) {
        for (int i = 0; i < mMatchList.size() ; i++)
            if (mMatchList.get(i).getID().equals(match.getID())) {
                mIsMatchEnabledList.set(i, true);
                notifyItemChanged(i);
                break;
            }
    }

    public void setOnSetButtonClickListener(OnSetButtonClickListener listener) {
        mListener = listener;
    }

    public interface OnSetButtonClickListener {
        void onClick(Match match);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvMatchNo;
        TextView tvHomeTeam;
        TextView tvAwayTeam;

        EditText etHomeTeamGoals;
        EditText etAwayTeamGoals;
        EditText etHomeTeamNotes;
        EditText etAwayTeamNotes;
        View progressBar;
        Button btSetResult;

        Match mMatch;

        String currentHomeTeamGoals;
        String currentHomeTeamNotes;
        String currentAwayTeamGoals;
        String currentAwayTeamNotes;

        boolean enableTextChangedListener = true;

        ViewHolder(View itemView, Match match) {
            super(itemView);
            mMatch = match;

            tvMatchNo = (TextView) itemView.findViewById(R.id.tv_match_no);
            tvHomeTeam = (TextView) itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = (TextView) itemView.findViewById(R.id.tv_match_away_team);
            etHomeTeamGoals = (EditText) itemView.findViewById(R.id.ed_match_home_team_goals);
            etAwayTeamGoals = (EditText) itemView.findViewById(R.id.ed_match_away_team_goals);
            etHomeTeamNotes = (EditText) itemView.findViewById(R.id.ed_match_home_team_notes);
            etAwayTeamNotes = (EditText) itemView.findViewById(R.id.ed_match_away_team_notes);

            progressBar = itemView.findViewById(R.id.progressBar_waiting_for_response);
            btSetResult = (Button) itemView.findViewById(R.id.bt_set_match);
            btSetResult.setOnClickListener(this);

            currentHomeTeamGoals = mMatch.getHomeTeamGoals() == -1?
                    "" : String.valueOf(mMatch.getHomeTeamGoals());
            currentAwayTeamGoals = mMatch.getAwayTeamGoals() == -1?
                    "" : String.valueOf(mMatch.getAwayTeamGoals());
            currentHomeTeamNotes = mMatch.getHomeTeamNotes() == null?
                    "" : mMatch.getHomeTeamNotes();
            currentAwayTeamNotes = mMatch.getAwayTeamNotes() == null?
                    "" : mMatch.getAwayTeamNotes();

            etHomeTeamGoals.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentHomeTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamGoals.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentAwayTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etHomeTeamNotes.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentHomeTeamNotes = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamNotes.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!enableTextChangedListener)
                        return;

                    currentAwayTeamNotes = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == EditorInfo.IME_ACTION_DONE) {
                        doClick();
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onClick(View v) {
            doClick();
        }

        private void doClick() {
            if (mListener != null) {
                mIsMatchEnabledList.set(getAdapterPosition(), false);
                Match match = mMatchList.get(getAdapterPosition());
                match.setHomeTeamGoals(getInt(currentHomeTeamGoals));
                match.setAwayTeamGoals(getInt(currentAwayTeamGoals));
                match.setHomeTeamNotes(getString(currentHomeTeamNotes.trim()));
                match.setAwayTeamNotes(getString(currentAwayTeamNotes.trim()));
                notifyItemChanged(getAdapterPosition());
                mListener.onClick(match);
            }
        }

        private void checkIfThereAreNewValues() {
            int color;
            boolean enabled;
            if (getInt(currentHomeTeamGoals) == mMatch.getHomeTeamGoals() &&
                    getInt(currentAwayTeamGoals) == mMatch.getAwayTeamGoals() &&
                    areEqual(currentHomeTeamNotes, mMatch.getHomeTeamNotes()) &&
                    areEqual(currentAwayTeamNotes, mMatch.getAwayTeamNotes())) {
                color = COLOR_BLACK;
                enabled = false;
            }
            else {
                color = COLOR_CORAL_RED;
                enabled = true;
            }
            etHomeTeamGoals.setTextColor(color);
            etAwayTeamGoals.setTextColor(color);
            etHomeTeamNotes.setTextColor(color);
            etAwayTeamNotes.setTextColor(color);
            btSetResult.setEnabled(enabled);
        }
    }

    private static boolean areEqual(String obj1, String obj2) {
        return isNullOrEmpty(obj1) && isNullOrEmpty(obj2) || obj1.equals(obj2);
    }

    private static boolean isNullOrEmpty(String obj1) {
        return obj1 == null || obj1.isEmpty();
    }

    private static String getString(String value) {
        return value.equals("") ? null: value;
    }

    private static int getInt(String value) {
        return getInt(value, -1);
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
}
