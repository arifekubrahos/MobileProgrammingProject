package com.example.arife.mobileprogrammingproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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
 * Created by Arife on 13.05.2018.
 */

public class UserRequestActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;

    private List<String> keyList = new ArrayList<>();
    private List<User>   userList = new ArrayList<>();
    private String content;

    private Toolbar ctoolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_request_activity);

        ctoolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(ctoolbar);

        if(getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = getIntent().getExtras();

        userList.add(new User());
        if(bundle != null){
            content = bundle.getString("key list");

        }

    }
    public void databaseProcess(){
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot d: dataSnapshot.child("Help Request").getChildren()){
                    if(content.equals(d.getKey())){
                        keyList.add(d.child("user").getValue().toString());
                        for(DataSnapshot ds: dataSnapshot.child("Users").getChildren()){
                            if(d.child("user").getValue().toString().equals(ds.getKey())){
                                userList.add(ds.getValue(User.class));
                            }
                        }
                        mDatabaseRef.child("User").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds: dataSnapshot.getChildren()){


                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });

    }
}
