package adapters;

import static models.ConstantVariables.FROM_MAIN;
import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.TYPE;
import static models.ConstantVariables.VIDEO;
import static models.ConstantVariables.VIDEO_PATH;

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
import com.solojet.onefoldtv.ContentActivity;
import com.solojet.onefoldtv.R;
import com.solojet.onefoldtv.UploadVideoActivity;

import config.FirebaseUtils;
import models.Video;

public class ContentProAdapter extends FirestoreRecyclerAdapter<Video, ContentProAdapter.ContentViewHolder> {
    private final boolean isAdmin;

    public ContentProAdapter(Query query, boolean isAdmin){
        super(new FirestoreRecyclerOptions.Builder<Video>()
                .setQuery(query, Video.class)
                .build());
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_lg, parent,false);
        return new ContentViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ContentViewHolder holder, int position, @NonNull Video model) {
        String thumbnailHq = "https://img.youtube.com/vi/"+model.getUrlIndex()+"/mqdefault.jpg";

        holder.bindView(model);
        Glide.with(holder.imgThumbnail).load(thumbnailHq).into(holder.imgThumbnail);

        holder.imgMore.setVisibility(isAdmin? View.VISIBLE: View.INVISIBLE);
        holder.imgMore.setOnClickListener(view -> holder.openDialog(model));

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

        void bindView(Video model){
            String userId;
            txtTitle.setText(model.getTitle());

            if(FirebaseUtils.getAuth().getCurrentUser()!=null){
                userId = FirebaseUtils.getAuth().getCurrentUser().getUid();
            }
            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(itemView.getContext(), ContentActivity.class);
                intent.putExtra(TYPE, model.getType());
                intent.putExtra(FROM_MAIN, true);
                intent.putExtra(VIDEO, model);
                itemView.getContext().startActivity(intent);
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

