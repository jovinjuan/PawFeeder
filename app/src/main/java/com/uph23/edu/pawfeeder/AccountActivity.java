package com.uph23.edu.pawfeeder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {
    ImageView imgBack, imgProfile, imgEditUsername, imgEditEmail;
    Button btnSaveAcc, btnChangePass;
    TextView txvUsername, txvEmail;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        loadData();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        imgEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentUsername = txvUsername.getText().toString();
                edit("Edit Username", currentUsername, "username", txvUsername);
            }
        });
        imgEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentEmail = txvEmail.getText().toString();
                edit("Edit Email", currentEmail, "email", txvEmail);
            }
        });
        btnSaveAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nama = txvUsername.getText().toString();
                String email = txvEmail.getText().toString();
                saveAcc(nama, email);
            }
        });
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePassword();
            }
        });

    }

    private void init() {
        imgBack = findViewById(R.id.imgBack);
        imgProfile = findViewById(R.id.imgProfile);
        imgEditUsername = findViewById(R.id.imgEditUsername);
        imgEditEmail = findViewById(R.id.imgEditEmail);
        btnSaveAcc = findViewById(R.id.btnSaveAcc);
        btnChangePass = findViewById(R.id.btnChangePass);
        txvUsername = findViewById(R.id.txvUsername);
        txvEmail = findViewById(R.id.txvEmail);
    }

    private void loadData() {
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                txvUsername.setText(document.get("Username").toString());
                                txvEmail.setText(document.get("Email").toString());
                            }
                        } else {
                            Log.w("USER", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void edit(String title, String currentValue, String fieldKey, TextView txvTarget) {
        // 1. Inflate Layout Custom Dialog Anda
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_alertdialog, null);

        // 2. Inisialisasi Views di dalam Dialog
        TextView dialogTitle = dialogView.findViewById(R.id.txvDialogTitle);
        TextInputEditText edtNewValue = dialogView.findViewById(R.id.edtNewValue);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);

        dialogTitle.setText(title);
        edtNewValue.setText(currentValue);

        // 3. Atur Input Type dan Filter sesuai Jenis Field
        if (fieldKey.equals("email")) {
            // Untuk Email
            edtNewValue.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else if (fieldKey.equals("username")) {
            // Untuk Username, batasi panjang dan input
            edtNewValue.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            // Contoh: edtNewValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        }

        // 4. Bangun Dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newValue = edtNewValue.getText().toString();
                if (!newValue.isEmpty()) {
                    txvTarget.setText(newValue);
                    dialog.dismiss();
                } else {
                    Toast.makeText(AccountActivity.this, "Nilai tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void saveAcc(String nama, String email) {
            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user.getUid();
            String currentEmail = user.getEmail();

            // Hanya update Firestore JIKA username berubah ATAU email sama
            if (nama.equals(txvUsername.getText().toString().trim()) && email.equals(currentEmail)) {
                Toast.makeText(this, "Tidak ada perubahan", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("Username", nama);
            updates.put("Email", email); // Simpan email baru di Firestore dulu

            // Update Firestore DULU
            db.collection("Users").document(uid).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (!email.equals(currentEmail)) {
                            // Baru panggil re-auth jika email berubah
                            reauthenticate(user, currentEmail, email, uid);
                        } else {
                            Toast.makeText(this, "Username berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            loadData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIRESTORE", "Gagal update Firestore", e);
                        Toast.makeText(this, "Gagal menyimpan data", Toast.LENGTH_SHORT).show();
                    });
        }
    private void reauthenticate(FirebaseUser user, String currentEmail, String newEmail, String uid) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_authentication, null);

        TextInputEditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        Button btnConfirm = dialogView.findViewById(R.id.btnSave);
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false);

        AlertDialog dialog = builder.create();

        imgClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String password = edtPassword.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(AccountActivity.this, "Masukkan password", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(AccountActivity.this, "Memverifikasi password...", Toast.LENGTH_SHORT).show();

            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();  // Baru dismiss di sini
                        updateEmail(user, newEmail, uid);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("REAUTH", "Gagal re-auth: " + e.getMessage());


                        mAuth.signInWithEmailAndPassword(currentEmail, password)
                                .addOnSuccessListener(authResult -> {
                                    dialog.dismiss();
                                    updateEmail(mAuth.getCurrentUser(), newEmail, uid);
                                })
                                .addOnFailureListener(ex -> {
                                    dialog.dismiss();  // Pastikan dialog tutup
                                    Toast.makeText(AccountActivity.this, "Password salah! Login ulang diperlukan.", Toast.LENGTH_LONG).show();
                                    logout();
                                });
                    });
        });

        dialog.show();
    }

    private void updateEmail(FirebaseUser user, String email,String uid) {
        user.verifyBeforeUpdateEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Link verifikasi dikirim ke " + email + "\nCek inbox/spam!", Toast.LENGTH_LONG).show();

                    // Tampilkan dialog instruksi
                    showVerificationDialog(email);
                })
                .addOnFailureListener(e -> {
                    Log.e("VERIFY_EMAIL", "Gagal kirim verifikasi", e);
                    Toast.makeText(this, "Gagal kirim link: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    rollbackFirestoreEmail(uid, user.getEmail());
                });
    }
    private void rollbackFirestoreEmail(String uid, String oldEmail) {
        Map<String, Object> rollback = new HashMap<>();
        rollback.put("Email", oldEmail);
        db.collection("Users").document(uid).update(rollback)
                .addOnSuccessListener(a -> Log.d("ROLLBACK", "Email Firestore dikembalikan ke " + oldEmail))
                .addOnFailureListener(e -> Log.e("ROLLBACK", "Gagal rollback Firestore", e));
    }
    private void showVerificationDialog(String newEmail) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Verifikasi Email Asli")
                .setMessage("Link verifikasi telah dikirim ke:\n\n" + newEmail +
                        "\n\n1. Buka email kamu (Gmail/Outlook)\n" +
                        "2. Cek folder Inbox atau Spam\n" +
                        "3. Klik tombol 'Verify Email' atau link biru\n" +
                        "4. Tutup app, buka lagi → email otomatis berubah!")
                .setPositiveButton("Saya sudah klik link", (dialog, which) -> {
                    // Force refresh user
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.reload().addOnSuccessListener(aVoid -> {
                            loadData(); // refresh UI
                            if (user.getEmail().equals(newEmail)) {
                                Toast.makeText(this, "Email berhasil diubah!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Belum diverifikasi. Coba lagi nanti.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Kirim ulang link", (dialog, which) -> {
                    mAuth.getCurrentUser().verifyBeforeUpdateEmail(newEmail);
                    Toast.makeText(this, "Link dikirim ulang!", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }
    private void changePassword(){
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_changepassword, null);

        TextInputEditText edtPassword = dialogView.findViewById(R.id.edtPassword);
        TextInputEditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        ImageView imgClose = dialogView.findViewById(R.id.imgClose);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false);

        AlertDialog dialog = builder.create();

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPassword = edtPassword.getText().toString();
                String newPassword = edtNewPassword.getText().toString();

                if(currentPassword.isEmpty()||newPassword.isEmpty()){
                    Toast.makeText(AccountActivity.this, "Semua password wajib diisi", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<6){
                    Toast.makeText(AccountActivity.this, "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(currentPassword.equals(newPassword)){
                    Toast.makeText(AccountActivity.this, "Password harus berbeda", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(AccountActivity.this, "Memverifikasi password ...", Toast.LENGTH_SHORT).show();

                FirebaseUser user = mAuth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(),currentPassword);
                user.reauthenticate(credential)
                        .addOnSuccessListener(aVoid ->{
                            updatePassword(user,newPassword);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("REAUTH_PASS", "Gagal re-auth: " + e.getMessage());
                            mAuth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                                    .addOnSuccessListener(auth -> updatePassword(mAuth.getCurrentUser(), newPassword))
                                    .addOnFailureListener(ex -> {
                                        Toast.makeText(AccountActivity.this, "Password salah", Toast.LENGTH_SHORT).show();
                                        logout();
                                    });
                        });
            }
        });
        dialog.show();
    }
    private void updatePassword(FirebaseUser user, String newPassword){
        user.updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                    logout();
                })
                .addOnFailureListener(e -> {
                    Log.e("UPDATE_PASS", "Gagal update password", e);
                    Toast.makeText(this, "Gagal ubah password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    private void logout(){
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}