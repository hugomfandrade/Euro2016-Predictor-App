package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
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

import java.util.List;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.utils.ViewUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

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
    public void onBindViewHolder(@NonNull final KnockoutListAdapter.ViewHolder holder, int position) {
        final Match match = mMatchList.get(holder.getAdapterPosition());

        holder.tvHomeTeam.setText(match.getHomeTeamName());
        holder.tvAwayTeam.setText(match.getAwayTeamName());
        holder.ivHomeTeam.setImageResource(Country.getImageID(match.getHomeTeam()));
        holder.ivAwayTeam.setImageResource(Country.getImageID(match.getAwayTeam()));
        holder.tvHomeTeamGoals.setText(
                (match.getHomeTeamGoals() == -1)? "" :
                        ((match.getHomeTeamNotes() == null ? "" : match.getHomeTeamNotes())
                                + String.valueOf(match.getHomeTeamGoals())));
        holder.tvAwayTeamGoals.setText(
                (match.getAwayTeamGoals() == -1)? "" :
                        (String.valueOf(match.getAwayTeamGoals())
                                + (match.getAwayTeamNotes() == null ? "" : match.getAwayTeamNotes())));
        holder.tvDateAndTime.setText(
                DateFormat.format("dd-MM-yyyy HH:mm", match.getDateAndTime()).toString());

        holder.tvMatchNumber.setText(TextUtils.concat("Match number: ", String.valueOf(match.getMatchNumber())));
        holder.tvStage.setText(
                String.format("%s%s", match.getStage(), match.getGroup() == null ? "" : (" - " + match.getGroup())));
        holder.tvStadium.setText(match.getStadium());


        holder.tvHomeTeam.setTypeface(null, Typeface.NORMAL);
        holder.tvAwayTeam.setTypeface(null, Typeface.NORMAL);
        if (match.getHomeTeamGoals() != -1 && match.getAwayTeamGoals() != -1) {
            if (match.getHomeTeamGoals() == match.getAwayTeamGoals()) {
                if (match.getHomeTeamNotes() != null && match.getAwayTeamNotes() == null)
                    holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
                else if (match.getHomeTeamNotes() == null && match.getAwayTeamNotes() != null)
                    holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
            }
            else if (match.getHomeTeamGoals() > match.getAwayTeamGoals())
                holder.tvHomeTeam.setTypeface(null, Typeface.BOLD);
            else if (match.getHomeTeamGoals() < match.getAwayTeamGoals())
                holder.tvAwayTeam.setTypeface(null, Typeface.BOLD);
        }

        boolean hasHomeCountryFlag = Country.getImageID(match.getHomeTeam()) != 0;
        boolean hasAwayCountryFlag = Country.getImageID(match.getAwayTeam()) != 0;
        ((View) holder.ivHomeTeam.getParent()).setVisibility(hasHomeCountryFlag ? VISIBLE : GONE);
        ((View) holder.ivAwayTeam.getParent()).setVisibility(hasAwayCountryFlag ? VISIBLE : GONE);
        holder.tvHomeTeam.setGravity(hasHomeCountryFlag ? Gravity.TOP | Gravity.CENTER_HORIZONTAL : Gravity.CENTER);
        holder.tvAwayTeam.setGravity(hasAwayCountryFlag ? Gravity.TOP | Gravity.CENTER_HORIZONTAL : Gravity.CENTER);
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

        TextView tvDateAndTime;
        TextView tvHomeTeam;
        TextView tvAwayTeam;
        ImageView ivHomeTeam;
        ImageView ivAwayTeam;
        TextView tvHomeTeamGoals;
        TextView tvAwayTeamGoals;

        // info
        ImageView ivInfo;

        // details
        View detailsInfoContainer;
        TextView tvMatchNumber;
        TextView tvStadium;
        TextView tvStage;

        @SuppressLint("ClickableViewAccessibility")
        ViewHolder(View itemView) {
            super(itemView);

            tvDateAndTime = itemView.findViewById(R.id.tv_match_date_time);
            tvHomeTeam = itemView.findViewById(R.id.tv_match_home_team);
            tvAwayTeam = itemView.findViewById(R.id.tv_match_away_team);
            ivHomeTeam = itemView.findViewById(R.id.iv_match_home_team);
            ivAwayTeam = itemView.findViewById(R.id.iv_match_away_team);
            tvHomeTeamGoals = itemView.findViewById(R.id.tv_home_team_goals);
            tvAwayTeamGoals = itemView.findViewById(R.id.tv_away_team_goals);

            ivInfo = itemView.findViewById(R.id.iv_info);
            tvStadium = itemView.findViewById(R.id.tv_match_stadium);
            tvStage = itemView.findViewById(R.id.tv_stage);
            detailsInfoContainer = itemView.findViewById(R.id.viewGroup_info_details_container);
            ivInfo.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            detailsInfoContainer.setVisibility(VISIBLE);
                            break;

                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            detailsInfoContainer.setVisibility(View.INVISIBLE);
                            break;
                    }

                    return true;
                }
            });
            tvMatchNumber = itemView.findViewById(R.id.tv_match_number);
        }
    }
}