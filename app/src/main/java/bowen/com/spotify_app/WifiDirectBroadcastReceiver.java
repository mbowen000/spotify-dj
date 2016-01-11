package bowen.com.spotify_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by mike on 1/10/16.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    public static WifiP2pDeviceList availablePeers;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                mActivity.isWifiDirectSupported = true;
            } else {
                // Wi-Fi P2P is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            this.connectToPeers();
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    public void connectToPeers() {
        // Call WifiP2pManager.requestPeers() to get a list of current peers
        if (mManager != null) {
            mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    availablePeers = peers;

                    for(WifiP2pDevice device : peers.getDeviceList()) {
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;

                        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.e("Connected to Peer: ", "");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.e("Failed to connect: ", "");
                            }
                        });
                    }



                }
            });
        }
    }

}
