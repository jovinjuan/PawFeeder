package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uph23.edu.pawfeeder.adapter.DateAdapter;
import com.uph23.edu.pawfeeder.adapter.ScheduleAdapter;
import com.uph23.edu.pawfeeder.model.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleFragment extends Fragment implements DateAdapter.OnDateClickListener, ScheduleAdapter.OnScheduleClickListener{
    RecyclerView rvDates, rvFeedSchedule;
    TextView txvDates, txvAddSchedule;
    DateAdapter dateAdapter;
    ScheduleAdapter feedAdapter;
    Date selectedDate = new Date();
    List<Schedule> scheduleList= new ArrayList<>();
    List<String> datesWithSchedule = new ArrayList<>();

    private SimpleDateFormat monthlyFormat = new SimpleDateFormat("MMMM yyyy",Locale.US);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    // Di ScheduleFragment.java
    private static final String TAG = "Schedule Fragment";

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        init(view);
        dateAdapter = new DateAdapter(this);
        rvDates.setAdapter(dateAdapter);


        updateMonthTitle();
        loadSchedulesData();
        scrollCalendarToToday();


        txvAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toAddSchedule();
            }
        });

        return view;
    }
    @Override
    public void onDateClick(Date date) {
        this.selectedDate = date;
        updateMonthTitle();
        showFeedingSchedule(date);
    }
    private void updateMonthTitle(){
        txvDates.setText(monthlyFormat.format(selectedDate));
    }

    public void init(View view) {
        txvDates = view.findViewById(R.id.txvDates);
        txvAddSchedule = view.findViewById(R.id.txvAddSchedule);
        rvDates = view.findViewById(R.id.rvDates);
        rvFeedSchedule = view.findViewById(R.id.rvfeedingSchedule);

        rvDates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeedSchedule.setLayoutManager(new LinearLayoutManager(getContext()));

    }
