package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import lava.walkinggroup.dataobjects.EarnedRewards;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.dataobjects.rewards.ColorTheme;
import lava.walkinggroup.dataobjects.rewards.MapTheme;
import lava.walkinggroup.dataobjects.rewards.ProfilePic;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import retrofit2.Call;

/**
 * This class is to allow the user to customize the themes that they want to apply and to check the previous unlocked rewards
 */
public class CustomizeProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_profile);

        initSpinners();
        updateImageViews();
    }

    private void setElements() {

        CurrentSession session = CurrentSession.getInstance();
        //grab currently selected themes from Current User
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        //create statelists for the Button (ripple and normal state
        int[][] states = new int[][]{new int[]{android.R.attr.state_enabled}};
        int[] colors = new int[]{getResources().getColor(theme[0])};
        int[][] states2 = new int[][]{new int[]{android.R.attr.state_enabled}};
        int[] colors2 = new int[]{getResources().getColor(theme[1])};

        ColorStateList rippleList = new ColorStateList(states, colors);
        ColorStateList buttonList = new ColorStateList(states2, colors2);
        //statelists^

        TextView mapDesc = this.findViewById(R.id.CustomizeProfile_TextView_MapTheme);
        TextView colorDesc = this.findViewById(R.id.CustomizeProfile_TextView_ColorTheme);
        TextView profilDesc = this.findViewById(R.id.CustomizeProfile_TextView_ProfilePic);
        Spinner mapSpin = this.findViewById(R.id.CustomizeProfile_Spinner_MapTheme);
        Spinner colorSpin = this.findViewById(R.id.CustomizeProfile_Spinner_ColorTheme);
        Spinner profilSpin = this.findViewById(R.id.CustomizeProfile_Spinner_ProfilePic);

        mapDesc.setTextColor(getResources().getColor(theme[3]));
        colorDesc.setTextColor(getResources().getColor(theme[3]));
        profilDesc.setTextColor(getResources().getColor(theme[3]));
        mapSpin.setBackgroundColor(getResources().getColor(theme[3]));
        colorSpin.setBackgroundColor(getResources().getColor(theme[3]));
        profilSpin.setBackgroundColor(getResources().getColor(theme[3]));

        View root = mapDesc.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));
    }
    @Override
    public void onBackPressed() {
        //Update server with modified current user
        User currentUser = CurrentSession.getCurrentUser();
        Call<User> caller = CurrentSession.proxy.editUser(CurrentSession.getCurrentUser().getId(), CurrentSession.getCurrentUser());
        ProxyBuilder.callProxy(caller, nothing -> {});
        super.onBackPressed();
    }

    private void initSpinners() {
        EarnedRewards rewards = CurrentSession.getCurrentUser().getRewards();
        if(rewards == null){
            return;
        }
        Spinner mapThemeSpinner = findViewById(R.id.CustomizeProfile_Spinner_MapTheme);

        ArrayAdapter<MapTheme> mapThemeAdapter = new ArrayAdapter(this, R.layout.unlocked_map_themes, rewards.getUnlockedMapThemes().toArray());
        mapThemeSpinner.setAdapter(mapThemeAdapter);

        MapTheme selectedMapTheme = rewards.getSelectedMapTheme();
        int mapSpinnerPosition = mapThemeAdapter.getPosition(selectedMapTheme);
        mapThemeSpinner.setSelection(mapSpinnerPosition);

        mapThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String mapName = mapThemeAdapter.getItem(position).getName();
                EarnedRewards rewards = CurrentSession.getCurrentUser().getRewards();
                rewards.setSelectedMapThemeName(mapName);
                updateImageViews();
                CurrentSession.getMapsActivity().changeMapTheme(rewards.getSelectedMapTheme());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner colorThemeSpinner = findViewById(R.id.CustomizeProfile_Spinner_ColorTheme);

        ArrayAdapter<ColorTheme> colorThemeAdapter = new ArrayAdapter(this, R.layout.unlocked_color_themes, rewards.getUnlockedColorThemes().toArray());
        colorThemeSpinner.setAdapter(colorThemeAdapter);

        ColorTheme selectedColorTheme = rewards.getSelectedColorTheme();
        int colorSpinnerPosition = colorThemeAdapter.getPosition(selectedColorTheme);
        colorThemeSpinner.setSelection(colorSpinnerPosition);

        colorThemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String colorName = colorThemeAdapter.getItem(position).getName();
                CurrentSession.getCurrentUser().getRewards().setSelectedColorThemeName(colorName);
                setElements();
                updateImageViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner profilePicSpinner = findViewById(R.id.CustomizeProfile_Spinner_ProfilePic);

        ArrayAdapter<ProfilePic> profilePicAdapter = new ArrayAdapter(this, R.layout.unlocked_profilepic_themes, rewards.getUnlockedProfilePics().toArray());
        profilePicSpinner.setAdapter(profilePicAdapter);

        ProfilePic selectedProfilePic = rewards.getSelectedProfilePic();
        int profilePicSpinnerPosition = profilePicAdapter.getPosition(selectedProfilePic);
        profilePicSpinner.setSelection(profilePicSpinnerPosition);

        profilePicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String profilePicName = profilePicAdapter.getItem(position).getName();
                CurrentSession.getCurrentUser().getRewards().setSelectedProfilePicName(profilePicName);
                updateImageViews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateImageViews() {
        EarnedRewards rewards = CurrentSession.getCurrentUser().getRewards();

        ImageView mapThemeIView = findViewById(R.id.CustomizeProfile_ImageView_MapTheme);
        ImageView colorThemeIView = findViewById(R.id.CustomizeProfile_ImageView_ColorTheme);
        ImageView profilePicIView = findViewById(R.id.CustomizeProfile_ImageView_ProfilePic);

        mapThemeIView.setImageDrawable(rewards.getSelectedMapTheme().getPreview());
        colorThemeIView.setImageDrawable(rewards.getSelectedColorTheme().getPreview());
        profilePicIView.setImageDrawable(rewards.getSelectedProfilePic().getPreview());
    }

    public static Intent getIntent(Context context){
        return new Intent(context, CustomizeProfileActivity.class);
    }
}
