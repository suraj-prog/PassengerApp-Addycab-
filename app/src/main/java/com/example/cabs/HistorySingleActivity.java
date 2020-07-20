package com.example.cabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.DataCollectionDefaultChange;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements PaymentResultListener {
    private String rideId,currentUserID,customerId,driverId,UserDriverorPassenger;
    private TextView rideLocation;
    private TextView rideDistance;
    private TextView rideDate;
    private TextView userName;
    private TextView userPhone;
    private ImageView userImage;
    private TextView rideprice;
    private DatabaseReference historyInfo;
    private LatLng destinationLatLng, pickUpLatLng;
    private RatingBar mRatingBar;
    private GoogleMap mMap;
    private String distance;
    private double ridePrice;
    private Button mPay;
    private Boolean PassengerPaid = false;
     private SupportMapFragment mMapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);
        rideId = getIntent().getExtras().getString("rideId");
       /* mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);*/
        mPay = (Button)findViewById(R.id.pay);
        rideLocation = (TextView) findViewById(R.id.rideLocation);
        rideDistance = (TextView) findViewById(R.id.rideDistance);
        rideDate = (TextView) findViewById(R.id.rideDate);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);
        rideprice = (TextView) findViewById(R.id.ridePrice);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        //polylines = new ArrayList<>();
        userImage = (ImageView) findViewById(R.id.userImage);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyInfo = FirebaseDatabase.getInstance().getReference().child("history").child(rideId);
        getRideInformation();
       // getPrice();
    }

    private void getPrice() {
        Intent intent = getIntent();
        String str  = intent.getStringExtra("price");;
        str = rideprice.getText() + str;
        rideprice.setText(str);
        System.out.println("satrfdgg"+str);
    }

    private void getRideInformation() {
        historyInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child:dataSnapshot.getChildren()){
                        if(child.getKey().equals("passenger")){
                            customerId = child.getValue().toString();
                            if(!customerId.equals(currentUserID)){
                                UserDriverorPassenger = "Driver";
                                getUserInformation("Passenger",customerId);
                            }
                        }
                        if(child.getKey().equals("driver")){
                            driverId = child.getValue().toString();
                            if(!driverId.equals(currentUserID)){
                                UserDriverorPassenger = "Passenger";
                                getUserInformation("Driver",driverId);
                                displayCustomerObject();
                            }
                        }
                        if(child.getKey().equals("timestamp")) {
                           rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if(child.getKey().equals("rating")) {
                             mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }
                        if(child.getKey().equals("PassengerPaid")) {
                            PassengerPaid = true;
                        }
                        if(child.getKey().equals("distance")) {
                              distance = child.getValue().toString();
                              rideDistance.setText(distance.substring(0,Math.min(distance.length(), 5))+" km");
                            /*  ridePrice = Double.valueOf(distance)* 0.5;*/
                              getPrice();
                        }
                        if(child.getKey().equals("destination")) {
                            rideLocation.setText(child.getValue().toString());
                        }
                        if(child.getKey().equals("location")) {
                            pickUpLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinationLatLng!= new LatLng(0,0)){
                                //getRouteToMarker();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerObject() {
        mRatingBar.setVisibility(View.VISIBLE);
        mPay.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyInfo.child("rating").setValue(rating);
                DatabaseReference mDriverRating = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("rating");
                mDriverRating.child(rideId).setValue(rating);
            }
        });
        if(PassengerPaid){
            mPay.setEnabled(false);
        }else {
            mPay.setEnabled(true);
        }
        Checkout.preload(getApplicationContext());
        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             setUpPayment();
            }
        });
    }

    private void setUpPayment() {
        final Activity activity = this;
        final Checkout checkout = new Checkout();
        JSONObject object = new JSONObject();
        try{
            object.put("name","AddyCab");
            object.put("",rideprice);
            JSONObject preFill = new JSONObject();
            preFill.put("email","surajniki5653@gmail.com");
            preFill.put("contact","7767939817");
            object.put("preFill",preFill);
            checkout.open(activity,object);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void getUserInformation(String DriverOrPassenger, String DriverOrPassengerId) {
    DatabaseReference other = FirebaseDatabase.getInstance().getReference().child("Users").child(DriverOrPassenger).child(DriverOrPassengerId);
    other.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                if(map.get("name") != null){
                    userName.setText(map.get("name").toString());
                }
                if(map.get("phone") != null){
                    userPhone.setText(map.get("phone").toString());
                }
                if(map.get("profileImageUrl") != null){
                    Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(userImage);
                }
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
   /* private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(pickUpLatLng,destinationLatLng)
                .build();
        routing.execute();
    }*/
  /*  @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }*/
    /*private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickUpLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.drawable.markerpic)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.markerpic)));
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        for (int n = 0; n <route.size(); n++) {
            int colorIndex = n % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + n * 3);
            polyOptions.addAll(route.get(n).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            Toast.makeText(getApplicationContext(),"Route "+ (n+1) +": distance - "+ route.get(n).getDistanceValue()+": duration - "+ route.get(n).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
        erasePolylines();
    }
    private void erasePolylines(){
        for(Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }*/
    @Override
    public void onPaymentSuccess(String razorPayId) {
        Toast.makeText(this,"Payment SuccessFull"+razorPayId,Toast.LENGTH_SHORT).show();
        historyInfo.child("PassengerPaid").setValue(rideprice);
        mPay.setEnabled(false);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this,"Payment Failed"+s,Toast.LENGTH_SHORT).show();
    }

}
