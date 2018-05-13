package com.example.arife.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Arife on 12.05.2018.
 */

class UserProfileActivity extends AppCompatActivity {

    private FirebaseUser mUser;

    private TextView nameText;
    private TextView mailText;

    private Toolbar btoolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile_activity);

        btoolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(btoolbar);

        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        nameText =  findViewById(R.id.nameText);
        mailText =  findViewById(R.id.mailText);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mUser != null){
            nameText.setText(mUser.getDisplayName());
            mailText.setText(mUser.getEmail());

        }
        else{
            Toast.makeText(getApplicationContext(),"Bilgiler yüklenirken hata!",Toast.LENGTH_LONG).show();
        }
    }

    public void goPostClick(View v){
        finish();
    }
    public void signOutClick(View v){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
