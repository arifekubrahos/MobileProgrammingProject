package com.example.arife.mobileprogrammingproject;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Arife on 10.05.2018.
 */

public class CreateHelpActivity extends AppCompatActivity
{

    private static final String TAG ="CREATE PAGE ACTİVİTY";

    private GeoLocation helpLocation;
    private GeoFire userGeofire;
    private GeoFire helpGeofire;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;

    private Spinner contentSpinner;
    private EditText titleText;
    private EditText descText;

    private TextInputLayout errTitleText;
    private TextInputLayout errDescText;

    private Help newHelp;
    private Toolbar btoolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_create_activity);

        btoolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(btoolbar);

        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        /*database e ekleme yapıcaz*/
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        helpGeofire = new GeoFire(mDatabaseReference.child("Help Location"));
        userGeofire = new GeoFire(mDatabaseReference.child("Users Location"));

        contentSpinner =  findViewById(R.id.contentSnipper);
        titleText = findViewById(R.id.titleText);
        descText =  findViewById(R.id.descText);
        errTitleText =  findViewById(R.id.errorTitleText);
        errDescText =  findViewById(R.id.errorDescText);


    }

    public void helpClick(View v){
        if (titleText.getText().toString().equals("")) {
            errTitleText.setError("Lütfen başlık giriniz!");
            Log.e(TAG,"TİTLE BOŞ");
        }
        else if (descText.getText().toString().equals("")) {
            errDescText.setError("Lütfen açıklamak giriniz!");
            Log.e(TAG,"DESC BOŞ");
        }
        else if (String.valueOf(contentSpinner.getSelectedItem()).equals("")) {
            Log.e(TAG,"SPİNNER BOŞ");
        }
        else if (!titleText.getText().toString().equals("")&&!descText.getText().toString().equals("")){
            save();

        }
        else
            Log.e(TAG,"BEKLENMEYEN HATA"+titleText.getText().toString()+descText.getText().toString()+String.valueOf(contentSpinner.getSelectedItem())+helpLocation);
    }

    private void save() {

        final String keys = mDatabaseReference.child("Help Post").push().getKey();

        userGeofire.getLocation(mUser.getUid(), new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if(location != null){
                    helpGeofire.setLocation(keys,location);
                    newHelp= new Help();
                    newHelp.setuId(mUser.getUid());
                    newHelp.setName(mUser.getDisplayName());
                    newHelp.setContent(contentSpinner.getSelectedItem().toString());
                    newHelp.setTitle(titleText.getText().toString());
                    newHelp.setDescription(descText.getText().toString());
                    mDatabaseReference.child("Help Post").child(keys).setValue(newHelp);
                }
                else{
                    //başarılı kayır olmadı
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG,"error to database");
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
