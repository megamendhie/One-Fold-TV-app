package com.solojet.onefoldtv;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.ContentAdapter;

import static config.Config.YOUTUBE_API_KEY;
import static models.ConstantVariables.ALL;
import static models.ConstantVariables.TYPE;

public class ContentActivity extends YouTubeBaseActivity implements ContentAdapter.ItemClickListener, YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView youTubeView;
    private static final int RECOVERY_REQUEST = 1;
    private TextView txtDescription;
    private String index;
    private YouTubePlayer player;
    private boolean instantPlay;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        youTubeView = findViewById(R.id.playerMain);
        RecyclerView lstVideos = findViewById(R.id.lstVideos);
        txtDescription = findViewById(R.id.txtDes);
        lstVideos.setLayoutManager(new LinearLayoutManager(this));
        youTubeView.initialize(YOUTUBE_API_KEY, this);

        FloatingActionButton fab = findViewById(R.id.fabPost);
        fab.setOnClickListener(view -> startActivity(new Intent(ContentActivity.this, UploadVideoActivity.class)));

        //get video type
        if(getIntent()!=null)
            type = getIntent().getStringExtra(TYPE);

        CollectionReference ref = FirebaseFirestore.getInstance().collection("videos");
        Query query;
        if(type==null || type.isEmpty() || type.equals(ALL))
            query = ref.orderBy("time", Query.Direction.DESCENDING);
        else
            query = ref.orderBy("time", Query.Direction.DESCENDING).whereEqualTo(TYPE, type);

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

    @Override
    public void onItemClicked(String index, boolean instantPlay) {
        this.index = index;
        this.instantPlay = instantPlay;
        if(player!=null)
            player.cueVideo(index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubeView.initialize(YOUTUBE_API_KEY, this);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        player = youTubePlayer;
        player.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(String s) {
                if(instantPlay)
                    player.play();
            }

            @Override
            public void onAdStarted() {

            }

            @Override
            public void onVideoStarted() {

            }

            @Override
            public void onVideoEnded() {

            }

            @Override
            public void onError(YouTubePlayer.ErrorReason errorReason) {

            }
        });
        if(index!=null)
            player.cueVideo(index);
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
}