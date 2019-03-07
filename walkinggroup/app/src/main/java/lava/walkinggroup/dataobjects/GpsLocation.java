package lava.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Store information about a GPS location of a user.
 *
 * To be completed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsLocation {
    private Double lat;
    private Double lng;

    private Date timestamp;

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public LatLng toGoogleLatLng() {
        return new LatLng( this.getLat(), this.getLng() );
    }

    @Override
    public String toString() {
        return "GpsLocation{" +
                "lat=" + getLat() +
                ", lng='" + getLng() +
                ", timestamp='" + getTimestamp() +
                '}';
    }

}
