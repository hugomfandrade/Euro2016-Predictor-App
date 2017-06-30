package hugoandrade.euro2016backend.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

import java.util.Date;
import java.util.HashMap;

import hugoandrade.euro2016backend.utils.ISO8601;
import hugoandrade.euro2016backend.utils.NetworkUtils;

public class Match implements Comparable<Match>, Parcelable {

    @SuppressWarnings("unused") private static final String TAG = Match.class.getSimpleName();

    public static final String TABLE_NAME = "Match";
    public static final String COLUMN__ID = "_id";
    public static final String COLUMN_MATCH_NO = "MatchNo";
    public static final String COLUMN_HOME_TEAM = "HomeTeamID";
    public static final String COLUMN_AWAY_TEAM = "AwayTeamID";
    public static final String COLUMN_HOME_TEAM_GOALS = "HomeTeamGoals";
    public static final String COLUMN_AWAY_TEAM_GOALS = "AwayTeamGoals";
    public static final String COLUMN_HOME_TEAM_NOTES = "HomeTeamNotes";
    public static final String COLUMN_AWAY_TEAM_NOTES = "AwayTeamNotes";
    public static final String COLUMN_GROUP = "GroupLetter";
    public static final String COLUMN_STAGE = "Stage";
    public static final String COLUMN_STADIUM = "Stadium";
    public static final String COLUMN_DATE_AND_TIME = "DateAndTime";

    public static HashMap<String, String> PROJECTION_MAP;

    private int _id = -1;
    public int matchNo = -1;
    public String homeTeam;
    public String awayTeam;
    public int homeTeamGoals = -1;
    public int awayTeamGoals = -1;
    public String homeTeamNotes;
    public String awayTeamNotes;
    public String stage;
    private String group;
    private String stadium;
    private Date dateAndTime;

    public Match(int _id, int MatchNo, String HomeTeam, String AwayTeam, int homeGoals, int awayGoals,
                 String homeTeamNotes, String awayTeamNotes, String group, String stage,
                 String stadium, Date dateAndTime) {
        this._id = _id;
        this.matchNo = MatchNo;
        this.homeTeam = HomeTeam;
        this.awayTeam = AwayTeam;
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
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN__ID, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_MATCH_NO, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_HOME_TEAM, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_AWAY_TEAM, null),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_HOME_TEAM_GOALS, -1),
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_AWAY_TEAM_GOALS, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_HOME_TEAM_NOTES, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_AWAY_TEAM_NOTES, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_GROUP, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_STAGE, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_STADIUM, null),
                ISO8601.toDate(NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_DATE_AND_TIME, null))
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
        jsonObject.addProperty(COLUMN__ID , _id);
        jsonObject.addProperty(COLUMN_MATCH_NO, matchNo);
        jsonObject.addProperty(COLUMN_HOME_TEAM, homeTeam);
        jsonObject.addProperty(COLUMN_AWAY_TEAM, awayTeam);
        jsonObject.addProperty(COLUMN_HOME_TEAM_GOALS, homeTeamGoals);
        jsonObject.addProperty(COLUMN_AWAY_TEAM_GOALS, awayTeamGoals);
        jsonObject.addProperty(COLUMN_HOME_TEAM_NOTES, homeTeamNotes);
        jsonObject.addProperty(COLUMN_AWAY_TEAM_NOTES, awayTeamNotes);
        jsonObject.addProperty(COLUMN_GROUP, group);
        jsonObject.addProperty(COLUMN_STAGE, stage);
        jsonObject.addProperty(COLUMN_STADIUM, stadium);
        jsonObject.addProperty(COLUMN_DATE_AND_TIME, ISO8601.fromDate(dateAndTime));
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
}
