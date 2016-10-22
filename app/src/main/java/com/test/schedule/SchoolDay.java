package com.test.schedule;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import org.joda.time.LocalDate;
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
    private final LocalDate date;
    private HashMap<Integer, Lesson> lessons = new HashMap<>();
    boolean empty = true;

    public SchoolDay(HashMap<Integer, Lesson> lessons, LocalDate date) {
        this.lessons = lessons;
        this.date = date;
    }

    public SchoolDay(JSONArray data, LocalDate date) {
        this.date = date;
        for (int i = 0; i < data.length(); i++) {
            try {
                if (data.getJSONArray(i).length() == 0) {
//                    Log.d(TAG, "SchoolDay: Creating empty Lesson");
                    lessons.put(i, null);
                } else {
                    lessons.put(i, new Lesson(data.getJSONArray(i).getJSONObject(0), i));
                    empty = false;
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        cleanUp();
    }

    public SchoolDay(Parcel in) {
        in.readMap(this.lessons, null);
        this.date = LocalDate.parse(in.readString());
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
//        Log.d(TAG, "cleanUp: DONE: " + lessons.toString());
    }

    public String getTitle() {
        TimetableUtils.getTitle(date);
        return null;
    }

    public HashMap<Integer, Lesson> getLessons() {
        return lessons;
    }

    public Lesson getLesson(int i) {
        return lessons.get(i);
    }

    public boolean isEmpty() {
        return empty;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SchoolDay createFromParcel(Parcel in) {
            return new SchoolDay(in);
        }

        @Override
        public SchoolDay[] newArray(int size) {
            return new SchoolDay[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        try {
            dest.writeMap(this.lessons);
            dest.writeString(this.date.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
