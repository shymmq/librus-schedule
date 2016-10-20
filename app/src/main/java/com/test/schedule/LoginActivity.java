package com.test.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "schedule:log";
    private static final String BASE_URL = "https://api.librus.pl/2.0";
    //    private OkHttpClient client = new OkHttpClient();
    private final String auth_token = "MzU6NjM2YWI0MThjY2JlODgyYjE5YTMzZjU3N2U5NGNiNGY=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);

        setContentView(R.layout.activity_login);

        final EditText passwordInput = (EditText) findViewById(R.id.password_input);
        final EditText usernameInput = (EditText) findViewById(R.id.username_input);
        final Button loginButton = (Button) findViewById(R.id.login_btn);
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString("username", usernameInput.getText().toString());
//                editor.putString("password", passwordInput.getText().toString());
//                editor.apply();
                Runnable onSuccess = new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
//                i.putExtra("username", usernameInput.getText().toString());
//                i.putExtra("password", passwordInput.getText().toString());
                        startActivity(i);
                        finish();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                };
                APIClient.Consumer onFailure = new APIClient.Consumer() {
                    @Override
                    public void run(Object result) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setVisibility(View.INVISIBLE);
                            }
                        });
                        Snackbar snackbar = Snackbar
                                .make(findViewById(R.id.coordinator), "Nieprawidłowe hasło, spróbuj ponownie", Snackbar.LENGTH_SHORT);

                        snackbar.show();
                        Log.d(TAG, "run: login failure, code " + (int) result);
                    }
                };
                APIClient.login(usernameInput.getText().toString(), passwordInput.getText().toString(), onSuccess, onFailure, getApplicationContext());
                progress.setVisibility(View.VISIBLE);
            }
        });

    }
}
