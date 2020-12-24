package com.nandkishor.adityabloodcross;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RequestBloodActivity extends AppCompatActivity {

    Button request;
    EditText name,email,contact;
    Spinner address,bloodGroup;
    CheckBox term_condition;
    String userId,tokenList;
    int total;
    ProgressDialog progressDialog;
    SharedPreferences sp;
    ScrollView requestBloodLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);
        request = findViewById(R.id.request);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        contact = findViewById(R.id.contactNo);
        address = findViewById(R.id.address);
        bloodGroup = findViewById(R.id.bloodGroup);
        term_condition = findViewById(R.id.term_condition);
        progressDialog = new ProgressDialog(RequestBloodActivity.this);
        progressDialog.setCancelable(false);
        requestBloodLayout = findViewById(R.id.requestBloodLayout);

        request.setClickable(false);
        term_condition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    request.setBackgroundColor(getResources().getColor(R.color.light_red));
                    request.setClickable(false);
                    request.setEnabled(false);
                }
                else {
                    request.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    request.setClickable(true);
                    request.setEnabled(true);
                }
            }
        });

        if (new CommonAdapter(RequestBloodActivity.this).isNetworkAvailable()){
            sp = getSharedPreferences("Log",MODE_PRIVATE);
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            progressDialog.setMessage("Fetching your data...");
            progressDialog.show();
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+sp.getString("User Type","")+"/"+userId);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    progressDialog.cancel();
                    name.setText((CharSequence) dataSnapshot.child("Name").getValue());
                    email.setText((CharSequence) dataSnapshot.child("Email").getValue());
                    contact.setText((CharSequence) dataSnapshot.child("Phone").getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.cancel();
                }
            });

            request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (new CommonAdapter(RequestBloodActivity.this).isNetworkAvailable()){
                        if (TextUtils.isEmpty(name.getText().toString())){
                            name.setError("required");
                        }
                        else if (TextUtils.isEmpty(email.getText().toString())){
                            email.setError("required");
                        }
                        else if (TextUtils.isEmpty(contact.getText().toString())){
                            contact.setError("required");
                        }else {
                            progressDialog.setMessage("Processing...");
                            progressDialog.show();
                            Map<String,String> map = new HashMap<>();
                            map.put("Name",name.getText().toString().trim());
                            map.put("Email",email.getText().toString().trim());
                            map.put("Phone",contact.getText().toString().trim());
                            map.put("Address",address.getSelectedItem().toString());
                            map.put("Blood Group",bloodGroup.getSelectedItem().toString());

                            String key = FirebaseDatabase.getInstance().getReference("Notifications").child("Unreleased").child(userId).push().getKey();
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications").child("Unreleased").child(userId);
                            dbRef.child(key).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+"Admin");
                                        db.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot db : dataSnapshot.getChildren()){
                                                    if (total == 0)
                                                        tokenList= String.valueOf(db.child("Token").getValue());
                                                    else
                                                        tokenList += "  "+db.child("Token").getValue();
                                                    total++;
                                                }
                                                final String message = name.getText().toString()+" requires "+bloodGroup.getSelectedItem().toString()+" urgently. Please help him/her by calling on "+contact.getText().toString();

                                                if (total != 0) {
                                                    StringRequest stringRequest = new StringRequest(Request.Method.POST,"https://srikas.000webhostapp.com/notification.php", new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(final String response) {
                                                            if (response!=null){
                                                                progressDialog.cancel();
                                                                Toast.makeText(RequestBloodActivity.this,"Blood request has been sent",Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(RequestBloodActivity.this,HomeActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError error) {
                                                            progressDialog.cancel();
                                                            Toast.makeText(RequestBloodActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                                                        }
                                                    }){
                                                        @Override
                                                        protected Map<String, String> getParams() throws AuthFailureError {
                                                            Map<String,String> map = new HashMap<>();
                                                            map.put("token",tokenList);
                                                            map.put("message",message);
                                                            return map;
                                                        }
                                                    };
                                                    RequestQueue queue = Volley.newRequestQueue(RequestBloodActivity.this);
                                                    queue.add(stringRequest);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                progressDialog.cancel();
                                            }
                                        });
                                    }else {
                                        progressDialog.cancel();
                                        Toast.makeText(RequestBloodActivity.this,""+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }else {
                        Snackbar.make(requestBloodLayout,"No Internet",Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            Snackbar.make(requestBloodLayout,"No Internet",Snackbar.LENGTH_LONG).show();
        }
    }
}
