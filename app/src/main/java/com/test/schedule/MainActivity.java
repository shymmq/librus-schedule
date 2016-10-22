package com.test.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter sectionsPagerAdapter;
    private final String TAG = "schedule:log";
    private ViewPager viewPager;
    final int DAY_MS = 86400000;
    private boolean debug = true;

    private Timetable timetable;

    void log(String text) {
        if (debug) {
            Log.d(TAG, text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long timeNow = System.currentTimeMillis();
        if (timeNow - prefs.getLong("lastUpdate", 0) < DAY_MS) {
            log("Loading from cache");
            display();
        } else if (prefs.getBoolean("logged_in", false)) {
            APIClient client = new APIClient(getApplicationContext());
            Runnable onSuccess = new Runnable() {
                @Override
                public void run() {
                    display();
                }
            };
            client.update(onSuccess);
        } else {
            log("Redirecting to LoginActivity");
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

    }


    public Timetable loadTimetable() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        try {
            JSONObject eventsJSON = new JSONObject(prefs.getString("events", ""));
            JSONObject timetableJSON = new JSONObject(prefs.getString("timetable", ""));
            Timetable res = new Timetable(timetableJSON, eventsJSON);
            log("loaded timetable from sharedprefs");
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    void display() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timetable = loadTimetable();

                setTheme(R.style.AppTheme_NoActionBar);
                setContentView(R.layout.activity_main);

                viewPager = (ViewPager) findViewById(R.id.container);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

                setSupportActionBar(toolbar);

                sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);

                viewPager.setCurrentItem(0, true);

                log("Tab count : " + TimetableUtils.getDayCount());
                log("Start date : "+TimetableUtils.getStartDate());
            }
        });

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            LocalDate date = TimetableUtils.getTabDate(position);
            log("Creating TabFragment for day " + date.toString("yyyy-MM-dd")+" at position "+ position);
            return TabFragment.newInstance(timetable.getSchoolDay(date));
        }

        @Override
        public int getCount() {
            return TimetableUtils.getDayCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            ;
            return TimetableUtils.getTabTitle(position);
        }
    }
}
