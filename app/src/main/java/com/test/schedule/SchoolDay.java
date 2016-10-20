package com.test.schedule;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by szyme on 15.10.2016.
 */

public class SchoolDay implements Parcelable {
    private final String TAG = "schedule:log";
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SchoolDay createFromParcel(Parcel in) {
            return new SchoolDay(in);
        }

        @Override
        public SchoolDay[] newArray(int size) {
            return new SchoolDay[size];
        }
    };
    private HashMap<Integer, Lesson> lessons = new HashMap<>();
    private int dayNumber;

    public SchoolDay(HashMap<Integer, Lesson> lessons, int dayNumber) {
        this.lessons = lessons;
        this.dayNumber = dayNumber;
    }

    public SchoolDay(JSONArray data, int dayNumber) {
        Log.d(TAG, "SchoolDay: Creating schoolday from JSON " + data.toString());
        this.dayNumber = dayNumber;
        for (int i = 0; i < data.length(); i++) {
            try {
                if (data.getJSONArray(i).length() == 0) {
//                    Log.d(TAG, "SchoolDay: Creating empty Lesson");
                    lessons.put(i, null);
                } else {
                    lessons.put(i, new Lesson(data.getJSONArray(i).getJSONObject(0), i));
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        cleanUp();
    }

    public SchoolDay(Parcel in) {
        in.readMap(this.lessons, null);
        this.dayNumber = in.readInt();
    }

    private void cleanUp() {
        if (lessons.containsKey(0)) {
            lessons.remove(0);
        }
        int index = 10;
        while (lessons.get(index) == null && index >= 0) {
            lessons.remove(index);
            index--;
        }
        Log.d(TAG, "cleanUp: DONE: " + lessons.toString());
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public String getDayName() {
        switch (dayNumber) {
            case 0:
                return "Poniedziałek";
            case 1:
                return "Wtorek";
            case 2:
                return "Środa";
            case 3:
                return "Czwartek";
            case 4:
                return "Piątek";
            case 5:
                return "Sobota";
            case 6:
                return "Niedziela";
        }
        return null;
    }

    public HashMap<Integer, Lesson> getLessons() {
        return lessons;
    }

    public Lesson getLesson(int i) {
        return lessons.get(i);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeMap(this.lessons);
            dest.writeInt(this.dayNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
