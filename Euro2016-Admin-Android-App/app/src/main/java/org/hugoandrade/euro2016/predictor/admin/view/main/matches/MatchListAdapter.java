package org.hugoandrade.euro2016.predictor.admin.view.main.matches;

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
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.utils.MatchUtils;

import java.util.ArrayList;
import java.util.List;

public class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {

    private static int COLOR_CORAL_RED = Color.parseColor("#ffff4444");
    private static int COLOR_BLACK = Color.parseColor("#ff000000");

    private List<InputMatch> mInputMatchList;

    private OnSetButtonClickListener mListener;

    public MatchListAdapter(List<Match> matchList) {
        set(matchList);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_match, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InputMatch inputMatch = mInputMatchList.get(holder.getAdapterPosition());
        Match match = inputMatch.mMatch;
        boolean isEnabled = inputMatch.mIsEnabled;

        holder.tvMatchNo.setText(String.valueOf(match.getMatchNumber()));
        holder.tvHomeTeam.setText(match.getHomeTeamName());
        holder.tvAwayTeam.setText(match.getAwayTeamName());
        holder.etHomeTeamGoals.setText(inputMatch.mHomeTeamGoals);
        holder.etAwayTeamGoals.setText(inputMatch.mAwayTeamGoals);
        holder.etHomeTeamNotes.setText(inputMatch.mHomeTeamNotes);
        holder.etAwayTeamNotes.setText(inputMatch.mAwayTeamNotes);

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
        return mInputMatchList.size();
    }

    public void set(List<Match> matchList) {
        mInputMatchList = new ArrayList<>();
        for (int i = 0 ; i < matchList.size(); i++) {
            mInputMatchList.add(new InputMatch(matchList.get(i)));
        }
    }

    public void updateMatch(Match match) {
        for (int i = 0; i < mInputMatchList.size() ; i++)
            if (mInputMatchList.get(i).mMatch.getID().equals(match.getID())) {
                mInputMatchList.set(i, new InputMatch(match));
                notifyItemChanged(i);
                break;
            }
    }

    public void updateFailedMatch(Match match) {
        for (int i = 0; i < mInputMatchList.size() ; i++)
            if (mInputMatchList.get(i).mMatch.getID().equals(match.getID())) {
                mInputMatchList.get(i).mIsEnabled = true;
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

        ViewHolder(View itemView) {
            super(itemView);

            tvMatchNo = (TextView) itemView.findViewById(R.id.tv_match_no);
            tvHomeTeam = (TextView) itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = (TextView) itemView.findViewById(R.id.tv_match_away_team);
            etHomeTeamGoals = (EditText) itemView.findViewById(R.id.et_match_home_team_goals);
            etAwayTeamGoals = (EditText) itemView.findViewById(R.id.et_match_away_team_goals);
            etHomeTeamNotes = (EditText) itemView.findViewById(R.id.ed_match_home_team_notes);
            etAwayTeamNotes = (EditText) itemView.findViewById(R.id.et_match_away_team_notes);

            progressBar = itemView.findViewById(R.id.progressBar_waiting_for_response);
            btSetResult = (Button) itemView.findViewById(R.id.bt_set_match);
            btSetResult.setOnClickListener(this);

            etHomeTeamGoals.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    mInputMatchList.get(getAdapterPosition()).mHomeTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamGoals.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    mInputMatchList.get(getAdapterPosition()).mAwayTeamGoals = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etHomeTeamNotes.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    mInputMatchList.get(getAdapterPosition()).mHomeTeamNotes = s.toString();

                    checkIfThereAreNewValues();
                }
            });
            etAwayTeamNotes.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    mInputMatchList.get(getAdapterPosition()).mAwayTeamNotes = s.toString();

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
                mInputMatchList.get(getAdapterPosition()).mIsEnabled = false;
                Match match = Match.instance(mInputMatchList.get(getAdapterPosition()).mMatch);
                InputMatch inputMatch = mInputMatchList.get(getAdapterPosition());
                match.setHomeTeamGoals(MatchUtils.getInt(inputMatch.mHomeTeamGoals));
                match.setAwayTeamGoals(MatchUtils.getInt(inputMatch.mAwayTeamGoals));
                match.setHomeTeamNotes(MatchUtils.getString(inputMatch.mHomeTeamNotes.trim()));
                match.setAwayTeamNotes(MatchUtils.getString(inputMatch.mAwayTeamNotes.trim()));
                notifyItemChanged(getAdapterPosition());
                mListener.onClick(match);
            }
        }

        private void checkIfThereAreNewValues() {
            boolean isEnabled = mInputMatchList.get(getAdapterPosition()).haveValuesChanged();
            int color = isEnabled? COLOR_CORAL_RED : COLOR_BLACK;

            etHomeTeamGoals.setTextColor(color);
            etAwayTeamGoals.setTextColor(color);
            etHomeTeamNotes.setTextColor(color);
            etAwayTeamNotes.setTextColor(color);
            btSetResult.setEnabled(isEnabled);
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

    static class InputMatch {

        Match mMatch;
        String mHomeTeamGoals;
        String mAwayTeamGoals;
        String mHomeTeamNotes;
        String mAwayTeamNotes;
        boolean mIsEnabled;

        InputMatch(Match match) {
            mMatch = match;
            mHomeTeamGoals = MatchUtils.getAsString(match.getHomeTeamGoals());
            mAwayTeamGoals = MatchUtils.getAsString(match.getAwayTeamGoals());
            mHomeTeamNotes = MatchUtils.getAsString(match.getHomeTeamNotes());
            mAwayTeamNotes = MatchUtils.getAsString(match.getAwayTeamNotes());
            mIsEnabled = true;
        }

        boolean haveValuesChanged() {
            return MatchUtils.getInt(mHomeTeamGoals) != mMatch.getHomeTeamGoals() ||
                    MatchUtils.getInt(mAwayTeamGoals) != mMatch.getAwayTeamGoals() ||
                    !areEqual(mHomeTeamNotes, mMatch.getHomeTeamNotes()) ||
                    !areEqual(mAwayTeamNotes, mMatch.getAwayTeamNotes());
        }

        private static boolean areEqual(String obj1, String obj2) {
            return isNullOrEmpty(obj1) && isNullOrEmpty(obj2) || obj1.equals(obj2);
        }

        private static boolean isNullOrEmpty(String obj1) {
            return obj1 == null || obj1.isEmpty();
        }
    }
}
