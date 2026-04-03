package com.uph23.edu.pawfeeder.adapter;

import android.graphics.Color;
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
        String timestamp = history.getTimestamp();
        holder.txvTime.setText(history.formatTime(timestamp));

        int portion = history.getPortion();
        holder.txvPortion.setText(history.formatPortion(portion));

        String status = history.getStatus();
        holder.txvStatus.setText(status);

        if(status.equalsIgnoreCase("SUCCESS")){
            holder.txvStatus.setTextColor(Color.parseColor("#4F8EF7"));
            holder.txvStatus.setBackgroundResource(R.drawable.bg_badge_blue);
        }
        else{
            holder.txvStatus.setTextColor(Color.parseColor("#F87171"));
            holder.txvStatus.setBackgroundResource(R.drawable.bg_badge_red);
        }

        String type = history.getType();

        if(type != null){
            if(status.equalsIgnoreCase("FAILED")){
                holder.imgIcon.setImageResource(R.drawable.missed_feed);
            }
            else if(type.equalsIgnoreCase("auto")){
                holder.imgIcon.setImageResource(R.drawable.auto_feed);
            }
            else if (type.equalsIgnoreCase("manual_touch")){
                holder.imgIcon.setImageResource(R.drawable.manual_feed);
            }
        }

    }

    @Override
    public int getItemCount() {
        return list_history != null ? list_history.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txvTitle, txvTime, txvStatus, txvPortion;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txvTitle   = itemView.findViewById(R.id.txvTitle);
            txvTime    = itemView.findViewById(R.id.txvTime);
            txvPortion = itemView.findViewById(R.id.txvPortion);
            txvStatus = itemView.findViewById(R.id.txvStatus);
            imgIcon  = itemView.findViewById(R.id.imgIcon);
        }
    }
}
