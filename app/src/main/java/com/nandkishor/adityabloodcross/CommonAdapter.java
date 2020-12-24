package com.nandkishor.adityabloodcross;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommonAdapter {

    private List<NotificationData> list;
    private Context ctx;
    private AlertDialog.Builder alertDialog;
    private AlertDialog dialog;
    private int total=0;
    private String tokenList;


    CommonAdapter(List<NotificationData> list, Context ctx) {
        this.list = list;
        this.ctx = ctx;
    }

    CommonAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public class AdminNotificationAdapter extends RecyclerView.Adapter<AdminNotificationViewHolder> {

        @NonNull
        @Override
        public AdminNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.admin_notification_item, parent, false);
            return new AdminNotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AdminNotificationViewHolder holder, int position) {
            final NotificationData data = list.get(position);
            holder.name.setText(data.getName());
            holder.address.setText(data.getAddress());
            holder.contactNo.setText(data.getContactNo());
            holder.bloodGroup.setText(data.getBloodGroup() + " blood group required urgently");
            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + data.getContactNo()));
                    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) ctx,new String[]{Manifest.permission.CALL_PHONE},100);
                        return;
                    }
                    ctx.startActivity(intent);
                }
            });

            holder.approve.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkAvailable()) {
                        final ProgressDialog progressDialog = new ProgressDialog(ctx);
                        progressDialog.setMessage("Processing");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+"Users");

                        dbRef.addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NewApi")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                progressDialog.cancel();
                                total = 0;
                                for (DataSnapshot db : dataSnapshot.getChildren()){
                                    if (Objects.equals(db.child("Blood Group").getValue(), data.getBloodGroup()) && Objects.equals(db.child("Address").getValue(), data.getAddress()) && !Objects.equals(db.child("Token").getValue(), "None")){
                                        if (total == 0)
                                            tokenList = String.valueOf(db.child("Token").getValue());
                                        else
                                            tokenList += "  "+db.child("Token").getValue();
                                        total++;
                                    }
                                }
                                final String mailBody = "Hi "+data.getName()+" The request for blood "+data.getBloodGroup()+" is approved.\nThanks & Regards \nAditya Blood Cross";
                                final String message = data.getName()+" requires "+data.getBloodGroup()+" urgently. Please help him/her by calling on "+data.getContactNo();
                                alertDialog = new AlertDialog.Builder(ctx);
                                alertDialog.setCancelable(false);
                                if (total!=0){
                                    alertDialog.setTitle(total+" users matched the requirements");
                                    alertDialog.setMessage("Do you want to send the notifications to them?");
                                    alertDialog.setPositiveButton("Yes, send", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                            progressDialog.show();
                                            StringRequest stringRequest = new StringRequest(Request.Method.POST,"https://srikas.000webhostapp.com/notification.php", new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(final String response) {
                                                    if (response!=null){
                                                        dialog.cancel();
                                                        progressDialog.cancel();
                                                        Map<String,String> map = new HashMap<>();
                                                        map.put("Name",data.getName());
                                                        map.put("Phone",data.getContactNo());
                                                        map.put("Address",data.getAddress());
                                                        map.put("Blood Group",data.getBloodGroup());
                                                        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Notifications");
                                                        db.child("Released").child(data.getPath()).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(ctx,"Notifications sent",Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        db.child("Unreleased").child(data.getPath()).removeValue();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    progressDialog.cancel();
                                                    Toast.makeText(ctx,error.getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            }){
                                                @Override
                                                protected Map<String, String> getParams() throws AuthFailureError {
                                                    Map<String,String> map = new HashMap<>();
                                                    map.put("token",tokenList);
                                                    map.put("message",message);
                                                    map.put("body",mailBody);
                                                    map.put("email",data.getEmail());
                                                    return map;
                                                }
                                            };
                                            RequestQueue queue = Volley.newRequestQueue(ctx);
                                            queue.add(stringRequest);
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialog.cancel();
                                            dialogInterface.cancel();
                                        }
                                    });
                                }
                                else {
                                    alertDialog.setTitle("No donors available currently matching your requirements");
                                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                            dialog.cancel();
                                        }
                                    });
                                }
                                try{
                                    dialog = alertDialog.create();
                                    dialog.show();
                                }catch (WindowManager.BadTokenException e){
                                    dialog.dismiss();
                                    //THIS ISSUE IS TO BE SOLVED LATER
                                    //Toast.makeText(ctx,""+e.getMessage(),Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressDialog.cancel();
                            }
                        });
                    } else {
                        Toast.makeText(ctx, "No Internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkAvailable()){
                        final String mailBody = "Hi "+data.getName()+" The request for blood "+data.getBloodGroup()+" is rejected.\nThanks & Regards \nAditya Blood Cross" ;
                        alertDialog = new AlertDialog.Builder(ctx);
                        alertDialog.setTitle("Are you sure");
                        alertDialog.setMessage("Do you want to reject the request for blood");
                        alertDialog.setPositiveButton("Yes, Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                final ProgressDialog progressDialog = new ProgressDialog(ctx);
                                progressDialog.setMessage("Processing");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                StringRequest sendMail = new StringRequest(Request.Method.POST, "https://srikas.000webhostapp.com/sendMail.php", new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        dialogInterface.cancel();
                                        progressDialog.cancel();
                                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications"+"/"+"Unreleased");
                                        dbRef.child(data.getPath()).removeValue();
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        dialogInterface.cancel();
                                        progressDialog.cancel();
                                        Toast.makeText(ctx,""+error.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }){
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String,String> map = new HashMap<>();
                                        map.put("email",data.getEmail());
                                        map.put("body",mailBody);
                                        return map;
                                    }
                                };
                                RequestQueue requestQueue = Volley.newRequestQueue(ctx);
                                requestQueue.add(sendMail);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        dialog = alertDialog.create();
                        dialog.show();
                    }else{
                        Toast.makeText(ctx,"No Internet",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class AdminNotificationViewHolder extends RecyclerView.ViewHolder{
        TextView name,contactNo,address,bloodGroup;
        ImageButton call;
        Button approve,reject;
        AdminNotificationViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            contactNo = itemView.findViewById(R.id.contactNo);
            address = itemView.findViewById(R.id.address);
            call = itemView.findViewById(R.id.call);
            approve = itemView.findViewById(R.id.approve_notification);
            reject = itemView.findViewById(R.id.reject_notification);
            bloodGroup = itemView.findViewById(R.id.bloodGroup);
        }
    }





    public class UserNotificationAdapter extends RecyclerView.Adapter<UserNotificationViewHolder>{

        @NonNull
        @Override
        public UserNotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.user_notification_item,parent,false);
            return new UserNotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserNotificationViewHolder holder, int position) {
            final NotificationData data = list.get(position);
            holder.name.setText(data.getName());
            holder.address.setText(data.getAddress());
            holder.contactNo.setText(data.getContactNo());
            holder.bloodGroup.setText(data.getBloodGroup()+" blood group required urgently");
            holder.call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + data.getContactNo()));
                    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) ctx,new String[]{Manifest.permission.CALL_PHONE},100);
                        return;
                    }
                    ctx.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class UserNotificationViewHolder extends RecyclerView.ViewHolder{

        TextView name,contactNo,address,bloodGroup;
        ImageButton call;
        UserNotificationViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            contactNo = itemView.findViewById(R.id.contact);
            address = itemView.findViewById(R.id.address);
            call = itemView.findViewById(R.id.call);
            bloodGroup = itemView.findViewById(R.id.bloodGroup);
        }
    }


    public class ViewBloodRequest extends RecyclerView.Adapter<ViewBloodRequestViewHolder>{

        @NonNull
        @Override
        public ViewBloodRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.user_view_request,parent,false);
            return new ViewBloodRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewBloodRequestViewHolder holder, int position) {
            final NotificationData data = list.get(position);
            holder.name.setText(data.getName());
            holder.address.setText(data.getAddress());
            holder.contactNo.setText(data.getContactNo());
            holder.bloodGroup.setText("Required blood group : "+data.getBloodGroup());
            holder.close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkAvailable()){
                        alertDialog = new AlertDialog.Builder(ctx);
                        alertDialog.setTitle("Are you sure");
                        alertDialog.setMessage("Do you want to cancel your request for blood");
                        alertDialog.setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Notifications"+"/"+"Unreleased");
                                dbRef.child(data.getPath()).removeValue();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        dialog = alertDialog.create();
                        dialog.show();
                    }else {
                        Toast.makeText(ctx,"No Internet",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class ViewBloodRequestViewHolder extends RecyclerView.ViewHolder{

        TextView name,contactNo,address,bloodGroup;
        ImageButton close;
        ViewBloodRequestViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            contactNo = itemView.findViewById(R.id.contactNo);
            address = itemView.findViewById(R.id.address);
            close = itemView.findViewById(R.id.close);
            bloodGroup = itemView.findViewById(R.id.bloodGroup);
        }
    }


    public static class NotificationData{
        String name,address,contactNo,bloodGroup,path,email;

        NotificationData(String name, String address, String contactNo, String bloodGroup,String path,String email) {
            this.name = name;
            this.address = address;
            this.contactNo = contactNo;
            this.bloodGroup = bloodGroup;
            this.path = path;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getContactNo() {
            return contactNo;
        }

        public String getBloodGroup() {
            return bloodGroup;
        }

        public String getPath() {
            return path;
        }

        public String getEmail() {
            return email;
        }
    }



    boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return networkInfo != null && networkInfo.isConnected();
    }
}
