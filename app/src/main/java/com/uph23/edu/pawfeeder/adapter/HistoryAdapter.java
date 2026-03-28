package com.uph23.edu.pawfeeder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uph23.edu.pawfeeder.R;
import com.uph23.edu.pawfeeder.model.History;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<History> list_history;

    public HistoryAdapter(List<History> list_history) {
        this.list_history = list_history;
    }
    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_history, parent, false);
        return new HistoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = list_history.get(position);

        holder.txvTitle.setText(history.getTitle());
        holder.txvDescription.setText(history.getDescription());

        String title = history.getTitle();

        if(title != null){
            if(title.equalsIgnoreCase("Auto Feeding")){
                holder.imgIcon.setImageResource(R.drawable.auto_feed);
            }
            else if (title.equalsIgnoreCase("Manual Feeding")){
                holder.imgIcon.setImageResource(R.drawable.manual_feed);
            }
            else if (title.equalsIgnoreCase("Failed Feeding")) {
                holder.imgIcon.setImageResource(R.drawable.missed_feed);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list_history != null ? list_history.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txvTitle, txvDescription, txvStatus;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txvTitle   = itemView.findViewById(R.id.txvTitle);
            txvDescription    = itemView.findViewById(R.id.txvDescription);
            txvStatus = itemView.findViewById(R.id.txvStatus);
            imgIcon  = itemView.findViewById(R.id.imgIcon);
        }
    }
}
