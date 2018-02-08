package org.hugoandrade.euro2016.predictor.view.listadapter;

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

import java.util.List;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.Country;
import org.hugoandrade.euro2016.predictor.data.Match;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;

public class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {

    private List<Match> mMatchList;

    private static final String TEMPLATE = "dd-MM-yyyy HH:mm";

    public MatchListAdapter(List<Match> matchList) {
        mMatchList = matchList;
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
        Match match = mMatchList.get(position);

        holder.tvMatchNo.setText(String.valueOf(match.getMatchNumber()));
        holder.tvHomeTeam.setText(match.getHomeTeamName());
        holder.tvAwayTeam.setText(match.getAwayTeamName());
        holder.ivHomeTeam.setImageResource(Country.getImageID(match.getHomeTeam()));
        holder.ivAwayTeam.setImageResource(Country.getImageID(match.getAwayTeam()));
        holder.etHomeTeamGoals.setText(MatchUtils.getAsString(match.getHomeTeamNotes()));
        holder.etHomeTeamGoals.append(MatchUtils.getAsString(match.getHomeTeamGoals()));
        holder.etAwayTeamGoals.setText(MatchUtils.getAsString(match.getAwayTeamGoals()));
        holder.etAwayTeamGoals.append(MatchUtils.getAsString(match.getAwayTeamNotes()));
        holder.tvStadium.setText(match.getStadium());
        holder.tvStageAndGroup.setText(
                String.format("%s%s", match.getStage(), match.getGroup() == null ? "" : (" - " + match.getGroup())));
        holder.tvDateAndTime.setText(DateFormat.format(TEMPLATE, match.getDateAndTime()).toString());

        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);

        if (MatchUtils.isMatchPlayed(match)) {
            if (MatchUtils.didHomeTeamWin(match))
                holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
            if (MatchUtils.didAwayTeamWin(match))
                holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return mMatchList.size();
    }

    public void set(List<Match> matchList) {
        mMatchList = matchList;
        notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void updateMatch(Match match) {
        for (int i = 0; i < mMatchList.size() ; i++)
            if (mMatchList.get(i).getMatchNumber() == match.getMatchNumber()) {
                mMatchList.set(i, match);
                notifyItemChanged(i);
                break;
            }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMatchNo;
        TextView tvHomeTeam;
        TextView tvAwayTeam;
        TextView tvStageAndGroup;
        TextView tvDateAndTime;
        TextView tvStadium;
        EditText etHomeTeamGoals;
        EditText etAwayTeamGoals;
        ImageView ivHomeTeam;
        ImageView ivAwayTeam;

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
