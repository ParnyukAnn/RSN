package com.aparnyuk.rsn.login;

import com.aparnyuk.rsn.Utils.Constants;
import com.aparnyuk.rsn.activity.MainActivity;
import com.aparnyuk.rsn.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    /* EditText that is used to enter information about the created or logged in user */
    private EditText mEmail;
    private EditText mPassword;
    private TextView mSignUp;

    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;

    /* A reference to the Firebase */
    private Firebase mFirebaseRef;

    /* Data from the authenticated user */
    private AuthData mAuthData;

    /* Listener for Firebase session changes */
    private Firebase.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.edit_text_email_login);
        mPassword = (EditText) findViewById(R.id.edit_text_password_login);

        Button mPasswordLoginButton = (Button) findViewById(R.id.login_with_password);
        assert (mPasswordLoginButton != null);
        mPasswordLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithPassword(mEmail.getText().toString(), mPassword.getText().toString());
                //   Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //   startActivity(intent);
            }
        });

                /* Open LoginActivity when user taps on "Sign in" textView */
        mSignUp = (TextView) findViewById(R.id.tv_sign_up);
        mSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                onSignUpPressed(view);
            }
        });

        /* Create the Firebase ref */
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        createProgressDialog();

        /* Delete this later */
        mAuthStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                mAuthProgressDialog.dismiss();
                setAuthenticatedUser(authData);
            }
        };
        /* Check if the user is authenticated with Firebase already. If this is true we set the authenticated user */
        mFirebaseRef.addAuthStateListener(mAuthStateListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if changing configurations, stop tracking firebase session.
        mFirebaseRef.removeAuthStateListener(mAuthStateListener);
    }

    /* Delete this later */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            String name = authData.getUid();
            if (name != null) {
                Toast.makeText(this, "Logged in as " + name + " (" + authData.getProvider() + ")", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No authenticated user", Toast.LENGTH_LONG).show();
        }
        this.mAuthData = authData;
    }

    /* Setup the progress dialog that is displayed later when authenticating with Firebase */
    private void createProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading");
        mAuthProgressDialog.setMessage("Authenticating with Firebase...");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.show();
    }

    /* Show errors to users */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public void loginWithPassword(String email, String password) {
        mAuthProgressDialog.show();
        mFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {

            @Override
            public void onAuthenticated(AuthData authData) {
                mAuthProgressDialog.dismiss();
                Log.i(TAG, "Auth successful");
                setAuthenticatedUser(authData);
//!!!
                /*if (MainService.state) {
                    startService(new Intent(this, MainService.class));
                } else if (MainService.no_data) {
                    stopService(new Intent(this, MainService.class));
                    Log.d(TAG,"delete in nav");
                    startService(new Intent(this, MainService.class));
                }*/
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationError(FirebaseError error) {
                mAuthProgressDialog.dismiss();
                String errorMessage;
                // Something went wrong :(
                switch (error.getCode()) {
                    case FirebaseError.USER_DOES_NOT_EXIST:
                        errorMessage = getString(R.string.user_does_not_exist);
                        break;
                    case FirebaseError.INVALID_PASSWORD:
                        errorMessage = getString(R.string.invalid_password);
                        break;
                    case FirebaseError.EMAIL_TAKEN:
                        errorMessage = getString(R.string.email_taken);
                        break;
                    case FirebaseError.INVALID_CREDENTIALS:
                        errorMessage = getString(R.string.invalid_credentials);
                        break;
                    case FirebaseError.INVALID_EMAIL:
                        errorMessage = getString(R.string.invalid_email);
                        break;
                    case FirebaseError.NETWORK_ERROR:
                        errorMessage = getString(R.string.network_error);
                        break;
                    case FirebaseError.PREEMPTED:
                        errorMessage = getString(R.string.preempted);
                        break;
                    case FirebaseError.UNKNOWN_ERROR:
                        errorMessage = getString(R.string.unknown_error);
                        break;
                    default:
                        errorMessage = getString(R.string.unknown_error);
                        break;
                }
                showErrorDialog(errorMessage);
            }
        });
    }

    /* Open CreateAccountActivity when user taps on "Sign up" TextView */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    private void logout() {
        if (this.mAuthData != null) {
            /* logout of Firebase */
            mFirebaseRef.unauth();
            /* Update authenticated user and show login buttons */
            setAuthenticatedUser(null);
        }
    }
}
