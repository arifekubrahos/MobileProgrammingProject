package com.example.arife.mobileprogrammingproject;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private static final int REQUEST_CODE = 12;
    private static final String TAG = "MAP ACTİVİTY" ;
    private FusedLocationProviderClient mFusedLocationClient; //location APIs ffrom google api
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationCallback mLocationCallback;
    private LatLng originLatLng;
    private LatLng destinationLatLng;

    private GoogleMap mMap;
    private ArrayList<LatLng> listPoints = new ArrayList<>();
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String userid;

    private DatabaseReference mDatabaseReference;
    private GeoFire geoFire;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            userid = bundle.getString("user id");
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        geoFire = new GeoFire(mDatabaseReference.child("Users Location"));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkForLocationRequest();
        buildLocationCallBack();
        databaseProcess();


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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 18));

        if(originLatLng!= null && destinationLatLng!= null){
            userMap();
        }
        else{
            mMap.addMarker(new MarkerOptions().title("burada").position(hcmus));
        }

    }

    public void userMap(){

        String origin = String.valueOf(originLatLng.latitude)+","+String.valueOf(originLatLng.longitude);
        String destination = String.valueOf(destinationLatLng.latitude)+","+String.valueOf(destinationLatLng.longitude);
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public boolean databaseProcess(){
        if(userid != null){
            geoFire.getLocation(userid, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    listPoints.add(0,new LatLng(location.latitude, location.longitude));
                    originLatLng = listPoints.get(0);
                    originMarkers.add(mMap.addMarker(new MarkerOptions()
                            .title("help ")
                            .position(listPoints.get(0))));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return false;
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

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    /*get the user location here*/
    public void buildLocationCallBack() {
        mLocationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                   listPoints.add(1,new LatLng(location.getLatitude(), location.getLongitude()));
                   destinationLatLng = listPoints.get(1);

                    destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                            .title("user ")
                            .position(destinationLatLng)));

                }

            }
        };
    }

    /*determine the updates property ex: how many times it takes the location */
    public void checkForLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
