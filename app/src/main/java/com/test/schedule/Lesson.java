package com.test.schedule;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;

/**
 * Created by szyme on 15.10.2016.
 */

public class Lesson implements Serializable {


    private final String TAG = "schedule:log";
    private Event event = null;
    private Subject subject;
    private Teacher teacher;
    private Subject orgSubject = null;
    private Teacher orgTeacher = null;
    private boolean substitution = false;
    private int lessonNumber;
    private boolean isCanceled;
    private LocalDate date;
    private LocalTime endTime;
    private LocalTime startTime;

    Lesson(JSONObject data, int lessonNumber, LocalDate date) throws JSONException, ParseException {
        this.lessonNumber = lessonNumber;
//        Log.d(TAG, "Creating lesson from JSON:   " + data.toString());
        if (data.length() > 0) {
            this.isCanceled = data.getBoolean("IsCanceled");
            this.subject = new Subject(data.getJSONObject("Subject"));
            this.teacher = new Teacher(data.getJSONObject("Teacher"));
            endTime = LocalTime.parse(data.getString("HourTo"), DateTimeFormat.forPattern("HH:mm"));
            startTime = LocalTime.parse(data.getString("HourFrom"), DateTimeFormat.forPattern("HH:mm"));
            this.date = date;
            this.substitution = data.getBoolean("IsSubstitutionClass");
            if (substitution) {
                this.orgTeacher = new Teacher(data.getJSONObject("orgTeacher"));
                this.orgSubject = new Subject(data.getJSONObject("orgSubject"));
            }
        }

    }

    Event getEvent() {
        return event;
    }

    void setEvent(Event event) {
        this.event = event;
    }

    public Subject getOrgSubject() {
        return orgSubject;
    }

    public Teacher getOrgTeacher() {
        return orgTeacher;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    int getLessonNumber() {
        return lessonNumber;
    }

    Subject getSubject() {
        return subject;

    }

    Teacher getTeacher() {
        return teacher;
    }


    public LocalTime getStartTime() {
        return startTime;
    }

    boolean isSubstitution() {
        return substitution;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
