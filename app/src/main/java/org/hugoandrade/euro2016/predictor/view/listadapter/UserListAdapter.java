package org.hugoandrade.euro2016.predictor.view.listadapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private List<User> mUserList;

    private OnItemClickListener mListener;

    public UserListAdapter(List<User> userList) {
        mUserList = userList;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(final UserListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        User user = mUserList.get(position);

        holder.tvUser.setText(user.getEmail());
        holder.tvPoints.setText(String.valueOf(user.getScore()));
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public void set(List<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
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

        ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            tvUser = (TextView) itemView.findViewById(R.id.tv_user);
            tvPoints = (TextView) itemView.findViewById(R.id.tv_points);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onItemClick(mUserList.get(getAdapterPosition()));
        }
    }
}