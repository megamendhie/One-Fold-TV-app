package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.solojet.onefoldtv.R;

import config.FirebaseUtils;
import models.Video;

public class ContentAdapter extends FirestoreRecyclerAdapter<Video, ContentAdapter.ContentViewHolder> {
    private ItemClickListener clickListener;
    private TextView txtCurrentlyPlaying;
    private boolean mediaLoaded = false;

    public interface ItemClickListener {
        void onItemClicked (Video model, boolean instantPlay);
    }

    public ContentAdapter (ItemClickListener clickListener, Query query){
        super(new FirestoreRecyclerOptions.Builder<Video>()
                .setQuery(query, Video.class)
                .build());
        this.clickListener = clickListener;
    }

    private void playFirst(TextView txtTitle, Video model){
        txtTitle.setTextColor(txtTitle.getContext().getResources().getColor(R.color.colorPrimary));
        this.txtCurrentlyPlaying = txtTitle;
        clickListener.onItemClicked(model, false);
        mediaLoaded = true;
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent,false);
        return new ContentViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContentViewHolder holder, int position, @NonNull Video model) {
        String thumbnailHq = "https://img.youtube.com/vi/"+model.getUrlIndex()+"/hqdefault.jpg";

        holder.bindView(clickListener, model);
        Glide.with(holder.imgThumbnail).load(thumbnailHq).into(holder.imgThumbnail);
        if(!mediaLoaded && position==0)
            playFirst(holder.txtTitle, model);

    }

    class ContentViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgThumbnail;
        private TextView txtTitle;
        private TextView txtLikes;

        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtLikes = itemView.findViewById(R.id.txtLikes);
        }

        void bindView(ItemClickListener listener, Video model){
            String userId;
            txtTitle.setText(model.getTitle());
            txtLikes.setText(model.getLikesCount()>=1? String.valueOf(model.getLikesCount()): "");

            if(FirebaseUtils.getAuth().getCurrentUser()!=null){
                userId = FirebaseUtils.getAuth().getCurrentUser().getUid();

            if(model.getLikes().contains(userId))
                txtLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_24dp, 0, 0, 0);
            else
                txtLikes.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);

            }
            itemView.setOnClickListener(view -> {
                if(txtCurrentlyPlaying!=null)
                    txtCurrentlyPlaying.setTextColor(txtCurrentlyPlaying.getContext().getResources()
                            .getColor(R.color.colorBlack));
                txtTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                mediaLoaded = true;
                txtCurrentlyPlaying = txtTitle;
                listener.onItemClicked(model, true);
            });
        }
    }
}

