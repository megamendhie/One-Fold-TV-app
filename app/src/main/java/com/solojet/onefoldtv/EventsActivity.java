package com.solojet.onefoldtv;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.ContentAdapter;
import adapters.EventAdapter;

import static models.ConstantVariables.ALL;
import static models.ConstantVariables.EVENT_PATH;
import static models.ConstantVariables.FIELD;
import static models.ConstantVariables.FIELD_NAME;
import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.TYPE;
import static models.ConstantVariables.VIDEO_PATH;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Upcoming Events");
        }

        TextView txtField = findViewById(R.id.txtField);
        if(getIntent()!=null)
            txtField.setText(getIntent().getStringExtra(FIELD_NAME));
        openEvent();
    }

    private void openEvent(){
        RecyclerView lstEvents = findViewById(R.id.lstEvents);
        lstEvents.setLayoutManager(new LinearLayoutManager(this));
        String field;

        //get video type
        Intent intent = getIntent();
        if(intent!=null)
            field = getIntent().getStringExtra(FIELD);
        else
            field = "uyo";


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isAdmin = prefs.getBoolean(IS_ADMIN, false);

        CollectionReference ref = FirebaseFirestore.getInstance().collection(EVENT_PATH);
        Query query = ref.orderBy("timeEvent", Query.Direction.DESCENDING).whereEqualTo(FIELD, field);

        EventAdapter adapter = new EventAdapter(query, isAdmin);
        lstEvents.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
