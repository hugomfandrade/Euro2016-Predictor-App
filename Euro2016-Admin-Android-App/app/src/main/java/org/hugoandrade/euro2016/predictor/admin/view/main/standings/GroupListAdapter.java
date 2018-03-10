package org.hugoandrade.euro2016.predictor.admin.view.main.standings;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.admin.R;
import org.hugoandrade.euro2016.predictor.admin.data.Country;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    @SuppressWarnings("unused") private static final String TAG = GroupListAdapter.class.getSimpleName();

    private List<Country> mCountryList;

    public GroupListAdapter(List<Country> countryList) {
        mCountryList = countryList;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new ViewHolder(vi.inflate(R.layout.list_item_group, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Country country = mCountryList.get(position);

        holder.tvPosition.setText(String.valueOf(country.getPosition()));
        holder.tvCountryName.setText(country.getName());
        holder.tvVictories.setText(String.valueOf(country.getVictories()));
        holder.tvDraws.setText(String.valueOf(country.getDraws()));
        holder.tvDefeats.setText(String.valueOf(country.getDefeats()));
        holder.tvGoalsFor.setText(String.valueOf(country.getGoalsFor()));
        holder.tvGoalsAgainst.setText(String.valueOf(country.getGoalsAgainst()));
        holder.tvGoalsDifference.setText(String.valueOf(country.getGoalsDifference()));
        holder.tvPoints.setText(String.valueOf(country.getPoints()));
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

    public void set(@NonNull List<Country> countryList) {
        mCountryList = countryList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPosition;
        TextView tvCountryName;
        TextView tvVictories;
        TextView tvDraws;
        TextView tvDefeats;
        TextView tvGoalsFor;
        TextView tvGoalsAgainst;
        TextView tvGoalsDifference;
        TextView tvPoints;

        ViewHolder(View view) {
            super(view);

            tvPosition = (TextView) view.findViewById(R.id.tv_country_position);
            tvCountryName = (TextView) view.findViewById(R.id.tv_country_name);
            tvVictories = (TextView) view.findViewById(R.id.tv_country_victories);
            tvDraws = (TextView) view.findViewById(R.id.tv_country_draws);
            tvDefeats = (TextView) view.findViewById(R.id.tv_country_defeats);
            tvGoalsFor = (TextView) view.findViewById(R.id.tv_country_goals_for);
            tvGoalsAgainst = (TextView) view.findViewById(R.id.tv_country_goals_against);
            tvGoalsDifference = (TextView) view.findViewById(R.id.tv_country_goals_difference);
            tvPoints = (TextView) view.findViewById(R.id.tv_country_points);
        }
    }
}
