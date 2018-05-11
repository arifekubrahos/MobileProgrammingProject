package com.example.arife.mobileprogrammingproject;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 * Created by Arife on 10.05.2018.
 */

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.MyViewHolder>{

    private static final String TAG = "ADAPTER FOR CREATE HELP" ;
    private List<Help> helpList;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference dbREf;
    private FirebaseUser mUser;  //bunu aynı kullanıcı olmasın diye kullnıcaz

    public MainPageAdapter(List<Help> helpList){
        this.helpList = helpList;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView nameText, contentText,titleText, descText;
        private Button helpButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameText =itemView.findViewById(R.id.nameHelpText);
            contentText =itemView.findViewById(R.id.contentHelpText);
            titleText =itemView.findViewById(R.id.titleHelpText);
            descText =itemView.findViewById(R.id.descHelpText);
            helpButton = itemView.findViewById(R.id.helpButton);
        }

        /*istek gönderme!*/
        @Override
        public void onClick(View view) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
            mDatabaseReference = FirebaseDatabase.getInstance().getReference();
            dbREf = FirebaseDatabase.getInstance().getReference().child("Help Request");

            mDatabaseReference.child("Help Post").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        Help help  = ds.getValue(Help.class);
                        //UID kullanıcı eşit olamayacak unutma
                        //help postların içinde görüntü olarak getiriğimiz sıradaki göndeririyi arıyoruz
                        if(help.getName().equals(helpList.get(getAdapterPosition()).getName())&&
                                help.getContent().equals(helpList.get(getAdapterPosition()).getContent())&&
                                help.getTitle().equals(helpList.get(getAdapterPosition()).getTitle()) &&
                                help.getDescription().equals(helpList.get(getAdapterPosition()).getDescription())){

                            String key = ds.getKey();
                            Map<String, String> userRequest = new HashMap<String, String>();
                            userRequest.put("user",mUser.getUid());
                            dbREf.child(key).setValue(userRequest);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,"DATABASE HATASI OLUŞTU");
                }
            });


        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.help_card,parent,false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        Help h = helpList.get(position);

        holder.nameText.setText(h.getName());
        holder.contentText.setText(h.getContent());
        holder.titleText.setText(h.getTitle());
        holder.descText.setText(h.getDescription());

        //burada database check etmemiz lazım hala eğer, istek göndermişe bu şekilde olacak
        holder.helpButton.setBackgroundColor(R.string.button_inactive);
        holder.helpButton.setTextColor(R.string.button_inactive_text);
        holder.helpButton.setText("İstek Gönderildi");
        holder.helpButton.setEnabled(false);
        holder.helpButton.setPressed(true);
    }


    @Override
    public int getItemCount() {
        return helpList.size();
    }
}
