package com.solojet.onefoldtv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import adapters.EventAdapter;

public class EventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        RecyclerView lstEvents = findViewById(R.id.lstEvents);
        lstEvents.setLayoutManager(new LinearLayoutManager(this));
        lstEvents.setAdapter(new EventAdapter());
    }

}
