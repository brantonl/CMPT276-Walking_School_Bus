package lava.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import lava.walkinggroup.dataobjects.EarnedRewards;
import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.InputValidation;
import retrofit2.Call;

/**
 * Activity to create new users
 *
 * Upon creation of new user, user is automatically be logged in
 * and MapsActivity is launched.
 */
public class CreateAccountActivity extends AppCompatActivity {
    private static final String TAG = "CreateAccountActivity";
    private static final String INTENT_EXTRA_EMAIL = "intent extra: 'email'";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        setSupportActionBar(findViewById(R.id.create_account_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initEmailField();
        initCreateAccountButton();
    }

    private void initEmailField() {
        EditText emailField = (EditText) findViewById(R.id.CreateAccountActivity_EditText_Email);
        Intent data = getIntent();
        String email = data.getStringExtra(INTENT_EXTRA_EMAIL);
        if(InputValidation.isValidEmail(email)){
            emailField.setText(email);
        }
    }

    private void initCreateAccountButton() {
        Button createAccountButton = (Button) findViewById(R.id.CreateAccountActivity_Btn_CreateAccount);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CurrentSession currentSession = CurrentSession.getInstance(CreateAccountActivity.this);
                Button signUp = findViewById(R.id.CreateAccountActivity_Btn_CreateAccount);

                EditText nameField = (EditText) findViewById(R.id.CreateAccountActivity_EditText_Name);
                EditText emailField = (EditText) findViewById(R.id.CreateAccountActivity_EditText_Email);
                EditText password1Field = (EditText) findViewById(R.id.CreateAccountActivity_EditText_Password1);
                EditText password2Field = (EditText) findViewById(R.id.CreateAccountActivity_EditText_Password2);

                String name = nameField.getText().toString();
                String email = emailField.getText().toString();
                String password1 = password1Field.getText().toString();
                String password2 = password2Field.getText().toString();

                if(!password1.equals(password2)){
                    Toast.makeText(getApplicationContext(), getString(R.string.validation_password_not_match), Toast.LENGTH_SHORT).show();
                    signUp.getBackground().setColorFilter(null);
                    signUp.setClickable(true);
                    return;
                }
                if(!InputValidation.isValidPassword(password1)){
                    Toast.makeText(getApplicationContext(), getString(R.string.validation_password_too_short), Toast.LENGTH_SHORT).show();
                    signUp.getBackground().setColorFilter(null);
                    signUp.setClickable(true);
                    return;
                }
                if(!InputValidation.isValidName(name)){
                    Toast.makeText(getApplicationContext(), getString(R.string.validation_invalid_name), Toast.LENGTH_SHORT).show();
                    signUp.getBackground().setColorFilter(null);
                    signUp.setClickable(true);
                    return;
                }
                if(!InputValidation.isValidEmail(email)){
                    Toast.makeText(getApplicationContext(), getString(R.string.validation_invalid_email), Toast.LENGTH_LONG).show();
                    signUp.getBackground().setColorFilter(null);
                    signUp.setClickable(true);
                    return;
                }

                User user = new User();
                user.setEmail(email);
                user.setName(name);
                user.setPassword(password1);
                user.setRewards(EarnedRewards.init());
                user.setCurrentPoints(0);
                user.setTotalPointsEarned(0);

                // Grey out button and avoid multiple clicks
                signUp.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                signUp.setClickable(false);


                // Make call
                Call<User> caller = CurrentSession.proxy.createUser(user);
                ProxyBuilder.callProxy(CreateAccountActivity.this, caller,
                                        returnedUser -> handleNewUser(returnedUser, password1),
                                        onError->handleCreateAccountFail());
            }
        });
    }


    /**
     * Restore button to normal so user can try again
     */
    private void handleCreateAccountFail() {
        Button signUp = findViewById(R.id.CreateAccountActivity_Btn_CreateAccount);

        signUp.getBackground().setColorFilter(null);
        signUp.setClickable(true);
    }

    /**
     * Returns to loginactivity where it will log in with the new user.
     * @param user new user being created
     * @param password new user's password
     */
    private void handleNewUser(User user, String password) {
        CurrentSession.setCurrentUser(user);

        notifyUserViaLogAndToast(getString(R.string.create_account_success_msg));

        Intent intent = new Intent();
        intent.putExtra("email", user.getEmail() );
        intent.putExtra("password", password);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Provide intent for launching this activity
     * @param context context of the calling activity
     * @param email email of new user if available, otherwise pass empty string
     * @return intent to launch CreateAccountActivity
     */
    public static Intent getIntent(Context context, String email) {
        Intent intent = new Intent(context, CreateAccountActivity.class);
        intent.putExtra(INTENT_EXTRA_EMAIL, email);
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
}
