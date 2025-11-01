package com.uph23.edu.pawfeeder;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomePageActivity extends AppCompatActivity {
    TextView txvMakanan, txvStatusMakan, txvMinuman, txvStatusMinum, txvBattery;
    Button btnFeedNow;

    Boolean servo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("pawfeeder");

        btnFeedNow.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/servo")
                            .setValue(true);
                    Log.d("ServoControl", "Servo ON");
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/servo")
                            .setValue(false);
                    Log.d("ServoControl", "Servo OFF");
                    return true;
            }
            return false;
        });


        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (dataSnapshot.exists()) {
                    int stokMakanan = dataSnapshot.child("makan/stok_makanan").getValue(Integer.class);
                    txvMakanan.setText(stokMakanan + "% left");

                    int stokMinuman = dataSnapshot.child("minum/stok_minuman").getValue(Integer.class);
                    txvMinuman.setText(stokMinuman + "% left");

                    Long battery = dataSnapshot.child("baterai/persentase").getValue(Long.class);
                    if (battery != null) {
                        txvBattery.setText(battery + "%");
                    } else {
                        txvBattery.setText("0%");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void init(){
        txvMakanan = findViewById(R.id.txvMakanan);
        txvStatusMakan = findViewById(R.id.txvStatusMakan);
        txvMinuman = findViewById(R.id.txvMinuman);
        txvStatusMinum = findViewById(R.id.txvStatusMinum);
        txvBattery = findViewById(R.id.txvBattery);
        btnFeedNow = findViewById(R.id.btnFeedNow);



    }
}