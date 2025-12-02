package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.uph23.edu.pawfeeder.adapter.TaskAdapter;
import com.uph23.edu.pawfeeder.model.Task;

import java.util.ArrayList;
import java.util.Collections;


public class HomeFragment extends Fragment {
    TextView txvMakanan, txvStatusMakan, txvMinuman, txvStatusMinum, txvBattery, txvUsername, btnToCreate;
    Button btnFeedNow;
    RecyclerView lsvTask;
    ImageView btnDone;
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
        txvBattery = view.findViewById(R.id.txvBattery);
        txvUsername = view.findViewById(R.id.txvUsername);
        btnFeedNow = view.findViewById(R.id.btnFeed);
        btnDone = view.findViewById(R.id.btnDone);
        btnToCreate = view.findViewById(R.id.btnToCreate);
        lsvTask = view.findViewById(R.id.lsvTask);
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
                            .getReference("pawfeeder/makan/servo")
                            .setValue(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    FirebaseDatabase.getInstance()
                            .getReference("pawfeeder/makan/servo")
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
                    Long battery = snapshot.child("baterai/persentase").getValue(Long.class);

                    txvMakanan.setText(stokMakanan != null ? stokMakanan + "% left" : "N/A");
                    txvMinuman.setText(stokMinuman != null ? stokMinuman + "% left" : "N/A");
                    txvBattery.setText(battery != null ? battery + "%" : "0%");
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


}
