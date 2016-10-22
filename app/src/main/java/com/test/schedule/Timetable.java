package com.test.schedule;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by szyme on 15.10.2016.
 */

public class Timetable {
    private Map<LocalDate, SchoolDay> timetable = new ArrayMap<>();
    private final String TAG = "schedule:log";

    public Timetable(JSONObject data, JSONObject events) {
        try {
            parseJSON(data, events);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseJSON(JSONObject data, JSONObject events) throws JSONException {

        Iterator iterator = data.keys();

        while (iterator.hasNext()) {

            String key = (String) iterator.next();
            LocalDate date = LocalDate.parse(key);
            SchoolDay schoolDay = new SchoolDay(data.getJSONArray(key), date);

            if (!schoolDay.isEmpty()) {

                if (events.has(key)) {

                    for (int i = 0; i < events.getJSONArray(key).length(); i++) {

                        JSONObject event = events.getJSONArray(key).getJSONObject(i);

                        Lesson lesson = schoolDay.getLesson(event.getInt("LessonNo"));

                        if (lesson != null) {
                            lesson.setEvent(new Event(event.getString("Description"), event.getString("Category")));
                        }

                    }
                }
                timetable.put(date, schoolDay);
            }
        }
    }

    public Map<LocalDate, SchoolDay> getTimetable() {
        return timetable;
    }

    public SchoolDay getSchoolDay(LocalDate date) {
        return timetable.get(date);
    }
}
