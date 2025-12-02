package com.uph23.edu.pawfeeder;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uph23.edu.pawfeeder.model.Schedule;

public class EditScheduleActivity extends AppCompatActivity {
    private Schedule currentSchedule;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    ImageView imgBack;
    TextInputLayout feedingTime,feedDate;
    TextInputEditText edtScheduleName, edtPortionSize, edtFeedingTime,edtFeedDate;
    SwitchCompat switchWeekly,switchNotif;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        db = FirebaseFirestore.getInstance();

        currentSchedule = getIntent().getParcelableExtra("EDITSCHEDULE");
        if (currentSchedule != null) {
            fillform(currentSchedule);
        }
    }
    private void init(){
        imgBack = findViewById(R.id.imgBack);
        edtScheduleName = findViewById(R.id.edtscheduleName);
        edtPortionSize = findViewById(R.id.edtpetFood);
        edtFeedingTime = findViewById(R.id.edtFeedingTime);
        edtFeedDate = findViewById(R.id.edtFeedDate);
        feedingTime = findViewById(R.id.feedingTime);
        feedDate = findViewById(R.id.feedDate);
        switchWeekly = findViewById(R.id.switchWeekly);
        switchNotif = findViewById(R.id.switchNotif);
        btnSubmit = findViewById(R.id.btnSubmit);
    }
    private void fillform(Schedule schedule){
        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        String scheduleId = schedule.getId_User();

        if(userId.equals(scheduleId)){
            edtScheduleName.setText(schedule.getSchedule_Name());
            edtPortionSize.setText(schedule.getPortion());
            edtFeedDate.setText(schedule.getFeedDate());
            edtFeedingTime.setText(schedule.getFeedTime());
            switchNotif.setChecked(schedule.isNotification());
            switchWeekly.setChecked(schedule.isRepeat_weekly());
        }
    }
}