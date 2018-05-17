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
import org.hugoandrade.euro2016.predictor.data.LeagueWrapper;
import org.hugoandrade.euro2016.predictor.data.raw.LeagueUser;
import org.hugoandrade.euro2016.predictor.data.raw.User;

public class LeagueStandingFullListAdapter extends RecyclerView.Adapter<LeagueStandingFullListAdapter.ViewHolder> {

    private LeagueWrapper mLeagueWrapper;
    private boolean containsSelf;

    private OnLeagueStandingClicked mListener;

    public LeagueStandingFullListAdapter(LeagueWrapper leagueWrapper) {
        mLeagueWrapper = leagueWrapper;
        containsSelf = doesItContainSelf();
    }

    @NonNull
    @Override
    public LeagueStandingFullListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_league_standings_full, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        LeagueUser user;
        if (position < mLeagueWrapper.getLeagueUserList().size()) {
            user = mLeagueWrapper.getLeagueUserList().get(position);
        }
        else {
            user = mLeagueWrapper.getMainUser();
            if (user == null) user = new LeagueUser(GlobalData.getInstance().user, position + 1);
            else android.util.Log.e(getClass().getSimpleName(), "FAILED MAIN USER!!");
        }

        holder.tvPosition.setText(String.valueOf(user.getRank()));
        holder.tvUser.setText(user.getUser().getEmail());
        holder.tvPoints.setText(String.valueOf(user.getUser().getScore()));

        if (GlobalData.getInstance().user.getID().equals(user.getUser().getID())) {
            holder.container.setBackgroundColor(Color.parseColor("#6626629e"));
        }
        else {
            holder.container.setBackgroundColor(Color.TRANSPARENT);
        }

        if (mLeagueWrapper.getLeague().getNumberOfMembers() == mLeagueWrapper.getLeagueUserList().size()) {
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

        return mLeagueWrapper.getLeagueUserList().size() + (containsSelf ? 0 : 1);
    }

    public void setOnLeagueStandingClicked(OnLeagueStandingClicked listener) {
        mListener = listener;
    }

    private boolean doesItContainSelf() {

        for (LeagueUser user : mLeagueWrapper.getLeagueUserList()) {
            if (GlobalData.getInstance().user.getID().equals(user.getUser().getID())) {
                return true;
            }
        }
        return false;
    }

    public void set(LeagueWrapper leagueWrapper) {
        mLeagueWrapper = leagueWrapper;
        containsSelf = doesItContainSelf();
    }

    public LeagueWrapper get() {
        return mLeagueWrapper ;
    }

    public void updateMoreButton() {
        containsSelf = doesItContainSelf();
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
                    if (getAdapterPosition() < mLeagueWrapper.getLeagueUserList().size()) {
                        user = mLeagueWrapper.getLeagueUserList().get(getAdapterPosition()).getUser();
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