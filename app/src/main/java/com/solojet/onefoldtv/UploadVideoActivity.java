package com.solojet.onefoldtv;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import config.FirebaseUtils;
import config.Reusable;
import models.Profile;
import models.Video;

import static models.ConstantVariables.ALL;
import static models.ConstantVariables.GIFTED;
import static models.ConstantVariables.PRAISE;
import static models.ConstantVariables.REPORT;
import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.USER_PROFILE;
import static models.ConstantVariables.VIDEO_PATH;
import static models.ConstantVariables.WORD;
import static models.ConstantVariables.YOUTH;

public class UploadVideoActivity extends AppCompatActivity {
    private EditText edtTitle, edtVideoId, edtDes;
    private Button btnPost;
    private String type;
    private String username;
    private TextView txtTime, txtDate;
    private String date, time;
    private FirebaseUser user;
    private Video model;
    private boolean edited;
    private final Calendar cal = Calendar.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        Spinner spnType = findViewById(R.id.spnVideoType);

        edtTitle = findViewById(R.id.edtTitle);
        edtVideoId = findViewById(R.id.edtId);
        edtDes = findViewById(R.id.edtDes);

        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtTitleSub = findViewById(R.id.txtTitleSub);
        txtDate = findViewById(R.id.txtDate);
        txtTime = findViewById(R.id.txtTime);

        btnPost = findViewById(R.id.btnPost);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        String status = getIntent().getStringExtra(STATUS);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = prefs.getString(USER_PROFILE, "");
        Profile profile = (json.equals("")) ? null : gson.fromJson(json, Profile.class);
        if(profile!=null)
            username = profile.getFirstName()+profile.getLastName();

        user = FirebaseUtils.getAuth().getCurrentUser();
        long currentDate = new Date().getTime();

        assert status != null;
        if(status.equals("new")){
            txtTitle.setText("Post Video");
            txtTitleSub.setText("Upload a new video");
            date = DateFormat.format("d/M/yyyy", currentDate).toString();
            time = DateFormat.format("H:m", currentDate).toString();
        }
        else{
            edited = true;
            txtTitle.setText("Edit Video");
            txtTitleSub.setText("Edit an existing video");
            btnPost.setText("Update");
            model = getIntent().getParcelableExtra("model");

            date = DateFormat.format("d/M/yyyy", model.getTime()).toString();
            time = DateFormat.format("H:m", model.getTime()).toString();
            cal.setTimeInMillis(model.getTime());
            edtDes.setText(model.getDes());
            edtTitle.setText(model.getTitle());
            edtVideoId.setText(model.getUrlIndex());

            spnType.setSelection(Reusable.getType(model.getType()));
        }

        txtDate.setText(getNewDate());
        txtTime.setText(getNewTime());

        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        type = "none";
                        break;
                    case 1:
                        type = ALL;
                        break;
                    case 2:
                        type = WORD;
                        break;
                    case 3:
                        type = PRAISE;
                        break;
                    case 4:
                        type = YOUTH;
                        break;
                    case 5:
                        type = GIFTED;
                        break;
                    case 6:
                        type = REPORT;
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
        String des = edtDes.getText().toString();
        String title = edtTitle.getText().toString();
        String videoId = edtVideoId.getText().toString();

        if(TextUtils.isEmpty(title)){
            edtTitle.setError("Enter title");
            return;
        }

        if((title.length() <= 7)){
            edtTitle.setError("Title too short");
            return;
        }

        if(type==null || type.isEmpty() || type.equals("none")){
            Snackbar.make(edtDes, "Select video segment", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(videoId)){
            edtVideoId.setError("Video Id");
            return;
        }

        if((videoId.length() <= 4)){
            edtVideoId.setError("Video Id too short");
            return;
        }
        if(TextUtils.isEmpty(des)){
            edtDes.setError("Enter description");
            return;
        }

        if((des.length() <= 10)){
            edtDes.setError("Description too short");
            return;
        }

        btnPost.setEnabled(false);
        CollectionReference ref = FirebaseUtils.getDatabase().collection(VIDEO_PATH);

        String userId="";
        if(user!=null)
            userId = user.getUid();

        if(edited){
            model.setTitle(title);
            model.setDes(des);
            model.setType(type);
            model.setUrlIndex(videoId);
            model.setTime(getTimeStamp());

            ref.document(model.getId()).set(model, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        btnPost.setEnabled(true);
                        if(task.isSuccessful())
                            Snackbar.make(edtDes, "Update successful", Snackbar.LENGTH_SHORT).show();
                    });
            return;
        }

        Video video = new Video(title, des, videoId, type, username, userId, getTimeStamp());

        ref.add(video).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            documentReference.update("id", id);

            edtDes.setText("");
            edtTitle.setText("");
            edtVideoId.setText("");

            Snackbar.make(edtDes, "Video Posted", Snackbar.LENGTH_SHORT).show();

        })
        .addOnFailureListener(e -> Snackbar.make(edtDes, "Error occurred", Snackbar.LENGTH_SHORT).show());
    }

    public void setDate(View view){
        int mYear = cal.get(Calendar.YEAR);
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            date = String.format("%s/%s/%s", day, (month+1), year);
            cal.setTimeInMillis(getTimeStamp());
            txtDate.setText(getNewDate());
        }, mYear, mMonth, mDay);
        datePickerDialog.show();


    }

    public void setTime(View view){
        int mHour = cal.get(Calendar.HOUR_OF_DAY);
        int mMinute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (timePicker1, hour, min) -> {
            time = String.format("%s:%s", hour, min);
            cal.setTimeInMillis(getTimeStamp());
            txtTime.setText(getNewTime());
        }, mHour, mMinute, false);
        timePicker.show();
    }

    private String getNewDate(){
        try {
            Date oldDate= new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).parse(date);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getNewTime(){
        try {
            Date oldTime= new SimpleDateFormat("H:m", Locale.getDefault()).parse(time);
            return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(oldTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private long getTimeStamp(){
        try {
            Date newDate = new SimpleDateFormat("d/M/yyyy H:m", Locale.getDefault())
                    .parse(String.format("%s %s",date, time));
            return newDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
