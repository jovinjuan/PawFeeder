package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    TextView txtSignIn;
    Button btnSubmit;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        txtSignIn.setOnClickListener(v -> toLogin());

        btnSubmit.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();

            if (!password.equals(confirmPassword)) {
                edtPassword.setError("Password tidak sama!");
                edtConfirmPassword.setError("Password tidak sama!");
                edtConfirmPassword.requestFocus();
                return;
            }

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Log.w("Register", "Input tidak boleh kosong");
                return;
            }

            register(username, email, password);
        });
    }

    private void init() {
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        txtSignIn = findViewById(R.id.txtSignIn);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void toLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    private void register(String username, String email, String password) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Register", "createUserWithEmail:success");

                        // Ambil UID user yang baru dibuat
                        String userId = mAuth.getCurrentUser().getUid();

                        // Buat data user
                        Map<String, Object> user = new HashMap<>();
                        user.put("Username", username);
                        user.put("Email", email);

                        // Simpan ke Firestore dengan UID sebagai document ID
                        db.collection("Users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid ->
                                        Log.d("Register", "User added to Firestore: " + userId))
                                .addOnFailureListener(e ->
                                        Log.w("Register", "Error adding user to Firestore", e));

                        // Pindah ke login setelah register
                        toLogin();

                    } else {
                        Log.w("Register", "createUserWithEmail:failure", task.getException());
                    }
                });
    }
}