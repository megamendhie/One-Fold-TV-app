package adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.hbb20.CountryCodePicker;
import com.solojet.onefoldtv.R;
import com.solojet.onefoldtv.UploadVideoActivity;

import config.FirebaseUtils;
import models.Video;

import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.VIDEO_PATH;

public class ContentAdapter extends FirestoreRecyclerAdapter<Video, ContentAdapter.ContentViewHolder> {
    private final ItemClickListener clickListener;
    private TextView txtCurrentlyPlaying;
    private boolean mediaLoaded = false;
    private final boolean isAdmin;
    private final boolean playFirstMedia;
    private final String videoId;

    public interface ItemClickListener {
        void onItemClicked (Video model, boolean instantPlay);
    }

    public ContentAdapter (ItemClickListener clickListener, Query query, boolean isAdmin,
                           boolean playFirstMedia, String videoId){
        super(new FirestoreRecyclerOptions.Builder<Video>()
                .setQuery(query, Video.class)
                .build());
        this.clickListener = clickListener;
        this.isAdmin = isAdmin;
        this.playFirstMedia = playFirstMedia;
        this.videoId = videoId;
    }

    private void playFirst(TextView txtTitle, Video model){
        txtTitle.setTextColor(txtTitle.getContext().getResources().getColor(R.color.colorPrimary));
        txtCurrentlyPlaying = txtTitle;
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
        String thumbnailHq = "https://img.youtube.com/vi/"+model.getUrlIndex()+"/mqdefault.jpg";

        holder.bindView(clickListener, model);
        Glide.with(holder.imgThumbnail).load(thumbnailHq).into(holder.imgThumbnail);
        if(playFirstMedia && !mediaLoaded && position==0)
            playFirst(holder.txtTitle, model);

        holder.imgMore.setVisibility(isAdmin? View.VISIBLE: View.INVISIBLE);
        holder.imgMore.setOnClickListener(view -> holder.openDialog(model));
        if(!playFirstMedia && videoId.equals(model.getId())) {
            txtCurrentlyPlaying = holder.txtTitle;
            holder.txtTitle.setTextColor(holder.txtTitle.getContext().getResources().getColor(R.color.colorPrimary));
        }
    }

    class ContentViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imgThumbnail;
        private final TextView txtTitle;
        private final ImageView imgMore;

        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            imgMore = itemView.findViewById(R.id.imgMore);
        }

        void bindView(ItemClickListener listener, Video model){
            String userId;
            txtTitle.setText(model.getTitle());

            if(FirebaseUtils.getAuth().getCurrentUser()!=null){
                userId = FirebaseUtils.getAuth().getCurrentUser().getUid();
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

        void openDialog(Video model){
            AlertDialog.Builder builder = new AlertDialog.Builder(imgMore.getRootView().getContext());
            LayoutInflater inflater = LayoutInflater.from(imgMore.getRootView().getContext());
            View dialogView;
            dialogView = inflater.inflate(R.layout.dialog_content, null);
            builder.setView(dialogView);
            final AlertDialog dialog= builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

            Button btnEdit = dialog.findViewById(R.id.btnEdit);
            Button btnDelete = dialog.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(view -> {
                dialog.cancel();
                Intent intent = new Intent(btnEdit.getContext(), UploadVideoActivity.class);
                intent.putExtra("model", model);
                intent.putExtra(STATUS, "edit");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                btnEdit.getContext().startActivity(intent);
            });

            btnDelete.setOnClickListener(view -> FirebaseUtils.getDatabase().collection(VIDEO_PATH)
                    .document(model.getId()).delete()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            dialog.cancel();
                            Snackbar.make(imgMore, "VIDEO DELETED", Snackbar.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(btnEdit.getRootView().getContext(),
                                    "Cannot delete video", Toast.LENGTH_SHORT).show();
                        }
                    }));
        }
    }
}

