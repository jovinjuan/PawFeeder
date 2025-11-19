package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class ProfileFragment extends Fragment {
    ImageView imgProfile;
    LinearLayout btnAccount, btnLogout;
    TextView txvNama, txvTitle, txvLevel, txvCurrentLevel, txvXp, txvLevelProgress, txvViewAll, txvNoBadges,txvBadgesName, txvBadgesProgress;
    RecyclerView recBadges;
    ProgressBar pro_level,proBadges;
    SwitchCompat switchNotif;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    private static final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDataUser();
        loadDataProgress();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                toLogin();
            }
        });
        btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toAccount();
            }
        });


    }
    @Override
    public void onResume() {
        super.onResume();
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);
        return view;
    }
    private void init(View view){
        imgProfile = view.findViewById(R.id.imgProfile);
        btnAccount = view.findViewById(R.id.btnAccount);
        switchNotif = view.findViewById(R.id.switchNotif);
        btnLogout = view.findViewById(R.id.btnLogout);
        txvNama = view.findViewById(R.id.txvNama);
        txvTitle = view.findViewById(R.id.txvTitle);
        txvLevel = view.findViewById(R.id.txvLevel);
        txvCurrentLevel = view.findViewById(R.id.txvCurrentLevel);
        txvXp = view.findViewById(R.id.txvXp);
        txvLevelProgress = view.findViewById(R.id.txvLevelProgress);
        txvViewAll = view.findViewById(R.id.txvViewAll);
        txvNoBadges = view.findViewById(R.id.txvNoBadges);
        txvBadgesName = view.findViewById(R.id.txvBadgesName);
        txvBadgesProgress = view.findViewById(R.id.txvBadgesProgress);
        recBadges = view.findViewById(R.id.recBadges);
        pro_level = view.findViewById(R.id.pro_Level);
        proBadges = view.findViewById(R.id.proBadges);
    }
    public void loadDataUser(){
        String uid = user.getUid();
        db.collection("Users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                txvNama.setText(document.get("Username").toString());
                                Log.d("users", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("users", "Error getting document.", task.getException());
                        }
                    }
                });
    }
    private void loadDataProgress(){
        String uid = user.getUid();
        db.collection("Exp").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                String level = document.get("Level").toString();
                                String expInLevel = document.get("Jumlah_Exp").toString();
                                String expNext = document.get("ExpNextLevel").toString();
                                String title = document.get("Title").toString();

                                txvLevel.setText("Level " + level);
                                txvCurrentLevel.setText("Level " + level);
                                txvXp.setText(expInLevel + " Exp");
                                txvTitle.setText(title);
                                txvLevelProgress.setText(expInLevel + " / " + expNext + " Xp to Level " + (Integer.parseInt(level) + 1));

                                int progress = (int) ((Double.parseDouble(expInLevel) * 100) / Double.parseDouble(expNext));
                                pro_level.setProgress(progress);

                                Log.d("progress", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("progress", "Error getting document.", task.getException());
                        }
                    }
                });
    }
    private void toLogin(){
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        startActivity(intent);
    }
    private void toAccount(){
       Intent intent = new Intent(requireContext(), AccountActivity.class);
       startActivity(intent);
    }
}