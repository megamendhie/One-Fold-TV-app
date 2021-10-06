package adapters;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.Query;
import com.solojet.onefoldtv.R;
import com.solojet.onefoldtv.UploadEventActivity;

import config.FirebaseUtils;
import models.Event;

import static models.ConstantVariables.STATUS;
import static models.ConstantVariables.VIDEO_PATH;

public class EventAdapter extends FirestoreRecyclerAdapter<Event, EventAdapter.EventViewHolder> {
    private boolean isAdmin;

    public EventAdapter(Query query, boolean isAdmin) {
        super(new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build());
        this.isAdmin = isAdmin;
    }

    @Override
    protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull Event model) {
        holder.bindView(model);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent,false);
        return new EventViewHolder(view);
    }

    class EventViewHolder extends RecyclerView.ViewHolder{
        TextView txtTitle, txtVenue, txtDate, txtTime, txtAbout;
        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtVenue = itemView.findViewById(R.id.txtVenue);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtAbout = itemView.findViewById(R.id.txtAbout);
        }

        void bindView(Event model){
            txtTitle.setText(model.getTitle());
            txtVenue.setText(model.getVenue());
            txtAbout.setText(model.getAbout());

            String date = DateFormat.format("E. dd MMM, yyyy", model.getTimeEvent()).toString();
            String time = DateFormat.format("h:mm a", model.getTimeEvent()).toString();
            txtDate.setText(date);
            txtTime.setText(time);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            itemView.setOnLongClickListener(view -> {
                if(!isAdmin)
                    return false;
                openDialog(model);
                return false;
            });
        }


        void openDialog(Event model){
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getRootView().getContext());
            LayoutInflater inflater = LayoutInflater.from(itemView.getRootView().getContext());
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
                Intent intent = new Intent(btnEdit.getContext(), UploadEventActivity.class);
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
                            Snackbar.make(itemView, "EVENT DELETED", Snackbar.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(btnEdit.getRootView().getContext(),
                                    "Cannot delete event", Toast.LENGTH_SHORT).show();
                        }
                    }));
        }
    }
}

