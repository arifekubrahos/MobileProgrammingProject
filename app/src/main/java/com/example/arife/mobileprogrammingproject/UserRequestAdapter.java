package com.example.arife.mobileprogrammingproject;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Adapter for designing each request
 *         add MyClickListener for button events
 *
 */

public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.MyViewHolder>{

    private static final String TAG = "REQUEST ADAPETER " ;
    private List<User> userList = new ArrayList<>();
    private List<String> userKeyList = new ArrayList<>();
    private Context mContext;
    private String helpPostKey;
    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public UserRequestAdapter(List<User> userList,List<String> userKeyList,String helpPostKey, Context mContext){
        this.userKeyList = userKeyList;
        this.userList = userList;
        this.mContext = mContext;
        this.helpPostKey = helpPostKey;
    }
    //implement MyCLickListener method
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.help_request,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(v, new MyClickListener() {
            @Override
            public void onEdit(int p, Button helpButton) {
            }

            @Override
            public void onAccept(int p) {
                databaseProcess(p, true);
            }

            @Override
            public void onReject(int p) {
                databaseProcess(p, false);
            }
        });

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.nameText.setText(userList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //if user accept add one count of user who want to help, reject delete request
    public void databaseProcess(final int p, final boolean which){

        if(which){
            Log.d(TAG,""+userKeyList.get(p));
            Intent intent = new Intent(mContext, MapsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("strings",new String[]{userKeyList.get(p),helpPostKey});
            mContext.startActivity(intent);
        }
        if(!which){
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(!which){
                        for(DataSnapshot ds: dataSnapshot.child("Help Request").getChildren()){
                            if(userKeyList.get(p).equals(ds.child("user").getValue().toString())){
                                ds.child("user").getRef().removeValue();
                            }
                        }

                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            notifyDataSetChanged();
        }


    }

    //add button to listener
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView nameText;
        private Button acceptButton,rejectButton;
        private MyClickListener listener;


        public MyViewHolder(View itemView, MyClickListener listener) {
            super(itemView);
            this.listener = listener;
            nameText =itemView.findViewById(R.id.nameText);
            acceptButton =itemView.findViewById(R.id.acceptButton);
            rejectButton =itemView.findViewById(R.id.rejectButton);

            acceptButton.setOnClickListener(this);
            rejectButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.acceptButton:
                    listener.onAccept(this.getLayoutPosition());
                    break;
                case R.id.rejectButton:
                    listener.onReject(this.getLayoutPosition()  );
                default:
                    break;
            }
        }
    }
}
