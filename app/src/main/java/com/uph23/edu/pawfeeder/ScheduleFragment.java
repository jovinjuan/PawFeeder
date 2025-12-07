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
import android.widget.Toast;

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
    private void toAddSchedule(){
        Intent intent = new Intent(requireContext(),CreateScheduleActivity.class);
        startActivity(intent);
    }
    private void loadSchedulesData() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Schedule")
                .whereEqualTo("Id_User", userId)
                .addSnapshotListener((snapshot, e) -> {


                    if (e != null) {
                        Log.e("ScheduleFragment", "Listen failed: " + e.getMessage(), e);
                        return;
                    }


                    if (snapshot != null) {
                        scheduleList.clear();
                        datesWithSchedule.clear();

                        for (QueryDocumentSnapshot doc : snapshot) {
                            Schedule s = doc.toObject(Schedule.class);
                            s.setId(doc.getId());
                            String feedDate = s.getFeedDate();
                            scheduleList.add(s);

                            if (feedDate != null && !datesWithSchedule.contains(feedDate)) {
                                datesWithSchedule.add(feedDate);
                            }
                        }

                        scheduleList.sort((a, b) -> {
                            String timeA = a.getFeedTime() != null ? a.getFeedTime() : "";
                            String timeB = b.getFeedTime() != null ? b.getFeedTime() : "";
                            return timeA.compareTo(timeB);
                        });

                        if (feedAdapter == null) {

                            feedAdapter = new ScheduleAdapter(scheduleList, ScheduleFragment.this);
                            rvFeedSchedule.setAdapter(feedAdapter);
                        } else {
                            feedAdapter.notifyDataSetChanged();
                        }

                        dateAdapter.updateDatesWithSchedule(datesWithSchedule);
                        showFeedingSchedule(selectedDate);

                        Log.d("ScheduleFragment", "Schedules loaded: " + scheduleList.size());
                    }
                });
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
        db = FirebaseFirestore.getInstance();
        db.collection("Schedule").document(schedule.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. Hapus data yang sesuai dari Realtime Database
                    DatabaseReference myRef = FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/autofeed");

                    // Gunakan scheduleId sebagai key (child) dan panggil removeValue()
                    myRef.child(schedule.getId()).removeValue()
                            .addOnSuccessListener(a -> {
                                Log.d(TAG, "Jadwal berhasil dihapus dari Realtime DB: " + schedule.getId());
                                // Opsional: Berikan feedback ke pengguna (misalnya Toast)
                                // Toast.makeText(context, "Jadwal berhasil dihapus!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(eRealtime -> {
                                Log.e(TAG, "Gagal menghapus jadwal dari Realtime DB: " + schedule.getId(), eRealtime);
                                // Berikan feedback bahwa ada masalah, meskipun di Firestore berhasil
                            });
                    Log.d(TAG, "Jadwal berhasil dihapus: " + schedule.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Gagal menghapus jadwal: " + schedule.getId(), e);
                });
    }

    @Override
    public void onNotif(Schedule schedule) {

    }
    private void autoFeed(){

    }
}

