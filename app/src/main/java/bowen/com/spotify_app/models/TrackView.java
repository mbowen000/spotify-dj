package bowen.com.spotify_app.models;

/**
 * Created by mike on 1/3/16.
 */
public class TrackView {

    public String name;
    public String artist;
    public String uri;

    public String toString() {
        return this.artist + " - " + this.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
