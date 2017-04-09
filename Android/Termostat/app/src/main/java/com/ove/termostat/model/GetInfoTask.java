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

public class GetInfoTask extends AsyncTask<String, Void, Void> {

    public interface InfoListener {
        void setInfo(String[] names, float[] currentTemps, int[] targetTemps, int[] hysteresis);
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

    private void getAllInfo(String authority, String password) throws URISyntaxException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/info")
                    .build();

            HttpUriRequest request = prepareRequest(uri, password);

            HttpResponse response = mClient.execute(request);
            BasicResponseHandler handler = new BasicResponseHandler();
            String resp = handler.handleResponse(response);

            String[] parts = resp.split("[\r\n]+");
            int length = Integer.parseInt(parts[0]);
            names = new String[length];
            currentTemps = new float[length];
            targetTemps = new int[length];
            hysteresis = new int[length];
            for (int i = 0; i < length; i++) {
                names[i] = parts[1 + 4*i];
                currentTemps[i] = Float.parseFloat(parts[1 + 4*i + 1]);
                targetTemps[i] = Integer.parseInt(parts[1 + 4*i + 2]);
                hysteresis[i] = Integer.parseInt(parts[1 + 4*i + 3]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
    protected Void doInBackground(String... params) {
        String url = params[0];
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        String pwd = params[1];

        try {
            Uri uri = Uri.parse(url);
            getAllInfo(uri.getAuthority(), pwd);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        if(listener != null) {
            listener.setInfo(names, currentTemps, targetTemps, hysteresis);
            listener.hideProgress();
        }
    }
}