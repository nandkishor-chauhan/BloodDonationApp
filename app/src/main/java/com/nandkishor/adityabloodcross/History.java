package com.nandkishor.adityabloodcross;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

public class History extends AppCompatActivity {

    LinearLayout linearLayout;
    RecyclerView historyRecyclerView;
    TextView nothingToShow;
    List<HistoryData> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        linearLayout = findViewById(R.id.linearLayout);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        nothingToShow = findViewById(R.id.nothingToShow);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(History.this);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        if (new CommonAdapter(History.this).isNetworkAvailable()){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            SharedPreferences sp = getSharedPreferences("Log",MODE_PRIVATE);
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("Users Log"+"/"+sp.getString("User Type","")+"/"+userId+"/"+"History");
            db.keepSynced(true);
            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    list.clear();
                    for (DataSnapshot d : dataSnapshot.getChildren()){
                        String date = d.getKey();
                        String venue = (String) d.getValue();
                        HistoryData data = new HistoryData(venue,date);
                        list.add(data);
                    }
                    if (list.size()>0){
                        linearLayout.setVisibility(View.VISIBLE);
                        nothingToShow.setVisibility(View.INVISIBLE);
                        HistoryAdapter adapter = new HistoryAdapter(list,History.this);
                        historyRecyclerView.setAdapter(adapter);
                    }else {
                        linearLayout.setVisibility(View.INVISIBLE);
                        nothingToShow.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            RelativeLayout historyLayout = findViewById(R.id.historyLayout);
            Snackbar.make(historyLayout,"No Internet",Snackbar.LENGTH_LONG).show();
        }

    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder>{

        List<HistoryData> list;
        Context ctx;

        HistoryAdapter(List<HistoryData> list, Context ctx) {
            this.list = list;
            this.ctx = ctx;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new HistoryViewHolder(LayoutInflater.from(ctx).inflate(R.layout.history_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            HistoryData historyData = list.get(position);
            holder.venue.setText(historyData.getVenue());
            holder.date.setText(historyData.getDate());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        TextView venue,date;
        HistoryViewHolder(View itemView) {
            super(itemView);
            venue = itemView.findViewById(R.id.venue);
            date = itemView.findViewById(R.id.date);
        }
    }

    class HistoryData{
        String venue;
        String date;

        HistoryData(String venue, String date) {
            this.venue = venue;
            this.date = date;
        }

        public String getVenue() {
            return venue;
        }

        public String getDate() {
            return date;
        }
    }

}
