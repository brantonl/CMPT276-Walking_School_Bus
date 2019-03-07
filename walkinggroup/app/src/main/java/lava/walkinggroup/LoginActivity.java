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

import lava.walkinggroup.dataobjects.User;
import lava.walkinggroup.proxy.ProxyBuilder;
import lava.walkinggroup.utility.CurrentSession;
import lava.walkinggroup.utility.InputValidation;
import retrofit2.Call;

/**
 * This is the gateway activity which is launched first.
 * It is responsible for setting up the CurrentSession.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    protected static final int ACCOUNT_CREATE_RESULT = 23149;

    private boolean PRE_FILL_FIELDS  = false;
    private boolean AUTO_LOGIN = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setSupportActionBar(findViewById(R.id.login_toolbar));

        setupCurrentSession();

        // Temporary switch for us until we have a logout button implemented
        if (AUTO_LOGIN) {
            autoLoginIfToken();
        }


        if (PRE_FILL_FIELDS) {
            //Temporary, populate login fields so we don't have to punch in 8 char passwords for testing
            EditText emailField = (EditText) findViewById(R.id.LoginActivity_EditText_Email);
            EditText passwordField = (EditText) findViewById(R.id.LoginActivity_EditText_Password);
            emailField.setText("user@u.com");
            passwordField.setText("asdfasdf");
        }


        initSignInButton();
        initCreateAccountButton();

    }

    private void setupCurrentSession() {
        CurrentSession.initialize(getApplicationContext());
        if (CurrentSession.authToken == null) {
            CurrentSession.proxy = ProxyBuilder.getProxy(getString(R.string.apikey), null);
        }
    }

    // Tokens last until logout, so store it and make proxy w/ it to avoid re-login
    private void autoLoginIfToken() {
        if (CurrentSession.authToken != null) {
            notifyUserViaLogAndToast(getString(R.string.autologin_message));
            authenticateWithTokenAndEmail(CurrentSession.authToken, CurrentSession.getCurrentUser().getEmail());
        }
    }

    private void initCreateAccountButton() {
        Button createAccountButton = (Button) findViewById(R.id.LoginActivity_Btn_CreateAccount);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailField = (EditText) findViewById(R.id.LoginActivity_EditText_Email);
                String email = emailField.getText().toString();

                Intent intent = CreateAccountActivity.getIntent(LoginActivity.this, email);
                startActivityForResult(intent, ACCOUNT_CREATE_RESULT);
            }
        });
    }

    // Logs in with new user from CreateAccountActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACCOUNT_CREATE_RESULT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Created new account " + data.getStringExtra("email") + ", logging in.");

                EditText emailField = (EditText) findViewById(R.id.LoginActivity_EditText_Email);
                EditText passwordField = (EditText) findViewById(R.id.LoginActivity_EditText_Password);
                emailField.setText(data.getStringExtra("email"));
                passwordField.setText(data.getStringExtra("password"));

                Button signInButton = (Button) findViewById(R.id.LoginActivity_Btn_SignIn);
                signInButton.callOnClick();
            }
        }
    }

    private void initSignInButton() {
        Button createAccountButton = (Button) findViewById(R.id.LoginActivity_Btn_CreateAccount);
        Button signInButton = (Button) findViewById(R.id.LoginActivity_Btn_SignIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailField = (EditText) findViewById(R.id.LoginActivity_EditText_Email);
                EditText passwordField = (EditText) findViewById(R.id.LoginActivity_EditText_Password);

                String userEmail = emailField.getText().toString();
                String userPassword = passwordField.getText().toString();

                if(!InputValidation.isValidPassword(userPassword)){
                    Toast.makeText(getApplicationContext(), "Password must be 8 or more characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!InputValidation.isValidEmail(userEmail)){
                    Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_LONG).show();
                    return;
                }

                // Grey out buttons and make un-clickable
                signInButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                signInButton.setClickable(false);
                createAccountButton.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                createAccountButton.setClickable(false);

                // Build new user
                User loginUser = new User();
                loginUser.setEmail(userEmail);
                loginUser.setPassword(userPassword);
                loginUser.setHasFullData(false);

                // Register for token received:
                CurrentSession.setConnectionStatus(CurrentSession.ConnectionStatus.WAITING);
                ProxyBuilder.setOnTokenReceiveCallback(token -> authenticateWithTokenAndEmail(token, loginUser.getEmail()));

                // Make call
                Call<Void> caller = CurrentSession.proxy.login(loginUser);
                ProxyBuilder.callProxy(LoginActivity.this, caller, null, onError->handleLoginFail() );
            }
        });
    }

    private void handleLoginFail() {
        Button createAccountButton = (Button) findViewById(R.id.LoginActivity_Btn_CreateAccount);
        Button signInButton = (Button) findViewById(R.id.LoginActivity_Btn_SignIn);

        signInButton.getBackground().setColorFilter(null);
        signInButton.setClickable(true);

        createAccountButton.getBackground().setColorFilter(null);
        createAccountButton.setClickable(true);
    }

    // Handle the token by generating a new Proxy which uses it. Update CurrentSession.
    private void authenticateWithTokenAndEmail(String token, String email) {
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
        CurrentSession.proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);

        CurrentSession.setAuthToken(token);
        getCurrentUserFromServer(email);
    }

    private void getCurrentUserFromServer(String email) {
        //CurrentSession.setCurrentUser(email);
        Call<User> caller = CurrentSession.proxy.getUserByEmail(email);
        ProxyBuilder.callProxy(this, caller, returnedUser -> {
            CurrentSession.setCurrentUser(returnedUser);
            launchMaps();
        }, nothing -> handleLoginFail());
    }

    // Proceed to main activity page
    private void launchMaps() {
        Intent intent = MapsActivity.getIntent(this);

        // Clears activity stack upon launching new activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    // Put message up in toast and logcat
    // -----------------------------------------------------------------------------------------
    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    //TODO: Errors - display as toast and remain on login screen. (incorrect password or user does not exist)

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);

        //Clears activity stack after launching activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }
}
