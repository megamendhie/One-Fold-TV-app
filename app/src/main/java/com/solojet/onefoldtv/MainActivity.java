package com.solojet.onefoldtv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;

import static models.ConstantVariables.ALL;
import static models.ConstantVariables.GIFTED;
import static models.ConstantVariables.PRAISE;
import static models.ConstantVariables.REPORT;
import static models.ConstantVariables.TYPE;
import static models.ConstantVariables.WORD;
import static models.ConstantVariables.YOUTH;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.custom_action_bar);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);


        //initialize DrawerLayout and NavigationView
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
    }

    public void openContent(View view){
        Intent intent = new Intent(this, ContentActivity.class);
        switch (view.getId()){
            case R.id.frmAll:
                intent.putExtra(TYPE, ALL); break;
            case R.id.crdMessages:
                intent.putExtra(TYPE, WORD); break;
            case R.id.crdGifted:
                intent.putExtra(TYPE, GIFTED); break;
            case R.id.crdPraise:
                intent.putExtra(TYPE, PRAISE); break;
            case R.id.crdYouth:
                intent.putExtra(TYPE, YOUTH); break;
            case R.id.crdReport:
                intent.putExtra(TYPE, REPORT); break;
        }
        startActivity(intent);
    }

    public void openClick(View view){
        switch (view.getId()) {
            case R.id.crdEvents:
                startActivity(new Intent(this, EventLandingActivity.class));
                break;
            case R.id.lnrAbout:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawer(GravityCompat.START);
            else
                mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
