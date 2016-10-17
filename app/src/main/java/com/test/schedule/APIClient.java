package com.test.schedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

/**
 * Created by szyme on 14.10.2016.
 */

public class APIClient {
    private final String BASE_URL = "https://api.librus.pl/2.0";
    private final String AUTH_URL = "https://api.librus.pl/OAuth/Token";
    private String auth_token;
    private String username, password, access_token;
    private long valid_until = 0;
    private Context context;
    private OkHttpClient client = new OkHttpClient();
    private boolean debug = true;
    private final String TAG = "schedule:log";

    public APIClient(String _auth_token, String _username, String _password, Context _context) {
        auth_token = _auth_token;
        username = _username;
        password = _password;
        context = _context;
    }

    void log(String text) {
        if (debug) {
            Log.d(TAG, text);
        }
    }

    interface Consumer {
        void run(Object result);
    }

    private void APIRequest(final String endpoint, final Consumer onSuccess) {
        Consumer onGetToken = new Consumer() {
            @Override
            public void run(Object result) {
                Request request = new Request.Builder().addHeader("Authorization", "Bearer " + result)
                        .url(BASE_URL + endpoint)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);
                        try {
                            onSuccess.run(new JSONObject(response.body().string()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        getAccessToken(onGetToken);

    }

    private void getAccessToken(final Consumer onSuccess) {

        if (System.currentTimeMillis() < valid_until) {
            onSuccess.run(access_token);
        } else {
            Request request = new Request.Builder()
                    .url(AUTH_URL)
                    .header("Authorization", "Basic " + auth_token)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "username=" + username + "&password=" + password + "&grant_type=password&librus_long_term_token=1"))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);
                    try {
                        JSONObject responseJSON = new JSONObject(response.body().string());
                        String _access_token = responseJSON.getString("access_token");
                        valid_until = System.currentTimeMillis() + responseJSON.getLong("expires_in");
                        access_token = _access_token;
                        onSuccess.run(_access_token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void getEventCategories(final Consumer onSuccess) {
        Consumer onDownloaded = new Consumer() {
            @Override
            public void run(Object result) {
                JSONObject data = (JSONObject) result;
                try {
                    JSONObject res = new JSONObject();
                    JSONArray categories = data.getJSONArray("Categories");
                    JSONObject category;
                    for (int i = 0; i < categories.length(); i++) {
                        category = categories.getJSONObject(i);
                        res.put(category.getString("Id"), category.getString("Name"));
                    }
                    log("Event categories:   " + res);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("categories", res.toString());
                    editor.commit();
                    onSuccess.run(res);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        APIRequest("/HomeWorks/Categories", onDownloaded);
    }

    private void getEventEntries(final Consumer onSuccess) {
        Consumer onDownloaded = new Consumer() {
            @Override
            public void run(Object result) {
                JSONObject data = (JSONObject) result;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString("event_entries", data.toString());
                editor.commit();
                onSuccess.run(data);
            }
        };
        APIRequest("/HomeWorks", onDownloaded);
    }

    public void getEvents(final Consumer onSuccess) {
        //setup CountDownLatch
        final CountDownLatch latch = new CountDownLatch(2);
        Consumer countDown = new Consumer() {
            @Override
            public void run(Object result) {
                latch.countDown();
            }
        };
        //start asynchronous tasks
        getEventEntries(countDown);
        getEventCategories(countDown);
        //wait for task to finish
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //tasks finished
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            JSONObject categories = new JSONObject(prefs.getString("categories", "{}"));
            JSONObject events = new JSONObject(prefs.getString("event_entries", "{}"));
            JSONArray eventArray = events.getJSONArray("HomeWorks");
            JSONArray res = new JSONArray();
            for (int eventIndex = 0; eventIndex < eventArray.length(); eventIndex++) {
                JSONObject event = eventArray.getJSONObject(eventIndex);
                int categoryId = event.getJSONObject("Category").getInt("Id");
                JSONObject resEvent = new JSONObject();
                resEvent.put("Category", categories.get(String.valueOf(categoryId)));
                resEvent.put("Description", event.getString("Content"));
                resEvent.put("LessonNo", event.getString("LessonNo"));
                resEvent.put("Date", event.getString("Date"));
                res.put(resEvent);
            }
            log("Resolved events:   " + res.toString());
            prefs.edit().putString("events", res.toString()).commit();
            onSuccess.run(res);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getTimetable(final Consumer onSuccess) {
        //get current week start date
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.DAY_OF_YEAR,2);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String weekStart = df.format(calendar.getTime());

        Consumer onDownloaded = new Consumer() {
            @Override
            public void run(Object result) {
                JSONObject data = (JSONObject) result;

                try {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("timetable", String.valueOf((data.getJSONObject("Timetable"))));
                    editor.putString("week_start", weekStart);
                    editor.commit();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onSuccess.run(data);
            }
        };
        APIRequest("/Timetables?weekStart=" + weekStart, onDownloaded);
    }

    public void largeLog(String content) {
        if (content.length() > 4000) {
            Log.d(TAG, content.substring(0, 4000));
            largeLog(content.substring(4000));
        } else {
            Log.d(TAG, content);
        }
    }

    public void update(Runnable onSuccess) {
        final CountDownLatch latch = new CountDownLatch(2);
        Consumer countDown = new Consumer() {
            @Override
            public void run(Object result) {
                latch.countDown();
            }
        };
        getTimetable(countDown);
        getEvents(countDown);
        log("Waiting for all tasks to finish..");
        try {
            latch.await();
            log("Finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong("lastUpdate", System.currentTimeMillis());
        editor.commit();

        onSuccess.run();
    }
}
