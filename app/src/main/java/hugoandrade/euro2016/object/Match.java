package hugoandrade.euro2016.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.Date;

import hugoandrade.euro2016.utils.ISO8601;
import hugoandrade.euro2016.utils.NetworkUtils;

public class Match implements Comparable<Match>, Parcelable {

    @SuppressWarnings("unused") private static final String TAG = Match.class.getSimpleName();

    public static final int TOTAL_MATCHES = 51;

    public static final String TABLE_NAME = "Match";
    private static final String COL_NAME__ID = "_id";
    private static final String COL_NAME_MATCH_NO = "MatchNo";
    private static final String COL_NAME_HOME_TEAM = "HomeTeamID";
    private static final String COL_NAME_AWAY_TEAM = "AwayTeamID";
    private static final String COL_NAME_HOME_TEAM_GOALS = "HomeTeamGoals";
    private static final String COL_NAME_AWAY_TEAM_GOALS = "AwayTeamGoals";
    private static final String COL_NAME_HOME_TEAM_NOTES = "HomeTeamNotes";
    private static final String COL_NAME_AWAY_TEAM_NOTES = "AwayTeamNotes";
    private static final String COL_NAME_GROUP = "GroupLetter";
    private static final String COL_NAME_STAGE = "Stage";
    private static final String COL_NAME_STADIUM = "Stadium";
    private static final String COL_NAME_DATE_AND_TIME = "DateAndTime";

    public static final String GROUP_STAGE = "Group Stage";
    public static final String ROUND_OF_16 = "Round of 16";
    public static final String QUARTER_FINALS = "Quarter Finals";
    public static final String SEMI_FINALS = "Semi Finals";
    public static final String FINAL = "Final";

    private int _id = -1;
    public int matchNo = -1;
    public String homeTeam;
    public String awayTeam;
    public int homeTeamImageID;
    public int awayTeamImageID;
    public int homeTeamGoals;
    public int awayTeamGoals;
    public String homeTeamNotes;
    public String awayTeamNotes;
    public String stage;
    public String group;
    public String stadium;
    public Date dateAndTime;

    public Match(int _id, int MatchNo, String HomeTeam, String AwayTeam, int homeGoals, int awayGoals,
                 String homeTeamNotes, String awayTeamNotes, String group, String stage,
                 String stadium, Date dateAndTime) {
        this._id = _id;
        this.matchNo = MatchNo;
        this.homeTeam = HomeTeam;
        this.awayTeam = AwayTeam;
        this.homeTeamImageID = Country.getImageID(HomeTeam);
        this.awayTeamImageID = Country.getImageID(AwayTeam);
        this.homeTeamGoals = homeGoals;
        this.awayTeamGoals = awayGoals;
        this.homeTeamNotes = homeTeamNotes;
        this.awayTeamNotes = awayTeamNotes;
        this.group = group;
        this.stadium = stadium;
        this.stage = stage;
        this.dateAndTime = dateAndTime;
    }

