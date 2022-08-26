package com.solojet.onefoldtv;

import static models.ConstantVariables.STATUS;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.solojet.onefoldtv.databinding.ActivityAdminPanelBinding;

public class AdminPanelActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAdminPanelBinding binding;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminPanelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        binding.crdVideo.setOnClickListener(this);
        binding.crdLivestream.setOnClickListener(this);
        binding.crdEvent.setOnClickListener(this);
        binding.crdNotification.setOnClickListener(this);
        binding.crdAdmin.setOnClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.crdVideo:
                intent = new Intent(this, UploadVideoActivity.class);
                intent.putExtra(STATUS, "new");
                startActivity(intent);
                break;
            case R.id.crdLivestream:
                startActivity(new Intent(this, UploadLiveActivity.class));
                break;
            case R.id.crdEvent:
                intent = new Intent(this, UploadEventActivity.class);
                intent.putExtra(STATUS, "new");
                startActivity(intent);
                break;
            case R.id.crdNotification:
                startActivity(new Intent(this, NotificationActivity.class));
                break;
            case R.id.crdAdmin:
                //startActivity(new Intent(this, UploadVideoActivity.class));
                break;
        }
    }
}