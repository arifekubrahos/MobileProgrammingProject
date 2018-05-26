package com.example.arife.mobileprogrammingproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Homepage; getting user current location and helps based that
 *          Geoquery adds listener to user location; this listen to other users help location into database
 *          if help location is in the certain place (draw circle with radius) return help post
 */

public class HomePageActivity extends AppCompatActivity implements GeoQueryEventListener {

    private static final String TAG ="MAİN PAGE ACTİVİTY";
    private FusedLocationProviderClient mFusedLocationClient; //location APIs from google api
    private LocationRequest mLocationRequest; //update location
    private LocationCallback mLocationCallback; //return location
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 34;

    private TextView textView;

    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;
    private int hascount;

    private GeoLocation userGeolocation; //user anlık konumunu aldığımız yer
    private GeoFire userGeofire; //userlarını içinden bizimkini bulacaz
    private GeoFire helpGeofire; // locationları kontrol ettiğimiz yerler
   // private GeoLocation userGeoLocation;

    private GeoQuery geoQuery;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private FloatingActionButton helpButton;
    private Toolbar cToolbar;

    private List<Help> helpList =new ArrayList<Help>(); //Bu listeyi adaptera göndericez onkeyexistde almamız gerek
    private List<String> userPostKeyList = new ArrayList<>();//hangi userlar yakında kullanıcı idleri

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        cToolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(cToolbar);

        textView = findViewById(R.id.text);
        recyclerView = findViewById(R.id.dailyRecyclerView);

        //liste adapterı oluşturma yeri
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new HomePageAdapter(helpList);
        recyclerView.setAdapter(adapter);
        helpButton = findViewById(R.id.helpButton);
        helpButton.setVisibility(View.VISIBLE);

        //database bağlantısı sağlayarak kullanıcıyı konumunu ve postların konumlarını alıyoruz
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        userGeofire = new GeoFire(mDatabaseReference.child("Users Location"));
        helpGeofire = new GeoFire(mDatabaseReference.child("Help Location"));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //kullanıcı konum olma izini sağlama
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission is not granted ask user
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        else {
            checkForLocationRequest();
            buildLocationCallBack();
        }
        databaseProcess();

    }

    /*Create post button*/
    public void createHelpClick(View v){

        Intent intent = new Intent(getApplicationContext(),CreateHelpActivity.class);
        startActivity(intent);
        finish();
    }

    /*when app restart it get the last location;request location update calling for the get last location*/
    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(HomePageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomePageActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        if(userGeolocation != null)
            display(userGeolocation);
    }

    /*stop the update of location */
    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /*get the user location here */
    public void buildLocationCallBack() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    userGeolocation = new GeoLocation(location.getLatitude(),location.getLongitude());
                    userGeofire.setLocation(mUser.getUid(),userGeolocation);
                    display(userGeolocation);
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

    public void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted ask user
            ActivityCompat.requestPermissions(this, new String[]{Manifest
                    .permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        else
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            textView.setText(String.valueOf(location.getLatitude()));
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            textView.setText(String.valueOf(e));
                        }
                    });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode){
           case REQUEST_CODE_ASK_PERMISSIONS:{
               if(grantResults.length > 0 ){
                   if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                       Log.d(TAG,"PERMİSSİON GRANTED");
                   }
                   else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                        Log.d(TAG,"PERMİSSİON DENİED");
                   }
               }
           }
       }
    }


    /*returns me the post that contain certain area*/
    public void display(GeoLocation userGeolocation){
        //radious in km, bunu değiştirilebilir yap!
        geoQuery = helpGeofire.queryAtLocation(userGeolocation,1000);
        geoQuery.addGeoQueryEventListener(HomePageActivity.this);

    }

    //if user has count to send help display button
    public void databaseProcess(){

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hascount = dataSnapshot.child("Users").child(mUser.getUid()).getValue(User.class).getHelpCount();
                if(hascount == 0 || hascount <0)
                    helpButton.setVisibility(View.GONE);

                helpList.clear();
                for(DataSnapshot ds: dataSnapshot.child("Help Post").getChildren()){
                    for(int i = 0 ; i< userPostKeyList.size(); i++){
                        if(userPostKeyList.get(i).equals(ds.getKey())){
                            helpList.add(ds.getValue(Help.class));
                            Log.d(TAG,"key "+ds.getKey()+"usrlist "+userPostKeyList.size());
                        }
                    }


                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    /*who is in the area*/
    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        //içine girenler post idelerini döndürüyor
        if(!userPostKeyList.contains(key)){
            userPostKeyList.add(key);
            databaseProcess();
        }
    }

    /*who is out of the area*/
    @Override
    public void onKeyExited(String key) {
        //içinden çıkanlar
        userPostKeyList.remove(key);
        databaseProcess();
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        //Değişenler
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new AlertDialog.Builder(getApplicationContext())
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //go user profile
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_page:
                Intent i = new Intent(getApplicationContext(),UserProfileActivity.class);
                startActivity(i);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_up,menu);
        return super.onCreateOptionsMenu(menu);
    }

}
