package com.test.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    final int DAY_MS = 86400000;
    private final String TAG = "schedule:log";
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
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
        SharedPreferences.Editor editor = prefs.edit();
        String syncDate = prefs.getString("lastSynchronization", null);
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
            syncDate = DateTime.now().toString("HH:mm:ss", new Locale("pl"));
        } else {
            log("Redirecting to LoginActivity");
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        editor.putString("lastSynchronization", syncDate);
        editor.commit();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor prefsEditor = sp.edit();
                prefsEditor.putBoolean("logged_in", false);
                prefsEditor.remove("access_token");
                prefsEditor.remove("refresh_token");
                prefsEditor.remove("lastUpdate");
                prefsEditor.commit();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.action_settings:
                Intent j = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(j);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.getBoolean("settings_changed", false)) {
            log("Settings changed, restarting MainActivity");
            preferences.edit().putBoolean("settings_changed", false).commit();
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
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

                sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), timetable);
                viewPager.setAdapter(sectionsPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);

                viewPager.setCurrentItem(0, true);

                log("Tab count : " + TimetableUtils.getDayCount());
                log("Start date : " + TimetableUtils.getStartDate());
                log("Week start : " + TimetableUtils.getWeekStart());
            }
        });

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        Timetable dataset;

        SectionsPagerAdapter(FragmentManager fm, Timetable dataset) {
            super(fm);
            this.dataset = dataset;
        }

        @Override
        public Fragment getItem(int position) {
            LocalDate date = TimetableUtils.getTabDate(position);
//            log("Creating TabFragment for day " + date.toString("yyyy-MM-dd") + " at position " + position);
            return TabFragment.newInstance(dataset.getSchoolDay(date));
        }

        @Override
        public int getCount() {
            return TimetableUtils.getDayCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            return TimetableUtils.getTabTitle(position, preferences.getBoolean("displayDates", true), preferences.getBoolean("useRelativeTabNames", true));
        }
    }
}
