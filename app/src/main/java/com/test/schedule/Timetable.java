package com.test.schedule;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
import java.util.List;

/**
 * Created by szyme on 15.10.2016.
 */

public class Timetable {
    private List<SchoolDay> timetable = new ArrayList<>();
    private Date startDate;
    private final String TAG = "schedule:log";

    public Timetable(JSONObject data, Date startDate) {
        parseJSON(data, startDate);
    }

    private void parseJSON(JSONObject data, Date startDate) {
        this.startDate = startDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(startDate);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date;
        for (int dayNumber = 0; dayNumber < data.length(); dayNumber++) {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY + dayNumber);

            date = df.format(calendar.getTime());
            try {
                timetable.add(dayNumber, new SchoolDay(data.getJSONArray(date), dayNumber));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void setEvents(JSONArray events) {
        for (int i = 0; i < events.length(); i++) {
            try {
                JSONObject event = events.getJSONObject(i);

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date date = df.parse(event.getString("Date"));
                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(Calendar.MONDAY);

                calendar.setTime(date);
                int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 2 % 7;

                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_YEAR, 7);
                Date endDate = calendar.getTime();

                int lessonNumber = event.getInt("LessonNo");

                Lesson lesson = getSchoolDay(weekDay).getLesson(lessonNumber);

                if (lesson != null && !date.before(startDate) && date.before(endDate)) {
                    Log.d(TAG, "setEvents: Setting event for " + getSchoolDay(weekDay).getDayName() + " at lesson " + lessonNumber);
                    lesson.setEvent(new Event(event.getString("Description"), event.getString("Category")));
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public List<SchoolDay> getTimetable() {
        return timetable;
    }

    public SchoolDay getSchoolDay(int i) {
        return timetable.get(i);
    }
}
