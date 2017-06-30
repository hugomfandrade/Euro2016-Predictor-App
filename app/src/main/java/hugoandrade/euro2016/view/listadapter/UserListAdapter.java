package hugoandrade.euro2016.view.listadapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016.R;
import hugoandrade.euro2016.object.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    @SuppressWarnings("unused") private final String TAG = getClass().getSimpleName();

    private final ArrayList<User> allUsersList = new ArrayList<>();
    private OnItemClickListener mListener;

    public UserListAdapter(ArrayList<User> objects) {
        allUsersList.clear();
        allUsersList.addAll(objects);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new UserListAdapter.ViewHolder(vi.inflate(R.layout.list_item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(final UserListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final User mUserInfo = allUsersList.get(position);

        holder.tvUser.setText(mUserInfo.username);
        holder.tvPoints.setText(String.valueOf(mUserInfo.score));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onItemClick(mUserInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allUsersList.size();
    }

    public void setAllUsers(ArrayList<User> usersList) {
        synchronized (this) {
            if (allUsersList.size() == 0 || allUsersList.size() != usersList.size()) {
                allUsersList.clear();
                allUsersList.addAll(usersList);
                notifyDataSetChanged();
            } else {
                for (int i = 0; i < allUsersList.size(); i++)
                    if (!allUsersList.get(i).equals(usersList.get(i))) {
                        allUsersList.set(i, usersList.get(i));
                        notifyItemChanged(i);
                    }
            }
        }
    }

    public void setOnItemClickListener(UserListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUser, tvPoints;

        ViewHolder(View itemView) {
            super(itemView);

            tvUser = (TextView) itemView.findViewById(R.id.tv_user);
            tvPoints = (TextView) itemView.findViewById(R.id.tv_points);
        }
    }
}