package hugoandrade.euro2016.view.listadapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016.R;
import hugoandrade.euro2016.object.Match;

public class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {

    @SuppressWarnings("unused") private final static String TAG = MatchListAdapter.class.getSimpleName();

    private final ArrayList<Match> mAllMatchesList = new ArrayList<>();

    public MatchListAdapter(ArrayList<Match> allMatchesList) {
        this.mAllMatchesList.clear();
        this.mAllMatchesList.addAll(allMatchesList);
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
    public void onBindViewHolder(final ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Match mMatchInfo = mAllMatchesList.get(position);

        holder.tvMatchNo.setText(String.valueOf(mMatchInfo.matchNo));
        holder.tvHomeTeam.setText(mMatchInfo.homeTeam);
        holder.tvAwayTeam.setText(mMatchInfo.awayTeam);
        holder.ivHomeTeam.setImageResource(mMatchInfo.homeTeamImageID);
        holder.ivAwayTeam.setImageResource(mMatchInfo.awayTeamImageID);
        holder.etHomeTeamGoals.setText(
                (mMatchInfo.homeTeamGoals == -1)? "" :
                        (((mMatchInfo.homeTeamNotes == null)? "" : mMatchInfo.homeTeamNotes)
                                + String.valueOf(mMatchInfo.homeTeamGoals)));
        holder.etAwayTeamGoals.setText(
                (mMatchInfo.awayTeamGoals == -1)? "" :
                        (String.valueOf(mMatchInfo.awayTeamGoals) +
                                ((mMatchInfo.awayTeamNotes == null)? "" : mMatchInfo.awayTeamNotes)));
        holder.tvStadium.setText(mMatchInfo.stadium);
        holder.tvStageAndGroup.setText(
                String.format("%s%s", mMatchInfo.stage, mMatchInfo.group == null ? "" : (" - " + mMatchInfo.group)));
        holder.tvDateAndTime.setText(
                DateFormat.format("dd-MM-yyyy HH:mm", mMatchInfo.dateAndTime).toString());

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
                    }

            }
        }
    }

    @SuppressWarnings("unused")
    public void updateMatch(Match match) {
        for (int i = 0 ; i < mAllMatchesList.size() ; i++)
            if (mAllMatchesList.get(i).matchNo == match.matchNo) {
                mAllMatchesList.set(i, match);
                notifyItemChanged(i);
                break;
            }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMatchNo, tvHomeTeam, tvAwayTeam, tvStageAndGroup, tvDateAndTime, tvStadium;
        EditText etHomeTeamGoals, etAwayTeamGoals;
        ImageView ivHomeTeam, ivAwayTeam;

        ViewHolder(View itemView) {
            super(itemView);

            tvMatchNo = (TextView) itemView.findViewById(R.id.tv_match_no);
            tvHomeTeam = (TextView) itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = (TextView) itemView.findViewById(R.id.tv_match_away_team);
            ivHomeTeam = (ImageView) itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = (ImageView) itemView.findViewById(R.id.iv_match_away_team);
            tvStageAndGroup = (TextView) itemView.findViewById(R.id.tv_stage_group);
            tvDateAndTime = (TextView) itemView.findViewById(R.id.tv_match_date_time);
            tvStadium = (TextView) itemView.findViewById(R.id.tv_match_stadium);
            etHomeTeamGoals = (EditText) itemView.findViewById(R.id.et_match_home_team_goals);
            etAwayTeamGoals = (EditText) itemView.findViewById(R.id.et_match_away_team_goals);

        }
    }
}
