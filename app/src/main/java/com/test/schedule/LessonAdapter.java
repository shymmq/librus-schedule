package com.test.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Locale;

class LessonAdapter extends BaseAdapter {
    private static final String TAG = "schedule:log";
    private final Context context;
    private SchoolDay schoolDay;

    LessonAdapter(SchoolDay _schoolDay, Context _context) {
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
        final Lesson lesson = schoolDay.getLesson(position + 1);

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

            final TextView lessonName = (TextView) convertView.findViewById(R.id.lessonSubject);
            final TextView lessonTeacher = (TextView) convertView.findViewById(R.id.lessonTeacher);
            TextView lessonNumber = (TextView) convertView.findViewById(R.id.lessonNumber);
            final CardView badge = (CardView) convertView.findViewById(R.id.badge);
            final TextView badgeText = (TextView) convertView.findViewById(R.id.badgeText);
            final ImageView badgeIcon = (ImageView) convertView.findViewById(R.id.badgeIcon);

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

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (LocalDate.now().equals(lesson.getDate()) && LocalTime.now().isAfter(lesson.getEndTime()) && prefs.getBoolean("greyOutFinishedLessons", true)) {
                convertView.setAlpha(0.35f);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(context).title(lesson.getSubject().getName()).positiveText("Zamknij");

                    LayoutInflater inflater = LayoutInflater.from(context);
                    View details = inflater.inflate(R.layout.details_layout, null);
                    TextView teacher = (TextView) details.findViewById(R.id.details_teacher);
                    TextView orgTeacher = (TextView) details.findViewById(R.id.details_org_teacher);
                    TextView orgSubject = (TextView) details.findViewById(R.id.details_org_subject);
                    TextView subject = (TextView) details.findViewById(R.id.details_subject);
                    RelativeLayout subjectContainer = (RelativeLayout) details.findViewById(R.id.details_subject_container);
                    TextView date = (TextView) details.findViewById(R.id.details_date);
                    TextView startTime = (TextView) details.findViewById(R.id.details_start_time);
                    TextView endTime = (TextView) details.findViewById(R.id.details_end_time);
                    TextView lessonNumber = (TextView) details.findViewById(R.id.details_lesson_number);
                    LinearLayout event = (LinearLayout) details.findViewById(R.id.event);

                    teacher.setText(lesson.getTeacher().getName());
                    date.setText(lesson.getDate().toString("EEEE, d MMMM yyyy", new Locale("pl")));
                    startTime.setText(lesson.getStartTime().toString("hh:mm"));
                    endTime.setText(" - " + lesson.getEndTime().toString("hh:mm"));
                    lessonNumber.setText(lesson.getLessonNumber() + ". lekcja");

                    if (lesson.getEvent() != null) {
                        TextView eventName = (TextView) details.findViewById(R.id.details_event_name);
                        TextView eventDescription = (TextView) details.findViewById(R.id.details_event_description);
                        event.setVisibility(View.VISIBLE);
                        eventName.setText(lesson.getEvent().getCategory());
                        eventDescription.setText(lesson.getEvent().getDescription());
                    } else {
                        event.setVisibility(View.GONE);
                    }
                    if (lesson.isSubstitution()) {
                        if (lesson.getTeacher() != lesson.getOrgTeacher()) {
                            orgTeacher.setText(lesson.getOrgTeacher().getName() + " -> ");
                        }
                        if (lesson.getSubject() != lesson.getOrgSubject()) {
                            orgSubject.setText(lesson.getOrgSubject().getName() + " -> ");
                            subject.setText(lesson.getSubject().getName());
                        }
                    } else {
                        orgTeacher.setVisibility(View.GONE);
                        subjectContainer.setVisibility(View.GONE);
                    }

                    //TODO Ogarnianie zastępstw i odwołań
                    MaterialDialog dialog = builder.customView(details, true).show();
                }
            });
        }
        return convertView;
    }
}
