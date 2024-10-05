package com.hackaton2024.wiliwilowilu;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class UrlFetchBinary extends AsyncTask<String, Void, Byte[]> {
    private byte[] result = null;
    private final Notifier notifier;

    public UrlFetchBinary(Notifier notifier) {
        this.notifier = notifier;
    }

    @Override
    protected Byte[] doInBackground(String... strings) {
        if(strings.length < 2) {
            return null;
        }

        String urlString = strings[0];

        ArrayList<Byte> bytes = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();

                byte[] buffer = new byte[1024];

                int length;
                while ((length = is.read(buffer)) != -1) {
                    for(int i = 0; i < length; i++) {
                        bytes.add(buffer[i]);
                    }
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        result = new byte[bytes.size()];

        for(int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }

        this.notifier.notify(strings[1]);

        return bytes.toArray(new Byte[0]);
    }

    public byte[] getResult() {
        return result;
    }
}
