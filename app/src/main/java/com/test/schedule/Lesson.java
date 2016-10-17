package com.test.schedule;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by szyme on 15.10.2016.
 */

public class Lesson {


    private final String TAG = "schedule:log";
    private Event event = null;
    private Subject subject;
    private Teacher teacher;
    //    private Subject orgSubject;
//    private Teacher orgTeacher;
    private boolean substitution = false;
    private int lessonNumber;
    private boolean isCanceled;

    public Lesson(Subject subject, Teacher teacher, int lessonNumber) {
        this.subject = subject;
        this.teacher = teacher;
        this.lessonNumber = lessonNumber;
        this.substitution = false;
    }

    Event getEvent() {
        return event;
    }

    void setEvent(Event event) {
        this.event = event;
    }
    //    public Lesson(Subject subject, Teacher teacher, Subject orgSubject, Teacher orgTeacher, int lessonNumber) {
//        this.subject = subject;
//        this.teacher = teacher;
//        this.orgSubject = orgSubject;
//        this.orgTeacher = orgTeacher;
//        this.lessonNumber = lessonNumber;
//        this.substitution = true;
//    }

    Lesson(JSONObject data, int lessonNumber) throws JSONException {
        this.lessonNumber = lessonNumber;
//        Log.d(TAG, "Creating lesson from JSON:   " + data.toString());
        if (data.length() > 0) {
            this.substitution = data.getBoolean("IsSubstitutionClass");
            this.isCanceled = data.getBoolean("IsCanceled");
            this.subject = new Subject(data.getJSONObject("Subject"));
            this.teacher = new Teacher(data.getJSONObject("Teacher"));
            if (substitution) {
//                this.orgTeacher = new Teacher(data.getJSONObject("orgTeacher"));
//                this.orgSubject = new Subject(data.getJSONObject("orgSubject"));
            } else {
//                this.orgSubject = this.subject;
//                this.orgTeacher = this.teacher;
            }
        }
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

//    public Subject getOrgSubject() {
//        return orgSubject;
//    }
//
//    public Teacher getOrgTeacher() {
//        return orgTeacher;
//    }

    boolean isSubstitution() {
        return substitution;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
