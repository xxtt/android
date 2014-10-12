package com.example.xx.placeinspace;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

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
    String json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        splashImageView = new ImageView(this);
        splashImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        splashImageView.setImageResource(R.drawable.splash);
        setContentView(splashImageView);

        new JsonHttpRequest().execute();
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
            start();
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
        HttpGet httpGet = new HttpGet("http://place-in-space.esy.es/get.php");
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
            Toast.makeText(getBaseContext(), "json read exception", Toast.LENGTH_SHORT).show();
        }
        return builder.toString();
    }
}