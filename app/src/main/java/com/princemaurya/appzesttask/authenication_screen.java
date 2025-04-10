package com.princemaurya.appzesttask;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class authenication_screen extends AppCompatActivity {
    private static final String TAG = "GoogleSignIn";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authenication_screen);

        // Print SHA-1 for debugging
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sha1 = bytesToHex(md.digest());
                Log.d(TAG, "SHA-1: " + sha1);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e(TAG, "Error getting SHA-1: " + e.getMessage());
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "Firebase Auth initialized");

        // Configure Google Sign In
        String webClientId = getString(R.string.default_web_client_id);
        Log.d(TAG, "Web Client ID: " + webClientId);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d(TAG, "Google Sign In client initialized");

        // Initialize the sign-in launcher
        signInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Sign in result received. Result code: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        try {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            handleSignInResult(task);
                        } catch (Exception e) {
                            Log.e(TAG, "Error getting sign in result: " + e.getMessage(), e);
                            Toast.makeText(this, "Error during sign in: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMsg = "Sign in failed with result code: " + result.getResultCode();
                        Log.e(TAG, errorMsg);
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });

        // Set up the Google sign-in button
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> {
            Log.d(TAG, "Google sign in button clicked");
            signIn();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void signIn() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            Log.d(TAG, "Starting Google Sign In");
            signInLauncher.launch(signInIntent);
        } catch (Exception e) {
            String errorMsg = "Error starting Google Sign In: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Log.d(TAG, "Google Sign In successful. Email: " + account.getEmail());
                String idToken = account.getIdToken();
                if (idToken != null) {
                    Log.d(TAG, "ID Token received");
                    firebaseAuthWithGoogle(idToken);
                } else {
                    String errorMsg = "ID Token is null";
                    Log.e(TAG, errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
            } else {
                String errorMsg = "Google Sign In account is null";
                Log.e(TAG, errorMsg);
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        } catch (ApiException e) {
            String errorMsg = "Google sign in failed: " + e.getMessage() + " (Status code: " + e.getStatusCode() + ")";
            Log.e(TAG, errorMsg, e);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d(TAG, "Starting Firebase authentication with Google");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase authentication successful");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "User signed in: " + user.getEmail());
                            navigateToHomepage();
                        } else {
                            String errorMsg = "Firebase user is null after successful authentication";
                            Log.e(TAG, errorMsg);
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String errorMsg = "Firebase authentication failed: " + 
                            (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, errorMsg, task.getException());
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToHomepage() {
        try {
            Log.d(TAG, "Navigating to homepage");
            Intent intent = new Intent(this, homepage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            String errorMsg = "Error navigating to homepage: " + e.getMessage();
            Log.e(TAG, errorMsg, e);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already signed in: " + currentUser.getEmail());
            navigateToHomepage();
        } else {
            Log.d(TAG, "No user currently signed in");
        }
    }
}