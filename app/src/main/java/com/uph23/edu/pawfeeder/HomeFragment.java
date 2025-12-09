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
    Button btnFeedNow, btnStream;
    RecyclerView lsvTask;
    ImageView btnDone;
    WebView imgCamera;
    private static final String TAG = "HomeFragment";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ArrayList<Task> taskList;
    private TaskAdapter adapter;
    private static final long REFRESH_INTERVAL = 100;
    private String cameraIp = "192.168.18.160";
    private DatabaseReference cameraRef;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);
        initCameraIp();
        setupFirestore();
        readData();
        setupServoControl();
        liveCamera();

        btnToCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toCreateTask();
            }
        });
        startStream();



        return view;
    }

    @Override public void onResume() { super.onResume(); loadTasks(); liveCamera();  }

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
        imgCamera = view.findViewById(R.id.imgCamera);
        btnFeedNow = view.findViewById(R.id.btnFeed);
        btnStream = view.findViewById(R.id.btnStream);
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

    public void startStream() {
        DatabaseReference streamRef = FirebaseDatabase.getInstance().getReference("pawfeeder/camera/stream_status");

        final boolean[] isStreaming = {false};

        streamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean status = snapshot.getValue(Boolean.class);
                if (status != null) {
                    isStreaming[0] = status;
                    updateButtonUi(isStreaming[0]);
                    if (isStreaming[0]) {
                        liveCamera();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Gagal membaca status stream awal dari Firebase", error.toException());
            }
        });

        btnStream.setOnClickListener(v -> {
            isStreaming[0] = !isStreaming[0];

            streamRef.setValue(isStreaming[0])
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Perintah Stream dikirim ke Firebase: " + isStreaming[0]);
                        updateButtonUi(isStreaming[0]);

                        if (isStreaming[0]) {
                            liveCamera();
                        } else {
                            if (imgCamera != null) {
                                imgCamera.stopLoading();
                                imgCamera.loadUrl("about:blank");
                                imgCamera.setBackgroundColor(Color.BLACK);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Gagal mengirim perintah Stream ke Firebase", e);
                        // Kembalikan status jika gagal
                        isStreaming[0] = !isStreaming[0];
                        updateButtonUi(isStreaming[0]); // Perbarui UI kembali ke status lama
                    });
        });
    }

    private void updateButtonUi(boolean streaming) {
        if (streaming) {
            btnStream.setText("STOP STREAMING");
            btnStream.setBackgroundColor(Color.parseColor("#FF4081"));
        } else {
            btnStream.setText("START STREAM");
            btnStream.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
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
    private void liveCamera() {
        if (imgCamera == null) {
            Log.e(TAG, "WebView imgCamera NULL! Tidak bisa load stream.");
            return;
        }

        Log.i(TAG, "Memulai load kamera → http://" + cameraIp + "/stream");

        // Setup WebView
        imgCamera.getSettings().setUserAgentString("Mozilla/5.0 ESP32-CAM");
        imgCamera.getSettings().setJavaScriptEnabled(true);
        imgCamera.getSettings().setLoadWithOverviewMode(true);
        imgCamera.getSettings().setUseWideViewPort(true);
        imgCamera.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_NO_CACHE);
        imgCamera.setBackgroundColor(Color.BLACK);

        imgCamera.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.w(TAG, "Mulai menghubungi ESP32-CAM: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "KAMERA BERHASIL TERKONEKSI & MENAMPILKAN STREAM! → " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "GAGAL TERKONEKSI KE KAMERA! Error " + errorCode + ": " + description);
                Log.e(TAG, "URL yang gagal: " + failingUrl);

                // Auto retry setelah 4 detik
                new Handler().postDelayed(() -> {
                    Log.w(TAG, "Mencoba reconnect ke kamera...");
                    liveCamera();
                }, 4000);
            }

            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                int statusCode = errorResponse.getStatusCode();
                Log.e(TAG, "HTTP ERROR dari ESP32: " + statusCode + " → " + errorResponse.getReasonPhrase());
                if (statusCode == 404) {
                    Log.e(TAG, "Endpoint tidak ada! Coba ganti /stream → /mjpeg/1 atau sebaliknya");
                }
            }
        });

        String streamUrl = "http://" + cameraIp + ":81/stream";
        imgCamera.loadUrl(streamUrl);
        Log.d(TAG, "loadUrl dipanggil: " + streamUrl);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (imgCamera != null) {
            imgCamera.stopLoading();
            imgCamera.loadUrl("about:blank");
            imgCamera.clearHistory();
            imgCamera.clearCache(true);
            imgCamera = null;
        }
    }
    private void initCameraIp() {
        cameraRef = FirebaseDatabase.getInstance().getReference("pawfeeder/camera/camera_ip");
        cameraRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ip = snapshot.getValue(String.class);
                if (ip != null && !ip.isEmpty()) {
                    if (!ip.equals(cameraIp)) {
                        Log.w(TAG, "IP KAMERA BERUBAH! Dari " + cameraIp + " → " + ip);
                        cameraIp = ip;
                        restartLiveCamera();
                    } else {
                        Log.d(TAG, "IP kamera tetap: " + ip);
                    }
                } else {
                    Log.e(TAG, "IP kamera di Firebase kosong atau null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Gagal baca camera_ip dari Firebase", error.toException());
            }
        });
    }

    private void restartLiveCamera() {
        if (imgCamera != null) {
            imgCamera.loadUrl("about:blank");
            liveCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imgCamera != null) {
            imgCamera.stopLoading();
        }
    }
}
