package hugoandrade.euro2016.view.listadapter;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016.R;
import hugoandrade.euro2016.object.Country;
import hugoandrade.euro2016.object.Match;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = getClass().getSimpleName();

    private final ArrayList<Country> mCountriesList = new ArrayList<>();

    public GroupListAdapter(ArrayList<Country> objects) {
        this.mCountriesList.clear();
        if (objects != null)
            this.mCountriesList.addAll(objects);
        Log.e(TAG, Integer.toString(mCountriesList.size()));
    }

    @Override
    public GroupListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        return new GroupListAdapter.ViewHolder(
                vi.inflate(R.layout.list_item_group, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final GroupListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Country country = mCountriesList.get(holder.getAdapterPosition());

        holder.tvPosition.setText(String.valueOf(country.position));
        holder.tvCountryName.setText(country.name);
        holder.tvCountryName.setGravity(Gravity.START);
        holder.ivCountryName.setImageResource(country.imageID);
        holder.tvVictories.setText(String.valueOf(country.victories));
        holder.tvDraws.setText(String.valueOf(country.draws));
        holder.tvDefeats.setText(String.valueOf(country.defeats));
        holder.tvGoalsFor.setText(String.valueOf(country.goalsFor));
        holder.tvGoalsAgainst.setText(String.valueOf(country.goalsAgainst));
        holder.tvGoalsDifference.setText(String.valueOf(country.goalsDifference));
        holder.tvPoints.setText(String.valueOf(country.points));

        boolean advancedGroupStage = country.advancedGroupStage;
        holder.tvPosition.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvCountryName.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvVictories.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvDraws.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvDefeats.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvGoalsFor.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvGoalsAgainst.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvGoalsDifference.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
        holder.tvPoints.setTypeface(null, advancedGroupStage? Typeface.BOLD : Typeface.NORMAL);
    }

    @Override
    public int getItemCount() {
        return mCountriesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void setAllCountries(ArrayList<Country> countryList) {
        synchronized (this) {
            if (mCountriesList.size() == 0 || mCountriesList.size() != countryList.size()) {
                mCountriesList.clear();
                mCountriesList.addAll(countryList);
                notifyDataSetChanged();
            } else {
                for (int i = 0; i < mCountriesList.size(); i++)
                    if (!mCountriesList.get(i).equals(countryList.get(i))) {
                        mCountriesList.set(i, countryList.get(i));
                        notifyItemChanged(i);
                    }
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvPosition, tvCountryName, tvVictories, tvDraws, tvDefeats, tvGoalsFor,
                 tvGoalsAgainst, tvGoalsDifference, tvPoints;
        ImageView ivCountryName;

        ViewHolder(View itemView) {
            super(itemView);

            tvPosition = (TextView) itemView.findViewById(R.id.tv_country_position);
            tvCountryName = (TextView) itemView.findViewById(R.id.tv_country_name);
            ivCountryName = (ImageView) itemView.findViewById(R.id.iv_country_name);
            tvVictories = (TextView) itemView.findViewById(R.id.tv_country_victories);
            tvDraws = (TextView) itemView.findViewById(R.id.tv_country_draws);
            tvDefeats = (TextView) itemView.findViewById(R.id.tv_country_defeats);
            tvGoalsFor = (TextView) itemView.findViewById(R.id.tv_country_goals_for);
            tvGoalsAgainst = (TextView) itemView.findViewById(R.id.tv_country_goals_against);
            tvGoalsDifference = (TextView) itemView.findViewById(R.id.tv_country_goals_difference);
            tvPoints = (TextView) itemView.findViewById(R.id.tv_country_points);

        }
    }
}
