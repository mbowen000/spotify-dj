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

import bowen.com.spotify_app.models.TrackView;
import kaaes.spotify.webapi.android.models.ArtistSimple;

/**
 * Created by mike on 1/1/16.
 */
public class Track {

    public static void getCurrentTracks(final VolleyCallbackArray callback, Context ctx) {
        final ArrayList<TrackView> listOfTracks = new ArrayList<TrackView>();

        // todo: move this so its not instantiated here (i think?)
        RequestQueue queue = Volley.newRequestQueue(ctx);

        String url = MainActivity.HOST_URI + "/queue";

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
                                if(!tracksJson.isNull(i)) {
                                    JSONObject trackJson = tracksJson.getJSONObject(i);
                                    TrackView tv = new TrackView();
                                    tv.setName(trackJson.getString("name"));
                                    tv.setUri(trackJson.getString("uri"));
                                    // todo: set artist here (get the first one)
                                    JSONArray artists = trackJson.getJSONArray("artists");
                                    tv.setArtist(artists.getJSONObject(0).getString("name"));
                                    listOfTracks.add(tv);
                                }
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
        String url = MainActivity.HOST_URI + "/queue";

        // todo: move this so its not instantiated here (i think?)
        RequestQueue queue = Volley.newRequestQueue(ctx);

        // Request a string response from the provided URL.
        JSONObject reqBody = new JSONObject();
        try {
            reqBody.put("uri", track.uri);
            reqBody.put("name", track.name);

            JSONArray artistArray = new JSONArray();
            for(ArtistSimple artist : track.artists) {
                JSONObject artistObject = new JSONObject();
                artistObject.put("name", artist.name);
                artistArray.put(artistObject);
            }

            reqBody.put("artists", artistArray);
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

    public static void playTrack(int index, Context ctx, final VolleyCallback callback) {
        String url = MainActivity.HOST_URI + "/queue/play";

        // todo: move this so its not instantiated here (i think?)
        RequestQueue queue = Volley.newRequestQueue(ctx);

        String reqBody = "";
        url = url + "/" + String.valueOf(index);

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
