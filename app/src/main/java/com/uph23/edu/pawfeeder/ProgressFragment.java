package com.uph23.edu.pawfeeder;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    TextView txvStreak, txvDrink, txvFeed, txvRefill;
    Spinner spiConsumption;
    BarChart barChartConsumption;
    ImageView imgAutomationMaster, imgRefillNinja, imgSupplySteady, imgFullTank, imgFirstBite, imgOnTheClock;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "Progress Fragment";

    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadStats();
        loadSpinner();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        init(view);
        return view;
    }
    private void init(View view){
        txvStreak = view.findViewById(R.id.txvStreak);
        txvDrink = view.findViewById(R.id.txvDrink);
        txvFeed = view.findViewById(R.id.txvFeed);
        txvRefill = view.findViewById(R.id.txvRefill);
        spiConsumption = view.findViewById(R.id.spiConsumption);
        barChartConsumption = view.findViewById(R.id.barChartConsumption);
        imgAutomationMaster = view.findViewById(R.id.imgAutomationMaster);
        imgRefillNinja = view.findViewById(R.id.imgRefillNinja);
        imgSupplySteady = view.findViewById(R.id.imgSupplySteady);
        imgFirstBite = view.findViewById(R.id.imgFirstBite);
        imgFullTank = view.findViewById(R.id.imgFullTank);
        imgOnTheClock = view.findViewById(R.id.imgOnTheClock);
    }
    private void loadStats(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("pawfeeder");
        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    Log.d(TAG, "Data belum ada");
                }
                // Baca nilai langsung per child
                Integer totalFeed   = dataSnapshot.child("makan/jlh_keluar").getValue(Integer.class);
                Integer totalDrink  = dataSnapshot.child("minum/jlh_konsumsi").getValue(Integer.class);
                Integer totalRefill = dataSnapshot.child("refill").getValue(Integer.class);
                Integer currentStreak = dataSnapshot.child("streak").getValue(Integer.class);

                txvFeed.setText(String.valueOf(totalFeed));
                txvDrink.setText(String.valueOf(totalDrink));
                txvRefill.setText(String.valueOf(totalRefill));
                txvStreak.setText(String.valueOf(currentStreak));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    private void loadCharts(String type){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){return;}
        String uid = user.getUid();
        db.collection("Daily_Consumption")
                .whereEqualTo("user_id",uid)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(7)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        // --- 1. DEKLARASI DATA SET ---
                        List<BarEntry> barEntryList = new ArrayList<>();
                        List<String> labels = new ArrayList<>(); // List untuk Label Sumbu X (Tanggal)
                        int index = 0;

                        // Flag untuk menentukan field yang akan diambil
                        String valueField = type.equals("food") ? "food" : "drink";

                        // --- 2. PROSES DATA (LOOP HANYA UNTUK MENGAMBIL DATA) ---
                        for(DocumentSnapshot doc : queryDocumentSnapshots) {
                            // Ambil field berdasarkan tipe (food/drink)
                            Double value = doc.getDouble(valueField);
                            Timestamp times = doc.getTimestamp("timestamp");

                            if (value != null && times != null) {
                                // Format Tanggal (gunakan "dd MMM" untuk label yang lebih ringkas)
                                Date date = times.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
                                String labelWaktu = sdf.format(date);

                                // Tambahkan Entry: X-axis adalah Index, Y-axis adalah Value
                                barEntryList.add(new BarEntry(index, value.floatValue())); // Gunakan floatValue()
                                labels.add(labelWaktu); // Simpan label untuk XAxis Formatter
                                index++;
                            }
                        }

                        // --- 3. LOGIKA JIKA DATA KOSONG ---
                        if(barEntryList.isEmpty()){
                            barChartConsumption.clear();
                            barChartConsumption.setNoDataText("Belum ada data untuk ditampilkan.");
                            barChartConsumption.invalidate();
                            Log.d(TAG, "Tidak ada data ditemukan untuk chart.");
                            return;
                        }

                        // --- 4. KONFIGURASI CHART (DI LUAR LOOP) ---

                        // A. Persiapan DataSet
                        int color = type.equals("food") ? Color.parseColor("#FF9800") : Color.parseColor("#2196F3");
                        String labelText = type.equals("food") ? "Makanan (gram)" : "Minuman (ml)";

                        BarDataSet dataSet = new BarDataSet(barEntryList, labelText);
                        dataSet.setColors(color);
                        dataSet.setValueTextSize(12f);
                        dataSet.setValueTextColor(Color.BLACK); // Agar nilai di atas bar terlihat jelas

                        BarData barData = new BarData(dataSet);
                        barData.setBarWidth(0.8f); // Lebar Bar
                        barChartConsumption.setData(barData);

                        // B. Konfigurasi Visual Umum
                        barChartConsumption.setDrawBarShadow(false);
                        barChartConsumption.setDrawValueAboveBar(true);
                        barChartConsumption.getDescription().setEnabled(false); // Deskripsi di sudut kanan bawah
                        barChartConsumption.setDrawGridBackground(false);
                        barChartConsumption.setPinchZoom(false);
                        barChartConsumption.getDescription().setText(type.equals("food") ? "Konsumsi Makanan Harian" : "Konsumsi Minuman Harian");

                        // C. Konfigurasi Sumbu X
                        XAxis xAxis = barChartConsumption.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Menggunakan label yang sudah di-format
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f); // Penting agar setiap label muncul
                        xAxis.setDrawGridLines(false);
                        xAxis.setLabelCount(labels.size()); // Menjamin semua label ditampilkan

                        // D. Animasi dan Refresh
                        barChartConsumption.animateY(1200);
                        barChartConsumption.setFitBars(true); // Memastikan bar fit dengan sumbu X
                        barChartConsumption.invalidate(); // Refresh tampilan chart
                        Log.i(TAG, "Chart berhasil dimuat dengan " + barEntryList.size() + " entri.");

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Gagal memuat data chart dari Firestore: ", e);
                    barChartConsumption.setNoDataText("Gagal memuat data.");
                    barChartConsumption.invalidate();
                });
    }
    private void loadSpinner(){
        String [] items = {"Food","Water"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_item,items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiConsumption.setAdapter(arrayAdapter);

        spiConsumption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(position == 0){
                    loadCharts("food");
                }
                else{
                    loadCharts("drink");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}