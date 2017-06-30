package hugoandrade.euro2016.object;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.JsonObject;
import hugoandrade.euro2016.R;
import hugoandrade.euro2016.utils.NetworkUtils;

public class Country implements Parcelable {

    public static final String TABLE_NAME = "Country";
    private final static String COL_NAME_NAME = "Name";
    private final static String COL_NAME_MATCHES_PLAYED = "MatchesPlayed";
    private final static String COL_NAME_VICTORIES = "Victories";
    private final static String COL_NAME_DRAWS = "Draws";
    private final static String COL_NAME_DEFEATS = "Defeats";
    private final static String COL_NAME_GOALS_FOR = "GoalsFor";
    private final static String COL_NAME_GOALS_AGAINST = "GoalsAgainst";
    private final static String COL_NAME_GOALS_DIFFERENCE = "GoalsDifference";
    private final static String COL_NAME_GROUP = "GroupLetter";
    private final static String COL_NAME_POSITION = "Position";

    public String name;
    public int matchesPlayed = 0;
    public int goalsFor = 0;
    public int goalsAgainst = 0;
    public int goalsDifference = 0;
    public int points = 0;
    public int position = 0;
    public int victories = 0;
    public int draws = 0;
    public int defeats = 0;
    private float coefficient = 0;
    public int fairPlayPoints = 0;
    public String group;
    public int imageID;
    public boolean advancedGroupStage = false;

    public Country(String name, int matchesPlayed, int victories, int draws, int defeats, int goalsFor,
                   int goalsAgainst, int goalsDifference, String group, int position){
        this.name = name;
        this.matchesPlayed = matchesPlayed;
        this.victories = victories;
        this.defeats = defeats;
        this.draws = draws;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalsDifference = goalsDifference;
        this.group = group;
        this.position = position;
        this.points = 3 * victories + draws;
        this.imageID = getImageID(name);
        this.advancedGroupStage = (matchesPlayed == 3 && position < 3);
    }

    public String toString() {
        return this.name
                + ", Points: " + Integer.toString(this.points)
                + ", GD: " + Integer.toString(this.goalsDifference)
                + ", GF: " + Integer.toString(this.goalsFor)
                + ", GA: " + Integer.toString(this.goalsAgainst)
                + ", MP: " + Integer.toString(this.matchesPlayed);
    }

    public static Country getInstanceFromJsonObject(JsonObject jsonObject) {
        return new Country(
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_NAME, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_MATCHES_PLAYED, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_VICTORIES, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_DRAWS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_DEFEATS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_GOALS_FOR, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_GOALS_AGAINST, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_GOALS_DIFFERENCE, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_GROUP, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_POSITION, -1)
        );
    }

    protected Country(Parcel in) {
        name = in.readString();
        matchesPlayed = in.readInt();
        goalsFor = in.readInt();
        goalsAgainst = in.readInt();
        goalsDifference = in.readInt();
        points = in.readInt();
        position = in.readInt();
        victories = in.readInt();
        draws = in.readInt();
        defeats = in.readInt();
        coefficient = in.readFloat();
        fairPlayPoints = in.readInt();
        group = in.readString();
        imageID = in.readInt();
        advancedGroupStage = in.readByte() != 0;
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(matchesPlayed);
        dest.writeInt(goalsFor);
        dest.writeInt(goalsAgainst);
        dest.writeInt(goalsDifference);
        dest.writeInt(points);
        dest.writeInt(position);
        dest.writeInt(victories);
        dest.writeInt(draws);
        dest.writeInt(defeats);
        dest.writeFloat(coefficient);
        dest.writeInt(fairPlayPoints);
        dest.writeString(group);
        dest.writeInt(imageID);
        dest.writeByte((byte) (advancedGroupStage ? 1 : 0));
    }

    static int getImageID(String name) {
        if (name == null) return -1;
        switch (name) {
            case "Albania": return R.drawable.ic_flag_of_albania;
            case "Austria": return R.drawable.ic_flag_of_austria;
            case "Belgium": return R.drawable.ic_flag_of_belgium;
            case "Croatia": return R.drawable.ic_flag_of_croatia;
            case "England": return R.drawable.ic_flag_of_england;
            case "France": return R.drawable.ic_flag_of_france;
            case "Germany": return R.drawable.ic_flag_of_germany;
            case "Hungary": return R.drawable.ic_flag_of_hungary;
            case "Iceland": return R.drawable.ic_flag_of_iceland;
            case "Ireland": return R.drawable.ic_flag_of_ireland;
            case "Italy": return R.drawable.ic_flag_of_italy;
            case "Northern Ireland": return R.drawable.ic_flag_of_northern_ireland;
            case "Poland": return R.drawable.ic_flag_of_poland;
            case "Portugal": return R.drawable.ic_flag_of_portugal;
            case "Romania": return R.drawable.ic_flag_of_romania;
            case "Russia": return R.drawable.ic_flag_of_russia;
            case "Slovakia": return R.drawable.ic_flag_of_slovakia;
            case "Spain": return R.drawable.ic_flag_of_spain;
            case "Sweden": return R.drawable.ic_flag_of_sweden;
            case "Switzerland": return R.drawable.ic_flag_of_switzerland;
            case "Czech Republic": return R.drawable.ic_flag_of_the_czech_republic;
            case "Turkey": return R.drawable.ic_flag_of_turkey;
            case "Ukraine": return R.drawable.ic_flag_of_ukraine;
            case "Wales": return R.drawable.ic_flag_of_wales;
        }
        return 0;
    }

    public void advancedGroupStage() {
        this.advancedGroupStage = true;
    }
}
