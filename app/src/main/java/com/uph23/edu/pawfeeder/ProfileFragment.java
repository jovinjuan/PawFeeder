package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class ProfileFragment extends Fragment {
    ImageView imgProfile, imgAccount, imgNotif, imgDevice, imgLogout;
    TextView txvNama, txvTitle, txvLevel, txvCurrentLevel, txvXp, txvLevelProgress, txvViewAll, txvNoBadges,txvBadgesName, txvBadgesProgress;
    RecyclerView recBadges;
    ProgressBar pro_level,proBadges;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDataUser();

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                toLogin();
            }
        });
        imgAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toAccount();
            }
        });


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
        imgAccount = view.findViewById(R.id.imgAccount);
        imgNotif = view.findViewById(R.id.imgNotif);
        imgDevice = view.findViewById(R.id.imgDevice);
        imgLogout = view.findViewById(R.id.imgLogout);
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
        db.collection("Users")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                txvNama.setText(document.get("Username").toString());
                                Log.d("users", document.getId() + " => " + document.getData());
                            }
                        }
                        else{
                            Log.w("users", "Error getting documents.", task.getException());
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