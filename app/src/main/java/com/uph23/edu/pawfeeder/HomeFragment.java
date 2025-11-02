package com.uph23.edu.pawfeeder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeFragment extends Fragment {
    TextView txvMakanan, txvStatusMakan, txvMinuman, txvStatusMinum, txvBattery;
    Button btnFeedNow;

    Boolean servo = false;
    private static final String TAG = "HomeFragment";

    public HomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        init(view);

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the root database reference
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("pawfeeder");

        // --- A. Button OnTouchListener for Servo Control ---
        btnFeedNow.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/servo")
                            .setValue(true);
                    Log.d("ServoControl", "Servo ON");
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/servo")
                            .setValue(false);
                    Log.d("ServoControl", "Servo OFF");
                    return true;
            }
            return false;
        });
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Update Food Stock
                    Integer stokMakanan = dataSnapshot.child("makan/stok_makanan").getValue(Integer.class);
                    if (stokMakanan != null) {
                        txvMakanan.setText(stokMakanan + "% left");
                    } else {
                        txvMakanan.setText("N/A"); // Handle missing data gracefully
                    }

                    // Update Water Stock
                    Integer stokMinuman = dataSnapshot.child("minum/stok_minuman").getValue(Integer.class);
                    if (stokMinuman != null) {
                        txvMinuman.setText(stokMinuman + "% left");
                    } else {
                        txvMinuman.setText("N/A"); // Handle missing data gracefully
                    }

                    // Update Battery
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
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    private void init(View view){
        // In a Fragment, you must call findViewById on the root View object ('view')
        txvMakanan = view.findViewById(R.id.txvMakanan);
        txvStatusMakan = view.findViewById(R.id.txvStatusMakan);
        txvMinuman = view.findViewById(R.id.txvMinuman);
        txvStatusMinum = view.findViewById(R.id.txvStatusMinum);
        txvBattery = view.findViewById(R.id.txvBattery);
        btnFeedNow = view.findViewById(R.id.btnFeedNow);
    }
}