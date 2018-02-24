package org.hugoandrade.euro2016.predictor.admin.utils;

import android.util.Log;

import org.hugoandrade.euro2016.predictor.admin.data.Country;
import org.hugoandrade.euro2016.predictor.admin.data.Match;
import org.hugoandrade.euro2016.predictor.admin.data.SystemData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.hugoandrade.euro2016.predictor.admin.utils.StaticVariableUtils.*;

public final class InitConfigUtils {

    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = MatchUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private InitConfigUtils() {
        throw new AssertionError();
    }

    public static SystemData buildInitSystemData() {
        Calendar date = Calendar.getInstance();
        return new SystemData(null, "0,1,2,4", true, date, date);
    }

    public static List<Match> buildInitMatchList(List<Country> countryList) {
        HashMap<String, String> countryIDs = new HashMap<>();
        for (Country c : countryList) {
            countryIDs.put(c.getName(), c.getID());
        }
        String idFrance = countryIDs.get(SCountry.France.name);
        String idRomania = countryIDs.get(SCountry.Romania.name);
        String idAlbania = countryIDs.get(SCountry.Albania.name);
        String idSwitzerland = countryIDs.get(SCountry.Switzerland.name);

        String idEngland = countryIDs.get(SCountry.England.name);
        String idRussia = countryIDs.get(SCountry.Russia.name);
        String idWales = countryIDs.get(SCountry.Wales.name);
        String idSlovakia = countryIDs.get(SCountry.Slovakia.name);

        String idGermany = countryIDs.get(SCountry.Germany.name);
        String idUkraine = countryIDs.get(SCountry.Ukraine.name);
        String idPoland = countryIDs.get(SCountry.Poland.name);
        String idNorthernIreland = countryIDs.get(SCountry.NorthernIreland.name);

        String idSpain = countryIDs.get(SCountry.Spain.name);
        String idCzechRepublic = countryIDs.get(SCountry.CzechRepublic.name);
        String idTurkey = countryIDs.get(SCountry.Turkey.name);
        String idCroatia = countryIDs.get(SCountry.Croatia.name);

        String idBelgium = countryIDs.get(SCountry.Belgium.name);
        String idItaly = countryIDs.get(SCountry.Italy.name);
        String idIreland = countryIDs.get(SCountry.Ireland.name);
        String idSweden = countryIDs.get(SCountry.Sweden.name);

        String idPortugal = countryIDs.get(SCountry.Portugal.name);
        String idIceland = countryIDs.get(SCountry.Iceland.name);
        String idAustria = countryIDs.get(SCountry.Austria.name);
        String idHungary = countryIDs.get(SCountry.Hungary.name);

        List<Match> matchList = new ArrayList<>();

        matchList.add(emptyMatchInstance(1, idFrance, idRomania, "100620162000",
                Stadium.StadeDeFrance.name, SGroup.A.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(2, idAlbania, idSwitzerland, "110620161400",
                Stadium.StadeBollaertDelelis.name, SGroup.A.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(3, idWales, idSlovakia, "110620161700",
                Stadium.StadeDeBordeaux.name, SGroup.B.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(4, idEngland, idRussia, "110620162000",
                Stadium.StadeVelodrome.name, SGroup.B.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(5, idTurkey, idCroatia, "120620161400",
                Stadium.ParcDesPrinces.name, SGroup.D.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(6, idPoland, idNorthernIreland, "120620161700",
                Stadium.StadeDeNice.name, SGroup.C.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(7, idGermany, idUkraine, "120620162000",
                Stadium.StadePierreMauroy.name, SGroup.C.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(8, idSpain, idCzechRepublic, "130620161400",
                Stadium.StadiumMunicipal.name, SGroup.D.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(9, idIreland, idSweden, "130620161700",
                Stadium.StadeDeFrance.name, SGroup.E.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(10, idBelgium, idItaly, "130620162000",
                Stadium.ParcOlympiqueLyonnais.name, SGroup.E.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(11, idAustria, idHungary, "140620161700",
                Stadium.StadeDeBordeaux.name, SGroup.F.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(12, idPortugal, idIceland, "140620162000",
                Stadium.StadeGeoffroyGuichard.name, SGroup.F.name, SStage.groupStage.name));

        /* ****************************************************************************************************** */

        matchList.add(emptyMatchInstance(13, idRussia, idSlovakia, "150620161400",
                Stadium.StadePierreMauroy.name(), SGroup.B.name(), SStage.groupStage.name));
        matchList.add(emptyMatchInstance(14, idRomania, idSwitzerland, "150620161700",
                Stadium.ParcDesPrinces.name(), SGroup.A.name(), SStage.groupStage.name));
        matchList.add(emptyMatchInstance(15, idFrance, idAlbania, "150620162000",
                Stadium.StadeVelodrome.name, SGroup.A.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(16, idEngland, idWales, "160620161400",
                Stadium.StadeBollaertDelelis.name, SGroup.B.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(17, idUkraine, idNorthernIreland, "160620161700",
                Stadium.ParcOlympiqueLyonnais.name, SGroup.C.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(18, idGermany, idPoland, "160620162000",
                Stadium.StadeDeFrance.name, SGroup.C.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(19, idItaly, idSweden, "170620161400",
                Stadium.StadiumMunicipal.name, SGroup.E.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(20, idCzechRepublic, idCroatia, "170620161700",
                Stadium.StadeGeoffroyGuichard.name, SGroup.D.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(21, idSpain, idTurkey, "170620162000",
                Stadium.StadeDeNice.name, SGroup.D.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(22, idBelgium, idIreland, "180620161400",
                Stadium.StadeDeBordeaux.name, SGroup.E.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(23, idIceland, idHungary, "180620161700",
                Stadium.StadeVelodrome.name, SGroup.F.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(24, idPortugal, idAustria, "180620162000",
                Stadium.ParcDesPrinces.name, SGroup.F.name, SStage.groupStage.name));

        /* ****************************************************************************************************** */

        matchList.add(emptyMatchInstance(25, idSwitzerland, idFrance, "190620162000",
                Stadium.StadeVelodrome.name, SGroup.A.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(26, idRomania, idAlbania, "190620162000",
                Stadium.ParcDesPrinces.name, SGroup.A.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(27, idSlovakia, idEngland, "200620162000",
                Stadium.StadeGeoffroyGuichard.name, SGroup.B.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(28, idRussia, idWales, "200620162000",
                Stadium.StadiumMunicipal.name, SGroup.B.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(29, idNorthernIreland, idGermany, "210620161700",
                Stadium.ParcDesPrinces.name, SGroup.C.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(30, idUkraine, idPoland, "210620161700",
                Stadium.StadeVelodrome.name, SGroup.C.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(31, idCroatia, idSpain, "210620162000",
                Stadium.StadeDeBordeaux.name, SGroup.D.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(32, idCzechRepublic, idTurkey, "210620162000",
                Stadium.StadeBollaertDelelis.name, SGroup.D.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(33, idHungary, idPortugal, "220620161700",
                Stadium.ParcOlympiqueLyonnais.name, SGroup.F.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(34, idIceland, idAustria, "220620161700",
                Stadium.StadeDeFrance.name, SGroup.F.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(35, idSweden, idBelgium, "220620162000",
                Stadium.StadeDeNice.name, SGroup.E.name, SStage.groupStage.name));
        matchList.add(emptyMatchInstance(36, idItaly, idIreland, "220620162000",
                Stadium.StadePierreMauroy.name, SGroup.E.name, SStage.groupStage.name));

        /* ****************************************************************************************************** */

        matchList.add(emptyMatchInstance(37, "Runner-up Group A", "Runner-up Group C",
                "250620161400", Stadium.StadeGeoffroyGuichard.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(38, "Winner Group B", "3rd Place A, C or D",
                "250620161700", Stadium.ParcDesPrinces.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(39, "Winner Group D", "3rd Place B, E or F",
                "250620162000", Stadium.StadeBollaertDelelis.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(40, "Winner Group A", "3rd Place C, D or E",
                "260620161400", Stadium.ParcOlympiqueLyonnais.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(41, "Winner Group C", "3rd Place A, B or F",
                "260620161700", Stadium.StadePierreMauroy.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(42, "Winner Group F", "Runner-up Group E",
                "260620162000", Stadium.StadiumMunicipal.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(43, "Winner Group E", "Runner-up Group D",
                "270620161700", Stadium.ParcDesPrinces.name, null, SStage.roundOf16.name));
        matchList.add(emptyMatchInstance(44, "Runner-up Group B", "Runner-up Group F",
                "270620162000", Stadium.StadeDeNice.name, null, SStage.roundOf16.name));

        /* ****************************************************************************************************** */

        matchList.add(emptyMatchInstance(45, "Winner Match 37", "Winner Match 39",
                "300620162000", Stadium.StadeVelodrome.name, null, SStage.quarterFinals.name));
        matchList.add(emptyMatchInstance(46, "Winner Match 38", "Winner Match 42",
                "010720162000", Stadium.StadePierreMauroy.name, null, SStage.quarterFinals.name));
        matchList.add(emptyMatchInstance(47, "Winner Match 41", "Winner Match 43",
                "020720162000", Stadium.StadeDeBordeaux.name, null, SStage.quarterFinals.name));
        matchList.add(emptyMatchInstance(48, "Winner Match 40", "Winner Match 44",
                "030720162000", Stadium.StadeDeFrance.name, null, SStage.quarterFinals.name));

        /* ****************************************************************************************************** */

        matchList.add(emptyMatchInstance(49, "Winner Match 45", "Winner Match 46",
                "060720162000", Stadium.ParcOlympiqueLyonnais.name, null, SStage.semiFinals.name));
        matchList.add(emptyMatchInstance(50, "Winner Match 47", "Winner Match 48",
                "070720162000", Stadium.StadeVelodrome.name, null, SStage.semiFinals.name));

        /* ****************************************************************************************************** */

        matchList.add(emptyMatchInstance(51, "Winner Match 49", "Winner Match 50",
                "100720162000", Stadium.StadeDeFrance.name, null, SStage.finals.name));
        return matchList;
    }

    public static List<Country> buildInitCountryList() {
        List<Country> countryList = new ArrayList<>();
        countryList.add(emptyCountryInstance(SCountry.France.name, SGroup.A.name, 33.599f));
        countryList.add(emptyCountryInstance(SCountry.Romania.name, SGroup.A.name, 28.038f));
        countryList.add(emptyCountryInstance(SCountry.Albania.name, SGroup.A.name, 23.216f));
        countryList.add(emptyCountryInstance(SCountry.Switzerland.name, SGroup.A.name, 31.254f));

        countryList.add(emptyCountryInstance(SCountry.England.name, SGroup.B.name, 35.963f));
        countryList.add(emptyCountryInstance(SCountry.Russia.name, SGroup.B.name, 31.345f));
        countryList.add(emptyCountryInstance(SCountry.Wales.name, SGroup.B.name, 24.531f));
        countryList.add(emptyCountryInstance(SCountry.Slovakia.name, SGroup.B.name, 27.171f));

        countryList.add(emptyCountryInstance(SCountry.Germany.name, SGroup.C.name, 40.236f));
        countryList.add(emptyCountryInstance(SCountry.Ukraine.name, SGroup.C.name, 30.313f));
        countryList.add(emptyCountryInstance(SCountry.Poland.name, SGroup.C.name, 28.306f));
        countryList.add(emptyCountryInstance(SCountry.NorthernIreland.name, SGroup.C.name, 22.961f));

        countryList.add(emptyCountryInstance(SCountry.Spain.name, SGroup.D.name, 37.962f));
        countryList.add(emptyCountryInstance(SCountry.CzechRepublic.name, SGroup.D.name, 29.403f));
        countryList.add(emptyCountryInstance(SCountry.Turkey.name, SGroup.D.name, 27.033f));
        countryList.add(emptyCountryInstance(SCountry.Croatia.name, SGroup.D.name, 30.642f));

        countryList.add(emptyCountryInstance(SCountry.Belgium.name, SGroup.E.name, 34.442f));
        countryList.add(emptyCountryInstance(SCountry.Italy.name, SGroup.E.name, 34.345f));
        countryList.add(emptyCountryInstance(SCountry.Ireland.name, SGroup.E.name, 26.902f));
        countryList.add(emptyCountryInstance(SCountry.Sweden.name, SGroup.E.name, 29.028f));

        countryList.add(emptyCountryInstance(SCountry.Portugal.name, SGroup.F.name, 35.138f));
        countryList.add(emptyCountryInstance(SCountry.Iceland.name, SGroup.F.name, 25.388f));
        countryList.add(emptyCountryInstance(SCountry.Austria.name, SGroup.F.name, 30.932f));
        countryList.add(emptyCountryInstance(SCountry.Hungary.name, SGroup.F.name, 27.142f));
        return countryList;
    }

    private static Country emptyCountryInstance(String name,
                                                String group,
                                                float coefficient) {
        int z = 0;
        return new Country(null, name, z, z, z, z, z, z, z, group, z, z, coefficient, z);
    }

    private static Match emptyMatchInstance(int matchNumber, String homeTeamID, String awayTeamID,
                                            String date, String stadium, String group, String stage) {
        int z = -1;
        return new Match(null, matchNumber, homeTeamID, awayTeamID, z, z, null, null, group, stage, stadium,
                parseDate(date, TEMPLATE, Locale.UK));
    }

    private final static String TEMPLATE = "ddMMyyyyHHmm";

    private static Date parseDate(String date, String template, Locale locale) {
        DateFormat DATE_FORMATTER = new SimpleDateFormat(template, locale);
        try {
            return DATE_FORMATTER.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "ParseException: " + e.getMessage());
        }
        return null;
    }
}
