package com.solojet.onefoldtv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import config.FirebaseUtils;
import config.Reusable;
import models.Profile;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int RC_SIGN_IN = 1234;
    private final int RC_SIGN_UP = 5647;
    private EditText edtEmail, edtPassword;
    private ProgressBar prgLogin;
    private ProgressDialog progressDialog;

    private SharedPreferences.Editor editor;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Login");
        }

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        prgLogin = findViewById(R.id.prgLogin);
        progressDialog = new ProgressDialog(this);

        TextView txtForgetPassword = findViewById(R.id.txtForgetPassword); txtForgetPassword.setOnClickListener(this);
        Button btnLogin = findViewById(R.id.btnLogin);  btnLogin.setOnClickListener(this);
        Button btnSignup = findViewById(R.id.btnSignup); btnSignup.setOnClickListener(this);
        Button gSignin = findViewById(R.id.gSignIn); gSignin.setOnClickListener(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        //show email and password from past entry
        if(! sharedPreferences.getString("EMAIL", "xyz9ac5@example.com").equals("xyz9ac5@example.com")){
            edtEmail.setText(sharedPreferences.getString("EMAIL", "xyz9ac5@example.com"));
            edtPassword.setText(sharedPreferences.getString("PASSWORD", "XAEFDgsjs537n&8"));
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
    }

    public void passwordReset(View v){
        /*
        Build a dialogView for user to enter email
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_reset_password, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.show();

        final EditText edtPassResetEmail = dialog.findViewById(R.id.edtEmail);
        final Button btnSendPassword = dialog.findViewById(R.id.btnSendPassword);
        final TextView txtHeading = dialog.findViewById(R.id.txtHeading);
        final ProgressBar progressBar = dialog.findViewById(R.id.prgPasswordReset);
        edtPassResetEmail.setText(edtEmail.getText().toString().trim());

        btnSendPassword.setOnClickListener(view -> {
            if(btnSendPassword.getText().toString().equals("CLOSE")){
                dialog.cancel();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            String email = edtPassResetEmail.getText().toString().trim();
            FirebaseUtils.getAuth().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    edtPassResetEmail.setVisibility(View.GONE);
                    txtHeading.setText("Password link has been sent to your email");
                    btnSendPassword.setText("CLOSE");
                }
                else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error " + task.getException().toString(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.txtForgetPassword:
                passwordReset(edtEmail);
                break;
            case R.id.btnLogin:
                signInWithEmail();
                break;
            case R.id.btnSignup:
                startActivityForResult(new Intent(LoginActivity.this, SignupActivity.class), RC_SIGN_UP);
                break;
            case R.id.gSignIn:
                edtPassword.setEnabled(false);
                edtEmail.setEnabled(false);
                prgLogin.setVisibility(View.VISIBLE);
                signInWithGoogle();
                break;
        }
    }

    private void signInWithEmail(){
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            edtEmail.setError("Enter email");
            return;
        }
        if(TextUtils.isEmpty(password)){
            edtPassword.setError("Enter password");
            return;
        }
        prgLogin.setVisibility(View.VISIBLE);
        progressDialog.setTitle("Signing in...");
        progressDialog.show();
        FirebaseUtils.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        prgLogin.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            editor.putString("PASSWORD", edtPassword.getText().toString().trim());
                            editor.putString("EMAIL", edtEmail.getText().toString().trim());
                            editor.apply();
                            Snackbar.make(edtEmail, "Login successful", Snackbar.LENGTH_SHORT).show();
                            user = FirebaseUtils.getAuth().getCurrentUser();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if(FirebaseUtils.getAuth().getCurrentUser()!=null)
                        FirebaseUtils.getAuth().signOut();
                    progressDialog.dismiss();
                    Snackbar.make(edtEmail, "Login failed. " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
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
                prgLogin.setVisibility(View.GONE);
                edtPassword.setEnabled(true);
                edtEmail.setEnabled(true);
                e.printStackTrace();
                Log.i("LoginActivity", "onActivityResult: account not Retrieved");
            }
        }
        else if(requestCode==RC_SIGN_UP){
            if(resultCode==RESULT_OK)
                finish();
        }
    }

    public void authWithGoogle(GoogleSignInAccount account){
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        FirebaseUtils.getAuth().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        user = FirebaseUtils.getAuth().getCurrentUser();
                        final String userId = user.getUid();
                        final DocumentReference ref = FirebaseUtils.getDatabase()
                                .collection("users").document(userId);

                        ref.get().addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){

                                String displayName = user.getDisplayName();
                                String[] names = displayName.split(" ");
                                String firstName = names[0];
                                String lastName = names[1];
                                String email = user.getEmail();
                                ref.set(new Profile(firstName, lastName, email, userId));

                                //Reusable.grabImage(user.getPhotoUrl().toString());
                                //completeProfile();
                                Snackbar.make(edtEmail, "LOGIN SUCCESSFUL", Snackbar.LENGTH_LONG).show();
                                finish();

                            }
                        });
                    }
                    else{
                        edtPassword.setEnabled(true);
                        edtEmail.setEnabled(true);
                        prgLogin.setVisibility(View.GONE);
                        final String message = "Login unsuccessful. " + task.getException().getMessage();
                        FirebaseUtils.getAuth().signOut();
                        mGoogleSignInClient.signOut();
                        Snackbar.make(edtEmail, message, Snackbar.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e -> {
                    edtPassword.setEnabled(true);
                    edtEmail.setEnabled(true);
                    prgLogin.setVisibility(View.GONE);
                });
    }


}
