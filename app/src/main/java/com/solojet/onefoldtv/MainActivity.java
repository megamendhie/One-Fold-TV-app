package com.solojet.onefoldtv;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import config.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

import static models.ConstantVariables.ALL;
import static models.ConstantVariables.CURRENT_USER;
import static models.ConstantVariables.GIFTED;
import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.PRAISE;
import static models.ConstantVariables.REPORT;
import static models.ConstantVariables.TYPE;
import static models.ConstantVariables.USERS_PATH;
import static models.ConstantVariables.USER_PROFILE;
import static models.ConstantVariables.WORD;
import static models.ConstantVariables.YOUTH;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private DrawerLayout mDrawerLayout;
    private String myId = "";
    private View header;
    private Profile profile;
    private SharedPreferences.Editor editor;
    private final Gson gson = new Gson();
    private CircleImageView imgDp;
    private TextView txtName;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private MenuItem mnuLogout;

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();


        //initialize DrawerLayout and NavigationView
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);

        mnuLogout = navigationView.getMenu().findItem(R.id.nav_logout);

        imgDp = header.findViewById(R.id.imgProfilePic);
        imgDp.setOnClickListener(this);
        txtName = header.findViewById(R.id.txtName);
        txtName.setOnClickListener(this);

        auth = FirebaseUtils.getAuth();
        if(auth.getCurrentUser()!= null){
            String json = prefs.getString(USER_PROFILE, "");
            profile = (json.equals("")) ? null : gson.fromJson(json, Profile.class);
            myId = auth.getCurrentUser().getUid();
            setProfile();
            mnuLogout.setTitle("Logout");
            FirebaseUtils.getDatabase().collection(USERS_PATH).document(myId).get()
                    .addOnCompleteListener(task -> {
                        if(!task.isSuccessful()||(task.getResult()==null)|| !task.getResult().exists())
                            return;
                        profile = task.getResult().toObject(Profile.class);
                        String profileJson = gson.toJson(profile);
                        editor.putString(USER_PROFILE, profileJson);
                        editor.putBoolean(IS_ADMIN, profile.isAdmin());
                        editor.apply();
                        setProfile();
                    });
        }
        else
            mnuLogout.setTitle("Login");

        authStateListener = firebaseAuth -> {
            if(firebaseAuth.getCurrentUser()==null){
            myId = "";
            editor.putString(USER_PROFILE, "");
            editor.putBoolean(IS_ADMIN, false);
            editor.apply();
            txtName.setText("Username");
            mnuLogout.setTitle("Login");
            }
            else {
                myId = firebaseAuth.getCurrentUser().getUid();
                setProfile();
                mnuLogout.setTitle("Logout");
            }
        };
    }

    private void setProfile() {
        if(profile==null)
            return;
        txtName.setText(String.format("%s %s", profile.getFirstName(), profile.getLastName()));
        if(profile.getImageUrl().isEmpty())
            Glide.with(this).load(getResources().getDrawable(R.drawable.blank_profile_pic)).into(imgDp);
        else
            Glide.with(this).load(profile.getImageUrl()).into(imgDp);
    }

    public void openContent(View view){
        Intent intent = new Intent(this, ContentActivity.class);
        switch (view.getId()){
            case R.id.frmAll:
                intent.putExtra(TYPE, ALL); break;
            case R.id.crdMessages:
                intent.putExtra(TYPE, WORD); break;
            case R.id.crdGifted:
                intent.putExtra(TYPE, GIFTED); break;
            case R.id.crdPraise:
                intent.putExtra(TYPE, PRAISE); break;
            case R.id.crdYouth:
                intent.putExtra(TYPE, YOUTH); break;
            case R.id.crdReport:
                intent.putExtra(TYPE, REPORT); break;
        }
        startActivity(intent);
    }

    public void openClick(View view){
        switch (view.getId()) {
            case R.id.crdEvents:
                startActivity(new Intent(this, EventLandingActivity.class));
                break;
            case R.id.lnrAbout:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.lnrShare:
                shareApp();
                break;
            case R.id.lnrNews:
                Toast.makeText(this, "Coming soon!!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.lnrRate:
                rateApp();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawer(GravityCompat.START);
            else
                mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        switch (id){
            case R.id.nav_profile:
                if(FirebaseUtils.getAuth().getCurrentUser()==null)
                    startActivity(new Intent(this, LoginActivity.class));
                else
                    openMyProfile();
                break;
            case R.id.nav_policy:
                break;
            case R.id.nav_contact:
                startActivity(new Intent(this, ContactActivity.class));
                break;
            case R.id.nav_logout:
                if(auth.getCurrentUser()==null)
                    startActivity(new Intent(this, LoginActivity.class));
                else
                    logOut();
                break;
        }
        return false;
    }

    private void openMyProfile() {
        Intent intent = new Intent(new Intent(this, ProfileActivity.class));
        intent.putExtra(CURRENT_USER, myId);
        startActivity(intent);
    }

    private void logOut(){
        if(FirebaseUtils.getAuth().getCurrentUser()==null)
            Snackbar.make(header,"No active user", Snackbar.LENGTH_SHORT).show();
        else{
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getResources().getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient= GoogleSignIn.getClient(MainActivity.this, gso);

            FirebaseUtils.getAuth().signOut();
            mGoogleSignInClient.signOut();
            Snackbar.make(header, "SIGNED OUT", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void shareApp(){
        String url = "Stream all the videos of One Fold TV and stay up-to-date on the events and programs of The Apostolic Church in Akwa Ibom Territory." +
                "\n\nhttps://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, url);
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivity(Intent.createChooser(share, "Share via:"));
    }

    public void rateApp(){
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    @Override
    public void onClick(View view) {
        if(FirebaseUtils.getAuth().getCurrentUser()==null)
            return;
        openMyProfile();
    }
}
