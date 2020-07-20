package com.example.cabs;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener/*,GoogleApiClient.ConnectionCallbacks*/,GoogleApiClient.OnConnectionFailedListener , NavigationView.OnNavigationItemSelectedListener/*, RoutingListener*/ {

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    LocationRequest mLocationRequest;
    String TAG = "Hello";
    LatLng latLng;
    Button Ride;
    private DrawerLayout drawer;
    GoogleSignInClient mGoogleSignInClient;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQ_PERMISSION = 101;
    private Boolean requestBol = false;
    private Marker pickupMarker;
    private String destination, requestService;
    private LinearLayout mDriverInfo;
    private LinearLayout mCarInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone,mDriverCar;
    private RatingBar mRatingBar;
    private RadioGroup mRadioGroup;
    PlacesClient placesClient;
    private LatLng destinationLatLng;
    private NotificationManager mNotificationManager;
    private TextView mAutoText;
    double ridePrice;
    private GetFloatingIconClick mGetServiceClick;
    public static boolean isFloatingIconServiceAlive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 180);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Ride = findViewById(R.id.Ride_button);
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        destinationLatLng = new LatLng(0.0,0.0);
        mAutoText = (TextView) findViewById(R.id.AutoText);
        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mCarInfo = (LinearLayout) findViewById(R.id.carInfo);
        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mDriverCar = (TextView) findViewById(R.id.driverCar);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.Auto);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==R.id.Auto){
                    if(destination!= null){
                        mLastLocation.getLongitude();
                        mLastLocation.getLatitude();
                        Location endPoint = new Location(String.valueOf(destinationLatLng));
                        endPoint.getLatitude();
                        endPoint.getLongitude();
                        double theta = mLastLocation.getLongitude() - destinationLatLng.longitude;
                        double dist = Math.sin(deg2rad(mLastLocation.getLatitude()))
                                * Math.sin(deg2rad(destinationLatLng.latitude))
                                + Math.cos(deg2rad(mLastLocation.getLatitude()))
                                * Math.cos(deg2rad(destinationLatLng.latitude))
                                * Math.cos(deg2rad(theta));
                        dist = Math.acos(dist);
                        dist = rad2deg(dist);
                        dist = dist * 60 * 1.1515;
                        ridePrice = (dist)* 5;
                        mAutoText.setText("Price is cheaper : "+ridePrice);
                        mAutoText.setGravity(Gravity.CENTER);
                    }else {
                        mAutoText.setText("Price is cheaper");
                        mAutoText.setGravity(Gravity.CENTER);
                    }
                }
                if(checkedId==R.id.Mini){
                    if(destination!= null){
                        mLastLocation.getLongitude();
                        mLastLocation.getLatitude();
                        Location endPoint = new Location(String.valueOf(destinationLatLng));
                        endPoint.getLatitude();
                        endPoint.getLongitude();
                        double theta = mLastLocation.getLongitude() - destinationLatLng.longitude;
                        double dist = Math.sin(deg2rad(mLastLocation.getLatitude()))
                                * Math.sin(deg2rad(destinationLatLng.latitude))
                                + Math.cos(deg2rad(mLastLocation.getLatitude()))
                                * Math.cos(deg2rad(destinationLatLng.latitude))
                                * Math.cos(deg2rad(theta));
                        dist = Math.acos(dist);
                        dist = rad2deg(dist);
                        dist = dist * 60 * 1.1515;
                        double ridePrice = (dist)* 10;
                        mAutoText.setText("Price is Medium : "+ridePrice);
                        mAutoText.setGravity(Gravity.CENTER);
                    }else {
                        mAutoText.setText("Price is Medium");
                        mAutoText.setGravity(Gravity.CENTER);
                    }
                }
                if(checkedId==R.id.Micro){
                    if(destination!= null){
                        mLastLocation.getLongitude();
                        mLastLocation.getLatitude();
                        Location endPoint = new Location(String.valueOf(destinationLatLng));
                        endPoint.getLatitude();
                        endPoint.getLongitude();
                        double theta = mLastLocation.getLongitude() - destinationLatLng.longitude;
                        double dist = Math.sin(deg2rad(mLastLocation.getLatitude()))
                                * Math.sin(deg2rad(destinationLatLng.latitude))
                                + Math.cos(deg2rad(mLastLocation.getLatitude()))
                                * Math.cos(deg2rad(destinationLatLng.latitude))
                                * Math.cos(deg2rad(theta));
                        dist = Math.acos(dist);
                        dist = rad2deg(dist);
                        dist = dist * 60 * 1.1515;
                        double ridePrice = (dist)* 15;
                        mAutoText.setText("Price is High : "+ridePrice);
                        mAutoText.setGravity(Gravity.CENTER);
                    }else {
                        mAutoText.setText("Price is High");
                        mAutoText.setGravity(Gravity.CENTER);
                    }
                }
                String str = mAutoText.getText().toString();
                Intent intent = new Intent(getApplicationContext(),HistorySingleActivity.class);
                intent.putExtra("price", str);

            }
        });
        Ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestBol){
                    endRide();
                }else {
                    int selectId = mRadioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton = (RadioButton) findViewById(selectId);
                    if (radioButton.getText() == null) {
                        Toast.makeText(MapsActivity.this,"No Driver Available",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    requestService = radioButton.getText().toString();
                    requestBol = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Passenger Request");
                    GeoFire geoFire = new GeoFire(reference);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.markerpic)));
                    Ride.setText("Getting your Driver...");
                    getClosestDriver();
                }
            }
        });
        String apiKey = "AIzaSyB29ad61z7gdtdWbV1sAjJLnOOD9vJxUSA";
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),apiKey);
        }
        placesClient = Places.createClient(this);
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    private class GetFloatingIconClick extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent selfIntent = new Intent(MapsActivity.this, MapsActivity.class);
            selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(selfIntent);
        }
    }
    private void askDrawOverPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // if OS is pre-marshmallow then create the floating icon, no permission is needed
            createFloatingBackButton();
        } else {
            if (!Settings.canDrawOverlays(this)) {
                // asking for DRAW_OVER permission in settings
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } else {
                createFloatingBackButton();
            }
        }
    }

    private void createFloatingBackButton() {
        Intent iconServiceIntent = new Intent(MapsActivity.this, FloatingOverMapIconService.class);
       // iconServiceIntent.putExtra("RIDE_ID", str_rideId);

        Intent navigation = new Intent(Intent.ACTION_VIEW, Uri
                .parse("google.navigation:q=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&mode=d"));
        navigation.setPackage("com.google.android.apps.maps");
        startActivityForResult(navigation, 1234);

        startService(iconServiceIntent);
    }


    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;
    GeoQuery geoQuery;
    private void getClosestDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location)
            {
                if(!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound) {
                                    return;
                                }
                                if (driverMap.get("service").equals(requestService)) {
                                    driverFound = true;
                                    driverFoundId = dataSnapshot.getKey();
                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundId).child("Passenger Request");
                                    String passengerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("PassengerRideId", passengerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);
                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    Ride.setText("Looking for Driver Location....");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
             if(!driverFound)
             {
                 radius++;
                 getClosestDriver();
             }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private DatabaseReference driverHasEndedRef;
    private ValueEventListener driverHasEndedRefListener;

    private void getHasRideEnded() {
        driverHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundId).child("Passenger Request").child("PassengerRideId");
         driverHasEndedRefListener = driverHasEndedRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(dataSnapshot.exists()){

                 }else{
                     endRide();
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }
    private void endRide() {
        requestBol = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driverHasEndedRef.removeEventListener(driverHasEndedRefListener);

        if (driverFoundId != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundId).child("Passenger Request");
            driverRef.removeValue();
            driverFoundId = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Passenger Request");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        Ride.setText("Ride Now");

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverProfileImage.setImageResource(R.drawable.ic_default_user);
    }

    private void getDriverInfo() {
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mDriverName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mDriverPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("car")!=null){
                        mDriverCar.setText(dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }
                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriverWorking").child(driverFoundId).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLog = 0;
                    Ride.setText("Driver Found");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }if(map.get(1) != null){
                        locationLog = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLog);
                    if(mDriverMarker!= null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(latLng.latitude);
                    loc1.setLongitude(latLng.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if(distance<100){
                        Ride.setText("Driver's Here");
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notification = new NotificationCompat.Builder(MapsActivity.this)
                                .setContentTitle("AddyCab Service")
                                .setContentText("Your driver is within 100 meters")
                                .setSmallIcon(android.R.drawable.ic_menu_view).build();
                        mNotificationManager.notify(1, notification);

                    }
                    else {
                        Ride.setText("Driver Found" + String.valueOf(distance));
                        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        Notification notification = new NotificationCompat.Builder(MapsActivity.this)
                                .setContentTitle("AddyCab Service")
                                .setContentText("Your driver has Accepted your request ")
                                .setSmallIcon(android.R.drawable.ic_menu_view).build();
                        mNotificationManager.notify(1, notification);

                    }
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.cab)));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            this.finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("I am here");
                mMap.clear();
                float zoomLevel = 16.0f;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.markerpic));
                mMap.addMarker(markerOptions);
            }
        });
        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            {

            }
        } else {
            MapsInitializer.initialize(getApplicationContext());
            if (checkPermission()) {
                mMap.setMyLocationEnabled(true);
            } else {
                askPermission();
            }
        }*/
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }
        LocationCallback mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    if(getApplicationContext()!=null){
                        mLastLocation = location;

                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        if(!getDriversAroundStarted)
                            getDriversAround();
                    }
                }
            }
        };

    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getDriversAround(){
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.drawable.cab)));
                mDriverMarker.setTag(key);

                markers.add(mDriverMarker);
            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                        markers.remove(markerIt);
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


   /* @Override
    public void onConnectionSuspended(int i) {

    }*/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Pickup Here");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.markerpic));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        float zoomLevel = 16.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
        LocationHelper helper = new LocationHelper(
                location.getLongitude(),
                location.getLatitude()
        );
        FirebaseDatabase.getInstance().getReference("Passenger current location").setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MapsActivity.this, "Location saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity.this, "Location not saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
      switch (menuItem.getItemId()){
          case  R.id.nav_book_your_ride:
              Intent in = new Intent(MapsActivity.this,Confirmation.class);
              startActivity(in);
              break;
          case R.id.nav_earn_coins:
              Intent ec = new Intent(MapsActivity.this,EarnCoins.class);
              startActivity(ec);
              break;
          case  R.id.nav_logout:
              FirebaseAuth.getInstance().signOut();
              signOut();
              Intent intToMain = new Intent(MapsActivity.this,MainActivity.class);
              startActivity(intToMain);
              break;

          case R.id.nav_findRestaurant:
              mGetServiceClick = new GetFloatingIconClick();
              askDrawOverPermission();
              break;

          case R.id.nav_profile:
              Intent intent = new Intent(MapsActivity.this,ProfileActivity.class);
              startActivity(intent);
              break;

          case R.id.nav_history:
              Intent inth = new Intent(MapsActivity.this,HistoryActivity.class);
              inth.putExtra("Passenger","Passenger");
              startActivity(inth);
              break;
      }
      drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void signOut() {
           mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity.this,"Signed Out SuccessFully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ","+ place.getLatLng() + "," +place.getAddress());
                if (Settings.canDrawOverlays(this)) {
                    createFloatingBackButton();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
