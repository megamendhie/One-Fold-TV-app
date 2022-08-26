package com.solojet.onefoldtv;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.ContentAdapter;
import config.FirebaseUtils;
import models.Video;
import views.LikeButton;

import static config.Config.YOUTUBE_API_KEY;
import static models.ConstantVariables.ALL;
import static models.ConstantVariables.FROM_MAIN;
import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.LIKES;
import static models.ConstantVariables.LIKES_COUNT;
import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.TYPE;
import static models.ConstantVariables.VIDEO;
import static models.ConstantVariables.VIDEO_PATH;
import static views.LikeButton.LIKED;
import static views.LikeButton.NOT_LIKED;

public class ContentActivity extends YouTubeBaseActivity implements ContentAdapter.ItemClickListener, YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView youTubeView;
    private static final int RECOVERY_REQUEST = 1;
    private TextView txtTitle, txtMore, txtDescription, txtLikes;
    private LikeButton imgLike;
    private String index;
    private String userId;
    private YouTubePlayer player;
    private boolean instantPlay;
    private String type;
    private boolean isAdmin;
    private SharedPreferences prefs;
    private Video model;
    private boolean fromMain;

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseUtils.getAuth().getCurrentUser()!=null)
            userId = FirebaseUtils.getAuth().getCurrentUser().getUid();
        isAdmin = prefs.getBoolean(IS_ADMIN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        youTubeView = findViewById(R.id.playerMain);
        RecyclerView lstVideos = findViewById(R.id.lstVideos);
        txtTitle = findViewById(R.id.txtTitle);
        txtMore = findViewById(R.id.txtMore);
        txtDescription = findViewById(R.id.txtDes);
        txtLikes = findViewById(R.id.txtLikes);
        imgLike = findViewById(R.id.imgLikes);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isAdmin = prefs.getBoolean(IS_ADMIN, false);

        lstVideos.setLayoutManager(new LinearLayoutManager(this));
        youTubeView.initialize(YOUTUBE_API_KEY, this);

        FloatingActionButton fab = findViewById(R.id.fabPost);
        fab.setVisibility(isAdmin? View.VISIBLE:View.GONE);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ContentActivity.this, UploadVideoActivity.class);
            intent.putExtra(STATUS, "new");
            startActivity(intent);

        });

        //get video type
        if(getIntent()!=null) {
            type = getIntent().getStringExtra(TYPE);
            fromMain = getIntent().getBooleanExtra(FROM_MAIN, false);
        }

        CollectionReference ref = FirebaseFirestore.getInstance().collection(VIDEO_PATH);
        Query query;
        if(type==null || type.isEmpty() || type.equals(ALL))
            query = ref.orderBy("time", Query.Direction.DESCENDING);
        else
            query = ref.orderBy("time", Query.Direction.DESCENDING).whereEqualTo(TYPE, type);

        ContentAdapter adapter;
        if(fromMain){
            Video video = getIntent().getParcelableExtra(VIDEO);
            adapter = new ContentAdapter(this, query, isAdmin, false, video.getId());
            lstVideos.setAdapter(adapter);
            adapter.startListening();
            playVideo(video, true);
        }
        else {
            adapter = new ContentAdapter(this, query, isAdmin, true, "");
            lstVideos.setAdapter(adapter);
            adapter.startListening();
        }
    }

    public void showDescription(View v){
        if(txtMore.getText().toString().equals("see more")){
            txtMore.setText("see less");
            txtDescription.setVisibility(View.VISIBLE);
        }
        else{
            txtMore.setText("see more");
            txtDescription.setVisibility(View.GONE);
        }
    }

    public void onLike(View v){
        if(model==null)
            return;
        if(userId==null||userId.isEmpty()){
            Toast.makeText(ContentActivity.this, "You have to login first", Toast.LENGTH_SHORT).show();
            return;
        }
        final String postId = model.getId();
        final DocumentReference postPath =  FirebaseUtils.getDatabase().collection(VIDEO_PATH).document(postId);

        FirebaseUtils.getDatabase().runTransaction((Transaction.Function<Void>) transaction -> {
            Log.i("TAG", "apply: likes entered");
            DocumentSnapshot snapshot = transaction.get(postPath);
            //check if post still exists
            if(!snapshot.exists()){
                Log.i("TAG", "apply: like doesn't exist");
                Toast.makeText(ContentActivity.this, "Video might have been deleted", Toast.LENGTH_SHORT).show();
                return null;
            }

            //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
            long likesCount = snapshot.getLong(LIKES_COUNT);
            List<String> likes = (List) snapshot.get(LIKES);

            Map<String, Object> upd = new HashMap<>();

            if(likes.contains(userId)){
                likesCount -=1;
                likes.remove(userId);
            }
            else{
                likesCount +=1;
                likes.add(userId);
            }


            model.setLikes(likes);
            model.setLikesCount(likesCount);

            final long lks = likesCount;
            runOnUiThread(() -> imgLike.setState(likes.contains(userId)?LIKED:NOT_LIKED));
            runOnUiThread(() -> txtLikes.setText(lks>=1? String.valueOf(model.getLikesCount()): ""));

            upd.put(LIKES_COUNT, likesCount);
            upd.put(LIKES, likes);
            transaction.update(postPath, upd);
            return null;
        })
                .addOnSuccessListener(aVoid -> Log.d("TAG", "Transaction success!"))
                .addOnFailureListener(e -> {
                    Log.w("TAG", "Transaction failure.", e);
                    /*
                    if(model.getLikes().contains(userId)){
                        model.removeLike(userId);
                        model.setLikesCount(model.getLikesCount()-1);
                        runOnUiThread(() -> txtLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0));
                    }
                    else{
                        model.addLike(userId);
                        model.setLikesCount(model.getLikesCount()+1);
                        runOnUiThread(() -> txtLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0));
                    }
                    txtLikes.setText(model.getLikesCount()>=1? String.valueOf(model.getLikesCount()): "");
                    */
                    Snackbar.make(txtLikes, "ERROR: CHECK NETWORK", Snackbar.LENGTH_SHORT).show();

                });
    }

    @Override
    public void onItemClicked(Video model, boolean instantPlay) {
        playVideo(model, instantPlay);
    }

    private void playVideo(@NonNull Video model, boolean instantPlay) {
        txtTitle.setText(model.getTitle());
        txtDescription.setText(model.getDes());
        imgLike.setState(model.getLikes().contains(userId)?LIKED:NOT_LIKED);
        txtLikes.setText(model.getLikesCount()>=1? String.valueOf(model.getLikesCount()): "");

        this.model = model;
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