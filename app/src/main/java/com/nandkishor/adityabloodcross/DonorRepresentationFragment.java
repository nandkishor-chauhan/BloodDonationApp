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
public class DonorRepresentationFragment extends Fragment {


    PieChartView pieChartView;
    long totalCount,maleCount,femaleCount;
    TextView maleDonor,femaleDonor,totalDonor;

    public DonorRepresentationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_donor_representation, container, false);
        pieChartView = view.findViewById(R.id.pieChart);
        maleDonor = view.findViewById(R.id.maleDonor);
        femaleDonor = view.findViewById(R.id.femaleDonor);
        totalDonor = view.findViewById(R.id.totalDonor);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users Log");
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalCount = 0;
                maleCount = 0;
                femaleCount = 0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                for (DataSnapshot db : dataSnapshot1.getChildren()){
                    if (Objects.equals(db.child("Gender").getValue(), "Male"))
                        maleCount++;
                    else if (Objects.equals(db.child("Gender").getValue(), "Female"))
                        femaleCount++;
                    totalCount++;
                }
                if (totalCount==0){
                    totalDonor.setText(""+totalCount);
                    totalCount=1;
                }else
                    totalDonor.setText(""+totalCount);
                maleDonor.setText(""+maleCount);
                femaleDonor.setText(""+femaleCount);
                List<SliceValue> pieData = new ArrayList<>();
                pieData.add(new SliceValue((maleCount*100)/totalCount,Color.RED).setLabel("Male"));
                pieData.add(new SliceValue((femaleCount*100)/totalCount,Color.CYAN).setLabel("Female"));
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
