package com.solojet.onefoldtv;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.ContentAdapter;

import static config.Config.YOUTUBE_API_KEY;

public class ContentActivity extends YouTubeBaseActivity implements ContentAdapter.ItemClickListener {

    private YouTubePlayerView youTubeView;
    private static final int RECOVERY_REQUEST = 1;
    private TextView txtDescription;
    private String index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        youTubeView = findViewById(R.id.playerMain);
        RecyclerView lstVideos = findViewById(R.id.lstVideos);
        txtDescription = findViewById(R.id.txtDes);
        lstVideos.setLayoutManager(new LinearLayoutManager(this));

        CollectionReference ref = FirebaseFirestore.getInstance().collection("videos");
        Query query = ref.orderBy("time", Query.Direction.DESCENDING);

        ContentAdapter adapter = new ContentAdapter(this, query);
        lstVideos.setAdapter(adapter);
        adapter.startListening();
    }

    public void showDescription(View v){
        if(txtDescription.getVisibility() == View.VISIBLE)
            txtDescription.setVisibility(View.GONE);
        else
            txtDescription.setVisibility(View.VISIBLE);
    }

    private void playVideo(){
        youTubeView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRstored) {
                if(!wasRstored)
                    youTubePlayer.cueVideo(index);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult initializationResult) {
                if (initializationResult.isUserRecoverableError()) {
                    initializationResult.getErrorDialog(ContentActivity.this, RECOVERY_REQUEST).show();
                } else {
                    String error = String.format(getString(R.string.player_error), initializationResult.toString());
                    Toast.makeText(ContentActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onItemClicked(String index) {
        setIndex(index);
        playVideo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            playVideo();
        }
    }

    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
