package bowen.com.spotify_app;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.socketio.client.Socket;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import bowen.com.spotify_app.models.TrackView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.github.nkzawa.socketio.client.IO;

public class MainActivity extends AppCompatActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "5cc6f71703df4006b7d77b7e2dd85d97";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "mike://callback";

    public static final String HOST_URI = "http://teamtunes.herokuapp.com";

    private Player mPlayer;

    private PlayerState mPlayerState;

    //private SpotifyConnection spotifyConnection = new SpotifyConnection();

    private String accessToken;
    // Request code that will be used to verify if the result comes from correct activity
    // Can be any integer
    private static final int REQUEST_CODE = 1337;

    private List<TrackView> trackList = new ArrayList<TrackView>();

    private int currentTrackIdx = 0;

    private ArrayAdapter<TrackView> trackAdapter;

    // wifi direct related stuff
    public static boolean isWifiDirectSupported = false;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(MainActivity.HOST_URI);
        } catch (URISyntaxException e) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.showContextMenu();

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        SpotifyConnection.apiConnection = new SpotifyApi();

        Track.getCurrentTracks(new VolleyCallbackArray() {
            @Override
            public void onSuccess(List<TrackView> result) {
                trackList = result;
                List<TrackView> listForAdapter = new ArrayList<TrackView>();
                trackAdapter = new ArrayAdapter(getApplicationContext(),
                        android.R.layout.simple_list_item_1, listForAdapter);

                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(trackAdapter);
                listView.setOnItemClickListener(itemClickedHandler);
            }
        }, this);

        mSocket.on("trackUpvoted", onNewMessage);
        mSocket.on("trackAdded", onNewMessage);
        mSocket.on("server-start", onServerStart);
        mSocket.connect();

        // wifi-direct related stuff
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast peersToast = Toast.makeText(getApplicationContext(), "Looking for peers!", Toast.LENGTH_LONG);
                peersToast.show();
            }

            @Override
            public void onFailure(int reason) {
                Toast peersToast = Toast.makeText(getApplicationContext(), "Peer detection failed.", Toast.LENGTH_LONG);
                peersToast.show();
            }
        });


    }

    private Listener onNewMessage = new Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(args.length > 0) {
                        JSONObject data = (JSONObject) args[0];
                        String uri;
                        try {
                            uri = data.getString("uri");

                        } catch (JSONException e) {
                            return;
                        }

                        Toast socketToast = Toast.makeText(getApplicationContext(), "New Track Recieved: " + uri, Toast.LENGTH_SHORT);
                        socketToast.show();
                    }

                    refreshResults();
                    // add the message to view
                    //addMessage(username, message);

                }
            });
        }
    };

    private Listener onServerStart = new Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshResults();
                    Toast socketToast = Toast.makeText(getApplicationContext(), "Server Started Up", Toast.LENGTH_SHORT);
                    socketToast.show();
                }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                accessToken = response.getAccessToken();

                SpotifyConnection.apiConnection.setAccessToken(response.getAccessToken());

                SpotifyService spotify = SpotifyConnection.apiConnection.getService();

                Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer = player;
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        //mPlayer.play("spotify:track:64EtCN7vOk8c8Pu58VlmX3");

//                        if(trackList.size() > 0) {
//                            mPlayer.play(trackList.get(0));
//                        }

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
        Context context = getApplicationContext();
        CharSequence text = eventType.name();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        mPlayerState = playerState;


        if(eventType.equals(EventType.END_OF_CONTEXT)) {
            if(currentTrackIdx < trackAdapter.getCount()-1) {

                startPlayback(0);
            }
            else {
                Toast endToast = Toast.makeText(context, "End of List Reached", Toast.LENGTH_SHORT);
                endToast.show();
            }
        }
    }

    // todo: this is just for testing, its all screwy - we need to define how the app state changes
    public void play(View view) {
        if(mPlayer.isInitialized()) {
            if(mPlayerState != null && mPlayerState.playing) {
                mPlayer.pause();

            }
            else {
//                Track.getCurrentTracks(new VolleyCallback() {
//                    @Override
//                    public void onSuccess(ArrayList<String> result) {
//                        trackAdapter.clear();
//                        trackAdapter.addAll(result);
//                        // start playing the first track (if there is one)
//                        if(trackAdapter.getCount() > 0) {
//                            mPlayer.play(trackAdapter.getItem(0));
//                        }
//                    }
//                }, this);
                if(mPlayerState != null) {
                    mPlayer.resume();
                }
                else {
                    if(trackAdapter.getCount() > 0) {
                        startPlayback(0);
                    }
                }
            }



        }
    }

    private void refreshResults() {
        Track.getCurrentTracks(new VolleyCallbackArray() {
            @Override
            public void onSuccess(List<TrackView> result) {
                trackAdapter.clear();
                trackAdapter.addAll(result);
                // start playing the first track (if there is one)
                if (trackAdapter.getCount() > 0) {
                    //mPlayer.play(trackAdapter.getItem(0));
                    //startPlayback(0);
                }
            }
        }, this);
    }

    private void startPlayback(int trackNo) {
        Log.d("Starting Playback trk: ", String.valueOf(trackNo));
        mPlayer.play(trackAdapter.getItem(trackNo).getUri());
        currentTrackIdx = trackNo;

        // send a play notification to the server
        Track.playTrack(trackNo, this, new VolleyCallback() {
            @Override
            public void onSuccess(ArrayList<String> result) {
                // todo: handle this
                refreshResults();
            }
        });
    }

    private AdapterView.OnItemClickListener itemClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // start playback of the selected track.
            startPlayback(position);
        }
    };


    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_search:
                this.onSearchRequested();
            case R.id.action_refresh:
                this.refreshResults();
            case R.id.action_settings:
                Intent intent = new Intent(this, AppSettingsActivity.class);
                startActivity(intent);
            case R.id.action_peers:
                Intent peersIntent = new Intent(this, PeersActivity.class);
                startActivity(peersIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // wifi direct related
    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}