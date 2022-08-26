package com.solojet.onefoldtv;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;

import adapters.ContentAdapter;
import adapters.ContentProAdapter;
import config.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;
import models.Video;

import static config.FirebaseUtils.getDatabase;
import static models.ConstantVariables.ALL;
import static models.ConstantVariables.CURRENT_USER;
import static models.ConstantVariables.FROM_MAIN;
import static models.ConstantVariables.GIFTED;
import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.LIVE;
import static models.ConstantVariables.LIVE_VIDEO;
import static models.ConstantVariables.PRAISE;
import static models.ConstantVariables.REPORT;
import static models.ConstantVariables.TYPE;
import static models.ConstantVariables.USERS_PATH;
import static models.ConstantVariables.USER_PROFILE;
import static models.ConstantVariables.VIDEO;
import static models.ConstantVariables.VIDEO_PATH;
import static models.ConstantVariables.WORD;
import static models.ConstantVariables.YOUTH;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private DrawerLayout mDrawerLayout;
    private String userId = "";
    private View header;
    private Profile profile;
    private Video video;
    private SharedPreferences.Editor editor;
    private final Gson gson = new Gson();
    private CircleImageView imgDp;
    private LinearLayout lnrLive;
    private TextView txtName, txtLive;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private MenuItem mnuLogout;
    private boolean isAdmin;

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLiveVideo();
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


        //initialize DrawerLayout and NavigationView
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        header = navigationView.getHeaderView(0);

        mnuLogout = navigationView.getMenu().findItem(R.id.nav_logout);

        imgDp = header.findViewById(R.id.imgProfilePic);
        imgDp.setOnClickListener(this);
        lnrLive = findViewById(R.id.lnrLive);
        lnrLive.setOnClickListener(view -> {
            Intent intent = new Intent(this, ContentActivity.class);
            intent.putExtra(TYPE, ALL);
            intent.putExtra(FROM_MAIN, true);
            intent.putExtra(VIDEO, video);
            startActivity(intent);
        });
        txtName = header.findViewById(R.id.txtName);
        txtName.setOnClickListener(this);
        txtLive = findViewById(R.id.txtLive);

        auth = FirebaseUtils.getAuth();
        isAdmin = prefs.getBoolean(IS_ADMIN, false);
        if(auth.getCurrentUser()!= null){
            String json = prefs.getString(USER_PROFILE, "");
            profile = (json.equals("")) ? null : gson.fromJson(json, Profile.class);
            userId = auth.getCurrentUser().getUid();
            setProfile(true);
            if(profile!=null && profile.isAdmin())
                navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);
            else
                navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
        }
        else {
            setProfile(false);
            navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
        }

        loadVideos(isAdmin);
        authStateListener = firebaseAuth -> {
            editor = prefs.edit();
            if(firebaseAuth.getCurrentUser()==null){
                if(navigationView!=null)
                    navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
                userId = "";
                if(isAdmin) loadVideos(false);
                isAdmin = false;
                editor.putString(USER_PROFILE, "");
                editor.putBoolean(IS_ADMIN, isAdmin);
                editor.apply();
                setProfile(false);
            }
            else {
                userId = firebaseAuth.getCurrentUser().getUid();
                FirebaseUtils.getDatabase().collection(USERS_PATH).document(userId).get()
                        .addOnCompleteListener(MainActivity.this, task -> {
                            if(!task.isSuccessful()||(task.getResult()==null)|| !task.getResult().exists())
                                return;
                            profile = task.getResult().toObject(Profile.class);
                            String profileJson = gson.toJson(profile);
                            if(profile.isAdmin()&& !isAdmin) loadVideos(true);
                            isAdmin = profile.isAdmin();
                            editor.putString(USER_PROFILE, profileJson);
                            editor.putBoolean(IS_ADMIN, isAdmin);
                            editor.apply();
                            setProfile(true);
                            if(navigationView!=null && isAdmin)
                                navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);
                        });
            }
        };
    }

    private void checkLiveVideo() {
        DocumentReference ref = getDatabase().collection(LIVE_VIDEO).document(LIVE);
        ref.addSnapshotListener(this, (snapshot, error) -> {
            if (error != null) {
                Log.w("TAGmm", "Listen failed.", error);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d("TAGmm", "Current data: " + snapshot.getData());
                video = snapshot.toObject(Video.class);
                if(video.isActive()){
                    txtLive.setText(video.getTitle());
                    lnrLive.setVisibility(View.VISIBLE);
                }
                else
                    lnrLive.setVisibility(View.GONE);
            } else {
                Log.d("TAGmm", "Current data: null");
                lnrLive.setVisibility(View.GONE);
            }
        });
    }

    private void loadVideos(boolean isAdmin){
        Log.d("MainAct", "loadVideos: ");
        RecyclerView lstVideos = findViewById(R.id.lstVideos);
        lstVideos.setLayoutManager(new LinearLayoutManager(this));
        CollectionReference ref = FirebaseFirestore.getInstance().collection(VIDEO_PATH);
        Query query = ref.orderBy("time", Query.Direction.DESCENDING).limit(25);
        ContentProAdapter adapter = new ContentProAdapter(query, isAdmin);
        lstVideos.setAdapter(adapter);
        adapter.startListening();
    }

    private void setProfile(boolean loggedIn) {
        if(loggedIn && profile!=null){
            mnuLogout.setTitle("Logout");
            txtName.setText(String.format("%s %s", profile.getFirstName(), profile.getLastName()));
            if (profile.getImageUrl().isEmpty())
                Glide.with(this).load(getResources().getDrawable(R.drawable.blank_profile_pic)).into(imgDp);
            else
                Glide.with(this).load(profile.getImageUrl()).into(imgDp);}
        else {
            txtName.setText("Username");
            mnuLogout.setTitle("Login");
            Glide.with(this).load(getResources().getDrawable(R.drawable.blank_profile_pic)).into(imgDp);
        }
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
            case R.id.nav_admin:
                startActivity(new Intent(this, AdminPanelActivity.class));
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
        intent.putExtra(CURRENT_USER, userId);
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
