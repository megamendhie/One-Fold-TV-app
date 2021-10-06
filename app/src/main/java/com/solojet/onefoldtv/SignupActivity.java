package com.solojet.onefoldtv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import config.FirebaseUtils;
import config.Reusable;
import models.Profile;

import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.USERS_PATH;
import static models.ConstantVariables.USER_PROFILE;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtFirstName, edtLastName, edtEmail, edtEmailConfirm, edtPassword;
    private Button btnSignUp, btngSignIn;

    private final static int RC_SIGN_IN = 123;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseUser user;
    private Gson gson = new Gson();
    private String message;
    private String messageFail;

    SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private ProgressBar prgLogin;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Sign up");
        }

        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtEmailConfirm = findViewById(R.id.edtEmailAgain);
        edtPassword = findViewById(R.id.edtPassword);
        prgLogin = findViewById(R.id.prgLogin);

        btnSignUp = findViewById(R.id.btnSignup); btnSignUp.setOnClickListener(this);
        btngSignIn = findViewById(R.id.gSignIn); btngSignIn.setOnClickListener(this);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        progressDialog = new ProgressDialog(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(SignupActivity.this, gso);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSignup:
                if(!Reusable.getNetworkAvailability(getApplicationContext())){
                    Snackbar.make(edtEmail, "No Internet connection", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                registerUserWithEmail();
                break;
            case R.id.gSignIn:
                btngSignIn.setEnabled(false);
                btnSignUp.setEnabled(false);
                edtPassword.setEnabled(false);
                edtEmail.setEnabled(false);
                edtFirstName.setEnabled(false);
                edtLastName.setEnabled(false);
                edtEmailConfirm.setEnabled(false);
                prgLogin.setVisibility(View.VISIBLE);
                signInWithGoogle();
                break;
        }
    }

    private void signInWithGoogle() {
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN ){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authWithGoogle(account);
            } catch (ApiException e) {
                btngSignIn.setEnabled(true);
                btnSignUp.setEnabled(true);
                edtPassword.setEnabled(true);
                edtEmail.setEnabled(true);
                edtFirstName.setEnabled(true);
                edtLastName.setEnabled(true);
                edtEmailConfirm.setEnabled(true);
                prgLogin.setVisibility(View.GONE);
                e.printStackTrace();
                Log.i("SignupActivity", "onActivityResult: account not Retrieved");
            }
        }
    }

    public void authWithGoogle(GoogleSignInAccount account){
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseUtils.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.i("Signup", "authWithGoogle: successful");
                        user = FirebaseUtils.getAuth().getCurrentUser();
                        final String userId = user.getUid();
                        final DocumentReference ref = FirebaseFirestore.getInstance().collection(USERS_PATH).document(userId);

                        ref.get().addOnCompleteListener(SignupActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> taskUser) {

                                if(taskUser.isSuccessful()){
                                    if(taskUser.getResult()!=null && taskUser.getResult().exists()){
                                        Snackbar.make(edtEmail, "LOGIN SUCCESSFUL", Snackbar.LENGTH_LONG).show();
                                        Profile profile = taskUser.getResult().toObject(Profile.class);
                                        String profileJson = gson.toJson(profile);
                                        editor.putString(USER_PROFILE, profileJson);
                                        editor.putBoolean(IS_ADMIN, profile.isAdmin());
                                        editor.apply();
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                    else{
                                        String displayName = user.getDisplayName();
                                        String[] names = displayName.split(" ");
                                        String firstName = names[0];
                                        String lastName = names[1];
                                        String email = user.getEmail();
                                        ref.set(new Profile(firstName, lastName, email, userId))
                                                .addOnCompleteListener(task1 -> saveData(true));

                                        //Reusable.grabImage(user.getPhotoUrl().toString());
                                        //completeProfile();
                                    }

                                }
                                else{
                                    Log.i("Signup", "OnCompleteListener: " + taskUser.getException().getMessage());
                                }
                            }
                        })
                                .addOnFailureListener(SignupActivity.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    else{
                        btngSignIn.setEnabled(true);
                        btnSignUp.setEnabled(false);
                        edtPassword.setEnabled(true);
                        edtEmail.setEnabled(true);
                        edtFirstName.setEnabled(true);
                        edtLastName.setEnabled(true);
                        edtEmailConfirm.setEnabled(true);
                        prgLogin.setVisibility(View.GONE);
                        message = "Login unsuccessful. " + task.getException().getMessage();
                        Log.i("Signup", "authWithGoogle: "+ message);
                        FirebaseUtils.getAuth().signOut();
                        mGoogleSignInClient.signOut();
                        Snackbar.make(edtEmail, message, Snackbar.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e -> {
                    btngSignIn.setEnabled(true);
                    btnSignUp.setEnabled(false);
                    edtPassword.setEnabled(true);
                    edtEmail.setEnabled(true);
                    edtFirstName.setEnabled(true);
                    edtLastName.setEnabled(true);
                    edtEmailConfirm.setEnabled(true);
                    prgLogin.setVisibility(View.GONE);
                    messageFail = "Login unsuccessful. " + e.getMessage();
                    Log.i("Signup", "authWithGoogleFail: "+ messageFail);
                });
    }

    private void registerUserWithEmail(){
        String firstName = edtFirstName.getText().toString();
        String lastName = edtLastName.getText().toString();
        String email = edtEmail.getText().toString();
        String confirmEmail = edtEmailConfirm.getText().toString();
        String password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(firstName)) {
            edtFirstName.setError("Enter first name");
            return;}

        if(firstName.length() < 3) {
            edtFirstName.setError("First name too short");
            return;}

        if (TextUtils.isEmpty(lastName)) {
            edtLastName.setError("Enter last name");
            return;}

        if(lastName.length() < 3) {
            edtFirstName.setError("Last name too short");
            return;}

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Enter email");
            return;}

        if (TextUtils.isEmpty(confirmEmail)) {
            edtEmail.setError("Confirm email");
            return;}

        if(!email.equals(confirmEmail)){
            edtEmail.setError("Email doesn't match");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Enter password");
            return;
        }

        if (password.length() < 5) {
            edtPassword.setError("password too small");
            return;
        }

        progressDialog.setCancelable(false);
        progressDialog.setMessage("Registering...");
        progressDialog.show();

        FirebaseUtils.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        editor.putString("PASSWORD", password);
                        editor.putString("EMAIL", email);
                        editor.apply();
                        Snackbar.make(edtEmail, "Registration successful.", Snackbar.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                        user = FirebaseUtils.getAuth().getCurrentUser();
                        userId = user.getUid();
                        FirebaseUtils.getDatabase().collection(USERS_PATH).document(userId)
                                .set(new Profile(firstName, lastName, email, userId))
                                .addOnCompleteListener(task1 -> saveData(true));
                    }
                    else
                        Snackbar.make(edtEmail, "Registration failed. Check your details.", Snackbar.LENGTH_LONG).show();
                });

    }

    private void openDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_congrats, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        Button btnContinue = dialog.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, FormFillActivity.class));
            setResult(RESULT_OK);
            dialog.dismiss();
            finish();
        });
    }

    private void saveData(boolean openDialog) {
        FirebaseUtils.getDatabase().collection(USERS_PATH).document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()||(task.getResult()==null))
                        return;
                    Profile profile = task.getResult().toObject(Profile.class);
                    String profileJson = gson.toJson(profile);
                    editor.putString(USER_PROFILE, profileJson);
                    editor.putBoolean(IS_ADMIN, profile.isAdmin());
                    editor.apply();
                    if(openDialog)
                        openDialog();
                    else
                        finish();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
