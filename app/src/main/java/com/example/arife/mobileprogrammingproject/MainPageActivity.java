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
import android.widget.Toast;


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
 * Created by Arife on 8.05.2018.
 */

public class MainPageActivity extends AppCompatActivity implements GeoQueryEventListener {

    private static final String TAG ="MAİN PAGE ACTİVİTY";
    private FusedLocationProviderClient mFusedLocationClient; //location APIs ffrom google api
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 34;

    private TextView textView;

    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;
    private int hascount;

    private GeoLocation userGeolocation;
    private GeoFire userGeofire; //userlarını içinden bizimkini bulacaz
    private GeoFire helpGeofire; // locationları kontrol ettiğimiz yerler
    private GeoLocation userGeoLocation; //user anlık konumunu aldığımız yer

    private GeoQuery geoQuery;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private FloatingActionButton helpButton;
    private Toolbar cToolbar;

    private List<Help> helpList =new ArrayList<Help>(); //Bu listeyi adaptera göndericez onkeyexistde almamız gerek
    private List<String> userList = new ArrayList<>();//hangi userlar yakında kullanıcı idleri

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage_activity);

        cToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(cToolbar);

        textView = findViewById(R.id.text);
        recyclerView = findViewById(R.id.dailyRecyclerView);

        //liste adapterı oluşturma yeri
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MainPageAdapter(helpList);
        recyclerView.setAdapter(adapter);
        helpButton = findViewById(R.id.helpButton);
        helpButton.setVisibility(View.VISIBLE);

        //database bağlantısı sağlayarak kullanıcıyı konumunu ve postların konumlarını alıyoruz
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, mUser.getUid());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseProcess();
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
            display();
        }


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
        if (ActivityCompat.checkSelfPermission(MainPageActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainPageActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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
    public void display(){

        userGeofire.getLocation(mUser.getUid(), new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if(location!= null){
                    userGeoLocation = new GeoLocation(location.latitude, location.longitude);

                    //radious in km, bunu değiştirilebilir yap!
                    geoQuery = helpGeofire.queryAtLocation(userGeoLocation,10);
                    geoQuery.addGeoQueryEventListener(MainPageActivity.this);
                }
                else{
                    Log.e(TAG,"user location null bulunamadı");
                }
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
        userList.add(key);
        databaseProcess();
    }

    /*who is out of the area*/
    @Override
    public void onKeyExited(String key) {
        //içinden çıkanlar
        userList.remove(key);
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

    public void databaseProcess(){

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hascount = dataSnapshot.child("Users").child(mUser.getUid()).getValue(User.class).getHelpCount();
                if(hascount == 0)
                 helpButton.setVisibility(View.GONE);

                helpList.clear();
                for(DataSnapshot ds: dataSnapshot.child("Help Post").getChildren()){
                    for(int i = 0 ; i< userList.size(); i++){
                        if(userList.get(i).equals(ds.getKey())){
                            helpList.add(ds.getValue(Help.class));
                            Log.d(TAG,"key "+ds.getKey()+"usrlist "+userList.size());
                        }
                    }
                    adapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.profile_page:
                Intent i = new Intent(getApplicationContext(),UserProfileActivity.class);
                startActivity(i);
                finish();
                break;
            case R.id.search:

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
