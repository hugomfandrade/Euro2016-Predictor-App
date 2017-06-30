package hugoandrade.euro2016backend.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;

import hugoandrade.euro2016backend.utils.NetworkUtils;

public class Country implements Comparable<Country>, Parcelable {

    @SuppressWarnings("unused") private static final String TAG = Country.class.getSimpleName();

    public static final String TABLE_NAME = "Country";
    public static final String COLUMN__ID = "_id";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_MATCHES_PLAYED = "MatchesPlayed";
    public static final String COLUMN_VICTORIES = "Victories";
    public static final String COLUMN_DRAWS = "Draws";
    public static final String COLUMN_DEFEATS = "Defeats";
    public static final String COLUMN_GOALS_FOR = "GoalsFor";
    public static final String COLUMN_GOALS_AGAINST = "GoalsAgainst";
    public static final String COLUMN_GOALS_DIFFERENCE = "GoalsDifference";
    public static final String COLUMN_GROUP = "GroupLetter";
    public static final String COLUMN_POSITION = "Position";
    public static final String COLUMN_POINTS = "Points";

    public static HashMap<String, String> PROJECTION_MAP;

    private int _id = -1;
    public String name;
    public String group;
    public float coefficient;
    public int matchesPlayed = 0;
    public int goalsFor = 0;
    public int goalsAgainst = 0;
    public int goalsDifference = 0;
    public int points = 0;
    public int position = 0;
    public int victories = 0;
    public int draws = 0;
    public int defeats = 0;
    private int fairPlayPoints = 0;

    private ArrayList<Integer> goalsForList;
    private ArrayList<Integer> goalsAgainstList;
    private ArrayList<String> opponentList;

    private Country(int _id, String name, int matchesPlayed, int victories, int draws, int defeats,
                    int goalsFor, int goalsAgainst, int goalsDifference, String group, int points,
                    int position) {
        this._id = _id;
        this.name = name;
        this.matchesPlayed = matchesPlayed;
        this.victories = victories;
        this.draws = draws;
        this.defeats = defeats;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
        this.goalsDifference = goalsDifference;
        this.group = group;
        this.points = points;
        this.position = position;
    }

    public Country(String name,
                   String group,
                   float coefficient,
                   ArrayList<Integer> goalsForList,
                   ArrayList<Integer> goalsAgainstList,
                   ArrayList<String> opponentList){
        this.name = name;
        this.group = group;
        this.coefficient = coefficient;
        this.goalsForList = new ArrayList<>();
        this.goalsAgainstList = new ArrayList<>();
        this.opponentList = new ArrayList<>();
        if (goalsForList != null)
            this.goalsForList.addAll(goalsForList);
        if (goalsAgainstList != null)
            this.goalsAgainstList.addAll(goalsAgainstList);
        if (opponentList != null)
            this.opponentList.addAll(opponentList);

        updateGroupStats();
    }

