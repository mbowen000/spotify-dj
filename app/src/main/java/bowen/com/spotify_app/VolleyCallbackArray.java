package bowen.com.spotify_app;

import java.util.List;

import bowen.com.spotify_app.models.TrackView;

/**
 * Created by mike on 1/3/16.
 */
public interface VolleyCallbackArray {
    void onSuccess(List<TrackView> response);
}
