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

public class LeagueStandingFullListAdapter extends RecyclerView.Adapter<LeagueStandingFullListAdapter.ViewHolder> {

    private List<User> mUserList;
    private List<Integer> mPositionList;
    private boolean containsSelf = false;
    private boolean mMoreButtonEnabled;

    private OnLeagueStandingClicked mListener;

    public LeagueStandingFullListAdapter() {
        mUserList = new ArrayList<>();
    }

    @NonNull
    @Override
    public LeagueStandingFullListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_league_standings_full, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        User user;
        if (position < mUserList.size()) {
            user = mUserList.get(position);
        }
        else {
            user = GlobalData.getInstance().user;
        }

        holder.tvPosition.setText(String.valueOf(mPositionList.get(holder.getAdapterPosition())));
        holder.tvUser.setText(user.getEmail());
        holder.tvPoints.setText(String.valueOf(user.getScore()));

        if (GlobalData.getInstance().user.getID().equals(user.getID())) {
            holder.container.setBackgroundColor(Color.parseColor("#6626629e"));
        }
        else {
            holder.container.setBackgroundColor(Color.TRANSPARENT);
        }

        if (!mMoreButtonEnabled) {
            holder.ivMore.setVisibility(View.GONE);
        }
        else {
            holder.ivMore.setVisibility(position == (getItemCount() - 1) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {

        return mUserList.size() + (containsSelf ? 0 : 1);
    }

    public void setOnLeagueStandingClicked(OnLeagueStandingClicked listener) {
        mListener = listener;
    }

    private boolean doesItContainSelf() {

        for (User user : mUserList) {
            if (GlobalData.getInstance().user.getID().equals(user.getID())) {
                return true;
            }
        }
        return false;
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
        containsSelf = doesItContainSelf();

        if (!containsSelf) {
            mPositionList.add(mPositionList.size() + 1);
        }
    }

    public void disableMoreButton() {
        mMoreButtonEnabled = false;
    }

    public interface OnLeagueStandingClicked {
        void onUserSelected(User user);
        void onMoreClicked();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View container;
        View innerContainer;
        TextView tvPosition;
        TextView tvUser;
        TextView tvPoints;
        ImageView ivMore;

        ViewHolder(View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.standing_container);
            innerContainer = itemView.findViewById(R.id.standing_inner_container);
            innerContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user;
                    if (getAdapterPosition() < mUserList.size()) {
                        user = mUserList.get(getAdapterPosition());
                    }
                    else {
                        user = GlobalData.getInstance().user;
                    }
                    if (mListener != null)
                        mListener.onUserSelected(user);
                }
            });
            tvPosition = itemView.findViewById(R.id. tv_position);
            tvUser = itemView.findViewById(R.id.tv_name);
            tvPoints = itemView.findViewById(R.id.tv_points);
            ivMore = itemView.findViewById(R.id.iv_more);
            ivMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onMoreClicked();
                }
            });
        }
    }
}