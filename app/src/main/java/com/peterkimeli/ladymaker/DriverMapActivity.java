package com.peterkimeli.ladymaker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,

        com.google.android.gms.location.LocationListener,
        RoutingListener {

    private GoogleMap mMap;
   // private FusedLocationProviderClient fusedLocationClient;
    GoogleApiClient googleApiClient;
    Location LastLocation;
    Location mLastLocation;
    LocationRequest locationRequest;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private Button LogoutDriverButton;
    private  Button SettingsDriverButton;
    private  FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private  Boolean currentLogOutMechanicstatus =false;
    private  DatabaseReference AssignedCustomerRef,AssignedCustomerPickUpRef;
    private String driverID,customerID="";
    Marker DriverMarker,PickUpMarker;
    GeoQuery geoQuery;
    private  ValueEventListener AssignedCustomerPickUpRefListener;

    private TextView txtName, txtPhone;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        driverID=mAuth.getCurrentUser().getUid();


        LogoutDriverButton =(Button)findViewById(R.id.logout_driv_btn);
        SettingsDriverButton=(Button)findViewById(R.id.settings_driver_btn);

        txtName = findViewById(R.id.name_customer);
        txtPhone = findViewById(R.id.phone_customer);
        profilePic = findViewById(R.id.profile_image_customer);
        relativeLayout = findViewById(R.id.rel2);


//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this);
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(DriverMapActivity.this,new  String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
//            return;
//        }
//     else {
//            mapFragment.getMapAsync(this);
//        }
        mFusedLocationClient=LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this,new  String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            return;
        }
        else {
            mapFragment.getMapAsync(this);
        }
//        mapFragment.getMapAsync(this);


        SettingsDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(DriverMapActivity.this, SettingsActivity.class);
                intent.putExtra("type", "Mechanics");
                startActivity(intent);
            }
        });


        LogoutDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogOutMechanicstatus=true;
                DisconnectTheDriver();
                mAuth.signOut();
                LogOutDriver();
            }
        });
//        getAssignedCustomerInformation();
        getAssignedCustomersRequest();

        ///buildGoogleaApiClient();
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    private void getAssignedCustomersRequest()
    {
        AssignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics")
                .child(driverID).child("CustomerRideID");
        AssignedCustomerRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
//                    customerID= Objects.requireNonNull(dataSnapshot.getValue()).toString();
////
////                    GetAssignedCustomerPickUpLocation();
                    customerID = dataSnapshot.getValue().toString();
                    //getting assigned customer location
                    GetAssignedCustomerPickUpLocation();

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedCustomerInformation();

                }

                else {
                    customerID="";
                    if (PickUpMarker != null){
                        PickUpMarker.remove();
                    }
                    if (AssignedCustomerPickUpRefListener !=null){
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListener);
                    }
                    relativeLayout.setVisibility(View.GONE);
