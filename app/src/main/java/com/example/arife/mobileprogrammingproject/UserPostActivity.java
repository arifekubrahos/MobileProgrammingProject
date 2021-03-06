package com.example.arife.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
 * User help post getting into listview
 */

public class UserPostActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;
    private List<String> helpList = new ArrayList<>();
    private List<String> keyList = new ArrayList<>();

    private ListView listView;
    private Toolbar dtoolbar;
    private ArrayAdapter<String> helpArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_post);

        listView = findViewById(R.id.list_item);
        dtoolbar = findViewById(R.id.tool_bar_post);
        dtoolbar.setNavigationIcon(R.drawable.left_arrow);
        dtoolbar.setNavigationOnClickListener(this);


        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        helpList.add("");
        keyList.add("");
        databaseProcess();

        helpArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.help_post,R.id.content,helpList);
        listView.setAdapter(helpArrayAdapter);
        listView.setOnItemClickListener(this);

    }

    //return help
    public void databaseProcess(){
        mDatabaseRef.child("Help Post").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                helpList.clear();
                keyList.clear();
                for(DataSnapshot d : dataSnapshot.getChildren()){
                    if(mUser.getUid().equals(d.getValue(Help.class).getuId())){
                        helpList.add(d.getValue(Help.class).getContent()+"\n"+d.getValue(Help.class).getDescription());
                        keyList.add(d.getKey());
                    }

                }
                helpArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getApplicationContext(), UserRequestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("key list",keyList.get(i));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getApplicationContext(), UserProfileActivity.class));
    }
}
