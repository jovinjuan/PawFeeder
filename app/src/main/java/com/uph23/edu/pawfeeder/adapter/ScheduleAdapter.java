package com.uph23.edu.pawfeeder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uph23.edu.pawfeeder.R;
import com.uph23.edu.pawfeeder.model.Schedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private OnScheduleClickListener listener;
    private List<Schedule> currentList;

    public ScheduleAdapter(List<Schedule> list, OnScheduleClickListener listener) {
        this.currentList = new ArrayList<>();
        this.currentList.addAll(list);
        this.listener = listener;
    }
    public interface OnScheduleClickListener {
        void onFeedNow(Schedule schedule);
        void onEdit(Schedule schedule);
        void onDelete(Schedule schedule);
        void onNotif(Schedule schedule);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_feedingschedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Schedule schedule = currentList.get(position);

        holder.txvMealName.setText(schedule.getSchedule_Name());
        holder.txvTime.setText(schedule.getFeedTime());
        holder.txvPortion.setText("Portion Size : " + schedule.getPortion() + " g");
        holder.btnFeedNow.setOnClickListener(v -> listener.onFeedNow(schedule));
        holder.imgDelete.setOnClickListener(v -> listener.onDelete(schedule));
        holder.imgEdit.setOnClickListener(v -> listener.onEdit(schedule));
        holder.imgNotif.setOnClickListener(v -> listener.onNotif(schedule));

    }
    public void updateList(List<Schedule> newList) {
        currentList.clear();
        currentList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return currentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txvMealName, txvTime, txvPortion;
        Button btnFeedNow;
        ImageView imgEdit, imgDelete, imgNotif;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txvMealName   = itemView.findViewById(R.id.txvMealName);
            txvTime       = itemView.findViewById(R.id.txvTime);
            txvPortion    = itemView.findViewById(R.id.txvPortion);
            btnFeedNow   = itemView.findViewById(R.id.btnFeed);
            imgEdit       = itemView.findViewById(R.id.imgEdit);
            imgDelete    = itemView.findViewById(R.id.imgDelete);
            imgNotif  = itemView.findViewById(R.id.imgNotification);
        }
    }
}
