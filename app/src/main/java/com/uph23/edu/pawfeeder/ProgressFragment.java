package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.uph23.edu.pawfeeder.adapter.HistoryAdapter;
import com.uph23.edu.pawfeeder.model.History;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressFragment extends Fragment {
    TextView txvStreak, txvDrink, txvFeed, txvSelectedDate, txvSchedule;
    RecyclerView rvFeedingHistory;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    HistoryAdapter adapter;
    List<History> historyList;
    List<History> allhistoryList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference rtRef;

    private static final String TAG = "Progress Fragment";
    private long lastFoodStock = 0;
    private long lastDrinkStock = 0;
    private boolean isFoodStockInitialized = false;
    private boolean isDrinkStockInitialized = false;


    public ProgressFragment() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadStock();
        calculateStreak();
        loadStats();
        historyList = new ArrayList<>();
        allhistoryList = new ArrayList<>();
        loadHistoryList();
        loadHistoryLog();

        txvSelectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDropdown(view);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        txvStreak = view.findViewById(R.id.txvStreak);
        txvDrink = view.findViewById(R.id.txvDrink);
        txvFeed = view.findViewById(R.id.txvFeed);
        txvSchedule = view.findViewById(R.id.txvSchedule);
        txvSelectedDate = view.findViewById(R.id.txvSelectedDate);
        txvSelectedDate.setText("Today ⌵");
        rvFeedingHistory = view.findViewById(R.id.rvFeedingHistory);
    }

    private void loadStats() {
        loadConsumption();
        loadStreak();
        countSchedules();
    }

    private void loadConsumption() {
        db.collection("Daily_Consumption")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            double totalFeed = 0;
                            double totalDrink = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                Double feedValue = document.getDouble("food");
                                Double drinkValue = document.getDouble("drink");
                                if (feedValue != null) {
                                    totalFeed += feedValue;
                                }
                                if (drinkValue != null) {
                                    totalDrink += drinkValue;
                                }
                            }

                            double totalFeedKg = totalFeed / 1000.0;
                            double totalDrinkL = totalDrink / 1000.0;

                            String formattedFeed = String.format(Locale.US, "%.1f", totalFeedKg);
                            String formattedDrink = String.format(Locale.US, "%.1f", totalDrinkL);

                            txvFeed.setText(formattedFeed);
                            txvDrink.setText(formattedDrink);

                        } else {
                            Log.e(TAG, "Gagal mendapatkan data total konsumsi dari Firestore.", task.getException());
                            txvFeed.setText("Err");
                            txvDrink.setText("Err");
                        }
                    }
                });
    }

    private void calculateStreak(){
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String todaydate = date.format(new Date());

        db.collection("Daily_Consumption")
                .orderBy(FieldPath.documentId(), Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult() != null) {
                            List<String> dates = new ArrayList<>();
                            for(DocumentSnapshot document : task.getResult()){
                                dates.add(document.getId());
                            }
                             countDates(dates,todaydate);
                        }
                        else{
                            Log.e(TAG, "Gagal memuat data", task.getException());
                        }
                    }
                });
    }
    private void countSchedules(){
        String userID = mAuth.getCurrentUser().getUid();
        db.collection("History")
                .whereEqualTo("Id_User", userID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            int totalSchedules = task.getResult().size();

                            txvSchedule.setText(String.valueOf(totalSchedules));

                            Log.d(TAG, "Total jadwal ditemukan: " + totalSchedules);
                        } else {
                            Log.e(TAG, "Error mendapatkan jumlah jadwal", task.getException());
                            txvSchedule.setText("0");
                        }
                    }
                });
    }
    private void countDates(List<String> dates,String todaydate){
        int streak = 0;
        if(dates.isEmpty()){streak = 0;}
        if(!dates.contains(todaydate)){streak = 0;}

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd",Locale.getDefault());
            Date currentDate = sdf.parse(todaydate);

            for(int i = dates.size() - 1; i >= 0; i--){
                Date docDate = sdf.parse(dates.get(i));

                long difference = currentDate.getTime() - docDate.getTime();
                long daydif = difference/ (24 * 60 * 60 * 1000);

                if(daydif == streak){
                    streak++;
                }
                else if(daydif > streak){
                    break;
                }
            }

        }catch (Exception e){
            Log.e(TAG,"Error calculating streak",e);
        }
        setStreak(streak);
    }
    private void setStreak(int streak){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
       Map<String,Object> streaks = new HashMap<>();
       streaks.put("Streak",streak);

        db.collection("Exp")
                .document(uid)
                .set(streaks, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("STREAK", "Streak berhasil diperbarui ke Exp/" + uid + ": " + streak);
                        // Update UI setelah berhasil
                        txvStreak.setText(String.valueOf(streak));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("STREAK", "Gagal memperbarui streak di Exp/" + uid, e);
                    }
                });
    }
    private void loadStreak(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        db.collection("Exp")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            Long currentStreak = document.getLong("Streak");
                            txvStreak.setText(currentStreak != null ? String.valueOf(currentStreak) : "0");
                            Log.d("STREAK", "Streak berhasil diload dari UID: " + uid);
                        }
                        else{
                            Log.e("STREAK", "Error load streak", task.getException());
                            txvStreak.setText("Err");
                        }
                    }
                });
    }
    private void loadStock() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("pawfeeder");
        // --- Listener Stok Makanan ---
        rootRef.child("makan/stok_makanan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentStok = dataSnapshot.getValue(Integer.class);
                if (currentStok == null) return;
                processChanges("Makanan",currentStok.longValue());

            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Gagal membaca stok makanan.", error.toException());
            }
        });

        // --- Listener Stok Minuman ---
        rootRef.child("minum/stok_minuman").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentStok = dataSnapshot.getValue(Integer.class);
                processChanges("Minuman",currentStok.longValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Gagal membaca stok minuman.", error.toException());
            }
        });
    }
    private void processChanges(String type, long currentStock) {
        long lastStock;
        boolean isInitialized;

        if (type.equals("Makanan")) {
            lastStock = lastFoodStock;
            isInitialized = isFoodStockInitialized;
        } else if (type.equals("Minuman")) {
            lastStock = lastDrinkStock;
            isInitialized = isDrinkStockInitialized;
        } else {
            Log.e(TAG, "Jenis stok tidak dikenal: " + type);
            return;
        }

        if (!isInitialized) {
            if (type.equals("Makanan")) {
                lastFoodStock = currentStock;
                isFoodStockInitialized = true;
            } else if (type.equals("Minuman")) {
                lastDrinkStock = currentStock;
                isDrinkStockInitialized = true;
            }
            Log.d(TAG, "Inisialisasi stok " + type + ": " + currentStock);
            return;
        }

        long differenceStock = lastStock - currentStock;

        if (differenceStock > 0) {
            Log.i(TAG, "JUMLAH KELUAR (" + type + "): " + differenceStock + " unit.");
            loadtoDailyConsumption(differenceStock, type);
        }


        if (type.equals("Makanan")) {
            lastFoodStock = currentStock;
        } else if (type.equals("Minuman")) {
            lastDrinkStock = currentStock;
        }
    }
    private void loadtoDailyConsumption(long amount,String itemType){

        if (db == null) {
            Log.e(TAG, "Firestore belum diinisialisasi.");
            return;
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String documentId = sdf.format(new Date());


        String fieldName = (itemType.equals("Makanan")) ? "food" : "drink";


        Map<String, Object> consumptionData = new HashMap<>();

        consumptionData.put(fieldName, FieldValue.increment(amount));
        consumptionData.put("timestamp", FieldValue.serverTimestamp());


        db.collection("Daily_Consumption")
                .document(documentId)
                .set(consumptionData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Konsumsi harian (" + itemType + ": " + amount + ") berhasil dicatat ke Firestore.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Gagal mencatat konsumsi harian ke Firestore.", e);
                    }
                });
    }
    private void loadHistoryList(){
        adapter = new HistoryAdapter(historyList);
        rvFeedingHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFeedingHistory.setAdapter(adapter);
    }
    private void showDateDropdown(View view){
        PopupMenu datePopup = new PopupMenu(requireContext(),view);
        datePopup.getMenuInflater().inflate(R.menu.datefilter, datePopup.getMenu());

        datePopup.setOnMenuItemClickListener(item -> {
            Calendar calendar = Calendar.getInstance();
            int itemID = item.getItemId();
            if(itemID == R.id.filter_today){
                txvSelectedDate.setText("Today ⌵");
                filterByTodayDate();
                return true;
            } else if (itemID == R.id.filter_yesterday) {
                calendar.add(Calendar.DATE, -1);
                txvSelectedDate.setText("Yesterday ⌵");
                filterByYesterdayDate();
                return true;
            } else if (itemID == R.id.filter_week) {
                txvSelectedDate.setText("Last 7 Days ⌵");
                filterByLastWeek();
                return true;
            }
            return false;
        });
        datePopup.show();
    }
    private void fetchHistoryLog(){
        rtRef = FirebaseDatabase.getInstance().getReference("pawfeeder/log_history");
        String userID = mAuth.getCurrentUser().getUid();

        rtRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    String key = snapshot.getKey();
                    String title = snapshot.child("Title").getValue(String.class);
                    Number portion = (Number) snapshot.child("portion").getValue();
                    String status = snapshot.child("status").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);
                    String type = snapshot.child("type").getValue(String.class);

                    Map<String, Object> history = new HashMap<>();
                    history.put("Id_User", userID);
                    history.put("ScheduleID", key);
                    history.put("Title", title);
                    history.put("Portion", portion != null ? portion.intValue() : 0);
                    history.put("Status", status);
                    history.put("Timestamp", timestamp);
                    history.put("Type", type);

                    db.collection("History")
                            .add(history)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("Firestore", "Success");
                                rtRef.child(key).removeValue();
                            })
                            .addOnFailureListener(e -> Log.e("Firestore", "Failed" , e));
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void loadHistoryLog(){
        fetchHistoryLog();
        String userID = mAuth.getCurrentUser().getUid();

        db.collection("History")
                .whereEqualTo("Id_User", userID)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allhistoryList.clear();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        History history = document.toObject(History.class);
                        allhistoryList.add(history);
                    }
                    txvSelectedDate.setText("Today ⌵");
                    filterByTodayDate();
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("History", "Failed", e));
    }
    private String getDate(int daysOffset){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, daysOffset);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }
    private void filterByTodayDate(){
        String todayDate = getDate(0);

        List<History> filterTodayList = new ArrayList<>();

        for(History h : allhistoryList){
            String timestamp = h.getTimestamp();
            if (timestamp != null && timestamp.length() >= 10) {
                String date = timestamp.substring(0, 10);

                if (date.equals(todayDate)) {
                    filterTodayList.add(h);
                }
            }
        }
        updateView(filterTodayList);
    }
    private void filterByYesterdayDate(){
        String todayDate = getDate(-1);

        List<History> filterYesterdayList = new ArrayList<>();

        for(History h : allhistoryList){
            String timestamp = h.getTimestamp();
            if (timestamp != null && timestamp.length() >= 10) {
                String date = timestamp.substring(0, 10);

                if (date.equals(todayDate)) {
                    filterYesterdayList.add(h);
                }
            }
        }
        updateView(filterYesterdayList);
    }
    private void filterByLastWeek(){
        List<History> filterLastWeekList = new ArrayList<>();

        String limitDate = getDate(-7);
        String todayDate = getDate(0);

        for (History h : allhistoryList) {
            String timestamp = h.getTimestamp();
            if (timestamp != null && timestamp.length() >= 10) {
                String docDate = timestamp.substring(0, 10);
                if (docDate.compareTo(limitDate) >= 0 && docDate.compareTo(todayDate) <= 0) {
                    filterLastWeekList.add(h);
                }
            }
        }
        updateView(filterLastWeekList);
    }
    private void updateView(List<History> newList) {
        historyList.clear();
        historyList.addAll(newList);
        adapter.notifyDataSetChanged();
    }
}