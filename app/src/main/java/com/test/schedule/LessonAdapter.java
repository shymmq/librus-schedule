package com.test.schedule;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
            convertView.setAlpha(0.35f);

        } else {

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_layout, null);
            }

            TextView lessonName = (TextView) convertView.findViewById(R.id.lessonSubject);
            TextView lessonTeacher = (TextView) convertView.findViewById(R.id.lessonTeacher);
            TextView lessonNumber = (TextView) convertView.findViewById(R.id.lessonNumber);
            CardView badge = (CardView) convertView.findViewById(R.id.badge);
            TextView badgeText = (TextView) convertView.findViewById(R.id.badgeText);
            ImageView badgeIcon = (ImageView) convertView.findViewById(R.id.badgeIcon);

            lessonNumber.setText(lesson.getLessonNumber() + ".");
            lessonName.setText(lesson.getSubject().getName());
            lessonTeacher.setText(lesson.getTeacher().getName());

            if (lesson.isCanceled()) {
                lessonName.setPaintFlags(lessonName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                lessonTeacher.setPaintFlags(lessonTeacher.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                badge.setVisibility(View.VISIBLE);
                badgeText.setText("odwołane");
                badgeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cancel_black_24dp, context.getTheme()));
            } else if (lesson.getEvent() != null) {
                badge.setVisibility(View.VISIBLE);
                badgeText.setText(lesson.getEvent().getCategory());
                badgeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_event_black_24dp, context.getTheme()));
            } else if (lesson.isSubstitution()) {
                badge.setVisibility(View.VISIBLE);
                badgeText.setText("zastępstwo");
                badgeIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_swap_horiz_black_24dp, context.getTheme()));
            } else {
                badge.setVisibility(View.GONE);
            }
            if (LocalDate.now() == lesson.getDate() && LocalTime.now().isAfter(lesson.getEndTime())) {
                convertView.setAlpha(0.35f);
            }
//            else {
//                FrameLayout background = (FrameLayout) convertView.findViewById(R.id.background);
//                background.setBackgroundColor(Color.argb(10, 0, 0, 0));
//            }
        }
        return convertView;
    }
}
