package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import lava.walkinggroup.dataobjects.PermissionRequest;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.proxy.WGServerProxy;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.PermissionListAdapter;
import retrofit2.Call;

import static lava.walkinggroup.utility.CurrentSession.proxy;

/**
 * This activity allows the user to see the previous permissions that they approved or denied
 */
public class PreviousRequestActivity extends AppCompatActivity {
    private static final String TAG = "PreviousRequestActivity";
    private List<PermissionRequest> cachePreviousPermissions;

    private String[] previousRequestDescribtion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_request);
        getPreviousPermissions();
        getLayoutElements();
    }

    private void getLayoutElements() {
        TextView title = findViewById(R.id.PreviousPermission_title);
        ListView list = findViewById(R.id.PreviousPermission_list);
        CurrentSession session = CurrentSession.getInstance();
        //grab currently selected themes from Current User
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        View root = title.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));

        title.setTextColor(getResources().getColor(theme[3]));
        list.setBackgroundColor(getResources().getColor(theme[3]));
    }

    private void getPreviousPermissions() {
        Call<List<PermissionRequest>> requestCaller = proxy.getPermissionsByUserId(CurrentSession.getCurrentUser().getId());
        ProxyBuilder.callProxy(PreviousRequestActivity.this, requestCaller, returnedRequests -> populateCacheRequests(returnedRequests));
    }

    private void populateCacheRequests(List<PermissionRequest> returnedRequests) {
        boolean nextPermission = false;
        cachePreviousPermissions = returnedRequests;
        for (int i =0; i < cachePreviousPermissions.size(); i++) {
            for (PermissionRequest.Authorizor a : cachePreviousPermissions.get(i).getAuthorizors()) {
                for (User user : a.getUsers()) {
                    if (user.equals(CurrentSession.getCurrentUser())
                            && a.getStatus() == WGServerProxy.PermissionStatus.PENDING
                            && cachePreviousPermissions.get(i).getStatus() == WGServerProxy.PermissionStatus.PENDING) {
                        nextPermission = true;
                        break;
                    }
                }
                if (nextPermission) {
                    break;
                }
            }
            if (nextPermission) {
                nextPermission = false;
                cachePreviousPermissions.remove(i);
            }
        }
        populateListView();
    }


    private void populateListView() {
        PermissionListAdapter permissionAdapter = new PermissionListAdapter(PreviousRequestActivity.this, R.layout.previouspermissions_layout, cachePreviousPermissions);
        ListView list = findViewById(R.id.PreviousPermission_list);
        list.setAdapter(permissionAdapter);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, PreviousRequestActivity.class);
    }
}
