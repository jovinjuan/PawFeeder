package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryActivity extends AppCompatActivity {

    ImageView imgBack, imgFilter;
    TextView txvTotalSchedule, txvFood, txvMissed, txvTodayDate, txvYesterdayDate;
    RecyclerView rvTodaySchedule, rvYesterdaySchedule;
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
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
}