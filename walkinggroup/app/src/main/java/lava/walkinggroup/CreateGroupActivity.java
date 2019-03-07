package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

import lava.walkinggroup.dataobjects.Group;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import retrofit2.Call;

/**
 * Activity to create new groups
 *
 * User specifies a group description and selects start and end locations
 * using Google PlacePickers.
 *
 * Returns to map upon creation of new group.
 */
public class CreateGroupActivity extends AppCompatActivity {
    private static final String TAG = "CreateGroupActivity";
    private final int PLACE_PICKER_STARTLOC_REQUEST = 1;
    private final int PLACE_PICKER_ENDLOC_REQUEST = 2;

    private Float startLat;
    private Float startLong;
    private Float endLat;
    private Float endLong;

    private String startAddress;
    private String endAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        getLayoutElement();
        initOkButton();
        initCancelButton();

        initPlacePickerButtons();
    }

    private void getLayoutElement() {
        TextView title = findViewById(R.id.CreateGroup_Title);
        Button confirmBtn = findViewById(R.id.CreateGroup_Button_Confirm);
        Button cancelBtn = findViewById(R.id.createGroup_Button_Cancel);
        EditText description = findViewById(R.id.CreateGroup_EditText_Description);
        Button pickStartBtn = findViewById(R.id.CreateGroup_Button_PickStartLocation);
        Button pickEndBtn = findViewById(R.id.CreateGroup_Button_PickEndLocation);
        TextView startAddress = findViewById(R.id.CreateGroup_TextView_StartAddress);
        TextView endAddress = findViewById(R.id.CreateGroup_TextView_EndAddress);


        //grab Root to change main background colour
        View root = confirmBtn.getRootView();
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


        RippleDrawable back = (RippleDrawable) confirmBtn.getBackground();
        back.setColor(rippleList);
        confirmBtn.setTextColor(getResources().getColor(theme[3]));
        confirmBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) cancelBtn.getBackground();
        back.setColor(rippleList);
        cancelBtn.setTextColor(getResources().getColor(theme[3]));
        cancelBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) pickStartBtn.getBackground();
        back.setColor(rippleList);
        pickStartBtn.setTextColor(getResources().getColor(theme[3]));
        pickStartBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) pickEndBtn.getBackground();
        back.setColor(rippleList);
        pickEndBtn.setTextColor(getResources().getColor(theme[3]));
        pickEndBtn.setBackgroundTintList(buttonList);


        title.setTextColor(getResources().getColor(theme[2]));
        description.setTextColor(getResources().getColor(theme[3]));
        startAddress.setTextColor(getResources().getColor(theme[3]));
        endAddress.setTextColor(getResources().getColor(theme[3]));
        root.setBackgroundColor(getResources().getColor(theme[0]));

    }

    private void initCancelButton() {
        Button cancelButton = (Button) findViewById(R.id.createGroup_Button_Cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initPlacePickerButtons() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Button pickStartLoc = (Button) findViewById(R.id.CreateGroup_Button_PickStartLocation);
        Button pickEndLoc = (Button) findViewById(R.id.CreateGroup_Button_PickEndLocation);

        bindPlacePickerToButton(pickStartLoc, PLACE_PICKER_STARTLOC_REQUEST);
        bindPlacePickerToButton(pickEndLoc, PLACE_PICKER_ENDLOC_REQUEST);

    }

    private void bindPlacePickerToButton(Button button, int requestCode) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(CreateGroupActivity.this), requestCode);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_STARTLOC_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place startLoc = PlacePicker.getPlace(this, data);
                startLat = (float) startLoc.getLatLng().latitude;
                startLong = (float) startLoc.getLatLng().longitude;
                startAddress = startLoc.getAddress().toString();
                updateStartAddressView();

            }
        } else if (requestCode == PLACE_PICKER_ENDLOC_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place endLoc = PlacePicker.getPlace(this, data);
                endLat = (float) endLoc.getLatLng().latitude;
                endLong = (float) endLoc.getLatLng().longitude;
                endAddress = endLoc.getAddress().toString();
                updateEndAddressView();
            }
        }
    }

    private void updateStartAddressView() {
        TextView startAddressView = (TextView) findViewById(R.id.CreateGroup_TextView_StartAddress);
        startAddressView.setText(this.startAddress);
    }

    private void updateEndAddressView() {
        TextView endAddressView = (TextView) findViewById(R.id.CreateGroup_TextView_EndAddress);
        endAddressView.setText(this.endAddress);
    }

    private void initOkButton() {
        Button okButton = (Button) findViewById(R.id.CreateGroup_Button_Confirm);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText descriptionField = (EditText) findViewById(R.id.CreateGroup_EditText_Description);
                String description = descriptionField.getText().toString();
                createGroup(description, startLat, startLong, endLat, endLong);
            }
        });
    }


    private void createGroup(String description, Float startLat, Float startLong, Float endLat, Float endLong){
        if (startLat == null || startLong == null) {
            notifyUserViaLogAndToast(getString(R.string.CreateGroup_prompt_enter_meeting_place));
            return;
        } else if (endLat == null || endLong == null){
            notifyUserViaLogAndToast(getString(R.string.CreateGroup_prompt_select_destination));
            return;
        } else if (description == null || description.isEmpty()){
            notifyUserViaLogAndToast(getString(R.string.CreateGroup_prompt_description));
            return;
        }

        List<Float> latList = new ArrayList<>();
        latList.add(startLat);
        latList.add(endLat);

        List<Float> longList = new ArrayList<>();
        longList.add(startLong);
        longList.add(endLong);

        Group group = new Group();
        group.setGroupDescription(description);
        group.setRouteLatArray(latList);
        group.setRouteLngArray(longList);

        group.setLeader(CurrentSession.getCurrentUser());
        group.addMember(CurrentSession.getCurrentUser());

        Call<Group> caller = CurrentSession.proxy.createGroup(group);
        ProxyBuilder.callProxy(this, caller, returnedGroup -> returnedGroupCallback(returnedGroup), err->handleError());
    }

    private void handleError() {
        Toast.makeText(this, R.string.CreateGroup_notify_error, Toast.LENGTH_SHORT).show();
    }

    private void returnedGroupCallback(Group returnedGroup) {
        notifyUserViaLogAndToast(getString(R.string.CreateGroup_notify_group_created));
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, CreateGroupActivity.class);
    }

    // Put message up in toast and logcat
    // -----------------------------------------------------------------------------------------
    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
