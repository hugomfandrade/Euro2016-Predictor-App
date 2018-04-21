package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private static final int COLOR_INCORRECT_PREDICTION = Color.parseColor("#aaff0000");
    private static final int COLOR_CORRECT_OUTCOME = Color.parseColor("#aaFF5500");
    private static final int COLOR_CORRECT_MARGIN_OF_VICTORY = Color.parseColor("#aaAAAA00");
    private static final int COLOR_CORRECT_PREDICTION = Color.parseColor("#aa00AA00");

    private List<User> mUserList;

    private OnItemClickListener mListener;

    public UserListAdapter(List<User> userList) {
        mUserList = userList;
    }

    @NonNull
    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListAdapter.ViewHolder holder, int position) {
        User user = mUserList.get(position);

        int[] points = GlobalData.getInstance().getLatestPerformance(user);

        for (int i = 0 ; i < holder.tvLatestPerformances.length ; i++) {
            TextView tv = holder.tvLatestPerformances[i];
            if (points.length <= i) {
                tv.setVisibility(View.INVISIBLE);
            }
            else {
                tv.setVisibility(View.VISIBLE);
                tv.setBackgroundColor(getCardColor(points[i]));
                tv.setText(String.valueOf(points[i]));
            }
        }

        holder.tvUser.setText(user.getEmail());
        holder.tvPoints.setText(String.valueOf(user.getScore()));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public void set(List<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    private int getCardColor(int points) {
        if (points == GlobalData.getInstance().systemData.getRules().getRuleCorrectMarginOfVictory()) {
            return COLOR_CORRECT_MARGIN_OF_VICTORY;
        }
        else if (points == GlobalData.getInstance().systemData.getRules().getRuleCorrectOutcome()) {
            return COLOR_CORRECT_OUTCOME;
        }
        else if (points == GlobalData.getInstance().systemData.getRules().getRuleCorrectPrediction()) {
            return COLOR_CORRECT_PREDICTION;
        }
        else {
            return COLOR_INCORRECT_PREDICTION;
        }
    }

    public void setOnItemClickListener(UserListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvUser;
        TextView tvPoints;
        TextView[] tvLatestPerformances;

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            tvUser = itemView.findViewById(R.id.tv_user);
            tvPoints = itemView.findViewById(R.id.tv_points);
            tvLatestPerformances = new TextView[5];
            tvLatestPerformances[0] = itemView.findViewById(R.id.tv_latest_performance_5);
            tvLatestPerformances[1] = itemView.findViewById(R.id.tv_latest_performance_4);
            tvLatestPerformances[2] = itemView.findViewById(R.id.tv_latest_performance_3);
            tvLatestPerformances[3] = itemView.findViewById(R.id.tv_latest_performance_2);
            tvLatestPerformances[4] = itemView.findViewById(R.id.tv_latest_performance_1);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onItemClick(mUserList.get(getAdapterPosition()));
        }
    }
}