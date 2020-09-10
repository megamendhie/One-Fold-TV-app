package com.solojet.onefoldtv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import models.Video;

public class UploadVideoActivity extends AppCompatActivity {
    private CollectionReference ref;
    private EditText edtTitle, edtVideoId, edtDes;
    private Spinner spnType;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        spnType = findViewById(R.id.spnVideoType);

        edtTitle = findViewById(R.id.edtTitle);
        edtVideoId = findViewById(R.id.edtVideoId);
        edtDes = findViewById(R.id.edtDes);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        type = "none";
                        break;
                    case 1:
                        type = "all";
                        break;
                    case 2:
                        type = "word";
                        break;
                    case 3:
                        type = "praise";
                        break;
                    case 4:
                        type = "youth";
                        break;
                    case 5:
                        type = "gifted";
                        break;
                    case 6:
                        type = "report";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void postVideo(View view){
        if(type==null || type.isEmpty() || type.equals("none")){
            Snackbar.make(edtDes, "Select video type", Snackbar.LENGTH_SHORT).show();
            return;
        }

        ref = FirebaseFirestore.getInstance().collection("videos");
        String des = edtDes.getText().toString();
        String title = edtTitle.getText().toString();
        String videoId = edtVideoId.getText().toString();

        Video video = new Video(title, des, videoId, type);
        ref.add(video).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            documentReference.update("id", id);

            edtDes.setText("");
            edtTitle.setText("");
            edtVideoId.setText("");

            Snackbar.make(edtDes, "Posted", Snackbar.LENGTH_SHORT).show();
            Toast.makeText(this, "Posted", Toast.LENGTH_SHORT).show();

        })
        .addOnFailureListener(e -> Snackbar.make(edtDes, "Error occurred", Snackbar.LENGTH_SHORT).show());
    }
}
