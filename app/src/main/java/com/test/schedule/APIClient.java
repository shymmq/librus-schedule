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

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

/**
 * Created by szyme on 14.10.2016.
 */

class APIClient {
    private final String BASE_URL = "https://api.librus.pl/2.0";
    private final String AUTH_URL = "https://api.librus.pl/OAuth/Token";
    private final String TAG = "schedule:log";
    private final String auth_token = "MzU6NjM2YWI0MThjY2JlODgyYjE5YTMzZjU3N2U5NGNiNGY=";
    private String access_token = null;
    private String refresh_token = null;
    private long valid_until = 0;
    private Context context;
    private OkHttpClient client = new OkHttpClient();
    private boolean debug = true;

    APIClient(Context _context) {
        context = _context;
    }

    static void login(String username, String password, final Runnable onSuccess, final Consumer onFailure, final Context c) {
        final String AUTH_URL = "https://api.librus.pl/OAuth/Token";
        final String auth_token = "MzU6NjM2YWI0MThjY2JlODgyYjE5YTMzZjU3N2U5NGNiNGY=";
        OkHttpClient client = new OkHttpClient();
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
                    onFailure.run(response.code());
                try {
                    JSONObject responseJSON = new JSONObject(response.body().string());

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();

                    editor.putString("refresh_token", responseJSON.getString("refresh_token"));
                    editor.putString("access_token", responseJSON.getString("access_token"));
                    editor.putLong("valid_until", System.currentTimeMillis() + responseJSON.getLong("expires_in"));
                    editor.putBoolean("logged_in", true);
                    editor.commit();
                    onSuccess.run();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void log(String text) {
        if (debug) {
            Log.d(TAG, text);
        }
    }

    private void APIRequest(final String endpoint, final Consumer onSuccess) {

        Consumer onRefresh = new Consumer() {
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

        refreshAccess(onRefresh);

    }

    private void refreshAccess(final Consumer onSuccess) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.contains("refresh_token")) {
            throw new Error("Client not logged in");
        } else {
            if (
                    !prefs.contains("valid_until") || !prefs.contains("access_token") || prefs.getLong("valid_until", 0) < System.currentTimeMillis()) {
                //request access with refresh_token
                refresh_token = prefs.getString("refresh_token", null);

                Request request = new Request.Builder()
                        .url(AUTH_URL)
                        .header("Authorization", "Basic " + auth_token)
                        .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "refresh_token=" + refresh_token + "&grant_type=refresh_token&librus_long_term_token=1"))
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful())
                            try {
                                JSONObject responseJSON = new JSONObject(response.body().string());

                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

                                editor.putString("refresh_token", responseJSON.getString("refresh_token"));
                                editor.putString("access_token", responseJSON.getString("access_token"));
                                editor.putLong("valid_until", System.currentTimeMillis() + responseJSON.getLong("expires_in"));
                                editor.commit();
                                onSuccess.run(access_token);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                    }
                });
            } else {
                //return current access_token
                access_token = prefs.getString("access_token", null);
                onSuccess.run(access_token);
            }
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

    private void getEvents(final Consumer onSuccess) {
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
            JSONObject res = new JSONObject();
            for (int eventIndex = 0; eventIndex < eventArray.length(); eventIndex++) {
                JSONObject event = eventArray.getJSONObject(eventIndex);
                JSONObject resEvent = new JSONObject();
                int categoryId = event.getJSONObject("Category").getInt("Id");
                String date = event.getString("Date");
                resEvent.put("Category", categories.get(String.valueOf(categoryId)));
                resEvent.put("Description", event.getString("Content"));
                resEvent.put("LessonNo", event.getString("LessonNo"));
                if (!res.has(date)) {
                    res.put(date, new JSONArray());
                }
                res.getJSONArray(date).put(resEvent);
            }
            log("Resolved events:   " + res.toString());
            prefs.edit().putString("events", res.toString()).commit();
            onSuccess.run(res);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getTimetable(final Consumer onSuccess, LocalDate... weeks) {
        //get current week start date
        final CountDownLatch timetablesLatch = new CountDownLatch(weeks.length);
        final JSONObject timetable = new JSONObject();
        Consumer onDownloaded = new Consumer() {
            @Override
            public void run(Object result) {
                try {
                    JSONObject data = ((JSONObject) result).getJSONObject("Timetable");
                    Iterator iterator = data.keys();
                    log("Downloaded timetable : " + String.valueOf(data));
                    while (iterator.hasNext()) {

                        String key = (String) iterator.next();
                        timetable.put(key, data.getJSONArray(key));

                    }
                    timetablesLatch.countDown();
                    onSuccess.run(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        for (LocalDate weekStart : weeks) {
            APIRequest("/Timetables?weekStart=" + weekStart.toString("yyyy-MM-dd"), onDownloaded);
            log("Requested timetable for " + weekStart.toString("yyyy-MM-dd"));
        }

        try {
            log("Waiting for timetables to download (" + weeks.length + ")");
            timetablesLatch.await();
            log("Timetables downloaded");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("timetable", timetable.toString());
        editor.commit();
        onSuccess.run(timetable);
    }

    void update(Runnable onSuccess) {
        final CountDownLatch latch = new CountDownLatch(2);
        Consumer countDown = new Consumer() {
            @Override
            public void run(Object result) {
                latch.countDown();
            }
        };
        getTimetable(countDown, TimetableUtils.getWeekStart(), TimetableUtils.getWeekStart().plusWeeks(1));
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

//    public void largeLog(String content) {
//        if (content.length() > 4000) {
//            Log.d(TAG, content.substring(0, 4000));
//            largeLog(content.substring(4000));
//        } else {
//            Log.d(TAG, content);
//        }
//    }

    interface Consumer {
        void run(Object result);
    }
}
