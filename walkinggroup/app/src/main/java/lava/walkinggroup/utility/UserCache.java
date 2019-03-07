package lava.walkinggroup.utility;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import retrofit2.Call;

/**
 * Local cache of user objects.
 * Synchronizes with server on startup.
 */
public class UserCache {
    private static final String TAG = "UserCache";

    //keeps two maps for O(log(n)) lookup by either id or email.
    private TreeMap<Long, User> idUserCache;
    private TreeMap<String, User> emailUserCache;
    private static UserCache instance;

    public UserCache() {
        idUserCache = new TreeMap<>((id1, id2) -> Long.signum(id1 - id2));
        emailUserCache = new TreeMap<>();
        refresh();
    }

    public static UserCache getInstance(){
        if(instance == null){
            instance = new UserCache();
        }
        return instance;
    }

    /**
     * Adds user to cache.
     * If user.hasFullData() is false, will pull full user
     * from server before caching.
     *
     * Any existing user in cache with the same id will be overwritten by the new user.
     *
     * @param user User to be added to cache
     */
    public void add(User user){
        if(user == null) {
            Log.e(TAG, "Attempt to add null user to cache");
            return;
        }
        if(!user.hasFullData()){
            if(user.getId() != null) {
                //pull user from server by id
                Call<User> caller = CurrentSession.proxy.getUserById(user.getId());
                ProxyBuilder.callProxy(caller, returnedUser -> add(returnedUser));

                //exits method to avoid halting app
                return;
            }
            else if (user.getEmail() != null && !user.getEmail().isEmpty()){
                //Pull user from server by email
                Call<User> caller = CurrentSession.proxy.getUserByEmail(user.getEmail());
                ProxyBuilder.callProxy(caller, returnedUser -> add(returnedUser));

                //exits method to avoid halting app
                return;
            }
            else {
                Log.e(TAG, "Attempt to add unidentifiable user to cache (no id or email)");
                return;
            }
        }
        idUserCache.put(user.getId(), user);
        emailUserCache.put(user.getEmail(), user);
    }

    public void add(List<User> userList){
        for (User user : userList){
            add(user);
        }
        fillMonitorAndMonitorByLists();
    }

    private void fillMonitorAndMonitorByLists() {
        for (User user:idUserCache.values()){
            List<User> newMonitorsList = new ArrayList<>();
            List<User> newMonitoredByList = new ArrayList<>();
            for (User monitoredUser : user.getMonitorsUsers()){
                if (getUser(monitoredUser.getId()) != null) {
                    newMonitorsList.add(getUser(monitoredUser.getId()));
                }
                else {
                    newMonitorsList.add(monitoredUser);
                }
            }
            for (User monitoringUser : user.getMonitoredByUsers()){
                if (getUser(monitoringUser.getId()) != null) {
                    newMonitoredByList.add(getUser(monitoringUser.getId()));
                }
                else {
                    newMonitoredByList.add(monitoringUser);
                }
            }
            user.setMonitorsUsers(newMonitorsList);
            user.setMonitoredByUsers(newMonitoredByList);
        }
    }

    public User getUser(Long id){
        return idUserCache.get(id);
    }

    public User getUser(String email){
        return emailUserCache.get(email);
    }

    public void refresh(){
        Call<List<User>> caller = CurrentSession.proxy.getUsers();
        ProxyBuilder.callProxy(caller, returnedUserList -> add(returnedUserList));
    }
}
