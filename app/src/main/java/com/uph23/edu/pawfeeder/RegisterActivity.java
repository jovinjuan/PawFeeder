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

        txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toLogin();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString();
                String confirmPassword =  edtConfirmPassword.getText().toString().trim();
                String username =  edtUsername.getText().toString().trim();

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

                register(email, password);
                registerUser(username, email);
            }
        });

    }

    public void init(){
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        txtSignIn = findViewById(R.id.txtSignIn);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    public void toLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void registerUser(String username, String email){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> Users = new HashMap<>();
        Users.put("Username", username);
        Users.put("Email", email);


// Add a new document with a generated ID
        db.collection("Users")
                .add(Users)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("USERS", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("USERS", "Error adding document", e);
                    }
                });
    }

    public void register(String email, String password){
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Register", "RegisterWithCustomToken:success");
                            toLogin();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Register", "RegisterWithCustomToken:failure", task.getException());
                        }
                    }
                });
    }
}