package com.ove.termostat.model;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

public class SetInfoTask extends AsyncTask<String, Integer, Boolean> {

    public interface SetInfoListener{
        void SetInfoComplete(Boolean success);
    }

    public static String SET_TARGET = "TARGET";
    public static String SET_HYST = "HYST";

    private HttpClient mClient;
    private SetInfoListener listener;

    public SetInfoTask(SetInfoListener listener)
    {
        this.listener = listener;
        mClient = HttpClientBuilder.create().build();
    }

    private HttpPost prepareRequest(URI uri, String password){
        HttpPost request = new HttpPost(uri);
        String passwordBase64 = Base64.encodeToString(password.getBytes(), Base64.NO_WRAP);
        request.setHeader("Authorization", "Basic " + passwordBase64);
        return request;
    }

    @Override
    protected void onPostExecute(Boolean response) {
        if(listener != null){
            listener.SetInfoComplete(response);
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String type = params[0];

        String url = params[1];
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        int idx = Integer.parseInt(params[2]);
        int target = Integer.parseInt(params[3]);
        String password = params[4];

        Boolean response = false;

        try {
            Uri uri = Uri.parse(url);
            publishProgress(0);
            if (type.equals(SET_TARGET)) {
                response = setTarget(uri.getAuthority(), idx, target, password);
            } else if (type.equals(SET_HYST)) {
                response = setHyst(uri.getAuthority(), idx, target, password);
            }
            publishProgress(100);
        } catch (URISyntaxException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
            response = false;
        }
        return response;
    }

    private Boolean setTarget(String authority, int idx, int target, String password) throws URISyntaxException, UnsupportedEncodingException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/target")
                    .setParameter(idx + "", null)
                    .build();

            HttpPost httpPost = prepareRequest(uri, password);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("temp", target + ""));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = mClient.execute(httpPost);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Boolean setHyst(String authority, int idx, int target, String password) throws URISyntaxException, UnsupportedEncodingException {
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(authority)
                    .setPath("/hyst")
                    .setParameter(idx + "", null)
                    .build();

            HttpPost httpPost = prepareRequest(uri, password);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("hyst", target + ""));
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = mClient.execute(httpPost);
            return response.getStatusLine().getStatusCode() == 200;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