//                    getAssignedCustomerInformation();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    private void GetAssignedCustomerPickUpLocation()
    {
        AssignedCustomerPickUpRef=FirebaseDatabase.getInstance().getReference().child("Customer Request")
                .child(customerID).child("l");

       AssignedCustomerPickUpRefListener= AssignedCustomerPickUpRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    List<Object>customerLocationMap=(List<Object>)dataSnapshot.getValue();
                    double LocationLat=0;
                    double LocationLng=0;


                    if (customerLocationMap.get(0) !=null)
                    {
                        LocationLat=Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if (customerLocationMap.get(1) !=null)
                    {
                        LocationLng=Double.parseDouble(customerLocationMap.get(1).toString());
                    }

                    LatLng DriverLatLng=new LatLng(LocationLat,LocationLng);
                    mMap.addMarker(new MarkerOptions().position
                            (DriverLatLng).title("Customer Pick Up Location")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }


//    @Override
////    public void onMapReady(GoogleMap googleMap) {
////        mMap = googleMap;
////
////        locationRequest = new LocationRequest();
////        locationRequest.setInterval(1000);
////        locationRequest.setFastestInterval(1000);
////        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
////
////            } else {
////                checkLocationPermission();
////            }
////        }
////
////    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // now let set user location enable
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        else{
            checkLocationPermission();
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }
//        buildGoogleApiClient();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            return;
//        }
//        mMap.setMyLocationEnabled(true);


//             // Add a marker in Nyeri and move the camera
//        LatLng nyeri = new LatLng(-0.4371, 36.9580);
//        mMap.addMarker(new MarkerOptions().position(nyeri).title("Marker in Nyeri"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(nyeri));


//    LocationCallback mLocationCallback = new  LocationCallback(){
//        @Override

    LocationCallback mLocationCallback=new LocationCallback(){
        @Override
        public void  onLocationResult (LocationResult locationResult){
           // super.onLocationResult(locationResult);
            for (Location location: locationResult.getLocations()){
                if (getApplication() !=null)
                {
                    LastLocation = location;
                    LatLng latLng= new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

                    String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference  DriverAvailabilityRef= FirebaseDatabase.getInstance().getReference().child("Mechanics Available");
                    GeoFire geoFireAvailability= new GeoFire(DriverAvailabilityRef);

                    DatabaseReference DriverWorkingRef=FirebaseDatabase.getInstance().getReference().child("Mechanics Working");
                    GeoFire geoFireWorking= new GeoFire(DriverWorkingRef);

                    switch (customerID)
                    {
                        case "":
                            geoFireWorking.removeLocation(userID);
                            geoFireAvailability.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));

                            break;

                        default:
                            geoFireAvailability.removeLocation(userID);
                            geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                            break;
                    }

                }


            }

        }

    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Allow Location")
                        .setMessage("Kindly allow Location Permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                              //  AppCompatActivity.requestPermissions(DriverMapActivity.this,new String[]{})
                                ActivityCompat.requestPermissions(DriverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                .create()
                .show();
            }

            else {
                ActivityCompat.requestPermissions(DriverMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);


            }

        }

    }



    final int LOCATION_REQUEST_CODE=1;
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        switch (requestCode){
            case  LOCATION_REQUEST_CODE:{
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //mapFragment.getMapAsync(this);
                    mMap.setMyLocationEnabled(true);
//                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION ==PackageManager.PERMISSION_GRANTED)){
//                       // mFusedLocationClient.removeLocationUpdates(locationRequest,mLocationCallback, Looper.myLooper());
//                        mMap.setMyLocationEnabled(true);
//                    }
                }
                else {
                    Toast.makeText(getApplicationContext(),"please provide permission",Toast.LENGTH_LONG).show();
                }
            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }

    }





    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //

            return;
        }
        //it will handle the refreshment of the location
        //if we dont call it we will get location only once
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
//
//    @Override
//    public void onConnectionSuspended(int i)
//    {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
//    {
//
//    }

    @Override
    public void onLocationChanged(Location location)
    {
//
        //getting the updated location
//        LastLocation = location;
//
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
//
//        String UserID=FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference DriverAvailabilityRef =FirebaseDatabase.getInstance().getReference().child("Mechanics Available");
//        GeoFire geoFire= new GeoFire(DriverAvailabilityRef);
//        geoFire.setLocation(UserID,new GeoLocation(location.getLatitude(),location.getLongitude()));
//
//        switch (customerID)
//        {
//            case "":
//                geoFireWorking.removeLocation(userID);
//                geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
//                break;
//
//            default:
//                geoFireAvailability.removeLocation(userID);
//                geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
//                break;
//        }
        if(getApplicationContext() != null)
        {
            //getting the updated location
            LastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
            mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference DriversAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Mechanics Available");
            GeoFire geoFireAvailability = new GeoFire(DriversAvailabilityRef);

            DatabaseReference DriversWorkingRef = FirebaseDatabase.getInstance().getReference().child("Mechanics Working");
            GeoFire geoFireWorking = new GeoFire(DriversWorkingRef);

            switch (customerID)
            {
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFireAvailability.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;

                default:
                    geoFireAvailability.removeLocation(userID);
                    geoFireWorking.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }

    }

    //create this method -- for useing apis
    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

//         protected synchronized void  buildGoogleApiClient() {
//        googleApiClient=new Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        googleApiClient.connect();
//     }

     protected  void onStop(){
        super.onStop();
        if (!currentLogOutMechanicstatus)
        {
            DisconnectTheDriver();
        }

     }

     private void connectDriver(){

        checkLocationPermission();
         //mFusedLocationClient.removeLocationUpdates(locationRequest,mLocationCallback, Looper.myLooper());
         mMap.setMyLocationEnabled(true);


     }

    private void DisconnectTheDriver() {
        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        if (mFusedLocationClient !=null){
            mFusedLocationClient.removeLocationUpdates((com.google.android.gms.location.LocationCallback) mLocationCallback);
        }
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference  DriverAvailabilityRef= FirebaseDatabase.getInstance().getReference().child("Mechanics Available");
        GeoFire geoFire= new GeoFire(DriverAvailabilityRef);
        geoFire.removeLocation(userID);

    }

    private void LogOutDriver() {
        Intent welcomeIntent=new Intent(DriverMapActivity.this,WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();

    }

    private void getAssignedCustomerInformation()
    {
        relativeLayout.setVisibility(View.VISIBLE);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Customers").child(customerID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0)
                {
                    String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    String phone = Objects.requireNonNull(dataSnapshot.child("phone").getValue()).toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);

                    if (dataSnapshot.hasChild("image"))
                    {
                        String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }

//                else {
//
//                    Toast.makeText(DriverMapActivity.this,"No Customer is availble!!!",Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRoutingFailure(RouteException e) {
        
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }
}
