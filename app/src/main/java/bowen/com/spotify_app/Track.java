package bowen.com.spotify_app;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mike on 1/1/16.
 */
public class Track {

    public static void getCurrentTracks(final VolleyCallback callback, Context ctx) {
        final ArrayList<String> listOfTracks = new ArrayList<String>();

        // todo: move this so its not instantiated here (i think?)
        RequestQueue queue = Volley.newRequestQueue(ctx);

        String url ="http://192.168.0.106:3000/track";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response from server: ", response);
                        try {
                            JSONArray tracksJson = new JSONArray(response);
                            for(int i =0; i<tracksJson.length(); i++) {
                                JSONObject trackJson = tracksJson.getJSONObject(i);
                                listOfTracks.add(trackJson.getString("uri"));
                            }
                            callback.onSuccess(listOfTracks);
                        }
                        catch(JSONException jse) {
                            jse.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Log.d("Error", error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    public static void addTrack(kaaes.spotify.webapi.android.models.Track track, Context ctx, final VolleyCallback callback) {
        String url = "http://192.168.0.106:3000/track";

        // todo: move this so its not instantiated here (i think?)
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        JSONObject reqBody = new JSONObject();
        try {
            reqBody.put("uri", track.uri);
        }
        catch(JSONException jse) {
            jse.printStackTrace();
        }

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, reqBody,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response from server: ", response.toString());
                        callback.onSuccess(new ArrayList<String>());
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Log.d("Error", error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }
}
