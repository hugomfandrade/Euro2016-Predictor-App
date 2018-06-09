package org.hugoandrade.euro2016.predictor.cloudsim.utils;


public final class StaticVariableUtils {
    /**
     * Logging tag.
     */
    @SuppressWarnings("unused")
    private static final String TAG = StaticVariableUtils.class.getSimpleName();

    /**
     * Ensure this class is only used as a utility.
     */
    private StaticVariableUtils() {
        throw new AssertionError();
    }

    public enum SCountry {

        France("France"),
        Romania("Romania"),
        Albania("Albania"),
        Switzerland("Switzerland"),

        England("England"),
        Russia("Russia"),
        Wales("Wales"),
        Slovakia("Slovakia"),

        Germany("Germany"),
        Ukraine("Ukraine"),
        Poland("Poland"),
        NorthernIreland("Northern Ireland"),

        Spain("Spain"),
        CzechRepublic("Czech Republic"),
        Turkey("Turkey"),
        Croatia("Croatia"),

        Belgium("Belgium"),
        Italy("Italy"),
        Ireland("Ireland"),
        Sweden("Sweden"),

        Portugal("Portugal"),
        Iceland("Iceland"),
        Austria("Austria"),
        Hungary("Hungary");


        public final String name;

        SCountry(String group) {
            name = group;
        }
    }

    public enum SStage {

        groupStage("Group Stage"),
        roundOf16("Round of 16"),
        quarterFinals("Quarter Final"),
        semiFinals("Semi Final"),
        finals("Final");

        public final String name;

        SStage(String stage) {
            name = stage;
        }
    }

    public enum SGroup {

        A("A"),
        B("B"),
        C("C"),
        D("D"),
        E("E"),
        F("F");

        public final String name;

        SGroup(String group) {
            name = group;
        }
    }

    public enum Stadium {

        StadeDeFrance("Stade de France (Saint-Denis)"),
        StadeBollaertDelelis("Stade Bollaert-Delelis (Lens)"),
        StadeDeBordeaux("Nouveau Stade de Bordeaux (Bordeaux)"),
        StadeVelodrome("Stade Vélodrome (Marseille)"),
        ParcDesPrinces("Parc des Princes (Paris)"),
        StadeDeNice("Stade de Nice (Nice)"),
        StadePierreMauroy("Stade Pierre-Mauroy (Lille)"),
        StadiumMunicipal("Stadium Municipal (Toulose)"),
        ParcOlympiqueLyonnais("Parc Olympique Lyonnais (Lyon)"),
        StadeGeoffroyGuichard("Stade Geoffroy-Guichard (Saint-Étienne)");

        public final String name;

        Stadium(String group) {
            name = group;
        }
    }
}
