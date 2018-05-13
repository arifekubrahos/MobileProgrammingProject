package com.example.arife.mobileprogrammingproject;

import android.nfc.Tag;
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
    private FirebaseUser mUser;  //bunu aynı kullanıcı olmasın diye kullnıcaz

    public MainPageAdapter(List<Help> helpList){
        this.helpList = helpList;
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView nameText, contentText,titleText, descText;
        private Button helpButton;
        private MyClickListener listener;

        public interface MyClickListener {
            void onEdit(int p, Button helpButton);
        }

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
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.help_card,parent,false);
         MyViewHolder holder = new MyViewHolder(v, new MyViewHolder.MyClickListener() {
            @Override
            public void onEdit(final int p,Button helpButton ){
                helpButton.setText("İstek Gönderiliyor...");
                helpButton.setEnabled(false);
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                dataProcess(p ,helpButton);


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

    public void dataProcess(final int p, final Button helpButton){
        mDatabaseReference.child("Help Post").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Help help = ds.getValue(Help.class);

                    if (help.getContent().equals(helpList.get(p).getContent()) &&
                            help.getTitle().equals(helpList.get(p).getTitle()) &&
                            help.getDescription().equals(helpList.get(p).getDescription())) {

                        String key = ds.getKey();
                        Map<String, String> userRequest = new HashMap<String, String>();
                        userRequest.put("user", mUser.getUid());
                        mDatabaseReference.child("Help Request").child(key).setValue(userRequest);

                        helpButton.setBackgroundColor(R.string.button_inactive);
                        helpButton.setTextColor(R.string.button_inactive_text);
                        helpButton.setText("İstek Gönderildi");
                        helpButton.setEnabled(false);
                    }

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
