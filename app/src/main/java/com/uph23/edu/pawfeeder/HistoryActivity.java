package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

public class HistoryActivity extends AppCompatActivity {

    ImageView imgBack, imgFilter;
    LinearLayout llFilterFood, llFilterWater, llFilterOption;
    TextView txvTotalSchedule, txvFood, txvMissed, txvSelectedDate;
    RecyclerView rvFeedingHistory;
    HistoryAdapter adapter;
    List<History> historyList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference rtRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        historyList = new ArrayList<>();
        loadHistoryList();
        fetchHistoryLog();
        loadHistoryLog();
        getTotalSchedule();
        getTotalFood();
//        getMissedSchedule();


        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        txvSelectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDropdown(view);
            }
        });
        imgFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilter();
            }
        });
    }
    public void init(){
        imgBack = findViewById(R.id.imgBack);
        imgFilter = findViewById(R.id.imgFilter);
        txvTotalSchedule = findViewById(R.id.txvTotalSchedule);
        txvFood = findViewById(R.id.txvFood);
        txvMissed = findViewById(R.id.txvMissed);
        txvSelectedDate = findViewById(R.id.txvSelectedDate);
        llFilterFood = findViewById(R.id.llFilterFood);
        llFilterWater = findViewById(R.id.llFilterWater);
        llFilterOption = findViewById(R.id.llFilterOption);
        rvFeedingHistory = findViewById(R.id.rvFeedingHistory);
    }
    private void getTotalSchedule(){
        String userId = mAuth.getCurrentUser().getUid();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        db.collection("Schedule")
                .whereEqualTo("Id_User", userId)
                .whereEqualTo("FeedDate", todayDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if(queryDocumentSnapshots.isEmpty()){
                        txvTotalSchedule.setText("0");
                    }
                    else{
                        int totalSchedule = queryDocumentSnapshots.size();
                        txvTotalSchedule.setText(String.valueOf(totalSchedule));
                    }
                });
    }
    private void getTotalFood(){
        String userId = mAuth.getCurrentUser().getUid();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = sdf.format(new Date());

        db.collection("Schedule")
                .whereEqualTo("Id_User", userId)
                .whereEqualTo("FeedDate", todayDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalPortion = 0;
                    if(queryDocumentSnapshots.isEmpty()){
                        txvFood.setText("0");
                    }
                    else{
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                           Object value = document.get("Portion");
                           if(value != null){
                               int portion = Integer.parseInt( (String) value);
                               totalPortion += portion;
                           }
                        }
                        txvFood.setText(String.valueOf(totalPortion) + "g");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("History", "Gagal mengambil data", e);
                });
    }
    private void loadHistoryList(){
        adapter = new HistoryAdapter(historyList);
        rvFeedingHistory.setLayoutManager(new LinearLayoutManager(this));
        rvFeedingHistory.setAdapter(adapter);
    }
    private void showDateDropdown(View view){
        PopupMenu datePopup = new PopupMenu(this,view);
        datePopup.getMenuInflater().inflate(R.menu.datefilter, datePopup.getMenu());

        datePopup.setOnMenuItemClickListener(item -> {
            Calendar calendar = Calendar.getInstance();
            int itemID = item.getItemId();
            if(itemID == R.id.filter_today){
                txvSelectedDate.setText("Today ⌵");
//                fetchHistoryData(calendar.getTime());
                return true;
            } else if (itemID == R.id.filter_yesterday) {
                calendar.add(Calendar.DATE, -1);
                txvSelectedDate.setText("Yesterday ⌵");
//                fetchHistoryData(calendar.getTime());
                return true;
            } else if (itemID == R.id.filter_week) {
                txvSelectedDate.setText("Last 7 Days");
//                fetchHistoryData(7);
                return true;
            }
            return false;
        });
        datePopup.show();
    }
    private void showFilter(){
        if(llFilterOption.getVisibility() == View.VISIBLE){
            llFilterOption.setVisibility(View.GONE);
        }
        else{
            llFilterOption.setVisibility(View.VISIBLE);
        }
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
        String userID = mAuth.getCurrentUser().getUid();

        db.collection("History")
                .whereEqualTo("Id_User", userID)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();
                    for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                        History history = document.toObject(History.class);
                        historyList.add(history);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("History", "Failed", e));
    }
//    private void getMissedSchedule(){
//        String userID = mAuth.getCurrentUser().getUid();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        String todayDate = sdf.format(new Date());
//
//        String startToday = todayDate + " 00:00:00";
//        String endToday = todayDate + " 23:59:59";
//
//        db.collection("History")
//                .whereEqualTo("Id_User", userID)
//                .whereEqualTo("Status", "FAILED")
//                .whereGreaterThanOrEqualTo("Timestamp", startToday)
//                .whereLessThanOrEqualTo("Timestamp", endToday)
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        int totalMissed = queryDocumentSnapshots.size();
//                        txvMissed.setText(String.valueOf(totalMissed));
//                        Log.d("Firestore", "Berhasil: " + totalMissed);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e("Firestore", "Gagal ambil data", e);
//                    }
//                });
//    }
}