package com.uph23.edu.pawfeeder;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uph23.edu.pawfeeder.model.Task;

import java.util.HashMap;
import java.util.Map;

public class CreateTaskActivity extends AppCompatActivity {

    EditText edtTitle, edtDescription;
    AutoCompleteTextView autoPriority;
    Button btnCreate;
    ImageView btnBack;

    String selectedPriority = "Medium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        autoPriority = findViewById(R.id.autoPriority);
        btnCreate = findViewById(R.id.btnCreate);
        btnBack = findViewById(R.id.btnBack);

        setupPriorityDropdown();

        btnCreate.setOnClickListener(v -> submitTask());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupPriorityDropdown() {
        final String[] priorities = {"High", "Medium", "Low"};

        // Adapter menggunakan custom item layout sehingga kita bisa style tiap baris
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.layout_item_dropdown, priorities) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                styleTextView((TextView) v, getItem(position));
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                styleTextView((TextView) v, getItem(position));
                return v;
            }

            private void styleTextView(TextView tv, String item) {
                if (tv == null || item == null) return;
                switch (item) {
                    case "High":
                        tv.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.red));
                        break;
                    case "Medium":
                        tv.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.orange));
                        break;
                    case "Low":
                        tv.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.green));
                        break;
                    default:
                        tv.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.black));
                }
            }
        };

        autoPriority.setAdapter(adapter);

        // Disable typing / keyboard and make it dropdown-only
        autoPriority.setInputType(0); // TYPE_NULL
        autoPriority.setKeyListener(null);
        autoPriority.setFocusable(false);
        autoPriority.setClickable(true);

        // show dropdown when clicked
        autoPriority.setOnClickListener(v -> autoPriority.showDropDown());

        // When an item is selected, change the text color of the field and set selectedPriority
        autoPriority.setOnItemClickListener((parent, view, position, id) -> {
            selectedPriority = (String) parent.getItemAtPosition(position);
            // change color in the main text field
            switch (selectedPriority) {
                case "High":
                    autoPriority.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.red));
                    break;
                case "Medium":
                    autoPriority.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.orange));
                    break;
                case "Low":
                    autoPriority.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.green));
                    break;
            }
            // set the chosen text into the view (false to avoid filtering)
            autoPriority.setText(selectedPriority, false);
        });

        // Set default shown value
        autoPriority.post(() -> {
            autoPriority.setText("Medium", false);
            autoPriority.setTextColor(ContextCompat.getColor(CreateTaskActivity.this, R.color.orange));
            selectedPriority = "Medium";
        });
    }

    private void submitTask() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Title required");
            return;
        }

        if (TextUtils.isEmpty(desc)) {
            edtDescription.setError("Description required");
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pilih salah satu: Simpan via model Task atau Map
        // Contoh pakai Map supaya field nama tepat sesuai di Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("Title", title);
        data.put("Description", desc);
        data.put("Id_User", uid);
        data.put("Priority", selectedPriority);

        FirebaseFirestore.getInstance()
                .collection("Task")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateTaskActivity.this, "Task created", Toast.LENGTH_SHORT).show();
                    finish(); // atau clear fields
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateTaskActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
