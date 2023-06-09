package akd.technologies.scanner;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scanner.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class SavedAdapter extends FirestoreRecyclerAdapter<saved,SavedAdapter.SavedViewHolder> {

    Context context;

    public SavedAdapter(@NonNull FirestoreRecyclerOptions<saved> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull SavedViewHolder holder, int position, @NonNull saved save) {
        holder.titleTextView.setText(save.name);
        holder.timestampTextView.setText(Utility.timestampToString(save.timestamp));

        holder.itemView.setOnClickListener((view -> {
            Intent intent = new Intent(context, savedActivity.class);
            intent.putExtra("title",save.name);
            intent.putExtra("content",save.content);
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId",docId);
            context.startActivity(intent);

        }));

    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_saved_items,parent,false);
        return new SavedViewHolder(view);
    }

    class SavedViewHolder extends RecyclerView.ViewHolder{


        TextView titleTextView,timestampTextView;

        public SavedViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.title);
            timestampTextView = itemView.findViewById(R.id.timestamp);
        }
    }
}