    public static Match getInstanceFromJsonObject(JsonObject jsonObject) {
        return new Match(
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME__ID, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_MATCH_NO, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_HOME_TEAM, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_AWAY_TEAM, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_HOME_TEAM_GOALS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_AWAY_TEAM_GOALS, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_HOME_TEAM_NOTES, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_AWAY_TEAM_NOTES, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_GROUP, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_STAGE, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_STADIUM, null),
                ISO8601.toDate(NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_DATE_AND_TIME, null))
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(matchNo);
        dest.writeString(homeTeam);
        dest.writeString(awayTeam);
        dest.writeInt(homeTeamGoals);
        dest.writeInt(awayTeamGoals);
        dest.writeInt(homeTeamImageID);
        dest.writeInt(awayTeamImageID);
        dest.writeString(homeTeamNotes);
        dest.writeString(awayTeamNotes);
        dest.writeString(group);
        dest.writeString(stadium);
        dest.writeString(stage);
        dest.writeSerializable(dateAndTime);
    }

    protected Match(Parcel in) {
        _id = in.readInt();
        matchNo = in.readInt();
        homeTeam = in.readString();
        awayTeam = in.readString();
        homeTeamGoals = in.readInt();
        awayTeamGoals = in.readInt();
        homeTeamImageID = in.readInt();
        awayTeamImageID = in.readInt();
        homeTeamNotes = in.readString();
        awayTeamNotes = in.readString();
        group = in.readString();
        stadium = in.readString();
        stage = in.readString();
        dateAndTime = (Date) in.readSerializable();
    }

    public static final Creator<Match> CREATOR = new Creator<Match>() {
        @Override
        public Match createFromParcel(Parcel in) {
            return new Match(in);
        }

        @Override
        public Match[] newArray(int size) {
            return new Match[size];
        }
    };

    public JsonObject getAsJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(COL_NAME__ID , _id);
        jsonObject.addProperty(COL_NAME_MATCH_NO, matchNo);
        jsonObject.addProperty(COL_NAME_HOME_TEAM, homeTeam);
        jsonObject.addProperty(COL_NAME_AWAY_TEAM, awayTeam);
        jsonObject.addProperty(COL_NAME_HOME_TEAM_GOALS, homeTeamGoals);
        jsonObject.addProperty(COL_NAME_AWAY_TEAM_GOALS, awayTeamGoals);
        jsonObject.addProperty(COL_NAME_HOME_TEAM_NOTES, homeTeamNotes);
        jsonObject.addProperty(COL_NAME_AWAY_TEAM_NOTES, awayTeamNotes);
        jsonObject.addProperty(COL_NAME_GROUP, group);
        jsonObject.addProperty(COL_NAME_STAGE, stage);
        jsonObject.addProperty(COL_NAME_STADIUM, stadium);
        jsonObject.addProperty(COL_NAME_DATE_AND_TIME, ISO8601.fromDate(dateAndTime));
        return jsonObject;
    }

    @Override
    public int compareTo(@NonNull Match o) {
        return this.matchNo - o.matchNo;
    }

    public Match cloneInstance() {
        return new Match(_id, matchNo, homeTeam, awayTeam, homeTeamGoals, awayTeamGoals,
                homeTeamNotes, awayTeamNotes, group, stage, stadium, dateAndTime);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Match))
            return false;
        if (((Match) o)._id != this._id)
            return false;
        if (((Match) o).matchNo != this.matchNo)
            return false;
        if (((Match) o).homeTeamGoals != this.homeTeamGoals)
            return false;
        if (((Match) o).awayTeamGoals != this.awayTeamGoals)
            return false;
        if (!areEqual(((Match) o).dateAndTime, this.dateAndTime))
            return false;
        if (!areEqual(((Match) o).homeTeam, this.homeTeam))
            return false;
        if (!areEqual(((Match) o).awayTeam, this.awayTeam))
            return false;
        if (!areEqual(((Match) o).homeTeamNotes, this.homeTeamNotes))
            return false;
        if (!areEqual(((Match) o).awayTeamNotes, this.awayTeamNotes))
            return false;
        if (!areEqual(((Match) o).stage, this.stage))
            return false;
        if (!areEqual(((Match) o).group, this.group))
            return false;
        if (!areEqual(((Match) o).stadium, this.stadium))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "_id: " + Integer.toString(_id)
                + ", MatchNo: " + Integer.toString(matchNo)
                + ", HomeTeam: " + homeTeam
                + ", AwayTeam: " + awayTeam
                + ", HomeTeamGoals: " + Integer.toString(homeTeamGoals)
                + ", AwayTeamGoals: " + Integer.toString(awayTeamGoals)
                + ", HomeTeamNotes: " + homeTeamNotes
                + ", AwayTeamNotes: " + awayTeamNotes
                + ", Group: " + group
                + ", Stage: " + stage
                + ", Stadium: " + stadium
                + ", DateTime: " + ISO8601.fromDate(dateAndTime);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean areEqual(String obj1, String obj2) {
        if (obj1 == null && obj2 == null)
            return true;
        if (obj1 != null && obj2 != null)
            return obj1.equals(obj2);

        return false;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean areEqual(Date date1, Date date2) {
        if (date1 == null && date2 == null)
            return true;
        if (date1 != null && date2 != null)
            return date1.equals(date2);

        return false;
    }
}
