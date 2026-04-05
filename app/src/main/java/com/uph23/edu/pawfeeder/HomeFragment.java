package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.uph23.edu.pawfeeder.adapter.TaskAdapter;
import com.uph23.edu.pawfeeder.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HomeFragment extends Fragment {
    TextView txvMakanan, txvStatusMakan, txvMinuman, txvStatusMinum, txvUsername, btnToCreate;
    Button btnFeedNow;
    RecyclerView lsvTask;
    ImageView btnDone;
    Spinner spiConsumption;
    BarChart barChartConsumption;
    private static final String TAG = "HomeFragment";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ArrayList<Task> taskList;
    private TaskAdapter adapter;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);
        setupFirestore();
        readData();
        setupServoControl();
        loadSpinner();

        btnToCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toCreateTask();
            }
        });
        return view;
    }

    @Override public void onResume() { super.onResume(); loadTasks(); }

    public void toCreateTask(){
        Intent intent = new Intent (requireContext(), CreateTaskActivity.class);
        startActivity(intent);
    }

    public void init(View view) {
        txvMakanan = view.findViewById(R.id.txvMakanan);
        txvStatusMakan = view.findViewById(R.id.txvStatusMakan);
        txvMinuman = view.findViewById(R.id.txvMinuman);
        txvStatusMinum = view.findViewById(R.id.txvStatusMinum);
        txvUsername = view.findViewById(R.id.txvUsername);
        btnFeedNow = view.findViewById(R.id.btnFeed);
        btnDone = view.findViewById(R.id.btnDone);
        btnToCreate = view.findViewById(R.id.btnToCreate);
        lsvTask = view.findViewById(R.id.lsvTask);
        spiConsumption = view.findViewById(R.id.spiConsumption);
        barChartConsumption = view.findViewById(R.id.barChartConsumption);
    }

    public void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);

        lsvTask.setLayoutManager(new LinearLayoutManager(requireContext()));
        lsvTask.setAdapter(adapter);
    }

    public void loadTasks() {
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "User belum login");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("Task")
                .whereEqualTo("Id_User", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    taskList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Task task = doc.toObject(Task.class);
                        task.setDocId(doc.getId());
                        taskList.add(task);
                    }

                    Collections.sort(taskList, (t1, t2) -> {
                        return getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority());
                    });

                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Task loaded: " + taskList.size());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Gagal load task: ", e));
    }

    public int getPriorityValue(String priority) {
        switch (priority) {
            case "High":
                return 1;
            case "Medium":
                return 2;
            case "Low":
                return 3;
            default:
                return 99;
        }
    }
    public void setupServoControl() {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("pawfeeder");

        btnFeedNow.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/kendali_servo")
                            .setValue(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/kendali_servo")
                            .setValue(false);
                    return true;
            }
            return false;
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer stokMakanan = snapshot.child("makan/stok_makanan").getValue(Integer.class);
                    Integer stokMinuman = snapshot.child("minum/stok_minuman").getValue(Integer.class);

                    txvMakanan.setText(stokMakanan != null ? stokMakanan + "% left" : "N/A");
                    txvMinuman.setText(stokMinuman != null ? stokMinuman + "% left" : "N/A");

                    if (stokMakanan != null) {
                        if (stokMakanan > 50) {
                            txvStatusMakan.setText("Good");
                            txvStatusMakan.setTextColor(Color.GREEN);
                        } else {
                            txvStatusMakan.setText("Refill");
                            txvStatusMakan.setTextColor(Color.RED);
                        }
                    } else {
                        txvStatusMakan.setText("N/A");
                        txvStatusMakan.setTextColor(Color.GRAY);
                    }

                    if (stokMinuman != null) {
                        if (stokMinuman > 50) {
                            txvStatusMinum.setText("Good");
                            txvStatusMinum.setTextColor(Color.GREEN);
                        } else {
                            txvStatusMinum.setText("Refill");
                            txvStatusMinum.setTextColor(Color.RED);
                        }
                    } else {
                        txvStatusMinum.setText("N/A");
                        txvStatusMinum.setTextColor(Color.GRAY);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
    public void readData() {
        if (auth.getCurrentUser() == null) {
            txvUsername.setText("Hi, Guest");
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String username = document.getString("Username");
                        txvUsername.setText("Hi, " + username);
                    } else {
                        txvUsername.setText("Hi, User");
                    }
                })
                .addOnFailureListener(e -> txvUsername.setText("Hi, User"));
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
}
