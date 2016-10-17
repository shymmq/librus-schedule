package com.test.schedule;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by szyme on 22.09.2016.
 */

public class LessonAdapter extends BaseAdapter {
    private final Context context;
    private SchoolDay schoolDay;
    private static final String TAG = "schedule:log";

    public LessonAdapter(SchoolDay _schoolDay, Context _context) {
        this.schoolDay = _schoolDay;
        this.context = _context;
    }

    @Override
    public int getCount() {
        return schoolDay.getLessons().size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        Lesson lesson = schoolDay.getLesson(position + 1);

        if (lesson == null) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.empty_item_layout, null);
            }

            TextView index = (TextView) convertView.findViewById(R.id.lessonNumber);
            index.setText(position + 1 + ".");

        } else if (!lesson.isCanceled()) {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_layout, null);
            }

            TextView lessonName = (TextView) convertView.findViewById(R.id.lessonSubject);
            TextView lessonTeacher = (TextView) convertView.findViewById(R.id.lessonTeacher);
            TextView lessonNumber = (TextView) convertView.findViewById(R.id.lessonNumber);
            CardView subBadge = (CardView) convertView.findViewById(R.id.subBadge);
            CardView eventBadge = (CardView) convertView.findViewById(R.id.eventBadge);
            TextView eventName = (TextView) convertView.findViewById(R.id.eventText);

            lessonNumber.setText(lesson.getLessonNumber() + ".");
            lessonName.setText(lesson.getSubject().getName());
            lessonTeacher.setText(lesson.getTeacher().getName());

            if (lesson.isSubstitution()) {
                subBadge.setVisibility(View.VISIBLE);

            } else {
                subBadge.setVisibility(View.GONE);
            }
            if (lesson.getEvent() != null) {
                eventBadge.setVisibility(View.VISIBLE);
                eventName.setText(lesson.getEvent().getCategory());
            } else {
                eventBadge.setVisibility(View.GONE);
            }
        } else {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.canceled_item_layout, null);
            }

            TextView lessonName = (TextView) convertView.findViewById(R.id.canceledLessonSubject);
            TextView lessonTeacher = (TextView) convertView.findViewById(R.id.canceledLessonTeacher);
            TextView lessonNumber = (TextView) convertView.findViewById(R.id.canceledLessonNumber);

            lessonNumber.setText(lesson.getLessonNumber() + ".");
            lessonName.setText(lesson.getSubject().getName());
            lessonTeacher.setText(lesson.getTeacher().getName());

            lessonName.setPaintFlags(lessonName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            lessonTeacher.setPaintFlags(lessonTeacher.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        return convertView;
    }
}
