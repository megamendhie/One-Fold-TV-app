package com.solojet.onefoldtv;

import static config.FirebaseUtils.getAuth;
import static config.FirebaseUtils.getDatabase;
import static models.ConstantVariables.LIVE_VIDEO;
import static models.ConstantVariables.NOTIFICATION;
import static models.ConstantVariables.USER_PROFILE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.gson.Gson;
import com.solojet.onefoldtv.databinding.ActivityNotificationBinding;

import java.util.Date;
import java.util.Objects;

import models.Notification;
import models.Profile;

public class NotificationActivity extends AppCompatActivity {
    ActivityNotificationBinding binding;
    private final Gson gson = new Gson();
    private String username ="";
    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Notification");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = prefs.getString(USER_PROFILE, "");
        Profile profile = (json.equals("")) ? null : gson.fromJson(json, Profile.class);
        if(profile!=null)
            username = profile.getFirstName()+profile.getLastName();

        userId = Objects.requireNonNull(getAuth().getCurrentUser()).getUid();

        binding.btnSend.setOnClickListener(view -> {
            String title = binding.edtTitle.getText().toString();
            String body = binding.edtText.getText().toString();

            if(title.isEmpty()){
                binding.edtTitle.setError("Title is empty");
                return;
            }

            if(body.isEmpty()){
                binding.edtText.setError("Text is empty");
                return;
            }

            long time = new Date().getTime();
            Notification notification = new Notification(title, body, "",  "", time);
            getDatabase().collection(NOTIFICATION).add(notification).addOnSuccessListener(documentReference -> {
                String id = documentReference.getId();
                documentReference.update("id", id);

                binding.edtText.setText("");
                binding.edtTitle.setText("");
            }).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                    Toast.makeText(NotificationActivity.this, "Notification sent", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(NotificationActivity.this, "Notification failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}