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

import java.util.ArrayList;
import java.util.List;

import lava.walkinggroup.dataobjects.Group;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.InputValidation;
import lava.walkinggroup.utility.UserCache;
import retrofit2.Call;

import static lava.walkinggroup.utility.CurrentSession.proxy;

/**
 * Activity to view details and manage a group
 *
 * Displays group leader as well as all members in a list.
 * Allows user to add and remove other users from group according
 * to their permissions.
 */
public class GroupManagementActivity extends AppCompatActivity {
    private static final String TAG = "GroupManagementActivity";

    private Group currentGroup;
    private List<User> localMemberList = new ArrayList<>();

    private String[] stringMemberInfo;
    private boolean hasLeader = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);
        getLayoutElement();
        getGroupInfo();
        SetUpMessageBtn();
        SetUpAddBtn();
        SetUpDeleteBtn();
        RegisterOnClickCallBack();
    }

    CurrentSession session = CurrentSession.getInstance();
    //grab currently selected themes from Current User
    int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();
    private void getLayoutElement() {
        TextView title = findViewById(R.id.GroupManagement_Title);
        TextView leader = findViewById(R.id.GroupManagement_Leader);
        TextView memtitle = findViewById(R.id.GroupManagement_memberTitle);
        ListView list = findViewById(R.id.GroupManagement_MemberList);
        Button msgBtn = findViewById(R.id.GroupManagement_Message_Btn);
        Button addBtn = findViewById(R.id.GroupManagement_AddMember_Btn);
        Button removeBtn = findViewById(R.id.GroupManagement_DeleteBtn);

        //grab Root to change main background colour
        View root = title.getRootView();


        //create statelists for the Button (ripple and normal state
        int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors = new int[] { getResources().getColor(theme[0])};
        int[][] states2 = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors2 = new int[] { getResources().getColor(theme[1])};

        ColorStateList rippleList = new ColorStateList(states, colors);
        ColorStateList buttonList = new ColorStateList(states2, colors2);
        //statelists^

        leader.setTextColor(getResources().getColor(theme[3]));
        title.setTextColor(getResources().getColor(theme[2]));
        memtitle.setTextColor(getResources().getColor(theme[2]));
        list.setBackgroundColor(getResources().getColor(theme[3]));
        root.setBackgroundColor(getResources().getColor(theme[0]));

        RippleDrawable back = (RippleDrawable) msgBtn.getBackground();
        back.setColor(rippleList);
        msgBtn.setTextColor(getResources().getColor(theme[3]));
        msgBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) addBtn.getBackground();
        back.setColor(rippleList);
        addBtn.setTextColor(getResources().getColor(theme[3]));
        addBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) removeBtn.getBackground();
        back.setColor(rippleList);
        removeBtn.setTextColor(getResources().getColor(theme[3]));
        removeBtn.setBackgroundTintList(buttonList);
    }

    private void getGroupInfo() {
        Intent intent = getIntent();
        //TODO: Remove default group from intent and handle if intent wasn't passed.
        Long groupID = intent.getLongExtra(getString(R.string.GroupManagement_IntentPackageString),0);
        Log.i(TAG,"GroupID: " + groupID);

        Call<Group> groupcaller = proxy.getGroupById(groupID);
        ProxyBuilder.callProxy(this, groupcaller, returnedGroup -> populateActivityWithGroup(returnedGroup), nothing -> checkGroupStatus());
    }

    private void populateActivityWithGroup(Group g) {
        Log.i(TAG,"Returned group ID: " + g.getId());
        currentGroup = g;
        if (currentGroup.getLeader() == null) {
            hasLeader = false;
        }
        getMemberList();
        setupGroupTitle();
        CheckMessageAuthority();
    }

    private void getMemberList() {
        Log.i(TAG,"# members: " + currentGroup.getMemberUsers().size());

        Call<List<User>> MemberCaller = proxy.getGroupMembers(currentGroup.getId());
        ProxyBuilder.callProxy(this, MemberCaller, returnedUsers -> initialSetup(returnedUsers));
    }

    private void setupGroupTitle() {
        TextView title = findViewById(R.id.GroupManagement_Title);
        title.setText(getString(R.string.GroupManagement_Title, currentGroup.getGroupDescription()));
        Log.i(TAG,getString(R.string.GroupManagement_Title, currentGroup.getGroupDescription()));
    }

    private void initialSetup(List<User> returnedUserList){
        if(returnedUserList.size() > 0) {
            currentGroup.setMemberUsers(returnedUserList);
            for(int i =0; i < returnedUserList.size(); i++){
                UserCache.getInstance().add(returnedUserList.get(i));
            }
        }
        refreshUI();
    }

    private void refreshUI(){
        if(currentGroup !=null && currentGroup.getMemberUsers().size() != 0){
            getMemberInfo();
        }
        else {
            stringMemberInfo = new String[]{};
        }
        setupGroupLeader();
        ListView MemberList = findViewById(R.id.GroupManagement_MemberList);
        ArrayAdapter<String> memberInfo;


        if(InfoAccessAuthority()){
            memberInfo = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringMemberInfo);
        }
        else{
            memberInfo = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.Noauthority));
        }
        MemberList.setAdapter(memberInfo);
    }

    private void getMemberInfo() {
        if(currentGroup != null){
            localMemberList = currentGroup.getMemberUsers();
            Log.i(TAG,"currentGroup_size(): " + currentGroup.getMemberUsers().size());
            for(int i =0; i < currentGroup.getMemberUsers().size(); i++){
                Log.i(TAG,"currentGroup.getMemberList(): " + currentGroup.getMemberUsers().get(i).getName());
            }
        }
        stringMemberInfo = new String[localMemberList.size()];
        Log.i(TAG,"StringMemberInfo.size(): " + stringMemberInfo.length);
        for(int i = 0; i < localMemberList.size(); i++){
            stringMemberInfo[i] = getString(R.string.GroupManagement_Member_string_format, UserCache.getInstance().getUser(localMemberList.get(i).getId()).getName());
            Log.i(TAG,"stringMemberInfo["+i+"] : " + stringMemberInfo[i]);
        }
    }

    private void setupGroupLeader() {
        TextView leader = findViewById(R.id.GroupManagement_Leader);
        if (InfoAccessAuthority()) {
            if (hasLeader) {
                leader.setText(getString(R.string.GroupManagement_Leader, UserCache.getInstance().getUser(currentGroup.getLeader().getId()).getName()));
            }
            else {
                leader.setText(getString(R.string.GroupManagement_nullLeader));
            }
        }
        else {
            leader.setText(getString(R.string.GroupManagement_Access_right));
        }
    }

    private void RegisterOnClickCallBack() {
        ListView list = findViewById(R.id.GroupManagement_MemberList);
        list.setOnItemClickListener((parent, view, position, id) -> {
            if(InfoAccessAuthority()){
                Log.i(TAG, position + "");
                Intent monitorByIntent = MonitorOrByActivity.getIntent(GroupManagementActivity.this, UserCache.getInstance().getUser(localMemberList.get(position).getId()), false);
                startActivity(monitorByIntent);
            }
            else{
                notifyUserViaLogAndToast(getString(R.string.GroupManagement_InfoAccessDenial));
            }
        });
    }

    private boolean InfoAccessAuthority() {
        if (hasLeader) {
            //check parents of leader
            User leader = UserCache.getInstance().getUser(currentGroup.getLeader().getId());
            for(int i =0; i < leader.getMonitoredByUsers().size(); i++){
                if(leader.getMonitoredByUsers().get(i).equals(CurrentSession.getCurrentUser())){
                    return true;
                }
            }
            //check if currentUser is the leader
            if(leader.equals(CurrentSession.getCurrentUser())){
                return true;
            }
        }

        //check parents of members
        for(int i =0; i < localMemberList.size(); i++){
            for(int j =0; j < localMemberList.get(i).getMonitoredByUsers().size(); j++){
                if(localMemberList.get(i).getMonitoredByUsers().get(j).equals(CurrentSession.getCurrentUser())){
                    return true;
                }
            }
        }
        //check if currentUser is a member of the group
        if(localMemberList.contains(CurrentSession.getCurrentUser())){
            return true;
        }
        return false;
    }

    private void CheckMessageAuthority() {
        Button messageBtn = findViewById(R.id.GroupManagement_Message_Btn);
        Button deleteBtn = findViewById(R.id.GroupManagement_DeleteBtn);
        if (!hasLeader || !currentGroup.getLeader().equals(CurrentSession.getCurrentUser())) {
            messageBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }
    }

    private void SetUpMessageBtn() {
        Button MessageBtn = findViewById(R.id.GroupManagement_Message_Btn);
        MessageBtn.setOnClickListener(v -> {
            Intent ComposeIntent = ComposeActivity.getIntent(GroupManagementActivity.this,currentGroup);
            startActivity(ComposeIntent);
        });
    }

    private void SetUpAddBtn() {
        Button button = findViewById(R.id.GroupManagement_AddMember_Btn);
        button.setOnClickListener(v -> {
            AlertDialog.Builder InputBuilder = new AlertDialog.Builder(GroupManagementActivity.this);
            InputBuilder.setCancelable(false);
            InputBuilder.setTitle(getString(R.string.GroupManagement_AddTitle, currentGroup.getGroupDescription()));
            final EditText UserEmailInput = new EditText(GroupManagementActivity.this);
            UserEmailInput.setInputType(InputType.TYPE_CLASS_TEXT);
            UserEmailInput.setHint(getString(R.string.GroupManagement_EditHint));
            InputBuilder.setView(UserEmailInput);

            InputBuilder.setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                String UserEmail = UserEmailInput.getText().toString();
                Log.i(TAG, "User email: " + UserEmail);
                if(!InputValidation.isValidEmail(UserEmail)){
                    notifyUserViaLogAndToast(getString(R.string.GroupManage_invalid_email_try_again));
                }
                else{
                    Call<User> TargetUserCaller = proxy.getUserByEmail(UserEmail);
                    ProxyBuilder.callProxy(GroupManagementActivity.this, TargetUserCaller, returnedUser -> addUserToGroup(returnedUser), returnedNothing -> emailError());
                }
            });

            InputBuilder.setNegativeButton(getText(R.string.cancel), (dialog, which) -> dialog.dismiss());
            AlertDialog InputDialog = InputBuilder.create();
            InputDialog.show();
        });
    }

    private void SetUpDeleteBtn(){
        Button button = findViewById(R.id.GroupManagement_DeleteBtn);
        button.setOnClickListener(v -> {
            if(CheckEmptyList()){
                notifyUserViaLogAndToast(getString(R.string.GroupManage_empty_group));
            }
            else{
                final int[] checkedItem = {0};
                AlertDialog.Builder RemoveUser = new AlertDialog.Builder(GroupManagementActivity.this);
                RemoveUser.setCancelable(false);
                RemoveUser.setTitle(getString(R.string.GroupManagement_RemoveTitle, currentGroup.getGroupDescription()));

                RemoveUser.setSingleChoiceItems(stringMemberInfo, checkedItem[0], (dialog, which) -> checkedItem[0] = which);

                RemoveUser.setPositiveButton(getText(R.string.confirm), (dialog, which) -> {
                    removeUserFromGroup(currentGroup.getMemberUsers().get(checkedItem[0]));
                    dialog.dismiss();
                });

                RemoveUser.setNegativeButton(getText(R.string.cancel), (dialog, which) -> dialog.dismiss());
                AlertDialog RemoveDialog = RemoveUser.create();
                RemoveDialog.show();
            }
        });
    }

    private boolean CheckEmptyList(){
        if(currentGroup.getMemberUsers().size() ==0){
            return true;
        }
        return false;
    }

    private void addUserToGroup(User user){
        if(user == null){
            Log.e(TAG, "Attempt to add null user to group");
            return;
        }
        if(currentGroup == null){
            Log.e(TAG, "Attempt to add user to null group");
            return;
        }
        if(CurrentSession.getCurrentUser().getMonitorsUsers().contains(user)
                || user.equals(CurrentSession.getCurrentUser())){
                Call<List<User>> caller = proxy.addGroupMember(currentGroup.getId(), user);
                ProxyBuilder.callProxy(this, caller, returnedUserList -> receiveUserListFromServer(returnedUserList));
                notifyUserViaLogAndToast(getString(R.string.Permission_AddSent));
        }
        else{
            Toast.makeText(this, R.string.GroupManage_info_only_add_monitored_users, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeUserFromGroup(User user){
        if(user == null){
            Log.e(TAG, "Attempt to remove null user to group");
            return;
        }
        if(currentGroup == null){
            Log.e(TAG, "Attempt to remove user to null group");
            return;
        }
        if(CurrentSession.getCurrentUser().getMonitorsUsers().contains(user)
                || user.equals(CurrentSession.getCurrentUser())
                ||(hasLeader && currentGroup.getLeader().equals(CurrentSession.getCurrentUser()))){
           Call<Void> caller = proxy.removeGroupMember(currentGroup.getId(), user.getId());
           ProxyBuilder.callProxy(this, caller, nothing -> { });

           //remove group if there is no leader and members left in the group
            notifyUserViaLogAndToast(getString(R.string.Permission_RemoveSent));
            checkGroupStatus();
            refreshUI();
        }
        else{
            Toast.makeText(this, R.string.GroupManage_only_leader_can_remove, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGroupStatus(){
        if(CheckEmptyList() && !hasLeader){
            removeGroup();
            notifyUserViaLogAndToast(getString(R.string.GroupManagement_deleteGroup));
            finish();
        }
    }

    private void removeGroup() {
        Call<Void> deleteCaller = proxy.deleteGroup(currentGroup.getId());
        ProxyBuilder.callProxy(this, deleteCaller, nothing -> {});
    }

    //Server callbacks
    private void receiveUserListFromServer(List<User> returnedUserList) {
        Log.i(TAG,"ListFromServer");
        currentGroup.setMemberUsers(returnedUserList);
        for(int i =0; i < returnedUserList.size(); i++){
            UserCache.getInstance().add(returnedUserList.get(i));
            Log.i(TAG,"ListFromServercache");
        }
        refreshUI();
    }

    private void emailError() {
        notifyUserViaLogAndToast(getResources().getString(R.string.AddMonitor_Invalid_user_email));
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static Intent getIntent(Context context, Long id){
        Intent intent = new Intent(context, GroupManagementActivity.class);
        intent.putExtra(context.getResources().getString(R.string.GroupManagement_IntentPackageString), id);
        return intent;
    }
}
