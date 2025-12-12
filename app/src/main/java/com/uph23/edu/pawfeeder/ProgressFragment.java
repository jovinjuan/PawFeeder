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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressFragment extends Fragment {
    TextView txvStreak, txvDrink, txvFeed, txvRefill;
    Spinner spiConsumption;
    BarChart barChartConsumption;
    ImageView imgAutomationMaster, imgRefillNinja, imgSupplySteady, imgFullTank, imgFirstBite, imgOnTheClock;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "Progress Fragment";
    private long lastFoodStock = 0;
    private long lastDrinkStock = 0;
    private boolean isFoodStockInitialized = false;
    private boolean isDrinkStockInitialized = false;


    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadStock();
        calculateStreak();
        loadSpinner();
        loadStats();


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

    private void loadStats() {
        loadConsumption();
        loadStreak();
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
                            txvFeed.setText(String.valueOf((int) totalFeed));
                            txvDrink.setText(String.valueOf((int) totalDrink));
                        } else {
                            Log.e(TAG, "Gagal mendapatkan data total konsumsi dari Firestore.", task.getException());
                            txvFeed.setText("Err");
                            txvDrink.setText("Err");
                        }
                    }
                });
    }

    private void loadCharts(String type) {
        db.collection("Daily_Consumption")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(7)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {


                        barChartConsumption.clear();
                        barChartConsumption.invalidate();

                        List<BarEntry> barEntryList = new ArrayList<>();
                        List<String> labels = new ArrayList<>();
                        int index = 0;


                        String valueField = type.equals("food") ? "food" : "drink";
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Double value = doc.getDouble(valueField);
                            Timestamp times = doc.getTimestamp("timestamp");

                            if (value != null && times != null) {
                                Date date = times.toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                                String labelWaktu = sdf.format(date);

                                barEntryList.add(new BarEntry(index, value.floatValue()));
                                labels.add(labelWaktu);
                                index++;
                            }
                        }

                        if (barEntryList.isEmpty()) {
                            barChartConsumption.clear();
                            barChartConsumption.setNoDataText("Belum ada data untuk ditampilkan.");
                            barChartConsumption.invalidate();
                            return;
                        }


                        int color = type.equals("food") ? Color.parseColor("#FF9800") : Color.parseColor("#2196F3");
                        String labelText = type.equals("food") ? "Makanan (gram)" : "Minuman (ml)";

                        BarDataSet dataSet = new BarDataSet(barEntryList, labelText);
                        dataSet.setColors(color);
                        dataSet.setValueTextSize(12f);
                        dataSet.setValueTextColor(Color.BLACK);

                        BarData barData = new BarData(dataSet);
                        barData.setBarWidth(0.8f);
                        barChartConsumption.setData(barData);


                        barChartConsumption.setDrawBarShadow(false);
                        barChartConsumption.getDescription().setEnabled(false);
                        barChartConsumption.setDrawGridBackground(false);
                        barChartConsumption.setPinchZoom(false);
                        barChartConsumption.getDescription().setText(type.equals("food") ? "Konsumsi Makanan Harian" : "Konsumsi Minuman Harian");




                        XAxis xAxis = barChartConsumption.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setDrawGridLines(false);


                        xAxis.setLabelCount(labels.size(), true);
                        xAxis.setAxisMinimum(-0.5f);
                        xAxis.setAxisMaximum(labels.size() - 0.5f);


                        xAxis.setDrawLabels(true);
                        xAxis.setTextColor(Color.DKGRAY);
                        xAxis.setTextSize(10f);
                        xAxis.setLabelRotationAngle(45f);


                        barChartConsumption.getAxisRight().setEnabled(false);
                        barChartConsumption.getAxisLeft().setGranularity(10f);
                        barChartConsumption.getAxisLeft().setAxisMinimum(0f);
                        barChartConsumption.setExtraBottomOffset(25f);
                        barChartConsumption.setExtraLeftOffset(5f);

                        barChartConsumption.notifyDataSetChanged();

                        barChartConsumption.animateY(1200);
                        barChartConsumption.setFitBars(true);
                        barChartConsumption.invalidate();
                        Log.i(TAG, "Chart berhasil dimuat dengan " + barEntryList.size() + " entri.");

                    }
                });

    }

    private void loadSpinner() {
        String[] items = {"Food", "Water"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, items);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiConsumption.setAdapter(arrayAdapter);

        spiConsumption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    loadCharts("food");
                } else {
                    loadCharts("drink");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
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

}