package com.example.a1401061.eatnear;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRequest extends AsyncTask<String, Void, Void>  {

    private String returnEntry;
    private boolean finished;

    public void readResponse(BufferedReader in) {
        String tmp = "";
        StringBuffer response = new StringBuffer();
        do {
            try {
                tmp = in.readLine();
            }
            catch (IOException ex) {

            }
            if (tmp != null) {
                response.append(tmp);
            }
        } while (tmp != null);
        returnEntry = response.toString();
    }

    public void sendPostRequest (String where) {
        URL loc = null;
        HttpURLConnection conn = null;
        InputStreamReader is;
        BufferedReader in;
        try {
            loc = new URL(where);
        }
        catch (MalformedURLException ex) {
            return;
        }
        try {
            conn = (HttpURLConnection)loc.openConnection();
            conn.setRequestProperty("Accept", " application/json");
            conn.setRequestProperty("user-key", "API"/*ADD ZOMATO API KEY HERE*/);
            is = new InputStreamReader (conn.getInputStream(), "UTF-8");
            in = new BufferedReader (is);
            readResponse (in);
        }
        catch (IOException ex) {
            Log.e("IOException",ex.toString());
        }
        finally {
            conn.disconnect();
        }
    }

    public String getReturnEntry() {
        if (!finished) {
            return "Hold tight!";
        }
        return returnEntry;
    }

    public JSONObject getResultAsJSON() {
        JSONArray jarr = null;
        JSONObject object = null;
        if (finished == false) {
            return null;
        }
        try {
            object = new JSONObject(returnEntry);
            //jarr = object.getJSONArray("cuisines");
            //Log.d ("JSONARRAY",jarr.toString());
        }
        catch (JSONException ex) {
            Log.d ("Output", "Error is " + ex.getMessage());
        }
        return object;
    }

    @Override
    protected void onPostExecute(Void result) {
        finished = true;
        Log.d("Output", returnEntry);
    }

    @Override
    protected Void doInBackground(String... params) {
        finished = false;
        sendPostRequest (params[0]);
        return null;
    }
}