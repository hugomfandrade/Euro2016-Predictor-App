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
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.Match;
import hugoandrade.euro2016.object.Prediction;

public class KnockoutListAdapter extends RecyclerView.Adapter<KnockoutListAdapter.ViewHolder> {

    public final String TAG = getClass().getSimpleName();

    private final ArrayList<Match> mMatchesList = new ArrayList<>();

    public KnockoutListAdapter(ArrayList<Match> objects) {
        this.mMatchesList.clear();
        if (objects != null)
            this.mMatchesList.addAll(objects);
    }

    @Override
    public KnockoutListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new KnockoutListAdapter.ViewHolder(
                vi.inflate(R.layout.list_item_knockout, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final KnockoutListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Match mMatchInfo = mMatchesList.get(holder.getAdapterPosition());
        holder.tvHomeTeam.setText(mMatchInfo.homeTeam);
        holder.tvAwayTeam.setText(mMatchInfo.awayTeam);
        holder.ivHomeTeam.setImageResource(mMatchInfo.homeTeamImageID);
        holder.ivAwayTeam.setImageResource(mMatchInfo.awayTeamImageID);
        holder.etHomeTeamGoalsScored.setText(
                (mMatchInfo.homeTeamGoals == -1)? "" :
                        (mMatchInfo.homeTeamNotes + String.valueOf(mMatchInfo.homeTeamGoals)));
        holder.etAwayTeamGoalsScored.setText(
                (mMatchInfo.awayTeamGoals == -1)? "" :
                        (String.valueOf(mMatchInfo.awayTeamGoals) + mMatchInfo.awayTeamNotes));
        holder.tvStadium.setText(mMatchInfo.stadium);
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
        return mMatchesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setAllMatches(ArrayList<Match> matchesList) {
        synchronized (this) {
            if (mMatchesList.size() == 0 || mMatchesList.size() != matchesList.size()) {
                mMatchesList.clear();
                mMatchesList.addAll(matchesList);
                notifyDataSetChanged();
            } else {
                for (int i = 0; i < mMatchesList.size(); i++)
                    if (!mMatchesList.get(i).equals(matchesList.get(i))) {
                        mMatchesList.set(i, matchesList.get(i));
                        notifyItemChanged(i);
                    }
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvHomeTeam, tvAwayTeam, tvStadium, tvDateAndTime;
        EditText etHomeTeamGoalsScored, etAwayTeamGoalsScored;
        ImageView ivHomeTeam, ivAwayTeam;

        ViewHolder(View itemView) {
            super(itemView);

            tvHomeTeam = (TextView) itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = (TextView) itemView.findViewById(R.id.tv_match_away_team);
            ivHomeTeam = (ImageView) itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = (ImageView) itemView.findViewById(R.id.iv_match_away_team);
            etHomeTeamGoalsScored = (EditText) itemView.findViewById(R.id.ed_match_home_team_goals);
            etAwayTeamGoalsScored = (EditText) itemView.findViewById(R.id.ed_match_away_team_goals);
            tvDateAndTime = (TextView) itemView.findViewById(R.id.tv_date_time);
            tvStadium = (TextView)  itemView.findViewById(R.id.tv_stadium);
        }
    }
}