package pl.adrian.sharedclipboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClipboardHistoryAdapter extends RecyclerView.Adapter<ClipboardHistoryAdapter.ViewHolder> {
    private List<String> historyItems;
    private FirstFragment fragment;

    public ClipboardHistoryAdapter(FirstFragment fragment) {
        this.fragment = fragment;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = historyItems.get(position);
        holder.itemText.setText(item);
        holder.container.setOnClickListener(view -> {
            Context context = view.getContext().getApplicationContext();
            Toast.makeText(context, item, Toast.LENGTH_SHORT).show();
        });
        holder.itemBtn.setOnClickListener(view -> {
            historyItems.remove(position);
            notifyDataSetChanged();
        });
    }

    public void setHistoryItems(List<String> historyItems) {
        this.historyItems = historyItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        ConstraintLayout container;
        ImageButton itemBtn;

        ViewHolder(View view) {
            super(view);
            itemText = view.findViewById(R.id.itemText);
            container = view.findViewById(R.id.itemContainer);
            itemBtn = view.findViewById(R.id.imageButton);
        }
    }
}
