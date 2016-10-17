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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private final String TAG = "schedule:log";
    private ViewPager mViewPager;
    final int DAY_MS = 86400000;
    private boolean debug = true;
    private final String auth_token = "MzU6NjM2YWI0MThjY2JlODgyYjE5YTMzZjU3N2U5NGNiNGY=";
    private Timetable timetable;

    void log(String text) {
        if (debug) {
            Log.d(TAG, text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long timeNow = System.currentTimeMillis();
        if (timeNow - prefs.getLong("lastUpdate", 0) < DAY_MS) {
            log("Loading from cache");
            display();
        } else {

            if (extras != null && extras.containsKey("username") && extras.containsKey("password")) {
                //if coming from LoginActivity
                log("Found credentials from intent");
                update(extras.getString("username"), extras.getString("password"));
            } else if (prefs.contains("username") && prefs.contains("password")) {
                //if credentials stored in prefs
                log("Found credentials in SharedPreferences");
                update(prefs.getString("username", null), prefs.getString("password", null));
            } else {
                log("Redirecting to LoginActivity");
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }

        }


    }

    public Timetable loadTimetable() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        try {
            JSONArray eventsJson = new JSONArray(prefs.getString("events", ""));
            JSONObject timetableJson = new JSONObject(prefs.getString("timetable", ""));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date weekStart = df.parse(prefs.getString("week_start", null));
            Timetable res = new Timetable(timetableJson, weekStart);
            res.setEvents(eventsJson);
            log("loaded timetable from sharedprefs");
            return res;
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    void update(String username, String password) {
        APIClient client = new APIClient(auth_token, username, password, getApplicationContext());
        Runnable onSuccess = new Runnable() {
            @Override
            public void run() {
                display();
            }
        };
        client.update(onSuccess);
    }


    void display() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log("Loading timetable...");
                timetable = loadTimetable();
                log("Timetable loaded, starting MainActivity");
                setTheme(R.style.AppTheme_NoActionBar);
                setContentView(R.layout.activity_main);
                mViewPager = (ViewPager) findViewById(R.id.container);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                setSupportActionBar(toolbar);
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
                mViewPager.setAdapter(mSectionsPagerAdapter);
                tabLayout.setupWithViewPager(mViewPager);
                Calendar calendar = Calendar.getInstance();
                int day = Math.min(calendar.get(Calendar.DAY_OF_WEEK) - 2, 4);
//                int day = 3;
                mViewPager.setCurrentItem(day, true);
            }
        });

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TabFragment.newInstance(timetable.getSchoolDay(position));
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Poniedziałek";
                case 1:
                    return "Wtorek";
                case 2:
                    return "Środa";
                case 3:
                    return "Czwartek";
                case 4:
                    return "Piątek";
                case 5:
                    return "Sobota";
                case 6:
                    return "Niedziela";
            }
            return null;
        }
    }
}
