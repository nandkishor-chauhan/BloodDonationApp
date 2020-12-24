package com.nandkishor.adityabloodcross;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    EditText userName;
    String email;
    boolean found = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        userName = findViewById(R.id.userName);
        Button reset = findViewById(R.id.reset);

        progressDialog = new ProgressDialog(ResetPasswordActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Processing...");

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new CommonAdapter(ResetPasswordActivity.this).isNetworkAvailable()){
                    if (TextUtils.isEmpty(userName.getText().toString())){
                        userName.setError("required");
                        return;
                    }
                    progressDialog.show();
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users Log");
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()){
                                for (DataSnapshot d : ds.getChildren()){
                                    if (Objects.equals(d.child("Reg or Emp ID").getValue(), userName.getText().toString().trim())){
                                        email = (String) d.child("Email").getValue();
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (found){
                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Toast.makeText(ResetPasswordActivity.this,"Reset link sent to "+email.substring(0,4)+"*****.com",Toast.LENGTH_LONG).show();
                                                    if (getIntent().getStringExtra("Page").equals("HomePage")){
                                                        Intent intent = new Intent(ResetPasswordActivity.this,HomeActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }else if (getIntent().getStringExtra("Page").equals("LoginPage")){
                                                        Intent intent = new Intent(ResetPasswordActivity.this,LoginActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }
                                        });

                            }else {
                                progressDialog.cancel();
                                Toast.makeText(ResetPasswordActivity.this,"User not found",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.cancel();
                            Toast.makeText(ResetPasswordActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    ScrollView resetLayout = findViewById(R.id.resetLayout);
                    Snackbar.make(resetLayout,"No Internet",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
