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
import org.hugoandrade.euro2016.predictor.data.raw.User;

import java.util.ArrayList;
import java.util.List;

public class LeagueStandingListAdapter extends RecyclerView.Adapter<LeagueStandingListAdapter.ViewHolder> {

    private static final int MAX_NUMBER_OF_ROWS = 4;

    private List<User> mUserList;
    private List<Integer> mPositionList;

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
        User user = mUserList.get(position);

        holder.tvPosition.setText(String.valueOf(mPositionList.get(holder.getAdapterPosition())));
        holder.tvUser.setText(user.getEmail());
        holder.tvPoints.setText(String.valueOf(user.getScore()));

        if (GlobalData.getInstance().user.getID().equals(user.getID())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#6626629e"));
        }
        else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mUserList.size() < MAX_NUMBER_OF_ROWS ? mUserList.size() : MAX_NUMBER_OF_ROWS;
    }

    public void set(List<User> userList) {
        mUserList = userList;
        mPositionList = new ArrayList<>();
        for (int i = 0 ; i < mUserList.size() ; i++) {
            if (i == 0) {
                mPositionList.add(i + 1);
            }
            else {
                if (mUserList.get(i).getScore() == mUserList.get(i - 1).getScore()) {
                    mPositionList.add(mPositionList.get(i - 1));
                }
                else {
                    mPositionList.add(i + 1);
                }
            }
        }
        notifyDataSetChanged();
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