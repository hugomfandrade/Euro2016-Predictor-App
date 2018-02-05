package org.hugoandrade.euro2016.predictor.admin.processing;

import org.hugoandrade.euro2016.predictor.admin.object.Country;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupComp {

    private static final String TAG = GroupComp.class.getSimpleName();

    private final String mGroup;
    private final List<CountryComp> mCountryList;

    GroupComp(String group) {
        mGroup = group;
        mCountryList = new ArrayList<>();
    }

    public void add(CountryComp c) {
        mCountryList.add(c);
    }

    List<CountryComp> getCountryCompList() {
        return mCountryList;
    }

    public List<Country> getCountryList() {
        List<Country> cList = new ArrayList<>();
        for (CountryComp c : mCountryList)
            cList.add(c.getCountry());
        return cList;
    }

    public CountryComp get(int i) {
        return mCountryList.get(i);
    }

    public String getGroup() {
        return mGroup;
    }

    void orderGroup() {
        for (CountryComp country : mCountryList)
            country.compute();

        orderCountryList();
    }

    private void orderCountryList() {
        // Sort Group
        Collections.sort(mCountryList, Collections.<CountryComp>reverseOrder());

        List<CountryComp> sortedGroup = new ArrayList<>();
        List<CountryComp> countriesWithEqualNumberOfPoints = new ArrayList<>();
        countriesWithEqualNumberOfPoints.add(mCountryList.get(0));

        for (int i = 1 ; i < 4 ; i++) {
            // The country "i" has equal number of points as the previous country. Store it in the
            // CountriesStillTied List
            if (mCountryList.get(i - 1).getCountry().getPoints() == mCountryList.get(i).getCountry().getPoints()) {
                countriesWithEqualNumberOfPoints.add(mCountryList.get(i));
            }
            // The country "i" does not have an equal number of points as the previous country.
            // Add the previous countries that were tied (which were stored in the
            // countriesWithEqualNumberOfPoints List) to the sortedGroup List after applying the
            // Tie-Breaking criteria to those countries; and clear and add country "i" to
            // countriesWithEqualNumberOfPoints List
            else {
                sortedGroup.addAll(computeTieBreaker(countriesWithEqualNumberOfPoints));
                countriesWithEqualNumberOfPoints.clear();
                countriesWithEqualNumberOfPoints.add(mCountryList.get(i));
            }
            if (i == 3) { // last iteration
                sortedGroup.addAll(computeTieBreaker(countriesWithEqualNumberOfPoints));
            }
        }

        mCountryList.clear();
        for (int i = 0 ; i < sortedGroup.size() ; i++) {
            sortedGroup.get(i).getCountry().setPosition(i + 1);
            mCountryList.add(sortedGroup.get(i));
        }

    }

    private List<CountryComp> computeTieBreaker(List<CountryComp> countriesTiedList) {

        // One Country only. Return it;
        if (countriesTiedList.size() == 1) {
            return countriesTiedList;
        }
        // Two Countries that were tied. Compute tiebreaker between two teams;
        else if (countriesTiedList.size() == 2) {
            // Clone List (It is not necessary)
            List<CountryComp> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Update Stats between the two teams. ie Compute Head to Head match-up (if it
            cloneCountriesTiedList.get(0).compute(
                    cloneCountriesTiedList.get(1).getCountry().getID());
            cloneCountriesTiedList.get(1).compute(
                    cloneCountriesTiedList.get(0).getCountry().getID());

            // Sort countries
            Collections.sort(cloneCountriesTiedList, Collections.<CountryComp>reverseOrder());

            // Check if, after having applied the 3 following 3 tie-breaking criteria, teams were
            // separated
            // 1. Higher number of points obtained in the matches played between the teams in question;
            // 2. Superior goal difference resulting from the matches played between the teams in question;
            // 3. Higher number of goals scored in the matches played between the teams in question;
            if (!cloneCountriesTiedList.get(0).equalsRanking(cloneCountriesTiedList.get(1))) {
                // if they were separated, it this sorting. just a quick update of GroupStats and return.
                for (CountryComp c : cloneCountriesTiedList)
                    c.compute();
                return cloneCountriesTiedList;
            }
            else {
                // if they have an equal ranking, untie them with the following criteria
                // 5. Superior goal difference in all group matches;
                // 6. Higher number of goals scored in all group matches;
                // (7. Penalty shootout between teams if they are playing each other in final fixture)
                //     is not applicable in this app
                // 8. Fair play conduct
                // 9. Position in the UEFA national team coefficient ranking system.

                // update GroupStats, sort them and return.
                for (CountryComp c : cloneCountriesTiedList)
                    c.compute();
                Collections.sort(cloneCountriesTiedList, Collections.<CountryComp>reverseOrder());
                return cloneCountriesTiedList;
            }
        }
        else if (countriesTiedList.size() == 3) {
            // Clone List (It is not necessary)
            List<CountryComp> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Update Stats between the three teams
            cloneCountriesTiedList.get(0).compute(
                    cloneCountriesTiedList.get(1).getCountry().getID(),
                    cloneCountriesTiedList.get(2).getCountry().getID());
            cloneCountriesTiedList.get(1).compute(
                    cloneCountriesTiedList.get(0).getCountry().getID(),
                    cloneCountriesTiedList.get(2).getCountry().getID());
            cloneCountriesTiedList.get(2).compute(
                    cloneCountriesTiedList.get(0).getCountry().getID(),
                    cloneCountriesTiedList.get(1).getCountry().getID());

            // Sort countries
            Collections.sort(cloneCountriesTiedList, Collections.<CountryComp>reverseOrder());

            // Initialize two arrayList's. One to store the countries still tied (Equal-Ranking)
            // after applying the tie-breakers criteria above. The other to store the countries
            // according to the tie-breaking criteria.
            List<CountryComp> countriesStillTiedList = new ArrayList<>();
            List<CountryComp> countriesAfterTieBreakerList = new ArrayList<>();

            // Add first country to CountriesStillTied List and iterate the remaining
            countriesStillTiedList.add(cloneCountriesTiedList.get(0));
            for (int i = 1 ; i < 3 ; i++) {
                // The country "i" has equal ranking has the previous country. Store it in the
                // CountriesStillTied List
                if (cloneCountriesTiedList.get(i - 1).equalsRanking(cloneCountriesTiedList.get(i))) {
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
                // The country "i" does not have an equal ranking has the previous country. Add the
                // previous countries that were tied (which were stored in the CountriesStillTied
                // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking
                // criteria to those countries; and clear and add country "i" to CountriesStillTied
                // List
                else {
                    countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                    countriesStillTiedList.clear();
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
            }

            // If all 3 countries are still tied, untie them with the following criteria
            // 5. Superior goal difference in all group matches;
            // 6. Higher number of goals scored in all group matches;
            // (7. Penalty shootout between teams if they are playing each other in final fixture)
            //     is not applicable in this app
            // 8. Fair play conduct
            // 9. Position in the UEFA national team coefficient ranking system.
            if (countriesStillTiedList.size() == 3) {
                for (CountryComp c : countriesStillTiedList)
                    c.compute();
                Collections.sort(countriesStillTiedList, Collections.<CountryComp>reverseOrder());
                return countriesStillTiedList;

            }
            // Add the last countries that were tied (which are stored in the CountriesStillTied
            // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking criteria
            // to those countries.
            else {
                countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                for (CountryComp c : countriesAfterTieBreakerList)
                    c.compute();
                return countriesAfterTieBreakerList;
            }
        }
        else {
            // Clone List (It is not necessary)
            List<CountryComp> cloneCountriesTiedList = new ArrayList<>();
            cloneCountriesTiedList.addAll(countriesTiedList);

            // Initialize two arrayList's. One to store the countries still tied (Equal-Ranking)
            // after applying the tie-breakers criteria above. The other to store the countries
            // according to the tie-breaking criteria.
            List<CountryComp> countriesStillTiedList = new ArrayList<>();
            List<CountryComp> countriesAfterTieBreakerList = new ArrayList<>();

            // Add first country to CountriesStillTied List and iterate the remaining
            countriesStillTiedList.add(cloneCountriesTiedList.get(0));
            for (int i = 1 ; i < 4 ; i++) {
                // The country "i" has equal ranking as the previous country. Store it in the
                // CountriesStillTied List
                if (cloneCountriesTiedList.get(i - 1).equalsRanking(cloneCountriesTiedList.get(i))) {
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
                // The country "i" does not have an equal ranking has the previous country. Add the
                // previous countries that were tied (which were stored in the CountriesStillTied
                // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking
                // criteria to those countries; and clear and add country "i" to CountriesStillTied
                // List
                else {
                    countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                    countriesStillTiedList.clear();
                    countriesStillTiedList.add(cloneCountriesTiedList.get(i));
                }
            }

            // If all 4 countries are still tied, untie them with the following criteria
            // 5. Superior goal difference in all group matches;
            // 6. Higher number of goals scored in all group matches;
            // (7. Penalty shootout between teams if they are playing each other in final fixture)
            //     is not applicable in this app
            // 8. Fair play conduct
            // 9. Position in the UEFA national team coefficient ranking system.
            if (countriesStillTiedList.size() == 4) {
                for (CountryComp c : countriesStillTiedList)
                    c.compute();
                /*for (CountryComp c : countriesStillTiedList) {
                    Log.e(TAG, c.getCountry().getName() + " -> " + Integer.toString(c.getCountry().getPosition()));
                }/**/
                Collections.sort(countriesStillTiedList, Collections.<CountryComp>reverseOrder());

                /*for (CountryComp c : countriesStillTiedList) {
                    Log.e(TAG, c.getCountry().getName()
                            + " -> "
                            + Integer.toString(c.getCountry().getPosition())
                            + " - "
                            + Float.toString(c.getCountry().getCoefficient()));
                } /**/
                return countriesStillTiedList;

            }
            // Add the last countries that were tied (which are stored in the CountriesStillTied
            // List) to the CountriesAfterTieBreaker List after applying the Tie-Breaking criteria
            // to those countries.
            else {
                countriesAfterTieBreakerList.addAll(computeTieBreaker(countriesStillTiedList));
                for (CountryComp c : countriesAfterTieBreakerList)
                    c.compute();
                return countriesAfterTieBreakerList;
            }
        }
    }
}
