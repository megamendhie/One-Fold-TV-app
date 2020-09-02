package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.solojet.onefoldtv.R;

import models.Video;

public class ContentAdapter extends FirestoreRecyclerAdapter<Video, ContentAdapter.ContentViewHolder> {
    private ItemClickListener clickListener;
    private TextView txtCurrentlyPlaying;
    private boolean mediaLoaded = false;

    public interface ItemClickListener {
        void onItemClicked (String urlIndex);
    }

    public ContentAdapter (ItemClickListener clickListener, Query query){
        super(new FirestoreRecyclerOptions.Builder<Video>()
                .setQuery(query, Video.class)
                .build());
        this.clickListener = clickListener;
    }

    private void playFirst(TextView txtTitle, String index){
        txtTitle.setTextColor(txtTitle.getContext().getResources().getColor(R.color.colorPrimary));
        this.txtCurrentlyPlaying = txtTitle;
        clickListener.onItemClicked(index);
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
        holder.bindView(clickListener, model);
        if(!mediaLoaded && position==0)
            playFirst(holder.txtTitle, model.getUrlIndex());
    }

    class ContentViewHolder extends RecyclerView.ViewHolder{
        TextView txtTitle;
        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }

        void bindView(ItemClickListener listener, Video model){
            txtTitle.setText(model.getTitle());
            itemView.setOnClickListener(view -> {
                if(txtCurrentlyPlaying!=null)
                    txtCurrentlyPlaying.setTextColor(txtCurrentlyPlaying.getContext().getResources()
                            .getColor(R.color.colorBlack));
                txtTitle.setTextColor(itemView.getContext().getResources().getColor(R.color.colorPrimary));
                mediaLoaded = true;
                txtCurrentlyPlaying = txtTitle;
                listener.onItemClicked(model.getUrlIndex());
            });
        }
    }
}

