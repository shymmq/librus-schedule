package com.test.schedule;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.HashMap;

class SchoolDay implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SchoolDay createFromParcel(Parcel in) {
            return new SchoolDay(in);
        }

        @Override
        public SchoolDay[] newArray(int size) {
            return new SchoolDay[size];
        }
    };
    private final String TAG = "schedule:log";
    private final LocalDate date;
    boolean empty = true;
    private HashMap<Integer, Lesson> lessons = new HashMap<>();

    public SchoolDay(HashMap<Integer, Lesson> lessons, LocalDate date) {
        this.lessons = lessons;
        this.date = date;
    }

    SchoolDay(JSONArray data, LocalDate date) {
        this.date = date;
        Log.d(TAG, "SchoolDay: parsing json: " + date.toString() + data.toString());
        for (int i = 0; i < data.length(); i++) {
            try {
                if (data.getJSONArray(i).length() == 0) {
//                    Log.d(TAG, "SchoolDay: Creating empty Lesson");
                    lessons.put(i, null);
                } else {
                    lessons.put(i, new Lesson(data.getJSONArray(i).getJSONObject(0), i, date));
                    empty = false;
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        if (empty) {
            Log.d(TAG, "SchoolDay: Empty");
        } else {
            Log.d(TAG, "SchoolDay: Done");
        }
        cleanUp();
    }

    private SchoolDay(Parcel in) {
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

    LocalDate getDate() {
        return date;
    }

    Lesson getLastLesson() {
        return lessons.get(lessons.size());
    }

    HashMap<Integer, Lesson> getLessons() {
        return lessons;
    }

    Lesson getLesson(int i) {
        return lessons.get(i);
    }

    boolean isEmpty() {
        return empty;
    }

    int size() {
        return lessons.size();
    }

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
