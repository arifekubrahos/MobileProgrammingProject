package com.example.arife.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * User add new help post, count decreased
 */

public class CreateHelpActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG ="CREATE PAGE ACTİVİTY";

    private GeoFire userGeofire;
    private GeoFire helpGeofire;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;

    private Spinner contentSpinner;
    private EditText titleText;
    private EditText descText;

    private TextInputLayout errTitleText;
    private TextInputLayout errDescText;
    String keys;

    private Help newHelp;
    private Toolbar btoolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_create);

        btoolbar = findViewById(R.id.tool_bar_create);
        btoolbar.setNavigationIcon(R.drawable.left_arrow);
        btoolbar.setNavigationOnClickListener(this);
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
    //error check on layout
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
            Log.e(TAG,"BEKLENMEYEN HATA");
    }

    private void save() {

        keys = mDatabaseReference.child("Help Post").push().getKey();

        userGeofire.getLocation(mUser.getUid(), new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if(location != null){
                    helpGeofire.setLocation(keys,location);
                    newHelp= new Help();
                    newHelp.setuId(mUser.getUid());
                    newHelp.setContent(contentSpinner.getSelectedItem().toString());
                    newHelp.setTitle(titleText.getText().toString());
                    newHelp.setDescription(descText.getText().toString());
                    databaseProcess();

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

    //user count reduced one
    public void databaseProcess(){
        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newHelp.setName(String.valueOf(dataSnapshot.child(mUser.getUid()).child("name").getValue()));
                mDatabaseReference.child("Help Post").child(keys).setValue(newHelp);
                int count = Integer.parseInt(String.valueOf(dataSnapshot.child(mUser.getUid()).child("helpCount").getValue())) ;
                mDatabaseReference.child("Users").child(mUser.getUid()).child("helpCount").setValue(count-1);
                startActivity(new Intent(getApplicationContext(),HomePageActivity.class));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(),HomePageActivity.class));
    }
}
