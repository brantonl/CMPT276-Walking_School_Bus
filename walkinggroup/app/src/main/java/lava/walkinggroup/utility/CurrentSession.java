package lava.walkinggroup.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import lava.walkinggroup.MapsActivity;
import lava.walkinggroup.dataobjects.Group;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/**
 * Convenience singleton to track state of our session
 */
public class CurrentSession {
    private static final String TAG = "CurrentSession";
    public static WGServerProxy proxy = null;

    private static MapsActivity mapsActivity;


    public static String authToken = null;

    public enum ConnectionStatus {READY, WAITING, CONNECTED, FAILED}
    private static ConnectionStatus connection_status = ConnectionStatus.READY;

    private static Context appContext;

    private final static String SHARED_PREFS = "lava.walkinggroup.SHARED_PREFS";
    private final static String AUTH_TOKEN = "lava.walkinggroup.AUTH_TOKEN";
    private final static String USER_EMAIL = "lava.walkinggroup.USER_EMAIL";

    private static int colorTheme = 0;
    private static int mapTheme = 0;


    private static CurrentSession instance = null;

    private static User currentUser;
    private static List<Group> currentGroups;

    private static Handler scheduler;
    private static UserCache userCache;

    public static Context getAppContext(){
        return appContext;
    }


    private CurrentSession(Context context) {
        appContext = context;
        SharedPreferences storage = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        if (storage.contains(AUTH_TOKEN) && storage.contains(USER_EMAIL)) {
            Log.d(TAG, "Restoring token and email from storage");
            authToken = storage.getString(AUTH_TOKEN, null);
            currentUser = new User();
            currentUser.setEmail(storage.getString(USER_EMAIL, null));
        } else {
            Log.d(TAG, "No token and/or email found in storage!");
        }

    }

    private static void initializeCaches() {
        userCache = UserCache.getInstance();
        MessageList.getInstance();

        startCacheScheduler();
    }

    public static void initialize(Context appContext) throws IllegalArgumentException {
        if (instance != null) {
            Log.e(TAG, "Tried to initialize CurrentSession, but it's already been initialized!");
            return;
        }
        if (appContext == null) {
            throw new IllegalArgumentException("Tried to initialize CurrentSesion with null Context");
        }

        Log.d(TAG, "Initializing new CurrentSession");
        instance = new CurrentSession(appContext);
    }


    /**
     * Singleton method to get instance
     */
    public static CurrentSession getInstance() {
            return getInstance(null);
    }

    public static CurrentSession getInstance(Context context) {
        if (instance == null) {
            try {
                initialize(context);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Destroys and rewrites SharedPreferences with CurrentSession vars.
     */
    public static void saveInstance() {
        SharedPreferences storage = appContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();

        editor.clear();

        if ((authToken != null) && (currentUser.getEmail() !=  null)) {
            Log.d(TAG,"Saving token to preferences: " + authToken);
            editor.putString(AUTH_TOKEN, authToken);
            editor.putString(USER_EMAIL, currentUser.getEmail());
        } else {
            Log.d(TAG, "Nothing to save to preferences, token or user email is null!");
        }

        editor.commit();
    }


    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String authToken) {
        Log.d(TAG, "Setting auth token");
        CurrentSession.authToken = authToken;
    }


    public static void setConnectionStatus(ConnectionStatus cs){
        connection_status = cs;
    }

    public static ConnectionStatus getConnectionStatus(){
        return connection_status;
    }


    public static void setMapsActivity(MapsActivity mapsActivity) {
        CurrentSession.mapsActivity = mapsActivity;
    }

    public static MapsActivity getMapsActivity() {
        return mapsActivity;
    }


    public static Context getContext(){
        return appContext;
    }


    public static void setCurrentUser(User user){
            Log.d(TAG, "Updating currentuser locally and on server by ID");
            currentUser = user;

            Call<User> caller = CurrentSession.proxy.editUser(currentUser.getId(), user);
            ProxyBuilder.callProxy(caller, returnedUser -> setCurrentUserCallback(returnedUser));
    }


    public static User getCurrentUser(){
        return currentUser;
    }

    private static void setCurrentUserCallback(User returnedUser) {
        //changed
        Log.i("UserID_Current", returnedUser.getId() + "");
        currentUser = returnedUser;
        initializeCaches();
    }


    // Pass a callback function which takes a list of groups, this will call it after getting latest.
    public static void getGroupsAndCallback(Context context, ProxyBuilder.SimpleCallback<List<Group>> func) {
        // Get group list (doesn't have all data), then fill it in for each group.
        ProxyBuilder.callProxy(context, proxy.getGroups(), groupList -> {
            currentGroups = groupList;
            func.callback(groupList);
        });
    }


//    // Pass a callback function which takes a list of groups, this will call it after getting latest.
//    public static void getGroupsAndCallback(Context context, ProxyBuilder.SimpleCallback<List<Group>> func) {
//        // Get group list (doesn't have all data), then fill it in for each group.
//        ProxyBuilder.callProxy(context, proxy.getGroups(), groupList -> {
//            currentGroups = new ArrayList<>(); //We got new group list, replace old one.
//            for (Group group : groupList) {
//                ProxyBuilder.callProxy(context, proxy.getGroupById(group.getId()), updatedGroup -> {
//                    currentGroups.add(updatedGroup);
//                    getGroupsAndCallbackHelper(currentGroups);
//                });
//            }
//            func.callback(currentGroups);
//        });
//    }


    //Auto update cache every minute
    private static void startCacheScheduler() {
        scheduler = new Handler();
        cacheUpdater.run();
    }


    private static Runnable cacheUpdater = new Runnable() {
        @Override
        public void run() {
            try{
                userCache.refresh();
            } finally {
                scheduler.postDelayed(cacheUpdater, 60000);
            }
        }
    };
}
