package com.nandkishor.adityabloodcross;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView notificationRecyclerView;
    SharedPreferences sp;
    List<CommonAdapter.NotificationData> list;
    ProgressDialog progressDialog;
    TextView nothingToShow;
    String fb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        notificationRecyclerView = findViewById(R.id.notificationRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(NotificationActivity.this);
        notificationRecyclerView.setLayoutManager(layoutManager);
        notificationRecyclerView.setHasFixedSize(true);
        sp = getSharedPreferences("Log",MODE_PRIVATE);
        nothingToShow = findViewById(R.id.nothingToShow);
        list = new ArrayList<>();
        progressDialog = new ProgressDialog(NotificationActivity.this);
        progressDialog.setCancelable(false);
        fb = FirebaseAuth.getInstance().getUid();

        if (!new CommonAdapter(NotificationActivity.this).isNetworkAvailable()){
            RelativeLayout notificationLayout = findViewById(R.id.notificationLayout);
            Snackbar.make(notificationLayout,"No Internet",Snackbar.LENGTH_LONG).show();
        }else {
            progressDialog.setMessage("Fetching data...");
            progressDialog.show();
        }


        if (sp.getString("User Type","").equals("Admin")){
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications"+"/"+"Unreleased");
            dbRef.keepSynced(true);
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list.clear();
                    progressDialog.cancel();
                    for (DataSnapshot parent : dataSnapshot.getChildren()){
                        for (DataSnapshot children : parent.getChildren()){
                            String name = (String) children.child("Name").getValue();
                            String contactNumber = (String) children.child("Phone").getValue();
                            String address = (String) children.child("Address").getValue();
                            String bloodGroup = (String) children.child("Blood Group").getValue();
                            String mail = (String) children.child("Email").getValue();
                            String path = parent.getKey()+"/"+children.getKey();
                            CommonAdapter.NotificationData notificationData = new CommonAdapter.NotificationData(name,address,contactNumber,bloodGroup,path,mail);
                            list.add(notificationData);
                        }
                    }
                    if (list.size()>0){
                        nothingToShow.setVisibility(View.INVISIBLE);
                        CommonAdapter adapter = new CommonAdapter(list,NotificationActivity.this);
                        notificationRecyclerView.setAdapter(adapter.new AdminNotificationAdapter());
                    }else {
                        nothingToShow.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    progressDialog.cancel();
                }
            });
        }


        else if (sp.getString("User Type","").equals("Users")){
            if (getIntent().getExtras()==null){
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications"+"/"+"Released");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.cancel();
                        list.clear();
                        for (DataSnapshot parent : dataSnapshot.getChildren()){
                            for (DataSnapshot children : parent.getChildren()){
                                String name = (String) children.child("Name").getValue();
                                String contactNumber = (String) children.child("Phone").getValue();
                                String address = (String) children.child("Address").getValue();
                                String bloodGroup = (String) children.child("Blood Group").getValue();
                                String mail = (String) children.child("Email").getValue();
                                String path = parent.getKey()+"/"+children.getKey();
                                CommonAdapter.NotificationData notificationData = new CommonAdapter.NotificationData(name,address,contactNumber,bloodGroup,path,mail);
                                list.add(notificationData);
                            }
                        }
                        if (list.size()>0){
                            nothingToShow.setVisibility(View.INVISIBLE);
                            CommonAdapter adapter = new CommonAdapter(list,NotificationActivity.this);
                            notificationRecyclerView.setAdapter(adapter.new UserNotificationAdapter());
                        }else {
                            nothingToShow.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.cancel();
                    }
                });
            }
            else if (getIntent().getStringExtra(("NOTIFICATION")).equals("NOTIFICATION")){
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications"+"/"+"Released");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.cancel();
                        list.clear();
                        for (DataSnapshot parent : dataSnapshot.getChildren()){
                            for (DataSnapshot children : parent.getChildren()){
                                String name = (String) children.child("Name").getValue();
                                String contactNumber = (String) children.child("Phone").getValue();
                                String address = (String) children.child("Address").getValue();
                                String bloodGroup = (String) children.child("Blood Group").getValue();
                                String mail = (String) children.child("Email").getValue();
                                String path = parent.getKey()+"/"+children.getKey();
                                CommonAdapter.NotificationData notificationData = new CommonAdapter.NotificationData(name,address,contactNumber,bloodGroup,path,mail);
                                list.add(notificationData);
                            }
                        }
                        if (list.size()>0){
                            nothingToShow.setVisibility(View.INVISIBLE);
                            CommonAdapter adapter = new CommonAdapter(list,NotificationActivity.this);
                            notificationRecyclerView.setAdapter(adapter.new UserNotificationAdapter());
                        }else {
                            nothingToShow.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.cancel();
                    }
                });
            }

            else if (getIntent().getStringExtra(("NOTIFICATION")).equals("REQUEST")){
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications"+"/"+"Unreleased"+"/"+fb);
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.cancel();
                        list.clear();
                        for (DataSnapshot children : dataSnapshot.getChildren()){
                            String name = (String) children.child("Name").getValue();
                            String contactNumber = (String) children.child("Phone").getValue();
                            String address = (String) children.child("Address").getValue();
                            String bloodGroup = (String) children.child("Blood Group").getValue();
                            String mail = (String) children.child("Email").getValue();
                            String path = fb+"/"+children.getKey();
                            CommonAdapter.NotificationData notificationData = new CommonAdapter.NotificationData(name,address,contactNumber,bloodGroup,path,mail);
                            list.add(notificationData);
                        }
                        if (list.size()>0){
                            nothingToShow.setVisibility(View.INVISIBLE);
                            CommonAdapter adapter = new CommonAdapter(list,NotificationActivity.this);
                            notificationRecyclerView.setAdapter(adapter.new ViewBloodRequest());
                        }else {
                            nothingToShow.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.cancel();
                    }
                });
            }
        }
    }
}
