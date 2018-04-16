package org.hugoandrade.euro2016.predictor.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.hugoandrade.euro2016.predictor.R;
import org.hugoandrade.euro2016.predictor.data.raw.Country;
import org.hugoandrade.euro2016.predictor.data.raw.Match;

import java.util.ArrayList;
import java.util.List;

public class CountryDetailsActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private final String TAG = CountryDetailsActivity.class.getSimpleName();

    private static final String INTENT_EXTRA_COUNTRY = "intent_extra_country";
    private static final String INTENT_EXTRA_MATCH_LIST = "intent_extra_match_list";
    private static final String INTENT_EXTRA_COUNTRY_LIST = "intent_extra_country_list";

    private List<Match> mMatchList;
    private List<Country> mGroupCountryList;
    private Country mCountry;

    public static Intent makeIntent(Context context,
                                    Country country,
                                    List<Match> matchList,
                                    List<Country> groupCountryList) {

        return new Intent(context, CountryDetailsActivity.class)
                .putExtra(INTENT_EXTRA_COUNTRY, country)
                .putParcelableArrayListExtra(INTENT_EXTRA_MATCH_LIST, new ArrayList<>(matchList))
                .putParcelableArrayListExtra(INTENT_EXTRA_COUNTRY_LIST, new ArrayList<>(groupCountryList));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_country_details);

        if (getIntent() != null && getIntent().getExtras() != null) {
            mCountry = getIntent().getExtras().getParcelable(INTENT_EXTRA_COUNTRY);
            mMatchList = getIntent().getExtras().getParcelableArrayList(INTENT_EXTRA_MATCH_LIST);
            mGroupCountryList = getIntent().getExtras().getParcelableArrayList(INTENT_EXTRA_COUNTRY_LIST);
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

        /*RecyclerView rvPredictions = findViewById(R.id.rv_all_predictions);
        PredictionListAdapter mAdapter = new PredictionListAdapter(mMatchList,
                mGroupCountryList,
                PredictionListAdapter.VIEW_TYPE_DISPLAY_ONLY);
        rvPredictions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvPredictions.setAdapter(mAdapter);/**/
    }
}
