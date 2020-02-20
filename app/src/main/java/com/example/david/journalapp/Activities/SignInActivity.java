package com.example.david.journalapp.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.david.journalapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import static com.example.david.journalapp.Helper.cancelProgress;
import static com.example.david.journalapp.Helper.isConnected;
import static com.example.david.journalapp.Helper.openProgress;

public class SignInActivity extends Activity {
    GoogleSignInClient mGoogleSignInClient;
    public static int RC_SIGN_IN = 1;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        progressDialog = new ProgressDialog(this);
        try {
            Log.d("DDDD", account.toJson());
        } catch (Exception e) {
            Log.d("DDDD", "AFTER");
            Log.w("FAILURE", "Error adding document", e);
        }

        if (account != null) {
            Log.d("STRAT", "IN HERE!");
            startActivity(new Intent(this, Main2Activity.class));
            finish();
        }
        setStatusBarColor();

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setStatusBarColor() {
        Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
    }

    /**
     * It signs a user in.
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        openProgress(progressDialog, "Loading...");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        openProgress(progressDialog, "Logining...");

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /**
     * It handles signin request
     *
     * @param completedTask The completed task
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.d("INSIDE", "INSIDE SIGN IN");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("INSIDE", "INSIDE SIGN IN1");
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d("INSIDE", "INSIDE SIGN IN2");
            SharedPreferences.Editor editor = sharedPref.edit();
            Log.d("INSIDE", "INSIDE SIGN IN3");
            editor.putString("loginUser", account.getEmail());
            Log.d("INSIDE", "INSIDE SIGN IN4");
            editor.apply();
            Log.d("INSIDE", "INSIDE SIGN IN5");

            cancelProgress(progressDialog);
            Log.d("INSIDE", "INSIDE SIGN IN6");
            startActivity(new Intent(this, Main2Activity.class));
            Log.d("INSIDE", "INSIDE SIGN IN7");
            finish();

        } catch (ApiException e) {
            cancelProgress(progressDialog);
            Log.d("", "AFTER");
            Log.w("FAILURE", "Error adding document", e);
            if (isConnected(SignInActivity.this)) {
                Toast.makeText(SignInActivity.this, "Could not sign in an error occurred", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SignInActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
