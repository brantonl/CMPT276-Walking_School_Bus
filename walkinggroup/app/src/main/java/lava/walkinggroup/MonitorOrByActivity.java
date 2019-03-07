package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.InputValidation;
import lava.walkinggroup.utility.UserCache;
import retrofit2.Call;

import static lava.walkinggroup.utility.CurrentSession.proxy;

/**
 *  Activity to handle user's monitor list and monitorBy list
 *  it also handles the adding and removing function of the lists
 */

public class MonitorOrByActivity extends AppCompatActivity {
    private static final String TAG = "MonitorOrByActivity";
    public static final String MonitorOrByUserIdPackageString = "USEROFMONITORORBYLIST";
    public static final String MonitorOrByListTypePackageString = "MONITORLISTTYPE";

    private boolean isMonitor = false;
    private List<User> cachedList;
    private String[] stringCachedList;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_or_by);
        getIntentExtra();
        getLayoutElements();
        getList();
        setTitle();
        registerOnClickListener();
        setupAddMonitorBtn();
        setupDeleteMonitorBtn();
    }

    private void getLayoutElements() {
        CurrentSession session = CurrentSession.getInstance();
        //grab currently selected themes from Current User
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        //create statelists for the Button (ripple and normal state
        int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors = new int[] { getResources().getColor(theme[0])};
        int[][] states2 = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors2 = new int[] { getResources().getColor(theme[1])};

        ColorStateList rippleList = new ColorStateList(states, colors);
        ColorStateList buttonList = new ColorStateList(states2, colors2);
        //statelists^

        TextView title = findViewById(R.id.UserManagement_title);
        ListView MonitorByList = findViewById(R.id.UserManagement_listView_List);
        Button deleteBtn = findViewById(R.id.UserManagement_deleteBtn);
        Button addBtn = findViewById(R.id.UserManagement_addBtn);
        View root = title.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));

        title.setTextColor(getResources().getColor(theme[3]));
        MonitorByList.setBackgroundColor(getResources().getColor(theme[3]));

        RippleDrawable back = (RippleDrawable) deleteBtn.getBackground();
        back.setColor(rippleList);
        deleteBtn.setTextColor(getResources().getColor(theme[3]));
        deleteBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) addBtn.getBackground();
        back.setColor(rippleList);
        addBtn.setTextColor(getResources().getColor(theme[3]));
        addBtn.setBackgroundTintList(buttonList);

    }

    public void getIntentExtra() {
        Intent intent = getIntent();
        user = UserCache.getInstance().getUser(intent.getLongExtra(MonitorOrByUserIdPackageString, 0));
        isMonitor = intent.getBooleanExtra(MonitorOrByListTypePackageString, false);
    }

    private void getList() {
        if (isMonitor) {
            Call<List<User>> monitorCaller = proxy.getMonitorsUsers(CurrentSession.getCurrentUser().getId());
            ProxyBuilder.callProxy(this, monitorCaller, returnedUsers -> updateList(returnedUsers,false));
        }
        else {
            Call<List<User>> monitorByCaller = proxy.getMonitoredByUsers(CurrentSession.getCurrentUser().getId());
            ProxyBuilder.callProxy(this, monitorByCaller, returnedUsers -> updateList(returnedUsers,false));
        }
    }

    private void updateList(List<User> returnedUsers, boolean isAdding) {
        if(isMonitor){
            user.setMonitorsUsers(returnedUsers);
        }
        else{
            user.setMonitoredByUsers(returnedUsers);
        }
        populateList();
        if(isAdding){
            notifyUserViaLogAndToast(getString(R.string.Permission_AddSent));
        }
        else {
            checkAuthority();
        }
    }

    private void setTitle() {
        TextView title = findViewById(R.id.UserManagement_title);
        if(isMonitor){
            title.setText(getString(R.string.UserManagement_monitor));
        }
        else{
            title.setText(getString(R.string.UserManagement_monitor_by, user.getName()));
        }
    }

    private void populateList() {
        if(isMonitor){
            cachedList = user.getMonitorsUsers();
        }
        else{
            cachedList = user.getMonitoredByUsers();
        }

        // extract the info to show from user cache
        if(checkEmptyList()){
            stringCachedList = new String[]{};
        }
        else{
            stringCachedList = new String[cachedList.size()];
            for(int i =0; i < cachedList.size(); i++){
                stringCachedList[i] = getString(R.string.UserManagement_stringformat, UserCache.getInstance().getUser(cachedList.get(i).getId()).getName(), UserCache.getInstance().getUser(cachedList.get(i).getId()).getEmail());
            }
        }

        ListView MonitorByList = findViewById(R.id.UserManagement_listView_List);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.listview_layout, stringCachedList);
        MonitorByList.setAdapter(adapter);
    }

    private void checkAuthority() {
        if(!user.getMonitoredByUsers().contains(CurrentSession.getCurrentUser()) && !user.equals(CurrentSession.getCurrentUser())){
            Button addBtn = findViewById(R.id.UserManagement_addBtn);
            addBtn.setVisibility(View.GONE);
            Button deleteBtn = findViewById(R.id.UserManagement_deleteBtn);
            deleteBtn.setVisibility(View.GONE);
        }
    }

    private void registerOnClickListener() {
        ListView list = findViewById(R.id.UserManagement_listView_List);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent InfoIntent = UserInformationActivity.getIntent(MonitorOrByActivity.this,cachedList.get(position));
            startActivity(InfoIntent);
        });
    }

    private void setupAddMonitorBtn() {
        Button addBtn = findViewById(R.id.UserManagement_addBtn);
        addBtn.setOnClickListener(v -> {
            AlertDialog.Builder addUser = new AlertDialog.Builder(MonitorOrByActivity.this);
            if(isMonitor){
                addUser.setTitle(getString(R.string.AddMonitor_add_user_to_monitor_list));
            }
            else{
                addUser.setTitle(getString(R.string.AddMonitor_add_user_to_monitor_by_list));
            }

            addUser.setCancelable(false);
            final EditText targetEmail = new EditText(MonitorOrByActivity.this);
            targetEmail.setInputType(InputType.TYPE_CLASS_TEXT);
            targetEmail.setHint(getString(R.string.GroupManagement_EditHint));
            addUser.setView(targetEmail);

            addUser.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                String stringTargetEmail = targetEmail.getText().toString();
                if(!InputValidation.isValidEmail(stringTargetEmail)){
                    notifyUserViaLogAndToast(getString(R.string.GroupManage_invalid_email_try_again));
                }
                else{
                    Call<User> emailAuthenticateCaller = proxy.getUserByEmail(stringTargetEmail);
                    ProxyBuilder.callProxy(MonitorOrByActivity.this, emailAuthenticateCaller, returnedUser -> addUserToList(returnedUser), returnedNothing -> emailError());
                }
            });

            addUser.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

            AlertDialog addDialog = addUser.create();
            addDialog.show();
        });
    }

    private void emailError() {
        notifyUserViaLogAndToast(getString(R.string.AddMonitor_Invalid_user_email));
    }

    private void addUserToList(User returnedUser) {
        if(!checkUserIsInTargetList(returnedUser)){
            if(isMonitor){
                Call<List<User>> ListUpdatecaller = proxy.addToMonitorsUsers(user.getId(), returnedUser);
                ProxyBuilder.callProxy(MonitorOrByActivity.this, ListUpdatecaller, returnedUsers -> updateList(returnedUsers,true));
            }
            else{
                Call<List<User>> ListUpdatecaller = proxy.addToMonitoredByUsers(user.getId(), returnedUser);
                ProxyBuilder.callProxy(MonitorOrByActivity.this, ListUpdatecaller, returnedUsers -> updateList(returnedUsers,true));
            }
        }
        else{
            notifyUserViaLogAndToast(getString(R.string.AddMonitor_UserAlreadyInList));
        }
    }

    private boolean checkUserIsInTargetList(User target){
        List<User> checkList;
        if(isMonitor){
            Log.i(TAG,"checklist is monitor");
            checkList = user.getMonitorsUsers();
        }
        else{
            Log.i(TAG,"checklist is monitorBy");
            checkList = user.getMonitoredByUsers();
        }

        for(int i =0; i< checkList.size(); i++){
            if(checkList.get(i).equals(target)){
                Log.i(TAG,checkList.get(i).getName());
                return true;
            }
        }
        return false;
    }

    private void setupDeleteMonitorBtn() {
        Button deleteBtn = findViewById(R.id.UserManagement_deleteBtn);
        deleteBtn.setOnClickListener(v -> {
            if(checkEmptyList()){
                notifyUserViaLogAndToast(getResources().getString(R.string.UserManagement_EmptyList));
            }
            else{
                final int[] checkedItem = {0};
                String[] choiceList = stringCachedList;

                AlertDialog.Builder deleteUser = new AlertDialog.Builder(MonitorOrByActivity.this);
                deleteUser.setCancelable(false);
                deleteUser.setTitle(getResources().getString(R.string.delete));

                deleteUser.setSingleChoiceItems(choiceList, checkedItem[0], (dialog, which) -> checkedItem[0] = which);

                deleteUser.setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                   if(isMonitor){
                       Call<Void> caller = proxy.removeFromMonitorsUsers(user.getId(), cachedList.get(checkedItem[0]).getId());
                       ProxyBuilder.callProxy(MonitorOrByActivity.this, caller, returnedNothing -> response());
                   }
                   else{
                       Call<Void> caller = proxy.removeFromMonitoredByUsers(user.getId(), cachedList.get(checkedItem[0]).getId());
                       ProxyBuilder.callProxy(MonitorOrByActivity.this, caller, returnedNothing -> response());
                   }
                   populateList();
                });

                deleteUser.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

                AlertDialog deleteDialog = deleteUser.create();
                deleteDialog.show();
            }
        });
    }

    private boolean checkEmptyList(){
        if(cachedList.size() == 0){
            return true;
        }
        return false;
    }

    private void response() {
        notifyUserViaLogAndToast(getString(R.string.Permission_RemoveSent));
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static Intent getIntent(Context context, User user, boolean isMonitorList){
        Intent intent = new Intent(context, MonitorOrByActivity.class);
        intent.putExtra(MonitorOrByUserIdPackageString, user.getId());
        intent.putExtra(MonitorOrByListTypePackageString,isMonitorList);
        return intent;
    }
}
