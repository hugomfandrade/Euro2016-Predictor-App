package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
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
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;

public class KnockoutListAdapter extends RecyclerView.Adapter<KnockoutListAdapter.ViewHolder> {

    public final String TAG = getClass().getSimpleName();

    private List<Match> mMatchList;

    public KnockoutListAdapter(List<Match> matchList) {
        mMatchList = matchList;
    }

    @NonNull
    @Override
    public KnockoutListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_knockout, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final KnockoutListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Match mMatchInfo = mMatchList.get(holder.getAdapterPosition());
        holder.tvHomeTeam.setText(mMatchInfo.getHomeTeamName());
        holder.tvAwayTeam.setText(mMatchInfo.getAwayTeamName());
        holder.ivHomeTeam.setImageResource(Country.getImageID(mMatchInfo.getHomeTeam()));
        holder.ivAwayTeam.setImageResource(Country.getImageID(mMatchInfo.getAwayTeam()));
        holder.etHomeTeamGoalsScored.setText(
                (mMatchInfo.getHomeTeamGoals() == -1)? "" :
                        ((mMatchInfo.getHomeTeamNotes() == null ? "" : mMatchInfo.getHomeTeamNotes())
                                + String.valueOf(mMatchInfo.getHomeTeamGoals())));
        holder.etAwayTeamGoalsScored.setText(
                (mMatchInfo.getAwayTeamGoals() == -1)? "" :
                        (String.valueOf(mMatchInfo.getAwayTeamGoals())
                                + (mMatchInfo.getAwayTeamNotes() == null ? "" : mMatchInfo.getAwayTeamNotes())));
        holder.tvStadium.setText(mMatchInfo.getStadium());
        holder.tvDateAndTime.setText(
                DateFormat.format("dd-MM-yyyy HH:mm", mMatchInfo.getDateAndTime()).toString());

        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);
        if (mMatchInfo.getHomeTeamGoals() != -1 && mMatchInfo.getAwayTeamGoals() != -1) {
            if (mMatchInfo.getHomeTeamGoals() == mMatchInfo.getAwayTeamGoals()) {
                if (mMatchInfo.getHomeTeamNotes() != null && mMatchInfo.getAwayTeamNotes() == null)
                    holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
                else if (mMatchInfo.getHomeTeamNotes() == null && mMatchInfo.getAwayTeamNotes() != null)
                    holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
            }
            else if (mMatchInfo.getHomeTeamGoals() > mMatchInfo.getAwayTeamGoals())
                holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
            else if (mMatchInfo.getHomeTeamGoals() < mMatchInfo.getAwayTeamGoals())
                holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
        }
    }

    @Override
    public int getItemCount() {
        return mMatchList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void set(List<Match> matchList) {
        mMatchList = matchList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvHomeTeam;
        TextView tvAwayTeam;
        TextView tvStadium;
        TextView tvDateAndTime;
        EditText etHomeTeamGoalsScored;
        EditText etAwayTeamGoalsScored;
        ImageView ivHomeTeam;
        ImageView ivAwayTeam;

        ViewHolder(View itemView) {
            super(itemView);

            tvHomeTeam = itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = itemView.findViewById(R.id.tv_match_away_team);
            ivHomeTeam = itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = itemView.findViewById(R.id.iv_match_away_team);
            etHomeTeamGoalsScored = itemView.findViewById(R.id.ed_match_home_team_goals);
            etAwayTeamGoalsScored = itemView.findViewById(R.id.ed_match_away_team_goals);
            tvDateAndTime = itemView.findViewById(R.id.tv_date_time);
            tvStadium = itemView.findViewById(R.id.tv_stadium);
        }
    }
}