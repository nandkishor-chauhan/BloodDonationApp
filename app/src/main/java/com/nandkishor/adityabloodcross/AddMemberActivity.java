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
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class AddMemberActivity extends AppCompatActivity {

    EditText donorName,donorRegistrationNumber,donorEmail,donorContactNo;
    RadioGroup donorGender;
    Spinner donorAddress,donorCollege,donorBloodGroup,donorDepartment,donorSection,donorYear,venue;
    Button addMember;
    ProgressDialog progressDialog;
    String gender = "Male",path,historyKey;
    DatabaseReference dbRef;
    SharedPreferences sp;
    ImageButton searchUser;
    boolean userExists = false,duplicate=false;
    TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        donorName = findViewById(R.id.donorName);
        donorRegistrationNumber = findViewById(R.id.donorRegistrationNumber);
        donorEmail = findViewById(R.id.donorEmail);
        donorContactNo = findViewById(R.id.donorContactNo);
        donorGender = findViewById(R.id.donorGender);
        donorAddress = findViewById(R.id.donorAddress);
        donorCollege = findViewById(R.id.donorCollege);
        donorBloodGroup = findViewById(R.id.donorBloodGroup);
        donorDepartment = findViewById(R.id.donorDepartment);
        donorSection = findViewById(R.id.donorSection);
        donorYear = findViewById(R.id.donorYear);
        venue = findViewById(R.id.venue);
        addMember = findViewById(R.id.addMember);
        searchUser = findViewById(R.id.searchUser);
        sp = getSharedPreferences("Log",MODE_PRIVATE);
        label = findViewById(R.id.label);
        label.setText(getIntent().getStringExtra("Label"));

        Calendar c = Calendar.getInstance();
        String dt = String.valueOf(c.get(Calendar.DATE));
        String mn = String.valueOf(c.get(Calendar.MONTH)+1);
        String yr = String.valueOf(c.get(Calendar.YEAR));
        historyKey = dt+"-"+mn+"-"+yr;


        progressDialog = new ProgressDialog(AddMemberActivity.this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);

        searchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(donorRegistrationNumber.getText().toString())){
                    donorRegistrationNumber.setError("Enter ID first");
                    return;
                }
                progressDialog.show();
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log");
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.cancel();
                        for (DataSnapshot d1 : dataSnapshot.getChildren()){
                            for (DataSnapshot d2 : d1.getChildren()){
                                if (Objects.equals(d2.child("Reg or Emp ID").getValue(), donorRegistrationNumber.getText().toString())){
                                    userExists = true;
                                    path = d1.getKey()+"/"+d2.getKey();
                                    donorName.setText((CharSequence) d2.child("Name").getValue());
                                    donorEmail.setText((CharSequence) d2.child("Email").getValue());
                                    donorContactNo.setText((CharSequence) d2.child("Phone").getValue());
                                    donorBloodGroup.setSelection(getBloodGroup((String) d2.child("Blood Group").getValue()));
                                    donorAddress.setSelection(getAddress((String) d2.child("Address").getValue()));
                                    donorCollege.setSelection(getCollege((String) d2.child("College").getValue()));
                                    donorDepartment.setSelection(getDepartment((String) d2.child("Department").getValue()));
                                    donorSection.setSelection(getSection((String) d2.child("Section").getValue()));
                                    donorYear.setSelection(getYear((String) d2.child("Year").getValue()));
                                    donorRegistrationNumber.setEnabled(false);
                                    donorEmail.setEnabled(false);
                                    break;
                                }
                            }
                            if (userExists)
                                break;
                        }
                        if (!userExists){
                            Toast.makeText(AddMemberActivity.this,"Record not found. Fill all details",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        });


        if (getIntent().getStringExtra("User Type").equals("Admin"))
            addMember.setText("Add as an Admin");

        addMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new CommonAdapter(AddMemberActivity.this).isNetworkAvailable()){
                    if (TextUtils.isEmpty(donorRegistrationNumber.getText().toString())){
                        donorRegistrationNumber.setError("required");
                        return;
                    }
                    if (TextUtils.isEmpty(donorName.getText().toString())){
                        donorName.setError("required");
                        return;
                    }
                    if (TextUtils.isEmpty(donorEmail.getText().toString())){
                        donorEmail.setError("required");
                        return;
                    }
                    if (TextUtils.isEmpty(donorContactNo.getText().toString())){
                        donorContactNo.setError("required");
                        return;
                    }
                    progressDialog.show();
                    switch (donorGender.getCheckedRadioButtonId()){
                        case R.id.male:
                            gender = "Male";
                            break;
                        case R.id.female:
                            gender = "Female";
                            break;
                    }

                    dbRef = FirebaseDatabase.getInstance().getReference("Users Log");
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot parent : dataSnapshot.getChildren()) {
                                for (DataSnapshot children : parent.getChildren()){
                                    if (Objects.equals(children.child("Reg or Emp ID").getValue(), donorRegistrationNumber.getText().toString())){
                                        duplicate = true;
                                        break;
                                    }
                                }
                                if (duplicate)
                                    break;
                            }
                            if (duplicate && !userExists){
                                progressDialog.cancel();
                                Toast.makeText(AddMemberActivity.this,"User ID already exist. Try searching...",Toast.LENGTH_LONG).show();
                            } else if (userExists){
                                Map<String,Object> map = new HashMap<>();
                                map.put("Name",donorName.getText().toString().trim());
                                map.put("Reg or Emp ID",donorRegistrationNumber.getText().toString().trim());
                                map.put("Phone",donorContactNo.getText().toString().trim());
                                map.put("Email",donorEmail.getText().toString().trim());
                                map.put("Department",donorDepartment.getSelectedItem().toString());
                                map.put("Year",donorYear.getSelectedItem().toString());
                                map.put("Section",donorSection.getSelectedItem().toString());
                                map.put("College",donorCollege.getSelectedItem().toString());
                                map.put("Address",donorAddress.getSelectedItem().toString());
                                map.put("Blood Group",donorBloodGroup.getSelectedItem().toString());
                                map.put("Gender",gender);
                                dbRef = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+path);
                                dbRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Map<String,Object> m = new HashMap<>();
                                            m.put(historyKey,venue.getSelectedItem().toString());
                                            dbRef.child("History").updateChildren(m);
                                            progressDialog.cancel();
                                            Toast.makeText(AddMemberActivity.this,"User data updated successfully",Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(AddMemberActivity.this,HomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                        else {
                                            progressDialog.cancel();
                                            Toast.makeText(AddMemberActivity.this,""+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }else {
                                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                Random random = new Random();
                                int password = random.nextInt(99999)+11111;
                                mAuth.createUserWithEmailAndPassword(donorEmail.getText().toString().trim(),"bloodbuddy"+password)
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()){
                                                    if (getIntent().getStringExtra("User Type").equals("Users"))
                                                        dbRef = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+"Users");
                                                    else if (getIntent().getStringExtra("User Type").equals("Admin"))
                                                        dbRef = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+"Admin");
                                                    final Map<String,String> map = new HashMap<>();
                                                    map.put("Name",donorName.getText().toString().trim());
                                                    map.put("Reg or Emp ID",donorRegistrationNumber.getText().toString().trim());
                                                    map.put("Phone",donorContactNo.getText().toString().trim());
                                                    map.put("Email",donorEmail.getText().toString().trim());
                                                    map.put("Department",donorDepartment.getSelectedItem().toString());
                                                    map.put("Year",donorYear.getSelectedItem().toString());
                                                    map.put("Section",donorSection.getSelectedItem().toString());
                                                    map.put("College",donorCollege.getSelectedItem().toString());
                                                    map.put("Address",donorAddress.getSelectedItem().toString());
                                                    map.put("Blood Group",donorBloodGroup.getSelectedItem().toString());
                                                    map.put("Gender",gender);
                                                    map.put("Token","None");

                                                    final String userId = mAuth.getCurrentUser().getUid();
                                                    dbRef.child(userId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Map<String,Object> m = new HashMap<>();
                                                                m.put(historyKey,venue.getSelectedItem().toString());
                                                                dbRef.child(userId).child("History").updateChildren(m);
                                                                mAuth.sendPasswordResetEmail(donorEmail.getText().toString().trim());
                                                                mAuth.signOut();
                                                                mAuth.signInWithEmailAndPassword(sp.getString("User Name",""),sp.getString("Password","")).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                                        if (task.isSuccessful()){
                                                                            progressDialog.cancel();
                                                                            Toast.makeText(AddMemberActivity.this,"Member added successfully",Toast.LENGTH_LONG).show();
                                                                            Intent intent = new Intent(AddMemberActivity.this,HomeActivity.class);
                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                            startActivity(intent);
                                                                        }
                                                                    }
                                                                });
                                                            }else {
                                                                progressDialog.cancel();
                                                                Toast.makeText(AddMemberActivity.this,""+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                                else {
                                                    progressDialog.cancel();
                                                    Toast.makeText(AddMemberActivity.this,""+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }else {
                    ScrollView addMemberLayout = findViewById(R.id.addMemberLayout);
                    Snackbar.make(addMemberLayout,"No Internet",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    int getDepartment(String s){
        List<String> list;
        list = Arrays.asList(getResources().getStringArray(R.array.department));
        return list.indexOf(s);
    }

    int getCollege(String s){
        List<String> list;
        list = Arrays.asList(getResources().getStringArray(R.array.college_name));
        return list.indexOf(s);
    }

    int getYear(String s){
        List<String> list;
        list = Arrays.asList(getResources().getStringArray(R.array.year));
        return list.indexOf(s);
    }

    int getSection(String s){
        List<String> list;
        list = Arrays.asList(getResources().getStringArray(R.array.section));
        return list.indexOf(s);
    }

    int getAddress(String s){
        List<String> list;
        list = Arrays.asList(getResources().getStringArray(R.array.city_names));
        return list.indexOf(s);
    }

    int getBloodGroup(String s){
        List<String> list;
        list = Arrays.asList(getResources().getStringArray(R.array.blood_group));
        return list.indexOf(s);
    }

}
