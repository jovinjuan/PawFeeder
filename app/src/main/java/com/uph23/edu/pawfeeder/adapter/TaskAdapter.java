package com.uph23.edu.pawfeeder.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.uph23.edu.pawfeeder.R;
import com.uph23.edu.pawfeeder.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> taskList;

    public TaskAdapter(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.txvTitle.setText(task.getTitle());
        holder.txvDescription.setText(task.getDescription());

        int colorTask;
        switch (task.getPriority()) {
            case "High":
                colorTask = R.color.red_priority;
                break;
            case "Medium":
                colorTask = R.color.orange_priority;
                break;
            case "Low":
            default:
                colorTask = R.color.green_priority;
                break;
        }

        int color = ContextCompat.getColor(holder.itemView.getContext(), colorTask);

        holder.btnDone.setBackgroundColor(color);

        holder.btnDone.setOnClickListener(v -> {
            deleteTask(task, holder.getAdapterPosition());
        });

    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txvTitle, txvDescription;
        ImageView btnDone;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txvTitle = itemView.findViewById(R.id.txvTitle);
            txvDescription = itemView.findViewById(R.id.txvDescription);
            btnDone = itemView.findViewById(R.id.btnDone);
        }
    }

    public void deleteTask(Task task, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // HAPUS TASK
        db.collection("Task")
                .document(task.getDocId())
                .delete()
                .addOnSuccessListener(unused -> {

                    // Setelah delete → Tambah EXP
                    addExp(userId, task.getPriority());

                    // Hapus dari list local
                    taskList.remove(position);
                    notifyItemRemoved(position);

                })
                .addOnFailureListener(e ->
                        Log.e("TaskAdapter", "Gagal delete task", e)
                );
    }

    private void addExp(String userId, String priority) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference expRef = db.collection("Exp").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(expRef);

            long currentExp = 0L;
            if (snapshot.exists() && snapshot.contains("Jumlah_Exp")) {
                Long cur = snapshot.getLong("Jumlah_Exp");
                if (cur != null) currentExp = cur;
            }

            int expGain;
            switch (priority) {
                case "High": expGain = 30; break;
                case "Medium": expGain = 20; break;
                default: expGain = 10; break;
            }

            long newExp = currentExp + expGain;

            Map<String, Object> data = new HashMap<>();
            data.put("Jumlah_Exp", newExp);

            transaction.set(expRef, data, SetOptions.merge());
            return null;
        }).addOnSuccessListener(unused ->
                Log.d("EXP", "EXP updated for user " + userId)
        ).addOnFailureListener(e ->
                Log.e("EXP", "Failed to update exp", e)
        );
    }


}
