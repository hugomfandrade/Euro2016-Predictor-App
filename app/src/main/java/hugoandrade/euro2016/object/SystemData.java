package hugoandrade.euro2016.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;

import java.util.Calendar;

import hugoandrade.euro2016.utils.ISO8601;
import hugoandrade.euro2016.utils.NetworkUtils;

public class SystemData implements Parcelable {

    @SuppressWarnings("unused") private static final String TAG = SystemData.class.getSimpleName();

    public static final String TABLE_NAME = "SystemData";
    private static final String COL_NAME__ID = "_id";
    private static final String COL_NAME_RULES = "Rules";
    private static final String COL_NAME_SYSTEM_DATE = "SystemDate";
    private static final String COL_NAME_DATE_OF_CHANGE = "DateOfChange";
    public static final String COL_NAME_APP_STATE = "AppState";

    public static final String REQUEST_TYPE = "RequestType";
    public static final String SYSTEM_TIME = "SystemTime";


    private int _id = -1;
    public int ruleCorrectPrediction;
    public int ruleCorrectOutcome;
    public int ruleCorrectOutcomeViaPenalties;
    public int ruleIncorrectPrediction;
    public boolean appState;
    public Calendar systemDate;
    public Calendar dateOfChange;

    private SystemData(int _id, String rules, boolean appState, Calendar systemDate, Calendar dateOfChange) {
        this._id = _id;
        this.appState = appState;
        this.systemDate = systemDate;
        this.dateOfChange = dateOfChange;
        try {
            String[] s = rules.split(",");
            ruleIncorrectPrediction = Integer.parseInt(s[0]);
            ruleCorrectOutcomeViaPenalties = Integer.parseInt(s[1]);
            ruleCorrectOutcome = Integer.parseInt(s[2]);
            ruleCorrectPrediction = Integer.parseInt(s[3]);
        } catch (Exception e) {
            ruleIncorrectPrediction = -1;
            ruleCorrectOutcomeViaPenalties = -1;
            ruleCorrectOutcome = -1;
            ruleCorrectPrediction = 1;
        }
    }

    private SystemData(Parcel in) {
        _id = in.readInt();
        ruleIncorrectPrediction = in.readInt();
        ruleCorrectOutcomeViaPenalties = in.readInt();
        ruleCorrectOutcome = in.readInt();
        ruleCorrectPrediction = in.readInt();
        appState = in.readByte() != 0;
        systemDate = (Calendar) in.readSerializable();
        dateOfChange = (Calendar) in.readSerializable();
    }

    public static final Creator<SystemData> CREATOR = new Creator<SystemData>() {
        @Override
        public SystemData createFromParcel(Parcel in) {
            return new SystemData(in);
        }

        @Override
        public SystemData[] newArray(int size) {
            return new SystemData[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(ruleIncorrectPrediction);
        dest.writeInt(ruleCorrectOutcomeViaPenalties);
        dest.writeInt(ruleCorrectOutcome);
        dest.writeInt(ruleCorrectPrediction);
        dest.writeByte((byte) (appState ? 1 : 0));
        dest.writeSerializable(systemDate);
        dest.writeSerializable(dateOfChange);
        Log.e(TAG, Boolean.toString(appState));
    }

    public JsonObject getAsJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(COL_NAME__ID, _id);
        jsonObject.addProperty(COL_NAME_APP_STATE, appState);
        jsonObject.addProperty(COL_NAME_RULES,
                String.valueOf(ruleIncorrectPrediction) + "," +
                String.valueOf(ruleCorrectOutcomeViaPenalties) + "," +
                String.valueOf(ruleCorrectOutcome) + "," +
                String.valueOf(ruleCorrectPrediction));
        jsonObject.addProperty(COL_NAME_SYSTEM_DATE, ISO8601.fromCalendar(systemDate));
        jsonObject.addProperty(COL_NAME_DATE_OF_CHANGE, ISO8601.fromCalendar(dateOfChange));
        return jsonObject;
    }

    public static SystemData getInstanceFromJsonObject(JsonObject jsonObject) {
        return new SystemData(
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME__ID, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_RULES, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_APP_STATE, false),
                ISO8601.toCalendar(NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_SYSTEM_DATE, null)),
                ISO8601.toCalendar(NetworkUtils.getJsonPrimitive(jsonObject, COL_NAME_DATE_OF_CHANGE, null)));

    }

    public Calendar getSystemDate() {
        Calendar c = Calendar.getInstance();
        long diff = c.getTimeInMillis() - dateOfChange.getTimeInMillis();
        c.setTimeInMillis(systemDate.getTimeInMillis() + diff);
        return c;
    }
}