    private Country(Parcel in) {
        _id = in.readInt();
        name = in.readString();
        group = in.readString();
        coefficient = in.readFloat();
        matchesPlayed = in.readInt();
        goalsFor = in.readInt();
        goalsAgainst = in.readInt();
        goalsDifference = in.readInt();
        points = in.readInt();
        position = in.readInt();
        victories = in.readInt();
        draws = in.readInt();
        defeats = in.readInt();
        fairPlayPoints = in.readInt();
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

    public String toString() {
        return this.name
                + ", Points: " + Integer.toString(this.points)
                + ", GD: " + Integer.toString(this.goalsDifference)
                + ", GF: " + Integer.toString(this.goalsFor)
                + ", GA: " + Integer.toString(this.goalsAgainst)
                + ", MP: " + Integer.toString(this.matchesPlayed);
    }

    @Override
    public int compareTo(@NonNull Country o) {
        if (this.points != o.points)
            return this.points - o.points;
        if (this.goalsDifference != o.goalsDifference)
            return this.goalsDifference - o.goalsDifference;
        if (this.goalsFor != o.goalsFor)
            return this.goalsFor - o.goalsFor;
        if (this.fairPlayPoints != o.fairPlayPoints)
            return this.fairPlayPoints - o.fairPlayPoints;
        if (this.coefficient != o.coefficient)
            return ((int) (this.coefficient - o.coefficient));
        return 0;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean equalsRanking(Country o) {
        if (this.points != o.points)
            return false;
        if (this.goalsDifference != o.goalsDifference)
            return false;
        if (this.goalsFor != o.goalsFor)
            return false;
        return true;

    }

    public void set(Country o) {
        if (!this.name.equals(o.name) || !this.group.equals(o.group))
            return;

        this.matchesPlayed = o.matchesPlayed;
        this.victories = o.victories;
        this.draws = o.draws;
        this.defeats = o.defeats;
        this.goalsFor = o.goalsFor;
        this.goalsAgainst = o.goalsAgainst;
        this.goalsDifference = o.goalsDifference;
        this.points = o.points;
        this.position = o.position;
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean equalsInstance(Country o) {
        if (!this.name.equals(o.name))
            return false;
        if (this.matchesPlayed != o.matchesPlayed)
            return false;
        if (this.victories != o.victories)
            return false;
        if (this.draws != o.draws)
            return false;
        if (this.defeats != o.defeats)
            return false;
        if (this.goalsFor != o.goalsFor)
            return false;
        if (this.goalsAgainst != o.goalsAgainst)
            return false;
        if (this.goalsDifference != o.goalsDifference)
            return false;
        if (!this.group.equals(o.group))
            return false;
        if (this.points != o.points)
            return false;
        if (this.position != o.position)
            return false;
        return true;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(name);
        dest.writeString(group);
        dest.writeFloat(coefficient);
        dest.writeInt(matchesPlayed);
        dest.writeInt(goalsFor);
        dest.writeInt(goalsAgainst);
        dest.writeInt(goalsDifference);
        dest.writeInt(points);
        dest.writeInt(position);
        dest.writeInt(victories);
        dest.writeInt(draws);
        dest.writeInt(defeats);
        dest.writeInt(fairPlayPoints);
    }

    public void updateGroupStats() {
        if (goalsAgainstList.size() != 3 && goalsForList.size() != 3 && opponentList.size() != 3)
            return;

        matchesPlayed = 0;
        victories = 0;
        defeats = 0;
        draws = 0;
        goalsFor = 0;
        goalsAgainst = 0;
        goalsDifference = 0;
        points = 0;

        // Set \"Matches Played\"
        for (int goalsF : goalsForList)
            if (goalsF != -1)
                matchesPlayed++;

        // Set \"Goals For\"
        for (int goalsF : goalsForList)
            if (goalsF != -1)
                goalsFor += goalsF;

        // Set \"Goals Against\"
        for (int goalsA : goalsAgainstList)
            if (goalsA != -1)
                goalsAgainst += goalsA;

        // Set \"Goal Difference\"
        goalsDifference = goalsFor - goalsAgainst;

        // Set \"Victories\", \"Draws\", \"Defeats\" and, consequently, \"Points\"
        for (int i = 0 ; i < 3 ; i++)
            if (goalsForList.get(i) != -1) {
                // if Goals scored are bigger than Goals conceded (or not set at all) in match "i"...
                // ... Victory
                if (goalsForList.get(i) >
                        (goalsAgainstList.get(i) == -1 ? 0 : goalsAgainstList.get(i))) {
                    points = points + 3;
                    victories += 1;
                }
                // ... are equal ... Draw
                else if(goalsForList.get(i) ==
                        (goalsAgainstList.get(i) == -1 ? 0 : goalsAgainstList.get(i))) {
                    points = points + 1;
                    draws += 1;
                }
                // ... are lower ... Defeat
                else {
                    defeats += 1;
                }
            }
    }

    public void updateStatsFilterByCountry(ArrayList<String> filterCountryNames) {
        if (filterCountryNames.size() == 1) {
            matchesPlayed = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsForList.get(i) != -1)
                        matchesPlayed++;
                }
            goalsFor = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsForList.get(i) != -1)
                        goalsFor += goalsForList.get(i);
                }
            goalsAgainst = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsAgainstList.get(i) != -1)
                        goalsAgainst += goalsAgainstList.get(i);
                }
            goalsDifference = goalsFor - goalsAgainst;
            points = 0;
            victories = 0;
            defeats = 0;
            draws = 0;
            for (int i = 0; i < 3; i++)
                if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i) {
                    if (goalsForList.get(i) != -1) {
                        if (goalsForList.get(i) > (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                            points = points + 3;
                            victories += 1;
                        } else if (goalsForList.get(i) == (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                            points = points + 1;
                            draws += 1;
                        }
                        else
                            defeats += 1;

                    }
                }
            return;
        }
        if (filterCountryNames.size() != 2)
            return;
        matchesPlayed = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsForList.get(i) != -1)
                    matchesPlayed++;
            }
        goalsFor = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsForList.get(i) != -1)
                    goalsFor += goalsForList.get(i);
            }
        goalsAgainst = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsAgainstList.get(i) != -1)
                    goalsAgainst += goalsAgainstList.get(i);
            }
        goalsDifference = goalsFor - goalsAgainst;
        points = 0;
        victories = 0;
        defeats = 0;
        draws = 0;
        for (int i = 0; i < 3; i++)
            if (opponentList.lastIndexOf(filterCountryNames.get(0)) == i ||
                    opponentList.lastIndexOf(filterCountryNames.get(1)) == i) {
                if (goalsForList.get(i) != -1) {
                    if (goalsForList.get(i) > (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                        points = points + 3;
                        victories += 1;
                    } else if (goalsForList.get(i) == (goalsAgainstList.get(i) == -1 ? 0 : goalsForList.get(i))) {
                        points = points + 1;
                        draws += 1;
                    } else
                        defeats += 1;
                }
            }
    }

    public JsonObject getAsJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty(COLUMN__ID, _id);
        jsonObject.addProperty(COLUMN_NAME, name);
        jsonObject.addProperty(COLUMN_MATCHES_PLAYED, matchesPlayed);
        jsonObject.addProperty(COLUMN_VICTORIES, victories);
        jsonObject.addProperty(COLUMN_DRAWS, draws);
        jsonObject.addProperty(COLUMN_DEFEATS, defeats);
        jsonObject.addProperty(COLUMN_GOALS_FOR, goalsFor);
        jsonObject.addProperty(COLUMN_GOALS_AGAINST, goalsAgainst);
        jsonObject.addProperty(COLUMN_GOALS_DIFFERENCE, goalsDifference);
        jsonObject.addProperty(COLUMN_GROUP, group);
        jsonObject.addProperty(COLUMN_POSITION, position);
        jsonObject.addProperty(COLUMN_POINTS, points);
        return jsonObject;
    }

    public static Country getInstanceFromJsonObject(JsonObject jsonObject) {
        return new Country(
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN__ID, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_NAME, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_MATCHES_PLAYED, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_VICTORIES, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_DRAWS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_DEFEATS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_GOALS_FOR, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_GOALS_AGAINST, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_GOALS_DIFFERENCE, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_GROUP, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_POINTS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_POSITION, -1));

    }
}
