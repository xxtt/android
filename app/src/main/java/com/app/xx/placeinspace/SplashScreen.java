package com.app.xx.placeinspace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SplashScreen extends Activity {

    public static final String HTTP = "http";
    private ImageView splashImageView;
    TextView text;
    String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        ImageView image = (ImageView) findViewById(R.id.splash_image);
        text = (TextView) findViewById(R.id.splash_text);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isNetworkAvailable()) {
                            text.setText(getResources().getString(R.string.splash_text));
                            new JsonHttpRequest().execute();
                        }
                    }
                });
            }
        });

        new JsonHttpRequest().execute();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    class JsonHttpRequest extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            json = http();
            return null;
        }

        protected void onPostExecute(String file_url) {
            if (json != null) {
                start();
            } else
                text.setText(getResources().getString(R.string.splash_try_again));
        }
    }

    private void start() {
        Intent i = new Intent(SplashScreen.this, MapsActivity.class);
        i.putExtra(HTTP, json);
        startActivity(i);
        finish();
    }

    public String http() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://placeins.com/admin/get.php");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return builder.toString();
    }
}