//    private void buildCalendar() {
//        dateList.clear();
//        Calendar todayCal = Calendar.getInstance();
//        int year = todayCal.get(Calendar.YEAR);
//        int month = todayCal.get(Calendar.MONTH);
//        int todayDate = todayCal.get(Calendar.DAY_OF_MONTH);
//
//        Calendar cal = Calendar.getInstance();
//        cal.set(year, month, 1);
//
//        int currentMonth = cal.get(Calendar.MONTH);
//        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
//        txvDates.setText(monthFormat.format(cal.getTime()));
//
//        while (cal.get(Calendar.MONTH) == currentMonth) {
//            int day = cal.get(Calendar.DAY_OF_MONTH);
//            String dayName = new SimpleDateFormat("EEE", Locale.ENGLISH).format(cal.getTime());
//            Date fullDate = cal.getTime();
//
//            boolean hasSchedule = false;
//            for (Schedule s : feedList) {
//                if (s.getFeedingDate() != null && !s.getFeedingDate().isEmpty()) {
//                    try{
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//                    Date feedingDate = sdf.parse(s.getFeedingDate());
//                    if(feedingDate != null && isSameDate(feedingDate,fullDate)) hasSchedule = true; break;
//                    }catch (Exception e){}
//                }
//            }
//
//            boolean isToday = (day == todayDate);
//            dateList.add(new DateItem(day, dayName, hasSchedule, fullDate));
//            cal.add(Calendar.DAY_OF_MONTH, 1);
//        }
//
//        if (dateAdapter != null) dateAdapter.notifyDataSetChanged();
//    }
//    private void filterFeedSchedule(Date selectedDate){
//        List<Schedule> todayFeedings = new ArrayList<>();
//
//
//        for (Schedule s : feedList) {
//            String raw = s.getFeedingDate();
//            Log.d("DEBUG_TANGGAL", "ID: " + s.getId() + " → feedingDate = '" + raw + "'");
//            try {
//                // Mengonversi feedingDate menjadi objek Date
//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//                Date feedingDate = dateFormat.parse(s.getFeedingDate());  // Mengonversi string ke Date
//
//                // Memeriksa apakah tanggal feedingDate sama dengan selectedDate
//                if (feedingDate != null && isSameDate(feedingDate, selectedDate)) {
//                    todayFeedings.add(s);
//                }
//            } catch (ParseException e) {
//                // Menangani kasus jika parsing gagal
//                Log.e(TAG, "Parse exception for feedingDate: " + s.getFeedingDate(), e);
//            }
//            }
//
//
//        todayFeedings.sort((a, b) -> {
//            String t1 = a.getFeedingTime() != null ? a.getFeedingTime() : "";
//            String t2 = b.getFeedingTime() != null ? b.getFeedingTime() : "";
//            return t1.compareTo(t2);
//        });
//
//        rvFeedSchedule.setAdapter(feedAdapter);
//
//    }
//    private boolean isSameDate(Date date1, Date date2){
//        Calendar cal1 = Calendar.getInstance();
//        Calendar cal2 = Calendar.getInstance();
//
//        cal1.setTime(date1);
//        cal2.setTime(date2);
//
//        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) &&
//                (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) &&
//                (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));
//    }
    private void toAddSchedule(){
        Intent intent = new Intent(requireContext(),CreateScheduleActivity.class);
        startActivity(intent);
    }
    private void loadSchedulesData(){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();


        db.collection("Schedule")
                .whereEqualTo("Id_User", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Schedule s = doc.toObject(Schedule.class);
                        s.setId(doc.getId());
                        String feedDate = s.getFeedDate();
                        scheduleList.add(s);

                        if (feedDate != null && !datesWithSchedule.contains(feedDate)) {
                            datesWithSchedule.add(feedDate);
                        }
                    }
                    // BARU BUAT ADAPTER SETELAH DATA SUDAH ADA!
                    if (feedAdapter == null) {
                        List<Schedule> initialList = new ArrayList<>();
                        feedAdapter = new ScheduleAdapter(initialList, ScheduleFragment.this);
                        rvFeedSchedule.setAdapter(feedAdapter);
                    } else {
                        feedAdapter.notifyDataSetChanged();
                    }
                    scheduleList.sort((a, b) -> {
                        String timeA = a.getFeedTime() != null ? a.getFeedTime() : "";
                        String timeB = b.getFeedTime() != null ? b.getFeedTime() : "";
                        return timeA.compareTo(timeB);
                    });
                    dateAdapter.updateDatesWithSchedule(datesWithSchedule);
                    showFeedingSchedule(selectedDate);

                    Log.d(TAG, "Task loaded: " + scheduleList.size());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Gagal load task: ", e));
    }
    private void scrollCalendarToToday() {
        for (int i = 0; i < dateAdapter.getItemCount(); i++) {
            Date dateInList = dateAdapter.dates.get(i);
            if (isSameDay(dateInList, new Date())) {
                rvDates.scrollToPosition(i);
                break;
            }
        }
    }
    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
        Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
    private void showFeedingSchedule(Date date){
        if (feedAdapter == null) return;  // safety

        String dateKey = dateFormat.format(date);
        List<Schedule> todayList = new ArrayList<>();

        // Filter dari scheduleList yang sudah full
        for (Schedule s : scheduleList) {
            if (dateKey.equals(s.getFeedDate())) {
                todayList.add(s);
            }
        }


        feedAdapter.updateList(todayList);
    }
    @Override
    public void onResume() {
        super.onResume();
        loadSchedulesData();   // refresh otomatis setelah tambah/edit
    }

    @Override
    public void onFeedNow(Schedule schedule) {
        DatabaseReference servoRef = FirebaseDatabase.getInstance().getReference("pawfeeder/makan/servo");

        servoRef.setValue(true).addOnSuccessListener(avoid -> {
            Log.d("FeedNow","Servo bernilai true");
        });
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        servoRef.setValue(false)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("FeedNow", "Servo set ke FALSE");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FeedNow", "Gagal set servo ke FALSE: " + e.getMessage());
                                });
                    }
                },
                3000
        );

    }

    @Override
    public void onEdit(Schedule schedule) {
        Intent intent = new Intent(requireContext(),EditScheduleActivity.class);
        intent.putExtra("EDITSCHEDULE", schedule);
        startActivity(intent);
    }

    @Override
    public void onDelete(Schedule schedule) {

    }

    @Override
    public void onNotif(Schedule schedule) {

    }
}

