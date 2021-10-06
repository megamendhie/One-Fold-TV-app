package com.solojet.onefoldtv;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;

import config.FirebaseUtils;

import static models.ConstantVariables.EKET;
import static models.ConstantVariables.ETINAN;
import static models.ConstantVariables.FIELD;
import static models.ConstantVariables.FIELD_NAME;
import static models.ConstantVariables.IKONO;
import static models.ConstantVariables.IKOT_ABASI;
import static models.ConstantVariables.IKOT_EKPKENE;
import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.ORON;
import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.UYO;

public class EventLandingActivity extends AppCompatActivity implements View.OnClickListener {
    private FloatingActionButton fab;
    private SharedPreferences prefs;

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseUtils.getAuth().getCurrentUser();
        if(user ==null)
            return;

        boolean isAdmin = prefs.getBoolean(IS_ADMIN, false);
        fab.setVisibility(isAdmin ? View.VISIBLE:View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_landing);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Events");
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fab = findViewById(R.id.fabPost);
        fab.setOnClickListener(this);
    }

    public void openEvent(View view) {
        Intent intent = new Intent(this, EventsActivity.class);

        switch (view.getId()){
            case R.id.crdUyo:
                intent.putExtra(FIELD_NAME, "Uyo Field");
                intent.putExtra(FIELD, UYO);
                break;
            case R.id.crdIk:
                intent.putExtra(FIELD_NAME, "Ikot Ekpene Field");
                intent.putExtra(FIELD, IKOT_EKPKENE);
                break;
            case R.id.crdEket:
                intent.putExtra(FIELD_NAME, "Eket Field");
                intent.putExtra(FIELD, EKET);
                break;
            case R.id.crdOron:
                intent.putExtra(FIELD_NAME, "Oron Field");
                intent.putExtra(FIELD, ORON);
                break;
            case R.id.crdIkabasi:
                intent.putExtra(FIELD_NAME, "Ikot Abasi Field");
                intent.putExtra(FIELD, IKOT_ABASI);
                break;
            case R.id.crdEtinan:
                intent.putExtra(FIELD_NAME, "Etinan Field");
                intent.putExtra(FIELD, ETINAN);
                break;
            case R.id.crdIkono:
                intent.putExtra(FIELD_NAME, "Ikono Field");
                intent.putExtra(FIELD, IKONO);
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        if(view == fab){
            Intent intent = new Intent(this, UploadEventActivity.class);
            intent.putExtra(STATUS, "new");
            startActivity(intent);
        }
    }
}
