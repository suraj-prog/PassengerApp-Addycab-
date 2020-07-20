package com.example.cabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.cabs.History.HistoryAdapter;
import com.example.cabs.History.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import java.text.DateFormat;
import android.text.format.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
private RecyclerView mHistoryRecyclerView;
private RecyclerView.Adapter mHistoryAdapter;
private RecyclerView.LayoutManager mHistoryLayoutManager;
private String Passenger,UserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mHistoryRecyclerView = (RecyclerView)findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(),HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        Passenger = getIntent().getExtras().getString("Passenger");
        UserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

    }

    private void getUserHistoryIds() {
        DatabaseReference Uhd = FirebaseDatabase.getInstance().getReference().child("Users").child(Passenger).child(UserID).child("history");
        Uhd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.exists()){
                 for(DataSnapshot history: dataSnapshot.getChildren()){
                     FetchRideInformation(history.getKey());
                 }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference hd = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        hd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if(dataSnapshot.exists()){
                 String rideId = dataSnapshot.getKey();
                 Long timestamp = 0L;
                 for(DataSnapshot child: dataSnapshot.getChildren()){
                     if(child.getKey().equals("timestamp")){
                         timestamp = Long.valueOf(child.getValue().toString());
                     }
                 }
                 HistoryObject obj = new HistoryObject(rideId,getDate(timestamp));
                 resultsHistory.add(obj);
                 mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory(){
        return resultsHistory;
    }

}
