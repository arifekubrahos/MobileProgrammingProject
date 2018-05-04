package com.example.arife.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class SignInActivity extends AppCompatActivity {
    private EditText mailText;
    private EditText passwordText;
    private EditText passwordConfirmText;

    private String userMail;
    private String userPassword;
    private String userConfirmPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReferance;
    private static String TAG ="Sign in activity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_activity);

        mailText=findViewById(R.id.mail_editText);
        passwordText = findViewById(R.id.password_editText);
        passwordConfirmText = findViewById(R.id.confirm_password_editText);

        //database kullancı bağlantısı
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    public void SignIn(View v){

        userMail = mailText.getText().toString();
        userPassword = passwordText.getText().toString();
        userConfirmPassword = passwordConfirmText.getText().toString();

        if(userPassword.equals(userConfirmPassword)){
            mAuth.createUserWithEmailAndPassword(userMail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }
        else{
            Toast.makeText(getApplicationContext(),"şifreler uyumlu değil",Toast.LENGTH_LONG).show();
        }
    }

    private void updateUI(FirebaseUser user) {
        //burası ne yapacağına karar vereceğin yer
        if(user != null){
            mDatabaseReferance = FirebaseDatabase.getInstance().getReference("Users");
            User newUser = new User();
            newUser.setName(user.getDisplayName());
            newUser.setMail(user.getEmail());
            newUser.setImage(user.getPhotoUrl().toString());
            newUser.setDailyCount(-1);
            mDatabaseReferance.child(user.getUid()).setValue(newUser);

            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}
