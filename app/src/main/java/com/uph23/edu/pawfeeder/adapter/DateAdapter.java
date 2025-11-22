package com.uph23.edu.pawfeeder.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.uph23.edu.pawfeeder.R;

import com.uph23.edu.pawfeeder.model.DateItem;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private List<DateItem> dates;
    private OnDateClickListener onDateClickListener;
    private int selectedPosition = -1;

    public interface OnDateClickListener {
        void onDateClick(DateItem dateItem);
    }

    public DateAdapter(List<DateItem> dates, OnDateClickListener onDateClickListener) {
        this.dates = dates;
        this.onDateClickListener = onDateClickListener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_card, parent, false);
        return new DateViewHolder(view);
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem dateItem = dates.get(position);
        holder.bind(dateItem, position);
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDayName, tvDate, tvMonth;
        private CardView cardView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(DateItem dateItem, int position) {
            tvDayName.setText(dateItem.getDayName());
            tvDate.setText(String.valueOf(dateItem.getDate()));
            tvMonth.setText(dateItem.getMonth());

            // Update selection state
            if (dateItem.isSelected() || selectedPosition == position) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.iconblue));
                tvDayName.setTextColor(Color.WHITE);
                tvDate.setTextColor(Color.WHITE);
                tvMonth.setTextColor(Color.WHITE);
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
                tvDayName.setTextColor(Color.parseColor("#666666"));
                tvDate.setTextColor(Color.BLACK);
                tvMonth.setTextColor(Color.parseColor("#666666"));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int previousSelected = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(previousSelected);
                    notifyItemChanged(selectedPosition);

                    if (onDateClickListener != null) {
                        onDateClickListener.onDateClick(dateItem);
                    }
                }
            });
        }
    }
}
