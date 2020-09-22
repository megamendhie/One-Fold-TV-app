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
import config.FirebaseUtils;
import models.Video;

import static config.Config.YOUTUBE_API_KEY;
import static models.ConstantVariables.ALL;
import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.TYPE;

public class ContentActivity extends YouTubeBaseActivity implements ContentAdapter.ItemClickListener, YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView youTubeView;
    private static final int RECOVERY_REQUEST = 1;
    private TextView txtTitle, txtDescription, txtLikes;
    private String index;

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseUtils.getAuth().getCurrentUser()!=null)
            userId = FirebaseUtils.getAuth().getCurrentUser().getUid();
    }

    private String userId;
    private YouTubePlayer player;
    private boolean instantPlay;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        youTubeView = findViewById(R.id.playerMain);
        RecyclerView lstVideos = findViewById(R.id.lstVideos);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDes);
        txtLikes = findViewById(R.id.txtLikes);

        lstVideos.setLayoutManager(new LinearLayoutManager(this));
        youTubeView.initialize(YOUTUBE_API_KEY, this);

        FloatingActionButton fab = findViewById(R.id.fabPost);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ContentActivity.this, UploadVideoActivity.class);
            intent.putExtra(STATUS, "new");
            startActivity(intent);

        });

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
    public void onItemClicked(Video model, boolean instantPlay) {
        txtTitle.setText(model.getTitle());
        txtDescription.setText(model.getDes());

        txtLikes.setText(model.getLikesCount()>=1? String.valueOf(model.getLikesCount()): "");

        if(model.getLikes().contains(userId))
            txtLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0);
        else
            txtLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);

        this.index = model.getUrlIndex();
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