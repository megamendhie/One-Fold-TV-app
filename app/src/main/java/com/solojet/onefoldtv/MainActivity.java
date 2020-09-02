package com.solojet.onefoldtv;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar);
    }

    public void openContent(View view){
        switch (view.getId()){
            case R.id.frmAll:
            case R.id.crdMessages:
                startActivity(new Intent(this, ContentActivity.class));
                break;
            case R.id.crdEvents:
                startActivity(new Intent(this, EventLandingActivity.class));
                break;
        }
    }
}
