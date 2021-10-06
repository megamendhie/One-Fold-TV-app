package com.solojet.onefoldtv;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import config.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

import static models.ConstantVariables.CURRENT_USER;

public class ProfileActivity extends AppCompatActivity {
    private String myId, userId;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(String.format(""));
        }

        CircleImageView imgDp = findViewById(R.id.imgDp);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtBio = findViewById(R.id.txtBio);
        TextView txtAssembly = findViewById(R.id.txtAssembly);
        TextView txtDistrict = findViewById(R.id.txtDistrict);
        TextView txtField = findViewById(R.id.txtField);
        TextView txtPhone = findViewById(R.id.txtPhone);
        TextView txtCountry = findViewById(R.id.txtCountry);

        userId = getIntent().getStringExtra(CURRENT_USER);
        FirebaseUser user = FirebaseUtils.getAuth().getCurrentUser();

        if(user !=null)
            myId = user.getUid();

        FirebaseUtils.getDatabase().collection("users").document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if(snapshot==null||!snapshot.exists())
                        return;

                    Profile profile = snapshot.toObject(Profile.class);
                    if(profile!=null) {
                        actionBar.setTitle(String.format("%s %s", profile.getFirstName(), profile.getLastName()));
                        txtName.setText(String.format("%s %s", profile.getFirstName(), profile.getLastName()));
                        txtBio.setText(profile.getBio());
                        txtAssembly.setText(profile.getAssembly());
                        txtDistrict.setText(profile.getDistrict());
                        txtField.setText(profile.getField());
                        txtPhone.setText(profile.getPhone());
                        txtCountry.setText(profile.getCountry());

                        if (profile.getBio().isEmpty())
                            txtBio.setVisibility(View.GONE);
                        else {
                            txtBio.setVisibility(View.VISIBLE);
                            txtBio.setText(profile.getBio());
                        }

                        if(profile.getImageUrl().isEmpty())
                            Glide.with(this).load(getResources().getDrawable(R.drawable.blank_profile_pic)).into(imgDp);
                        else
                            Glide.with(this).load(profile.getImageUrl()).into(imgDp);
                    }
                });
    }

    public void openForm(View v){
        Intent intent = new Intent(this, FormFillActivity.class);
        intent.putExtra("caller", "profileActivity");
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
