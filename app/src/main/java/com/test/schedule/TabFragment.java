package com.test.schedule;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Locale;

public class TabFragment extends Fragment {
    private static final String TAG = "schedule:log";

    public TabFragment() {
    }

    public static TabFragment newInstance(SchoolDay data) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putParcelable("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SchoolDay schoolDay = getArguments().getParcelable("data");

        if (schoolDay == null || schoolDay.isEmpty()) {
            View rootView = inflater.inflate(R.layout.fragment_empty, container, false);
            return rootView;
        } else {
            LessonAdapter adapter;
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView list = (ListView) rootView.findViewById(R.id.listView);

            Log.d(TAG, String.valueOf(schoolDay.getLessons().size()));

            /*
            for (int i = 1; i < schoolDay.getLessons().size(); i++) {
                Lesson currentLesson = schoolDay.getLesson(i);

                if (LocalDate.now().equals(currentLesson.getDate()) && LocalTime.now().isAfter(currentLesson.getStartTime()) && LocalTime.now().isBefore(currentLesson.getEndTime())) {
                    TextView lessonSubject = (TextView) list.getChildAt(i).findViewById(R.id.lessonSubject);
                    //lessonSubject.setTypeface(lessonSubject.getTypeface(), Typeface.BOLD);
                } else {
                    //TextView lessonSubject = (TextView) list.getChildAt(i).findViewById(R.id.lessonSubject);
                    //lessonSubject.setTypeface(lessonSubject.getTypeface(), Typeface.NORMAL);
                }

                if (i == schoolDay.getLessons().size() && LocalDate.now().equals(currentLesson.getDate()) && LocalTime.now().isAfter(currentLesson.getEndTime())) {
                    //Show next day or week.
                }
            }
            */

            if (!PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("showDividers", true)) {
                list.setDivider(null);
                list.setDividerHeight(0);
            }
            adapter = new LessonAdapter(schoolDay, getActivity());
            list.setAdapter(adapter);
            return rootView;
        }
    }
}