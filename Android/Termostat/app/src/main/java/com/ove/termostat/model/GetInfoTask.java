package com.ove.termostat.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

public class GetInfoTask extends AsyncTask<String, Integer, Void> {

    public interface InfoListener {
        void setNames(String[] names);
        void setCurrentTemps(float[] currentTemps);
        void setTargetTemps(int[] targetTemps);
        void setHysteresis(int[] hysteresis);
        void showProgress(int progress);
        void hideProgress();
    }

    private InfoListener listener;

    private String[] names;
    private float[] currentTemps;
    private int[] targetTemps;
    private int[] hysteresis;

    private HttpClient mClient;

    public GetInfoTask(InfoListener listener) {
        this.listener = listener;
        mClient = HttpClientBuilder.create().build();
    }

    private HttpUriRequest prepareRequest(URI uri, String password){
        HttpUriRequest request = new HttpGet(uri);
        String passwordBase64 = Base64.encodeToString(password.getBytes(), Base64.NO_WRAP);
        request.setHeader("Authorization", "Basic " + passwordBase64);
        return request;
    }

    private void getNames(String authority, String password) throws URISyntaxException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/name")
                    .build();

            HttpUriRequest request = prepareRequest(uri, password);

            HttpResponse response = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String resp = handler.handleResponse(response);

            String[] parts = resp.split("[\r\n]+");
            int length = Integer.parseInt(parts[0]);
            names = new String[length];
            for (int i = 0; i < length; i++) {
                names[i] = parts[i + 1];
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getCurrentTemps(String authority, String password) throws URISyntaxException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/temp")
                    .build();

            HttpUriRequest request = prepareRequest(uri, password);
            HttpResponse response = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String resp = handler.handleResponse(response);

            String[] parts = resp.split("[\r\n]+");
            int length = Integer.parseInt(parts[0]);
            currentTemps = new float[length];
            for (int i = 0; i < length; i++) {
                currentTemps[i] = Float.parseFloat(parts[i + 1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getTargetTemps(String authority, String password) throws URISyntaxException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/target")
                    .build();

            HttpUriRequest request = prepareRequest(uri, password);
            HttpResponse response = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String resp = handler.handleResponse(response);

            String[] parts = resp.split("[\r\n]+");
            int length = Integer.parseInt(parts[0]);
            targetTemps = new int[length];
            for (int i = 0; i < length; i++) {
                targetTemps[i] = Integer.parseInt(parts[i + 1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getHysteresis(String authority, String password) throws URISyntaxException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/hyst")
                    .build();

            HttpUriRequest request = prepareRequest(uri, password);
            HttpResponse response = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String resp = handler.handleResponse(response);

            String[] parts = resp.split("[\r\n]+");
            int length = Integer.parseInt(parts[0]);
            hysteresis = new int[length];
            for (int i = 0; i < length; i++) {
                hysteresis[i] = Integer.parseInt(parts[i + 1]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        listener.showProgress(0);
    }

    @Override
    protected void onCancelled() {
        listener.hideProgress();
    }

    @Override
    protected void onCancelled(Void aVoid) {
        listener.hideProgress();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        listener.showProgress(values[0]);
        setResults();
    }

    @Override
    protected Void doInBackground(String... params) {
        String url = params[0];
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        String pwd = params[1];

        try {
            Uri uri = Uri.parse(url);
            publishProgress(0);
            getNames(uri.getAuthority(), pwd);
            publishProgress(25);
            getCurrentTemps(uri.getAuthority(), pwd);
            publishProgress(50);
            getTargetTemps(uri.getAuthority(), pwd);
            publishProgress(75);
            getHysteresis(uri.getAuthority(), pwd);
            publishProgress(100);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        setResults();
        listener.hideProgress();
    }

    private void setResults(){
        if (listener != null) {
            if (names != null) {
                listener.setNames(names);
            }
            if (currentTemps != null) {
                listener.setCurrentTemps(currentTemps);
            }
            if (targetTemps != null) {
                listener.setTargetTemps(targetTemps);
            }
            if (hysteresis != null) {
                listener.setHysteresis(hysteresis);
            }
        }
    }
}