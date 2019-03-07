package lava.walkinggroup;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.sip.SipSession;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

/**
 * An IntentService subclass for handling asynchronous geofence notifications.
 * Service on a separate handler thread to act on sent intents.
 */
public class MapsGeofenceService extends IntentService {
    private static final String TAG = "GeofenceTransitions";

    public static final String REACHED_DESTINATION = "lava.walkinggroup.MapsGeofenceService.REACHED_DESTINATION";

    public MapsGeofenceService() {
        super("MapsGeofenceService");
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "onHandleIntent has error: " + GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode()));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d(TAG, "onHandleIntent: Broadcasting: REACHED_DESTINATION");

            Intent resultIntent = new Intent();
            resultIntent.setAction(REACHED_DESTINATION);
            this.sendBroadcast(resultIntent);

        } else {
            // Log the error.
            Log.e(TAG, "Invalid geofence transition caught, type: " + geofenceTransition);
        }
    }
}