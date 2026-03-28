package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uph23.edu.pawfeeder.adapter.HistoryAdapter;
import com.uph23.edu.pawfeeder.model.History;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ImageView imgBack, imgFilter;
    TextView txvTotalSchedule, txvFood, txvMissed, txvTodayDate, txvYesterdayDate;
    RecyclerView rvTodaySchedule, rvYesterdaySchedule;
    HistoryAdapter adapter;
    List<History> historyList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();



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
        prepareData(); // Untuk development saja , data dummy

        adapter = new HistoryAdapter(historyList);
        rvTodaySchedule.setLayoutManager(new LinearLayoutManager(this));
        rvYesterdaySchedule.setLayoutManager(new LinearLayoutManager(this));

        rvTodaySchedule.setAdapter(adapter);
        rvYesterdaySchedule.setAdapter(adapter);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getTotalSchedule();
        getTotalFood();

    }
    public void init(){
        imgBack = findViewById(R.id.imgBack);
        imgFilter = findViewById(R.id.imgFilter);
        txvTotalSchedule = findViewById(R.id.txvTotalSchedule);
        txvFood = findViewById(R.id.txvFood);
        txvMissed = findViewById(R.id.txvMissed);
        txvTodayDate = findViewById(R.id.txvTodayDate);
        txvYesterdayDate = findViewById(R.id.txvYesterdayDate);
        rvTodaySchedule = findViewById(R.id.rvTodaySchedule);
        rvYesterdaySchedule = findViewById(R.id.rvYesterdaySchedule);
    }
    private void prepareData() {
        historyList.add(new History("Auto Feeding",  "07.00 PM - 75 g", "SUCCESS"));
        historyList.add(new History("Manual Feeding",  "08.30 AM - 50 g", "SUCCESS"));
        historyList.add(new History("Failed Feeding", "12.00 PM - 75 g", "FAILED"));
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
}