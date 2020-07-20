package com.example.cabs;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter<String> {
    DatabaseReference myref;

    public MyAdapter(@NonNull Context context, int simple_list_item_1, ArrayList<String> records) {
        super(context,0,records);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String item = getItem(position);
        myref = FirebaseDatabase.getInstance().getReference("PassengerDetail");

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_custom_layout,parent,false);
        }
        final Button list_but = (Button)convertView.findViewById(R.id.list_But);
        TextView list_Txt = (TextView)convertView.findViewById(R.id.list_txt);
        list_Txt.setText(item);
        list_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list_but.setBackgroundColor(R.drawable.ic_book_your_ride);
            }
        });
        return convertView;
    }
}
