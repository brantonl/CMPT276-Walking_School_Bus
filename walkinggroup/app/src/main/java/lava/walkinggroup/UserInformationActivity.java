package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.InputValidation;
import lava.walkinggroup.utility.UserCache;
import retrofit2.Call;

/**
 * This class is to show the details of the user and allow the user to edit their personal info that is stored on the server.
 */

public class UserInformationActivity extends AppCompatActivity {
    private static final String TAG = "User Info Activity";
    private CurrentSession currentSession;
    private static final String LAUNCH_INTENT_USER_TAG = "Launch intent tag for user id extra";

    private User user;
    private boolean editPermission;
    private List<EditText> fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);

        currentSession = currentSession.getInstance(this);

        Long userId = getIntent().getLongExtra(LAUNCH_INTENT_USER_TAG, -1);
        user = UserCache.getInstance().getUser(userId);
        getLayoutElement();
        updateUserFromServer();
    }

    private void getLayoutElement() {
        TextView nameLabel = findViewById(R.id.UserInformation_TextView_Name);
        EditText nameField = findViewById(R.id.UserInformation_EditText_NameField);

        TextView yearLabel = findViewById(R.id.UserInformation_TextView_BirthYear);
        EditText birthdayYearField = findViewById(R.id.UserInformation_EditText_BirthYearField);

        TextView monthLabel = findViewById(R.id.UserInformation_TextView_BirthMonth);

        TextView emailLabel = findViewById(R.id.UserInformation_TextView_Email);
        EditText emailField = findViewById(R.id.UserInformation_EditText_EmailField);

        TextView homephoneLabel = findViewById(R.id.UserInformation_TextView_HomePhone);
        EditText homePhoneField = findViewById(R.id.UserInformation_EditText_HomePhoneField);

        TextView celllabel = findViewById(R.id.UserInformation_TextView_CellPhone);
        EditText cellPhoneField = findViewById(R.id.UserInformation_EditText_CellPhoneField);

        TextView addressLabel = findViewById(R.id.UserInformation_TextView_Address);
        EditText addressField = findViewById(R.id.UserInformation_EditText_AddressField);

        TextView gradeLabel = findViewById(R.id.UserInformation_TextView_Grade);
        EditText gradeField = findViewById(R.id.UserInformation_EditText_GradeField);

        TextView teacherLabel = findViewById(R.id.UserInformation_TextView_TeacherName);
        EditText teacherNameField = findViewById(R.id.UserInformation_EditText_TeacherNameField);

        TextView contactLabel = findViewById(R.id.UserInformation_TextView_EmergencyContact);
        EditText emergencyContactField = findViewById(R.id.UserInformation_EditText_EmergencyContactField);

        Button saveBtn = findViewById(R.id.UserInformation_Button_Save);
        Button backBtn = findViewById(R.id.UserInformation_Button_Back);

        CurrentSession session = CurrentSession.getInstance();
        //grab currently selected themes from Current User
        int[] theme = session.getCurrentUser().getRewards().getSelectedColorTheme().getColorTheme();

        //create state lists for the Button (ripple and normal state
        int[][] states = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors = new int[] { getResources().getColor(theme[0])};
        int[][] states2 = new int[][] { new int[] { android.R.attr.state_enabled} };
        int[] colors2 = new int[] { getResources().getColor(theme[1])};

        ColorStateList rippleList = new ColorStateList(states, colors);
        ColorStateList buttonList = new ColorStateList(states2, colors2);
        //state lists^

        View root = nameField.getRootView();
        root.setBackgroundColor(getResources().getColor(theme[0]));

        nameLabel.setTextColor(getResources().getColor(theme[3]));
        nameField.setTextColor(getResources().getColor(theme[3]));

        yearLabel.setTextColor(getResources().getColor(theme[3]));
        birthdayYearField.setTextColor(getResources().getColor(theme[3]));

        monthLabel.setTextColor(getResources().getColor(theme[3]));

        emailLabel.setTextColor(getResources().getColor(theme[3]));
        emailField.setTextColor(getResources().getColor(theme[3]));

        homephoneLabel.setTextColor(getResources().getColor(theme[3]));
        homePhoneField.setTextColor(getResources().getColor(theme[3]));

        celllabel.setTextColor(getResources().getColor(theme[3]));
        cellPhoneField.setTextColor(getResources().getColor(theme[3]));

        addressLabel.setTextColor(getResources().getColor(theme[3]));
        addressField.setTextColor(getResources().getColor(theme[3]));

        gradeLabel.setTextColor(getResources().getColor(theme[3]));
        gradeField.setTextColor(getResources().getColor(theme[3]));

        teacherLabel.setTextColor(getResources().getColor(theme[3]));
        teacherNameField.setTextColor(getResources().getColor(theme[3]));

        contactLabel.setTextColor(getResources().getColor(theme[3]));
        emergencyContactField.setTextColor(getResources().getColor(theme[3]));

        RippleDrawable back = (RippleDrawable) saveBtn.getBackground();
        back.setColor(rippleList);
        saveBtn.setTextColor(getResources().getColor(theme[3]));
        saveBtn.setBackgroundTintList(buttonList);

        back = (RippleDrawable) backBtn.getBackground();
        back.setColor(rippleList);
        backBtn.setTextColor(getResources().getColor(theme[3]));
        backBtn.setBackgroundTintList(buttonList);
    }

    private void updateUserFromServer() {
        Call<User> caller = CurrentSession.proxy.getUserById(user.getId());
        ProxyBuilder.callProxy(this, caller, updatedUser -> initUI(updatedUser));
    }

    private void initUI(User updatedUser) {
        this.user = updatedUser;
        setEditPermission();
        initFields();
        initBirthMonthSpinner();
        initSaveButton();
        initBackButton();

    }

    private void setEditPermission() {
        //CurrentUser has edit permission for user if currentUser == user or if currentUser monitors user
        this.editPermission = (CurrentSession.getCurrentUser().equals(user)) ||
                (CurrentSession.getCurrentUser().getMonitorsUsers().contains(user));
    }

    private List<EditText> getFields() {
        if (fields == null){
            fields = new ArrayList<>();
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_NameField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_BirthYearField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_EmailField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_HomePhoneField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_CellPhoneField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_AddressField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_GradeField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_TeacherNameField));
            fields.add((EditText) findViewById(R.id.UserInformation_EditText_EmergencyContactField));
        }
        return fields;
    }

    private void initFields(){
        if(!editPermission){
            disableFields();
        }
        EditText nameField = findViewById(R.id.UserInformation_EditText_NameField);
        EditText birthdayYearField = findViewById(R.id.UserInformation_EditText_BirthYearField);
        EditText emailField = findViewById(R.id.UserInformation_EditText_EmailField);
        EditText homePhoneField = findViewById(R.id.UserInformation_EditText_HomePhoneField);
        EditText cellPhoneField = findViewById(R.id.UserInformation_EditText_CellPhoneField);
        EditText addressField = findViewById(R.id.UserInformation_EditText_AddressField);
        EditText gradeField = findViewById(R.id.UserInformation_EditText_GradeField);
        EditText teacherNameField = findViewById(R.id.UserInformation_EditText_TeacherNameField);
        EditText emergencyContactField = findViewById(R.id.UserInformation_EditText_EmergencyContactField);

        nameField.setText(user.getName());
        //TODO:: combine birth year and birth month fields
        Integer birthYear = user.getBirthYear();
        if(birthYear != null) {
            birthdayYearField.setText(birthYear.toString());
        }
        emailField.setText(user.getEmail());
        homePhoneField.setText(user.getHomePhone());
        cellPhoneField.setText(user.getCellPhone());
        addressField.setText(user.getAddress());
        gradeField.setText(user.getGrade());
        teacherNameField.setText(user.getTeacherName());
        emergencyContactField.setText(user.getEmergencyContactInfo());
    }

    private void disableFields(){
        List<EditText> fields = getFields();
        for(EditText field : fields){
                //make prevent fields from being edited if current user does not have permission
                field.setFocusable(false);
                field.setLongClickable(false);
                field.setKeyListener(null);
        }
        Spinner birthMonthSpinner = findViewById(R.id.UserInformation_Spinner_BirthMonth);
        birthMonthSpinner.setClickable(false);
        birthMonthSpinner.setEnabled(false);
    }

    private void initBirthMonthSpinner(){
        Spinner birthMonthSpinner = findViewById(R.id.UserInformation_Spinner_BirthMonth);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.BirthMonth, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        birthMonthSpinner.setAdapter(adapter);
        if(InputValidation.isValidBirthMonth(user.getBirthMonth())){
            birthMonthSpinner.setSelection(user.getBirthMonth());
        } else {
            birthMonthSpinner.setSelection(0);
        }
    }

    private void initSaveButton() {
        Button saveButton = findViewById(R.id.UserInformation_Button_Save);
        if(editPermission){
            saveButton.setOnClickListener(v -> saveChanges());
        }
        else {
            saveButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initBackButton() {
        Button backButton = findViewById(R.id.UserInformation_Button_Back);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void saveChanges(){
        EditText nameField = findViewById(R.id.UserInformation_EditText_NameField);
        EditText birthdayYearField = findViewById(R.id.UserInformation_EditText_BirthYearField);
        Spinner monthSpinner = findViewById(R.id.UserInformation_Spinner_BirthMonth);
        EditText emailField = findViewById(R.id.UserInformation_EditText_EmailField);
        EditText homePhoneField = findViewById(R.id.UserInformation_EditText_HomePhoneField);
        EditText cellPhoneField = findViewById(R.id.UserInformation_EditText_CellPhoneField);
        EditText addressField = findViewById(R.id.UserInformation_EditText_AddressField);
        EditText gradeField = findViewById(R.id.UserInformation_EditText_GradeField);
        EditText teacherNameField = findViewById(R.id.UserInformation_EditText_TeacherNameField);
        EditText emergencyContactField = findViewById(R.id.UserInformation_EditText_EmergencyContactField);

        String name = nameField.getText().toString();
        if(InputValidation.isValidName(name)){
            user.setName(name);
        }
        try{
        Integer birthYear = Integer.parseInt(birthdayYearField.getText().toString());
            if(InputValidation.isValidBirthYear(birthYear)){
                user.setBirthYear(birthYear);
            }
        }catch(NumberFormatException e){
            Log.e(TAG, e.getMessage());
        }

        Integer birthMonth = monthSpinner.getSelectedItemPosition();
        if(birthMonth != 0) {
            user.setBirthMonth(birthMonth);
        }

        String email = emailField.getText().toString();
        if(InputValidation.isValidEmail(email)){
            user.setEmail(email);
        }
        String homePhone = homePhoneField.getText().toString();
        if(InputValidation.isValidPhoneNumber(homePhone)){
            user.setHomePhone(homePhone);
        }
        String cellPhone = cellPhoneField.getText().toString();
        if(InputValidation.isValidPhoneNumber(cellPhone)){
            user.setCellPhone(cellPhone);
        }
        String address = addressField.getText().toString();
        if(InputValidation.isValidAddress(address)){
            user.setAddress(address);
        }
        String grade = gradeField.getText().toString();
        if(InputValidation.isValidGrade(grade)){
            user.setGrade(grade);
        }
        String teacherName = teacherNameField.getText().toString();
        if(InputValidation.isValidName(teacherName)){
            user.setTeacherName(teacherName);
        }
        String emergencyContact = emergencyContactField.getText().toString();
        if(InputValidation.isValidEmergencyContactInfo(emergencyContact)){
            user.setEmergencyContactInfo(emergencyContact);
        }

        //Post edited user to server.
        Call<User> caller = CurrentSession.proxy.editUser(user.getId(), user);
        ProxyBuilder.callProxy(this, caller, returnedUser -> callback(returnedUser));
    }

    private void callback(User returnedUser) {
        notifyUserViaLogAndToast(getString(R.string.save_changes));
    }

    /**
     * Returns intent to launch an instance of UserInformationActivity using
     * startActivity(intent);
     * Displays and edits currentUser's information if no user is provided.
     * @param context context of the calling activity.
     * @param user User who's information is to be displayed and potentially edited. Defaults to currentUser.
     * @return intent to launch an instance of UserInformationActivity
     */
    public static Intent getIntent(Context context, User user){
        Intent intent = new Intent(context, UserInformationActivity.class);
        intent.putExtra(LAUNCH_INTENT_USER_TAG, user.getId().longValue());
        return intent;
    }

    /**
     * Returns intent to launch an instance of UserInformationActivity using
     * startActivity(intent);
     * Displays and edits currentUser's information if no user is provided.
     * @param context context of the calling activity.
     * @return intent to launch an instance of UserInformationActivity
     */
    public static Intent getIntent(Context context){
        return getIntent(context, CurrentSession.getCurrentUser());
    }

    /**
     * Put message up in toast and logcat
     * @param message message to be displayed in toast and log
     */
    private void notifyUserViaLogAndToast(String message) {
        Log.w(getString(R.string.UserManagement_tag), message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
