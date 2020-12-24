package com.nandkishor.adityabloodcross;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText userName,password;
    String email,userType;
    boolean idFound = false;
    ProgressDialog progressDialog;
    SharedPreferences sp,token;
    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button loginButton = findViewById(R.id.login);
        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        sp = getSharedPreferences("Log",MODE_PRIVATE);
        ed = sp.edit();
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new CommonAdapter(LoginActivity.this).isNetworkAvailable()){
                    if (TextUtils.isEmpty(userName.getText().toString())){
                        userName.setError("required");
                        return;
                    }
                    if (TextUtils.isEmpty(password.getText().toString())){
                        password.setError("required");
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
                                        userType = ds.getKey();
                                        idFound = true;
                                        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                        mAuth.signInWithEmailAndPassword(email,password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()){
                                                    DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+userType+"/"+mAuth.getCurrentUser().getUid());
                                                    token = getSharedPreferences("TOKEN",MODE_PRIVATE);
                                                    db.child("Token").setValue(token.getString("Token","")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                progressDialog.cancel();
                                                                ed.putString("User Type",userType);
                                                                ed.putString("User Name",email);
                                                                ed.putString("Password",password.getText().toString());
                                                                ed.apply();
                                                                startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                                                                finish();
                                                            }else {
                                                                progressDialog.cancel();
                                                                Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }else {
                                                    progressDialog.cancel();
                                                    Toast.makeText(LoginActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        break;
                                    }
                                }
                            }
                            if (!idFound){
                                progressDialog.cancel();
                                Toast.makeText(LoginActivity.this,"User not found",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.cancel();
                            Toast.makeText(LoginActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    ScrollView loginLayout = findViewById(R.id.loginLayout);
                    Snackbar.make(loginLayout,"No Internet",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void ResetPassword(View view) {
        Intent intent = new Intent(LoginActivity.this,ResetPasswordActivity.class);
        intent.putExtra("Page","LoginPage");
        startActivity(intent);
    }
}