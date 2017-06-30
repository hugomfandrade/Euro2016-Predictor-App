package hugoandrade.euro2016backend.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.Calendar;
import java.util.HashMap;

import hugoandrade.euro2016backend.utils.ISO8601;
import hugoandrade.euro2016backend.utils.NetworkUtils;

public class SystemData implements Parcelable {

    @SuppressWarnings("unused") private static final String TAG = SystemData.class.getSimpleName();

    public static final String TABLE_NAME = "SystemData";
    public static final String COLUMN__ID = "_id";
    public static final String COLUMN_RULES = "Rules";
    public static final String COLUMN_SYSTEM_DATE = "SystemDate";
    public static final String COLUMN_DATE_OF_CHANGE = "DateOfChange";
    public static final String COLUMN_APP_STATE = "AppState";

    public static HashMap<String, String> PROJECTION_MAP;

    private int _id = -1;
    public String rules;
    public boolean appState;
    public Calendar systemDate;
    public Calendar dateOfChange;

    private SystemData(int _id, String rules, boolean appState, Calendar systemDate, Calendar dateOfChange) {
        Log.e(TAG, Boolean.toString(appState));
        this._id = _id;
        this.rules = rules;
        this.appState = appState;
        this.systemDate = systemDate;
        this.dateOfChange = dateOfChange;
    }

    private SystemData(Parcel in) {
        Log.e(TAG, Boolean.toString(appState));
        _id = in.readInt();
        rules = in.readString();
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
        dest.writeString(rules);
        dest.writeByte((byte) (appState ? 1 : 0));
        dest.writeSerializable(systemDate);
        dest.writeSerializable(dateOfChange);
        Log.e(TAG, Boolean.toString(appState));
    }

    public JsonObject getAsJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(COLUMN__ID, _id);
        jsonObject.addProperty(COLUMN_APP_STATE, appState);
        jsonObject.addProperty(COLUMN_RULES, rules);
        jsonObject.addProperty(COLUMN_SYSTEM_DATE, ISO8601.fromCalendar(systemDate));
        jsonObject.addProperty(COLUMN_DATE_OF_CHANGE, ISO8601.fromCalendar(dateOfChange));
        return jsonObject;
    }

    public static SystemData getInstanceFromJsonObject(JsonObject jsonObject) {
        return new SystemData(
                (int) NetworkUtils.getJsonPrimitive(jsonObject, COLUMN__ID, -1),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_RULES, null),
                NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_APP_STATE, false),
                ISO8601.toCalendar(NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_SYSTEM_DATE, null)),
                ISO8601.toCalendar(NetworkUtils.getJsonPrimitive(jsonObject, COLUMN_DATE_OF_CHANGE, null)));

    }

    public Calendar getSystemDate() {
        Calendar c = Calendar.getInstance();
        long diff = c.getTimeInMillis() - dateOfChange.getTimeInMillis();
        c.setTimeInMillis(systemDate.getTimeInMillis() + diff);
        return c;
    }
}
