package com.example.arife.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Users who want to help and send a request certain help post current user checked
 */

public class UserRequestActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "USER REQUEST";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    private DatabaseReference mDatabaseRef;

    private List<String> userKeyList = new ArrayList<>();
    private List<User>   userList = new ArrayList<>();
    private String helpPostKey;
    private Toolbar etoolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);

        etoolbar = findViewById(R.id.tool_bar_request);
        etoolbar.setNavigationIcon(R.drawable.left_arrow);
        etoolbar.setNavigationOnClickListener(this);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            helpPostKey = bundle.getString("key list");

        }
        databaseProcess();

        recyclerView = findViewById(R.id.helpRequestRecyler);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UserRequestAdapter(userList,userKeyList,helpPostKey, getApplicationContext());
        recyclerView.setAdapter(adapter);
    }
    //return request
    public void databaseProcess(){
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.child("Help Request").getChildren()) {
                    if (helpPostKey.equals(d.getKey())) {
                        userKeyList.add(d.child("user").getValue().toString());
                        Log.d(TAG,"SLAS"+d.child("user").getValue().toString());
                    }
                }
                for(DataSnapshot ds: dataSnapshot.child("Users").getChildren()){
                    for(int i=0; i<userKeyList.size(); i++){
                        if(userKeyList.get(i).equals(ds.getKey())){
                            userList.add(ds.getValue(User.class));
                            Log.d(TAG,"key list "+ds.getKey());
                        }
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });

    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(), UserPostActivity.class));
    }
}
