package com.uph23.edu.pawfeeder;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateScheduleActivity extends AppCompatActivity {
    ImageView imgBack;
    TextInputLayout feedingTime,feedDate;
    TextInputEditText edtScheduleName, edtPortionSize, edtFeedingTime,edtFeedDate;
    SwitchCompat switchNotif;
    Button btnSubmit;
    private String selectedTime = "08:00";
    private String selectedSpecificDate = "";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_schedule);
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
        edtFeedingTime.setOnClickListener(v -> setFeedTime());
        feedingTime.setOnClickListener(v -> setFeedTime());

        edtFeedDate.setOnClickListener(v -> setFeedDate());
        feedDate.setOnClickListener(v -> setFeedDate());
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String schedulename = edtScheduleName.getText().toString().trim();
                String portion = edtPortionSize.getText().toString().trim();
                String feedtime = edtFeedingTime.getText().toString().trim();
                String feeddate = edtFeedDate.getText().toString().trim();
                boolean notification = switchNotif.isChecked();

                createSchedule(schedulename,portion,feedtime,feeddate,notification);
            }
        });
    }
   private void init(){
        imgBack = findViewById(R.id.imgBack);
        edtScheduleName = findViewById(R.id.edtscheduleName);
        edtPortionSize = findViewById(R.id.edtpetFood);
        edtFeedingTime = findViewById(R.id.edtFeedingTime);
        edtFeedDate = findViewById(R.id.edtFeedDate);
        feedingTime = findViewById(R.id.feedingTime);
        feedDate = findViewById(R.id.feedDate);
        switchNotif = findViewById(R.id.switchNotif);
        btnSubmit = findViewById(R.id.btnSubmit);
   }
   private void createSchedule(String schedulename, String portion, String feedtime, String feeddate, boolean notification){
       db = FirebaseFirestore.getInstance();
       mAuth = FirebaseAuth.getInstance();
       String userId = mAuth.getCurrentUser().getUid();
       if(schedulename.isEmpty()){
           edtScheduleName.setError("Schedule Name must be filled");
           edtScheduleName.requestFocus();
           return;
       }
       if(portion.isEmpty()){
           edtPortionSize.setError("Portion Size must be filled");
           edtPortionSize.requestFocus();
           return;
       }
       if(feedtime.equals("") || feedtime.equals("Pilih Waktu") ){
           edtFeedingTime.setError("Feed Time must be set");
           edtFeedingTime.requestFocus();
           return;
       }
       if(feeddate.equals("") || feeddate.equals("Pilih Tanggal") ){
           edtFeedDate.setError("Feed Time must be set");
           edtFeedDate.requestFocus();
           return;
       }

       Map<String, Object> schedule = new HashMap<>();
       schedule.put("Id_User", userId);
       schedule.put("Schedule_Name", schedulename);
       schedule.put("Portion", portion);
       schedule.put("FeedTime", feedtime);
       schedule.put("FeedDate",feeddate);
       schedule.put("notification", notification);

       db.collection("Schedule").add(schedule).addOnSuccessListener(documentReference -> {
           DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("pawfeeder/autofeed");
           String key = documentReference.getId();
           Map<String, Object> autoData = new HashMap<>();
           autoData.put("feed_date", feeddate);
           autoData.put("feed_time", feedtime);
           autoData.put("portion",portion);
           myRef.child(key).setValue(autoData);

           Toast.makeText(this, "Feeding Schedule has been set", Toast.LENGTH_SHORT).show();
           finish();
       }).addOnFailureListener(e -> {
           Toast.makeText(this, "Failed to add" + e.getMessage(), Toast.LENGTH_LONG).show();
       });



   }
   private void setFeedTime(){
       Calendar calendar = Calendar.getInstance();
       int hour = calendar.get(Calendar.HOUR_OF_DAY);
       int minute = calendar.get(Calendar.MINUTE);

       // Parse waktu yang sudah ada (jika ada)
       if (!selectedTime.equals("")) {
           String[] parts = selectedTime.split(":");
           hour = Integer.parseInt(parts[0]);
           minute = Integer.parseInt(parts[1]);
       }

       MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
               .setTimeFormat(TimeFormat.CLOCK_24H)     // 24 jam (Indonesia standard)
               .setHour(hour)
               .setMinute(minute)
               .setTitleText("Choose Feeding Time")
               .build();

       timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               int selectedHour = timePicker.getHour();
               int selectedMinute = timePicker.getMinute();

               // Format jadi String HH:mm (2 digit)
               selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

               // Tampilkan ke EditText
               edtFeedingTime.setText(selectedTime);
           }
       });
       timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
   }
   private void setFeedDate(){
       Calendar cal = Calendar.getInstance();
       MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
               .setTitleText("Choose Date")
               .setSelection(cal.getTimeInMillis())
               .build();

       datePicker.addOnPositiveButtonClickListener(selection -> {
           String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                   .format(new Date(selection));
           selectedSpecificDate = dateStr;
           edtFeedDate.setText(dateStr);
       });

       datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
   }
}
