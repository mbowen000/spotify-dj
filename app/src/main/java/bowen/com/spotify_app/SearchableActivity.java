package bowen.com.spotify_app;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;

public class SearchableActivity extends AppCompatActivity {

    private ArrayList<Track> results = new ArrayList<Track>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, new ArrayList<String>());

        ListView listView = (ListView)findViewById(R.id.search_results);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(mMessageClickedHandler);

    }

    private void doMySearch(String query) {
        SpotifyService sc = SpotifyConnection.apiConnection.getService();

        sc.searchTracks(query, new SpotifyCallback<TracksPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {

            }
            @Override
            public void success(TracksPager tracksPager, Response response) {
                Log.d("search response: ", tracksPager.toString());
                adapter.clear();
                results.addAll(tracksPager.tracks.items);
                for(int i=0; i<tracksPager.tracks.items.size(); i++) {
                    adapter.add(tracksPager.tracks.items.get(i).name);
                }


            }
        });
    }


    // Create a message handling object as an anonymous class.
    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // Do something in response to the click
            Track t = results.get(position);
            Log.d("Track: ", t.uri);
            bowen.com.spotify_app.Track.addTrack(t, getApplicationContext(), new VolleyCallback() {
                @Override
                public void onSuccess(ArrayList<String> result) {
                    Log.d("Response", "");
                }
            });
        }
    };


}
