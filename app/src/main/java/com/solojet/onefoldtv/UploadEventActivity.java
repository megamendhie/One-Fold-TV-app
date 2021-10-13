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
import android.util.Log;
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
import models.Event;
import models.Profile;

import static models.ConstantVariables.EKET;
import static models.ConstantVariables.ETINAN;
import static models.ConstantVariables.EVENT_PATH;
import static models.ConstantVariables.IKONO;
import static models.ConstantVariables.IKOT_ABASI;
import static models.ConstantVariables.IKOT_EKPKENE;
import static models.ConstantVariables.ORON;
import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.USER_PROFILE;
import static models.ConstantVariables.UYO;

public class UploadEventActivity extends AppCompatActivity {
    private EditText edtTitle, edtVenue, edtAbout;
    private String field;
    private String username;
    private TextView txtTime, txtDate;
    private Button btnPost;
    private String date, time;
    private FirebaseUser user;
    private Event model;
    private boolean edited;
    final Calendar cal = Calendar.getInstance();
    private Date eventDate = new Date();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_event);

        Spinner spnField = findViewById(R.id.spnFieldType);

        edtTitle = findViewById(R.id.edtTitle);
        edtVenue = findViewById(R.id.edtVenue);
        edtAbout = findViewById(R.id.edtAbout);

        TextView txtTitle = findViewById(R.id.txtTitle);
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
        if (status.equals("new")) {
            txtTitle.setText("Post New Event");
            date = DateFormat.format("d/M/yyyy", currentDate).toString();
            time = DateFormat.format("H:m", currentDate).toString();
        } else {
            edited = true;
            txtTitle.setText("Edit Event");
            btnPost.setText("Update");
            model = getIntent().getParcelableExtra("model");

            date = DateFormat.format("d/M/yyyy", model.getTimeEvent()).toString();
            time = DateFormat.format("H:m", model.getTimeEvent()).toString();
            cal.setTimeInMillis(model.getTimeEvent());
            edtAbout.setText(model.getAbout());
            edtTitle.setText(model.getTitle());
            edtVenue.setText(model.getVenue());

            spnField.setSelection(Reusable.getField(model.getField()));
        }

        txtDate.setText(getNewDate());
        txtTime.setText(getNewTime());

        spnField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        field = "none";
                        break;
                    case 1:
                        field = UYO;
                        break;
                    case 2:
                        field = IKOT_EKPKENE;
                        break;
                    case 3:
                        field = EKET;
                        break;
                    case 4:
                        field = ORON;
                        break;
                    case 5:
                        field = IKOT_ABASI;
                        break;
                    case 6:
                        field = ETINAN;
                        break;
                    case 7:
                        field = IKONO;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    public void postEvent(View view){
        String about = edtAbout.getText().toString();
        String title = edtTitle.getText().toString();
        String venue = edtVenue.getText().toString();

        if(TextUtils.isEmpty(title)){
            edtTitle.setError("Enter title");
            return;
        }

        if((title.length() <= 7)){
            edtTitle.setError("Title too short");
            return;
        }

        if(field==null || field.isEmpty() || field.equals("none")){
            Snackbar.make(edtTitle, "Select a field", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(venue)){
            edtVenue.setError("Venue");
            return;
        }

        if((venue.length() <= 4)){
            edtVenue.setError("Venue is too short");
            return;
        }
        if(TextUtils.isEmpty(about)){
            edtAbout.setError("Describe the event");
            return;
        }

        if((about.length() <= 10)){
            edtAbout.setError("Description too short");
            return;
        }

        btnPost.setEnabled(false);
        CollectionReference ref = FirebaseUtils.getDatabase().collection(EVENT_PATH);

        String userId="";
        if(user!=null)
            userId = user.getUid();

        if(edited){
            model.setTitle(title);
            model.setAbout(about);
            model.setField(field);
            model.setVenue(venue);
            model.setTimeEvent(getTimeStamp());

            ref.document(model.getId()).set(model, SetOptions.merge())
                    .addOnCompleteListener(task -> {
                        btnPost.setEnabled(true);
                        if(task.isSuccessful())
                            Snackbar.make(edtTitle, "Update successful", Snackbar.LENGTH_SHORT).show();
                    });
            return;
        }

        Event event = new Event(field, title, venue, about, username, userId,  getTimeStamp());

        ref.add(event).addOnSuccessListener(documentReference -> {
            String id = documentReference.getId();
            documentReference.update("id", id);

            edtAbout.setText("");
            edtTitle.setText("");
            edtVenue.setText("");

            Snackbar.make(edtTitle, "Event Posted", Snackbar.LENGTH_SHORT).show();

        })
                .addOnFailureListener(e -> Snackbar.make(edtTitle, "Error occurred", Snackbar.LENGTH_SHORT).show());
    }

    public void setDate(View view){
        int mYear = cal.get(Calendar.YEAR);
        int mMonth = cal.get(Calendar.MONTH);
        int mDay = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            date = String.format("%s/%s/%s", day, (month+1), year);
            cal.setTimeInMillis(getTimeStamp());
            txtDate.setText(getNewDate());
        },
                mYear, mMonth, mDay);
        datePickerDialog.show();

    }

    public void setTime(View view){
        int mHour = cal.get(Calendar.HOUR_OF_DAY);
        int mMinute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (timePicker1, hour, min) -> {
            time = String.format("%s:%s", hour, min);
            cal.setTimeInMillis(getTimeStamp());
            txtTime.setText(getNewTime());
        },
                mHour, mMinute, false);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

}
