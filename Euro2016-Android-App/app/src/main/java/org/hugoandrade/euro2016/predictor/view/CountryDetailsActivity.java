package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.hugoandrade.euro2016.predictor.GlobalData;
import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;
import org.hugoandrade.euro2016.predictor.utils.MatchUtils;
import org.hugoandrade.euro2016.predictor.utils.StaticVariableUtils;
import org.hugoandrade.euro2016.predictor.utils.TranslationUtils;
import org.hugoandrade.euro2016.predictor.view.listadapter.GroupListAdapter;
import org.hugoandrade.euro2016.predictor.view.listadapter.KnockoutListAdapter;

import java.util.List;
import java.util.Locale;

public class CountryDetailsActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private final String TAG = CountryDetailsActivity.class.getSimpleName();

    private static final String INTENT_EXTRA_COUNTRY = "intent_extra_country";

    private List<Match> mMatchList;
    private List<Country> mGroupCountryList;
    private Country mCountry;

    public static Intent makeIntent(Context context, Country country) {

        return new Intent(context, CountryDetailsActivity.class)
                .putExtra(INTENT_EXTRA_COUNTRY, country);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_country_details);

        if (getIntent() != null && getIntent().getExtras() != null) {

            Country country = getIntent().getParcelableExtra(INTENT_EXTRA_COUNTRY);
            mCountry = GlobalData.getInstance().getCountry(country);
            mMatchList = GlobalData.getInstance().getMatchList(country);
            mGroupCountryList = GlobalData.getInstance().getCountryList(country);
        }
        else {
            finish();
            return;
        }

        initializeUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeUI() {

        setSupportActionBar((Toolbar) findViewById(R.id.anim_toolbar));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mCountry.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView ivFlag = findViewById(R.id.iv_country_flag);
        ivFlag.setImageResource(Country.getImageID(mCountry));

        TextView tvCountryName = findViewById(R.id.tv_country_name);
        tvCountryName.setText(TranslationUtils.translateCountryName(this, mCountry.getName()));

        TextView tvCountryStatus = findViewById(R.id.tv_country_status);
        String status = getStatus();
        tvCountryStatus.setText(status);
        tvCountryStatus.setVisibility(status == null? View.GONE : View.VISIBLE);

        // Setup title
        TextView tvGroupTitle = findViewById(R.id.tv_group);
        tvGroupTitle.setText(getString(getTitleResource()));

        // Setup recycler view
        GroupListAdapter adapter = new GroupListAdapter(mGroupCountryList);
        adapter.setPrimaryCountry(mCountry);
        RecyclerView recyclerView = findViewById(R.id.rv_group);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        // Setup recycler view
        KnockoutListAdapter knockoutListAdapter = new KnockoutListAdapter(mMatchList);
        knockoutListAdapter.setOnKnockoutListAdapterListener(new KnockoutListAdapter.OnKnockoutListAdapterListener() {
            @Override
            public void onCountryClicked(Country country) {
                if (!mCountry.getID().equals(country.getID())) {
                    finish();
                    startActivity(CountryDetailsActivity.makeIntent(CountryDetailsActivity.this, country));
                }

            }
        });
        RecyclerView rvMatches = findViewById(R.id.rv_matches);
        rvMatches.setAdapter(knockoutListAdapter);
        rvMatches.setNestedScrollingEnabled(false);
        rvMatches.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private int getTitleResource() {
        switch (mCountry.getGroup()) {
            case "A":
                return R.string.group_a;
            case "B":
                return R.string.group_b;
            case "C":
                return R.string.group_c;
            case "D":
                return R.string.group_d;
            case "E":
                return R.string.group_e;
            case "F":
                return R.string.group_f;
        }
        return 0;
    }

    private String getStatus() {
        if (haveAllMatchesBeenPlayed()) {
            if (mCountry.hasAdvancedGroupStage()) {
                // if round of 16
                if (eliminatedIn(StaticVariableUtils.SStage.roundOf16)) {
                    return getString(R.string.eliminated_round_of_16);
                }
                // if quarter final
                if (eliminatedIn(StaticVariableUtils.SStage.quarterFinals)) {
                    return getString(R.string.eliminated_quarter_finals);
                }
                // if semi final
                if (eliminatedIn(StaticVariableUtils.SStage.semiFinals)) {
                    return getString(R.string.eliminated_semi_finals);
                }
                // if final
                if (eliminatedIn(StaticVariableUtils.SStage.finals)) {
                    return getString(R.string.lost_final);
                }
                if (wonItAll()) {
                    return getString(R.string.winners);
                }
            }
            else {
                int placeFinish = mCountry.getPosition();
                return String.valueOf(placeFinish) + TranslationUtils.getSuffix(placeFinish) + " " + getString(R.string.in_group) + " " + mCountry.getGroup();
            }
        }
        return null;
    }

    private boolean eliminatedIn(StaticVariableUtils.SStage stage) {

        for (Match m : mMatchList) {
            if (m.getStage().equals(stage.name)) {
                if (MatchUtils.isMatchPlayed(m)) {
                    if (m.getHomeTeamName().equals(mCountry.getName())) {
                        if (MatchUtils.didAwayTeamWin(m)) {
                            return true;
                        }
                    }
                    else if (m.getAwayTeamName().equals(mCountry.getName())) {
                        if (MatchUtils.didHomeTeamWin(m)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean wonItAll() {

        for (Match m : mMatchList) {
            if (m.getStage().equals(StaticVariableUtils.SStage.finals.name)) {
                if (MatchUtils.isMatchPlayed(m)) {
                    if (m.getHomeTeamName().equals(mCountry.getName())) {
                        if (MatchUtils.didHomeTeamWin(m)) {
                            return true;
                        }
                    }
                    else if (m.getAwayTeamName().equals(mCountry.getName())) {
                        if (MatchUtils.didAwayTeamWin(m)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean haveAllMatchesBeenPlayed() {
        for (Country c : mGroupCountryList) {
            if (c.getMatchesPlayed() != 3)
                return false;
        }
        return true;
    }
}
