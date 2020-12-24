package com.nandkishor.adityabloodcross;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupWiseRepresentationFragment extends Fragment {

    PieChartView pieChartView;
    long apos=0,aneg=0,bpos=0,bneg=0,opos=0,oneg=0,abpos=0,abneg=0,totalCount;
    TextView a_pos,a_neg,b_pos,b_neg,o_pos,o_neg,ab_pos,ab_neg;

    public GroupWiseRepresentationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_wise_representation, container, false);
        pieChartView = view.findViewById(R.id.pieChart);
        a_pos = view.findViewById(R.id.a_pos);
        a_neg = view.findViewById(R.id.a_neg);
        b_pos = view.findViewById(R.id.b_pos);
        b_neg = view.findViewById(R.id.b_neg);
        ab_pos = view.findViewById(R.id.ab_pos);
        ab_neg = view.findViewById(R.id.ab_neg);
        o_pos = view.findViewById(R.id.o_pos);
        o_neg = view.findViewById(R.id.o_neg);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users Log");
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalCount=0; apos=0; aneg=0; bpos=0; bneg=0; opos=0; oneg=0; abpos=0; abneg=0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                for (DataSnapshot db : dataSnapshot1.getChildren()){
                    if (Objects.equals(db.child("Blood Group").getValue(), "A+"))
                        apos++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "A-"))
                        aneg++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "B+"))
                        bpos++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "B-"))
                        bneg++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "AB+"))
                        abpos++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "AB-"))
                        abneg++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "O+"))
                        opos++;
                    else if (Objects.equals(db.child("Blood Group").getValue(), "O-"))
                        oneg++;
                    totalCount++;
                }
                if (totalCount==0){
                    totalCount=1;
                }
                a_pos.setText(""+apos);
                b_pos.setText(""+bpos);
                a_neg.setText(""+aneg);
                b_neg.setText(""+bneg);
                ab_pos.setText(""+abpos);
                ab_neg.setText(""+abneg);
                o_pos.setText(""+opos);
                o_neg.setText(""+oneg);

                List<SliceValue> pieData = new ArrayList<>();
                pieData.add(new SliceValue((apos*100)/totalCount,Color.RED).setLabel("A+"));
                pieData.add(new SliceValue((aneg*100)/totalCount,Color.CYAN).setLabel("A-"));
                pieData.add(new SliceValue((bpos*100)/totalCount,Color.GREEN).setLabel("B+"));
                pieData.add(new SliceValue((bneg*100)/totalCount,Color.YELLOW).setLabel("B-"));
                pieData.add(new SliceValue((abpos*100)/totalCount,Color.MAGENTA).setLabel("AB+"));
                pieData.add(new SliceValue((abneg*100)/totalCount,Color.GRAY).setLabel("AB-"));
                pieData.add(new SliceValue((opos*100)/totalCount,Color.BLUE).setLabel("O+"));
                pieData.add(new SliceValue((oneg*100)/totalCount,Color.BLACK).setLabel("O-"));
                PieChartData pieChartData = new PieChartData(pieData);
                pieChartData.setHasLabels(true);
                pieChartView.setPieChartData(pieChartData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

}