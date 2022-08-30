package com.solojet.onefoldtv;

import static config.FirebaseUtils.getAuth;
import static config.FirebaseUtils.getDatabase;
import static models.ConstantVariables.LIVE;
import static models.ConstantVariables.LIVE_VIDEO;
import static models.ConstantVariables.USER_PROFILE;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.solojet.onefoldtv.databinding.ActivityUploadLiveBinding;

import java.util.Date;
import java.util.Objects;

import models.Profile;
import models.Video;

public class UploadLiveActivity extends AppCompatActivity {
    private String TAG = "upld";
    private ActivityUploadLiveBinding binding;
    private Video video = null;
    private final Gson gson = new Gson();
    private String username ="";
    private String userId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadLiveBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Livestream");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = prefs.getString(USER_PROFILE, "");
        Profile profile = (json.equals("")) ? null : gson.fromJson(json, Profile.class);
        if(profile!=null)
            username = profile.getFirstName()+profile.getLastName();

        userId = Objects.requireNonNull(getAuth().getCurrentUser()).getUid();
        DocumentReference ref = getDatabase().collection(LIVE_VIDEO).document(LIVE);
        ref.addSnapshotListener(this, (snapshot, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());
                video = snapshot.toObject(Video.class);
                binding.txtTitle.setText(video.getTitle());
                binding.txtDes.setText(video.getDes());
                binding.swtActive.setChecked(video.isActive());
                binding.btnAdd.setVisibility(View.GONE);

            } else {
                Log.d(TAG, "Current data: null");
                video = null;
                setEmptyVideo();
            }
        });

        binding.swtActive.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            updateActiveState(isChecked);
        });
        binding.imgDelete.setOnClickListener(view1 -> deleteLiveStream());
        binding.btnAdd.setOnClickListener(view1 -> showAddVideoPanel());
        binding.btnPost.setOnClickListener(view1 -> postVideo());
        binding.txtCancel.setOnClickListener(view1 -> {
            binding.crdAddVideo.setVisibility(View.GONE);
            binding.btnAdd.setVisibility(video==null?View.VISIBLE:View.GONE);
        });
        binding.crdLive.setOnLongClickListener(view12 -> {
            if(video!=null){
                binding.edtTitle.setText(video.getTitle());
                binding.edtDes.setText(video.getDes());
                binding.edtId.setText(video.getUrlIndex());
                binding.crdAddVideo.setVisibility(View.VISIBLE);
            }
            return false;
        });
    }

    private void postVideo() {
        String title = binding.edtTitle.getText().toString();
        String des = binding.edtDes.getText().toString();
        String id = binding.edtId.getText().toString();

        if(title.isEmpty()){
            binding.edtTitle.setError("Enter Title");
            return;
        } if(title.length() < 4){
            binding.edtTitle.setError("Title too short");
            return;
        }
        if(id.isEmpty()){
            binding.edtId.setError("Enter Video Id");
            return;
        }
        if(id.length() <= 4){
            binding.edtId.setError("Video Id too short");
            return;
        }

        Video video = new Video(title, des, id, username, userId, new Date().getTime());
        getDatabase().collection(LIVE_VIDEO).document(LIVE).set(video).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(this, "Video uploaded", Toast.LENGTH_SHORT).show();
                clearScreen();
            }
            else
                Toast.makeText(this, "Video upload unsuccessful", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearScreen() {
        binding.edtTitle.setText("");
        binding.edtDes.setText("");
        binding.edtId.setText("");
        binding.crdAddVideo.setVisibility(View.GONE);
    }

    private void showAddVideoPanel() {
        binding.btnAdd.setVisibility(View.GONE);
        binding.crdAddVideo.setVisibility(View.VISIBLE);
    }

    private void setEmptyVideo() {
        binding.txtTitle.setText("No live video now");
        binding.txtDes.setText("");
        binding.btnAdd.setVisibility(View.VISIBLE);
        binding.crdAddVideo.setVisibility(View.GONE);
        binding.swtActive.setChecked(false);
    }

    private void deleteLiveStream() {
        if(video==null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this video?")
                .setPositiveButton("Yes", (dialogInterface, i) -> delete())
                .setNegativeButton("No", (dialogInterface, i) -> {});
        builder.show();
    }

    private void delete() {
        getDatabase().collection(LIVE_VIDEO).document(LIVE).delete().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                video=null;
                setEmptyVideo();
                Toast.makeText(this, "Video deleted", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "Video delete unsuccessful", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateActiveState(boolean isChecked) {
        if(video==null || (isChecked==video.isActive()))
            return;

        video.setActive(isChecked);
        getDatabase().collection(LIVE_VIDEO).document(LIVE).set(video, SetOptions.merge()).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(isChecked)
                    Toast.makeText(UploadLiveActivity.this,
                            "Livestream activated", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(UploadLiveActivity.this,
                            "Livestream deactivated", Toast.LENGTH_SHORT).show();
            }
            else{if(isChecked)
                Toast.makeText(UploadLiveActivity.this,
                        "Livestream deactivation unsuccessful", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(UploadLiveActivity.this,
                        "Livestream activation unsuccessful", Toast.LENGTH_SHORT).show();
                video.setActive(!isChecked);
                binding.swtActive.setChecked(!isChecked);
            }
        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: "+ e.toString()));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}