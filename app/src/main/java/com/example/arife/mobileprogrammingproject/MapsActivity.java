package com.example.arife.mobileprogrammingproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private static final int REQUEST_CODE = 12;
    private static final String TAG = "MAP ACTİVİTY" ;
    private TextView helpUserName;
    private Button thanksButton;
    private GoogleMap mMap;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String userid;

    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;
    private GeoFire geoFire;
    private String postKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        String[] myStrings = intent.getStringArrayExtra("strings");
        userid = myStrings[0];
        postKey = myStrings[1];

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(mDatabaseReference.child("Users Location"));
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        helpUserName = findViewById(R.id.nameText);
        thanksButton = findViewById(R.id.thanks);

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               helpUserName.setText(String.valueOf(dataSnapshot.child("Users").child(userid).child("name").getValue())+"'ye");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        helpDatabaseProcess();

    }
    //if user help add one to count
    public void mapclick(View v){
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = Integer.parseInt(String.valueOf(dataSnapshot.child("Users").child(mUser.getUid()).child("helpCount").getValue())) ;
                mDatabaseReference.child("Users").child(userid).child("helpCount").setValue(count+1);
                thanksButton.setText("Biz Teşekkür ederiz.");
                mDatabaseReference.child(postKey).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng hcmus = new LatLng(10.762963, 106.682394);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    //go to direction finder with user location and request location
    public void userMap(LatLng destinationLatLng, LatLng originLatLng){

        String destination= destinationLatLng.latitude+","+destinationLatLng.longitude ;
        String origin = originLatLng.latitude+","+originLatLng.longitude;

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //get help location here
    public void helpDatabaseProcess(){
        if(userid != null){
            geoFire.getLocation(userid, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    LatLng destinationLatLng = new LatLng(location.latitude, location.longitude);
                    Log.d(TAG,"destination "+destinationLatLng.longitude);
                    userDatabaseProcess(destinationLatLng);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
            Log.d(TAG, "user id null");

    }

    /*get the user location here*/
    public void userDatabaseProcess(final LatLng destinationLatLng){
        geoFire.getLocation(mUser.getUid(), new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                LatLng originLatLng = new LatLng(location.latitude, location.longitude);
                Log.d(TAG,"des "+destinationLatLng.longitude+" origin"+originLatLng.longitude);
                userMap(destinationLatLng, originLatLng);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //add marker
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    //draw map
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("Ben"+"\n"+route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title("Yardım"+"\n"+route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }
}
