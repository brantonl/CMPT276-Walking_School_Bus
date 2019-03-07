package lava.walkinggroup;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import lava.walkinggroup.dataobjects.Group;
import lava.walkinggroup.utility.CurrentSession;

/**
 * Info window for group markers on map.
 *
 * Gives user option to join/leave selected group or open group management activity
 * for detailed group information and editing options.
 */
public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.custom_infowindow, null);

        TextView name_tv = view.findViewById(R.id.InfoWindow_name);
        TextView details_tv = view.findViewById(R.id.InfoWindow_details);

        // Current group from marker
        Group tGroup = null;
        boolean joined = false;
        try {
            tGroup = (Group) marker.getTag();
        }catch(Exception e){
            joined = false;
        }
        name_tv.setText(marker.getTitle());

        Resources res = context.getResources();

        CurrentSession currentSession = CurrentSession.getInstance();
        if(tGroup == null){
            return null;
        }
        if (tGroup.getLeader() != null){
            if (tGroup.getMemberUsers().contains(CurrentSession.getCurrentUser()) || tGroup.getLeader().equals(currentSession.getCurrentUser())){
                details_tv.setText(res.getString(R.string.InfoWindow_already_member));
            }else{
                details_tv.setText(res.getString(R.string.InfoWindow_not_member));
            }
        }else{
            if (tGroup.getMemberUsers().contains(CurrentSession.getCurrentUser())){
                details_tv.setText(res.getString(R.string.InfoWindow_already_member));
            }else{
                details_tv.setText(res.getString(R.string.InfoWindow_not_member));
            }
        }

        return view;
    }
}