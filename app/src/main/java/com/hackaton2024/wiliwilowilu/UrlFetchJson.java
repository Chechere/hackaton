package com.hackaton2024.wiliwilowilu;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlFetchJson extends AsyncTask<String, Void, String> {
    private String result = null;
    private final Notifier notifier;

    public UrlFetchJson(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    protected String doInBackground(String... strings) {
        if(strings.length < 2) {
            return "";
        }

        String urlString = strings[0];
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if(strings.length >= 3) {
                conn.setRequestProperty("X-API-Key", strings[2]);
            }

            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        result = response.toString();

        this.notifier.notify(strings[1]);

        return response.toString();
    }

    public String getResult() {
        return result;
    }
}
