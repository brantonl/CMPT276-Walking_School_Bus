package lava.walkinggroup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import lava.walkinggroup.dataobjects.GpsLocation;
import lava.walkinggroup.dataobjects.Group;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.dataobjects.rewards.MapTheme;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.MessageList;
import retrofit2.Call;

/**
 * This class is to perform all the on map functions and activities with google map
 * Much of the code in class is thanks to the tutorial series by CodingWithMitch,
 * one video of which is https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt
 *
 * Much of the geofencing code is copied and altered from the official Google Geofencing API docs.
 *
 * the unread message count badge and onCreateOptionsMenu code is copied and altered from
 * https://stackoverflow.com/questions/43194243/notification-badge-on-action-item-android
 */
public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MapsActivity";
    private static final int PERMISSION_REQUEST_CODE = 7138;
    private static final int CREATE_GROUP_RESULT_CODE = 6348;
    private static final long PARENT_DASHBOARD_REFRESH_MS = 15000 ;

    private GoogleMap mMap;
    private TextView mailBadge;

    private static final float DEFAULT_ZOOM = 15f;
    private static final long LOCATION_UPDATE_INTERVAL_MS = 30000; //30s
    private static final long LOCATION_UPDATE_FASTEST_INTERVAL_MS = 30000; //30s

    private static final int GEOFENCE_RADIUS = 100; //meters
    private static final long GEOFENCE_EXPIRATION_DURATION = Long.MAX_VALUE;

    //private int GEOFENCE_POST_ARRIVAL_TRANSMISSION_DURATION = 600000; //10 minutes
    private int GEOFENCE_POST_ARRIVAL_TRANSMISSION_DURATION = 60000; //10 minutes

    private int REWARD_FOR_DESTINATION_ARRIVAL = 100;

    private Location currentLocation = null;
    private FusedLocationProviderClient locationClient;
    LocationRequest locationRequest;
    HandlerThread locationUpdaterHandlerThread;

    private PendingIntent geofencePendingIntent = null;
    private GeofencingClient geofencingClient;
    private GeofenceTransitionReceiver geofenceTransitionReceiver;

    private boolean gotLocationPermission = false;
    private boolean isTransmittingCurrentLocation = false;
    private boolean isParentDashBoard = false;

    private List<Group> globalGroupList = new ArrayList<>();

    private Handler scheduler;
    private Runnable messageUpdater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_drawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CurrentSession.setMapsActivity(this);

        setupFloatingButtons();
        setupDrawer(toolbar);

        setupLocationSendToggleButton();

        setupMaps(); // Prompts for permissions, initializes map if permissions present.

        setupPointsView();

        setupMessageUpdater();

        setupGeofencing();
    }

    private void setupMessageUpdater() {
        MessageList messageList = MessageList.getInstance();

        scheduler = new Handler();

        messageUpdater = new Runnable() {
            @Override
            public void run() {
                try{
                    MessageList.getInstance().update();
                } finally {
                    scheduler.postDelayed(messageUpdater, 60000);
                }
            }
        };
        messageUpdater.run();
        setupGeofencing();
        setupColors();
    }

    private void setupColors() {
        CurrentSession session = CurrentSession.getInstance();
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        Toolbar toolbar = MapsActivity.this.findViewById(R.id.toolbar);
        FloatingActionButton fablocal = MapsActivity.this.findViewById(R.id.maps_fab_location_start_stop);
        FloatingActionButton fabalert = MapsActivity.this.findViewById(R.id.map_floatButton_panic);
        LinearLayout navHead = MapsActivity.this.findViewById(R.id.nav_header_layout);

        if (navHead!=null){
            navHead.setBackgroundColor(getResources().getColor(theme[0]));
            TextView nameView = findViewById(R.id.nav_header_title);
            TextView levelTitleView = findViewById(R.id.nav_header_subtitle);
            nameView.setText(CurrentSession.getCurrentUser().getName());
            levelTitleView.setText(CurrentSession.getCurrentUser().getRewards().getTitle());
            ImageView profilePic = findViewById(R.id.nav_header_pic);
            profilePic.setImageDrawable(CurrentSession.getCurrentUser().getRewards().getSelectedProfilePic().getProfilePic());
        }

        toolbar.setBackgroundColor(getResources().getColor(theme[1]));
        toolbar.setSubtitleTextColor(getResources().getColor(theme[3]));
        toolbar.setTitleTextColor(getResources().getColor(theme[3]));
        fablocal.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(theme[0])));
        fabalert.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(theme[2])));

    }



    private void setupDrawer(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                setupColors();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                setupColors();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                setupColors();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                setupColors();
            }
        });
        }


    private void setupGeofencing() {
        geofencingClient = LocationServices.getGeofencingClient(this);
        registerGeofencingReceiver();
        CurrentSession.getGroupsAndCallback(this, groupList -> createGeofencesFromGroups(groupList));
    }

    private void registerGeofencingReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MapsGeofenceService.REACHED_DESTINATION);

        geofenceTransitionReceiver = new GeofenceTransitionReceiver();
        registerReceiver(geofenceTransitionReceiver, intentFilter);
    }

    private void createGeofencesFromGroups(List<Group> groupList) {
        if (groupList.size() == 0) {
            Log.d(TAG, "Trying to create geofences from an empty list");
            return;
        }

        List<Geofence> geofenceList = new ArrayList();

        for (Group group : groupList) {
            if ( !group.hasFullData() ) {
                Log.e(TAG, "Trying to use group which doesn't have full data");
                return;
            } else if ( group.getRouteLatArray().size() < 2 || group.getRouteLatArray().size() < 2) {
                Log.e(TAG, "Groups must have a start and end point");
                return;
            }

            double end_lat = group.getRouteLatArray().get(group.getRouteLatArray().size() - 1);
            double end_lng = group.getRouteLngArray().get(group.getRouteLngArray().size() - 1);

            geofenceList.add(new Geofence.Builder()
                    .setRequestId(group.getId().toString())
                    .setCircularRegion(
                            end_lat,
                            end_lng,
                            GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(GEOFENCE_EXPIRATION_DURATION)
                    .build()
            );
        }

        // Should already have permissions from setting up map, but let's double check.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getGeofencePendingIntent())
                .addOnSuccessListener(this, nothing -> {
                    Log.d(TAG, "Successfully registered Geofence listeners");
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Failed to register Geofence listener: " + e.getMessage());
                });
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        return new GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, MapsGeofenceService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    /*
     * Handles user arriving at destinations.
     */
    private class GeofenceTransitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Got a broadcasted intent");
            if (intent.getAction().equals(MapsGeofenceService.REACHED_DESTINATION)) {
                Log.i(TAG, "Reached end of group walk, setting timer to terminate.");

                scorePoints(CurrentSession.getCurrentUser(), REWARD_FOR_DESTINATION_ARRIVAL);
                stopLocationBroadcastAfterDelay(GEOFENCE_POST_ARRIVAL_TRANSMISSION_DURATION);
            }
        }
    }

    public void setupPointsView() {
        ImageView i = (ImageView) findViewById(R.id.maps_reward_display_icon);
        i.setOnClickListener(v -> {
            scorePoints(CurrentSession.getCurrentUser(), REWARD_FOR_DESTINATION_ARRIVAL);
        });

        displayCurrentPoints(CurrentSession.getCurrentUser());
    }

    public void scorePoints(User user, int points) {
        user.setTotalPointsEarned(user.getTotalPointsEarned() + points);
        user.setCurrentPoints(user.getCurrentPoints() + points);

        CurrentSession.setCurrentUser(user);

        notifyUserViaLogAndToast(getString(R.string.maps_score_points_notification));
        displayCurrentPoints(user);
    }

    public void displayCurrentPoints(User user) {
        TextView t = (TextView) findViewById(R.id.maps_reward_display_amount);
        t.setText(user.getCurrentPoints().toString());
    }


    public void stopLocationBroadcastAfterDelay(int delayInMs) {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Timer is up, ending location broadcast");
                isTransmittingCurrentLocation = false;

                LocationSendToggleButtonOff(findViewById(R.id.maps_fab_location_start_stop));
            }
        }, delayInMs);
    }


    private void setupLocationSendToggleButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.maps_fab_location_start_stop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTransmittingCurrentLocation == false) {
                    LocationSendToggleButtonOn(view);
                }
                else {
                    LocationSendToggleButtonOff(view);
                }
            }
        });
    }

    private void LocationSendToggleButtonOn(View view) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.maps_fab_location_start_stop);
        Snackbar.make(view, getString(R.string.MapsActivity_transmit_location), Snackbar.LENGTH_LONG).show();
        isTransmittingCurrentLocation = true;
        fab.setImageResource(R.drawable.ic_my_location_black_24dp);
    }
    private void LocationSendToggleButtonOff(View view) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.maps_fab_location_start_stop);
        Snackbar.make(view, getString(R.string.MapsActivity_stop_transmit_location), Snackbar.LENGTH_LONG).show();
        isTransmittingCurrentLocation = false;
        fab.setImageResource(R.drawable.ic_location_disabled_black_24dp);
    }


    private void setupMaps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gotLocationPermission = true;
            setUpLocationServices();
            initializeMap();
        } else {
            //Permission is not granted for location services, ask for it.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * When we request location permissions, we handle the allowance or refusal here.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d(TAG, "Fine location permissions granted!");
                    gotLocationPermission = true;
                    setUpLocationServices();
                    initializeMap();
                } else {
                    Log.d(TAG, "Fine location permissions not granted!");
                    // Todo: Handle permissions not being granted more gracefully.
                    Toast.makeText(this, R.string.maps_permissions_not_granted_msg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Sets up a thread to run in the background and call a callback whenever location is updated.
     */
    private void setUpLocationServices() {
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL_MS)
                .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL_MS);

        locationUpdaterHandlerThread = new HandlerThread("Server Location Updater");
        locationUpdaterHandlerThread.start();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                locationUpdaterHandlerThread.getLooper());
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            updateLocalLocation(locationResult);
            if (isTransmittingCurrentLocation) {
                updateServerLocation(locationResult);
            }
        }
    };

    private void updateServerLocation(LocationResult result) {
        Log.d(TAG, "Sending current location to server");

        GpsLocation loc = new GpsLocation();
        loc.setLat(result.getLastLocation().getLatitude());
        loc.setLng(result.getLastLocation().getLongitude());
        loc.setTimestamp(new Date());

        Call<GpsLocation> call = CurrentSession.proxy.setLastGpsLocation(CurrentSession.getCurrentUser().getId(), loc);

        ProxyBuilder.callProxy(this, call, nothing->{}); //send location to server
    }

    @SuppressLint("DefaultLocale")
    private void updateLocalLocation(LocationResult result) {
        currentLocation = result.getLastLocation();
    }


    /**
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Move map to current location and display blue dot
        if (gotLocationPermission) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            moveCameraAfterDelay(4000);
        } else {
            notifyUserViaLogAndToast(getString(R.string.MapsActivity_no_location_permission));
        }

        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                CurrentSession.getCurrentUser().getRewards().getSelectedMapTheme().getMapId()));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        generateGroupMarkers();
    }

    public void changeMapTheme (MapTheme mapTheme) {
        boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, mapTheme.getMapId()));

        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
    }


    private void showStudentDashBoard() {
        mMap.clear();
        generateGroupMarkers();
    }

    private void showParentDashboard() {
        mMap.clear();
        generateGroupMarkers();

        Log.d(TAG, "Showing parent dashboard");
        Call<List<User>> call = CurrentSession.proxy.getMonitorsUsers(CurrentSession.getCurrentUser().getId());
        ProxyBuilder.callProxy(this, call, (List<User> userList) -> drawUserLastSeenMarkers(userList));

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showParentDashboard();
            }
        }, PARENT_DASHBOARD_REFRESH_MS);
    }

    private void drawUserLastSeenMarkers(List<User> userList) {
        Log.i(TAG,"isParentDashBoard: " + isParentDashBoard);
        for (User u : userList) {
            if (u.getLastGpsLocation() != null
                    && u.getLastGpsLocation().getTimestamp() != null)
            {
                mMap.addMarker(new MarkerOptions()
                        .position(u.getLastGpsLocation().toGoogleLatLng())
                        .icon(createLastSeenUserMarkerIcon(u))
                );
            }
        }
    }

    // Creates the marker with text describing user's last seen location
    private BitmapDescriptor createLastSeenUserMarkerIcon(User u) {
        IconGenerator ic = new IconGenerator(this);
        ic.setColor(Color.LTGRAY);

        Date now = new Date();

        // String describing time span, e.g. "15 minutes ago"
        String last_seen = DateUtils.getRelativeTimeSpanString(
                u.getLastGpsLocation().getTimestamp().getTime(), //To
                now.getTime(), //From
                DateUtils.MINUTE_IN_MILLIS) //Round to minute
                .toString();

        BitmapDescriptor bmp = BitmapDescriptorFactory.fromBitmap(
                ic.makeIcon(getString(R.string.MapsActivity_user_marker_text, u.getName(), last_seen))
        );

        return bmp;
    }


    private void moveCameraAfterDelay(int delayInMs) {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveCameraToCurrentLocation();
            }
        }, delayInMs);
    }

    private void moveCameraToCurrentLocation() {
        if (currentLocation != null) {
            Log.d(TAG, "Moving camera to current location");
            LatLng latlng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Could not move camera, location is null");
        }
    }

    private void initializeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    /**
     * Return to Main Activity when back button pressed
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_mail);

        View actionView = MenuItemCompat.getActionView(menuItem);
        mailBadge = (TextView) actionView.findViewById(R.id.mail_badge);

        setupBadge(mailBadge,0);


        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });

        return true;
    }


    /**
     * Handles menu bar clicks
     */
    Boolean style = true;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(item.getItemId()){
            case R.id.action_mail: {
                Intent intent = InboxActivity.getIntent(MapsActivity.this);
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle clicks in slide-out sidebar
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        boolean ret = false;

        switch (id) {
            case R.id.nav_monitoring:
                startActivity(MonitorOrByActivity.getIntent(this, CurrentSession.getCurrentUser(), true));
                ret = true;
                break;
            case R.id.nav_monitored_by:
                startActivity(MonitorOrByActivity.getIntent(this, CurrentSession.getCurrentUser(), false));
                ret = true;
                break;
            case R.id.nav_logout:
                CurrentSession.authToken = null;
                startActivity(LoginActivity.getIntent(this));
                ret = true;
                break;
            case R.id.nav_createGroup:
                startActivityForResult(CreateGroupActivity.getIntent(this), CREATE_GROUP_RESULT_CODE);
                ret = true;
                break;
            //TODO:: Remove userInformation from drawer.
            //temp button for testing access
            case R.id.nav_userInformation:
                startActivity(UserInformationActivity.getIntent(this));
                ret = true;
                break;
            case R.id.nav_parent_dashboard_toggle:
                if(!isParentDashBoard){
                    isParentDashBoard = true;
                    showParentDashboard();
                    item.setTitle(R.string.show_parent_dashboard_student);
                    drawer.closeDrawer(GravityCompat.START);
                }
                else{
                    isParentDashBoard = false;
                    showStudentDashBoard();
                    item.setTitle(R.string.show_parent_dashboard_parent);
                    drawer.closeDrawer(GravityCompat.START);
                }
                ret = true;
                break;
            case R.id.nav_customizeProfile:
                startActivity(CustomizeProfileActivity.getIntent(this));
                ret = true;
                break;
            case R.id.nav_leaderBoard:
                startActivity(LeaderboardActivity.getIntent(this));
                ret = true;
                break;
            case R.id.nav_permission_dashboard:
                startActivity(PermissionActivity.getIntent(this));
                ret = true;
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return ret;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CREATE_GROUP_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    generateGroupMarkers();
                }
        }

    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        return intent;
    }

    /**
     * Put message up in toast and logcat
     * @param message message to be displayed
     */
    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Creation of markers for each group
     */
    private void generateGroupMarkers(){
        Call<List<Group>> caller = CurrentSession.proxy.getGroups();
        ProxyBuilder.callProxy(caller, returnedGroup -> drawMarkersFromGroupsCallback(returnedGroup, mMap));

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
        mMap.setInfoWindowAdapter(customInfoWindow);

        mMap.setOnInfoWindowClickListener(this);
    }

    private void drawMarkersFromGroupsCallback(List<Group> retGroups, GoogleMap mMap){
        globalGroupList = retGroups;
        List<Marker> newMarkers = new ArrayList<>();

        if (retGroups.size() == 0){
            notifyUserViaLogAndToast(getString(R.string.MapsActivity_no_groups));
            return;
        }
        for (int i = 0; i < retGroups.size(); i++){
            if (retGroups.get(i).getRouteLatArray().size() == 0 || retGroups.get(i).getRouteLngArray().size() == 0){
                Log.w(TAG,"Group " + retGroups.get(i).getGroupDescription()+" has corrupted LATLANG");
                continue;
            }

            //don't show the groups without leader and members
            if(retGroups.get(i).getLeader() == null && retGroups.get(i).getMemberUsers().size() == 0){
                Log.w(TAG,"Group " + retGroups.get(i).getGroupDescription()+" has no leader and member" );
                continue;
            }

            Group currentGroup = retGroups.get(i);

            // Draw departure marker
            LatLng departure_loc = new LatLng(currentGroup.getRouteLatArray().get(0), currentGroup.getRouteLngArray().get(0));
            Log.w(TAG,retGroups.get(i).getGroupDescription() + " " + String.valueOf(departure_loc));

            BitmapDescriptor departBmp = createGroupMarkerIcon(currentGroup, true);
            MarkerOptions departMarker = new MarkerOptions()
                    .position(departure_loc)
                    .title(retGroups.get(i).getGroupDescription())
                    .icon(departBmp);


            // Draw destination marker
            LatLng arrival_loc = new LatLng(currentGroup.getRouteLatArray().get(currentGroup.getRouteLatArray().size() - 1),
                                     currentGroup.getRouteLngArray().get(currentGroup.getRouteLngArray().size() - 1) );

            Log.w(TAG,retGroups.get(i).getGroupDescription() + " " + String.valueOf(arrival_loc));

            BitmapDescriptor arriveBmp = createGroupMarkerIcon(currentGroup, false);
            MarkerOptions arrivalMarker = new MarkerOptions()
                    .position(arrival_loc)
                    .title(retGroups.get(i).getGroupDescription())
                    .icon(arriveBmp);


            newMarkers.add(mMap.addMarker(departMarker));
            newMarkers.get(newMarkers.size()-1).setTag(retGroups.get(i));

            newMarkers.add(mMap.addMarker(arrivalMarker));
            newMarkers.get(newMarkers.size()-1).setTag(retGroups.get(i));
        }
    }

    // Creates the group markers, customized for departure/arrival points.
    private BitmapDescriptor createGroupMarkerIcon(Group g, Boolean isDepartureIcon) {
        IconGenerator ic = new IconGenerator(this);

        String arrive_or_depart = "";
        if (isDepartureIcon) {
            arrive_or_depart = "Depart";
        } else {
            arrive_or_depart = "Arrive";
        }

        int[] colors = {Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN};
        Random rand = new Random();
        rand.setSeed(g.getId());
        int color = colors[rand.nextInt(6)];
        ic.setColor(color);

        ic.setTextAppearance(R.style.FontForGroupMarkers);

        return BitmapDescriptorFactory.fromBitmap(
                ic.makeIcon(getString(R.string.MapsActivity_group_marker_text, arrive_or_depart))
        );
    }



    @Override
    public void onInfoWindowClick(final Marker marker){
        // Retrieve object data

        //try catch to check if marker has proper tag, if it doesn't just do default (jump to marker)
        //Needed this because activity kept creating a marker at where I am currently
        //Also we should probably have it jump to the user when permission is granted.
        Group tGroup;
        try{tGroup = (Group) marker.getTag();}
        catch(Exception e){
            return;
        }

        // Create basic Dialog Box to get a response

        //First Initialize the 3 possible buttons, positive negative and neutral responses.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(getString(R.string.confirm), (dialog, id) -> positiveButtonCallback(dialog,id,marker));
        builder.setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
            //Nothing Happens
        });
        builder.setNeutralButton(getString(R.string.details), (dialog, id) -> {
            Intent intent = GroupManagementActivity.getIntent(this, ((Group)marker.getTag()).getId());
            startActivity(intent);
        });

        boolean leader = false;
        if (tGroup.getLeader() != null){
            leader = tGroup.getLeader().equals(CurrentSession.getCurrentUser());
        } else{
            leader = false;
        }

        if (tGroup.getMemberUsers().contains(CurrentSession.getCurrentUser())){
            //do something when person has already joined the group
            builder.setMessage(getString(R.string.MapsActivity_member_leave_group,marker.getTitle()))
                    .setTitle(marker.getTitle());

        }else if (leader){
            //do something when person has already joined the group
            builder.setMessage(getString(R.string.MapsActivity_leader_leave_group, marker.getTitle()))
                    .setTitle(marker.getTitle());
        }else
            {
                //do something if person hasn't joined group
                builder.setMessage(getString(R.string.MapsActivity_join_group, marker.getTitle()))
                        .setTitle(marker.getTitle());
        }
        AlertDialog dialog = builder.create();

        dialog.show();
    }


    private void updateGroupCallback(List<User> retListUsers, int i, Marker marker){
        Call<List<Group>> caller = CurrentSession.proxy.getGroups();
        ProxyBuilder.callProxy(caller, returnedGroup -> groupUpdateCallBack(returnedGroup,marker,i));
        marker.hideInfoWindow();
        marker.showInfoWindow();
    }


    private void positiveButtonCallback(DialogInterface dialog, int id, Marker marker){
        Group group = (Group) marker.getTag();
        for (int i = 0; i < globalGroupList.size(); i++) {

            if (globalGroupList.get(i).equals(group)) {
                List<User> userList = globalGroupList.get(i).getMemberUsers();
                if (CurrentSession.getCurrentUser().equals(group.getLeader())) {
                    notifyUserViaLogAndToast(getString(R.string.CannotRemoveLeaderError));
                }
                else if (!userList.contains(CurrentSession.getCurrentUser())) {
                    notifyUserViaLogAndToast(getString(R.string.Permission_AddSent));
                    Call<List<User>> caller = CurrentSession.proxy.addGroupMember(globalGroupList.get(i).getId(),CurrentSession.getCurrentUser());
                    final int b = i;
                    ProxyBuilder.callProxy(caller, returnListUsers -> updateGroupCallback(returnListUsers, b, marker));
                }
                else {
                    notifyUserViaLogAndToast(getString(R.string.Permission_RemoveSent));
                    Call<Void> caller = CurrentSession.proxy.removeGroupMember(globalGroupList.get(i).getId(),CurrentSession.getCurrentUser().getId());
                    final int b = i;
                    ProxyBuilder.callProxy(caller,something -> userRemoveCallback(b, userList,marker));
                }
            }
        }

        marker.hideInfoWindow();
        marker.showInfoWindow();
    }


    private void userRemoveCallback(int i, List<User> userUpdate, Marker marker) {
        Call<List<Group>> caller = CurrentSession.proxy.getGroups();
        globalGroupList.get(i).setMemberUsers(userUpdate);
        marker.setTag(globalGroupList.get(i));
        ProxyBuilder.callProxy(caller, returnedGroup -> groupUpdateCallBack(returnedGroup, marker, i));
        marker.hideInfoWindow();
        marker.showInfoWindow();
    }


    private void groupUpdateCallBack(List<Group> returnedGroup, Marker marker, int i) {
        globalGroupList = returnedGroup;
        marker.setTag(globalGroupList.get(i));
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop - removing location update callbacks");
        locationClient.removeLocationUpdates(locationCallback);
        CurrentSession.saveInstance();
        unregisterReceiver(geofenceTransitionReceiver);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "OnRestart - resuming location services");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, locationUpdaterHandlerThread.getLooper());
        registerGeofencingReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap!=null){
            generateGroupMarkers();
        }
        setupColors();
    }

    private void setupFloatingButtons() {
        FloatingActionButton panicButton = (FloatingActionButton) findViewById(R.id.map_floatButton_panic);

        panicButton.setOnClickListener(view -> {
            Intent intent = ComposeActivity.getIntent(MapsActivity.this, ComposeActivity.MessageType.PANIC_MESSAGE);
            startActivity(intent);
        });
//        FloatingActionButton inboxButton = (FloatingActionButton) findViewById(R.id.map_floatButton_inbox);
//        inboxButton.setOnClickListener(view -> {
//            Intent intent = InboxActivity.getIntent(MapsActivity.this);
//            startActivity(intent);
//        });
    }

    private void setupBadge(TextView numText, int unreadNum) {

        Log.i(TAG,"Currently " + unreadNum + "messages.");
        if (numText != null) {
            if (unreadNum == 0) {
                if (numText.getVisibility() != View.GONE) {
                    numText.setVisibility(View.GONE);
                }
            } else {
                numText.setText(String.valueOf(Math.min(unreadNum, 99)));
                if (numText.getVisibility() != View.VISIBLE) {
                    numText.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void refreshUnreadMessagesCount() {
        int unreadMessageCount = MessageList.getInstance().getUnreadMessageCount();
        setupBadge(mailBadge,unreadMessageCount);
    }
}

