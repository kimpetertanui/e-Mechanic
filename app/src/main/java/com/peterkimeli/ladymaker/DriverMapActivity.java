package com.peterkimeli.ladymaker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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

import java.util.List;

import static com.google.android.gms.common.api.GoogleApiClient.*;


public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks,
        OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;

    private Button LogoutDriverButton;
    private  Button SettingsDriverButton;
    private  FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private  Boolean currentLogOutDriverStatus =false;
    private  DatabaseReference AssignedCustomerRef,AssignedCustomerPickUpRef;
    private String driverID,customerID="";
    Marker DriverMarker,PickUpMarker;
    GeoQuery geoQuery;
    private  ValueEventListener AssignedCustomerPickUpRefListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        driverID=mAuth.getCurrentUser().getUid();


        LogoutDriverButton =(Button)findViewById(R.id.driver_logout_btn);
        SettingsDriverButton=(Button)findViewById(R.id.driver_settings_btn);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        LogoutDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogOutDriverStatus=true;
                DiconnectTheDriver();
                mAuth.signOut();
                LogOutDriver();
            }
        });

        GetAssignedCustomerRequeest();

        ///buildGoogleaApiClient();
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    private void GetAssignedCustomerRequeest()
    {
        AssignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(driverID).child("CustomerRideID");
        AssignedCustomerRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    customerID=dataSnapshot.getValue().toString();

                    GetAssignedCustomerPickUpLocation();

                }

                else {
                    customerID="";
                    if (PickUpMarker != null){
                        PickUpMarker.remove();
                    }
                    if (AssignedCustomerPickUpRefListener !=null){
                        AssignedCustomerPickUpRef.removeEventListener(AssignedCustomerPickUpRefListener);
                    }


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
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Customer Pick Up Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);


             // Add a marker in Nyeri and move the camera
        LatLng nyeri = new LatLng(-0.4371, 36.9580);
        mMap.addMarker(new MarkerOptions().position(nyeri).title("Marker in Nyeri"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nyeri));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (getApplication() !=null)
        {
            lastLocation= location;
            LatLng latLng= new LatLng(location.getLatitude(),location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

            String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference  DriverAvailabilityRef= FirebaseDatabase.getInstance().getReference().child("  Drivers Available");
            GeoFire geoFireAvailability= new GeoFire(DriverAvailabilityRef);

            DatabaseReference DriverWorkingRef=FirebaseDatabase.getInstance().getReference().child("Drivers Working");
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

         protected synchronized void  buildGoogleApiClient()
         {
        googleApiClient=new Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
     }

     protected  void onStop(){
        super.onStop();
        if (!currentLogOutDriverStatus)
        {
            DiconnectTheDriver();
        }

     }

    private void DiconnectTheDriver() {
        String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference  DriverAvailabilityRef= FirebaseDatabase.getInstance().getReference().child("  Drivers Available");
        GeoFire geoFire= new GeoFire(DriverAvailabilityRef);
        geoFire.removeLocation(userID);

    }

    private void LogOutDriver() {
        Intent welcomeIntent=new Intent(DriverMapActivity.this,WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();

    }


}
