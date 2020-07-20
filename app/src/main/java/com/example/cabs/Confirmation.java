package com.example.cabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Confirmation extends AppCompatActivity {
    ListView listView;
     DatabaseReference myref,mycur;
     ArrayList<String> muser = new ArrayList<String>();
     ArrayAdapter<String> arrayAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        myref = FirebaseDatabase.getInstance().getReference("PassengerDetail");
       // mycur = FirebaseDatabase.getInstance().getReference("current location");
        listView =(ListView) findViewById(R.id.cust_list);
        arrayAdapter = new MyAdapter(this,android.R.layout.simple_list_item_1,muser);
        listView.setAdapter(arrayAdapter);
        myref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String value = dataSnapshot.getValue(User.class).toString();
               // String value1 = dataSnapshot.getValue(LocationHelper.class).toString();
                muser.add(value);
               // muser.add(value1);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
