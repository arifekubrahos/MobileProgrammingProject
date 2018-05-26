package com.example.arife.mobileprogrammingproject;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for designing each post and add spesific feature
 *          add MyClickListener for control button event
 *          MyViewHolder class create View and button listener
 *
 */

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.MyViewHolder>{

    private static final String TAG = "ADAPTER FOR CREATE HELP" ;
    private List<Help> helpList;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;  //bunu aynı kullanıcı olmasın diye kullnıcaz

    public HomePageAdapter(List<Help> helpList){
        this.helpList = helpList;
    }

    //add click listener and specify button event
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView nameText, contentText,titleText, descText;
        private Button helpButton;
        private MyClickListener listener;



        public MyViewHolder(View itemView, MyClickListener listener) {
            super(itemView);
            this.listener = listener;
            nameText =itemView.findViewById(R.id.nameHelpText);
            contentText =itemView.findViewById(R.id.contentHelpText);
            titleText =itemView.findViewById(R.id.titleHelpText);
            descText =itemView.findViewById(R.id.descHelpText);
            helpButton = itemView.findViewById(R.id.helpButton);

            helpButton.setOnClickListener(this);
        }

        /*istek gönderme!*/
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.helpButton:
                    listener.onEdit(this.getLayoutPosition(), helpButton);
                    break;
                default:
                    break;
            }

        }
    }

    //implement listerner method and return view
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.help_card,parent,false);
         MyViewHolder holder = new MyViewHolder(v, new MyClickListener() {
            @Override
            public void onEdit(final int p,Button helpButton ){
                helpButton.setText("İstek Gönderiliyor...");
                helpButton.setEnabled(false);
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                dataProcess(p ,helpButton);


            }

             @Override
             public void onAccept(int p) {
             }

             @Override
             public void onReject(int p) {
             }
         });
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {


        Help h = helpList.get(position);

        holder.nameText.setText(h.getName());
        holder.contentText.setText(h.getContent());
        holder.titleText.setText(h.getTitle());
        holder.descText.setText(h.getDescription());




    }

    //if user click send request
    public void dataProcess(final int p, final Button helpButton){
        mDatabaseReference.child("Help Post").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Help help = ds.getValue(Help.class);

                    if (help.getContent().equals(helpList.get(p).getContent()) &&
                            help.getTitle().equals(helpList.get(p).getTitle()) &&
                            help.getDescription().equals(helpList.get(p).getDescription())) {
                        Map<String , String > map = new HashMap<>();
                        map.put("user",mUser.getUid());
                        String key = ds.getKey();
                        mDatabaseReference.child("Help Request").child(key).setValue(map);

                        helpButton.setBackgroundColor(R.string.button_inactive);
                        helpButton.setTextColor(R.string.button_inactive_text);
                        helpButton.setText("İstek Gönderildi");
                        helpButton.setEnabled(false);
                    }

                    //


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "DATABASE HATASI OLUŞTU");
            }
        });
    }


    @Override
    public int getItemCount() {
        return helpList.size();
    }

}
