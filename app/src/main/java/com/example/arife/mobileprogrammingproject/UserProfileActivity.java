package com.example.arife.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * User profile
 */

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseUser mUser;

    private TextView nameText;
    private TextView mailText;

    private Toolbar btoolbar;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        btoolbar = findViewById(R.id.tool_bar_profile);
        btoolbar.setNavigationIcon(R.drawable.left_arrow);
        btoolbar.setNavigationOnClickListener(this);

        nameText =  findViewById(R.id.nameText);
        mailText =  findViewById(R.id.mailText);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.getChildren())
                    if(d.getKey().equals(mUser.getUid()))
                        nameText.setText(d.getValue(User.class).getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(mUser != null){
            mailText.setText(mUser.getEmail());

        }
        else{
            Toast.makeText(getApplicationContext(),"Bilgiler yüklenirken hata!",Toast.LENGTH_LONG).show();
        }
    }

    public void goPostClick(View v){
        startActivity(new Intent(getApplicationContext(), UserPostActivity.class));
        finish();
    }
    public void signOutClick(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
    }
}
