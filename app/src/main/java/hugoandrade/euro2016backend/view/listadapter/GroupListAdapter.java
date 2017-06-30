package hugoandrade.euro2016backend.view.listadapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import hugoandrade.euro2016backend.object.Country;
import hugoandrade.euro2016backend.R;

public class GroupListAdapter extends ArrayAdapter<Country> {

    @SuppressWarnings("unused") private static final String TAG = GroupListAdapter.class.getSimpleName();

    private final ArrayList<Country> countryList = new ArrayList<>();

    public GroupListAdapter(Activity context, int resource, ArrayList<Country> objects) {
        super(context, resource, objects);
        this.countryList.addAll(objects);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_group, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvPosition = (TextView) convertView.findViewById(R.id.tv_country_position);
            viewHolder.tvCountryName = (TextView) convertView.findViewById(R.id.tv_country_name);
            viewHolder.tvVictories = (TextView) convertView.findViewById(R.id.tv_country_victories);
            viewHolder.tvDraws = (TextView) convertView.findViewById(R.id.tv_country_draws);
            viewHolder.tvDefeats = (TextView) convertView.findViewById(R.id.tv_country_defeats);
            viewHolder.tvGoalsFor = (TextView) convertView.findViewById(R.id.tv_country_goals_for);
            viewHolder.tvGoalsAgainst = (TextView) convertView.findViewById(R.id.tv_country_goals_against);
            viewHolder.tvGoalsDifference = (TextView) convertView.findViewById(R.id.tv_country_goals_difference);
            viewHolder.tvPoints = (TextView) convertView.findViewById(R.id.tv_country_points);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Country country = this.countryList.get(position);
        if (country != null) {
            viewHolder.tvPosition.setText(String.valueOf(country.position));
            viewHolder.tvCountryName.setText(country.name);
            viewHolder.tvVictories.setText(String.valueOf(country.victories));
            viewHolder.tvDraws.setText(String.valueOf(country.draws));
            viewHolder.tvDefeats.setText(String.valueOf(country.defeats));
            viewHolder.tvGoalsFor.setText(String.valueOf(country.goalsFor));
            viewHolder.tvGoalsAgainst.setText(String.valueOf(country.goalsAgainst));
            viewHolder.tvGoalsDifference.setText(String.valueOf(country.goalsDifference));
            viewHolder.tvPoints.setText(String.valueOf(country.points));
        }

        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    public void setAll(@NonNull ArrayList<Country> countryList) {
        this.countryList.clear();
        this.countryList.addAll(countryList);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return countryList.size();
    }

    private static class ViewHolder {
        TextView tvPosition, tvCountryName, tvVictories, tvDraws, tvDefeats,
                tvGoalsFor, tvGoalsAgainst, tvGoalsDifference, tvPoints;
    }
}
