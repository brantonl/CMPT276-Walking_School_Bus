package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import lava.walkinggroup.dataobjects.PermissionRequest;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.proxy.WGServerProxy;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.PermissionListAdapter;
import retrofit2.Call;

import static lava.walkinggroup.utility.CurrentSession.proxy;

/**
 * This activity handles the pending permission requests of the users
 * allow users to decide on pending requests and view previous requests
 */
public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";
    private List<PermissionRequest> cachePendingPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        getLayoutElements();
        setUpViewPreviousBtn();
        getPendingPermissions();
        registerOnClickListener();
    }

    private void getLayoutElements() {
        TextView title = findViewById(R.id.Permission_title);
        Button historyBtn = findViewById(R.id.Permission_previousHistoryBtn);
        ListView list = findViewById(R.id.Permission_list);

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

        View root = title.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));

        title.setTextColor(getResources().getColor(theme[3]));
        RippleDrawable back = (RippleDrawable) historyBtn.getBackground();
        back.setColor(rippleList);
        historyBtn.setTextColor(getResources().getColor(theme[3]));
        historyBtn.setBackgroundTintList(buttonList);
        list.setBackgroundColor(getResources().getColor(theme[3]));
    }

    private void setUpViewPreviousBtn() {
        Button previousBtn = findViewById(R.id.Permission_previousHistoryBtn);
        previousBtn.setOnClickListener(v -> {
            startActivity(PreviousRequestActivity.getIntent(PermissionActivity.this));
        });
    }

    private void getPendingPermissions() {
        Call<List<PermissionRequest>> pendingRequestCaller = proxy.getPendingPermissions(CurrentSession.getCurrentUser().getId());
        ProxyBuilder.callProxy(this, pendingRequestCaller, returnedPermissions -> populateCachePermissions(returnedPermissions));
    }

    private void populateCachePermissions(List<PermissionRequest> returnedPermissions) {
        cachePendingPermissions = returnedPermissions;
        populateRequestList();
    }

    private void populateRequestList() {
        ListView requestList = findViewById(R.id.Permission_list);
        PermissionListAdapter requests = new PermissionListAdapter(this, R.layout.previouspermissions_layout, cachePendingPermissions);
        requestList.setAdapter(requests);
    }

    private void registerOnClickListener() {
        ListView requestList = findViewById(R.id.Permission_list);
        requestList.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder actionOnrequest = new AlertDialog.Builder(PermissionActivity.this);
            actionOnrequest.setCancelable(false);
            actionOnrequest.setTitle(getString(R.string.Permission_dialogTitle));

            actionOnrequest.setPositiveButton(getString(R.string.accept), (dialog, which) -> {
                Call<PermissionRequest> approveCaller = proxy.approveOrDenyPermissionRequest(cachePendingPermissions.get(position).getId(), WGServerProxy.PermissionStatus.APPROVED);
                ProxyBuilder.callProxy(PermissionActivity.this, approveCaller, returnedPermission -> getPendingPermissions());
            });

            actionOnrequest.setNegativeButton(getString(R.string.deny), (dialog, which) -> {
                Call<PermissionRequest> denyCaller = proxy.approveOrDenyPermissionRequest(cachePendingPermissions.get(position).getId(), WGServerProxy.PermissionStatus.DENIED);
                ProxyBuilder.callProxy(PermissionActivity.this, denyCaller, returnedPermission -> getPendingPermissions());
            });

            actionOnrequest.setNeutralButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

            AlertDialog actionDialog = actionOnrequest.create();
            actionDialog.show();
        });
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, PermissionActivity.class);
    }
}
