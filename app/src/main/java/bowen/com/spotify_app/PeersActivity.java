package bowen.com.spotify_app;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class PeersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView indicator = (TextView)findViewById(R.id.peersEnabledIndication);
        if(MainActivity.isWifiDirectSupported) {
            indicator.setText("Wifi Direct is Available");
        }
        else {
            indicator.setText("Wifi Direct is NOT available");
        }

        TextView indicatorPeers = (TextView)findViewById(R.id.peersList);
        if(WifiDirectBroadcastReceiver.availablePeers != null) {
            for(WifiP2pDevice device : WifiDirectBroadcastReceiver.availablePeers.getDeviceList()) {
              indicatorPeers.append(device.deviceName + "\n");
            }
            //indicatorPeers.setText("Peers list available");
        }
        else {
            indicatorPeers.setText("Peers list unavailable");
        }


       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
