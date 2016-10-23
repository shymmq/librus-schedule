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
    //    private Subject orgSubject;
//    private Teacher orgTeacher;
    private boolean substitution = false;
    private int lessonNumber;
    private boolean isCanceled;
    //    private DateTime dateTime;
    private LocalDate date;
    private LocalTime endTime;
    private LocalTime startTime;

    Lesson(JSONObject data, int lessonNumber, LocalDate date) throws JSONException, ParseException {
        this.lessonNumber = lessonNumber;
//        Log.d(TAG, "Creating lesson from JSON:   " + data.toString());
        if (data.length() > 0) {
            this.substitution = data.getBoolean("IsSubstitutionClass");
            this.isCanceled = data.getBoolean("IsCanceled");
            this.subject = new Subject(data.getJSONObject("Subject"));
            this.teacher = new Teacher(data.getJSONObject("Teacher"));
            endTime = LocalTime.parse(data.getString("HourTo"), DateTimeFormat.forPattern("HH:mm"));
            startTime = LocalTime.parse(data.getString("HourFrom"), DateTimeFormat.forPattern("HH:mm"));
            this.date = date;
        }
        if (substitution) {
//                this.orgTeacher = new Teacher(data.getJSONObject("orgTeacher"));
//                this.orgSubject = new Subject(data.getJSONObject("orgSubject"));
        } else {
//                this.orgSubject = this.subject;
//                this.orgTeacher = this.teacher;
        }
    }

    Event getEvent() {
        return event;
    }
    //    public Lesson(Subject subject, Teacher teacher, Subject orgSubject, Teacher orgTeacher, int lessonNumber) {
//        this.subject = subject;
//        this.teacher = teacher;
//        this.orgSubject = orgSubject;
//        this.orgTeacher = orgTeacher;
//        this.lessonNumber = lessonNumber;
//        this.substitution = true;
//    }

    void setEvent(Event event) {
        this.event = event;
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
