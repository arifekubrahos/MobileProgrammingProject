package com.example.arife.mobileprogrammingproject;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Arife on 4.05.2018.
 */

public class SignUpActivity extends Fragment implements View.OnClickListener {
    private EditText mailText;
    private EditText passwordText;
    private EditText passwordConfirmText;
    private Button signUpButton;

    private String userMail;
    private String userPassword;
    private String userConfirmPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReferance;
    private static String TAG ="Sign in activity";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //container değişecek attım şimdilik
        View v = inflater.inflate(R.layout.signup_activity,container,false);

        mailText=v.findViewById(R.id.mailUp_editText);
        passwordText = v.findViewById(R.id.passwordUp_editText);
        passwordConfirmText = v.findViewById(R.id.confirm_password_editText);
        signUpButton = v.findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(this);
        //database kullancı bağlantısı
        mAuth = FirebaseAuth.getInstance();

        return v;
    }


    private void updateUI(FirebaseUser user) {
        //burası ne yapacağına karar vereceğin yer
        if(user != null){
            mDatabaseReferance = FirebaseDatabase.getInstance().getReference("Users");
            User newUser = new User();
            newUser.setName(user.getDisplayName());
            newUser.setMail(user.getEmail());
            newUser.setDailyCount(-1);
            mDatabaseReferance.child(user.getUid()).setValue(newUser);

            //Intent i = new Intent(getActivity(),MainActivity.class);
            //startActivity(i);
            //finish();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.signup_button){
            userMail = mailText.getText().toString();
            userPassword = passwordText.getText().toString();
            userConfirmPassword = passwordConfirmText.getText().toString();

            if(userPassword.equals(userConfirmPassword)){
                mAuth.createUserWithEmailAndPassword(userMail, userPassword)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(getActivity(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                            }
                        });
            }
            else{
                Toast.makeText(getActivity(),"şifreler uyumlu değil",Toast.LENGTH_LONG).show();
            }
        }

    }
}
