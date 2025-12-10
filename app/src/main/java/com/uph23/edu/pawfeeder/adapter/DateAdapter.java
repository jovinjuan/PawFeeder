package com.uph23.edu.pawfeeder.adapter;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uph23.edu.pawfeeder.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    public List<Date> dates = new ArrayList<>();
    private Date selectedDate = new Date();
    private List<String> datesWithSchedule = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private OnDateClickListener dateslistener;

    public interface OnDateClickListener {
        void onDateClick(Date date);
    }
    public DateAdapter(OnDateClickListener dateslistener) {
        this.dateslistener = dateslistener;
        selectedDate = new Date();
        loadListDate();

    }
    public void setSelectedDate(Date date){
        this.selectedDate = date;
        notifyDataSetChanged();
    }
    private void loadListDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for(int i = 0; i < maxDays; i++){
            dates.add(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH,1);

        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_card, parent, false);
        return new DateViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        Date date = dates.get(position);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String dateKey = dateFormat.format(date);

        holder.tvDayName.setText(new SimpleDateFormat("EEE", Locale.US).format(date));
        holder.tvDate.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

        boolean isSelected = isSameDay(date, selectedDate);
        boolean hasSchedule = datesWithSchedule.contains(dateKey);

        if(isSelected){
            holder.tvDayName.setTextColor(Color.WHITE);
            holder.cvDates.setBackgroundColor(Color.BLUE);
            holder.tvDate.setTextColor(Color.WHITE);
        }
        else{
            holder.cvDates.setBackgroundColor(Color.WHITE);
            holder.tvDayName.setTextColor(Color.parseColor("#888888"));
            holder.tvDate.setTextColor(Color.BLACK);
        }
        holder.dotindicator.setVisibility(hasSchedule ? View.VISIBLE : View.GONE);


        holder.itemView.setOnClickListener(v -> {
            setSelectedDate(date);
            notifyDataSetChanged();
            dateslistener.onDateClick(date);
        });
    }


    @Override
    public int getItemCount() {
        return dates.size();
    }
    private boolean isSameDay(Date date1, Date selectedDate){
        Calendar c1 = Calendar.getInstance(); c1.setTime(date1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(selectedDate);

        if(c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)){
            return true;
        } else{
            return false;
        }
    }
    public void updateDatesWithSchedule(List<String> dates) {
        this.datesWithSchedule = dates;
        notifyDataSetChanged();
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDate;
        View dotindicator;
        CardView cvDates;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDate = itemView.findViewById(R.id.tvDate);
            dotindicator = itemView.findViewById(R.id.dotIndicator);
            cvDates = itemView.findViewById(R.id.cvDate);
        }

    }
}
