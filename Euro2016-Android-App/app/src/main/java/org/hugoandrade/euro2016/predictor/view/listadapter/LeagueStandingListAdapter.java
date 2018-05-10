package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.League;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.User;

import java.util.ArrayList;
import java.util.List;

public class LeagueStandingListAdapter extends RecyclerView.Adapter<LeagueStandingListAdapter.ViewHolder> {

    private static final int MAX_NUMBER_OF_ROWS = 5;

    private List<LeagueUser> mUserList;
    private LeagueUser mMainUser;

    public LeagueStandingListAdapter() {
        mUserList = new ArrayList<>();
    }

    @NonNull
    @Override
    public LeagueStandingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_league_standings, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final LeagueStandingListAdapter.ViewHolder holder, int position) {
        LeagueUser user = mUserList.get(position);

        if ((position + 1) == MAX_NUMBER_OF_ROWS && !doesItContainSelf()) {
            user = mMainUser;
            if (user == null) user = new LeagueUser(GlobalData.getInstance().user, position + 1);
        }

        holder.tvPosition.setText(String.valueOf(user.getRank()));
        holder.tvUser.setText(user.getUser().getEmail());
        holder.tvPoints.setText(String.valueOf(user.getUser().getScore()));

        if (GlobalData.getInstance().user.getID().equals(user.getUser().getID())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#6626629e"));
        }
        else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private boolean doesItContainSelf() {

        for (int i = 0 ; i < mUserList.size() && i < MAX_NUMBER_OF_ROWS ; i++) {
            LeagueUser user = mUserList.get(i);
            if (GlobalData.getInstance().user.getID().equals(user.getUser().getID())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mUserList.size() < MAX_NUMBER_OF_ROWS ? mUserList.size() : MAX_NUMBER_OF_ROWS;
    }

    public void set(List<LeagueUser> userList) {
        mUserList = userList;
    }

    public void setMainUser(LeagueUser mainUser) {
        mMainUser = mainUser;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPosition;
        TextView tvUser;
        TextView tvPoints;

        ViewHolder(View itemView) {
            super(itemView);

            tvPosition = itemView.findViewById(R.id. tv_position);
            tvUser = itemView.findViewById(R.id.tv_name);
            tvPoints = itemView.findViewById(R.id.tv_points);
        }
    }
}